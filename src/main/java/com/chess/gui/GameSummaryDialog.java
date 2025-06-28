package com.chess.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Simple dialog shown when a chess game ends
 */
public class GameSummaryDialog extends JDialog {
    
    public GameSummaryDialog(JFrame parent, String gameResult) {
        super(parent, "Game Over", true);
        
        setLayout(new BorderLayout(10, 10));
        setSize(400, 200);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Game result label
        JLabel resultLabel = new JLabel(gameResult);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultLabel.setForeground(getResultColor(gameResult));
        
        // Add components to main panel
        mainPanel.add(resultLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JButton newGameButton = new JButton("New Game");
        JButton closeButton = new JButton("Close");
        
        newGameButton.addActionListener(e -> dispose());
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(newGameButton);
        buttonPanel.add(closeButton);
        
        // Add panels to dialog
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set focus to new game button
        getRootPane().setDefaultButton(newGameButton);
    }
    
    private Color getResultColor(String gameResult) {
        if (gameResult.contains("CHECKMATE")) {
            return new Color(0, 100, 0); // Dark green
        } else if (gameResult.contains("STALEMATE") || gameResult.contains("DRAW")) {
            return new Color(100, 100, 100); // Gray
        } else {
            return Color.BLACK;
        }
    }
    
    /**
     * Show the game summary dialog
     */
    public static void showGameSummary(JFrame parent, String gameResult) {
        GameSummaryDialog dialog = new GameSummaryDialog(parent, gameResult);
        dialog.setVisible(true);
    }
} 