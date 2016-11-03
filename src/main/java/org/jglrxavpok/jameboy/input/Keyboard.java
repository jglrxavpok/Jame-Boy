package org.jglrxavpok.jameboy.input;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

public class Keyboard implements KeyListener {

    private static Keyboard instance;
    private static HashMap<Integer, Boolean> keysDown = new HashMap<Integer, Boolean>();

    private static Keyboard getInstance() {
        if (instance == null)
            instance = new Keyboard();
        return instance;
    }

    public static void init(Component c) {
        c.addKeyListener(getInstance());
    }

    public static boolean isKeyDown(int key) {
        Boolean b = keysDown.get(key);
        if (b == null)
            return false;
        else
            return b;
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        keysDown.put(arg0.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        keysDown.put(arg0.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent arg0) {

    }
}
