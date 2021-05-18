package io.uymaz.chip8;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Emulator {

    static CPU cpu;

    private static final String USERDIR = System.getProperty("user.dir");

    static File rom;
    static Logger cpuLogger;
    static Logger memoryLogger;

    public static void main(String[] args){
        cpuLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        memoryLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        cpuLogger.setLevel(Level.ALL);
        memoryLogger.setLevel(Level.ALL);

        try {
            cpuLogger.addHandler(new FileHandler(USERDIR + "/logs/cpu.log"));
            memoryLogger.addHandler(new FileHandler(USERDIR + "/logs/memory.log"));
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        cpu = new CPU();

        initializeGUI();

    }

    private static void initializeGUI() {
        JFrame frame = new JFrame("chipsekiz");
        JPanel panel = new JPanel();
        frame.add(panel);




        frame.add(createMenuBar(), BorderLayout.NORTH);

        frame.pack();
        frame.setSize(640,480);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static JMenuBar createMenuBar() {
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

    private static void startEmulation() {
        if(rom.exists()) {
            cpu.loadProgram(rom);
            cpu.dumpArrayToFile(cpu.memory);
        }
        else {
            cpuLogger.severe("ROM does not exist!");
        }
    }

}
