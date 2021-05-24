package io.uymaz.chip8;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.Calendar;

import static java.lang.String.*;

public class CPU implements Serializable {

    static File rom;

    @Serial
    private static final long serialVersionUID = -7888118080367890773L;

    Input inputHandler;
    Display displayHandler;

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

    int cyclesNeededForRefreshing;
    final int freq = 6000;
    int cycleNanoSeconds;
    int drawFlag;

    public CPU() {
        pc = 0x200; //default start value
        opcode = 0;
        indexRegister = 0;
        sp = 0;

        cyclesNeededForRefreshing = freq / 60;
        cycleNanoSeconds = 1000000000 / freq;

        this.displayHandler = new Display(this);
        this.inputHandler = new Input();

        delayTimer = 0;
        soundTimer = 0;

        display = new int[2048];
        memory = new int[0xFFF];
        registers = new byte[16];
        stack = new int[16];

        loadFontset();

        initializeGUI();
    }

    private void fetchOpcode() {
        /*
            this operation consists of two steps:
            first, the memory value at pc is shifted left by 8 - i.e., 8 zeroes are added.
            second, we perform a bitwise or on the resulting 16 bit value and the memory value at [pc+1],
                    thereby effectively merging memory[pc] and memory[pc+1].
         */
        opcode = (memory[pc] << 8) | memory[pc +1];
        Emulator.cpuLogger.info(format("OPCode %04X at address 0x%04X has been loaded", opcode, pc));
    }

