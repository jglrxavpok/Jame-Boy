package org.jglrxavpok.jameboy.graphics;

import org.jglrxavpok.jameboy.memory.Interrupts;
import org.jglrxavpok.jameboy.memory.MemoryController;
import org.jglrxavpok.jameboy.utils.BitUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
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
    public static final int ADDR_OAM_DMA_TRANSFER = 0xFF46;
    public static final int ADDR_STAT = 0xFF41;
    public static final int ADDR_LY = 0xFF44;
    public static final int ADDR_LYC = 0xFF45;
    public static final byte VBLANK_MODE = 0x1;
    public static final byte HBLANK_MODE = 0x0;
    public static final byte OAM_READ_MODE = 0x2;
    public static final byte VRAM_READ_MODE = 0x3;
    private final ByteBuffer videoRAM;
    private final ByteBuffer oam;
    private final List<SpriteBlock> spriteBlocks;
    private final int[] grayScaleBackgroundColors;
    private final int[] greenScaleBackgroundColors;

    // TODO: Make it modifiable in settings
    private int[] backgroundColors;

    private int[] pixels;
    private int clockCount;
    private int lineY;
    private int[] obj0Palette;
    private int[] obj1Palette;
    private int[] backgroundPalette;
    private int currentSpriteHeight;
    private boolean shouldRenderSprites;
    private boolean shouldRenderBackground;
    private boolean shouldRenderWindow;
    private int windowX;
    private int windowY;
    private int scrollX;
    private int scrollY;
    private int backgroundTileMapAddress;
    private int tileDataAddress;
    private int windowTileMapAddress;
    private byte modeFlag;
    private boolean coincidenceInterrupt;
    private boolean hBlankInterrupt;
    private boolean vBlankInterrupt;
    private boolean interruptOAM;
    private boolean coincidenceFlag;
    private boolean enableDisplay;
    private MemoryController memory;
    private byte lyc;
    private byte oamTransferStart;
    private boolean thrownLCDInt;

    public GPU() {
        videoRAM = ByteBuffer.allocate(8*1024);
        oam = ByteBuffer.allocate(4*40);

        spriteBlocks = new LinkedList<>();
        for (int i = 0; i < 40; i++) {
            spriteBlocks.add(new SpriteBlock(i*4 + ADDR_OAM_START, this));
        }

        grayScaleBackgroundColors = new int[] {
                0xFFFFFFFF,
                0xFFDDDDDD,
                0xFF808080,
                0xFF000000
        };

        greenScaleBackgroundColors = new int[] {
                0xFF00FF00,
                0xFF00DD00,
                0xFF008000,
                0xFF000000
        };

        backgroundColors = greenScaleBackgroundColors;

        pixels = new int[WIDTH*HEIGHT];

        backgroundPalette = new int[4];
        obj0Palette = new int[4];
        obj1Palette = new int[4];

        System.arraycopy(backgroundColors, 0, backgroundPalette, 0, backgroundColors.length);
        System.arraycopy(backgroundColors, 0, obj0Palette, 0, backgroundColors.length);
        System.arraycopy(backgroundColors, 0, obj1Palette, 0, backgroundColors.length);

        write(ADDR_LCDC, (byte) 0x91);
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
        } else if(index == ADDR_OAM_DMA_TRANSFER) {
            oamTransferStart = value;
        } else if(index == ADDR_LCDC) {
            enableDisplay = BitUtils.getBit(value, 7);

            currentSpriteHeight = BitUtils.getBit(value, 2) ? 16 : 8;
            tileDataAddress = BitUtils.getBit(value, 4) ? 0x8000 : 0x8800;
            backgroundTileMapAddress = BitUtils.getBit(value, 3) ? 0x9C00 : 0x9800;

            windowTileMapAddress = BitUtils.getBit(value, 6) ? 0x9C00 : 0x9800;

            shouldRenderWindow = BitUtils.getBit(value, 5);
            shouldRenderSprites = BitUtils.getBit(value, 1);
            shouldRenderBackground = BitUtils.getBit(value, 0);

        } else if(index == ADDR_LYC) {
            lyc = value;
        } else if(index == ADDR_SCROLL_X) {
            scrollX = value & 0xFF;
        } else if(index == ADDR_STAT) {
            coincidenceFlag = BitUtils.getBit(value, 6);
            interruptOAM = BitUtils.getBit(value, 5);
            vBlankInterrupt = BitUtils.getBit(value, 4);
            hBlankInterrupt = BitUtils.getBit(value, 3);
        } else if(index == ADDR_SCROLL_Y) {
            scrollY = value & 0xFF;
        } else if(index >= ADDR_VRAM_START && index < ADDR_VRAM_END) {
            videoRAM.put(index - ADDR_VRAM_START, value);
        } else if(index >= ADDR_OAM_START && index < ADDR_OAM_END) {
            oam.put(index - ADDR_OAM_START, value);
        } else if(!isValidGPUAddress(index)) {
            throw new IllegalArgumentException("Invalid address for GPU: "+Integer.toHexString(index).toUpperCase());
        } else {
            System.out.println("GPU: Unknown write address: "+Integer.toHexString(index).toUpperCase());
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
            byte value = 0;
            if(enableDisplay)
                value |= 1<<7;
            if(windowTileMapAddress == 0x9C00)
                value |= 1<<6;
            if(shouldRenderWindow)
                value |= 1<<5;
            if(tileDataAddress == 0x8000)
                value |= 1<<4;
            if(backgroundTileMapAddress == 0x9C00)
                value |= 1<<3;
            if(currentSpriteHeight == 16)
                value |= 1<<2;
            if(shouldRenderSprites)
                value |= 1<<1;
            if(shouldRenderBackground)
                value |= 0x1;
            return value;
        } else if(index == ADDR_LYC) {
            return lyc;
        } else if(index == ADDR_STAT) {
            byte value = 0;
            if(coincidenceInterrupt)
                value |= 1<<6;
            if(interruptOAM)
                value |= 1<<5;
            if(vBlankInterrupt)
                value |= 1<<4;
            if(hBlankInterrupt)
                value |= 1<<3;
            if(coincidenceFlag)
                value |= 1<<2;
            value |= modeFlag;
            return value;
        } else if(index == ADDR_LY) {
            return (byte) lineY;
        } else if(index == ADDR_SCROLL_X) {
            return (byte) scrollX;
        } else if(index == ADDR_SCROLL_Y) {
            return (byte) scrollY;
        } else if(index == ADDR_OAM_DMA_TRANSFER) {
            return oamTransferStart;
        } else if(index >= ADDR_VRAM_START && index < ADDR_VRAM_END) {
            return videoRAM.get(index - ADDR_VRAM_START);
        } else if(index >= ADDR_OAM_START && index < ADDR_OAM_END) {
            return oam.get(index - ADDR_OAM_START);
        } else if(!isValidGPUAddress(index)) {
            throw new IllegalArgumentException("Invalid address for GPU: "+Integer.toHexString(index).toUpperCase());
        } else {
            System.out.println("GPU: Unknown read address: "+Integer.toHexString(index).toUpperCase());
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

    public void step(int cycles) {
        if(!enableDisplay) {
            modeFlag = HBLANK_MODE;
            return;
        }
        clockCount+=cycles;

        if(coincidenceInterrupt && lineY == (lyc & 0xFF)) {
            memory.interrupt(Interrupts.LCD_COINCIDENCE);
        }

        if(lineY >= 144) {
            modeFlag = VBLANK_MODE;
            thrownLCDInt = false;
        } else {
            if (clockCount >= 80) {
                modeFlag = OAM_READ_MODE;
                thrownLCDInt = false;
            } else if (clockCount >= 172 + 80) {
                modeFlag = VRAM_READ_MODE;
                thrownLCDInt = false;
            } else {
                modeFlag = HBLANK_MODE;
                if(!thrownLCDInt && hBlankInterrupt) {
                    memory.interrupt(Interrupts.LCD_COINCIDENCE);
                    thrownLCDInt = true;
                }
            }
        }

        if(clockCount >= 456) {
            clockCount = 0;
            sortSprites();

            if(lineY < 144) {
                renderSingleLine();
            }

            if(lineY == 144) {
                memory.interrupt(Interrupts.V_BLANK);
            }
            lineY++;

            if(lineY >= 154) {
                lineY = 0;
            }
        }
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
        Arrays.fill(pixels, lineY*WIDTH, (lineY+1)*WIDTH, 0xFFFFFFFF);
        if(shouldRenderBackground) {
            renderBackgroundLine();
        }

        if(shouldRenderSprites) {
            renderSpriteLine();
        }

        if(shouldRenderWindow) {
            renderWindowLine();
        }
    }

    private void renderWindowLine() {
        for (int x = 0; x < WIDTH; x++) {
            int tileX = x+windowX-7;
            int tileY = lineY-windowY;

            if(tileY < 0)
                continue;

            int tileColumn = tileX / 8;
            int tileRow = tileY / 8;

            int localX = (tileX % 8);
            int localY = (tileY % 8);

            int color = getWindowColor(tileColumn, tileRow, localX, localY);

            pixels[x+lineY*WIDTH] = color;
        }
    }

    private int getWindowColor(int column, int row, int x, int y) {
        byte patternNumberByte = read(column+row*(32) + windowTileMapAddress);
        int patternNumber;
        patternNumber = patternNumberByte;
        if(tileDataAddress == 0x8800) {
            patternNumber -= 128;
        }
        int startAddress = patternNumber*16 + tileDataAddress + 0x1000; // TODO: why + 0x1000
        int byteIndex = y*2;
        x = 7 - x;
        int low = read(startAddress+byteIndex) & 0xFF;
        int high = read(startAddress+byteIndex+1) & 0xFF;
        int colorIndex = ((low&0xFF) & (1 << (x))) >> (x) | (((high&0xFF) & (1 << (x))) >> (x)) <<1;
        return backgroundPalette[colorIndex];
    }

    private void renderBackgroundLine() {
        for (int x = 0; x < WIDTH; x++) {
            int tileX = x+scrollX;
            int tileY = lineY+scrollY;

            int tileColumn = tileX / 8;
            int tileRow = tileY / 8;

            int localX = (tileX % 8);
            int localY = (tileY % 8);

            int color = getBackgroundColor(tileColumn, tileRow, localX, localY);

            pixels[x+lineY*WIDTH] = color;
        }
    }

    private int getBackgroundColor(int column, int row, int x, int y) {
        byte patternNumberByte = read(column+row*32 + backgroundTileMapAddress);
        int patternNumber;
        patternNumber = patternNumberByte;
        if(tileDataAddress == 0x8800) {
            patternNumber -= 128;
        }
        int startAddress = patternNumber*16 + tileDataAddress + 0x1000; // TODO: why + 0x1000
        int byteIndex = y*2;
        x = 7 - x;
        int low = read(startAddress+byteIndex) & 0xFF;
        int high = read(startAddress+byteIndex+1) & 0xFF;
        int colorIndex = ((low&0xFF) & (1 << (x))) >> (x) | (((high&0xFF) & (1 << (x))) >> (x)) <<1;
        return backgroundPalette[colorIndex];
    }

    private void renderSpriteLine() {
        final int spriteHeight = currentSpriteHeight;
        final int spriteWidth = 8;
        for (int screenX = 0; screenX < WIDTH; screenX++) {
            int color = 0x0;
            for (SpriteBlock sprite : spriteBlocks) {
                sprite.loadFromMemory();
                if(!sprite.isEnabledForRendering() || !sprite.isVisible())
                    continue;
                int y = lineY + scrollY;
                int x = screenX + scrollX;
                if(sprite.getScreenPositionX() <= x && sprite.getScreenPositionX()+spriteWidth-1 >= x) { // the vertical line intersects the sprite
                    if(sprite.getScreenPositionY() <= y && sprite.getScreenPositionY() +spriteHeight-1 >= y) { // the scan line intersects the sprite
                        int localSpriteX = x - sprite.getScreenPositionX();
                        int localSpriteY = y - sprite.getScreenPositionY();
                        if(sprite.isFlippedOnX()) {
                            localSpriteX = spriteWidth-1-localSpriteX;
                        }
                        if(sprite.isFlippedOnY()) {
                            localSpriteY = spriteHeight-1-localSpriteY;
                        }
                        color = getColorForSprite(sprite.getPatternNumber(), localSpriteX, localSpriteY, sprite.getPalette());
                    }
                }
                if(color >>> 24 > 0) { // if alpha > 0, draw the color
                    pixels[screenX + lineY * WIDTH] = color;
                }
            }
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

    public void setBuffer(int[] buffer) {
        this.pixels = buffer;
    }

    public void linkToMemory(MemoryController memory) {
        this.memory = memory;
    }
}
