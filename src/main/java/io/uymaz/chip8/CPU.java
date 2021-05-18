package io.uymaz.chip8;

import java.io.*;
import java.util.Calendar;

import static java.lang.String.*;

public class CPU {

    int opcode; //currently fetched opcode

    int[] memory;

    int[] display; //64x32 = 2048

    byte[] registers;
    int indexRegister;

    int pc;
    int sp;

    int delayTimer;
    int soundTimer;

    int[] stack;

    public CPU() {
        pc = 0x200; //default start value
        opcode = 0;
        indexRegister = 0;
        sp = 0;

        delayTimer = 0;
        soundTimer = 0;

        display = new int[2048];
        memory = new int[0xFFF];
        registers = new byte[16];
        stack = new int[16];

        loadFontset();
    }

    private void fetchOpcode() {
        /*
            this operation consists of two steps:
            first, the memory value at pc is shifted left by 8 - i.e., 8 zeroes are added.
            second, we perform a bitwise or on the resulting 16 bit value and the memory value at [pc+1],
                    thereby effectively merging memory[pc] and memory[pc+1].
         */
        opcode = (memory[pc] << 8) | memory[pc +1];
        Emulator.cpuLogger.info(format("OPCode %05X has been loaded", opcode));
    }

    private void decodeAndExecuteOpcode() {
        //look at first 4 bits only (rest is irrelevant)
        switch(opcode & 0xf000) {
            case 0x0000: //CLS, RET
                if((opcode & 0x00FF) == 0x00EE) { //RET
                    pc = stack[sp];
                    sp--;
                }
                else { //CLS

                }
                break;
            case 0x1000: //JP
                pc = opcode & 0x0FFF;
                break;
            case 0x2000: //CALL
                sp++;
                stack[sp] = pc;
                pc = opcode & 0x0FFF;
                break;
            case 0x3000: //SE
                if(registers[opcode & 0x0F00 >> 8] == (opcode & 0x00FF)) {
                    pc += 2;
                }
                break;
            case 0x4000: //SNE
                if(registers[opcode & 0x0F00 >> 8] != (opcode & 0x00FF)) {
                    pc += 2;
                }
                break;
            case 0x5000: //SE
                if(registers[opcode & 0x0F00 >> 8] == registers[opcode & 0x00F0 >> 4]) {
                   pc += 2;
                }
                break;
            case 0x6000: //LD
                registers[opcode & 0x0F00 >> 8] = (byte) (opcode & 0x00FF);
                pc += 2;
                break;
            case 0x7000: //ADD
                registers[opcode & 0x0F00 >> 8] += (byte) (opcode & 0x00FF);
                pc += 2;
                break;
            case 0x8000: //LD, OR, AND, XOR, ADD, SUB, SHR, SUBN, SHL
                switch(opcode & 0x000F) {
                    case 0x0000: //LD
                        registers[opcode & 0x0F00 >> 8] = registers[opcode & 0x00F0 >> 4];
                        pc += 2;
                        break;
                    case 0x0001: //OR
                        registers[opcode & 0x0F00 >> 8] |= registers[opcode & 0x00F0 >> 4];
                        pc += 2;
                        break;
                    case 0x0002: //AND
                        registers[opcode & 0x0F00 >> 8] &= registers[opcode & 0x00F0 >> 4];
                        pc += 2;
                        break;
                    case 0x0003: //XOR
                        registers[opcode & 0x0F00 >> 8] ^= registers[opcode & 0x00F0 >> 4];
                        pc += 2;
                        break;
                    case 0x0004: //ADD with carry flag
                        byte a = registers[opcode & 0x0F00 >> 8];
                        byte b = registers[opcode & 0x0F00 >> 4];

                        if(a + b > 127) {
                            registers[0xF] = 1;
                        }
                        else if(a + b < -128) {
                            registers[0xF] = 1;
                        }
                        else {
                            registers[0xF] = 0;
                        }

                        registers[opcode & 0x0F00 >> 8] += registers[opcode & 0x00F0 >> 4];

                        pc += 2;
                        break;
                    //TODO fix carryover flag calculation
                    case 0x0005: //SUB
                        if(registers[opcode & 0x0F00 >> 8] > registers[opcode & 0x00F0 >> 4]) {
                            registers[0xF] = 1; //carry register
                        }
                        else {
                            registers[0xF] = 0;
                        }
                        registers[opcode & 0x0F00 >> 8] -= registers[opcode & 0x00F0 >> 4];
                        pc += 2;
                        break;
                    //TODO fix carryover flag calculation
                    case 0x0006: //SHR
                        if((registers[opcode & 0x0F00 >> 8] & 0x0001) == 1) {
                            registers[0xF] = 1;
                        }
                        else {
                            registers[0xF] = 0;
                        }
                        registers[opcode & 0x0F00 >> 8] /= 2;
                        pc += 2;
                        break;
                    //TODO fix carryover flag calculation
                    case 0x0007: //SUBN
                        if(registers[opcode & 0x00F0 >> 4] > registers[opcode & 0x0F00 >> 8]) {
                            registers[0xF] = 1;
                        }
                        else {
                            registers[0xF] = 0;
                        }
                        registers[opcode & 0x0F00 >> 8] = (byte) (registers[opcode & 0x00F0 >> 4] - registers[opcode & 0x0F00 >> 8]);
                        pc += 2;
                        break;
                    //TODO fix carryover flag calculation
                    case 0x000E:
                        if((registers[opcode & 0x0F00 >> 8] & 0x0010) == 1) {
                            registers[0xF] = 1;
                        }
                        else {
                            registers[0xF] = 0;
                        }
                        registers[opcode & 0x0F00 >> 8] *= 2;
                        pc += 2;
                        break;
                }
                break;
            case 0x9000: //SNE
                if(registers[opcode & 0x0F00 >> 8] != registers[opcode & 0x00F0 >> 4]) {
                    pc += 2;
                }
                break;
            case 0xA000: //LD
                indexRegister = (opcode & 0x0FFF);
                pc += 2;
                break;
            case 0xB000: //JP
                pc = (opcode & 0x0FFF) + registers[0];
                break;
            case 0xC000: //RND
                byte randomNumber = (byte) new java.util.Random().nextInt(255);
                registers[opcode & 0x0F00 >> 8] = (byte) (randomNumber & (opcode & 0x00FF));
                break;
            case 0xD000: //DRW

                break;
            case 0xE000: //SKP, SKNP
                switch(opcode & 0x00FF) {
                    case 0x009E: //SKP

                        break;
                    case 0x00A1: //SKNP

                        break;
                }
                break;
            case 0xF000: //LD, ADD
                switch(opcode & 0x00FF) {
                    case 0x0007:
                        registers[opcode & 0x0F00 >> 8] = (byte) delayTimer;
                        break;
                    case 0x000A:

                        break;
                    case 0x0015:

                        break;
                    case 0x0018:

                        break;
                    case 0x001E:

                        break;
                    case 0x0029:

                        break;
                    case 0x0033:

                        break;
                    case 0x0055:

                        break;
                    case 0x0065:

                        break;

                }
                break;
            default:
                Emulator.cpuLogger.severe(format("unknown opcode: %s", opcode));

        }


    }

