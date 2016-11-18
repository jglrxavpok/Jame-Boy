package org.jglrxavpok.jameboy.io;

import org.jglrxavpok.jameboy.input.Keyboard;
import org.jglrxavpok.jameboy.memory.Interrupts;
import org.jglrxavpok.jameboy.memory.MemoryController;
import org.jglrxavpok.jameboy.utils.BitUtils;

public class IOHandler {

    public static final int ADDR_JOYPAD = 0xFF00;
    private final MemoryController memory;
    private IOJoypadSelection selection = IOJoypadSelection.NONE;
    private boolean leftPressed;
    private boolean upPressed;
    private boolean rightPressed;
    private boolean downPressed;
    private boolean startPressed;
    private boolean selectPressed;
    private boolean aPressed;
    private boolean bPressed;
    private byte joypadTop;

    public IOHandler(MemoryController memory) {
        this.memory = memory;
    }

    public void write(int address, byte value) {
        if(address == ADDR_JOYPAD) {
            if(BitUtils.getBit(value & 0xFF, 5) && !BitUtils.getBit(value & 0xFF, 4)) {
                selection = IOJoypadSelection.DIRECTIONS;
            } else if(BitUtils.getBit(value & 0xFF, 4) && !BitUtils.getBit(value & 0xFF, 5)) {
                selection = IOJoypadSelection.BUTTONS;
            }

            joypadTop = (byte) ((value & 0xC0) | 0xF);
        }
    }

    public byte read(int address) {
        if(address == ADDR_JOYPAD) {
            byte val = (byte) 0xCF;
            val ^= 0b00110000;
            if(selection == IOJoypadSelection.DIRECTIONS) {
                if(leftPressed)
                    val &= (~(1<<1)) & 0xFF;
                if(rightPressed)
                    val &= (~(0x1)) & 0xFF;
                if(downPressed)
                    val &= (~(1 << 3)) & 0xFF;
                if(upPressed) {
                    val &= (~(1 << 2)) & 0xFF;
                }
            } else if(selection == IOJoypadSelection.BUTTONS) {
                if(bPressed)
                    val &= (~(1<<1)) & 0xFF;
                if(aPressed)
                    val &= (~(0x1)) & 0xFF;
                if(selectPressed)
                    val &= (~(1 << 2)) & 0xFF;
                if(startPressed) {
                    val &= (~(1 << 3)) & 0xFF;
                }
            } else {
                return (byte) 0xFF;
            }
            return (byte) (val & joypadTop);
        }
        return 0;
    }

    public boolean isUpPressed() {
        return upPressed;
    }

    public void setUpPressed(boolean upPressed) {
        interruptIfBecamePressed(this.upPressed, upPressed);
        this.upPressed = upPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public void setRightPressed(boolean rightPressed) {
        interruptIfBecamePressed(this.rightPressed, rightPressed);
        this.rightPressed = rightPressed;
    }

    public boolean isDownPressed() {
        return downPressed;
    }

    public void setDownPressed(boolean downPressed) {
        interruptIfBecamePressed(this.downPressed, downPressed);
        this.downPressed = downPressed;
    }

    public boolean isStartPressed() {
        return startPressed;
    }

    public void setStartPressed(boolean startPressed) {
        interruptIfBecamePressed(this.startPressed, startPressed);
        this.startPressed = startPressed;
    }

    public boolean isSelectPressed() {
        return selectPressed;
    }

    public void setSelectPressed(boolean selectPressed) {
        interruptIfBecamePressed(this.selectPressed, selectPressed);
        this.selectPressed = selectPressed;
    }

    public boolean isAPressed() {
        return aPressed;
    }

    public void setAPressed(boolean aPressed) {
        interruptIfBecamePressed(this.aPressed, aPressed);
        this.aPressed = aPressed;
    }

    public boolean isBPressed() {
        return bPressed;
    }

    public void setBPressed(boolean bPressed) {
        interruptIfBecamePressed(this.bPressed, bPressed);
        this.bPressed = bPressed;
    }

    public void setLeftPressed(boolean leftPressed) {
        interruptIfBecamePressed(this.leftPressed, leftPressed);
        this.leftPressed = leftPressed;
    }

    private void interruptIfBecamePressed(boolean old, boolean newValue) {
        if(!old & newValue) { // was not pressed but now is
            memory.interrupt(Interrupts.JOYPAD);
        }
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    private enum IOJoypadSelection {
        DIRECTIONS, NONE, BUTTONS
    }
}
