/*
 * Copyright (c) 2024 lax1dude. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package net.lax1dude.eaglercraft.v1_8.minecraft;

import net.lax1dude.eaglercraft.v1_8.Mouse;
import net.lax1dude.eaglercraft.v1_8.internal.EnumCursorType;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;

public class ButtonWithStupidIcons extends AbstractButton {
    protected ResourceLocation leftIcon;
    protected float leftIconAspect;
    protected ResourceLocation rightIcon;
    protected float rightIconAspect;
    protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    protected final OnPress onPress;
    protected boolean selected;

    public static ButtonWithStupidIcons.Builder builder(Component message, OnPress onPress) {
        return new ButtonWithStupidIcons.Builder(message, onPress);
    }

    protected ButtonWithStupidIcons(int x, int y, int width, int height, Component message, 
            ResourceLocation leftIcon, float leftIconAspect, ResourceLocation rightIcon, float rightIconAspect,
            OnPress onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.leftIcon = leftIcon;
        this.leftIconAspect = leftIconAspect;
        this.rightIcon = rightIcon;
        this.rightIconAspect = rightIconAspect;
    }

    public ResourceLocation getLeftIcon() { 
        return leftIcon; 
    }
    
    public ResourceLocation getRightIcon() { 
        return rightIcon; 
    }
    
    public void setLeftIcon(ResourceLocation leftIcon, float aspectRatio) {
        this.leftIcon = leftIcon;
        this.leftIconAspect = aspectRatio;
    }
    
    public void setRightIcon(ResourceLocation rightIcon, float aspectRatio) {
        this.rightIcon = rightIcon;
        this.rightIconAspect = aspectRatio;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        this.isHovered = isMouseOver(mouseX, mouseY);
        
        if (this.active && this.isHovered) {
            Mouse.showCursor(EnumCursorType.HAND);
        }
        
        // Draw button background
        ResourceLocation resourcelocation = WIDGETS_LOCATION;
        
        // Draw text
        int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
        if (this.isHovered) {
            textColor = 0xFFFFA0; // Lighter color when hovered
        }
        
        guiGraphics.drawCenteredString(minecraft.font, getMessage(), 
                this.getX() + this.width / 2, 
                this.getY() + (this.height - 8) / 2, textColor);
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public interface OnPress {
        void onPress(ButtonWithStupidIcons button);
    }

    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        private ResourceLocation leftIcon = null;
        private float leftIconAspect = 1.0f;
        private ResourceLocation rightIcon = null;
        private float rightIconAspect = 1.0f;
        private int x = 0;
        private int y = 0;
        private int width = 150;
        private int height = 20;

        public Builder(Component message, OnPress onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public Builder leftIcon(ResourceLocation icon, float aspectRatio) {
            this.leftIcon = icon;
            this.leftIconAspect = aspectRatio;
            return this;
        }

        public Builder rightIcon(ResourceLocation icon, float aspectRatio) {
            this.rightIcon = icon;
            this.rightIconAspect = aspectRatio;
            return this;
        }

        public ButtonWithStupidIcons build() {
            return new ButtonWithStupidIcons(x, y, width, height, message, 
                leftIcon, leftIconAspect, rightIcon, rightIconAspect, onPress);
        }
    }
}