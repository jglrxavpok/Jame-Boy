package org.jglrxavpok.jameboy.cpu;

import org.jglrxavpok.jameboy.CPU;
import org.jglrxavpok.jameboy.memory.Interrupts;
import org.jglrxavpok.jameboy.utils.BitUtils;

public class Z80Timer {

    public static final int ADDR_DIV_REGISTER = 0xFF04;
    public static final int ADDR_TIMER_COUNTER = 0xFF05;
    public static final int ADDR_TIMER_MODULO = 0xFF06;
    public static final int ADDR_TIMER_CONTROL = 0xFF07;
    public static final long CLOCK_SPEED = 4194304L;

    private final int[] clockSelect = { 4096, 262144, 65536, 16384 };
    private final CPU cpu;
    private byte div;
    private byte timer;
    private int timerCycles;
    private boolean timerRunning;
    private byte timerModulo;
    private int currentSpeedSelector;

    public Z80Timer(CPU cpu) {
        this.cpu = cpu;
    }

    public void postCycles(int cycleCount) {
        div += cycleCount / 4;

        if(timerRunning) {
            timerCycles += cycleCount / 4;
        }

        if(timerRunning && timerCycles > (CLOCK_SPEED / clockSelect[currentSpeedSelector])) {
            timerCycles = 0;

            int next = timer&0xFF +1;
            if(next > 0xFF) {
                next = timerModulo & 0xFF;
                cpu.getMemory().interrupt(Interrupts.TIMER);
            }

            timer = (byte)(next & 0xFF);
        }
    }

    public byte read(int address) {
        switch (address) {
            case ADDR_DIV_REGISTER:
                return div;

            case ADDR_TIMER_COUNTER:
                return timer;

            case ADDR_TIMER_MODULO:
                return timerModulo;

            case ADDR_TIMER_CONTROL:
                byte val = 0x0;
                if(timerRunning)
                    val |= 1<<2;
                val |= currentSpeedSelector & 0x3;
                return val;

            default:
                return (byte) 0x0;
        }
    }

    public void write(int address, byte value) {
        switch (address) {
            case ADDR_DIV_REGISTER:
                div = 0;
                break;

            case ADDR_TIMER_COUNTER:
                timer = value;
                break;

            case ADDR_TIMER_MODULO:
                timerModulo = value;
                break;

            case ADDR_TIMER_CONTROL:
                timerRunning = BitUtils.getBit(value, 2);
                currentSpeedSelector = value & 0x3;
                break;
        }
    }
}
