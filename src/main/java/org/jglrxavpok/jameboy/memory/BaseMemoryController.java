package org.jglrxavpok.jameboy.memory;

import org.jglrxavpok.jameboy.graphics.GPU;
import org.jglrxavpok.jameboy.utils.BitUtils;

import java.nio.ByteBuffer;

public class BaseMemoryController implements MemoryController {

    public static final int ADDR_INTERRUPT_FLAG = 0xFF0F;
    public static final int ADDR_INTERRUPT_ENABLE = 0xFFFF;
    private ByteBuffer internalRAM = ByteBuffer.allocate(0xE000 - 0xC000 +1);
    private GPU gpu;
    private byte interruptFlags;
    private byte interruptEnable;

    @Override
    public void write(int index, byte value) {
        if(index == ADDR_INTERRUPT_ENABLE) {
            interruptEnable = value;
        }
        else if(index == ADDR_INTERRUPT_FLAG) {
            interruptFlags = value;
        }

        if(index >= 0xC000 && index <= 0xE000) {
            internalRAM.put(index - 0xC000, value);
        }

        if(index >= 0xE000 && index <= 0xFE00) {
            internalRAM.put(index - 0xE000, value);
        }

        if(gpu != null) {
            if(index == GPU.ADDR_OAM_DMA_TRANSFER) {
                System.out.println("OAM transfer!");
                int sourceStart = (value & 0xFF) * 0x100;
                int length = GPU.ADDR_OAM_END-GPU.ADDR_OAM_START;
                for (int i = 0; i < length; i++) {
                    gpu.write(GPU.ADDR_OAM_START+i, read(sourceStart+i));
                }
            } else if(gpu.isValidGPUAddress(index)) {
                gpu.write(index, value);
            }
        }
    }

    @Override
    public byte read(int index) {
        if(index == ADDR_INTERRUPT_ENABLE) {
            return interruptEnable;
        }
        if(index == ADDR_INTERRUPT_FLAG) {
            return interruptFlags;
        }
        if(index >= 0xC000 && index <= 0xE000) {
            return internalRAM.get(index - 0xC000);
        }

        if(index >= 0xE000 && index <= 0xFE00) {
            return internalRAM.get(index - 0xE000);
        }

        if(gpu != null) {
            if(gpu.isValidGPUAddress(index)) {
                return gpu.read(index);
            }
        }
        return 0;
    }

    public GPU getGPU() {
        return gpu;
    }

    public ByteBuffer getInternalRAM() {
        return internalRAM;
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
        return BitUtils.getBit(read(ADDR_INTERRUPT_ENABLE) & 0xFF, interrupt.ordinal()) && BitUtils.getBit(read(ADDR_INTERRUPT_FLAG) & 0xFF, interrupt.ordinal());
    }

    @Override
    public void disableInterrupt(Interrupts interrupt) {
        write(ADDR_INTERRUPT_ENABLE, (byte) (interruptFlags ^ (1<<interrupt.ordinal())));
    }
}
