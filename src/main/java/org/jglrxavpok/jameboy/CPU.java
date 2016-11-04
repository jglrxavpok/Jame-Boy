package org.jglrxavpok.jameboy;

import org.jglrxavpok.jameboy.memory.MemoryController;

/**
 * Emulates a LR35902 CPU (used in Nintendo's Game Boy console)
 *
 * @author jglrxavpok
 */
public class CPU {

    public int PC = 0x100;
    public int SP = 0;
    private int clockCycles = 0;
    private JameBoyApp emulator;
    public byte A;
    public byte F;
    public boolean Z;
    public boolean N;
    public boolean H;
    public boolean C;
    public int BC, DE, HL;
    private boolean stop;
    private boolean halted;
    private MemoryController memory;

    public void setEmulator(JameBoyApp emu) {
        this.emulator = emu;
    }

    public void setMemory(MemoryController memory) {
        this.memory = memory;
    }

    public int doCycle() {
        if (stop)
            return -1;
        int opcode = nextByte();
        clockCycles = 4;
        return executeOP(opcode);
    }

    private int nextPart() {
        return (short) ((short) (nextByte()) | (nextByte() << 8));
    }

    private byte nextByte() {
        return this.memory.read(PC++);
    }

    public void pushPart(int val) {
        SP -= 2;
        writePart(SP, val);
    }

    public int popPart() {
        int val = (this.memory.read(SP))
                | ((this.memory.read(SP + 1)) << 8);
        SP += 2;
        return val;
    }

    public void writePart(int pos, int val) {
        this.memory.write(pos, (byte) (val & 0xFF));
        this.memory.write(pos + 1, (byte) ((val >> 8) & 0xFF));
    }

    public void hardGoto(int index) {
        PC = index;
    }

    public void hardReset() {
        A = 0;
        F = 0;
        Z = N = C = H = false;
        BC = DE = HL = 0;
        SP = 0;
        PC = 0x100;
    }

