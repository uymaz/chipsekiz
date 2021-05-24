package io.uymaz.chip8;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Emulator implements Serializable {


    @Serial
    private static final long serialVersionUID = 9121477454979846379L;

    static CPU cpu;

    private static final String USERDIR = System.getProperty("user.dir");


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

    }





}
