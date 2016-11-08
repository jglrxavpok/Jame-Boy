package org.jglrxavpok.jameboy.graphics;

import org.jglrxavpok.jameboy.utils.BitUtils;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

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
    public static final int ADDR_WX = 0xFF4B;
    public static final int ADDR_WY = 0xFF4A;
    public static final int ADDR_BGP = 0xFF47;
    public static final int ADDR_OBJ0PAL = 0xFF48;
    public static final int ADDR_OBJ1PAL = 0xFF49;
    public static final int ADDR_LCDC = 0xFF40;
    public static final int ADDR_SCROLL_X = 0xFF43;
    public static final int ADDR_SCROLL_Y = 0xFF42;
    private final ByteBuffer videoRAM;
    private final ByteBuffer oam;
    private final List<SpriteBlock> spriteBlocks;
    private final int[] backgroundColors;
    private final int[] pixels;
    private int stepCount;
    private int lineY;
    private int[] obj0Palette;
    private int[] obj1Palette;
    private int[] backgroundPalette;
    private int currentSpriteHeight;
    private boolean shouldRenderSprites;
    private int windowX;
    private int windowY;
    private int scrollX;
    private int scrollY;

    public GPU() {
        videoRAM = ByteBuffer.allocate(8*1024);
        oam = ByteBuffer.allocate(4*40);

        spriteBlocks = new LinkedList<>();
        for (int i = 0; i < 40; i++) {
            spriteBlocks.add(new SpriteBlock(i*4 + ADDR_OAM_START, this));
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

        currentSpriteHeight = 8;

        write(ADDR_LCDC, (byte) 0x91);

        // TODO: Remove, only debug for now
        shouldRenderSprites = true;
    }

    public boolean isValidGPUAddress(int address) {
        return (address >= 0xFF40 && address <= 0xFF4B)
                || (address >= ADDR_VRAM_START && address < ADDR_VRAM_END)
                || (address >= ADDR_OAM_START && address < ADDR_OAM_END);
    }

    public void write(int index, byte value) {
        if(index == ADDR_BGP) {
            convertByteToPalette(backgroundPalette, value);
        } else if(index == ADDR_OBJ0PAL) {
            convertByteToPalette(obj0Palette, value);
        } else if(index == ADDR_OBJ1PAL) {
            convertByteToPalette(obj1Palette, value);
        } else if(index == ADDR_WX) {
            windowX = value & 0xFF;
        } else if(index == ADDR_WY) {
            windowY = value & 0xFF;
        } else if(index == ADDR_LCDC) {
            // TODO
            currentSpriteHeight = BitUtils.getBit(value, 2) ? 16 : 8;
        } else if(index == ADDR_SCROLL_X) {
            scrollX = value & 0xFF;
        } else if(index == ADDR_SCROLL_Y) {
            scrollY = value & 0xFF;
        } else if(index >= ADDR_VRAM_START && index < ADDR_VRAM_END) {
            videoRAM.put(index - ADDR_VRAM_START, value);
        } else if(index >= ADDR_OAM_START && index < ADDR_OAM_END) {
            oam.put(index - ADDR_OAM_START, value);
        } else if(!isValidGPUAddress(index)) {
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
        if(index == ADDR_BGP) {
            return convertPaletteToByte(backgroundPalette);
        } else if(index == ADDR_OBJ0PAL) {
            return convertPaletteToByte(obj0Palette);
        } else if(index == ADDR_OBJ1PAL) {
            return convertPaletteToByte(obj1Palette);
        } else if(index == ADDR_LCDC) {
            // TODO
            return 0;
        } else if(index >= ADDR_VRAM_START && index < ADDR_VRAM_END) {
            return videoRAM.get(index - ADDR_VRAM_START);
        } else if(index >= ADDR_OAM_START && index < ADDR_OAM_END) {
            return oam.get(index - ADDR_OAM_START);
        } else if(!isValidGPUAddress(index)) {
            throw new IllegalArgumentException("Invalid address for GPU: "+Integer.toHexString(index).toUpperCase());
        }
        return 0;
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
        sortSprites();

        if(lineY < 144) {
            renderSingleLine();
        }
        lineY++;

        if(lineY >= 154) {
            lineY = 0;
        }
        // TODO: implement GPU clock
    }

    private void sortSprites() {
        spriteBlocks.forEach(SpriteBlock::enable);
        spriteBlocks.sort((a, b) -> {
            int positionCompare = -Integer.compare(a.getPositionX(), b.getPositionX()); // inverted, 'a' must be drawn last
            if(positionCompare == 0) {
                return Integer.compare(a.getStartAddress(), b.getStartAddress()); // b must be drawn last
            }
            return positionCompare;
        });
    }

    /**
     * Renders line number 'lineY'
     */
    private void renderSingleLine() {
        // todo Arrays.fill(pixels, lineY*WIDTH, (lineY+1)*WIDTH, 0xFFFFFFFF);
        if(shouldRenderSprites) {
            renderSpriteLine();
        }
    }

    private void renderSpriteLine() {
        final int spriteHeight = currentSpriteHeight;
        final int spriteWidth = 8;
        for (int screenX = 0; screenX < WIDTH; screenX++) {
            int color = 0xFFFFFFFF;
            for (SpriteBlock sprite : spriteBlocks) {
                sprite.loadFromMemory();
                if(!sprite.isEnabledForRendering() || !sprite.isVisible())
                    continue;
                int x = screenX + scrollX;
                int y = lineY + scrollY;
                if(sprite.getScreenPositionX() <= x && sprite.getScreenPositionX()+spriteWidth-1 >= x) { // the vertical line intersects the sprite
                    if(sprite.getScreenPositionY() <= y && sprite.getScreenPositionY() +spriteHeight-1 >= y) { // the scan line intersects the sprite
                        int localSpriteX = x-sprite.getScreenPositionX();
                        int localSpriteY = y-sprite.getScreenPositionY();
                        if(sprite.isFlippedOnX()) {
                            localSpriteX = spriteWidth-1-localSpriteX;
                        }
                        if(sprite.isFlippedOnY()) {
                            localSpriteY = spriteHeight-1-localSpriteY;
                        }
                        int spriteColor = getColorForSprite(sprite.getPatternNumber(), localSpriteX, localSpriteY, sprite.getPalette());
                        if(spriteColor >>> 24 > 0) { // if alpha > 0, draw the color
                            color = spriteColor;
                        }
                    }
                }
            }
            pixels[screenX+lineY*WIDTH] = color;
        }
    }

    private int getColorForSprite(int patternNumber, int localSpriteX, int localSpriteY, Palettes palette) {
        int startAddress = patternNumber*16;
        int byteIndex = localSpriteY*2;
        localSpriteX = 7 - localSpriteX;
        int low = read(ADDR_SPRITE_PATTERN_TABLE_START+startAddress+byteIndex) & 0xFF;
        int high = read(ADDR_SPRITE_PATTERN_TABLE_START+startAddress+byteIndex+1) & 0xFF;
        int colorIndex = ((low&0xFF) & (1 << (localSpriteX))) >> (localSpriteX) | (((high&0xFF) & (1 << (localSpriteX))) >> (localSpriteX)) <<1;
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