    private int executeOP(int opcode) {
        switch (opcode) {
            case 0x00: {
                op_nop();
                break;
            }
            case 0x01: {
                op_LD_BC();
                break;
            }
            case 0x02: {
                op_LD_BC_A();
                break;
            }
            case 0x03: {
                op_INC_BC();
                break;
            }
            case 0x04: {
                op_INC_B();
                break;
            }
            case 0x05: {
                op_DEC_B();
                break;
            }
            case 0x06: {
                op_LD_B();
                break;
            }
            case 0x07: {
                op_RLCA();
                break;
            }
            case 0x08: {
                op_LD_SP8b();
                break;
            }
            case 0x09: {
                op_LD_ADD_HL_BC();
                break;
            }
            case 0x0A: {
                op_LD_A_BC();
                break;
            }
            case 0x0B: {
                op_DEC_BC();
                break;
            }
            case 0x0C: {
                op_INC_C();
                break;
            }
            case 0x0D: {
                op_DEC_C();
                break;
            }
            case 0x0E: {
                op_LD_C();
                break;
            }
            case 0x0F: {
                op_RRCA();
                break;
            }
            case 0x10: {
                op_STOP();
                break;
            }
            case 0x11: {
                op_LD_DE();
                break;
            }
            case 0x12: {
                op_LD_DE_A();
                break;
            }
            case 0x13: {
                op_INC_DE();
                break;
            }
            case 0x14: {
                op_INC_D();
                break;
            }
            case 0x15: {
                op_DEC_D();
                break;
            }
            case 0x16: {
                op_LD_D();
                break;
            }
            case 0x17: {
                op_RLA();
                break;
            }
            case 0x18: {
                op_JR();
                break;
            }
            case 0x19: {
                op_ADD_HL_DE();
                break;
            }
            case 0x1A: {
                op_LD_A_DE();
                break;
            }
            case 0x1B: {
                op_DEC_DE();
                break;
            }
            case 0x1C: {
                op_INC_E();
                break;
            }
            case 0x1D: {
                op_DEC_E();
                break;
            }
            case 0x1E: {
                op_LD_E();
                break;
            }
            case 0x1F: {
                op_RRA();
                break;
            }
            case 0x20: {
                op_JR_NZ();
                break;
            }
            case 0x21: {
                op_LD_HL();
                break;
            }
            case 0x22: {
                op_LD_HL_INC_A();
                break;
            }
            case 0x23: {
                op_INC_HL();
                break;
            }
            case 0x24: {
                op_INC_H();
                break;
            }
            case 0x25: {
                op_DEC_H();
                break;
            }
            case 0x26: {
                op_LD_H();
                break;
            }
            case 0x27: {
                op_DAA();
                break;
            }
            case 0x28: {
                op_JR_Z();
                break;
            }
            case 0x29: {
                op_ADD_HL_HL();
                break;
            }
            case 0x2A: {
                op_LD_A_HL_INC();
                break;
            }
            case 0x2B: {
                op_DEC_HL();
                break;
            }
            case 0x2C: {
                op_INC_L();
                break;
            }
            case 0x2D: {
                op_DEC_L();
                break;
            }
            case 0x2E: {
                op_LD_L();
                break;
            }
            case 0x2F: {
                op_CPL();
                break;
            }
            case 0x30: {
                op_JR_NC();
                break;
            }
            case 0x31: {
                op_LD_SP16b();
                break;
            }
            case 0x32: {
                op_LD_HL_DEC_A();
                break;
            }
            case 0x33: {
                op_INC_SP();
                break;
            }
            case 0x34: {
                op_INC_HL_VALUE();
                break;
            }
            case 0x35: {
                op_DEC_HL_VALUE();
                break;
            }
            case 0x36: {
                op_LD_HL_VALUE();
                break;
            }
            case 0x37: {
                op_SCF();
                break;
            }
            case 0x38: {
                op_JR_C();
                break;
            }
            case 0x39: {
                op_ADD_HL_SP();
                break;
            }
            case 0x3A: {
                op_LD_A_HL_DEC();
                break;
            }
            case 0x3B: {
                op_DEC_SP();
                break;
            }
            case 0x3C: {
                op_INC_A();
                break;
            }
            case 0x3D: {
                op_DEC_A();
                break;
            }
            case 0x3E: {
                op_LD_A();
                break;
            }
            case 0x3F: {
                op_CCF();
                break;
            }
            case 0x40: {
                op_LD_B_B();
                break;
            }
            case 0x41: {
                op_LD_B_C();
                break;
            }
            case 0x42: {
                op_LD_B_D();
                break;
            }
            case 0x43: {
                op_LD_B_E();
                break;
            }
            case 0x44: {
                op_LD_B_H();
                break;
            }
            case 0x45: {
                op_LD_B_L();
                break;
            }
            case 0x46: {
                op_LD_B_HL_VALUE();
                break;
            }
            case 0x47: {
                op_LD_B_A();
                break;
            }
            case 0x48: {
                op_LD_C_B();
                break;
            }
            case 0x49: {
                op_LD_C_C();
                break;
            }
            case 0x4A: {
                op_LD_C_D();
                break;
            }
            case 0x4B: {
                op_LD_C_E();
                break;
            }
            case 0x4C: {
                op_LD_C_H();
                break;
            }
            case 0x4D: {
                op_LD_C_L();
                break;
            }
            case 0x4E: {
                op_LD_C_HL_VALUE();
                break;
            }
            case 0x4F: {
                op_LD_C_A();
                break;
            }
            case 0x50: {
                op_LD_D_B();
                break;
            }
            case 0x51: {
                op_LD_D_C();
                break;
            }
            case 0x52: {
                op_LD_D_D();
                break;
            }
            case 0x53: {
                op_LD_D_E();
                break;
            }
            case 0x54: {
                op_LD_D_H();
                break;
            }
            case 0x55: {
                op_LD_D_L();
                break;
            }
            case 0x56: {
                op_LD_D_HL_VALUE();
                break;
            }
            case 0x57: {
                op_LD_D_A();
                break;
            }
            case 0x58: {
                op_LD_E_B();
                break;
            }
            case 0x59: {
                op_LD_E_C();
                break;
            }
            case 0x5A: {
                op_LD_E_D();
                break;
            }
            case 0x5B: {
                op_LD_E_E();
                break;
            }
            case 0x5C: {
                op_LD_E_H();
                break;
            }
            case 0x5D: {
                op_LD_E_L();
                break;
            }
            case 0x5E: {
                op_LD_E_HL_VALUE();
                break;
            }
            case 0x5F: {
                op_LD_E_A();
                break;
            }
            case 0x60: {
                op_LD_H_B();
                break;
            }
            case 0x61: {
                op_LD_H_C();
                break;
            }
            case 0x62: {
                op_LD_H_D();
                break;
            }
            case 0x63: {
                op_LD_H_E();
                break;
            }
            case 0x64: {
                op_LD_H_H();
                break;
            }
            case 0x65: {
                op_LD_H_L();
                break;
            }
            case 0x66: {
                op_LD_H_HL_VALUE();
                break;
            }
            case 0x67: {
                op_LD_H_A();
                break;
            }
            case 0x68: {
                op_LD_L_B();
                break;
            }
            case 0x69: {
                op_LD_L_C();
                break;
            }
            case 0x6A: {
                op_LD_L_D();
                break;
            }
            case 0x6B: {
                op_LD_L_E();
                break;
            }
            case 0x6C: {
                op_LD_L_H();
                break;
            }
            case 0x6D: {
                op_LD_L_L();
                break;
            }
            case 0x6E: {
                op_LD_L_HL_VALUE();
                break;
            }
            case 0x6F: {
                op_LD_L_A();
                break;
            }
            case 0x70: {
                op_LD_HL_VALUE_B();
                break;
            }
            case 0x71: {
                op_LD_HL_VALUE_C();
                break;
            }
            case 0x72: {
                op_LD_HL_VALUE_D();
                break;
            }
            case 0x73: {
                op_LD_HL_VALUE_E();
                break;
            }
            case 0x74: {
                op_LD_HL_VALUE_H();
                break;
            }
            case 0x75: {
                op_LD_HL_VALUE_L();
                break;
            }
            case 0x76: {
                op_HALT();
                break;
            }
            case 0x77: {
                op_LD_HL_VALUE_A();
                break;
            }
            case 0x78: {
                op_LD_A_B();
                break;
            }
            case 0x79: {
                op_LD_A_C();
                break;
            }
            case 0x7A: {
                op_LD_A_D();
                break;
            }
            case 0x7B: {
                op_LD_A_E();
                break;
            }
            case 0x7C: {
                op_LD_A_H();
                break;
            }
            case 0x7D: {
                op_LD_A_L();
                break;
            }
            case 0x7E: {
                op_LD_A_HL_VALUE();
                break;
            }
            case 0x7F: {
                op_LD_A_A();
                break;
            }
            case 0x80: {
                op_ADD_A_B();
                break;
            }
            case 0x81: {
                op_ADD_A_C();
                break;
            }
            case 0x82: {
                op_ADD_A_D();
                break;
            }
            case 0x83: {
                op_ADD_A_E();
                break;
            }
            case 0x84: {
                op_ADD_A_H();
                break;
            }
            case 0x85: {
                op_ADD_A_L();
                break;
            }
            case 0x86: {
                op_ADD_A_HL_VALUE();
                break;
            }
            case 0x87: {
                op_ADD_A_A();
                break;
            }
            case 0x88: {
                op_ADC_A_B();
                break;
            }
            case 0x89: {
                op_ADC_A_C();
                break;
            }
            case 0x8A: {
                op_ADC_A_D();
                break;
            }
            case 0x8B: {
                op_ADC_A_E();
                break;
            }
            case 0x8C: {
                op_ADC_A_H();
                break;
            }
            case 0x8D: {
                op_ADC_A_L();
                break;
            }
            case 0x8E: {
                op_ADC_A_HL_VALUE();
                break;
            }
            case 0x8F: {
                op_ADC_A_A();
                break;
            }
            case 0x90: {
                op_SUB_B();
                break;
            }
            case 0x91: {
                op_SUB_C();
                break;
            }
            case 0x92: {
                op_SUB_D();
                break;
            }
            case 0x93: {
                op_SUB_E();
                break;
            }
            case 0x94: {
                op_SUB_H();
                break;
            }
            case 0x95: {
                op_SUB_L();
                break;
            }
            case 0x96: {
                op_SUB_HL_VALUE();
                break;
            }
            case 0x97: {
                op_SUB_A();
                break;
            }
            case 0x98: {
                op_SBC_A_B();
                break;
            }
            case 0x99: {
                op_SBC_A_C();
                break;
            }
            case 0x9A: {
                op_SBC_A_D();
                break;
            }
            case 0x9B: {
                op_SBC_A_E();
                break;
            }
            case 0x9C: {
                op_SBC_A_H();
                break;
            }
            case 0x9D: {
                op_SBC_A_L();
                break;
            }
            case 0x9E: {
                op_SBC_A_HL_VALUE();
                break;
            }
            case 0x9F: {
                op_SBC_A_A();
                break;
            }
            case 0xA0: {
                op_AND_B();
                break;
            }
            case 0xA1: {
                op_AND_C();
                break;
            }
            case 0xA2: {
                op_AND_D();
                break;
            }
            case 0xA3: {
                op_AND_E();
                break;
            }
            case 0xA4: {
                op_AND_H();
                break;
            }
            case 0xA5: {
                op_AND_L();
                break;
            }
            case 0xA6: {
                op_AND_HL_VALUE();
                break;
            }
            case 0xA7: {
                op_AND_A();
                break;
            }
            case 0xA8: {
                op_XOR_B();
                break;
            }
            case 0xA9: {
                op_XOR_C();
                break;
            }
            case 0xAA: {
                op_XOR_D();
                break;
            }
            case 0xAB: {
                op_XOR_E();
                break;
            }
            case 0xAC: {
                op_XOR_H();
                break;
            }
            case 0xAD: {
                op_XOR_L();
                break;
            }
            case 0xAE: {
                op_XOR_HL_VALUE();
                break;
            }
            case 0xAF: {
                op_XOR_A();
                break;
            }
            case 0xB0: {
                op_OR_B();
                break;
            }
            case 0xB1: {
                op_OR_C();
                break;
            }
            case 0xB2: {
                op_OR_D();
                break;
            }
            case 0xB3: {
                op_OR_E();
                break;
            }
            case 0xB4: {
                op_OR_H();
                break;
            }
            case 0xB5: {
                op_OR_L();
                break;
            }
            case 0xB6: {
                op_OR_HL_VALUE();
                break;
            }
            case 0xB7: {
                op_OR_A();
                break;
            }
            case 0xB8: {
                op_CP_B();
                break;
            }
            case 0xB9: {
                op_CP_C();
                break;
            }
            case 0xBA: {
                op_CP_D();
                break;
            }
            case 0xBB: {
                op_CP_E();
                break;
            }
            case 0xBC: {
                op_CP_H();
                break;
            }
            case 0xBD: {
                op_CP_L();
                break;
            }
            case 0xBE: {
                op_CP_HL_VALUE();
                break;
            }
            case 0xBF: {
                op_CP_A();
                break;
            }
            case 0xC0: {
                op_RET_NZ();
                break;
            }
            case 0xC1: {
                op_POP_BC();
                break;
            }
            case 0xC2: {
                op_JP_NZ();
                break;
            }
            case 0xC3: {
                op_JP();
                break;
            }
            case 0xC4: {
                op_CALL_NZ();
                break;
            }
            case 0xC5: {
                op_PUSH_BC();
                break;
            }
            case 0xC6: {
                op_ADD_A();
                break;
            }
            case 0xC7: {
                op_RST_00H();
                break;
            }
            case 0xC8: {
                op_RET_Z();
                break;
            }
            case 0xC9: {
                op_RET();
                break;
            }
            case 0xCA: {
                op_JP_Z();
                break;
            }
            case 0xCB: {
                op_CBCode();
                break;
            }
            case 0xCC: {
                op_CALL_Z();
                break;
            }
            case 0xCD: {
                op_CALL();
                break;
            }
            case 0xCE: {
                op_ADC_A();
                break;
            }
            case 0xCF: {
                op_RST_08H();
                break;
            }
            case 0xD0: {
                op_RET_NC();
                break;
            }
            case 0xD1: {
                op_POP_DE();
                break;
            }
            case 0xD2: {
                op_JP_NC();
                break;
            }
            case 0xD3: {
                op_NULL();
                break;
            }
            case 0xD4: {
                op_CALL_NC();
                break;
            }
            default: {
                System.out.println("[Jame Boy] Unknown opcode: " + Integer.toHexString(opcode));
                break;
            }
        }
        return clockCycles;
    }

