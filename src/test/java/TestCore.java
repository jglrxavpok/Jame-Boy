import org.jglrxavpok.jameboy.JameBoy;
import org.jglrxavpok.jameboy.memory.GameROM;
import org.jglrxavpok.jameboy.memory.MBC1;
import org.jglrxavpok.jameboy.utils.IOUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static junit.framework.TestCase.assertTrue;

public class TestCore {

    private byte[] readTestROM(String name) throws IOException {
        InputStream in = getClass().getResourceAsStream("/roms/"+name+".gb");
        return IOUtils.read(in);
    }

    @Test
    public void loadROM() throws IOException {
        JameBoy core = new JameBoy();
        GameROM rom = new GameROM(ByteBuffer.wrap(readTestROM("cpu_instrs")));
        core.loadROM(rom);
        assertTrue(core.getMemoryController() instanceof MBC1);
    }
}
