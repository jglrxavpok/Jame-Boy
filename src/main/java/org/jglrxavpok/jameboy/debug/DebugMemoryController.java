package org.jglrxavpok.jameboy.debug;

import org.jglrxavpok.jameboy.cpu.Z80Timer;
import org.jglrxavpok.jameboy.graphics.GPU;
import org.jglrxavpok.jameboy.io.IOHandler;
import org.jglrxavpok.jameboy.memory.Interrupts;
import org.jglrxavpok.jameboy.memory.MemoryController;

public class DebugMemoryController implements MemoryController {
    private final MemoryController delegate;

    public DebugMemoryController(MemoryController delegate) {
        this.delegate = delegate;
    }

    public MemoryController getDelegate() {
        return delegate;
    }

    @Override
    public void write(int index, byte value) {
        delegate.write(index, value);

        MemoryViewFrame.getInstance().updateValue(index);
    }

    @Override
    public byte read(int index) {
        return delegate.read(index);
    }

    @Override
    public void setGPU(GPU gpu) {
        delegate.setGPU(gpu);
    }

    @Override
    public void interrupt(Interrupts interrupt) {
        delegate.interrupt(interrupt);
    }

    @Override
    public boolean isInterruptOn(Interrupts interrupt) {
        return delegate.isInterruptOn(interrupt);
    }

    @Override
    public void resetInterrupt(Interrupts interrupt) {
        delegate.resetInterrupt(interrupt);
    }

    @Override
    public IOHandler getIOHandler() {
        return delegate.getIOHandler();
    }

    @Override
    public void setTimer(Z80Timer timer) {
        delegate.setTimer(timer);
    }
}