    public void op_CALL_NC() {
        clockCycles = 12;
        int address = nextPart();
        if (!Z) {
            clockCycles = 24;
            pushPart(PC);
            PC = address;
        }
    }

    public void op_NULL() {
        throw new RuntimeException("Invalid opcode");
    }

    public void op_JP_NC() {
        int address = nextPart();
        clockCycles = 12;
        if (!C) {
            clockCycles = 16;
            PC = address;
        }
    }

    public void op_POP_DE() {
        DE = popPart();
        clockCycles = 12;
    }

    public void op_RET_NC() {
        clockCycles = 8;
        int address = nextPart();
        if (!C) {
            clockCycles = 20;
            pushPart(PC);
            PC = address;
        }
    }

    public void op_RST_08H() {
        rst(0x08);
        clockCycles = 16;
    }

    public void op_ADC_A() {
        adc(nextByte());
        clockCycles = 8;
    }

    public void op_CALL() {
        clockCycles = 24;
        int address = nextPart();
        pushPart(PC);
        PC = address;
    }

    public void op_CALL_Z() {
        clockCycles = 12;
        int address = nextPart();
        if (Z) {
            clockCycles = 24;
            pushPart(PC);
            PC = address;
        }
    }

    public void op_CBCode() {
        int b = nextByte();
        if ((b & 0xF) == 0x6 || (b & 0xF) == 0xE) {
            clockCycles = 16;
        } else {
            clockCycles = 8;
        }
        executeCBCode(b);
    }

