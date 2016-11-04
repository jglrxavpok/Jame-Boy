import org.jglrxavpok.jameboy.CPU;
import org.jglrxavpok.jameboy.memory.BaseMemoryController;
import org.jglrxavpok.jameboy.memory.MemoryController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestCPU {

    private CPU cpu;
    private TestController controller;

    @Before
    public void init() {
        cpu = new CPU();
        controller = new TestController(new byte[0]);
        cpu.setMemory(controller);
    }

    @Test
    public void ld8bit() {
        testCycles(new byte[] {
                0x06, 0x1 // ld B,1
        }, 8);
        assertEquals(1, cpu.getUpper(cpu.BC));

        testCycles(new byte[] {
                0x0E, 1 // ld C,1
        }, 8);
        assertEquals(1, cpu.getLower(cpu.BC));

        testCycles(new byte[] {
                0x16, 1 // ld D,1
        }, 8);
        assertEquals(1, cpu.getUpper(cpu.DE));

        testCycles(new byte[] {
                0x1E, 1 // ld E,1
        }, 8);
        assertEquals(1, cpu.getLower(cpu.DE));

        testCycles(new byte[] {
                0x26, 1 // ld H,1
        }, 8);
        assertEquals(1, cpu.getUpper(cpu.HL));

        testCycles(new byte[] {
                0x2E, 1 // ld L,1
        }, 8);
        assertEquals(1, cpu.getLower(cpu.HL));

        // ------
        testCyclesWithRandomFillAtStart(new byte[] {
                0x7F // ld A,A
        }, 4);
        assertEquals(cpu.A, cpu.A); // quite stupid indeed

        testCyclesWithRandomFillAtStart(new byte[] {
                0x78 // ld A,B
        }, 4);
        assertEquals(cpu.getUpper(cpu.BC), cpu.A);

        testCyclesWithRandomFillAtStart(new byte[] {
                0x79 // ld A,C
        }, 4);
        assertEquals(cpu.getLower(cpu.BC), cpu.A);

        testCyclesWithRandomFillAtStart(new byte[] {
                0x7A // ld A,D
        }, 4);
        assertEquals(cpu.getUpper(cpu.DE), cpu.A);

        testCyclesWithRandomFillAtStart(new byte[] {
                0x7B // ld A,E
        }, 4);
        assertEquals(cpu.getLower(cpu.DE), cpu.A);

        testCyclesWithRandomFillAtStart(new byte[] {
                0x7C // ld A,H
        }, 4);
        assertEquals(cpu.getUpper(cpu.HL), cpu.A);

        testCyclesWithRandomFillAtStart(new byte[] {
                0x7D // ld A,L
        }, 4);
        assertEquals(cpu.getLower(cpu.HL), cpu.A);

        testCyclesWithRandomFillAtStartAndSetRegister(new byte[] {
                0x7E, (byte)0xFF // ld A,(HL)
        }, "HL", 1, 8);
        assertEquals((byte)0xFF, cpu.A);

        testCyclesWithRandomFillAtStart(new byte[] {
                0x40 // ld B,B
        }, 4);
        assertEquals(cpu.getUpper(cpu.BC), cpu.getUpper(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x41 // ld B,C
        }, 4);
        assertEquals(cpu.getLower(cpu.BC), cpu.getUpper(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x42 // ld B,D
        }, 4);
        assertEquals(cpu.getUpper(cpu.DE), cpu.getUpper(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x43 // ld B,E
        }, 4);
        assertEquals(cpu.getLower(cpu.DE), cpu.getUpper(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x44 // ld B,H
        }, 4);
        assertEquals(cpu.getUpper(cpu.HL), cpu.getUpper(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x45 // ld B,L
        }, 4);
        assertEquals(cpu.getLower(cpu.HL), cpu.getUpper(cpu.BC));

        testCyclesWithRandomFillAtStartAndSetRegister(new byte[] {
                0x46, (byte)0xFF // ld B,(HL)
        }, "HL", 1, 8);
        assertEquals((byte)0xFF, cpu.getUpper(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x48 // ld C,B
        }, 4);
        assertEquals(cpu.getUpper(cpu.BC), cpu.getLower(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x49 // ld C,C
        }, 4);
        assertEquals(cpu.getLower(cpu.BC), cpu.getLower(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x4A // ld C,D
        }, 4);
        assertEquals(cpu.getUpper(cpu.DE), cpu.getLower(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x4B // ld C,E
        }, 4);
        assertEquals(cpu.getLower(cpu.DE), cpu.getLower(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x4C // ld C,H
        }, 4);
        assertEquals(cpu.getUpper(cpu.HL), cpu.getLower(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x4D // ld C,L
        }, 4);
        assertEquals(cpu.getLower(cpu.HL), cpu.getLower(cpu.BC));

        testCyclesWithRandomFillAtStartAndSetRegister(new byte[] {
                0x4E, (byte)0xFF // ld B,(HL)
        }, "HL", 1, 8);
        assertEquals((byte)0xFF, cpu.getLower(cpu.BC));
    }

    private void testCycles(byte[] instruction, int expected) {
        cpu.hardReset();
        cpu.hardGoto(0);
        controller.setRaw(instruction);
        int cycles = cpu.doCycle();
        assertEquals("invalid clock cycle count", expected, cycles);
    }

    private void testCyclesWithRandomFillAtStartAndSetRegister(byte[] instruction, String registerName, int value, int expected) {
        cpu.hardReset();
        cpu.hardGoto(0);
        controller.setRaw(instruction);
        cpu.A = randByte();
        cpu.BC = randByte() << 8 | randByte();
        cpu.HL = randByte() << 8 | randByte();
        cpu.DE = randByte() << 8 | randByte();
        cpu.setRegistryValue(registerName, value);
        int cycles = cpu.doCycle();
        assertEquals("invalid clock cycle count", expected, cycles);
    }

    private void testCyclesWithRandomFillAtStart(byte[] instruction, int expected) {
        cpu.hardReset();
        cpu.hardGoto(0);
        controller.setRaw(instruction);
        cpu.A = randByte();
        cpu.BC = randByte() << 8 | randByte();
        cpu.HL = randByte() << 8 | randByte();
        cpu.DE = randByte() << 8 | randByte();
        int cycles = cpu.doCycle();
        assertEquals("invalid clock cycle count", expected, cycles);
    }

    private byte randByte() {
        int value = (int)(Math.random() * 0x10) + 0x50;
        if(value == 0 || value == 0xFF)
            return randByte();
        return (byte) value;
    }

    private class TestController extends BaseMemoryController {

        private byte[] raw;

        TestController(byte[] raw) {
            this.raw = raw;
        }

        public void setRaw(byte[] raw) {
            this.raw = raw;
        }

        @Override
        public void write(int index, byte value) {
            super.write(index, value);
        }

        @Override
        public byte read(int index) {
            if(index <= 0x3FFF) {
                return raw[index];
            }
            return super.read(index);
        }
    }
}
