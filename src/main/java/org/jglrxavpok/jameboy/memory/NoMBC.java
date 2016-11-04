package org.jglrxavpok.jameboy.memory;

import java.nio.ByteBuffer;

public class NoMBC extends BaseMemoryController {

    private final ByteBuffer rom;
    private final ByteBuffer ram;

    public NoMBC(ByteBuffer rom, ByteBuffer ram) {
        this.rom = rom;
        this.ram = ram;
    }

    @Override
    public void write(int index, byte value) {
        super.write(index, value);
        if(index >= 0xA000 && index <= 0xBFFF) {
            ram.put(index - 0xA000, value);
        }
    }

    @Override
    public byte read(int index) {
        if(index >= 0x0000 && index <= 0x7FFF) {
            return rom.get(index);
        }

        if(index >= 0xA000 && index <= 0xBFFF) {
            return ram.get(index - 0xA000);
        }
        return super.read(index);
    }
}
