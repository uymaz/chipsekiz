package io.uymaz.chip8;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Input {

    boolean[] pressedKeys;

    int nrOfPressedKeys = 0;
    int lastPressedKeyAddress;

    public Input() {
        pressedKeys = new boolean[16];
        addEventDispatcher();
    }

    int awaitInput() {
        while(nrOfPressedKeys == 0) {
            try {
                Thread.sleep(0);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        return lastPressedKeyAddress;
    }

    void addEventDispatcher() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                synchronized (Input.class) {
                    if(e.getID() == KeyEvent.KEY_PRESSED) {
                        if(toggleKey(true, e.getKeyCode()) == true) {
                            nrOfPressedKeys++;
                        }
                    }
                    else if(e.getID() == KeyEvent.KEY_RELEASED) {
                        if(toggleKey(false, e.getKeyCode()) == true) {
                            nrOfPressedKeys--;
                        }
                    }

                    return false;
                }
            }
        });
    }

    boolean toggleKey(boolean value, int key) {
        switch(key) {
            case KeyEvent.VK_1 -> {
                pressedKeys[0x0] = value;
                lastPressedKeyAddress = 0x0;
            }
            case KeyEvent.VK_2 -> {
                pressedKeys[0x1] = value;
                lastPressedKeyAddress = 0x1;
            }
            case KeyEvent.VK_3 -> {
                pressedKeys[0x2] = value;
                lastPressedKeyAddress = 0x2;
            }
            case KeyEvent.VK_4 -> {
                pressedKeys[0x3] = value;
                lastPressedKeyAddress = 0x3;
            }
            case KeyEvent.VK_Q -> {
                pressedKeys[0x4] = value;
                lastPressedKeyAddress = 0x4;
            }
            case KeyEvent.VK_W -> {
                pressedKeys[0x5] = value;
                lastPressedKeyAddress = 0x5;
            }
            case KeyEvent.VK_E -> {
                pressedKeys[0x6] = value;
                lastPressedKeyAddress = 0x6;
            }
            case KeyEvent.VK_R -> {
                pressedKeys[0x7] = value;
                lastPressedKeyAddress = 0x7;
            }
            case KeyEvent.VK_A -> {
                pressedKeys[0x8] = value;
                lastPressedKeyAddress = 0x8;
            }
            case KeyEvent.VK_S -> {
                pressedKeys[0x9] = value;
                lastPressedKeyAddress = 0x9;
            }
            case KeyEvent.VK_D -> {
                pressedKeys[0xA] = value;
                lastPressedKeyAddress = 0xA;
            }
            case KeyEvent.VK_F -> {
                pressedKeys[0xB] = value;
                lastPressedKeyAddress = 0xB;
            }
            case KeyEvent.VK_Z -> {
                pressedKeys[0xC] = value;
                lastPressedKeyAddress = 0xC;
            }
            case KeyEvent.VK_X -> {
                pressedKeys[0xD] = value;
                lastPressedKeyAddress = 0xD;
            }
            case KeyEvent.VK_C -> {
                pressedKeys[0xE] = value;
                lastPressedKeyAddress = 0xE;
            }
            case KeyEvent.VK_V -> {
                pressedKeys[0xF] = value;
                lastPressedKeyAddress = 0xF;
            }
            default -> {
                return false;
            }
        }
        return true;
    }

}
