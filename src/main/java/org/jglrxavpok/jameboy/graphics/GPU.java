package org.jglrxavpok.jameboy.graphics;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Represents the graphics on the GB (the screen). Does not provide methods to render directly!
 */
public class GPU {

    public static final int WIDTH = 160;
    public static final int HEIGHT = 144;
    public static final int MIN_SPRITE_WIDTH = 8;
    public static final int MAX_SPRITE_WIDTH = 16;
    public static final int SPRITE_HEIGHT = 8;
    public static final int ADDR_OAM_START = 0xFE00;
    public static final int ADDR_OAM_END = 0xFEA0;
    public static final int ADDR_VRAM_START = 0x8000;
    public static final int ADDR_VRAM_END = 0xA000;
    public static final int ADDR_SPRITE_PATTERN_TABLE_START = 0x8000;
    public static final int ADDR_SPRITE_PATTERN_TABLE_END = 0x8FFF+1;
    private final ByteBuffer videoRAM;
    private final ByteBuffer oam;
    private final SpriteBlock[] spriteBlocks;
    private final int[] backgroundColors;
    private final int[] pixels;
    private int stepCount;
    private int lineY;
    private int[] obj0Palette;
    private int[] obj1Palette;
    private int[] backgroundPalette;

    public GPU() {
        videoRAM = ByteBuffer.allocate(8*1024);
        oam = ByteBuffer.allocate(4*40);

        spriteBlocks = new SpriteBlock[40];
        for (int i = 0; i < spriteBlocks.length; i++) {
            spriteBlocks[i] = new SpriteBlock(i*4 + ADDR_OAM_START, this);
        }

        backgroundColors = new int[] {
                0xFFFFFFFF,
                0xFFDDDDDD,
                0xFF808080,
                0xFF000000
        };

        pixels = new int[WIDTH*HEIGHT];

        backgroundPalette = new int[4];
        obj0Palette = new int[4];
        obj1Palette = new int[4];

        System.arraycopy(backgroundColors, 0, backgroundPalette, 0, backgroundColors.length);
        System.arraycopy(backgroundColors, 0, obj0Palette, 0, backgroundColors.length);
        System.arraycopy(backgroundColors, 0, obj1Palette, 0, backgroundColors.length);
    }

    public void write(int index, byte value) {
        if(index == 0xFF47) {
            convertByteToPalette(backgroundPalette, value);
        } else if(index == 0xFF48) {
            convertByteToPalette(obj0Palette, value);
        } else if(index == 0xFF49) {
            convertByteToPalette(obj1Palette, value);
        } else if(index >= ADDR_VRAM_START && index < ADDR_VRAM_END) {
            videoRAM.put(index - ADDR_VRAM_START, value);
        } else if(index >= ADDR_OAM_START && index < ADDR_OAM_END) {
            oam.put(index - ADDR_OAM_START, value);
        } else {
            throw new IllegalArgumentException("Invalid address for GPU: "+Integer.toHexString(index).toUpperCase());
        }
    }

    private void convertByteToPalette(int[] palette, byte value) {
        palette[0] = backgroundColors[value & 0b11];
        palette[1] = backgroundColors[(value >> 2) & 0b11];
        palette[2] = backgroundColors[(value >> 4) & 0b11];
        palette[3] = backgroundColors[(value >> 6) & 0b11];
    }

    public byte read(int index) {
        if(index == 0xFF47) {
            return convertPaletteToByte(backgroundPalette);
        } else if(index == 0xFF48) {
            return convertPaletteToByte(obj0Palette);
        } else if(index == 0xFF49) {
            return convertPaletteToByte(obj1Palette);
        } else if(index >= ADDR_VRAM_START && index < ADDR_VRAM_END) {
            return videoRAM.get(index - ADDR_VRAM_START);
        } else if(index >= ADDR_OAM_START && index < ADDR_OAM_END) {
            return oam.get(index - ADDR_OAM_START);
        } else {
            throw new IllegalArgumentException("Invalid address for GPU: "+Integer.toHexString(index).toUpperCase());
        }
    }

    private byte convertPaletteToByte(int[] palette) {
        int result = 0;
        for (int i = 0; i < palette.length; i++) {
            int shadeIndex;
            int color = palette[i];
            if(color == backgroundColors[0]) {
                shadeIndex = 0;
            } else if(color == backgroundColors[1]) {
                shadeIndex = 1;
            } else if(color == backgroundColors[2]) {
                shadeIndex = 2;
            } else {
                shadeIndex = 3;
            }
            result |= (shadeIndex) << (i*2);
        }
        return (byte) result;
    }

    public void step() {
        for (int i = 0; i < spriteBlocks.length; i++) {
            spriteBlocks[i].loadFromMemory();
        }

        if(lineY < 144) {
            renderSingleLine();
        }
        lineY++;

        if(lineY >= 154) {
            lineY = 0;
        }
        // TODO: implement GPU clock
    }

    /**
     * Renders line number 'lineY'
     */
    private void renderSingleLine() {
        // sprites, TODO: cleanup
        final int spriteHeight = 8;
        final int spriteWidth = 8;
        for (int x = 0; x < WIDTH; x++) {
            int color = 0xFFFFFFFF;
            for (int i = 0; i < spriteBlocks.length; i++) {
                SpriteBlock block = spriteBlocks[i];
                if(block.getPositionX() > 0 && block.getPositionY() > 0) {
                    if(block.getScreenPositionX() <= x && block.getScreenPositionX()+spriteWidth-1 >= x) {
                        if(block.getScreenPositionY() <= lineY && block.getScreenPositionY() +spriteHeight-1 >= lineY) { // the line intersects the sprite
                            int localSpriteX = x-block.getScreenPositionX();
                            int localSpriteY = lineY-block.getScreenPositionY();
                            if(block.isFlipX()) {
                                localSpriteX = spriteWidth-1-localSpriteX;
                            }
                            if(block.isFlipY()) {
                                localSpriteY = spriteHeight-1-localSpriteY;
                            }
                            int spriteColor = getColorForSprite(block.getPatternNumber(), localSpriteX, localSpriteY, block.getPalette());
                            if(spriteColor >>> 24 > 0) {
                                color = spriteColor;
                            }
                        }
                    }
                }
            }
            pixels[x+lineY*WIDTH] = color;
        }
    }

    private int getColorForSprite(int patternNumber, int localSpriteX, int localSpriteY, Palettes palette) {
        int startAddress = patternNumber*16;
        int byteIndex = localSpriteY*2;
        localSpriteX = 7 - localSpriteX;
        int low = read(ADDR_SPRITE_PATTERN_TABLE_START+startAddress+byteIndex) & 0xFF;
        int high = read(ADDR_SPRITE_PATTERN_TABLE_START+startAddress+byteIndex+1) & 0xFF;
        int colorIndex = ((low&0xFF) & (1 << (localSpriteX))) >> (localSpriteX) | (((high&0xFF) & (1 << (localSpriteX))) >> (localSpriteX)) <<1;
        System.out.println(localSpriteX+", "+localSpriteY);
        if(colorIndex == 0)
            return 0x0;
        if(palette == Palettes.OBJ0PAL) {
            return obj0Palette[colorIndex];
        } else {
            return obj1Palette[colorIndex];
        }
    }

    public int[] getPixels() {
        return pixels;
    }
}
