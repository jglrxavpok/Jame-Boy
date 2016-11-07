package notunittests;

import org.jglrxavpok.jameboy.JameBoy;
import org.jglrxavpok.jameboy.graphics.GPU;
import org.jglrxavpok.jameboy.memory.NoMBC;

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
        byte[] dump = readRaw("memdumps/DrMarioDump.DMP");
        ByteBuffer rom = ByteBuffer.allocate(0xFFFF+1);
        ByteBuffer ram = ByteBuffer.allocate(32*1024);
        NoMBC memory = new NoMBC(rom, ram);
        memory.setGPU(core.getGPU());
        core.setMemoryController(memory);

        for (int i = GPU.ADDR_VRAM_START; i < GPU.ADDR_VRAM_END; i++) {
            memory.write(i, dump[i]);
        }

        for (int i = GPU.ADDR_OAM_START; i < GPU.ADDR_OAM_END; i++) {
            memory.write(i, dump[i]);
        }

        int index = 0;

        while(index < 154) {
            core.getGPU().step();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
