package org.jglrxavpok.jameboy.memory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MemoryControllers {

    private static final Map<String, MemoryControllerFactory> factories = new HashMap<>();

    static {
        initControllers();
    }

    public static void initControllers() {
        registerControllerFactory("ROM ONLY", (romData, romSize, ramSize) -> new NoMBC(romData, ByteBuffer.allocate(ramSize)));
        registerControllerFactory("MBC1", (romData, romSize, ramSize) -> new MBC1(romSize, ramSize, romData, ByteBuffer.allocate(ramSize))); // TODO: Allow saving to file
    }

    private MemoryControllers() {}

    public static void registerControllerFactory(String controllerID, MemoryControllerFactory factory) {
        factories.put(controllerID, factory);
    }

    public static MemoryController create(GameROM rom) {
        String controllerID = CartridgeHeader.getCartrigeTypeName(rom.getHeader().getCartrigeType());
        MemoryControllerFactory factory = factories.get(controllerID);
        if(factory == null)
            throw new UnsupportedOperationException("Controller "+controllerID+" is not supported yet");
        return factory.create(rom.getData(), rom.getHeader().getROMSize(), rom.getHeader().getRAMSize());
    }

    public interface MemoryControllerFactory {
        MemoryController create(ByteBuffer romData, int romSize, int ramSize);
    }
}
