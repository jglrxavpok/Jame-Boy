package org.jglrxavpok.jameboy.memory;

import org.jglrxavpok.jameboy.cpu.Z80Timer;
import org.jglrxavpok.jameboy.graphics.GPU;
import org.jglrxavpok.jameboy.io.IOHandler;
import org.jglrxavpok.jameboy.utils.BitUtils;

import java.nio.ByteBuffer;

public class BaseMemoryController implements MemoryController {

    public static final int ADDR_INTERRUPT_FLAG = 0xFF0F;
    public static final int ADDR_INTERRUPT_ENABLE = 0xFFFF;
    private IOHandler ioHandler;
    private ByteBuffer internal8kbRAM = ByteBuffer.allocate(0xE000 - 0xC000 +1);
    private ByteBuffer highRAM = ByteBuffer.allocate(0xFFFF - 0xFF80 +1);
    private ByteBuffer empty = ByteBuffer.allocate(0xFF7F - 0xFF4C +1);
    private GPU gpu;
    private byte interruptFlags;
    private byte interruptEnable;
    private Z80Timer timer;

    public BaseMemoryController() {
        ioHandler = new IOHandler(this);
    }

    @Override
    public void write(int index, byte value) {
        if(index == 0xFF00) {
            ioHandler.write(index, value);
        } else if(index >= Z80Timer.ADDR_DIV_REGISTER && index <= Z80Timer.ADDR_TIMER_CONTROL) {
            timer.write(index, value);
        } else if(index == ADDR_INTERRUPT_ENABLE) {
            interruptEnable = value;
        } else if(index == ADDR_INTERRUPT_FLAG) {
            interruptFlags = value;
        } else if(index >= 0xC000 && index < 0xE000) {
            internal8kbRAM.put(index - 0xC000, value);
        } else if(index >= 0xE000 && index < 0xFE00) {
            internal8kbRAM.put(index - 0xE000, value);
        } else if(index == GPU.ADDR_OAM_DMA_TRANSFER) {
            int sourceStart = (value & 0xFF) * 0x100;
            int length = GPU.ADDR_OAM_END-GPU.ADDR_OAM_START;
            for (int i = 0; i < length; i++) {
                gpu.write(GPU.ADDR_OAM_START+i, read(sourceStart+i));
            }
        } else if(gpu.isValidGPUAddress(index)) {
            gpu.write(index, value);
        } else if(index >= 0xFF4C && index < 0xFF7F) {
            empty.put(index - 0xFF4C, value);
        } else if(index >= 0xFF80 && index < 0xFFFF) {
            highRAM.put(index - 0xFF80, value);
        } else {
          // TODO  System.err.println("[BaseMemoryController] Unknown write location: "+Integer.toHexString(index).toUpperCase());
        }
    }

    @Override
    public byte read(int index) {
        if(index == 0xFF00) {
            return ioHandler.read(index);
        }
        else if(index >= Z80Timer.ADDR_DIV_REGISTER && index <= Z80Timer.ADDR_TIMER_CONTROL) {
            return timer.read(index);
        }
        else if(index == ADDR_INTERRUPT_ENABLE) {
            return interruptEnable;
        }
        else if(index == ADDR_INTERRUPT_FLAG) {
            return interruptFlags;
        }
        else if(index >= 0xC000 && index <= 0xE000) {
            return internal8kbRAM.get(index - 0xC000);
        }
        else if(index >= 0xE000 && index <= 0xFE00) {
            return internal8kbRAM.get(index - 0xE000);
        }
        else if(index >= 0xFF4C && index <= 0xFF7F) {
            return empty.get(index - 0xFF4C);
        }
        else if(index >= 0xFF80 && index < 0xFFFF) {
            return highRAM.get(index - 0xFF80);
        }
        else if(gpu.isValidGPUAddress(index)) {
            return gpu.read(index);
        }
        else {
            // TODO System.err.println("[BaseMemoryController] Unknown read location: "+Integer.toHexString(index).toUpperCase());
        }
        return 0;
    }

    public GPU getGPU() {
        return gpu;
    }

    public ByteBuffer getInternal8kbRAM() {
        return internal8kbRAM;
    }

    public void setGPU(GPU gpu) {
        this.gpu = gpu;
    }

    @Override
    public void interrupt(Interrupts interrupt) {
        byte flag = read(ADDR_INTERRUPT_FLAG);
        flag |= 1<<interrupt.ordinal();
        write(ADDR_INTERRUPT_FLAG, flag);
    }

    @Override
    public boolean isInterruptOn(Interrupts interrupt) {
        return BitUtils.getBit(read(ADDR_INTERRUPT_ENABLE) & 0xFF, interrupt.ordinal())
                && BitUtils.getBit(read(ADDR_INTERRUPT_FLAG) & 0xFF, interrupt.ordinal());
    }

    @Override
    public void resetInterrupt(Interrupts interrupt) {
        write(ADDR_INTERRUPT_FLAG, (byte) (interruptFlags ^ (1<<interrupt.ordinal())));
    }

    @Override
    public IOHandler getIOHandler() {
        return ioHandler;
    }

    @Override
    public void setTimer(Z80Timer timer) {
        this.timer = timer;
    }

    public Z80Timer getTimer() {
        return timer;
    }
}
