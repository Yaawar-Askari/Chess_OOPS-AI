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
    private JLabel whiteCapturedLabel;
    private JLabel blackCapturedLabel;
    private JButton hintButton;
    private JLabel hintLabel;

    public GameStatusPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Game Status");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel);
        add(Box.createVerticalStrut(20));

        turnLabel = new JLabel("Turn: ");
        movesLabel = new JLabel("Moves: 0");
        whiteCapturedLabel = new JLabel("Captured by White: ");
        blackCapturedLabel = new JLabel("Captured by Black: ");
        
        add(turnLabel);
        add(movesLabel);
        add(Box.createVerticalStrut(20));
        add(whiteCapturedLabel);
        add(blackCapturedLabel);

        add(Box.createVerticalStrut(20));

        hintButton = new JButton("Get Hint");
        add(hintButton);

        hintLabel = new JLabel("Hints remaining: 3");
        add(hintLabel);
    }
    
    public void setStatus(String status) {
        turnLabel.setText("Turn: " + status);
    }
    
    public void updateStatus(String status) {
        turnLabel.setText("Turn: " + status);
    }
    
    public void setMoveCount(int count) {
        movesLabel.setText("Moves: " + count);
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
        hintLabel.setText("Hints remaining: " + count);
    }
} 