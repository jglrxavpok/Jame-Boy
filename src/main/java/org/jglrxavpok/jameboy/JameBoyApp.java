package org.jglrxavpok.jameboy;

import org.jglrxavpok.jameboy.graphics.old.OldGPU;
import org.jglrxavpok.jameboy.graphics.old.Screen;
import org.jglrxavpok.jameboy.input.Keyboard;
import org.jglrxavpok.jameboy.input.Mouse;
import org.jglrxavpok.jameboy.memory.GameROM;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;

public class JameBoyApp {

    public static JameBoyApp emulator;
    public static JFrame mainFrame;
    public static int scale;
    public static Screen screen;
    public static EmulatorThread emulatorThread;
    private static JFileChooser chooser;
    private final JameBoy core;

    public JameBoyApp() {
        core = new JameBoy();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        chooser = new JFileChooser();
        loadEmulator();
    }

    private static void loadEmulator() {
        emulator = new JameBoyApp();
        mainFrame = new JFrame();
        scale = 6;
        mainFrame.setSize(160 * scale, 144 * scale);
        screen = new Screen(160, 144);
        emulator.getCore().getGPU().setBuffer(screen.pixels);
        mainFrame.setLocationRelativeTo(null);
        Mouse.init(mainFrame);
        Keyboard.init(mainFrame);
        MenuBar bar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem open = new MenuItem("Open ROM");
        open.addActionListener(evt -> {
            chooser.showOpenDialog(null);

            File f = chooser.getSelectedFile();
            if (f != null) {
                try {
                    FileInputStream in = new FileInputStream(f);
                    byte[] rawRom = read(in);
                    GameROM rom = new GameROM(ByteBuffer.wrap(rawRom));
                    emulator.core.loadROM(rom);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        fileMenu.add(open);
        bar.add(fileMenu);
        mainFrame.setMenuBar(bar);
        mainFrame.setVisible(true);
        emulatorThread = new EmulatorThread();
        emulatorThread.start();
    }

    public static byte[] read(InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;
        byte[] buffer = new byte[65565];
        while ((i = stream.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, i);
        }
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

    public boolean hasRomLoaded() {
        return core.getCurrentROM() != null;
    }

    public void doCycles(int count) {
        if(hasRomLoaded()) {
            for (int i = 0; i < count;) {
                i += core.cycle();
            }
        }
    }


    public JameBoy getCore() {
        return core;
    }
}
