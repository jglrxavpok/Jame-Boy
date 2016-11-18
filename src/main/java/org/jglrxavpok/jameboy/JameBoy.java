package org.jglrxavpok.jameboy;

import org.jglrxavpok.jameboy.debug.DebugMemoryController;
import org.jglrxavpok.jameboy.debug.MemoryViewFrame;
import org.jglrxavpok.jameboy.graphics.GPU;
import org.jglrxavpok.jameboy.io.IOHandler;
import org.jglrxavpok.jameboy.memory.GameROM;
import org.jglrxavpok.jameboy.memory.MemoryController;
import org.jglrxavpok.jameboy.memory.MemoryControllers;

/**
 * The emulator core, handles the interactions between the components
 */
public class JameBoy {

    private final CPU cpu;
    private IOHandler ioHandler;
    private GameROM currentROM;
    private MemoryController memoryController;
    private GPU gpu;
    private boolean paused;
    private boolean shouldStep;

    public JameBoy() {
        cpu = new CPU();
        gpu = new GPU();
    }

    public void loadROM(GameROM rom) {
        memoryController = new DebugMemoryController(MemoryControllers.create(rom));
        cpu.setMemory(memoryController);
        memoryController.setGPU(gpu);
        ioHandler = memoryController.getIOHandler();
        gpu.linkToMemory(memoryController);
        boot();
        currentROM = rom;
        MemoryViewFrame.getInstance().resetTable();
    }

    public void boot() {
        cpu.turnOn();
    }

    public GameROM getCurrentROM() {
        return currentROM;
    }

    public void setMemoryController(MemoryController controller) {
        memoryController = controller;
    }

    public MemoryController getMemoryController() {
        return memoryController;
    }

    public int cycle() {
        int cycles = cpu.doCycle();
        gpu.step(cycles);
        return cycles;
    }

    public IOHandler getIOHandler() {
        return ioHandler;
    }

    public GPU getGPU() {
        return gpu;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean shouldStep() {
        return shouldStep;
    }

    public void stepDone() {
        shouldStep = false;
    }

    public void requestStep() {
        shouldStep = true;
    }

    public CPU getCPU() {
        return cpu;
    }
}
