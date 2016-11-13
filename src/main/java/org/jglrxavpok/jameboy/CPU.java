package org.jglrxavpok.jameboy;

import org.jglrxavpok.jameboy.memory.Interrupts;
import org.jglrxavpok.jameboy.memory.MemoryController;
import org.jglrxavpok.jameboy.utils.BitUtils;

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
    public boolean Z;
    public boolean N;
    public boolean H;
    public boolean C;
    public int BC, DE, HL;
    private boolean stop;
    private boolean halted;
    private MemoryController memory;
    private boolean disabledInterrupts;
    private boolean disableInterruptsNextInstruction;
    private boolean enableInterruptsNextInstruction;
    private boolean masterInterrupt;

    public void setMemory(MemoryController memory) {
        this.memory = memory;
    }

    public int doCycle() {
        if (stop)
            return -1;
        int opcode = nextByte();
        clockCycles = 0;
        return executeOP(opcode);
    }

    private int nextPart() {
        return (nextByte() & 0xFF | (nextByte() & 0xFF) << 8) & 0xFFFF;
    }

    private byte nextByte() {
        byte value = this.memory.read(PC);
        PC++;
        return value;
    }

    public void push16Bit(int val) {
        SP -= 2;
        write16Bits(SP, val & 0xFFFF);
    }

    public int popPart() {
        int val = (this.memory.read(SP) & 0xFF)
                | ((this.memory.read(SP + 1) & 0xFF) << 8);
        SP += 2;
        return val & 0xFFFF;
    }

    public void write16Bits(int pos, int val) {
        this.memory.write(pos, getLower(val));
        this.memory.write(pos + 1, getUpper(val));
    }

    public void hardGoto(int index) {
        PC = index;
    }

    public void hardReset() {
        A = 0;
        Z = N = C = H = false;
        BC = DE = HL = 0;
        SP = 0;
        PC = 0x100;
        halted = false;
        masterInterrupt = true;
    }

    private int executeOP(int opcode) {
        if(disableInterruptsNextInstruction) {
            disableInterruptsNextInstruction = false;
            masterInterrupt = false;
        }
        if(enableInterruptsNextInstruction) {
            enableInterruptsNextInstruction = false;
            masterInterrupt = true;
        }
        opcode = opcode & 0xFF;
        //System.out.println("opcode: "+Integer.toHexString(opcode)+", ("+Integer.toHexString(PC-1)+")");
        //System.out.println(Integer.toHexString(PC-1));
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
                op_ADD_HL_BC();
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
                op_SUB_A_A();
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
                op_AND_A_A();
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
                op_XOR_A_A();
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
                op_OR_A_A();
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
                op_CP_A_A();
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
            case 0xFA: {
                op_LDH_A();
                break;
            }
            case 0xEA: {
                op_LDH_A_TO();
                break;
            }
            case 0xF2: {
                op_LD_A_C_OFFSET();
                break;
            }
            case 0xE2: {
                op_LD_C_OFFSET_ADDRESS_A();
                break;
            }
            case 0xE0: {
                op_LD_OFFSET_ADDRESS_A();
                break;
            }
            case 0xF0: {
                op_LD_A_OFFSET_ADDRESS();
                break;
            }
            case 0xF9: {
                op_LD_SP_HL();
                break;
            }
            case 0xF8: {
                op_LD_HL_SP_OFFSET();
                break;
            }
            case 0xF5: {
                op_PUSH_AF();
                break;
            }
            case 0xD5: {
                op_PUSH_DE();
                break;
            }
            case 0xE5: {
                op_PUSH_HL();
                break;
            }
            case 0xF1: {
                op_POP_AF();
                break;
            }
            case 0xE1: {
                op_POP_HL();
                break;
            }
            case 0xD6: {
                op_SUB_A();
                break;
            }
            case 0xE6: {
                op_AND_A();
                break;
            }
            case 0xF6: {
                op_OR_A();
                break;
            }
            case 0xEE: {
                op_XOR_A();
                break;
            }
            case 0xFE: {
                op_CP_A();
                break;
            }
            case 0xE8: {
                op_ADD_SP();
                break;
            }
            case 0xF3: {
                op_DI();
                break;
            }
            case 0xFB: {
                op_EI();
                break;
            }
            case 0xDA: {
                op_JP_C();
                break;
            }
            case 0xE9: {
                op_JP_HL_VALUE();
                break;
            }
            case 0xDC: {
                op_CALL_C();
                break;
            }
            case 0xD7: {
                rst(0x10);
                clockCycles = 32;
                break;
            }
            case 0xDF: {
                rst(0x18);
                clockCycles = 32;
                break;
            }
            case 0xE7: {
                rst(0x20);
                clockCycles = 32;
                break;
            }
            case 0xEF: {
                rst(0x28);
                clockCycles = 32;
                break;
            }
            case 0xF7: {
                rst(0x30);
                clockCycles = 32;
                break;
            }
            case 0xFF: {
                rst(0x38);
                clockCycles = 32;
                break;
            }
            case 0xD8: {
                op_RET_C();
                break;
            }
            case 0xD9: {
                op_RETI();
                break;
            }
            case 0xDE: {
                op_SBC_A();
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown opcode: " + Integer.toHexString(opcode));
            }
        }

        if(masterInterrupt) {
            if(memory.isInterruptOn(Interrupts.V_BLANK)) {
                memory.resetInterrupt(Interrupts.V_BLANK);
                handleVBlankInterrupt();
            } else if(memory.isInterruptOn(Interrupts.LCD_COINCIDENCE)) {
                memory.resetInterrupt(Interrupts.LCD_COINCIDENCE);
                handleLCDCoincidenceInterrupt();
            }
        } else {
            if(memory.isInterruptOn(Interrupts.V_BLANK)) {
//                System.out.println("v blank int0000 "+Integer.toHexString(PC).toUpperCase());
            }
        }

        return clockCycles;
    }

    private boolean debug_shouldPrintPC() {
        return true;
    }

    private void op_SBC_A() {
        clockCycles = 8;
        sbc(nextByte());
    }

    private void handleLCDCoincidenceInterrupt() {
        masterInterrupt = false;
        rst(0x48); // jump to interrupt handler at 0x0048
    }

    private void handleVBlankInterrupt() {
        masterInterrupt = false;
        //System.out.println("v blank int");
        rst(0x40); // jump to interrupt handler at 0x0040
    }

    private void op_RETI() {
        int addr = popPart();
        PC = addr;
        disabledInterrupts = false;
        clockCycles = 8;
    }

    private void op_RET_C() {
        clockCycles = 8;
        if (C) {
            int address = popPart();
            PC = address;
        }
    }

    private void op_CALL_C() {
        int addr = nextPart();
        if(C) {
            push16Bit(PC);
            PC = addr;
        }
        clockCycles = 12;
    }

    private void op_JP_HL_VALUE() {
        PC = HL; // TODO: or PC = (HL) ? Seems not clear
        clockCycles = 4;
    }

    private void op_JP_C() {
        int address = nextPart();
        if(C)
            PC = address;
        clockCycles = 12;
    }

    private void op_EI() {
        enableInterruptsNextInstruction = true;
        disableInterruptsNextInstruction = false;
        //masterInterrupt = true;
        //System.out.println("ei "+Integer.toHexString(PC-1).toUpperCase());
        clockCycles = 4;
    }

    private void op_DI() {
        disableInterruptsNextInstruction = true;
        enableInterruptsNextInstruction = false;
        //masterInterrupt = false;
        //System.out.println("di "+Integer.toHexString(PC-1).toUpperCase());
        clockCycles = 4;
    }

    private void op_ADD_SP() {
        int a = SP;
        int b = nextByte();
        int temp = a + b;
        N = false;
        C = temp > 0xFFFF;
        H = ((a & 0x0FFF) + (b & 0x0FFF)) > 0x0FFF;
        SP = (temp & 0xFFFF);
        clockCycles = 16;
    }

    private void op_CP_A() {
        cp(nextByte());
        clockCycles = 8;
    }

    private void op_XOR_A() {
        xor(nextByte());
        clockCycles = 8;
    }

    private void op_OR_A() {
        or(nextByte());
        clockCycles = 8;
    }

    private void op_AND_A() {
        and(nextByte());
        clockCycles = 8;
    }

    private void op_SUB_A() {
        sub(nextByte());
        clockCycles = 8;
    }

    private void op_POP_HL() {
        HL = popPart();
        clockCycles = 12;
    }

    private void op_POP_AF() {
        int AF = popPart();
        A = getUpper(AF);
        setFlags(getLower(AF));
        clockCycles = 12;
    }

    private void op_PUSH_HL() {
        push16Bit(HL);
        clockCycles = 16;
    }

    private void op_PUSH_DE() {
        push16Bit(DE);
        clockCycles = 16;
    }

    private void op_PUSH_AF() {
        push16Bit(((A & 0xFF) << 8 | (getFlags() & 0xFF)) & 0xFFFF);
        clockCycles = 16;
    }

    public byte getFlags() {
        int value = 0;
        if(Z)
            value |= 1 << 7;
        if(N)
            value |= 1 << 5;
        if(H)
            value |= 1 << 5;
        if(C)
            value |= 1 << 4;
        return (byte)value;
    }

    private void op_LD_HL_SP_OFFSET() {
        Z = false;
        N = false;
        byte val = nextByte();
        H = (PC & 0x0FFF) + (val & 0xFF) > 0x0FFF;
        C = (val & 0xFF) + PC > 0xFFFF;
        HL = SP + val; // val is signed here
        clockCycles = 12;
    }

    private void op_LD_SP_HL() {
        SP = HL;
        clockCycles = 8;
    }

    private void op_LD_A_OFFSET_ADDRESS() {
        A = memory.read(0xFF00 + (nextByte() & 0xFF));
        clockCycles = 12;
    }

    private void op_LD_OFFSET_ADDRESS_A() {
        int addr = 0xFF00 + (nextByte() & 0xFF);
        memory.write(addr, A);
        clockCycles = 12;
    }

    private void op_LD_C_OFFSET_ADDRESS_A() {
        memory.write(0xFF00 + (getLower(BC)&0xFF), A);
        clockCycles = 8;
    }

    private void op_LD_A_C_OFFSET() {
        A = memory.read(0xFF00 + (getLower(BC)&0xFF));
        clockCycles = 8;
    }

    private void op_LDH_A_TO() {
        memory.write(nextPart(), A);
        clockCycles = 16;
    }

    private void op_LDH_A() {
        A = memory.read((nextByte() & 0xFF) | (nextByte() & 0xFF) << 8);
        clockCycles = 16;
    }

    private void op_CALL_NC() {
        clockCycles = 12;
        int address = nextPart();
        if (!Z) {
            push16Bit(PC);
            PC = address;
        }
    }

    private void op_NULL() {
        throw new RuntimeException("Invalid opcode");
    }

    private void op_JP_NC() {
        int address = nextPart();
        clockCycles = 12;
        if (!C) {
            clockCycles = 16;
            PC = address;
        }
    }

    private void op_POP_DE() {
        DE = popPart();
        clockCycles = 12;
    }

    private void op_RET_NC() {
        clockCycles = 8;
        if (!C) {
            int address = popPart();
            PC = address;
        }
    }

    private void op_RST_08H() {
        rst(0x08);
        clockCycles = 32;
    }

    private void op_ADC_A() {
        adc(nextByte());
        clockCycles = 8;
    }

    private void op_CALL() {
        clockCycles = 12;
        int address = nextPart();
        push16Bit(PC);
        PC = address;
        //System.out.println("CALL "+Integer.toHexString(address&0xFFFF));
    }

    private void op_CALL_Z() {
        clockCycles = 12;
        int address = nextPart();
        if (Z) {
            push16Bit(PC);
            PC = address;
        }
    }

    private void op_CBCode() {
        int b = nextByte();
        if ((b & 0xF) == 0x6 || (b & 0xF) == 0xE) {
            clockCycles = 16;
        } else {
            clockCycles = 8;
        }
        executeCBCode(b);
    }

    private void op_JP_Z() {
        clockCycles = 12;
        int address = nextPart();
        if (Z) {
            clockCycles = 16;
            PC = address;
        }
    }

    private void op_RET() {
        PC = popPart();
        clockCycles = 8;
    }

    private void op_RET_Z() {
        clockCycles = 8;
        if (Z) {
            int addr = popPart();
            PC = addr;
        }
    }

    private void op_RST_00H() {
        rst(0x00);
        clockCycles = 32;
    }

    private void op_ADD_A() {
        clockCycles = 8;
        add(nextByte());
    }

    private void op_PUSH_BC() {
        push16Bit(BC);
        clockCycles = 16;
    }

    private void op_CALL_NZ() {
        clockCycles = 12;
        int address = nextPart();
        if (!Z) {
            push16Bit(PC);
            PC = address;
        }
    }

    private void op_JP() {
        clockCycles = 12;
        PC = nextPart();
    }

    private void op_JP_NZ() {
        clockCycles = 12;
        int address = nextPart();
        if (!Z) {
            clockCycles = 16;
            PC = address;
        }
    }

    private void op_POP_BC() {
        clockCycles = 12;
        BC = popPart();
    }

    private void op_RET_NZ() {
        clockCycles = 8;
        if (!Z) {
            int addr = popPart();
            PC = addr;
        }
    }

    private void op_CP_A_A() {
        cp(A);
        clockCycles = 4;
    }

    private void op_CP_HL_VALUE() {
        cp(this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_CP_L() {
        cp(getLower(HL));
        clockCycles = 4;
    }

    private void op_CP_H() {
        cp(getUpper(HL));
        clockCycles = 4;
    }

    private void op_CP_E() {
        cp(getLower(DE));
        clockCycles = 4;
    }

    private void op_CP_D() {
        cp(getUpper(DE));
        clockCycles = 4;
    }

    private void op_CP_C() {
        cp(getLower(BC));
        clockCycles = 4;
    }

    private void op_CP_B() {
        cp(getUpper(BC));
        clockCycles = 4;
    }

    private void op_OR_A_A() {
        or(A);
        clockCycles = 4;
    }

    private void op_OR_HL_VALUE() {
        or(this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_OR_L() {
        or(getLower(HL));
        clockCycles = 4;
    }

    private void op_OR_H() {
        or(getUpper(HL));
        clockCycles = 4;
    }

    private void op_OR_E() {
        or(getLower(DE));
        clockCycles = 4;
    }

    private void op_OR_D() {
        or(getUpper(DE));
        clockCycles = 4;
    }

    private void op_OR_C() {
        or(getLower(BC));
        clockCycles = 4;
    }

    private void op_OR_B() {
        or(getUpper(BC));
        clockCycles = 4;
    }

    private void op_XOR_A_A() {
        xor(A);
        clockCycles = 4;
    }

    private void op_XOR_HL_VALUE() {
        xor(this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_XOR_L() {
        xor(getLower(HL));
        clockCycles = 4;
    }

    private void op_XOR_H() {
        xor(getUpper(HL));
        clockCycles = 4;
    }

    private void op_XOR_E() {
        xor(getLower(DE));
        clockCycles = 4;
    }

    private void op_XOR_D() {
        xor(getUpper(DE));
        clockCycles = 4;
    }

    private void op_XOR_C() {
        xor(getLower(BC));
        clockCycles = 4;
    }

    private void op_XOR_B() {
        xor(getUpper(BC));
        clockCycles = 4;
    }

    private void op_AND_A_A() {
        and(A);
        clockCycles = 4;
    }

    private void op_AND_HL_VALUE() {
        and(this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_AND_L() {
        and(getLower(HL));
        clockCycles = 4;
    }

    private void op_AND_H() {
        and(getUpper(HL));
        clockCycles = 4;
    }

    private void op_AND_E() {
        and(getLower(DE));
        clockCycles = 4;
    }

    private void op_AND_D() {
        and(getUpper(DE));
        clockCycles = 4;
    }

    private void op_AND_C() {
        and(getLower(BC));
        clockCycles = 4;
    }

    private void op_AND_B() {
        and(getUpper(BC));
        clockCycles = 4;
    }

    private void op_SBC_A_A() {
        sbc(A);
        clockCycles = 4;
    }

    private void op_SBC_A_HL_VALUE() {
        sbc(this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_SBC_A_L() {
        sbc(getLower(HL));
        clockCycles = 4;
    }

    private void op_SBC_A_H() {
        sbc(getUpper(HL));
        clockCycles = 4;
    }

    private void op_SBC_A_E() {
        sbc(getLower(DE));
        clockCycles = 4;
    }

    private void op_SBC_A_D() {
        sbc(getUpper(DE));
        clockCycles = 4;
    }

    private void op_SBC_A_C() {
        sbc(getLower(BC));
        clockCycles = 4;
    }

    private void op_SBC_A_B() {
        sbc(getUpper(BC));
        clockCycles = 4;
    }

    private void op_SUB_A_A() {
        clockCycles = 4;
        sub(A);
    }

    private void op_SUB_HL_VALUE() {
        clockCycles = 8;
        sub(this.memory.read(HL));
    }

    private void op_SUB_L() {
        clockCycles = 4;
        sub(getLower(HL));
    }

    private void op_SUB_H() {
        clockCycles = 4;
        sub(getUpper(HL));
    }

    private void op_SUB_E() {
        clockCycles = 4;
        sub(getLower(DE));
    }

    private void op_SUB_D() {
        clockCycles = 4;
        sub(getUpper(DE));
    }

    private void op_SUB_C() {
        clockCycles = 4;
        sub(getLower(BC));
    }

    private void op_SUB_B() {
        clockCycles = 4;
        sub(getUpper(BC));
    }

    private void op_ADC_A_A() {
        adc(A);
        clockCycles = 4;
    }

    private void op_ADC_A_HL_VALUE() {
        adc(this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_ADC_A_L() {
        adc(getLower(HL));
        clockCycles = 4;
    }

    private void op_ADC_A_H() {
        adc(getUpper(HL));
        clockCycles = 4;
    }

    private void op_ADC_A_E() {
        adc(getLower(DE));
        clockCycles = 4;
    }

    private void op_ADC_A_D() {
        adc(getUpper(DE));
        clockCycles = 4;
    }

    private void op_ADC_A_C() {
        adc(getLower(BC));
        clockCycles = 4;
    }

    private void op_ADC_A_B() {
        adc(getUpper(BC));
        clockCycles = 4;
    }

    private void op_ADD_A_A() {
        add(A);
        clockCycles = 4;
    }

    private void op_ADD_A_HL_VALUE() {
        add(this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_ADD_A_L() {
        add(getLower(HL));
        clockCycles = 4;
    }

    private void op_ADD_A_H() {
        add(getUpper(HL));
        clockCycles = 4;
    }

    private void op_ADD_A_E() {
        add(getLower(DE));
        clockCycles = 4;
    }

    private void op_ADD_A_D() {
        add(getUpper(DE));
        clockCycles = 4;
    }

    private void op_ADD_A_C() {
        add(getLower(BC));
        clockCycles = 4;
    }

    private void op_ADD_A_B() {
        add(getUpper(BC));
        clockCycles = 4;
    }

    private void op_LD_A_A() {
        A = A; // ???
        clockCycles = 4;
    }

    private void op_LD_A_HL_VALUE() {
        A = this.memory.read(HL);
        clockCycles = 8;
    }

    private void op_LD_A_L() {
        A = getLower(HL);
        clockCycles = 4;
    }

    private void op_LD_A_H() {
        A = getUpper(HL);
        clockCycles = 4;
    }

    private void op_LD_A_E() {
        A = getLower(DE);
        clockCycles = 4;
    }

    private void op_LD_A_D() {
        A = getUpper(DE);
        clockCycles = 4;
    }

    private void op_LD_A_C() {
        A = getLower(BC);
        clockCycles = 4;
    }

    private void op_LD_A_B() {
        A = getUpper(BC);
        clockCycles = 4;
    }

    private void op_LD_HL_VALUE_A() {
        this.memory.write(HL, A);
        clockCycles = 8;
    }

    private void op_HALT() {
        halted = true;
        clockCycles = 4;
    }

    private void op_LD_HL_VALUE_L() {
        this.memory.write(HL, getLower(getRegistryValue("HL")));
        clockCycles = 8;
    }

    private void op_LD_HL_VALUE_H() {
        this.memory.write(HL, getUpper(getRegistryValue("HL")));
        clockCycles = 8;
    }

    private void op_LD_HL_VALUE_E() {
        this.memory.write(HL, getLower(getRegistryValue("DE")));
        clockCycles = 8;
    }

    private void op_LD_HL_VALUE_D() {
        this.memory.write(HL, getUpper(getRegistryValue("DE")));
        clockCycles = 8;
    }

    private void op_LD_HL_VALUE_C() {
        this.memory.write(HL, getLower(getRegistryValue("BC")));
        clockCycles = 8;
    }

    private void op_LD_HL_VALUE_B() {
        this.memory.write(HL, getUpper(getRegistryValue("BC")));
        clockCycles = 8;
    }

    private void op_LD_L_A() {
        setLower("HL", A);
        clockCycles = 4;
    }

    private void op_LD_L_HL_VALUE() {
        setLower("HL", this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_LD_L_L() {
        setLower("HL", getLower(getRegistryValue("HL")));
        clockCycles = 4;
    }

    private void op_LD_L_H() {
        setLower("HL", getUpper(getRegistryValue("HL")));
        clockCycles = 4;
    }

    private void op_LD_L_E() {
        setLower("HL", getLower(getRegistryValue("DE")));
        clockCycles = 4;
    }

    private void op_LD_L_D() {
        setLower("HL", getUpper(getRegistryValue("DE")));
        clockCycles = 4;
    }

    private void op_LD_L_C() {
        setLower("HL", getLower(getRegistryValue("BC")));
        clockCycles = 4;
    }

    private void op_LD_L_B() {
        setLower("HL", getUpper(getRegistryValue("BC")));
        clockCycles = 4;
    }

    private void op_LD_H_A() {
        setUpper("HL", A);
        clockCycles = 4;
    }

    private void op_LD_H_HL_VALUE() {
        setUpper("HL", this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_LD_H_L() {
        setUpper("HL", getLower(getRegistryValue("HL")));
        clockCycles = 4;
    }

    private void op_LD_H_H() {
        setUpper("HL", getUpper(getRegistryValue("HL")));
        clockCycles = 4;
    }

    private void op_LD_H_E() {
        setUpper("HL", getLower(getRegistryValue("DE")));
        clockCycles = 4;
    }

    private void op_LD_H_D() {
        setUpper("HL", getUpper(getRegistryValue("DE")));
        clockCycles = 4;
    }

    private void op_LD_H_C() {
        setUpper("HL", getLower(getRegistryValue("BC")));
        clockCycles = 4;
    }

    private void op_LD_H_B() {
        setUpper("HL", getUpper(getRegistryValue("BC")));
        clockCycles = 4;
    }

    private void op_LD_E_A() {
        setLower("DE", A);
        clockCycles = 4;
    }

    private void op_LD_E_HL_VALUE() {
        setLower("DE", this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_LD_E_L() {
        setLower("DE", getLower(getRegistryValue("HL")));
        clockCycles = 4;
    }

    private void op_LD_E_H() {
        setLower("DE", getUpper(getRegistryValue("HL")));
        clockCycles = 4;
    }

    private void op_LD_E_E() {
        setLower("DE", getLower(getRegistryValue("DE")));
        clockCycles = 4;
    }

    private void op_LD_E_D() {
        setLower("DE", getUpper(getRegistryValue("DE")));
        clockCycles = 4;
    }

    private void op_LD_E_C() {
        setLower("DE", getLower(getRegistryValue("BC")));
        clockCycles = 4;
    }

    private void op_LD_E_B() {
        setLower("DE", getUpper(getRegistryValue("BC")));
        clockCycles = 4;
    }

    private void op_LD_D_A() {
        setUpper("DE", A);
        clockCycles = 4;
    }

    private void op_LD_D_HL_VALUE() {
        setUpper("DE", this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_LD_D_L() {
        setUpper("DE", getLower(getRegistryValue("HL")));
        clockCycles = 4;
    }

    private void op_LD_D_H() {
        setUpper("DE", getUpper(getRegistryValue("HL")));
        clockCycles = 4;
    }

    private void op_LD_D_E() {
        setUpper("DE", getLower(getRegistryValue("DE")));
        clockCycles = 4;
    }

    private void op_LD_D_D() {
        setUpper("DE", getUpper(getRegistryValue("DE")));
        clockCycles = 4;
    }

    private void op_LD_D_C() {
        setUpper("DE", getLower(getRegistryValue("BC")));
        clockCycles = 4;
    }

    private void op_LD_D_B() {
        setUpper("DE", getUpper(getRegistryValue("BC")));
        clockCycles = 4;
    }

    private void op_LD_C_A() {
        setLower("BC", A);
        clockCycles = 4;
    }

    private void op_LD_C_HL_VALUE() {
        setLower("BC", this.memory.read(HL));
        clockCycles = 8;
    }

    private void op_LD_C_L() {
        setLower("BC", getLower(getRegistryValue("HL")));
        clockCycles = 4;
    }

    private void op_LD_C_H() {
        setLower("BC", getUpper(getRegistryValue("HL")));
        clockCycles = 4;
    }

    private void op_LD_C_E() {
        setLower("BC", getLower(getRegistryValue("DE")));
        clockCycles = 4;
    }

    private void op_LD_C_D() {
        setLower("BC", getUpper(getRegistryValue("DE")));
        clockCycles = 4;
    }

    private void op_LD_C_C() {
        setLower("BC", getLower(getRegistryValue("BC")));
        clockCycles = 4;
    }

    private void op_LD_C_B() {
        setLower("BC", getUpper(getRegistryValue("BC")));
        clockCycles = 4;
    }

    private void op_LD_B_A() {
        clockCycles = 4;
        setUpper("BC", A);
    }

    private void op_LD_B_HL_VALUE() {
        clockCycles = 8;
        setUpper("BC", this.memory.read(HL));
    }

    private void op_LD_B_L() {
        clockCycles = 4;
        setUpper("BC", getLower(getRegistryValue("HL")));
    }

    private void op_LD_B_H() {
        clockCycles = 4;
        setUpper("BC", getUpper(getRegistryValue("HL")));
    }

    private void op_LD_B_E() {
        clockCycles = 4;
        setUpper("BC", getLower(getRegistryValue("DE")));
    }

    private void op_LD_B_D() {
        clockCycles = 4;
        setUpper("BC", getUpper(getRegistryValue("DE")));
    }

    private void op_LD_B_C() {
        clockCycles = 4;
        setUpper("BC", getLower(getRegistryValue("BC")));
    }

    private void op_LD_B_B() {
        clockCycles = 4;
        setUpper("BC", getUpper(getRegistryValue("BC")));
    }

    private void op_CCF() {
        C = !C;
        H = false;
        N = false;
        clockCycles = 4;
    }

    private void op_LD_A() {
        clockCycles = 8;
        A = nextByte();
    }

    private void op_DEC_A() {
        A = dec(A);
        clockCycles = 4;
    }

    private byte dec(byte r) {
        H = (r & 0xF) == 0xF;
        r--;
        Z = (r&0xFF) == 0;
        N = true;
        return r;
    }

    private byte inc(byte r) {
        H = (r & 0xF) == 0xF;
        r++;
        Z = r == 0;
        N = false;
        return r;
    }

    private void op_INC_A() {
        clockCycles = 4;
        A = inc(A);
    }

    private void op_DEC_SP() {
        clockCycles = 8;
        SP--;
    }

    private void op_LD_A_HL_DEC() {
        A = this.memory.read(HL);
        HL--;
        clockCycles = 8;
    }

    private void op_ADD_HL_SP() {
        clockCycles = 8;
        addRegs("HL", "SP");
    }

    private void op_JR_C() {
        clockCycles = 8;
        byte offset = nextByte();
        if (C) {
            relativeJump(offset);
          //  PC -= 2; // accounts for the fact that the PC increased twice before running this instruction
        }
    }

    private void op_SCF() {
        C = true;
        H = false;
        N = false;
        clockCycles = 4;
    }

    private void op_LD_HL_VALUE() {
        this.memory.write(HL, nextByte());
        clockCycles = 12;
    }

    private void op_DEC_HL_VALUE() {
        clockCycles = 12;
        this.memory.write(HL, dec(this.memory.read(HL)));
    }

    private void op_INC_HL_VALUE() {
        clockCycles = 12;
        this.memory.write(HL, inc(this.memory.read(HL)));
    }

    private void op_INC_SP() {
        SP++;
        clockCycles = 8;
    }

    private void op_LD_HL_DEC_A() {
        clockCycles = 8;
        this.memory.write(HL, A);
        HL--;
    }

    private void op_LD_SP16b() {
        clockCycles = 12;
        SP = nextPart();
    }

    private void op_JR_NC() {
        clockCycles = 8;
        byte offset = nextByte();
        if (!C) {
            relativeJump(offset);
            //PC -= 2; // accounts for the fact that the PC increased twice before running this instruction
        }
    }

    private void op_CPL() {
        A = (byte) (~A & 0xFF);
        N = true;
        H = true;
        clockCycles = 4;
    }

    private void op_LD_L() {
        setLower("HL", nextByte());
        clockCycles = 8;
    }

    private void op_DEC_L() {
        setLower("HL", dec(getLower(HL)));
        clockCycles = 4;
    }

    private void op_INC_L() {
        setLower("HL", inc(getLower(HL)));
        clockCycles = 4;
    }

    private void op_DEC_HL() {
        HL--;
        clockCycles = 8;
    }

    private void op_LD_A_HL_INC() {
        clockCycles = 8;
        A = this.memory.read(HL++);
    }

    private void op_ADD_HL_HL() {
        clockCycles = 8;
        addRegs("HL", "HL");
    }

    private void op_JR_Z() {
        clockCycles = 8;
        byte offset = nextByte();
        if (Z) {
            relativeJump(offset);
          //  PC -= 2; // accounts for the fact that the PC increased twice before running this instruction
        }
    }

    private void op_DAA() {
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

    private void op_LD_H() {
        setUpper("HL", nextByte());
        clockCycles = 8;
    }

    private void op_DEC_H() {
        clockCycles = 4;
        setUpper("HL", dec(getUpper(HL)));
    }

    private void op_INC_H() {
        clockCycles = 4;
        setUpper("HL", inc(getUpper(HL)));
    }

    private void op_INC_HL() {
        clockCycles = 8;
        HL++;
    }

    private void op_LD_HL_INC_A() {
        clockCycles = 8;
        this.memory.write(HL, A);
        HL++;
    }

    private void op_LD_HL() {
        HL = nextPart();
        clockCycles = 12;
    }

    private void op_JR_NZ() {
        clockCycles = 8;
        int offset = nextByte() & 0xFF;
        if (!Z) {
      //      PC -= 2; // accounts for the fact that the PC increased twice before running this instruction
            relativeJump(offset);
        }
    }

    private void op_RRA() {
        A = rr(A);
        clockCycles = 4;
        Z = false;
    }

    private void op_LD_E() {
        setLower("DE", nextByte());
        clockCycles = 8;
    }

    private void op_DEC_E() {
        setLower("DE", dec(getLower(DE)));
        clockCycles = 4;
    }

    private void op_INC_E() {
        setLower("DE", inc(getLower(DE)));
        clockCycles = 4;
    }

    private void op_DEC_DE() {
        DE--;
        clockCycles = 8;
    }

    private void op_LD_A_DE() {
        clockCycles = 8;
        A = this.memory.read(DE);
    }

    private void op_ADD_HL_DE() {
        clockCycles = 8;
        addRegs("HL", "DE");
    }

    private void op_JR() {
        clockCycles = 8;
        relativeJump(nextByte() & 0xFF);
    //    PC -= 2; // accounts for the fact that the PC increased twice before running this instruction
    }

    private void op_RLA() {
        A = rl(A);
        clockCycles = 4;
        Z = false;
    }

    private void op_LD_D() {
        setUpper("DE", nextByte());
        clockCycles = 8;
    }

    private void op_DEC_D() {
        setUpper("DE", dec(getUpper(DE)));
        clockCycles = 4;
    }

    private void op_INC_D() {
        setUpper("DE", inc(getUpper(DE)));
        clockCycles = 4;
    }

    private void op_INC_DE() {
        clockCycles = 8;
        DE++;
    }

    private void op_LD_DE_A() {
        this.memory.write(DE, A);
        clockCycles = 8;
    }

    private void op_LD_DE() {
        DE = nextPart();
        clockCycles = 12;
    }

    private void op_STOP() {
        stop = true;
        clockCycles = 4;
    }

    private void op_RRCA() {
        A = rrc(A);
        Z = false;
        clockCycles = 4;
    }

    private void op_LD_C() {
        setLower("BC", nextByte());
        clockCycles = 8;
    }

    private void op_DEC_C() {
        clockCycles = 4;
        setLower("BC", dec(getLower(BC)));
    }

    private void op_INC_C() {
        clockCycles = 4;
        setLower("BC", inc(getLower(BC)));
    }

    private void op_DEC_BC() {
        clockCycles = 8;
        BC--;
    }

    private void op_LD_A_BC() {
        A = this.memory.read(BC);
        clockCycles = 8;
    }

    private void op_ADD_HL_BC() {
        clockCycles = 8;
        addRegs("HL", "BC");
    }

    private void op_LD_SP8b() {
        clockCycles = 20;
        write16Bits(nextPart(), SP);
    }

    private void op_RLCA() {
        clockCycles = 4;
        A = rlc(A);
        Z = false;
    }

    private void op_LD_B() {
        setUpper("BC", nextByte());
        clockCycles = 8;
    }

    private void op_INC_B() {
        setUpper("BC", inc(getUpper(BC)));
        clockCycles = 4;
    }

    private void op_DEC_B() {
        setUpper("BC", dec(getUpper(BC)));
        clockCycles = 4;
    }

    private void op_INC_BC() {
        BC++;
        clockCycles = 8;
    }

    private void op_LD_BC() {
        BC = nextPart();
        clockCycles = 12;
    }

    private void op_LD_BC_A() {
        this.memory.write(BC, A);
        clockCycles = 8;
    }

    private void op_nop() {
        // no operation
        clockCycles = 4;
    }

    public void setLower(String registry, byte val) {
        int registryValue = getRegistryValue(registry);
        registryValue = ((registryValue & 0xFF00) | (val & 0xFF));
        setRegistryValue(registry, registryValue);
    }

    public void setUpper(String registry, byte val) {
        int registryValue = getRegistryValue(registry);
        registryValue = ((registryValue & 0x00FF) | ((val & 0xFF) << 8));
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
            setFlags((byte) (registryValue & 0xFF));
        } else if (registry.equals("HL")) {
            HL = registryValue;
        } else if (registry.equals("SP")) {
            SP = registryValue;
        } else if (registry.equals("B")) {
            setUpper("BC", (byte) (registryValue & 0xFF));
        } else if (registry.equals("C")) {
            setLower("BC", (byte) (registryValue & 0xFF));
        } else if (registry.equals("D")) {
            setUpper("DE", (byte) (registryValue & 0xFF));
        } else if (registry.equals("E")) {
            setLower("DE", (byte) (registryValue & 0xFF));
        } else if (registry.equals("H")) {
            setUpper("HL", (byte) (registryValue & 0xFF));
        } else if (registry.equals("L")) {
            setLower("HL", (byte) (registryValue & 0xFF));
        } else if(registry.equals("(HL)")) {
            memory.write(HL, (byte) (registryValue & 0xFF));
        }
    }

    public int getRegistryValue(String registry) {
        if (registry.equals("BC")) {
            return BC;
        } else if (registry.equals("A")) {
            return A;
        } else if (registry.equals("F")) {
            return getFlags();
        } else if (registry.equals("DE")) {
            return DE;
        } else if (registry.equals("HL")) {
            return HL;
        } else if (registry.equals("SP")) {
            return SP;
        } else if(registry.equals("B")) {
            return getUpper(BC) & 0xFF;
        } else if(registry.equals("C")) {
            return getLower(BC) & 0xFF;
        } else if(registry.equals("D")) {
            return getUpper(DE) & 0xFF;
        } else if(registry.equals("E")) {
            return getLower(DE) & 0xFF;
        } else if(registry.equals("H")) {
            return getUpper(HL) & 0xFF;
        } else if(registry.equals("L")) {
            return getLower(HL) & 0xFF;
        } else if(registry.equals("(HL)")) {
            return memory.read(HL & 0xFFFF) & 0xFF;
        }
        return 0;
    }

    public byte getLower(int val) {
        return (byte) (val & 0xFF);
    }

    public byte getUpper(int val) {
        return (byte) ((val >> 8) & 0xFF);
    }

    public void rst(int addr) {
        push16Bit(PC);
        PC = addr;
    }

    public void add(byte b) {
        int n = (A & 0xFF) + (b & 0xFF);
        Z = (n & 0xFF) == 0;
        H = (((A & 0x0F) + (b & 0x0F)) & 0x10) == 0x10;
        C = n > 0xFF;
        N = false;
        A = (byte) (n & 0xFF);
    }

    public void adc(byte b) {
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

    public void sub(byte b) {
        cp(b);
        A -= b;
    }

    public void sbc(byte b) {
        if(C)
            b++;
        H = ((A & 0xF) - (b & 0xF)) < 0;
        C = ((A & 0xFF) - (b & 0xFF) - 1) < 0;
        N = true;
        A -= b & 0xFF;
        Z = A == 0;
    }

    public void and(byte b) {
        A &= b;
        Z = A == 0;
        H = true;
        N = false;
        C = false;
    }

    public void xor(byte b) {
        A ^= b;
        Z = A == 0;
        H = false;
        N = false;
        C = false;
    }

    public void or(byte b) {
        A |= b;
        Z = A == 0;
        H = false;
        N = false;
        C = false;
    }

    public void cp(byte b) {
        Z = A == b;
        H = (A & 0xF) < (b & 0xF);
        N = true;
        C = A < b;
    }

    public byte rl(byte val) {
        boolean oldcarry = C;
        C = (val & (1 << 7)) == (1 << 7);
        H = false;
        N = false;
        val <<= 1;
        if (oldcarry) {
            val |= 0x1;
        }
        Z = val == 0;
        return val;
    }

    public byte rlc(byte val) {
        C = (val & (1 << 7)) == (1 << 7);
        H = false;
        N = false;
        val <<= 1;
        if(C)
            val ^= 0x1;
        Z = val == 0;
        return val;
    }

    public byte rr(byte val) {
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

    public byte rrc(byte val) {
        C = (val & 0x1) == 0x1;
        H = false;
        N = false;
        val >>= 1;
        if(C)
            val |= 0x80;
        Z = val == 0;
        return (byte) val;
    }

    public byte sla(byte val) {
        C = (val & (1 << 7)) != 0;
        val <<= 1;
        Z = val == 0;
        H = false;
        N = false;
        return val;
    }

    public byte sra(byte val) {
        C = (val & 0x1) != 0;
        val = (byte) (((val & (1 << 7)) | (val >> 1)) & 0xFF);
        Z = val == 0;
        H = false;
        N = false;
        return val;
    }

    public byte swap(byte val) {
        Z = val == 0;
        N = false;
        H = false;
        C = false;
        return (byte) ((val << 4) | (val >> 4));
    }

    public byte srl(byte val) {
        C = (val & 0x1) != 0;
        val >>= 1;
        Z = val == 0;
        H = false;
        N = false;
        return val;
    }

    public void bit(int val, int b) {
        Z = (val & (1 << b)) != 0;
        N = false;
        H = true;
    }

    public void addRegs(String registryA, String registryB) {
        int a = getRegistryValue(registryA) & 0xFFFF;
        int b = getRegistryValue(registryB) & 0xFFFF;
        int temp = a + b;
        N = false;
        C = temp > 0xFFFF;
        H = ((a & 0x0FFF) + (b & 0x0FFF)) > 0x0FFF;
        setRegistryValue(registryA, (temp & 0xFFFF));
    }

    public void relativeJump(int d) {
        //PC += d;
        PC = signedAdd(PC, d);
    }

    private int signedAdd(int a, int b) {
        byte bSigned = (byte) (b & 0xFF);
        if(bSigned >= 0) {
            return a + ((bSigned)&0xFF);
        } else {
            return a - ((-bSigned)&0xFF);
        }
    }

    private final String[] cbRegisterList = {"B", "C", "D", "E", "H", "L", "(HL)", "A"};

    private void executeCBCode(int b) {
        b = b & 0xFF;
        if(b >= 0x40 && b <= 0x7F) { // BIT b,register opcodes
            int registerIndex = (b-0x40) % 8;
            int bitIndex = (b-0x40) / 8;
            String register = cbRegisterList[registerIndex];
            bit(getRegistryValue(register) & 0xFF, bitIndex);
            if(register.equals("(HL)"))
                clockCycles = 16;
        } else if(b >= 0xC0 && b <= 0xFF) { // SET b,register opcodes
            int registerIndex = (b-0xC0) % 8;
            int bitIndex = (b-0xC0) / 8;
            String register = cbRegisterList[registerIndex];
            setRegistryValue(register, getRegistryValue(register) | (1<<bitIndex));
            if(register.equals("(HL)"))
                clockCycles = 16;
        } else if(b >= 0x80 && b <= 0xBF) { // RST b,register opcodes
            int registerIndex = (b-0x80) % 8;
            int bitIndex = (b-0x80) / 8;
            String register = cbRegisterList[registerIndex];
            setRegistryValue(register, getRegistryValue(register) & ~(1<<bitIndex));
            clockCycles = 8;
            if(register.equals("(HL)"))
                clockCycles = 16;
        } else {
            switch (b) {
                case 0x37: {
                    clockCycles = 8;
                    A = swap(A);
                    break;
                }

                case 0x30: {
                    clockCycles = 8;
                    setUpper("BC", swap(getUpper(BC)));
                    break;
                }

                case 0x31: {
                    clockCycles = 8;
                    setLower("BC", swap(getLower(BC)));
                    break;
                }

                case 0x32: {
                    clockCycles = 8;
                    setUpper("DE", swap(getUpper(DE)));
                    break;
                }

                case 0x33: {
                    clockCycles = 8;
                    setLower("DE", swap(getLower(DE)));
                    break;
                }

                case 0x34: {
                    clockCycles = 8;
                    setUpper("HL", swap(getUpper(HL)));
                    break;
                }

                case 0x35: {
                    setLower("HL", swap(getLower(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x36: {
                    memory.write(HL, swap(memory.read(HL)));
                    clockCycles = 16;
                    break;
                }

                case 0x07: {
                    A = rlc(A);
                    clockCycles = 8;
                    break;
                }

                case 0x00: {
                    setUpper("BC", rlc(getUpper(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x01: {
                    setLower("BC", rlc(getLower(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x02: {
                    setUpper("DE", rlc(getUpper(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x03: {
                    setLower("DE", rlc(getLower(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x04: {
                    setUpper("HL", rlc(getUpper(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x05: {
                    setLower("HL", rlc(getLower(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x06: {
                    memory.write(HL, rlc(memory.read(HL)));
                    clockCycles = 16;
                    break;
                }

                case 0x17: {
                    A = rl(A);
                    clockCycles = 8;
                    break;
                }

                case 0x10: {
                    setUpper("BC", rl(getUpper(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x11: {
                    setLower("BC", rl(getLower(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x12: {
                    setUpper("DE", rl(getUpper(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x13: {
                    setLower("DE", rl(getLower(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x14: {
                    setUpper("HL", rl(getUpper(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x15: {
                    setLower("HL", rl(getLower(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x16: {
                    memory.write(HL, rl(memory.read(HL)));
                    clockCycles = 16;
                    break;
                }

                case 0x0F: {
                    A = rrc(A);
                    clockCycles = 8;
                    break;
                }

                case 0x08: {
                    setUpper("BC", rrc(getUpper(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x09: {
                    setLower("BC", rrc(getLower(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x0A: {
                    setUpper("DE", rrc(getUpper(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x0B: {
                    setLower("DE", rrc(getLower(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x0C: {
                    setUpper("HL", rrc(getUpper(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x0D: {
                    setLower("HL", rrc(getLower(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x0E: {
                    memory.write(HL, rrc(memory.read(HL)));
                    clockCycles = 16;
                    break;
                }

                case 0x1F: {
                    A = rr(A);
                    clockCycles = 8;
                    break;
                }

                case 0x18: {
                    setUpper("BC", rr(getUpper(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x19: {
                    setLower("BC", rr(getLower(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x1A: {
                    setUpper("DE", rr(getUpper(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x1B: {
                    setLower("DE", rr(getLower(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x1C: {
                    setUpper("HL", rr(getUpper(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x1D: {
                    setLower("HL", rr(getLower(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x1E: {
                    memory.write(HL, rr(memory.read(HL)));
                    clockCycles = 16;
                    break;
                }

                case 0x27: {
                    A = sla(A);
                    clockCycles = 8;
                    break;
                }

                case 0x20: {
                    setUpper("BC", sla(getUpper(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x21: {
                    setLower("BC", sla(getLower(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x22: {
                    setUpper("DE", sla(getUpper(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x23: {
                    setLower("DE", sla(getLower(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x24: {
                    setUpper("HL", sla(getUpper(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x25: {
                    setLower("HL", sla(getLower(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x26: {
                    memory.write(HL, sla(memory.read(HL)));
                    clockCycles = 16;
                    break;
                }

                case 0x2F: {
                    A = sra(A);
                    clockCycles = 8;
                    break;
                }

                case 0x28: {
                    setUpper("BC", sra(getUpper(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x29: {
                    setLower("BC", sra(getLower(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x2A: {
                    setUpper("DE", sra(getUpper(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x2B: {
                    setLower("DE", sra(getLower(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x2C: {
                    setUpper("HL", sra(getUpper(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x2D: {
                    setLower("HL", sra(getLower(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x2E: {
                    memory.write(HL, sra(memory.read(HL)));
                    clockCycles = 16;
                    break;
                }

                case 0x3F: {
                    A = srl(A);
                    clockCycles = 8;
                    break;
                }

                case 0x38: {
                    setUpper("BC", srl(getUpper(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x39: {
                    setLower("BC", srl(getLower(BC)));
                    clockCycles = 8;
                    break;
                }

                case 0x3A: {
                    setUpper("DE", srl(getUpper(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x3B: {
                    setLower("DE", srl(getLower(DE)));
                    clockCycles = 8;
                    break;
                }

                case 0x3C: {
                    setUpper("HL", srl(getUpper(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x3D: {
                    setLower("HL", srl(getLower(HL)));
                    clockCycles = 8;
                    break;
                }

                case 0x3E: {
                    memory.write(HL, srl(memory.read(HL)));
                    clockCycles = 16;
                    break;
                }

                default:
                    System.out.println("CPU: Unknown CB code: " + Integer.toHexString(b & 0xFF));
                    break;
            }
        }
    }

    public boolean isHalted() {
        return halted;
    }

    public boolean isStopped() {
        return stop;
    }

    public void turnOn() {
        stop = false;
        hardReset();
        A = 1;
        BC = 0x13;
        DE = 0xD8;
        HL = 0x14D;
        SP = 0xFFFE;
        setFlags(0xB0);
        memory.write(0xFF05, (byte)0x00); // TIMA
        memory.write(0xFF06, (byte)0x00); // TMA
        memory.write(0xFF07, (byte)0x00); // TAC
        memory.write(0xFF10, (byte)0x80); // NR10
        memory.write(0xFF11, (byte)0xBF); // NR11
        memory.write(0xFF12, (byte)0xF3); // NR12
        memory.write(0xFF14, (byte)0xBF); // NR14
        memory.write(0xFF16, (byte)0x3F); // NR21
        memory.write(0xFF17, (byte)0x00); // NR22
        memory.write(0xFF19, (byte)0xBF); // NR24
        memory.write(0xFF1A, (byte)0x7F); // NR30
        memory.write(0xFF1B, (byte)0xFF); // NR31
        memory.write(0xFF1C, (byte)0x9F); // NR32
        memory.write(0xFF1E, (byte)0xBF); // NR33
        memory.write(0xFF20, (byte)0xFF); // NR41
        memory.write(0xFF21, (byte)0x00); // NR42
        memory.write(0xFF22, (byte)0x00); // NR43
        memory.write(0xFF23, (byte)0xBF); // NR30
        memory.write(0xFF24, (byte)0x77); // NR50
        memory.write(0xFF25, (byte)0xF3); // NR51
        memory.write(0xFF26, (byte)0xF1); // NR52
        memory.write(0xFF40, (byte)0x91); // LCDC
        memory.write(0xFF42, (byte)0x00); // SCY
        memory.write(0xFF43, (byte)0x00); // SCX
        memory.write(0xFF45, (byte)0x00); // LYC
        memory.write(0xFF47, (byte)0xFC); // BGP
        memory.write(0xFF48, (byte)0xFF); // OBP0
        memory.write(0xFF49, (byte)0xFF); // OBP1
        memory.write(0xFF4A, (byte)0x00); // WY
        memory.write(0xFF4B, (byte)0x00); // WX
        memory.write(0xFFFF, (byte)0x00); // IE
    }

    public void setFlags(int flags) {
        Z = BitUtils.getBit(flags, 7);
        N = BitUtils.getBit(flags, 6);
        H = BitUtils.getBit(flags, 5);
        C = BitUtils.getBit(flags, 4);
    }

    public boolean areInterruptsDisabled() {
        return disabledInterrupts;
    }

    public void forceDisableInterrupts() {
        disabledInterrupts = true;
    }

    public MemoryController getMemory() {
        return memory;
    }
}
