import org.jglrxavpok.jameboy.memory.CartridgeHeader;
import org.jglrxavpok.jameboy.memory.MBC1;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestMBC {

    @Test
    public void bankCounts() {
        ByteBuffer rom = ByteBuffer.allocate(CartridgeHeader.ROM_SIZES[1]);
        ByteBuffer ram = ByteBuffer.allocate(CartridgeHeader.RAM_SIZES[0]);
        MBC1 controller = new MBC1(rom.remaining(), ram.remaining(), rom, ram);
        assertEquals(0, controller.getRamBankCount());
        assertEquals(4, controller.getRomBankCount());
    }

    @Test
    public void bankEnableRAM() {
        ByteBuffer rom = ByteBuffer.allocate(CartridgeHeader.ROM_SIZES[1]);
        ByteBuffer ram = ByteBuffer.allocate(CartridgeHeader.RAM_SIZES[0]);
        MBC1 controller = new MBC1(rom.remaining(), ram.remaining(), rom, ram);
        controller.write(0x0000, (byte) 0x0A);
        assertTrue(controller.isRAMEnabled());
    }

    @Test
    public void bankSwitchMode() {
        ByteBuffer rom = ByteBuffer.allocate(CartridgeHeader.ROM_SIZES[1]);
        ByteBuffer ram = ByteBuffer.allocate(CartridgeHeader.RAM_SIZES[0]);
        MBC1 controller = new MBC1(rom.remaining(), ram.remaining(), rom, ram);
        controller.write(0x6000, (byte) 0x01);
        assertTrue("should be in ram mode", controller.isInRamBankingMode());

        controller.write(0x6000, (byte) 0x00);
        assertFalse("should not be in ram mode", controller.isInRamBankingMode());

        controller.write(0x6000, (byte) 0x45);
        assertFalse("should not be in ram mode", controller.isInRamBankingMode());

        controller.write(0x6000, (byte) 0x51);
        assertFalse("should not be in ram mode", controller.isInRamBankingMode());
    }
}
