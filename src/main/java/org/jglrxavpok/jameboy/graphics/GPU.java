package org.jglrxavpok.jameboy.graphics;

import java.awt.Graphics;

import org.jglrxavpok.jameboy.JameBoy;

public class GPU
{

    public static final double REFRESH_RATE = 59.73;

    public void render(JameBoy emulator, Screen s, Graphics g, float interpolation)
    {
        s.fillRect(0, 0, s.width, s.height, 0xFF000000);
    }
}
