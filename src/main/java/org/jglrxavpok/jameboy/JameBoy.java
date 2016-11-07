package org.jglrxavpok.jameboy;

import org.jglrxavpok.jameboy.graphics.GPU;
import org.jglrxavpok.jameboy.memory.GameROM;
import org.jglrxavpok.jameboy.memory.MemoryController;
import org.jglrxavpok.jameboy.memory.MemoryControllers;

/**
 * The emulator core, handles the interactions between the components
 */
public class JameBoy {

    private final CPU cpu;
    private GameROM currentROM;
    private MemoryController memoryController;
    private GPU gpu;

    public JameBoy() {
        cpu = new CPU();
        gpu = new GPU();
    }

    public void loadROM(GameROM rom) {
        currentROM = rom;
        memoryController = MemoryControllers.create(rom);
        cpu.setMemory(memoryController);
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

    public void cycle() {
        cpu.doCycle();
    }

    public GPU getGPU() {
        return gpu;
    }
}