    private void decodeAndExecuteOpcode() {
        //look at first 4 bits only (rest is irrelevant)
        pc+=2;
        switch(opcode & 0xf000) {
            case 0x0000: //CLS, RET
                if((opcode & 0x00FF) == 0x00EE) { //RET
                    pc = stack[sp];
                    sp--;

                }
                else { //CLS
                    for(int i = 0; i < 2048; i++) {
                        display[i] = 0;
                    }
                    drawFlag = 1;

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
                if(registers[(opcode & 0x0F00) >> 8] == (byte)  ((opcode & 0x00FF))) {
                    pc+=2;
                }

                break;
            case 0x4000: //SNE
                if(registers[(opcode & 0x0F00) >> 8] != (byte) ((opcode & 0x00FF) & 0xFF)) {
                    pc+=2;
                }

                break;
            case 0x5000: //SE
                if(registers[(opcode & 0x0F00) >> 8] == (byte) registers[(opcode & 0x00F0) >> 4]) {
                    pc+=2;
                }

                break;
            case 0x6000: //LD
                registers[(opcode & 0x0F00) >> 8] = (byte) (opcode & 0x00FF);

                break;
            case 0x7000: //ADD
                registers[(opcode & 0x0F00) >> 8] += (byte) (opcode & 0x00FF);

                break;
            case 0x8000: //LD, OR, AND, XOR, ADD, SUB, SHR, SUBN, SHL
                switch(opcode & 0x000F) {
                    case 0x0000 -> { //LD
                        registers[(opcode & 0x0F00) >> 8] = registers[(opcode & 0x00F0) >> 4];
                    }
                    case 0x0001 -> { //OR
                        registers[(opcode & 0x0F00) >> 8] |= registers[(opcode & 0x00F0) >> 4];

                    }
                    case 0x0002 -> { //AND
                        registers[(opcode & 0x0F00) >> 8] &= registers[(opcode & 0x00F0) >> 4];
                    }
                    case 0x0003 -> { //XOR
                        registers[(opcode & 0x0F00) >> 8] ^= registers[(opcode & 0x00F0) >> 4];

                    }
                    case 0x0004 -> { //ADD with carry flag
                        byte a = registers[(opcode & 0x0F00) >> 8];
                        byte b = registers[(opcode & 0x00F0) >> 4];

                        if (a + b > 127) {
                            registers[0xF] = 1;
                        } else if (a + b < -128) {
                            registers[0xF] = 1;
                        } else {
                            registers[0xF] = 0;
                        }

                        registers[(opcode & 0x0F00) >> 8] = (byte) ((a + b) & 0xFF);


                    }
                    case 0x0005-> { //SUB
                        byte a = registers[(opcode & 0x0F00) >> 8];
                        byte b = registers[(opcode & 0x00F0) >> 4];
                        if (a > b) {
                            registers[0xF] = 1;
                        } else {
                            registers[0xF] = 0;
                        }
                        registers[(opcode & 0x0F00) >> 8] = (byte) ((a-b));

                    }
                    case 0x0006 -> { //SHR
                        if ((registers[(opcode & 0x0F00) >> 8] & 0x0001) == 1) {
                            registers[0xF] = 1;
                        } else {
                            registers[0xF] = 0;
                        }
                        registers[(opcode & 0x0F00) >> 8] /= 2;

                    }
                    case 0x0007 -> { //SUBN
                        if (registers[(opcode & 0x00F0) >> 4] > registers[(opcode & 0x0F00) >> 8]) {
                            registers[0xF] = 1;
                        } else {
                            registers[0xF] = 0;
                        }
                        registers[(opcode & 0x0F00) >> 8] = (byte) (registers[(opcode & 0x00F0) >> 4] - registers[(opcode & 0x0F00) >> 8]);

                    }
                    case 0x000E -> {
                        if (((registers[(opcode & 0x0F00) >> 8] & 0x80) >> 7) == 1) {
                            registers[0xF] = 1;
                        } else {
                            registers[0xF] = 0;
                        }
                        registers[(opcode & 0x0F00) >> 8] *= 2;

                    }
                }
                break;
            case 0x9000: //SNE
                if(registers[(opcode & 0x0F00) >> 8] != registers[(opcode & 0x00F0) >> 4]) {
                    pc+=2;
                }

                break;
            case 0xA000: //LD
                indexRegister = (opcode & 0x0FFF);

                break;
            case 0xB000: //JP
                pc = (opcode & 0x0FFF) + registers[0];
                break;
            case 0xC000: //RND
                byte randomNumber = (byte) new java.util.Random().nextInt(255);
                registers[(opcode & 0x0F00) >> 8] = (byte) (randomNumber & (opcode & 0x00FF));

                break;
            case 0xD000: //DRW
                byte nibble = (byte) (opcode & 0x000F);
                byte vf = 0x00;
                if(indexRegister < 80) {
                    System.out.println("Fontset is used");
                }
                for(int i = 0; i < nibble; i++) {
                    byte readByte = (byte) (memory[indexRegister + i] & 0xFF);

                    for(int j = 0; j < 8; j++) {
                        int x = ((registers[(opcode & 0x0F00) >> 8] + j) % 64) & 0xFF;
                        int y = ((registers[(opcode & 0x00F0) >> 4] + i) % 32) & 0xFF;

                        boolean prevPixel = display[x + displayHandler.screenWidth * y] > 0;
                        boolean pixel = prevPixel ^ ((readByte & (1 << 7-j)) != 0);
                        System.out.println("Pixels: " + prevPixel + " " + pixel);
                        display[x + displayHandler.screenWidth * y] = pixel ? 1 : 0;

                        if(prevPixel == true && pixel == false) {
                            vf = 0x01;
                        }
                    }

                }
                registers[0xF] = vf;
                drawFlag = 1;

                break;
            case 0xE000: //SKP, SKNP
                switch(opcode & 0x00FF) {
                    case 0x009E -> { //SKP
                        byte key = (byte) (registers[opcode & 0xF00 >> 8] & 0xF);
                        if(inputHandler.pressedKeys[key]) {
                            pc+=2;
                        }

                    }
                    case 0x00A1 -> { //SKNP
                        byte key = (byte) (registers[opcode & 0xF00 >> 8] & 0xF);
                        if(!inputHandler.pressedKeys[key]) {
                            pc+=2;
                        }

                    }
                }
                break;
            case 0xF000: //LD, ADD
                switch(opcode & 0x00FF) {
                    case 0x0007:
                        registers[(opcode & 0x0F00) >> 8] = (byte) (delayTimer & 0xFF);

                        break;
                    case 0x000A:
                        registers[(opcode & 0x0F00) >> 8] = (byte) (inputHandler.awaitInput() & 0xFF);

                        break;
                    case 0x0015:
                        delayTimer = registers[(opcode & 0x0F00) >> 8];

                        break;
                    case 0x0018:
                        soundTimer = registers[(opcode & 0x0F00) >> 8];

                        break;
                    case 0x001E:
                        //maybe cut short at 12 bits idk
                        indexRegister = (indexRegister + registers[(opcode & 0x0F00) >> 8]) & 0xFF;

                        break;
                    case 0x0029:
                        indexRegister = memory[registers[(opcode & 0x0F00) >> 8] * 5];

                        break;
                    case 0x0033:
                        byte hundredsDigit, tenthDigit, onesDigit;
                        byte value = registers[(opcode & 0x0F00) >> 8];
                        hundredsDigit = (byte) (((value % 1000) / 100) & 0xFF);
                        tenthDigit = (byte) (((value % 100) / 10) & 0xFF);
                        onesDigit = (byte) ((value % 10) & 0xFF);

                        memory[indexRegister] = hundredsDigit;
                        memory[indexRegister + 1] = tenthDigit;
                        memory[indexRegister + 2] = onesDigit;

                        break;
                    case 0x0055:
                        for(int i = 0; i < ((opcode & 0x0F00) >> 8); i++) {
                            memory[indexRegister+i] = registers[i];
                        }

                        break;
                    case 0x0065:
                        for(int i = 0; i < ((opcode & 0x0F00) >> 8); i++) {
                            registers[i] = (byte) (memory[indexRegister+i] & 0xFF);
                        }

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

    public void emulationLoop() {

        int refresh = 0;

        long start = 0;
        long end = 0;


        while(true) {

            start = System.nanoTime();
            fetchOpcode();
            decodeAndExecuteOpcode();

            if(refresh % cyclesNeededForRefreshing == 0) {
                refresh = 0;
                if(drawFlag == 1) {
                    System.out.println("draw flag was 1");
                    displayHandler.repaint();
                    drawFlag = 0;
                }
                if(delayTimer > 0) {
                    delayTimer--;
                }
                //TODO sound

            }

            end = System.nanoTime();
            refresh++;
            sleepForAccuracy(start, end);

        }
    }

    private void sleepForAccuracy(long start, long end) {
        long duration = cycleNanoSeconds - (end - start);
        long sleepStart = System.nanoTime();
        long sleepDuration = sleepStart + duration;

        while(System.nanoTime() < sleepDuration) {
            try {
                Thread.sleep(0);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeGUI() {
        JFrame frame = new JFrame("chipsekiz");
        frame.add(displayHandler);
        displayHandler.setVisible(true);
        displayHandler.revalidate();



        //frame.add(createMenuBar(), BorderLayout.NORTH);


        frame.pack();
        frame.setSize(640,480);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        this.rom = new File("C:\\Users\\Uyilm\\IdeaProjects\\chipsekiz\\roms\\test_opcode.ch8");
        startEmulation();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("File");

        JMenuItem open = new JMenuItem("Open...");
        JMenuItem exit = new JMenuItem("Exit");

        open.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

            fileChooser.setFileFilter(new FileNameExtensionFilter("Chip-8 ROMs", "ch8"));

            fileChooser.setVisible(true);
            final int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                rom = fileChooser.getSelectedFile();
                startEmulation();
            }
        });

        exit.addActionListener(e -> System.exit(0));

        file.add(open);
        file.add(exit);


        file.add(file);
        menuBar.add(file);


        return menuBar;
    }
    private void startEmulation() {
        if(rom.exists()) {
            loadProgram(rom);
            dumpArrayToFile(memory);
            emulationLoop();
        }
        else {
            Emulator.cpuLogger.severe("ROM does not exist!");
        }
    }

}
