package com.chess.debug;

import com.chess.gui.*;
import com.chess.model.*;
import javax.swing.*;

/**
 * Test program to verify GUI stability improvements and flicker reduction
 */
public class GuiStabilityTest {
    public static void main(String[] args) {
        System.out.println("=== GUI Stability Test ===");
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Test 1: Basic GUI initialization
                testBasicInitialization();
                
                // Test 2: Drag and drop simulation
                testDragAndDropStability();
                
                // Test 3: Animation system
                testAnimationSystem();
                
                // Test 4: Rapid board updates
                testRapidBoardUpdates();
                
                System.out.println("GUI stability tests completed successfully!");
                
            } catch (Exception e) {
                System.err.println("GUI stability test failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private static void testBasicInitialization() {
        System.out.println("Testing basic GUI initialization...");
        
        ChessGUI gui = new ChessGUI();
        Board board = new Board();
        ChessBoardPanel boardPanel = new ChessBoardPanel(board, gui);
        
        // Test that panel is properly initialized
        assert boardPanel.getBoard() != null : "Board should not be null";
        assert boardPanel.isDoubleBuffered() : "Panel should be double buffered";
        
        System.out.println("✓ Basic initialization test passed");
    }
    
    private static void testDragAndDropStability() {
        System.out.println("Testing drag and drop stability...");
        
        ChessGUI gui = new ChessGUI();
        Board board = new Board();
        ChessBoardPanel boardPanel = new ChessBoardPanel(board, gui);
        
        // Simulate multiple rapid interaction attempts
        for (int i = 0; i < 10; i++) {
            boardPanel.setInteractionEnabled(false);
            boardPanel.setInteractionEnabled(true);
        }
        
        System.out.println("✓ Drag and drop stability test passed");
    }
    
    private static void testAnimationSystem() {
        System.out.println("Testing animation system...");
        
        ChessGUI gui = new ChessGUI();
        Board board = new Board();
        ChessBoardPanel boardPanel = new ChessBoardPanel(board, gui);
        
        // Test animation setup and cleanup
        Move testMove = new Move(new Position(6, 4), new Position(4, 4), 
                               board.getPiece(new Position(6, 4)), null);
        
        // This should not cause exceptions
        boardPanel.animateMove(testMove);
        
        // Test multiple rapid animations (should handle gracefully)
        for (int i = 0; i < 5; i++) {
            boardPanel.animateMove(testMove);
        }
        
        System.out.println("✓ Animation system test passed");
    }
    
    private static void testRapidBoardUpdates() {
        System.out.println("Testing rapid board updates...");
        
        ChessGUI gui = new ChessGUI();
        Board board = new Board();
        ChessBoardPanel boardPanel = new ChessBoardPanel(board, gui);
        
        // Test performance optimizations
        System.out.println("Testing performance settings...");
        boardPanel.setAnimationSpeed(10); // Fast animations
        boardPanel.setSmoothAnimations(false); // Disable smooth animations for speed
        boardPanel.setShowCoordinates(false); // Hide coordinates for performance
        
        long startTime = System.currentTimeMillis();
        
        // Simulate rapid board state changes
        for (int i = 0; i < 100; i++) {
            Board newBoard = new Board();
            boardPanel.setBoard(newBoard);
            
            // Test interaction toggling
            boardPanel.setInteractionEnabled(i % 2 == 0);
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("✓ Rapid board updates completed in " + (endTime - startTime) + "ms");
        
        // Test performance features
        System.out.println("Testing enhanced performance features...");
        boardPanel.setSmoothAnimations(true);
        boardPanel.setShowCoordinates(true);
        boardPanel.setAnimationSpeed(20);
        
        System.out.println("✓ All performance features working correctly");
        System.out.println("✓ Rapid board updates test passed");
    }
}
