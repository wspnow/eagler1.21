/*
 * Copyright (c) 2022-2024 lax1dude. All Rights Reserved.
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

import com.mojang.blaze3d.vertex.PoseStack;
import net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU;
import net.lax1dude.eaglercraft.v1_8.opengl.InstancedFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;
import java.util.function.Function;

/**
 * Copyright (c) 2022-2024 lax1dude (Eagler Network Extensions)
 */
public class EaglerFont extends Font {

    private static final ResourceLocation DEFAULT_FONT = new ResourceLocation("minecraft:default");
    private final InstancedFont fontRenderer;
    private final Random random = new Random();
    private float posX = 0.0f;
    private float posY = 0.0f;
    private int textColor = 0xFFFFFFFF;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean strikethrough = false;
    private boolean obfuscated = false;

    public static Font createSupportedFont(Function<ResourceLocation, net.minecraft.client.gui.font.FontSet> fontManager, boolean filterFishyGlyphs) {
        if (EaglercraftGPU.checkInstancingCapable()) {
            return new EaglerFont(fontManager, filterFishyGlyphs);
        } else {
            return new Font(fontManager, filterFishyGlyphs);
        }
    }

    public EaglerFont(Function<ResourceLocation, net.minecraft.client.gui.font.FontSet> fontManager, boolean filterFishyGlyphs) {
        super(fontManager, filterFishyGlyphs);
        this.fontRenderer = new InstancedFont();
    }

    public int draw(PoseStack poseStack, String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) {
            this.posX = x;
            this.posY = y;
            return 0;
        }
        Style style = Style.EMPTY;
        if (bold) style = style.withBold(true);
        if (italic) style = style.withItalic(true);
        if (strikethrough) style = style.withStrikethrough(true);
        if (underline) style = style.withUnderlined(true);
        if (obfuscated) {
            text = obfuscateText(text);
        }
        Component component = Component.literal(text).withStyle(style);
        return super.drawInBatch(
            component,
            x,
            y,
            color,
            false,
            poseStack.last().pose(),
            MultiBufferSource.immediate(new ByteBufferBuilder(786432)),
            Font.DisplayMode.NORMAL,
            15728880,
            0,
            true
        );
    }

    public int renderString(String text, float x, float y, int color, boolean dropShadow, PoseStack poseStack, MultiBufferSource buffer, boolean transparent, int packedLight, int packedOverlay) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        Style style = Style.EMPTY;
        if (bold) style = style.withBold(true);
        if (italic) style = style.withItalic(true);
        if (strikethrough) style = style.withStrikethrough(true);
        if (underline) style = style.withUnderlined(true);
        if (obfuscated) {
            text = obfuscateText(text);
        }
        Component component = Component.literal(text).withStyle(style);
        return super.drawInBatch(
            component,
            x,
            y,
            color,
            dropShadow,
            poseStack.last().pose(),
            buffer,
            Font.DisplayMode.NORMAL,
            packedLight,
            packedOverlay,
            true
        );
    }

    public int width(String text) {
        if (text == null) {
            return 0;
        }
        return super.width(text);
    }

    public int getStringWidth(String text) {
        return this.width(text);
    }

    // Helper method to get color from format code
    private int getColorFromFormatCode(int code) {
        // Map format codes to RGB colors (same as vanilla Minecraft)
        switch (code) {
            case 0:
                return 0x000000; // Black
            case 1:
                return 0x0000AA; // Dark Blue
            case 2:
                return 0x00AA00; // Dark Green
            case 3:
                return 0x00AAAA; // Dark Aqua
            case 4:
                return 0xAA0000; // Dark Red
            case 5:
                return 0xAA00AA; // Dark Purple
            case 6:
                return 0xFFAA00; // Gold
            case 7:
                return 0xAAAAAA; // Gray
            case 8:
                return 0x555555; // Dark Gray
            case 9:
                return 0x5555FF; // Blue
            case 10:
                return 0x55FF55; // Green
            case 11:
                return 0x55FFFF; // Aqua
            case 12:
                return 0xFF5555; // Red
            case 13:
                return 0xFF55FF; // Light Purple
            case 14:
                return 0xFFFF55; // Yellow
            case 15:
                return 0xFFFFFF; // White
            default:
                return 0xFFFFFF; // Default to white
        }
    }

    // Helper method to obfuscate text
    private String obfuscateText(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != ' ' && chars[i] != 0) {
                // Replace with a random character from the font
                chars[i] = (char) (0x2580 + random.nextInt(64));
            }
        }
        return new String(chars);
    }
}