package org.jglrxavpok.jameboy.memory;

import java.nio.ByteBuffer;

public class BaseMemoryController implements MemoryController {

    private ByteBuffer internalRAM = ByteBuffer.allocate(0xE000 - 0xC000 +1);

    @Override
    public void write(int index, byte value) {
        if(index >= 0xC000 && index <= 0xE000) {
            internalRAM.put(index - 0xC000, value);
        }

        if(index >= 0xE000 && index <= 0xFE00) {
            internalRAM.put(index - 0xE000, value);
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
        return 0;
    }
}
