import org.jglrxavpok.jameboy.JameBoy;
import org.jglrxavpok.jameboy.memory.CartridgeHeader;
import org.jglrxavpok.jameboy.memory.GameROM;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestCartHeader {

    @Test
    public void readMisc() throws IOException {
        byte[] romData = readTestROM("cpu_instrs");
        ByteBuffer buffer = ByteBuffer.wrap(romData);
        GameROM rom = new GameROM(buffer);
        CartridgeHeader header = rom.getHeader();
        assertEquals("Pre CGB is true", false, header.isPreCGB());
        assertEquals("Only CGB game ?", false, header.isOnlyCBG());
        assertEquals("Must be backwards compatible", true, header.isBackwardsCompatible());
    }

    @Test
    public void readCartridgeType() throws IOException {
        byte[] romData = readTestROM("cpu_instrs");
        ByteBuffer buffer = ByteBuffer.wrap(romData);
        GameROM rom = new GameROM(buffer);
        CartridgeHeader header = rom.getHeader();
        assertEquals("MBC1", CartridgeHeader.getCartrigeTypeName(header.getCartrigeType()));
    }

    @Test
    public void testChecksum() throws IOException {
        byte[] romData = readTestROM("cpu_instrs");
        ByteBuffer buffer = ByteBuffer.wrap(romData);
        GameROM rom = new GameROM(buffer);
        CartridgeHeader header = rom.getHeader();
        assertEquals(header.getHeaderChecksum(), header.getHeaderSum());
    }

    @Test
    public void readLogo() throws IOException {
        byte[] romData = readTestROM("cpu_instrs");
        ByteBuffer buffer = ByteBuffer.wrap(romData);
        GameROM rom = new GameROM(buffer);
        CartridgeHeader header = rom.getHeader();
        assertTrue("Invalid Nintendo logo", header.isLogoValid());
    }

    @Test
    public void readTitle() throws IOException {
        byte[] romData = readTestROM("cpu_instrs");
        ByteBuffer buffer = ByteBuffer.wrap(romData);
        GameROM rom = new GameROM(buffer);
        CartridgeHeader header = rom.getHeader();
        assertEquals("CPU_INSTRS", header.getTitle());
    }

    private byte[] readTestROM(String name) throws IOException {
        InputStream in = getClass().getResourceAsStream("/roms/"+name+".gb");
        byte[] buffer = new byte[4*1024];
        int i;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while((i = in.read(buffer)) != -1) {
            out.write(buffer, 0, i);
        }
        out.flush();
        out.close();
        return out.toByteArray();
    }
}
