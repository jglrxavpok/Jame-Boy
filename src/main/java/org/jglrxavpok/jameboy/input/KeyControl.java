package org.jglrxavpok.jameboy.input;

public class KeyControl extends AbstractControl {

    public boolean pressed;
    private int key;

    public KeyControl(int keycode) {
        this.key = keycode;
    }

    public void tick() {
        this.pressed = Keyboard.isKeyDown(key);
    }
}
