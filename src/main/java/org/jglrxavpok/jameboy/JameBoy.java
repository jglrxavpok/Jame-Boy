package org.jglrxavpok.jameboy;

import org.jglrxavpok.jameboy.memory.GameROM;
import org.jglrxavpok.jameboy.memory.MemoryController;
import org.jglrxavpok.jameboy.memory.MemoryControllers;

/**
 * The emulator core, handles the interactions between the components
 */
public class JameBoy {

    private GameROM currentROM;
    private MemoryController memoryController;

    public JameBoy() {

    }

    public void loadROM(GameROM rom) {
        currentROM = rom;
        memoryController = MemoryControllers.create(rom);
    }

    public GameROM getCurrentROM() {
        return currentROM;
    }

    public MemoryController getMemoryController() {
        return memoryController;
    }

    public void cycle() {

    }

}
