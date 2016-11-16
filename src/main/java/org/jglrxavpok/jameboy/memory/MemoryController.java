package org.jglrxavpok.jameboy.memory;

import org.jglrxavpok.jameboy.cpu.Z80Timer;
import org.jglrxavpok.jameboy.graphics.GPU;
import org.jglrxavpok.jameboy.io.IOHandler;

public interface MemoryController {

    void write(int index, byte value);

    byte read(int index);

    void setGPU(GPU gpu);

    void interrupt(Interrupts interrupt);

    boolean isInterruptOn(Interrupts interrupt);

    void resetInterrupt(Interrupts interrupt);

    IOHandler getIOHandler();

    void setTimer(Z80Timer timer);
}