    public void op_JP_Z() {
        clockCycles = 12;
        int address = nextPart();
        if (Z) {
            clockCycles = 16;
            PC = address;
        }
    }

    public void op_RET() {
        PC = popPart();
        clockCycles = 16;
    }

    public void op_RET_Z() {
        clockCycles = 8;
        if (Z) {
            PC = popPart();
            clockCycles = 20;
        }
    }

    public void op_RST_00H() {
        rst(0x00);
        clockCycles = 16;
    }

    public void op_ADD_A() {
        clockCycles = 8;
        add(nextByte());
    }

    public void op_PUSH_BC() {
        pushPart(BC);
        clockCycles = 16;
    }

    public void op_CALL_NZ() {
        clockCycles = 12;
        int address = nextPart();
        if (!Z) {
            clockCycles = 24;
            pushPart(PC);
            PC = address;
        }
    }

    public void op_JP() {
        clockCycles = 16;
        PC = nextPart();
    }

    public void op_JP_NZ() {
        clockCycles = 12;
        int address = nextPart();
        if (!Z) {
            clockCycles = 16;
            PC = address;
        }
    }

    public void op_POP_BC() {
        clockCycles = 12;
        BC = popPart();
    }

    public void op_RET_NZ() {
        clockCycles = 8;
        if (!Z) {
            PC = popPart();
            clockCycles = 20;
        }
    }

    public void op_CP_A() {
        cp(A);
        clockCycles = 4;
    }

    public void op_CP_HL_VALUE() {
        cp(this.memory.read(HL));
        clockCycles = 8;
    }

    public void op_CP_L() {
        cp(getLower(HL));
        clockCycles = 4;
    }

    public void op_CP_H() {
        cp(getUpper(HL));
        clockCycles = 4;
    }

    public void op_CP_E() {
        cp(getUpper(DE));
        clockCycles = 4;
    }

    public void op_CP_D() {
        cp(getLower(DE));
        clockCycles = 4;
    }

    public void op_CP_C() {
        cp(getLower(BC));
        clockCycles = 4;
    }

    public void op_CP_B() {
        cp(getUpper(BC));
        clockCycles = 4;
    }

    public void op_OR_A() {
        or(A);
        clockCycles = 4;
    }

    public void op_OR_HL_VALUE() {
        or(this.memory.read(HL));
        clockCycles = 4;
    }

    public void op_OR_L() {
        or(getLower(HL));
        clockCycles = 4;
    }

    public void op_OR_H() {
        or(getUpper(HL));
        clockCycles = 4;
    }

    public void op_OR_E() {
        or(getLower(DE));
        clockCycles = 4;
    }

    public void op_OR_D() {
        or(getUpper(DE));
        clockCycles = 4;
    }

    public void op_OR_C() {
        or(getLower(BC));
        clockCycles = 4;
    }

    public void op_OR_B() {
        or(getUpper(BC));
        clockCycles = 4;
    }

    public void op_XOR_A() {
        xor(A);
        clockCycles = 4;
    }

    public void op_XOR_HL_VALUE() {
        xor(this.memory.read(HL));
        clockCycles = 8;
    }

    public void op_XOR_L() {
        xor(getLower(BC));
        clockCycles = 4;
    }

    public void op_XOR_H() {
        xor(getUpper(HL));
        clockCycles = 4;
    }

    public void op_XOR_E() {
        xor(getLower(DE));
        clockCycles = 4;
    }

    public void op_XOR_D() {
        xor(getUpper(DE));
        clockCycles = 4;
    }

    public void op_XOR_C() {
        xor(getLower(BC));
        clockCycles = 4;
    }

    public void op_XOR_B() {
        xor(getUpper(BC));
        clockCycles = 4;
    }

    public void op_AND_A() {
        and(A);
        clockCycles = 4;
    }

    public void op_AND_HL_VALUE() {
        and(this.memory.read(HL));
        clockCycles = 8;
    }

    public void op_AND_L() {
        and(getLower(HL));
        clockCycles = 4;
    }

    public void op_AND_H() {
        and(getUpper(HL));
        clockCycles = 4;
    }

    public void op_AND_E() {
        and(getLower(DE));
        clockCycles = 4;
    }

    public void op_AND_D() {
        and(getUpper(DE));
        clockCycles = 4;
    }

    public void op_AND_C() {
        and(getLower(BC));
        clockCycles = 4;
    }

    public void op_AND_B() {
        and(getUpper(BC));
        clockCycles = 4;
    }

    public void op_SBC_A_A() {
        sbc(A);
        clockCycles = 4;
    }

    public void op_SBC_A_HL_VALUE() {
        sbc(this.memory.read(HL));
        clockCycles = 8;
    }

    public void op_SBC_A_L() {
        sbc(getLower(HL));
        clockCycles = 4;
    }

    public void op_SBC_A_H() {
        sbc(getUpper(HL));
        clockCycles = 4;
    }

    public void op_SBC_A_E() {
        sbc(getLower(DE));
        clockCycles = 4;
    }

    public void op_SBC_A_D() {
        sbc(getUpper(DE));
        clockCycles = 4;
    }

    public void op_SBC_A_C() {
        sbc(getLower(BC));
        clockCycles = 4;
    }

    public void op_SBC_A_B() {
        sbc(getUpper(BC));
        clockCycles = 4;
    }

    public void op_SUB_A() {
        clockCycles = 4;
        sub(A);
    }

