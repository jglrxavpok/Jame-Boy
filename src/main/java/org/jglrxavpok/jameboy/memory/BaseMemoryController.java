package org.jglrxavpok.jameboy.memory;

import org.jglrxavpok.jameboy.graphics.GPU;

import java.nio.ByteBuffer;

public class BaseMemoryController implements MemoryController {

    private ByteBuffer internalRAM = ByteBuffer.allocate(0xE000 - 0xC000 +1);
    private GPU gpu;

    @Override
    public void write(int index, byte value) {
        if(index >= 0xC000 && index <= 0xE000) {
            internalRAM.put(index - 0xC000, value);
        }

        if(index >= 0xE000 && index <= 0xFE00) {
            internalRAM.put(index - 0xE000, value);
        }

        if(gpu != null) {
            if((index >= GPU.ADDR_OAM_START && index <= GPU.ADDR_OAM_END) /*Video RAM*/
                    || (index >= GPU.ADDR_VRAM_START && index < GPU.ADDR_VRAM_END /*OAM*/)) {
                gpu.write(index, value);
            }
        }
    }

    @Override
    public byte read(int index) {
        if(index >= 0xC000 && index <= 0xE000) {
            return internalRAM.get(index - 0xC000);
        }

        if(index >= 0xE000 && index <= 0xFE00) {
            return internalRAM.get(index - 0xE000);
        }

        if(gpu != null) {
            if((index >= GPU.ADDR_OAM_START && index <= GPU.ADDR_OAM_END) /*Video RAM*/
                    || (index >= GPU.ADDR_VRAM_START && index < GPU.ADDR_VRAM_END /*OAM*/)) {
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
}
