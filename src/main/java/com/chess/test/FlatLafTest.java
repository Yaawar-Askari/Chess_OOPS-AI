package com.chess.test;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Simple test class to verify FlatLaf is working
 */
public class FlatLafTest {
    public static void main(String[] args) {
        try {
            // Apply FlatLaf theme
            UIManager.setLookAndFeel(new FlatLightLaf());
            System.out.println("FlatLaf applied successfully!");
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to apply FlatLaf: " + e.getMessage());
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FlatLaf Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);
            
            JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            panel.add(new JLabel("FlatLaf Test - Modern UI", SwingConstants.CENTER));
            panel.add(new JButton("Test Button"));
            panel.add(new JTextField("Test Text Field"));
            
            JComboBox<String> combo = new JComboBox<>(new String[]{"Option 1", "Option 2", "Option 3"});
            panel.add(combo);
            
            frame.add(panel);
            frame.setVisible(true);
            
            System.out.println("FlatLaf test window created successfully!");
        });
    }
}
