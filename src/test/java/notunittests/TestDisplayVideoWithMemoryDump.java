package notunittests;

import org.jglrxavpok.jameboy.JameBoy;
import org.jglrxavpok.jameboy.graphics.GPU;
import org.jglrxavpok.jameboy.memory.NoMBC;
import org.jglrxavpok.jameboy.utils.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class TestDisplayVideoWithMemoryDump {

    public static void main(String[] args) throws IOException {
        JameBoy core = new JameBoy();
        byte[] dump = readRaw("memdumps/PokemonYellowBattle.DMP");
        ByteBuffer rom = ByteBuffer.allocate(0xFFFF+1);
        ByteBuffer ram = ByteBuffer.allocate(32*1024);
        NoMBC memory = new NoMBC(rom, ram);
        memory.setGPU(core.getGPU());
        core.setMemoryController(memory);
        core.getGPU().linkToMemory(memory);

        for (int i = 0; i < dump.length; i++) {
            if(core.getGPU().isValidGPUAddress(i))
                memory.write(i, dump[i]);
        }

        int index = 0;
        while(index < 154) {
            core.getGPU().step(456);
            index++;
        }

        BufferedImage image = new BufferedImage(GPU.WIDTH, GPU.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < GPU.HEIGHT; y++) {
            for (int x = 0; x < GPU.WIDTH; x++) {
                image.setRGB(x, y, core.getGPU().getPixels()[x + y*GPU.WIDTH]);
            }
        }

        ImageIO.write(image, "png", new File("./src/test/resources/videoDump.png"));
    }

    private static byte[] readRaw(String name) throws IOException {
        InputStream in = TestDisplayVideoWithMemoryDump.class.getResourceAsStream("/"+name);
        return IOUtils.read(in);
    }
}