    protected void dumpArrayToFile(int[] array) {
        File file = new File(System.getProperty("user.dir") + "/logs/DUMP-" + Calendar.getInstance().getTime().toString().replaceAll("[ :]", "-") + ".txt");
        try(PrintWriter printWriter = new PrintWriter(new FileWriter(file))) {
            for(int i = 0; i < array.length; i++) {
                printWriter.printf("0x%03X\t0x%02X\n", i, memory[i]);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void doCycle() {
        fetchOpcode();
        decodeAndExecuteOpcode();

    }

    public void loadProgram(File file) {
        byte[] byteMemory = new byte[0xFFF];
        try (InputStream inputStream = new FileInputStream(file)) {
            inputStream.read(byteMemory, 0x200, 0xDFF);
            for(int i = 0x200; i < 0xFFF; i++) {
                /*
                    in order to convert from byte to int, the int in question has to be ANDed with 0xFF in order to
                    nullify unneeded bits, i.e. any bit from 32 to 9.
                 */
                memory[i] = (byteMemory[i] & 0xFF);
            }
            Emulator.cpuLogger.info("ROM has been loaded into memory");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFontset() {
        //by specification, the fontset has to be in the first 16*5 = 80 bytes of the memory.
        for(int i = 0; i < 16*5; i++) {
            memory[i] = Fontset.fontset[i];
        }
    }
}
