package org.jglrxavpok.jameboy.memory;

import org.jglrxavpok.jameboy.graphics.GPU;
import org.jglrxavpok.jameboy.io.IOHandler;

public interface MemoryController {

    void write(int index, byte value);

    byte read(int index);

    void setGPU(GPU gpu);

    void interrupt(Interrupts interrupt);

    boolean isInterruptOn(Interrupts interrupt);

    void resetInterrupt(Interrupts interrupt);

    void setIOHandler(IOHandler handler);
}
