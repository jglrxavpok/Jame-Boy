package org.jglrxavpok.jameboy.io;

import org.jglrxavpok.jameboy.input.Keyboard;
import org.jglrxavpok.jameboy.utils.BitUtils;

public class IOHandler {

    public static final int ADDR_JOYPAD = 0xFF00;
    private IOJoypadSelection selection = IOJoypadSelection.NONE;
    private boolean leftPressed;
    private boolean upPressed;
    private boolean rightPressed;
    private boolean downPressed;
    private boolean startPressed;
    private boolean selectPressed;
    private boolean aPressed;
    private boolean bPressed;

    public void write(int address, byte value) {
        if(address == ADDR_JOYPAD) {
            if(!BitUtils.getBit(value & 0xFF, 5)) {
                selection = IOJoypadSelection.DIRECTIONS;
            } else if(!BitUtils.getBit(value & 0xFF, 4)) {
                selection = IOJoypadSelection.BUTTONS;
            } else {
                selection = IOJoypadSelection.NONE;
            }
        }
    }

    public byte read(int address) {
        if(address == ADDR_JOYPAD) {
            byte val = 0;
            if(selection == IOJoypadSelection.DIRECTIONS) {
                val |= (1<<5);
                if(!leftPressed)
                    val |= 1<<1;
                if(!rightPressed)
                    val |= 0x1;
                if(!upPressed)
                    val |= 1<<2;
                if(!downPressed) {
                    val |= 1 << 3;
                }
            } else if(selection == IOJoypadSelection.BUTTONS) {
                val |= (1<<4);
                if(!bPressed)
                    val |= 1<<1;
                if(!aPressed)
                    val |= 0x1;
                if(!selectPressed)
                    val |= 1<<2;
                if(!startPressed)
                    val |= 1<<3;
            }
            return (byte) (val | 0xC0);
        }
        return 0;
    }

    public boolean isUpPressed() {
        return upPressed;
    }

    public void setUpPressed(boolean upPressed) {
        this.upPressed = upPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public void setRightPressed(boolean rightPressed) {
        this.rightPressed = rightPressed;
    }

    public boolean isDownPressed() {
        return downPressed;
    }

    public void setDownPressed(boolean downPressed) {
        this.downPressed = downPressed;
    }

    public boolean isStartPressed() {
        return startPressed;
    }

    public void setStartPressed(boolean startPressed) {
        this.startPressed = startPressed;
    }

    public boolean isSelectPressed() {
        return selectPressed;
    }

    public void setSelectPressed(boolean selectPressed) {
        this.selectPressed = selectPressed;
    }

    public boolean isAPressed() {
        return aPressed;
    }

    public void setAPressed(boolean aPressed) {
        this.aPressed = aPressed;
    }

    public boolean isBPressed() {
        return bPressed;
    }

    public void setBPressed(boolean bPressed) {
        this.bPressed = bPressed;
    }

    public void setLeftPressed(boolean leftPressed) {
        this.leftPressed = leftPressed;
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    private enum IOJoypadSelection {
        DIRECTIONS, NONE, BUTTONS
    }
}
