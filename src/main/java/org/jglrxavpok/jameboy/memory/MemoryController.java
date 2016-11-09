package org.jglrxavpok.jameboy.memory;

import org.jglrxavpok.jameboy.graphics.GPU;

public interface MemoryController {

    void write(int index, byte value);

    byte read(int index);

    void setGPU(GPU gpu);
}
