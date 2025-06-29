package com.chess.debug;

import com.chess.gui.ChessGUI;

import javax.swing.*;

public class DragDropTestWithAI {
    
    public static void main(String[] args) {
        System.out.println("=== Drag and Drop Test with AI ===");
        System.out.println("Instructions:");
        System.out.println("1. Start the AI game");
        System.out.println("2. Make a move by dragging a white piece");
        System.out.println("3. While the AI is thinking, try to drag another piece");
        System.out.println("4. The board should NOT reset during your drag operation");
        System.out.println("5. The AI move should be applied after you complete your drag");
        System.out.println();
        
        SwingUtilities.invokeLater(() -> {
            try {
                ChessGUI gui = new ChessGUI();
                gui.startGame(ChessGUI.GameMode.AI, null);
                gui.setVisible(true);
                gui.setTitle("Chess Game - Drag & Drop Test (vs AI)");
                
                System.out.println("AI game window opened successfully!");
                System.out.println("Test the drag and drop behavior during AI turns.");
                
            } catch (Exception e) {
                System.err.println("Error starting test: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
