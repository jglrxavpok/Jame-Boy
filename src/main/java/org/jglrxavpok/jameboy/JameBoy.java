package org.jglrxavpok.jameboy;

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

    public JameBoy() {
        cpu = new CPU();
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

    public MemoryController getMemoryController() {
        return memoryController;
    }

    public void cycle() {
        cpu.doCycle();
    }

}
