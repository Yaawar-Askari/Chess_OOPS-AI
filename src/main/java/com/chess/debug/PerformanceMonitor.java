package com.chess.debug;

import com.chess.gui.*;
import com.chess.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Performance monitoring tool for GUI operations
 */
public class PerformanceMonitor {
    private static final int TEST_DURATION_MS = 5000; // 5 seconds per test
    private static long paintCallCount = 0;
    private static long repaintCallCount = 0;
    private static List<Long> paintTimes = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("=== GUI Performance Monitor ===");
        
        SwingUtilities.invokeLater(() -> {
            try {
                testPaintPerformance();
                testDragPerformance();
                testAnimationPerformance();
                
                System.out.println("\n=== Performance Summary ===");
                System.out.println("Total paint calls monitored: " + paintCallCount);
                System.out.println("Total repaint calls monitored: " + repaintCallCount);
                if (!paintTimes.isEmpty()) {
                    double avgPaintTime = paintTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
                    System.out.println("Average paint time: " + String.format("%.2f", avgPaintTime) + "ms");
                }
                
                System.out.println("Performance monitoring completed successfully!");
                
            } catch (Exception e) {
                System.err.println("Performance monitoring failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private static void testPaintPerformance() {
        System.out.println("\nTesting paint performance...");
        
        Board board = new Board();
        ChessGUI gui = new ChessGUI();
        PerformanceChessBoardPanel boardPanel = new PerformanceChessBoardPanel(board, gui);
        
        JFrame testFrame = new JFrame("Performance Test");
        testFrame.add(boardPanel);
        testFrame.setSize(500, 500);
        testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        testFrame.setVisible(true);
        
        paintCallCount = 0;
        paintTimes.clear();
        
        // Trigger repaints for specified duration
        Timer repaintTimer = new Timer(16, e -> boardPanel.repaint()); // ~60 FPS
        repaintTimer.start();
        
        // Stop after test duration
        Timer stopTimer = new Timer(TEST_DURATION_MS, e -> {
            repaintTimer.stop();
            testFrame.dispose();
            System.out.println("✓ Paint performance test completed");
            System.out.println("  Paint calls in " + TEST_DURATION_MS + "ms: " + paintCallCount);
            double fps = (paintCallCount * 1000.0) / TEST_DURATION_MS;
            System.out.println("  Effective FPS: " + String.format("%.1f", fps));
        });
        stopTimer.setRepeats(false);
        stopTimer.start();
        
        // Wait for test to complete
        try {
            Thread.sleep(TEST_DURATION_MS + 500);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void testDragPerformance() {
        System.out.println("\nTesting drag performance...");
        
        Board board = new Board();
        ChessGUI gui = new ChessGUI();
        PerformanceChessBoardPanel boardPanel = new PerformanceChessBoardPanel(board, gui);
        
        JFrame testFrame = new JFrame("Drag Performance Test");
        testFrame.add(boardPanel);
        testFrame.setSize(500, 500);
        testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        testFrame.setVisible(true);
        
        paintCallCount = 0;
        
        // Simulate drag operations
        Timer dragTimer = new Timer(50, new ActionListener() {
            private int x = 100, y = 100;
            private int dx = 2, dy = 3;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                // Simulate mouse drag
                x += dx;
                y += dy;
                if (x > 400 || x < 50) dx = -dx;
                if (y > 400 || y < 50) dy = -dy;
                
                MouseEvent dragEvent = new MouseEvent(boardPanel, MouseEvent.MOUSE_DRAGGED,
                        System.currentTimeMillis(), 0, x, y, 1, false);
                boardPanel.dispatchEvent(dragEvent);
            }
        });
        
        dragTimer.start();
        
        Timer stopTimer = new Timer(TEST_DURATION_MS / 2, e -> {
            dragTimer.stop();
            testFrame.dispose();
            System.out.println("✓ Drag performance test completed");
            System.out.println("  Paint calls during drag simulation: " + paintCallCount);
        });
        stopTimer.setRepeats(false);
        stopTimer.start();
        
        try {
            Thread.sleep(TEST_DURATION_MS / 2 + 500);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void testAnimationPerformance() {
        System.out.println("\nTesting animation performance...");
        
        Board board = new Board();
        ChessGUI gui = new ChessGUI();
        PerformanceChessBoardPanel boardPanel = new PerformanceChessBoardPanel(board, gui);
        
        JFrame testFrame = new JFrame("Animation Performance Test");
        testFrame.add(boardPanel);
        testFrame.setSize(500, 500);
        testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        testFrame.setVisible(true);
        
        paintCallCount = 0;
        
        // Test different animation settings
        System.out.println("  Testing smooth animations...");
        boardPanel.setSmoothAnimations(true);
        boardPanel.setAnimationSpeed(16); // 60 FPS
        
        // Trigger multiple animations
        Move testMove = new Move(new Position(6, 4), new Position(4, 4), 
                               board.getPiece(new Position(6, 4)), null);
        
        Timer animationTimer = new Timer(1000, e -> {
            if (boardPanel.getBoard().getPiece(new Position(6, 4)) != null) {
                boardPanel.animateMove(testMove);
            }
        });
        animationTimer.start();
        
        Timer stopTimer = new Timer(TEST_DURATION_MS / 2, e -> {
            animationTimer.stop();
            testFrame.dispose();
            System.out.println("✓ Animation performance test completed");
            System.out.println("  Paint calls during animations: " + paintCallCount);
        });
        stopTimer.setRepeats(false);
        stopTimer.start();
        
        try {
            Thread.sleep(TEST_DURATION_MS / 2 + 500);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Extended ChessBoardPanel that monitors performance metrics
     */
    static class PerformanceChessBoardPanel extends ChessBoardPanel {
        public PerformanceChessBoardPanel(Board board, ChessGUI parent) {
            super(board, parent);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            long startTime = System.nanoTime();
            paintCallCount++;
            super.paintComponent(g);
            long endTime = System.nanoTime();
            paintTimes.add((endTime - startTime) / 1_000_000); // Convert to milliseconds
        }
        
        @Override
        public void repaint() {
            repaintCallCount++;
            super.repaint();
        }
        
        @Override
        public void repaint(int x, int y, int width, int height) {
            repaintCallCount++;
            super.repaint(x, y, width, height);
        }
    }
}
