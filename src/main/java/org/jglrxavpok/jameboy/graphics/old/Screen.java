package org.jglrxavpok.jameboy.graphics.old;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Screen extends Sprite {

    public BufferedImage image;

    public Screen(int w, int h) {
        super(w, h);
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        this.width = w;
        this.height = h;
        this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public void copyFromBuffer(int[] screenBuffer) {
        for (int i = 0; i < screenBuffer.length; i += 4) {
            int color = screenBuffer[i + 0] == 0 ? 0xFF000000 : 0xFFFFFFFF;
            pixels[i / 4] = color;
        }
    }
}
