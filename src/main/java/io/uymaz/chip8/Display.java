package io.uymaz.chip8;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.io.Serializable;

public class Display extends JPanel implements Serializable {

    @Serial
    private static final long serialVersionUID = 868818163512903442L;

    transient Graphics graphics;
    CPU cpu;

    private int scaleFactor = 5;
    private int screenWidth = 64;
    private int screenHeight = 32;

    public Display(CPU cpu) {
        this.cpu = cpu;
    }

    void drawPixel(int x, int y, int value) {
        if(value == 1) {
            graphics.setColor(Color.WHITE);
        }
        else {
            graphics.setColor(Color.BLACK);
        }
        graphics.fillRect(x * scaleFactor, y * scaleFactor, scaleFactor, scaleFactor);
    }

    void drawDisplay() {
        for(int i = 0; i < 64; i++) {
            for(int j = 0; j < 32; j++) {
                int value = cpu.display[i*j];
                drawPixel(i, j, value);
            }
        }
    }

    void paintScreen() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        this.graphics = graphics;
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, screenWidth, screenHeight);
        drawDisplay();
    }

}