    public void op_SUB_HL_VALUE() {
        clockCycles = 8;
        sub(this.memory.read(HL));
    }

    public void op_SUB_L() {
        clockCycles = 4;
        sub(getLower(HL));
    }

    public void op_SUB_H() {
        clockCycles = 4;
        sub(getUpper(HL));
    }

    public void op_SUB_E() {
        clockCycles = 4;
        sub(getLower(DE));
    }

    public void op_SUB_D() {
        clockCycles = 4;
        sub(getUpper(DE));
    }

    public void op_SUB_C() {
        clockCycles = 4;
        sub(getLower(BC));
    }

    public void op_SUB_B() {
        clockCycles = 4;
        sub(getUpper(BC));
    }

    public void op_ADC_A_A() {
        adc(A);
        clockCycles = 4;
    }

    public void op_ADC_A_HL_VALUE() {
        adc(this.memory.read(HL));
        clockCycles = 8;
    }

    public void op_ADC_A_L() {
        adc(getLower(HL));
        clockCycles = 4;
    }

    public void op_ADC_A_H() {
        adc(getUpper(HL));
        clockCycles = 4;
    }

    public void op_ADC_A_E() {
        adc(getLower(DE));
        clockCycles = 4;
    }

    public void op_ADC_A_D() {
        adc(getUpper(DE));
        clockCycles = 4;
    }

    public void op_ADC_A_C() {
        adc(getLower(BC));
        clockCycles = 4;
    }

    public void op_ADC_A_B() {
        adc(getUpper(BC));
        clockCycles = 4;
    }

    public void op_ADD_A_A() {
        add(A);
        clockCycles = 4;
    }

    public void op_ADD_A_HL_VALUE() {
        add(this.memory.read(HL));
        clockCycles = 8;
    }

    public void op_ADD_A_L() {
        add(getLower(HL));
        clockCycles = 4;
    }

    public void op_ADD_A_H() {
        add(getUpper(HL));
        clockCycles = 4;
    }

    public void op_ADD_A_E() {
        add(getLower(DE));
        clockCycles = 4;
    }

    public void op_ADD_A_D() {
        add(getUpper(DE));
        clockCycles = 4;
    }

    public void op_ADD_A_C() {
        add(getLower(BC));
        clockCycles = 4;
    }

    public void op_ADD_A_B() {
        add(getUpper(BC));
        clockCycles = 4;
    }

    public void op_LD_A_A() {
        A = A; // ???
        clockCycles = 4;
    }

    public void op_LD_A_HL_VALUE() {
        A = this.memory.read(HL);
        clockCycles = 8;
    }

    public void op_LD_A_L() {
        A = getLower(HL);
        clockCycles = 4;
    }

    public void op_LD_A_H() {
        A = getUpper(HL);
        clockCycles = 4;
    }

    public void op_LD_A_E() {
        A = getLower(DE);
        clockCycles = 4;
    }

    public void op_LD_A_D() {
        A = getUpper(DE);
        clockCycles = 4;
    }

    public void op_LD_A_C() {
        A = getLower(BC);
        clockCycles = 4;
    }

    public void op_LD_A_B() {
        A = getUpper(BC);
        clockCycles = 4;
    }

    public void op_LD_HL_VALUE_A() {
        this.memory.write(HL, A);
        clockCycles = 8;
    }

    public void op_HALT() {
        halted = true;
    }

    public void op_LD_HL_VALUE_L() {
        this.memory.write(HL, getLower(getRegistryValue("HL")));
        clockCycles = 8;
    }

    public void op_LD_HL_VALUE_H() {
        this.memory.write(HL, getUpper(getRegistryValue("HL")));
        clockCycles = 8;
    }

    public void op_LD_HL_VALUE_E() {
        this.memory.write(HL, getLower(getRegistryValue("DE")));
        clockCycles = 8;
    }

    public void op_LD_HL_VALUE_D() {
        this.memory.write(HL, getUpper(getRegistryValue("DE")));
        clockCycles = 8;
    }

    public void op_LD_HL_VALUE_C() {
        this.memory.write(HL, getLower(getRegistryValue("BC")));
        clockCycles = 8;
    }

    public void op_LD_HL_VALUE_B() {
        this.memory.write(HL, getUpper(getRegistryValue("BC")));
        clockCycles = 8;
    }

    public void op_LD_L_A() {
        setLower("HL", A);
        clockCycles = 4;
    }

    public void op_LD_L_HL_VALUE() {
        setLower("HL", this.memory.read(HL));
        clockCycles = 8;
    }

    public void op_LD_L_L() {
        setLower("HL", getLower(getRegistryValue("HL")));
        clockCycles = 4;
    }

    public void op_LD_L_H() {
        setLower("HL", getUpper(getRegistryValue("HL")));
        clockCycles = 4;
    }

    public void op_LD_L_E() {
        setLower("HL", getLower(getRegistryValue("DE")));
        clockCycles = 4;
    }

    public void op_LD_L_D() {
        setLower("HL", getUpper(getRegistryValue("DE")));
        clockCycles = 4;
    }

    public void op_LD_L_C() {
        setLower("HL", getLower(getRegistryValue("BC")));
        clockCycles = 4;
    }

    public void op_LD_L_B() {
        setLower("HL", getUpper(getRegistryValue("BC")));
        clockCycles = 4;
    }

    public void op_LD_H_A() {
        setUpper("HL", A);
        clockCycles = 4;
    }

    public void op_LD_H_HL_VALUE() {
        setUpper("HL", this.memory.read(HL));
        clockCycles = 8;
    }

    public void op_LD_H_L() {
        setUpper("HL", getLower(getRegistryValue("HL")));
        clockCycles = 4;
    }

    public void op_LD_H_H() {
        setUpper("HL", getUpper(getRegistryValue("HL")));
        clockCycles = 4;
    }

    public void op_LD_H_E() {
        setUpper("HL", getLower(getRegistryValue("DE")));
        clockCycles = 4;
    }

