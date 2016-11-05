import org.jglrxavpok.jameboy.CPU;
import org.jglrxavpok.jameboy.memory.BaseMemoryController;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
        assertEquals((byte)0xFF, cpu.getUpper(cpu.HL));
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
        System.out.println(Arrays.toString(memory));
        testCyclesWithRandomFillAtStart(memory, 12);
        System.out.println(Arrays.toString(memory));
        System.out.println(cpu.A);
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
        int value = ((((int)(Math.random() * 0xF)) << 8) &0xFF) | (int)(Math.random() * 0xF);
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
