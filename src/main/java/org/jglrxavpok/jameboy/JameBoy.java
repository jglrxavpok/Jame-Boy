package org.jglrxavpok.jameboy;

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
    private final IOHandler ioHandler;
    private GameROM currentROM;
    private MemoryController memoryController;
    private GPU gpu;

    public JameBoy() {
        cpu = new CPU();
        gpu = new GPU();
        ioHandler = new IOHandler();
    }

    public void loadROM(GameROM rom) {
        memoryController = MemoryControllers.create(rom);
        cpu.setMemory(memoryController);
        memoryController.setGPU(gpu);
        memoryController.setIOHandler(ioHandler);
        gpu.linkToMemory(memoryController);
        boot();
        currentROM = rom;
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
}
