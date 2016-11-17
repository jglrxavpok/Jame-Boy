package org.jglrxavpok.jameboy.debug;

import org.jglrxavpok.jameboy.JameBoy;
import org.jglrxavpok.jameboy.JameBoyApp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;
import java.util.Arrays;

// TODO: Create hook between memory controllers and this class
public class MemoryViewFrame extends JFrame {

    private static final MemoryViewFrame instance = new MemoryViewFrame();
    private DefaultTableModel model;
    private static final int byteCount = 4;

    private MemoryViewFrame() {
        super("Jame-Boy Memory View");
        buildFrame();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildFrame() {
        model = new DefaultTableModel((0xFFFF+1)/byteCount, byteCount+1);
        JTable table = new JTable(model);
        String[] header = new String[byteCount*4+1];
        header[0] = "Base address";
        for (int i = 1; i < header.length; i++) {
            header[i] = String.format("%01X", (i-1) & 0xF);
        }
        model.setColumnIdentifiers(header);
        model.fireTableDataChanged();
        getContentPane().add(new JScrollPane(table));
    }

    public static MemoryViewFrame getInstance() {
        return instance;
    }

    public void onUpdate() {
        JameBoy core = JameBoyApp.emulator.getCore();
        if(core.getCurrentROM() != null) {
            int row = 0;
            for (int i = 0; i < (0xFFFF+1)/byteCount; i+=byteCount) {
                model.setValueAt(String.format("%04X",i*byteCount), row, 0);
                for (int byteIndex = 0; byteIndex < byteCount*4; byteIndex++) {
                    byte value = (byte) 0xFF;
                    try {
                        value = core.getMemoryController().read(i*byteCount+byteIndex);
                    } catch (Exception e) {
                        // shhh
                    }
                    model.setValueAt(String.format("%02X",value & 0xFF).toUpperCase(), row, byteIndex+1);
                }
                row++;
            }
            model.fireTableDataChanged();
        }
    }
}