    public void op_LD_H_D() {
        setUpper("HL", getUpper(getRegistryValue("DE")));
        clockCycles = 4;
    }

    public void op_LD_H_C() {
        setUpper("HL", getLower(getRegistryValue("BC")));
        clockCycles = 4;
    }

    public void op_LD_H_B() {
        setUpper("HL", getUpper(getRegistryValue("BC")));
        clockCycles = 4;
    }

    public void op_LD_E_A() {
        setLower("DE", A);
        clockCycles = 4;
    }

    public void op_LD_E_HL_VALUE() {
        setLower("DE", this.memory.read(HL));
        clockCycles = 4;
    }

    public void op_LD_E_L() {
        setLower("DE", getLower(getRegistryValue("HL")));
        clockCycles = 4;
    }

    public void op_LD_E_H() {
        setLower("DE", getUpper(getRegistryValue("HL")));
        clockCycles = 4;
    }

    public void op_LD_E_E() {
        setLower("DE", getLower(getRegistryValue("DE")));
        clockCycles = 4;
    }

    public void op_LD_E_D() {
        setLower("DE", getUpper(getRegistryValue("DE")));
        clockCycles = 4;
    }

    public void op_LD_E_C() {
        setLower("DE", getLower(getRegistryValue("BC")));
        clockCycles = 4;
    }

    public void op_LD_E_B() {
        setLower("DE", getUpper(getRegistryValue("BC")));
        clockCycles = 4;
    }

    public void op_LD_D_A() {
        setUpper("DE", A);
        clockCycles = 4;
    }

    public void op_LD_D_HL_VALUE() {
        setUpper("DE", this.memory.read(HL));
        clockCycles = 8;
    }

    public void op_LD_D_L() {
        setUpper("DE", getLower(getRegistryValue("HL")));
        clockCycles = 4;
    }

    public void op_LD_D_H() {
        setUpper("DE", getUpper(getRegistryValue("HL")));
        clockCycles = 4;
    }

    public void op_LD_D_E() {
        setUpper("DE", getLower(getRegistryValue("DE")));
        clockCycles = 4;
    }

    public void op_LD_D_D() {
        setUpper("DE", getUpper(getRegistryValue("DE")));
        clockCycles = 4;
    }

    public void op_LD_D_C() {
        setUpper("DE", getLower(getRegistryValue("BC")));
        clockCycles = 4;
    }

    public void op_LD_D_B() {
        setUpper("DE", getUpper(getRegistryValue("BC")));
        clockCycles = 4;
    }

    public void op_LD_C_A() {
        setLower("BC", A);
        clockCycles = 4;
    }

    public void op_LD_C_HL_VALUE() {
        setLower("BC", this.memory.read(HL));
        clockCycles = 8;
    }

    public void op_LD_C_L() {
        setLower("BC", getLower(getRegistryValue("HL")));
        clockCycles = 4;
    }

    public void op_LD_C_H() {
        setLower("BC", getUpper(getRegistryValue("HL")));
        clockCycles = 4;
    }

    public void op_LD_C_E() {
        setLower("BC", getLower(getRegistryValue("DE")));
        clockCycles = 4;
    }

    public void op_LD_C_D() {
        setLower("BC", getUpper(getRegistryValue("DE")));
        clockCycles = 4;
    }

    public void op_LD_C_C() {
        setLower("BC", getLower(getRegistryValue("BC")));
        clockCycles = 4;
    }

    public void op_LD_C_B() {
        setLower("BC", getUpper(getRegistryValue("BC")));
        clockCycles = 4;
    }

    public void op_LD_B_A() {
        clockCycles = 4;
        setUpper("BC", A);
    }

    public void op_LD_B_HL_VALUE() {
        clockCycles = 8;
        setUpper("BC", this.memory.read(HL));
    }

    public void op_LD_B_L() {
        clockCycles = 4;
        setUpper("BC", getLower(getRegistryValue("HL")));
    }

    public void op_LD_B_H() {
        clockCycles = 4;
        setUpper("BC", getUpper(getRegistryValue("HL")));
    }

    public void op_LD_B_E() {
        clockCycles = 4;
        setUpper("BC", getLower(getRegistryValue("DE")));
    }

    public void op_LD_B_D() {
        clockCycles = 4;
        setUpper("BC", getUpper(getRegistryValue("DE")));
    }

    public void op_LD_B_C() {
        clockCycles = 4;
        setUpper("BC", getLower(getRegistryValue("BC")));
    }

    public void op_LD_B_B() {
        clockCycles = 4;
        setUpper("BC", getUpper(getRegistryValue("BC")));
    }

    public void op_CCF() {
        C = !C;
        H = false;
        N = false;
        clockCycles = 4;
    }

    public void op_LD_A() {
        clockCycles = 8;
        A = nextByte();
    }

    public void op_DEC_A() {
        clockCycles = 8;
        A--;
    }

    public void op_INC_A() {
        clockCycles = 8;
        A++;
    }

    public void op_DEC_SP() {
        clockCycles = 8;
        SP--;
    }

    public void op_LD_A_HL_DEC() {
        A = this.memory.read(HL);
        HL--;
        clockCycles = 8;
    }

    public void op_ADD_HL_SP() {
        clockCycles = 8;
        addRegs("HL", "SP");
    }

    public void op_JR_C() {
        clockCycles = 8;
        if (C) {
            clockCycles = 12;
            relativeJump(nextByte());
        }
    }

    public void op_SCF() {
        C = true;
        H = false;
        N = false;
        clockCycles = 4;
    }

    public void op_LD_HL_VALUE() {
        this.memory.write(HL, nextByte());
        clockCycles = 12;
    }

    public void op_DEC_HL_VALUE() {
        clockCycles = 12;
        this.memory.write(HL, decrement(this.memory.read(HL)));
    }

    public void op_INC_HL_VALUE() {
        clockCycles = 12;
        this.memory.write(HL, increment(this.memory.read(HL)));
    }

    public void op_INC_SP() {
        SP++;
        clockCycles = 8;
    }

