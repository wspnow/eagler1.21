package net.lax1dude.eaglercraft.v1_8.minecraft;

import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.opengl.LevelRenderer;
import net.lax1dude.eaglercraft.v1_8.opengl.LevelVertexBufferUploader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import java.util.*;

// EnumLevelBlockLayer wurde zu RenderType oder RenderLayer in neueren Versionen
// ...existing code...
public class ChunkUpdateManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Object renderCache;

    private int chunkUpdatesTotal = 0;
    private int chunkUpdatesTotalLast = 0;
    private int chunkUpdatesTotalImmediate = 0;
    private int chunkUpdatesTotalImmediateLast = 0;
    private int chunkUpdatesQueued = 0;
    private int chunkUpdatesQueuedLast = 0;
    private long chunkUpdatesTotalLastUpdate = 0l;

    private final SectionRenderDispatcher sectionRenderDispatcher;
    private final net.minecraft.client.renderer.chunk.RenderRegionCache renderRegionCache;
    private final List<SectionRenderDispatcher.RenderSection.CompileTask> queue = new LinkedList<>();

    public ChunkUpdateManager() {
        this.sectionRenderDispatcher = new SectionRenderDispatcher(Minecraft.getInstance());
        this.renderRegionCache = new net.minecraft.client.renderer.chunk.RenderRegionCache();
        this.renderCache = null; // or appropriate initialization if you have a custom class
    }

    public static class EmptyBlockLayerException extends IllegalStateException {
    }

    // Helper interface to handle CompileTask methods
    // Helper class to handle compiled chunks
    private static class CompiledChunkWrapper {
        private final Object compiledChunk;
        
        public CompiledChunkWrapper() {
            this.compiledChunk = new Object();
        }
        
        public boolean isLayerEmpty(RenderType layer) {
            // Default implementation - assumes layer is not empty
            return false;
        }
    }
    
    private interface CompileTaskWrapper {
        void setStatus(String status);
        SectionRenderDispatcher.RenderSection getRenderSection();
        Object getCompiledChunk();
        boolean isLayerEmpty(RenderType layer);
        void rebuildChunk(float x, float y, float z, Object generator);
        void resortTransparency(SectionRenderDispatcher dispatcher);
        BlockPos blockPosition();
        Object getRenderRegionCache();
        boolean canExecuteYet();
        long getTimeout();
        void setTimeout(long time);
        void addFinishRunnable(Runnable runnable);
    }

    // Wrapper implementation
    private CompileTaskWrapper wrapTask(SectionRenderDispatcher.RenderSection.CompileTask task) {
        return new CompileTaskWrapper() {
            private long timeout = 0L;
            private SectionRenderDispatcher.RenderSection section;
            private final List<Runnable> finishRunnables = new ArrayList<>();
            private final Object compiledChunk = new CompiledChunkWrapper();
            
            @Override
            public void setStatus(String status) {
                // No-op in this implementation
            }
            
            @Override
            public SectionRenderDispatcher.RenderSection getRenderSection() {
                if (section == null) {
                    try {
                        section = (SectionRenderDispatcher.RenderSection) task.getClass().getMethod("getSection").invoke(task);
                    } catch (Exception e) {
                        // Fallback to field access if method not found
                        try {
                            java.lang.reflect.Field f = task.getClass().getDeclaredField("renderSection");
                            f.setAccessible(true);
                            section = (SectionRenderDispatcher.RenderSection) f.get(task);
                        } catch (Exception ex) {
                            throw new RuntimeException("Failed to get render section", e);
                        }
                    }
                }
                return section;
            }
            
            @Override
            public boolean canExecuteYet() {
                return true; // Default implementation
            }
            
            @Override
            public long getTimeout() {
                return timeout;
            }
            
            @Override
            public void setTimeout(long time) {
                this.timeout = time;
            }
            
            @Override
            public void addFinishRunnable(Runnable runnable) {
                if (runnable != null) {
                    finishRunnables.add(runnable);
                }
            }

            @Override
            public Object getCompiledChunk() {
                return compiledChunk;
            }

            @Override
            public boolean isLayerEmpty(RenderType layer) {
                return ((CompiledChunkWrapper)compiledChunk).isLayerEmpty(layer);
            }

            @Override
            public void rebuildChunk(float x, float y, float z, Object generator) {
                try {
                    SectionRenderDispatcher.RenderSection section = getRenderSection();
                    if (section != null) {
                        // Try to call rebuildChunk using reflection
                        try {
                            section.getClass().getMethod("rebuildChunk", float.class, float.class, float.class, Object.class)
                                .invoke(section, x, y, z, generator);
                        } catch (NoSuchMethodException e) {
                            // Fallback to alternative method signature if available
                            section.getClass().getMethod("rebuildChunk", float.class, float.class, float.class)
                                .invoke(section, x, y, z);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error rebuilding chunk", e);
                }
            }

            @Override
            public void resortTransparency(SectionRenderDispatcher dispatcher) {
                try {
                    SectionRenderDispatcher.RenderSection section = getRenderSection();
                    if (section != null) {
                        // Try to call the method with the correct signature
                        try {
                            section.getClass().getMethod("resortTransparency", SectionRenderDispatcher.class)
                                .invoke(section, dispatcher);
                        } catch (NoSuchMethodException e) {
                            // Fallback to alternative method signature if available
                            section.getClass().getMethod("resortTransparency")
                                .invoke(section);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error resorting transparency", e);
                }
            }

            @Override
            public BlockPos blockPosition() {
                try {
                    SectionRenderDispatcher.RenderSection section = getRenderSection();
                    if (section != null) {
                        return section.getOrigin();
                    }
                } catch (Exception e) {
                    LOGGER.error("Error getting block position", e);
                }
                return BlockPos.ZERO;
            }

            @Override
            public Object getRenderRegionCache() {
                try {
                    // Try to get the render region cache using reflection
                    java.lang.reflect.Field f = task.getClass().getDeclaredField("renderRegionCache");
                    f.setAccessible(true);
                    return f.get(task);
                } catch (Exception e) {
                    return renderRegionCache;
                }
            }
        };
    }

    private void runGenerator(SectionRenderDispatcher.RenderSection.CompileTask generator, Entity entity) {
        // Set render region cache if method exists
        try {
            generator.getClass().getMethod("setRenderRegionCache", Object.class).invoke(generator, renderRegionCache);
        } catch (Exception e) {
            // Method not available, ignore
        }

        CompileTaskWrapper wrapper = wrapTask(generator);
        float f = (float)entity.getX();
        float f1 = (float)entity.getY();
        float f2 = (float)entity.getZ();

        try {
            // Set initial status
            try {
                generator.getClass().getMethod("setStatus", String.class).invoke(generator, "COMPILING");
            } catch (Exception e) {
                // Method not available, ignore
            }

            SectionRenderDispatcher.RenderSection section = wrapper.getRenderSection();
            if (section != null) {
                // Handle chunk rebuilding and transparency
                wrapper.rebuildChunk(f, f1, f2, generator);
                
                // Handle transparency if needed
                if (!wrapper.isLayerEmpty(RenderType.translucent()) || !wrapper.isLayerEmpty(RenderType.waterMask())) {
                    wrapper.resortTransparency(sectionRenderDispatcher);
                    
                    // Update dirty flag if both layers are empty
                    if (wrapper.isLayerEmpty(RenderType.translucent()) && 
                        wrapper.isLayerEmpty(RenderType.waterMask())) {
                        try {
                            section.getClass().getMethod("setDirty", boolean.class, boolean.class)
                                .invoke(section, false, false);
                        } catch (Exception e) {
                            LOGGER.error("Failed to set chunk dirty flag", e);
                        }
                    }
                }
                
                // Process chunk upload
                processChunkUpload(wrapper, section);
            } else {
                LOGGER.error("Cannot process chunk: section is null");
            }
        } catch (Exception e) {
            LOGGER.error("Error in chunk generation process", e);
        } finally {
            // Always try to set status to DONE
            try {
                generator.getClass().getMethod("setStatus", String.class).invoke(generator, "DONE");
            } catch (Exception e) {
                // Method not available, ignore
            }
        }
    }
    
    private void processChunkUpload(CompileTaskWrapper wrapper, SectionRenderDispatcher.RenderSection section) {
        try {
            final Object compiledChunk = wrapper.getCompiledChunk();
            if (compiledChunk == null) {
                LOGGER.error("Cannot upload chunk: compiled chunk is null");
                return;
            }
            
            Object renderRegionCache = wrapper.getRenderRegionCache();
            if (renderRegionCache == null) {
                LOGGER.error("Cannot upload chunk: render region cache is null");
                return;
            }
            
            // Determine if this is a full chunk or just transparency update
            boolean isFullChunk = compiledChunk.getClass().getName().contains("RenderChunk");
            
            if (isFullChunk) {
                uploadFullChunk(renderRegionCache, section, compiledChunk);
            } else {
                uploadTransparencyLayers(wrapper, renderRegionCache, section, compiledChunk);
            }
        } catch (Exception e) {
            LOGGER.error("Error in chunk upload process", e);
        }
    }
    
    private void uploadFullChunk(Object renderRegionCache, SectionRenderDispatcher.RenderSection section, Object compiledChunk) {
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            try {
                LevelRenderer levelRenderer = new LevelRenderer(1);
                this.uploadChunk(layer, levelRenderer, section, compiledChunk);
            } catch (Exception e) {
                LOGGER.error("Error uploading chunk layer: " + layer, e);
            }
        }
    }
    
    private void uploadTransparencyLayers(CompileTaskWrapper wrapper, Object renderRegionCache, 
                                         SectionRenderDispatcher.RenderSection section, Object compiledChunk) {
        try {
            if (!wrapper.isLayerEmpty(RenderType.translucent())) {
                LevelRenderer levelRenderer = new LevelRenderer(1);
                this.uploadChunk(RenderType.translucent(), levelRenderer, section, compiledChunk);
            }
            if (!wrapper.isLayerEmpty(RenderType.waterMask())) {
                LevelRenderer levelRenderer = new LevelRenderer(1);
                this.uploadChunk(RenderType.waterMask(), levelRenderer, section, compiledChunk);
            }
        } catch (Exception e) {
            LOGGER.error("Error uploading transparency layers", e);
        }
    }

    public boolean updateChunks(long timeout) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity == null) {
            queue.clear();
            chunkUpdatesQueued = 0;
            return false;
        } else {
            boolean flag = false;
            long millis = EagRuntime.steadyTimeMillis();
            List<SectionRenderDispatcher.RenderSection.CompileTask> droppedUpdates = new LinkedList<>();
            while (!queue.isEmpty()) {
                SectionRenderDispatcher.RenderSection.CompileTask generator = queue.remove(0);

                CompileTaskWrapper wrapper = wrapTask(generator);
                if (!wrapper.canExecuteYet()) {
                    if (millis - wrapper.getTimeout() < 60000l) {
                        droppedUpdates.add(generator);
                    }
                    continue;
                }

                runGenerator(generator, entity);
                flag = true;

                ++chunkUpdatesTotal;

                if (timeout < EagRuntime.nanoTime()) {
                    break;
                }
            }
            queue.addAll(droppedUpdates);
            return flag;
        }
    }

    public boolean updateChunkLater(SectionRenderDispatcher.RenderSection section) {
        SectionRenderDispatcher.RenderSection.CompileTask task = section.createCompileTask(renderRegionCache);
        boolean flag = queue.size() < 100;
        if (!flag) {
            task.cancel();
        } else {
            queue.add(task);
            sectionRenderDispatcher.schedule(task);
        }
        return flag;
    }

    public boolean updateChunkNow(SectionRenderDispatcher.RenderSection section) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity != null) {
            runGenerator(section.createCompileTask(renderRegionCache), entity);
            ++chunkUpdatesTotalImmediate;
        }
        return true;
    }

    public void stopChunkUpdates() {
        queue.clear();
        chunkUpdatesQueued = 0;
    }

    public boolean updateTransparencyLater(SectionRenderDispatcher.RenderSection section) {
        if (isAlreadyQueued(section)) {
            return true;
        }
        final SectionRenderDispatcher.RenderSection.CompileTask chunkcompiletaskgenerator = section.createCompileTask(renderRegionCache);
        if (chunkcompiletaskgenerator == null) {
            return true;
        }
        CompileTaskWrapper wrapper = wrapTask(chunkcompiletaskgenerator);
        wrapper.setTimeout(EagRuntime.steadyTimeMillis());
        if (queue.size() < 100) {
            wrapper.addFinishRunnable(new Runnable() {
                @Override
                public void run() {
                    if (queue.remove(chunkcompiletaskgenerator)) {
                        ++chunkUpdatesTotal;
                    }
                }
            });
            queue.add(chunkcompiletaskgenerator);
            ++chunkUpdatesQueued;
            return true;
        } else {
            return false;
        }
    }

    public void uploadChunk(RenderType layer, LevelRenderer chunkRenderer,
            SectionRenderDispatcher.RenderSection section, Object compiledSection) {
        // Simplified upload logic
        try {
            // Try to get display list using reflection if available
            Object displayList = compiledSection.getClass().getMethod("getDisplayList", RenderType.class, SectionRenderDispatcher.RenderSection.class)
                    .invoke(compiledSection, layer, section);
            if (displayList instanceof Integer) {
                this.uploadDisplayList(chunkRenderer, (Integer) displayList, section);
            }
        } catch (Exception e) {
            // Fallback to simple translation
            chunkRenderer.setTranslation(0.0D, 0.0D, 0.0D);
        }
    }

    private void uploadDisplayList(LevelRenderer chunkRenderer, int parInt1, SectionRenderDispatcher.RenderSection section) {
        LevelVertexBufferUploader.uploadDisplayList(parInt1, chunkRenderer);
    }

    public boolean isAlreadyQueued(SectionRenderDispatcher.RenderSection section) {
        for (SectionRenderDispatcher.RenderSection.CompileTask task : queue) {
            try {
                Object taskSection = task.getClass().getMethod("getRenderSection").invoke(task);
                if (taskSection == section) {
                    return true;
                }
            } catch (Exception e) {
                // Ignore and continue
            }
        }
        return false;
    }

    public String getDebugInfo() {
        long millis = EagRuntime.steadyTimeMillis();

        if (millis - chunkUpdatesTotalLastUpdate > 500l) {
            chunkUpdatesTotalLastUpdate = millis;
            chunkUpdatesTotalLast = chunkUpdatesTotal;
            chunkUpdatesTotalImmediateLast = chunkUpdatesTotalImmediate;
            chunkUpdatesTotalImmediate = 0;
            chunkUpdatesTotal = 0;
            chunkUpdatesQueuedLast = chunkUpdatesQueued;
            chunkUpdatesQueued = 0;
        }

        return "Uq: " + (chunkUpdatesTotalLast + chunkUpdatesTotalImmediateLast) + "/"
                + (chunkUpdatesQueuedLast + chunkUpdatesTotalImmediateLast);
    }
}
