package org.jglrxavpok.jameboy.graphics.old;

import org.jglrxavpok.jameboy.JameBoyApp;

import java.awt.*;

public class OldGPU {

    public static final double REFRESH_RATE = 59.73;

    public void render(JameBoyApp emulator, Screen s, Graphics g, float interpolation) {
        s.fillRect(0, 0, s.width, s.height, 0xFF000000);
    }
}