    public void op_LD_HL_DEC_A() {
        clockCycles = 8;
        this.memory.write(HL, A);
        HL--;
    }

    public void op_LD_SP16b() {
        clockCycles = 12;
        SP = nextPart();
    }

    public void op_JR_NC() {
        clockCycles = 8;
        if (!C) {
            clockCycles = 12;
            relativeJump(nextByte());
        }
    }

    public void op_CPL() {
        A ^= A;
        clockCycles = 4;
    }

    public void op_LD_L() {
        setLower("HL", nextByte());
        clockCycles = 8;
    }

    public void op_DEC_L() {
        setLower("HL", decrement(getLower(getRegistryValue("HL"))));
        clockCycles = 4;
    }

    public void op_INC_L() {
        setLower("HL", increment(getLower(getRegistryValue("HL"))));
        clockCycles = 4;
    }

    public void op_DEC_HL() {
        HL--;
        clockCycles = 8;
    }

    public void op_LD_A_HL_INC() {
        clockCycles = 8;
        A = this.memory.read(HL++);
    }

    public void op_ADD_HL_HL() {
        clockCycles = 8;
        addRegs("HL", "HL");
    }

    public void op_JR_Z() {
        clockCycles = 8;
        if (Z) {
            clockCycles = 12;
            relativeJump(nextByte());
        }
    }

    public void op_DAA() {
        C = false;
        if ((A & 0x0F) > 9) {
            A += 0x06;
        }
        if (((A & 0xF0) >> 4) > 9) {
            C = true;
            A += 0x60;
        }
        H = false;
        Z = A == 0x00;
        clockCycles = 4;
    }

    public void op_LD_H() {
        setUpper("HL", nextByte());
        clockCycles = 8;
    }

    public void op_DEC_H() {
        clockCycles = 4;
        setUpper("HL", decrement(getUpper(getRegistryValue("HL"))));
    }

    public void op_INC_H() {
        clockCycles = 4;
        setUpper("HL", increment(getUpper(getRegistryValue("HL"))));
    }

    public void op_INC_HL() {
        clockCycles = 8;
        HL++;
    }

    public void op_LD_HL_INC_A() {
        clockCycles = 8;
        this.memory.write(HL, A);
        HL++;
    }

    public void op_LD_HL() {
        HL = nextPart();
        clockCycles = 12;
    }

    public void op_JR_NZ() {
        clockCycles = 8;
        if (!Z) {
            relativeJump(nextByte());
            clockCycles = 12;
        }
    }

    public void op_RRA() {
        A = rr(A);
        clockCycles = 4;
        Z = false;
    }

    public void op_LD_E() {
        setLower("DE", nextByte());
        clockCycles = 8;
    }

    public void op_DEC_E() {
        setLower("DE", decrement(getLower(getRegistryValue("DE"))));
        clockCycles = 4;
    }

    public void op_INC_E() {
        setLower("DE", increment(getLower(getRegistryValue("DE"))));
        clockCycles = 4;
    }

    public void op_DEC_DE() {
        DE--;
        clockCycles = 8;
    }

    public void op_LD_A_DE() {
        clockCycles = 8;
        A = this.memory.read(DE);
    }

    public void op_ADD_HL_DE() {
        clockCycles = 8;
        addRegs("HL", "DE");
    }

    public void op_JR() {
        clockCycles = 12;
        relativeJump(nextByte());
    }

    public void op_RLA() {
        A = rl(A);
        clockCycles = 4;
        Z = false;
    }

    public void op_LD_D() {
        setUpper("DE", nextByte());
        clockCycles = 8;
    }

    public void op_DEC_D() {
        setUpper("DE", decrement(getUpper(getRegistryValue("DE"))));
        clockCycles = 4;
    }

    public void op_INC_D() {
        setUpper("DE", increment(getUpper(getRegistryValue("DE"))));
        clockCycles = 4;
    }

    public void op_INC_DE() {
        clockCycles = 8;
        DE++;
    }

    public void op_LD_DE_A() {
        this.memory.write(DE, A);
        clockCycles = 8;
    }

    public void op_LD_DE() {
        DE = nextPart();
        clockCycles = 12;
    }

    public void op_STOP() {
        stop = true;
        clockCycles = 4;
    }

    public void op_RRCA() {
        A = rrc(A);
        Z = false;
        clockCycles = 4;
    }

    public void op_LD_C() {
        setLower("BC", nextByte());
        clockCycles = 8;
    }

    public void op_DEC_C() {
        clockCycles = 4;
        setLower("BC", decrement(getLower(getRegistryValue("BC"))));
    }

    public void op_INC_C() {
        clockCycles = 4;
        setLower("BC", increment(getLower(getRegistryValue("BC"))));
    }

    public void op_DEC_BC() {
        clockCycles = 8;
        BC--;
    }

    public void op_LD_A_BC() {
        A = this.memory.read(BC);
        clockCycles = 8;
    }

    public void op_LD_ADD_HL_BC() {
        clockCycles = 8;
        addRegs("HL", "BC");
    }

    public void op_LD_SP8b() {
        clockCycles = 20;
        writePart(nextByte(), SP);
    }

    public void op_RLCA() {
        clockCycles = 4;
        A = rlc(A);
        Z = false;
    }

    public void op_LD_B() {
        setUpper("BC", nextByte());
        clockCycles = 8;
    }

    public void op_INC_B() {
        setUpper("BC", increment(getUpper(getRegistryValue("BC"))));
        clockCycles = 8;
    }

    public void op_DEC_B() {
        setUpper("BC", decrement(getUpper(getRegistryValue("BC"))));
        clockCycles = 8;
    }

    public void op_INC_BC() {
        BC++;
        clockCycles = 8;
    }

    public void op_LD_BC() {
        BC = nextPart();
        clockCycles = 12;
    }

    public void op_LD_BC_A() {
        this.memory.write(BC, A);
        clockCycles = 8;
    }

    public void op_nop() {
    }

