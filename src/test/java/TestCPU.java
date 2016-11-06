import org.jglrxavpok.jameboy.CPU;
import org.jglrxavpok.jameboy.memory.BaseMemoryController;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theory;

import java.util.Arrays;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// based on http://marc.rawer.de/Gameboy/Docs/GBCPUman.pdf
// and http://www.pastraiser.com/cpu/gameboy/gameboy_opcodes.html
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
    public void misc() {
        swap((byte) 0x37, "A", 8, -1);
        swap((byte) 0x30, "B", 8, -1);
        swap((byte) 0x31, "C", 8, -1);
        swap((byte) 0x32, "D", 8, -1);
        swap((byte) 0x33, "E", 8, -1);
        swap((byte) 0x34, "H", 8, -1);
        swap((byte) 0x35, "L", 8, -1);
        swap((byte) 0x36, "(HL)", 16, 2);

        // TODO: More tests on DAA
        controller.setRaw(new byte[] { 0x27 });
        cpu.hardReset();
        cpu.hardGoto(0);
        int cycles = cpu.doCycle();
        assertEquals(4, cycles);

        // CPL
        controller.setRaw(new byte[] { 0x2F });
        cpu.hardReset();
        cpu.hardGoto(0);
        cpu.A = randByte();
        byte prevA = cpu.A;
        cycles = cpu.doCycle();
        assertEquals(4, cycles);
        assertEquals(~prevA & 0xFF, cpu.A & 0xFF);
        assertTrue(cpu.N);
        assertTrue(cpu.H);

        // CCF
        controller.setRaw(new byte[] { 0x3F });
        cpu.hardReset();
        cpu.hardGoto(0);
        boolean carryFlag = randByte() % 2 == 0;
        cpu.C = carryFlag;
        cycles = cpu.doCycle();
        assertEquals(4, cycles);
        assertEquals(!carryFlag, cpu.C);
        assertFalse(cpu.H);
        assertFalse(cpu.N);

        // SCF
        controller.setRaw(new byte[] { 0x37 });
        cpu.hardReset();
        cpu.hardGoto(0);
        cycles = cpu.doCycle();
        assertEquals(4, cycles);
        assertTrue(cpu.C);
        assertFalse(cpu.H);
        assertFalse(cpu.N);

        // NOP
        controller.setRaw(new byte[] { 0x00 });
        cpu.hardReset();
        cpu.hardGoto(0);
        cycles = cpu.doCycle();
        assertEquals(4, cycles);

        // HALT
        controller.setRaw(new byte[] { 0x76 });
        cpu.hardReset();
        cpu.hardGoto(0);
        cycles = cpu.doCycle();
        assertEquals(4, cycles);
        assertTrue(cpu.isHalted());

        // STOP
        controller.setRaw(new byte[] { 0x10, 0x00 });
        cpu.hardReset();
        cpu.hardGoto(0);
        cycles = cpu.doCycle();
        assertEquals(4, cycles);
        assertTrue(cpu.isStopped());
        cpu.turnOn();

        // DI
        cpu.hardReset();
        cpu.hardGoto(0);
        controller.setRaw(new byte[] {(byte) 0xF3});
        cycles = cpu.doCycle();
        assertEquals(4, cycles);
        assertFalse(cpu.areInterruptsDisabled());
        controller.setRaw(new byte[] {(byte) 0x00}); // interrupts are disabled after the next instruction
        cpu.doCycle();
        assertTrue(cpu.areInterruptsDisabled());
    }

    private void swap(byte cbCode, String valueName, int clockCycles, int valueHL) {
        cpu.hardReset();
        cpu.hardGoto(0);
        byte[] memory = new byte[] {(byte) 0xCB, cbCode, randByte() };
        controller.setRaw(memory);
        cpu.A = randByte();
        cpu.F = randByte();
        cpu.BC = (randByte()&0xFF) << 8 | randByte()&0xFF;
        if(valueHL == -1) {
            cpu.HL = ((randByte()&0xFF) << 8 | randByte()&0xFF) & 0xFFFF;
        } else {
            cpu.HL = valueHL;
        }
        cpu.DE = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.SP = (randByte()&0xFF) << 8 | randByte()&0xFF;

        byte prev = (byte) cpu.getRegistryValue(valueName);
        int cycles = cpu.doCycle();
        byte current = (byte) cpu.getRegistryValue(valueName);
        assertEquals(clockCycles, cycles);
        assertEquals((byte) (((prev >> 4) | ((prev << 4) & 0xFF)) & 0xFF), current);
    }

    @Test
    public void arithmetic16bit() {
        addHL16bit((byte)0x09, "BC", 8);
        addHL16bit((byte)0x19, "DE", 8);
        addHL16bit((byte)0x29, "HL", 8);
        addHL16bit((byte)0x39, "SP", 8);

        addSP(randByte());

        inc16Bit((byte)0x03, "BC", 8);
        inc16Bit((byte)0x13, "DE", 8);
        inc16Bit((byte)0x23, "HL", 8);
        inc16Bit((byte)0x33, "SP", 8);

        dec16Bit((byte)0x0B, "BC", 8);
        dec16Bit((byte)0x1B, "DE", 8);
        dec16Bit((byte)0x2B, "HL", 8);
        dec16Bit((byte)0x3B, "SP", 8);
    }

    private void inc16Bit(byte opcode, String register, int clockCycles) {
        cpu.hardReset();
        cpu.hardGoto(0);
        byte[] memory = new byte[] {opcode};
        controller.setRaw(memory);
        cpu.A = randByte();
        cpu.F = randByte();
        cpu.BC = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.HL = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.DE = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.SP = (randByte()&0xFF) << 8 | randByte()&0xFF;

        int prev = cpu.getRegistryValue(register);
        int cycles = cpu.doCycle();
        assertEquals(clockCycles, cycles);
        assertEquals((prev+1) & 0xFFFF, cpu.getRegistryValue(register) & 0xFFFF);
    }

    private void dec16Bit(byte opcode, String register, int clockCycles) {
        cpu.hardReset();
        cpu.hardGoto(0);
        byte[] memory = new byte[] {opcode};
        controller.setRaw(memory);
        cpu.A = randByte();
        cpu.F = randByte();
        cpu.BC = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.HL = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.DE = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.SP = (randByte()&0xFF) << 8 | randByte()&0xFF;

        int prev = cpu.getRegistryValue(register);
        int cycles = cpu.doCycle();
        assertEquals(clockCycles, cycles);
        assertEquals((prev-1) & 0xFFFF, cpu.getRegistryValue(register) & 0xFFFF);
    }


    private void addSP(byte value) {
        cpu.hardReset();
        cpu.hardGoto(0);
        byte[] memory = new byte[] {(byte) 0xE8, value };
        controller.setRaw(memory);
        cpu.A = randByte();
        cpu.F = randByte();
        cpu.BC = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.HL = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.DE = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.SP = (randByte()&0xFF) << 8 | randByte()&0xFF;

        int prev = cpu.SP;
        int cycles = cpu.doCycle();
        assertEquals(16, cycles);
        assertEquals((prev+value) & 0xFFFF, cpu.SP & 0xFFFF);
    }

    private void addHL16bit(byte opcode, String toAdd, int clockCycles) {
        cpu.hardReset();
        cpu.hardGoto(0);
        byte[] memory = new byte[] { opcode };
        controller.setRaw(memory);
        cpu.A = randByte();
        cpu.F = randByte();
        cpu.BC = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.HL = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.DE = (randByte()&0xFF) << 8 | randByte()&0xFF;
        cpu.SP = (randByte()&0xFF) << 8 | randByte()&0xFF;

        int prev = cpu.HL;
        int valueToAdd = cpu.getRegistryValue(toAdd);
        int cycles = cpu.doCycle();
        assertEquals(clockCycles, cycles);
        assertEquals((prev+valueToAdd) & 0xFFFF, cpu.HL & 0xFFFF);
    }

    @Test
    public void decOpcodes() {
        testDec((byte) 0x3D, "A", 4);
        testDec((byte) 0x05, "B", 4);
        testDec((byte) 0x0D, "C", 4);
        testDec((byte) 0x15, "D", 4);
        testDec((byte) 0x1D, "E", 4);
        testDec((byte) 0x25, "H", 4);
        testDec((byte) 0x2D, "L", 4);
        testDecWithSetRegister((byte) 0x35, "(HL)", 12, "HL", 1);
    }

    private void testDec(byte opcode, String parameter, int clockCycles) {
        testDecWithSetRegister(opcode, parameter, clockCycles, "", 0);
    }

    private void testDecWithSetRegister(byte opcode, String parameter, int clockCycles, String register, int value) {
        cpu.hardReset();
        cpu.hardGoto(0);
        byte[] memory = new byte[] { opcode, randByte() };
        controller.setRaw(memory);
        cpu.A = randByte();
        cpu.F = randByte();
        cpu.BC = randByte() << 8 | randByte();
        cpu.HL = randByte() << 8 | randByte();
        cpu.DE = randByte() << 8 | randByte();
        cpu.SP = randByte() << 8 | randByte();

        cpu.setRegistryValue(register, value);
        int previousValue = cpu.getRegistryValue(parameter);
        int cycles = cpu.doCycle();
        assertEquals(clockCycles, cycles);
        assertEquals((previousValue-1 & 0xFFFF), cpu.getRegistryValue(parameter));
    }

    @Test
    public void incOpcodes() {
        testInc((byte) 0x3C, "A", 4);
        testInc((byte) 0x04, "B", 4);
        testInc((byte) 0x0C, "C", 4);
        testInc((byte) 0x14, "D", 4);
        testInc((byte) 0x1C, "E", 4);
        testInc((byte) 0x24, "H", 4);
        testInc((byte) 0x2C, "L", 4);
        testIncWithSetRegister((byte) 0x34, "(HL)", 12, "HL", 1);
    }

    private void testInc(byte opcode, String parameter, int clockCycles) {
        testIncWithSetRegister(opcode, parameter, clockCycles, "", 0);
    }

    private void testIncWithSetRegister(byte opcode, String parameter, int clockCycles, String register, int value) {
        cpu.hardReset();
        cpu.hardGoto(0);
        byte[] memory = new byte[] { opcode, randByte() };
        controller.setRaw(memory);
        cpu.A = randByte();
        cpu.F = randByte();
        cpu.BC = randByte() << 8 | randByte();
        cpu.HL = randByte() << 8 | randByte();
        cpu.DE = randByte() << 8 | randByte();
        cpu.SP = randByte() << 8 | randByte();

        cpu.setRegistryValue(register, value);
        int previousValue = cpu.getRegistryValue(parameter);
        int cycles = cpu.doCycle();
        assertEquals(clockCycles, cycles);
        assertEquals((previousValue+1 & 0xFFFF), cpu.getRegistryValue(parameter));
    }

    @Test
    public void addOpcodes() {
        BiFunction<Byte, Byte, Byte> add = (a, b) -> (byte) ((a + b) & 0xFF);
        testOperation(new byte[] {
                (byte) 0x87
        }, "A", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x80
        }, "B", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x81
        }, "C", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x82
        }, "D", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x83
        }, "E", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x84
        }, "H", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x85
        }, "L", "A", 4, add);

        testOperationWithSetRegister(new byte[] {
                (byte) 0x86, randByte()
        }, "(HL)", "A", 8, add, "HL", 1);

        testOperation(new byte[] {
                (byte) 0xC6, randByte()
        }, "#", "A", 8, add);

        // ------------ ADC
        testOperation(new byte[] {
                (byte) 0x8F, randByte()
        }, "A", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x88, randByte()
        }, "B", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x89, randByte()
        }, "C", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x8A, randByte()
        }, "D", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x8B, randByte()
        }, "E", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x8C, randByte()
        }, "H", "A", 4, add);

        testOperation(new byte[] {
                (byte) 0x8D, randByte()
        }, "L", "A", 4, add);

        testOperationWithSetRegister(new byte[] {
                (byte) 0x8E, randByte()
        }, "(HL)", "A", 8, add, "HL", 1);

        testOperation(new byte[] {
                (byte) 0xCE, randByte()
        }, "#", "A", 8, add);
    }

    @Test
    public void subOpcodes() {
        // ---------- SUB
        BiFunction<Byte, Byte, Byte> sub = (a, b) -> (byte)((a - b) & 0xFF);
        testOperation(new byte[] {
                (byte) 0x97,
        }, "A", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x90,
        }, "B", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x91,
        }, "C", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x92,
        }, "D", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x93,
        }, "E", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x94,
        }, "H", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x95,
        }, "L", "A", 4, sub);

        testOperationWithSetRegister(new byte[] {
                (byte) 0x96, randByte()
        }, "(HL)", "A", 8, sub, "HL", 1);

        testOperation(new byte[] {
                (byte) 0xD6, randByte()
        }, "#", "A", 8, sub);

        // ----------------- SBC
        testOperation(new byte[] {
                (byte) 0x9F,
        }, "A", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x98,
        }, "B", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x99,
        }, "C", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x9A,
        }, "D", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x9B,
        }, "E", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x9C,
        }, "H", "A", 4, sub);

        testOperation(new byte[] {
                (byte) 0x9D,
        }, "L", "A", 4, sub);

        testOperationWithSetRegister(new byte[] {
                (byte) 0x9E, randByte()
        }, "(HL)", "A", 8, sub, "HL", 1);

        /* Does not exist apparently
        testOperation(new byte[] {
                (byte) 0xD6, randByte()
        }, "#", "A", 8, sub);*/
    }

    @Test
    public void xorOpcodes() {
        BiFunction<Byte, Byte, Byte> xor = (a, b) -> (byte) (a ^ b);
        testOperation(new byte[] {
                (byte) 0xAF
        }, "A", "A", 4, xor);

        testOperation(new byte[] {
                (byte) 0xA8
        }, "B", "A", 4, xor);

        testOperation(new byte[] {
                (byte) 0xA9
        }, "C", "A", 4, xor);

        testOperation(new byte[] {
                (byte) 0xAA
        }, "D", "A", 4, xor);

        testOperation(new byte[] {
                (byte) 0xAB
        }, "E", "A", 4, xor);

        testOperation(new byte[] {
                (byte) 0xAC
        }, "H", "A", 4, xor);

        testOperation(new byte[] {
                (byte) 0xAD
        }, "L", "A", 4, xor);

        testOperationWithSetRegister(new byte[] {
                (byte) 0xAE, randByte()
        }, "(HL)", "A", 8, xor, "HL", 1);

        testOperation(new byte[] {
                (byte) 0xEE, randByte()
        }, "#", "A", 8, xor);
    }

    @Test
    public void compareOpcodes() {
        testCompare((byte) 0xBF, "A", 4);
        testCompare((byte) 0xB8, "B", 4);
        testCompare((byte) 0xB9, "C", 4);
        testCompare((byte) 0xBA, "D", 4);
        testCompare((byte) 0xBB, "E", 4);
        testCompare((byte) 0xBC, "H", 4);
        testCompare((byte) 0xBD, "L", 4);
        testCompareWithSetRegister((byte) 0xBE, "(HL)", 8, "HL", 1);
        testCompare((byte) 0xFE, "#", 8);
    }

    private void testCompare(byte opcode, String value, int clockCycles) {
        testCompareWithSetRegister(opcode, value, clockCycles, "", 0);
    }

    private void testCompareWithSetRegister(byte opcode, String value, int clockCycles, String register, int registerValue) {
        cpu.hardReset();
        cpu.hardGoto(0);
        byte[] memory = new byte[] { opcode, randByte() };
        controller.setRaw(memory);
        cpu.A = randByte();
        cpu.F = randByte();
        cpu.BC = randByte() << 8 | randByte();
        cpu.HL = randByte() << 8 | randByte();
        cpu.DE = randByte() << 8 | randByte();
        cpu.SP = randByte() << 8 | randByte();

        cpu.setRegistryValue(register, registerValue);
        int cycles = cpu.doCycle();
        assertEquals(clockCycles, cycles);
        byte compareTo = value.equals("#") ? memory[1] : (byte) (cpu.getRegistryValue(value) & 0xFF);
        assertEquals(cpu.A == compareTo, cpu.Z);
        assertTrue(cpu.N);
        assertEquals(cpu.A < compareTo, cpu.C);
        assertEquals((cpu.A & 0xF) < (compareTo & 0xF), cpu.H);
    }

    @Test
    public void orOpcodes() {
        BiFunction<Byte, Byte, Byte> or = (a, b) -> (byte) (a | b);
        testOperation(new byte[] {
                (byte) 0xB7
        }, "A", "A", 4, or);

        testOperation(new byte[] {
                (byte) 0xB0
        }, "B", "A", 4, or);

        testOperation(new byte[] {
                (byte) 0xB1
        }, "C", "A", 4, or);

        testOperation(new byte[] {
                (byte) 0xB2
        }, "D", "A", 4, or);

        testOperation(new byte[] {
                (byte) 0xB3
        }, "E", "A", 4, or);

        testOperation(new byte[] {
                (byte) 0xB4
        }, "H", "A", 4, or);

        testOperation(new byte[] {
                (byte) 0xB5
        }, "L", "A", 4, or);

        testOperationWithSetRegister(new byte[] {
                (byte) 0xB6, randByte()
        }, "(HL)", "A", 8, or, "HL", 1);

        testOperation(new byte[] {
                (byte) 0xF6, randByte()
        }, "#", "A", 8, or);
    }

    @Test
    public void andOpcodes() {
        BiFunction<Byte, Byte, Byte> and = (a, b) -> (byte) (a & b);
        testOperation(new byte[] {
                (byte) 0xA7
        }, "A", "A", 4, and);

        testOperation(new byte[] {
                (byte) 0xA0
        }, "B", "A", 4, and);

        testOperation(new byte[] {
                (byte) 0xA1
        }, "C", "A", 4, and);

        testOperation(new byte[] {
                (byte) 0xA2
        }, "D", "A", 4, and);

        testOperation(new byte[] {
                (byte) 0xA3
        }, "E", "A", 4, and);

        testOperation(new byte[] {
                (byte) 0xA4
        }, "H", "A", 4, and);

        testOperation(new byte[] {
                (byte) 0xA5
        }, "L", "A", 4, and);

        testOperationWithSetRegister(new byte[] {
                (byte) 0xA6, randByte()
        }, "(HL)", "A", 8, and, "HL", 1);

        testOperation(new byte[] {
                (byte) 0xE6, randByte()
        }, "#", "A", 8, and);
    }

    private void testOperationWithSetRegister(byte[] memory, String parameter, String destination, int clockCycles, BiFunction<Byte, Byte, Byte> expected, String register, int registerValue) {
        cpu.hardReset();
        cpu.hardGoto(0);
        controller.setRaw(memory);
        cpu.A = randByte();
        cpu.F = randByte();
        cpu.BC = randByte() << 8 | randByte();
        cpu.HL = randByte() << 8 | randByte();
        cpu.DE = randByte() << 8 | randByte();
        cpu.SP = randByte() << 8 | randByte();

        cpu.setRegistryValue(register, registerValue);

        byte baseValue = (byte) (cpu.getRegistryValue(destination) & 0xFF);
        byte param = parameter.equals("#") ? memory[1] : (byte) (cpu.getRegistryValue(parameter) & 0xFF);
        int cycles = cpu.doCycle();
        byte finalValue = (byte) (cpu.getRegistryValue(destination) & 0xFF);
        assertEquals(clockCycles, cycles);
        assertEquals(expected.apply(baseValue, param) & 0xFF, finalValue & 0xFF);
    }

    private void testOperation(byte[] memory, String parameter, String destination, int clockCycles, BiFunction<Byte, Byte, Byte> expected) {
        testOperationWithSetRegister(memory, parameter, destination, clockCycles, expected, "", 0);
    }

    @Test
    public void ld8bitValueToRegister() {
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
    }

    @Test
    public void ld8bitTwoRegisters() {
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
                0x4E, (byte)0xFF // ld C,(HL)
        }, "HL", 1, 8);
        assertEquals((byte)0xFF, cpu.getLower(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x50 // ld D,B
        }, 4);
        assertEquals(cpu.getUpper(cpu.BC), cpu.getUpper(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x51 // ld D,C
        }, 4);
        assertEquals(cpu.getLower(cpu.BC), cpu.getUpper(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x52 // ld D,D
        }, 4);
        assertEquals(cpu.getUpper(cpu.DE), cpu.getUpper(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x53 // ld D,E
        }, 4);
        assertEquals(cpu.getLower(cpu.DE), cpu.getUpper(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x54 // ld D,H
        }, 4);
        assertEquals(cpu.getUpper(cpu.HL), cpu.getUpper(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x55 // ld D,L
        }, 4);
        assertEquals(cpu.getLower(cpu.HL), cpu.getUpper(cpu.DE));

        testCyclesWithRandomFillAtStartAndSetRegister(new byte[] {
                0x56, (byte)0xFF // ld D,(HL)
        }, "HL", 1, 8);
        assertEquals((byte)0xFF, cpu.getUpper(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x58 // ld E,B
        }, 4);
        assertEquals(cpu.getUpper(cpu.BC), cpu.getLower(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x59 // ld E,C
        }, 4);
        assertEquals(cpu.getLower(cpu.BC), cpu.getLower(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x5A // ld E,D
        }, 4);
        assertEquals(cpu.getUpper(cpu.DE), cpu.getLower(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x5B // ld E,E
        }, 4);
        assertEquals(cpu.getLower(cpu.DE), cpu.getLower(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x5C // ld E,H
        }, 4);
        assertEquals(cpu.getUpper(cpu.HL), cpu.getLower(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x5D // ld E,L
        }, 4);
        assertEquals(cpu.getLower(cpu.HL), cpu.getLower(cpu.DE));

        testCyclesWithRandomFillAtStartAndSetRegister(new byte[] {
                0x5E, (byte)0xFF // ld E,(HL)
        }, "HL", 1, 8);
        assertEquals((byte)0xFF, cpu.getLower(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x60 // ld H,B
        }, 4);
        assertEquals(cpu.getUpper(cpu.BC), cpu.getUpper(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x61 // ld H,C
        }, 4);
        assertEquals(cpu.getLower(cpu.BC), cpu.getUpper(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x62 // ld H,D
        }, 4);
        assertEquals(cpu.getUpper(cpu.DE), cpu.getUpper(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x63 // ld H,E
        }, 4);
        assertEquals(cpu.getLower(cpu.DE), cpu.getUpper(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x64 // ld H,H
        }, 4);
        assertEquals(cpu.getUpper(cpu.HL), cpu.getUpper(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x65 // ld H,L
        }, 4);
        assertEquals(cpu.getLower(cpu.HL), cpu.getUpper(cpu.HL));

        testCyclesWithRandomFillAtStartAndSetRegister(new byte[] {
                0x66, (byte)0xFF // ld H,(HL)
        }, "HL", 1, 8);
        assertEquals((byte)0xFF, cpu.getUpper(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x68 // ld L,B
        }, 4);
        assertEquals(cpu.getUpper(cpu.BC), cpu.getLower(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x69 // ld L,C
        }, 4);
        assertEquals(cpu.getLower(cpu.BC), cpu.getLower(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x6A // ld L,D
        }, 4);
        assertEquals(cpu.getUpper(cpu.DE), cpu.getLower(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x6B // ld L,E
        }, 4);
        assertEquals(cpu.getLower(cpu.DE), cpu.getLower(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x6C // ld L,H
        }, 4);
        assertEquals(cpu.getUpper(cpu.HL), cpu.getLower(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                0x6D // ld L,L
        }, 4);
        assertEquals(cpu.getLower(cpu.HL), cpu.getLower(cpu.HL));

        testCyclesWithRandomFillAtStartAndSetRegister(new byte[] {
                0x6E, (byte)0xFF // ld L,(HL)
        }, "HL", 1, 8);
        assertEquals((byte)0xFF, cpu.getLower(cpu.HL));
    }

    @Test
    public void writeRegisterValueToMemoryAtHLAddress() {
        byte[] memory = new byte[] {
                0x70, 0x0/*placeholder for the value to write*/ // ld (HL),B
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 1, 8);
        assertEquals(cpu.getUpper(cpu.BC), memory[1]);

        memory = new byte[] {
                0x71, 0x0/*placeholder for the value to write*/ // ld (HL),C
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 1, 8);
        assertEquals(cpu.getLower(cpu.BC), memory[1]);

        memory = new byte[] {
                0x72, 0x0/*placeholder for the value to write*/ // ld (HL),D
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 1, 8);
        assertEquals(cpu.getUpper(cpu.DE), memory[1]);

        memory = new byte[] {
                0x73, 0x0/*placeholder for the value to write*/ // ld (HL),E
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 1, 8);
        assertEquals(cpu.getLower(cpu.DE), memory[1]);

        memory = new byte[] {
                0x74, 0x0/*placeholder for the value to write*/ // ld (HL),H
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 1, 8);
        assertEquals(cpu.getUpper(cpu.HL), memory[1]);

        memory = new byte[] {
                0x75, 0x0/*placeholder for the value to write*/ // ld (HL),L
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 1, 8);
        assertEquals(cpu.getLower(cpu.HL), memory[1]);

        memory = new byte[] {
                0x36, 0x50, 0x0 // ld (HL),n
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 2, 12);
        assertEquals(0x50, memory[1]);
    }

    @Test
    public void ld8bitIntoA() {
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
                0x0A, (byte) 0xFF // ld A,(BC)
        }, "BC", 1, 8);
        assertEquals((byte)0xFF, cpu.A);

        testCyclesWithRandomFillAtStartAndSetRegister(new byte[] {
                0x1A, (byte) 0xFF // ld A,(DE)
        }, "DE", 1, 8);
        assertEquals((byte)0xFF, cpu.A);

        testCyclesWithRandomFillAtStartAndSetRegister(new byte[] {
                0x7E, (byte) 0xFF // ld A,(HL)
        }, "HL", 1, 8);
        assertEquals((byte)0xFF, cpu.A);

        testCyclesWithRandomFillAtStart(new byte[] {
                (byte) 0xFA, 0x2, (byte) 0xFF // ld A,(nn)
        }, 16);
        assertEquals((byte)0xFF, cpu.A);

        testCyclesWithRandomFillAtStart(new byte[] {
                (byte) 0x3E, (byte) 0xFF // ld A,#
        }, 8);
        assertEquals((byte)0xFF, cpu.A);
    }

    @Test
    public void ld8bitAInto() {
        testCyclesWithRandomFillAtStart(new byte[] {
                (byte) 0x47 // ld B,A
        }, 4);
        assertEquals(cpu.A, cpu.getUpper(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                (byte) 0x4F // ld C,A
        }, 4);
        assertEquals(cpu.A, cpu.getLower(cpu.BC));

        testCyclesWithRandomFillAtStart(new byte[] {
                (byte) 0x57 // ld D,A
        }, 4);
        assertEquals(cpu.A, cpu.getUpper(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                (byte) 0x5F // ld E,A
        }, 4);
        assertEquals(cpu.A, cpu.getLower(cpu.DE));

        testCyclesWithRandomFillAtStart(new byte[] {
                (byte) 0x67 // ld H,A
        }, 4);
        assertEquals(cpu.A, cpu.getUpper(cpu.HL));

        testCyclesWithRandomFillAtStart(new byte[] {
                (byte) 0x6F // ld L,A
        }, 4);
        assertEquals(cpu.A, cpu.getLower(cpu.HL));
    }

    @Test
    public void writeAValueToMemoryAtAddress() {
        byte[] memory = new byte[] {
                0x02, 0x0/*placeholder for the value to write*/, 0x1 // ld (BC),A
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "BC", 1, 8);
        assertEquals(cpu.A, memory[1]);

        memory = new byte[] {
                0x12, 0x0/*placeholder for the value to write*/, 0x1 // ld (DE),A
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "DE", 1, 8);
        assertEquals(cpu.A, memory[1]);

        memory = new byte[] {
                0x77, 0x0/*placeholder for the value to write*/, 0x1 // ld (HL),A
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 1, 8);
        assertEquals(cpu.A, memory[1]);

        memory = new byte[] {
                (byte) 0xEA, 0x1 // ld (nn),A
        };
        testCyclesWithRandomFillAtStart(memory, 16);
        assertEquals(cpu.A, memory[1]);
    }

    @Test
    public void ld8bitWithOffsetsAndIncrementsAndDecrements() {
        byte[] memory = new byte[] {
                (byte) 0xF2, // ld A,($FF00+C)
                0x0, 0x0, 0x0, 0x0, 0x0, (byte) 0x50/*value to read*/
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "BC", 1, 8);
        assertEquals(memory[6], cpu.A);

        memory = new byte[] {
                (byte) 0xE2, // ld ($FF00+C),A
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0/*value to override*/
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "BC", 1, 8);
        assertEquals(cpu.A, memory[6]);

        // -----------------

        memory = new byte[] {
                (byte) 0x3A, 0x1, // ld A,(HLD)
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 1, 8);
        assertEquals(0x1, cpu.A);
        assertEquals(0, cpu.HL); // check decrement

        // -----------------
        memory = new byte[] {
                (byte) 0x32, 0x1, // ld (HLD),A
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 1, 8);
        assertEquals(cpu.A, memory[1]);
        assertEquals(0, cpu.HL); // check decrement

        // -----------------

        memory = new byte[] {
                (byte) 0x2A, 0x1, // ld A,(HLI)
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 1, 8);
        assertEquals(0x1, cpu.A);
        assertEquals(2, cpu.HL); // check decrement

        // -----------------
        memory = new byte[] {
                (byte) 0x22, 0x1, // ld (HLI),A
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "HL", 1, 8);
        assertEquals(cpu.A, memory[1]);
        assertEquals(2, cpu.HL); // check decrement

        // ---------
        memory = new byte[] {
                (byte) 0xE0, // ld ($FF00+n),A
                0x1, 0x0, 0x0, 0x0, 0x0, 0x0/*value to override*/
        };
        testCyclesWithRandomFillAtStart(memory, 12);
        assertEquals(cpu.A, memory[6]);

        memory = new byte[] {
                (byte) 0xF0, // ld A,($FF00+n)
                0x1, 0x0, 0x0, 0x0, 0x0, (byte) 0xFF/*value to write*/
        };
        testCyclesWithRandomFillAtStart(memory, 12);
        assertEquals(memory[6], cpu.A);
    }

    @Test
    public void ld16bit() {
        testCyclesWithRandomFillAtStart(new byte[] {
             0x01, (byte) 0xFF, (byte) 0xFF // ld BC,nn
        }, 12);
        assertEquals(0xFFFF, cpu.BC & 0xFFFF);

        testCyclesWithRandomFillAtStart(new byte[] {
                0x11, (byte) 0xFF, (byte) 0xFF // ld DE,nn
        }, 12);
        assertEquals(0xFFFF, cpu.DE & 0xFFFF);

        testCyclesWithRandomFillAtStart(new byte[] {
                0x21, (byte) 0xFF, (byte) 0xFF // ld HL,nn
        }, 12);
        assertEquals(0xFFFF, cpu.HL & 0xFFFF);

        testCyclesWithRandomFillAtStart(new byte[] {
                0x31, (byte) 0xFF, (byte) 0xFF // ld SP,nn
        }, 12);
        assertEquals(0xFFFF, cpu.SP & 0xFFFF);

        // ----

        testCyclesWithRandomFillAtStart(new byte[] {
                (byte) 0xF9, // ld SP,HL
        }, 8);
        assertEquals(cpu.HL & 0xFFFF, cpu.SP & 0xFFFF);

        testCyclesWithRandomFillAtStartAndSetRegister(new byte[] {
                (byte) 0xF8, 0x1,// ldhl SP,n
        }, "SP", 1, 12);
        assertEquals(cpu.SP +1, cpu.HL);
        assertFalse(cpu.Z);
        assertFalse(cpu.N);

        testCyclesWithRandomFillAtStartAndSetRegister(new byte[] {
                (byte) 0xF8, (byte) 0xFF,// ldhl SP,n
        }, "SP", 1, 12);
        assertEquals(cpu.SP -1, cpu.HL);
        assertFalse(cpu.Z);
        assertFalse(cpu.N);

        // --------------
        byte[] memory = new byte[] {
                (byte) 0x08, 0x03, 0x0, (byte) 0xF0, (byte) 0xFF // ld (nn)S,SP
        };
        testCyclesWithRandomFillAtStart(memory, 20);
        assertEquals(cpu.getLower(cpu.SP), memory[3]);
        assertEquals(cpu.getUpper(cpu.SP), memory[4]);

        // ------------
        memory = new byte[] {
                (byte) 0xF5, (byte) 0xF4 /*garbage*/, 0x0, 0x0// push AF
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "SP", 2, 16);
        assertEquals(cpu.SP, 0);
        assertEquals(cpu.A & 0xFF, memory[3] & 0xFF);
        assertEquals(cpu.F & 0xFF, memory[2] & 0xFF);

        memory = new byte[] {
                (byte) 0xC5, (byte) 0xF4 /*garbage*/, 0x0, 0x0// push BC
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "SP", 2, 16);
        assertEquals(cpu.SP, 0);
        assertEquals(cpu.getLower(cpu.BC) & 0xFF, memory[2] & 0xFF);
        assertEquals(cpu.getUpper(cpu.BC) & 0xFF, memory[3] & 0xFF);

        memory = new byte[] {
                (byte) 0xD5, (byte) 0xF4 /*garbage*/, 0x0, 0x0// push DE
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "SP", 2, 16);
        assertEquals(cpu.SP, 0);
        assertEquals(cpu.getLower(cpu.DE) & 0xFF, memory[2] & 0xFF);
        assertEquals(cpu.getUpper(cpu.DE) & 0xFF, memory[3] & 0xFF);

        memory = new byte[] {
                (byte) 0xE5, (byte) 0xF4 /*garbage*/, 0x0, 0x0// push HL
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "SP", 2, 16);
        assertEquals(cpu.SP, 0);
        assertEquals(cpu.getLower(cpu.HL) & 0xFF, memory[2] & 0xFF);
        assertEquals(cpu.getUpper(cpu.HL) & 0xFF, memory[3] & 0xFF);

        // ---------
        memory = new byte[] {
                (byte) 0xF1, (byte) 0xF4 /*garbage*/, 0x0, 0x0// pop AF
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "SP", 2, 12);
        assertEquals(cpu.SP, 4);
        assertEquals(memory[2] & 0xFF, cpu.F & 0xFF);
        assertEquals(memory[3] & 0xFF, cpu.A & 0xFF);

        memory = new byte[] {
                (byte) 0xC1, (byte) 0xF4 /*garbage*/, 0x0, 0x0// pop BC
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "SP", 2, 12);
        assertEquals(cpu.SP, 4);
        assertEquals(memory[2] & 0xFF, cpu.getLower(cpu.BC) & 0xFF);
        assertEquals(memory[3] & 0xFF, cpu.getUpper(cpu.BC) & 0xFF);

        memory = new byte[] {
                (byte) 0xD1, (byte) 0xF4 /*garbage*/, 0x0, 0x0// pop DE
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "SP", 2, 12);
        assertEquals(cpu.SP, 4);
        assertEquals(memory[2] & 0xFF, cpu.getLower(cpu.DE) & 0xFF);
        assertEquals(memory[3] & 0xFF, cpu.getUpper(cpu.DE) & 0xFF);

        memory = new byte[] {
                (byte) 0xE1, (byte) 0xF4 /*garbage*/, 0x0, 0x0// pop HL
        };
        testCyclesWithRandomFillAtStartAndSetRegister(memory, "SP", 2, 12);
        assertEquals(cpu.SP, 4);
        assertEquals(memory[2] & 0xFF, cpu.getLower(cpu.HL) & 0xFF);
        assertEquals(memory[3] & 0xFF, cpu.getUpper(cpu.HL) & 0xFF);
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
        cpu.F = randByte();
        cpu.BC = randByte() << 8 | randByte();
        cpu.HL = randByte() << 8 | randByte();
        cpu.DE = randByte() << 8 | randByte();
        cpu.SP = randByte() << 8 | randByte();
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
        cpu.SP = randByte() << 8 | randByte();
        int cycles = cpu.doCycle();
        assertEquals("invalid clock cycle count", expected, cycles);
    }

    private byte randByte() {
        int value = ((((int)(Math.random() * 0xF)) << 4) &0xFF) | (int)(Math.random() * 0xF);
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
            if(index <= 0x3FFF) {
                raw[index] = value;
            }

            if(index >= 0xFF00 && index <= 0xFFFF) {
                raw[index - 0xFF00 + 5] = value;
            }
            super.write(index, value);
        }

        @Override
        public byte read(int index) {
            if(index <= 0x3FFF) {
                return raw[index];
            }

            if(index >= 0xFF00 && index <= 0xFFFF) {
                return raw[index - 0xFF00 + 5];
            }
            return super.read(index);
        }
    }
}
