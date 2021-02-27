import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Emulator {

    static CPU cpu;
    static Chip8 chip8;

    static File rom;
    static Logger logger;

    public static void main(String[] args) {
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(Level.ALL);

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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JButton play = new JButton();
        Icon playIcon = new ImageIcon(System.getProperty("user.dir") + "/icons/play.png");
        play.setIcon(playIcon);

        play.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Emulation started.");
                startEmulation();
            }
        });

        menuBar.add(play);


        JMenu file = new JMenu("File");

        JMenuItem open = new JMenuItem("Open...");
        JMenuItem exit = new JMenuItem("Exit");

        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

                fileChooser.setFileFilter(new FileNameExtensionFilter("Chip-8 ROMs", "ch8"));

                fileChooser.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
                                || e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                            final File f = (File) e.getNewValue();
                        }
                    }
                });

                fileChooser.setVisible(true);
                final int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    rom = fileChooser.getSelectedFile();
                }
            }
        });

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        file.add(open);
        file.add(exit);


        file.add(file);
        menuBar.add(file);


        return menuBar;
    }

    private static void startEmulation() {
        if(rom.exists()) {
            cpu.loadProgram(rom);
            logger.info("Program loaded");

            chip8.start();
        }
        else {
            logger.severe("ROM does not exist!");
        }
    }

}