    public void setLower(String registry, int val) {
        int registryValue = getRegistryValue(registry);
        registryValue = ((registryValue & 0xFF00) | (val));
        setRegistryValue(registry, registryValue);
    }

    public void setUpper(String registry, int val) {
        int registryValue = getRegistryValue(registry);
        registryValue = ((registryValue & 0x00FF) | ((val) << 8));
        setRegistryValue(registry, registryValue);
    }

    public void setRegistryValue(String registry, int registryValue) {
        if (registry.equals("BC")) {
            BC = registryValue;
        } else if (registry.equals("A")) {
            A = (byte) (registryValue & 0xFF);
        } else if (registry.equals("DE")) {
            DE = registryValue;
        } else if (registry.equals("F")) {
            F = (byte) (registryValue & 0xFF);
        } else if (registry.equals("HL")) {
            HL = registryValue;
        } else if (registry.equals("SP")) {
            SP = registryValue;
        }
    }

    public int getRegistryValue(String registry) {
        if (registry.equals("BC")) {
            return BC;
        } else if (registry.equals("A")) {
            return A;
        } else if (registry.equals("F")) {
            return F;
        } else if (registry.equals("DE")) {
            return DE;
        } else if (registry.equals("HL")) {
            return HL;
        } else if (registry.equals("SP")) {
            return SP;
        }
        return 0;
    }

    public byte getLower(int val) {
        return (byte) (val & 0xFF);
    }

    public byte getUpper(int val) {
        return (byte) (val >> 8 & 0xFF);
    }

    public byte increment(int val) {
        H = (val & 0xF) == 0xF;
        val++;
        Z = val == 0;
        N = false;
        return (byte) val;
    }

    public byte decrement(int val) {
        val--;
        Z = val == 0;
        N = true;
        H = (val & 0xF) == 0xF;
        return (byte) val;
    }

    public void rst(int addr) {
        pushPart(PC);
        PC = addr;
    }

    public void add(int b) {
        int n = (A) + (b);
        Z = (n & 0xFF) == 0;
        H = (((A & 0x0F) + (b & 0x0F)) & 0x10) == 0x10;
        C = n > 0xFF;
        N = false;
        A = (byte) (n & 0xFF);
    }

    public void adc(int b) {
        int n = (A) + (b);
        if (C) {
            n++;
        }
        Z = (n & 0xFF) == 0;
        H = (((A & 0x0F) + (b & 0x0F)) & 0x10) == 0x10;
        C = n > 0xFF;
        N = false;
        A = (byte) (n & 0xFF);
    }

    public void sub(int b) {
        cp(b);
        A -= b;
    }

    public void sbc(int b) {
        H = ((A & 0xF) - (b & 0xF)) < 0;
        C = ((A) - (b) - 1) < 0;
        N = true;
        A -= b;
        A--;
        Z = A == 0;
    }

    public void and(int b) {
        A &= b;
        Z = A == 0;
        H = true;
        N = false;
        C = false;
    }

    public void xor(int b) {
        A ^= b;
        Z = A == 0;
        H = false;
        N = false;
        C = false;
    }

    public void or(int b) {
        A |= b;
        Z = A == 0;
        H = false;
        N = false;
        C = false;
    }

    public void cp(int b) {
        Z = A == b;
        H = (A & 0xF) < (b & 0xF);
        N = true;
        C = A < b;
    }

    public byte rl(int val) {
        boolean oldcarry = C;
        C = (val & (1 << 7)) == (1 << 7);
        H = false;
        N = false;
        val <<= 1;
        if (oldcarry) {
            val |= 0x1;
        }
        Z = val == 0;
        return (byte) val;
    }

    public byte rlc(int val) {
        C = (val & (1 << 7)) == (1 << 7);
        H = false;
        N = false;
        val <<= 1;
        if (C) {
            val |= 0x1;
        }
        Z = val == 0;
        return (byte) val;
    }

    public byte rr(int val) {
        boolean oldcarry = C;
        C = (val & 0x1) == 0x1;
        H = false;
        N = false;
        val >>= 1;
        if (oldcarry) {
            val |= (1 << 7);
        }
        Z = val == 0;
        return (byte) val;
    }

    public byte rrc(int val) {
        C = (val & 0x1) == 0x1;
        H = false;
        N = false;
        val >>= 1;
        if (C) {
            val |= (1 << 7);
        }
        Z = val == 0;
        return (byte) val;
    }

    public byte sla(int val) {
        C = (val & (1 << 7)) != 0;
        val <<= 1;
        Z = val == 0;
        H = false;
        N = false;
        return (byte) val;
    }

    public byte sra(int val) {
        val = ((val & (1 << 7)) | (val >> 1));
        C = false;
        Z = val == 0;
        H = false;
        N = false;
        return (byte) val;
    }

    public byte swap(int val) {
        Z = val == 0;
        N = false;
        H = false;
        C = false;
        return (byte) ((val << 4) | (val >> 4));
    }

    public byte srl(int val) {
        C = (val & 0x1) != 0;
        val >>= 1;
        Z = val == 0;
        H = false;
        N = false;
        return (byte) val;
    }

    public void bit(int val, int b) {
        Z = (val & (1 << b)) != 0;
        N = false;
        H = true;
    }

    public void addRegs(String registryA, String registryB) {
        int a = getRegistryValue(registryA);
        int b = getRegistryValue(registryB);
        int temp = (a) + (b);
        N = false;
        C = temp > 0xFFFF;
        H = ((a & 0x0FFF) + (b & 0x0FFF)) > 0x0FFF;
        setRegistryValue(registryA, (temp & 0xFFFF));
    }

    public void relativeJump(int d) {
        PC = signedAdd(PC, d);
    }

    public int signedAdd(int a, int b) {
        int bSigned = (b);
        if (bSigned >= 0) {
            return a + (bSigned);
        } else {
            return a - (-bSigned);
        }
    }

    private void executeCBCode(int b) {
        // TODO: Prefix CB implementation
    }
}
