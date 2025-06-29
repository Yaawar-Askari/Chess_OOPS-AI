package com.chess.debug;

import com.chess.gui.*;
import com.chess.model.*;
import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Test to verify that drag operations are not interrupted by AI moves
 */
public class DragInterruptionTest {
    public static void main(String[] args) {
        System.out.println("=== Drag Interruption Test ===");
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Create GUI for AI game
                ChessGUI gui = new ChessGUI();
                gui.startAIGame();
                
                Board board = new Board();
                ChessBoardPanel boardPanel = new ChessBoardPanel(board, gui);
                
                // Test 1: Start a drag operation
                System.out.println("Testing drag operation stability...");
                
                // Simulate starting a drag (commenting out unused variable)
                // Position dragStart = new Position(6, 4); // e2 pawn
                MouseEvent pressEvent = new MouseEvent(boardPanel, MouseEvent.MOUSE_PRESSED,
                        System.currentTimeMillis(), 0, 250, 400, 1, false);
                boardPanel.dispatchEvent(pressEvent);
                
                // Check if drag is in progress
                if (boardPanel.isDragInProgress()) {
                    System.out.println("✓ Drag operation started successfully");
                    
                    // Test 2: Try to update board while dragging
                    System.out.println("Testing board update during drag...");
                    boardPanel.updateBoard();
                    
                    // Verify drag is still in progress
                    if (boardPanel.isDragInProgress()) {
                        System.out.println("✓ Drag operation preserved during board update");
                    } else {
                        System.out.println("✗ Drag operation was interrupted by board update");
                    }
                    
                    // Test 3: Try to set new board while dragging
                    System.out.println("Testing board replacement during drag...");
                    Board newBoard = new Board();
                    boardPanel.setBoard(newBoard);
                    
                    // Verify drag is still in progress
                    if (boardPanel.isDragInProgress()) {
                        System.out.println("✓ Drag operation preserved during board replacement");
                    } else {
                        System.out.println("✗ Drag operation was interrupted by board replacement");
                    }
                    
                    // Complete the drag
                    MouseEvent releaseEvent = new MouseEvent(boardPanel, MouseEvent.MOUSE_RELEASED,
                            System.currentTimeMillis(), 0, 300, 350, 1, false);
                    boardPanel.dispatchEvent(releaseEvent);
                    
                    System.out.println("✓ Drag operation completed successfully");
                } else {
                    System.out.println("✗ Failed to start drag operation");
                }
                
                System.out.println("Drag interruption test completed!");
                
            } catch (Exception e) {
                System.err.println("Drag interruption test failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
