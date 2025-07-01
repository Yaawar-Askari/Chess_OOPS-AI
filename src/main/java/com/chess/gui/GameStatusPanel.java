package com.chess.gui;

import com.chess.model.Piece;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel that displays game status information
 */
public class GameStatusPanel extends JPanel {
    private JLabel turnLabel;
    private JLabel movesLabel;
    private JLabel lastMoveLabel;
    private JLabel whiteCapturedLabel;
    private JLabel blackCapturedLabel;
    private JButton hintButton;
    private JLabel hintLabel;

    public GameStatusPanel() {
        this(null);
    }
    
    public GameStatusPanel(String gameMode) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Game Status"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Create components with better styling
        turnLabel = new JLabel("Turn: ");
        turnLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        movesLabel = new JLabel("Moves: 0");
        movesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        lastMoveLabel = new JLabel("Last Move: None");
        lastMoveLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        whiteCapturedLabel = new JLabel("Captured by White: ");
        whiteCapturedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        blackCapturedLabel = new JLabel("Captured by Black: ");
        blackCapturedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Add components with better spacing
        add(turnLabel);
        add(Box.createVerticalStrut(8));
        add(movesLabel);
        add(Box.createVerticalStrut(5));
        add(lastMoveLabel);
        add(Box.createVerticalStrut(15));
        
        // Captured pieces section
        JLabel capturedTitle = new JLabel("Captured Pieces:");
        capturedTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        add(capturedTitle);
        add(Box.createVerticalStrut(5));
        add(whiteCapturedLabel);
        add(Box.createVerticalStrut(3));
        add(blackCapturedLabel);

        // Only show hint button for AI mode
        if ("AI".equals(gameMode)) {
            add(Box.createVerticalStrut(20));
            
            hintButton = new JButton("💡 Get Hint");
            hintButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            hintButton.setPreferredSize(new Dimension(120, 30));
            hintButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(hintButton);
            
            add(Box.createVerticalStrut(5));
            hintLabel = new JLabel("Hints remaining: 3");
            hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            add(hintLabel);
        }
        
        // Add some bottom padding
        add(Box.createVerticalGlue());
    }
      public void setStatus(String status) {
        // Enhanced status display with color coding
        turnLabel.setText("<html><b>Status: " + status + "</b></html>");
        
        // Color code based on status
        if (status.contains("Check")) {
            turnLabel.setForeground(new Color(200, 0, 0)); // Red for check
        } else if (status.contains("Checkmate")) {
            turnLabel.setForeground(new Color(150, 0, 0)); // Dark red for checkmate
        } else if (status.contains("Stalemate")) {
            turnLabel.setForeground(new Color(100, 100, 100)); // Gray for stalemate
        } else {
            turnLabel.setForeground(Color.BLACK); // Normal color
        }
    }
    
    public void updateStatus(String status) {
        setStatus(status);
    }
    
    public void setMoveCount(int count) {
        movesLabel.setText("Moves: " + count);
    }
    
    public void setLastMove(String moveNotation) {
        lastMoveLabel.setText("Last Move: " + moveNotation);
    }
    
    public void updateCapturedPieces(List<Piece> whiteCaptured, List<Piece> blackCaptured) {
        StringBuilder whiteSb = new StringBuilder("<html>Captured by White:<br>");
        for (Piece p : whiteCaptured) {
            whiteSb.append(p.getSymbol()).append(" ");
        }
        whiteSb.append("</html>");
        whiteCapturedLabel.setText(whiteSb.toString());

        StringBuilder blackSb = new StringBuilder("<html>Captured by Black:<br>");
        for (Piece p : blackCaptured) {
            blackSb.append(p.getSymbol()).append(" ");
        }
        blackSb.append("</html>");
        blackCapturedLabel.setText(blackSb.toString());
    }

    public JButton getHintButton() {
        return hintButton;
    }

    public void setHintsRemaining(int count) {
        if (hintLabel != null) {
            hintLabel.setText("Hints remaining: " + count);
        }
    }
}