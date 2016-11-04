package org.jglrxavpok.jameboy.memory;

public interface MemoryController {

    void write(int index, byte value);

    byte read(int index);

}
