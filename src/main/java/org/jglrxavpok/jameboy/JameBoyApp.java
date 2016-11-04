package org.jglrxavpok.jameboy;

import org.jglrxavpok.jameboy.graphics.GPU;
import org.jglrxavpok.jameboy.graphics.Screen;
import org.jglrxavpok.jameboy.input.Keyboard;
import org.jglrxavpok.jameboy.input.Mouse;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class JameBoyApp {

    public static JameBoyApp emulator;
    public static JFrame mainFrame;
    public static int scale;
    public static Screen screen;
    public static EmulatorThread emulatorThread;
    private static JFileChooser chooser;
    private GBMemory memory;
    private boolean romLoaded;
    private CPU cpu;
    private GPU gpu;

    public JameBoyApp() {
        memory = new GBMemory();
        cpu = new CPU();
        cpu.setEmulator(this);
        gpu = new GPU();
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
        mainFrame = new JFrame();
        scale = 6;
        mainFrame.setSize(160 * scale, 144 * scale);
        screen = new Screen(160, 144);
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
                    byte[] rom = read(in);
                    emulator.load(rom);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        fileMenu.add(open);
        bar.add(fileMenu);
        mainFrame.setMenuBar(bar);
        mainFrame.setVisible(true);
        emulator = new JameBoyApp();
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

    public GBMemory getMemory() {
        return memory;
    }

    public CPU getCPU() {
        return cpu;
    }

    public GPU getGPU() {
        return gpu;
    }

    protected void load(byte[] rom) {
        romLoaded = false;
        memory.loadROM(rom);
        romLoaded = true;
    }

    public boolean hasRomLoaded() {
        return romLoaded;
    }

    public void doCycle() {
        cpu.doCycle();
    }


}
