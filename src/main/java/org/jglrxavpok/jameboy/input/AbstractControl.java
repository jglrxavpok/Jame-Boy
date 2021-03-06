package org.jglrxavpok.jameboy.input;

import java.util.ArrayList;

public abstract class AbstractControl {

    private static final ArrayList<AbstractControl> controls = new ArrayList<AbstractControl>();

    public AbstractControl() {
        register(this);
    }

    private static void register(AbstractControl c) {
        controls.add(c);
    }

    public static void tickAll() {
        for (AbstractControl control : controls)
            control.tick();
    }

    public abstract void tick();
}
