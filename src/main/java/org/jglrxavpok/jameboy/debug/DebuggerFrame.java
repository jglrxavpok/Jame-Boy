package org.jglrxavpok.jameboy.debug;

import org.jglrxavpok.jameboy.JameBoy;
import org.jglrxavpok.jameboy.JameBoyApp;

import javax.swing.*;

public class DebuggerFrame extends JFrame {

    private static final DebuggerFrame instance = new DebuggerFrame();
    private JLabel opcodeLabel;

    private DebuggerFrame() {
        super("Jame-Boy Debugger");
        buildFrame();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildFrame() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JButton stepButton = new JButton("Step");
        stepButton.addActionListener(e -> JameBoyApp.emulator.getCore().requestStep());
        panel.add(stepButton);

        opcodeLabel = new JLabel("/");
        panel.add(opcodeLabel);

        add(panel);
    }

    public static DebuggerFrame getInstance() {
        return instance;
    }

    public void onUpdate() {
        JameBoy core = JameBoyApp.emulator.getCore();
        if(core.getCurrentROM() != null)
            opcodeLabel.setText(Integer.toHexString(core.getMemoryController().read(core.getCPU().getProgramCounter()) & 0xFF)
                    +" ("+Integer.toHexString(core.getCPU().getProgramCounter() & 0xFFFF)+")");
    }
}
