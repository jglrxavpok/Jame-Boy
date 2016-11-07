package org.jglrxavpok.jameboy.graphics;

import org.jglrxavpok.jameboy.utils.BitUtils;

import java.nio.ByteBuffer;

public class SpriteBlock {

    private final int startAddress;
    private final GPU owner;
    private Palettes palette;
    private int yPosition;
    private int xPosition;
    private int patternNumber;
    private boolean priority;
    private boolean xFlip;
    private boolean yFlip;

    public SpriteBlock(int startAddress, GPU owner) {
        this.startAddress = startAddress;
        this.owner = owner;
        yPosition = 0;
        xPosition = 0;
        patternNumber = 0;
        priority = false;
        yFlip = false;
        xFlip = false;
        palette = Palettes.OBJ0PAL;
    }

    public void loadFromMemory() {
        yPosition = owner.read(startAddress) & 0xFF;
        xPosition = owner.read(startAddress+1) & 0xFF;
        patternNumber = owner.read(startAddress+2) & 0xFF;
        int flags = owner.read(startAddress+3) & 0xFF;
        priority = BitUtils.getBit(flags, 7);
        yFlip = BitUtils.getBit(flags, 6);
        xFlip = BitUtils.getBit(flags, 5);
        palette = BitUtils.getBit(flags, 4) ? Palettes.OBJ1PAL : Palettes.OBJ0PAL;
    }

    public void writeToMemory() {
        owner.write(startAddress, (byte) yPosition);
        owner.write(startAddress+1, (byte) xPosition);
        owner.write(startAddress+2, (byte) patternNumber);
        owner.write(startAddress+3, (byte) getFlags());
    }

    private int getFlags() {
        int flags = 0;
        if(priority) {
            flags |= (1<<7);
        }
        if(yFlip) {
            flags |= (1<<6);
        }
        if(xFlip) {
            flags |= (1<<5);
        }
        if(palette == Palettes.OBJ1PAL) {
            flags |= (1<<4);
        }
        return flags;
    }

    public Palettes getPalette() {
        return palette;
    }

    public void setPalette(Palettes palette) {
        this.palette = palette;
    }

    public int getPositionY() {
        return yPosition;
    }

    public void setPositionY(int yPosition) {
        this.yPosition = yPosition;
    }

    public int getPositionX() {
        return xPosition;
    }

    public void setPositionX(int xPosition) {
        this.xPosition = xPosition;
    }

    public int getPatternNumber() {
        return patternNumber;
    }

    public void setPatternNumber(int patternNumber) {
        this.patternNumber = patternNumber;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public boolean isFlipX() {
        return xFlip;
    }

    public void setFlipX(boolean xFlip) {
        this.xFlip = xFlip;
    }

    public boolean isFlipY() {
        return yFlip;
    }

    public void setFlipY(boolean yFlip) {
        this.yFlip = yFlip;
    }

    public int getScreenPositionY() {
        return yPosition-16;
    }

    public int getScreenPositionX() {
        return xPosition-8;
    }

}
