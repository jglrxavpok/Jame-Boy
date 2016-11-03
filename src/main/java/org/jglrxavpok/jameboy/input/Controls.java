package org.jglrxavpok.jameboy.input;

import java.awt.event.KeyEvent;

public class Controls {

    public static final KeyControl left = new KeyControl(KeyEvent.VK_Q);
    public static final KeyControl right = new KeyControl(KeyEvent.VK_D);
    public static final KeyControl jump = new KeyControl(KeyEvent.VK_SPACE);
    public static final KeyControl crouch = new KeyControl(KeyEvent.VK_SHIFT);
    public static final KeyControl fire = new KeyControl(KeyEvent.VK_ENTER);
}
