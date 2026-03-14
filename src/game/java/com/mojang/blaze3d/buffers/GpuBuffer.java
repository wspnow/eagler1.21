package com.mojang.blaze3d.buffers;

import com.mojang.blaze3d.platform.GlStateManager;
//import com.mojang.jtracy.MemoryPool;
//import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GpuBuffer implements AutoCloseable {
    private final BufferType type;
    private final BufferUsage usage;
    private boolean closed;
    private boolean initialized = false;
    public final int handle;
    public int size;

    public GpuBuffer(BufferType type, BufferUsage usage, int size) {
        this.type = type;
        this.usage = usage;
        this.size = size;
        this.handle = GlStateManager._glGenBuffers();
    }

    public GpuBuffer(BufferType type, BufferUsage usage, ByteBuffer data) {
        this(type, usage, data.remaining());
        this.write(data, 0);
    }

    public void resize(int newSize) {
        if (this.closed) {
            throw new IllegalStateException("Buffer already closed");
        }
        this.size = newSize;
        this.bind();
        GlStateManager._glBufferData(this.type.id, (long)newSize, this.usage.id);
        this.initialized = true;
    }

    public void write(ByteBuffer data, int offset) {
        if (this.closed) {
            throw new IllegalStateException("Buffer already closed");
        }
        if (!this.usage.writable) {
            throw new IllegalStateException("Buffer is not writable");
        }
        int i = data.remaining();
        if (i + offset > this.size) {
            throw new IllegalArgumentException("Cannot write more data than this buffer can hold");
        }
        this.bind();
        if (!this.initialized) {
            GlStateManager._glBufferData(this.type.id, (long)this.size, this.usage.id);
            this.initialized = true;
        }
        GlStateManager._glBufferSubData(this.type.id, offset, data);
    }

    @Nullable
    public ReadView read() {
        return read(0, this.size);
    }

    @Nullable
    public ReadView read(int offset, int length) {
        if (this.closed) {
            throw new IllegalStateException("Buffer already closed");
        }
        if (!this.usage.readable) {
            throw new IllegalStateException("Buffer is not readable");
        }
        if (offset + length > this.size) {
            throw new IllegalArgumentException("Cannot read more data than this buffer can hold");
        }
        this.bind();
        ByteBuffer mapped = GlStateManager._glMapBufferRange(this.type.id, offset, length, 1);
        return mapped == null ? null : new ReadView(this.type.id, mapped);
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            GlStateManager._glDeleteBuffers(this.handle);
        }
    }

    public void bind() {
        GlStateManager._glBindBuffer(this.type.id, this.handle);
    }

    @OnlyIn(Dist.CLIENT)
    public static class ReadView implements AutoCloseable {
        private final int target;
        private final ByteBuffer data;

        public ReadView(int target, ByteBuffer data) {
            this.target = target;
            this.data = data;
        }

        public ByteBuffer data() {
            return this.data;
        }

        @Override
        public void close() {
            GlStateManager._glUnmapBuffer(this.target);
        }
    }
}