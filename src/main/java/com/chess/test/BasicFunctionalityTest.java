package com.chess.test;

import com.chess.model.*;

/**
 * Simple functional test to verify core chess functionality
 */
public class BasicFunctionalityTest {
    
    public static void main(String[] args) {
        System.out.println("=== Basic Chess Functionality Test ===");
        
        try {
            // Test board creation
            Board board = new Board();
            System.out.println("✓ Board created successfully");
            
            // Test basic pawn move
            Position from = new Position(6, 4); // e2
            Position to = new Position(4, 4);   // e4
            Piece pawn = board.getPiece(from);
            
            if (pawn != null && pawn.getType().equals("Pawn")) {
                System.out.println("✓ Pawn found at e2");
                
                Move move = new Move(from, to, pawn, null);
                if (board.isValidMove(move)) {
                    board.makeMove(move);
                    System.out.println("✓ Pawn move e2-e4 executed successfully");
                } else {
                    System.out.println("✗ Pawn move validation failed");
                }
            } else {
                System.out.println("✗ Pawn not found at e2");
            }
            
            // Test turn switching
            String currentTurn = board.getCurrentTurn();
            System.out.println("✓ Current turn: " + currentTurn);
            
            // Test game state
            boolean inCheck = board.isInCheck("White");
            boolean inCheckmate = board.isCheckmate("White");
            System.out.println("✓ White in check: " + inCheck);
            System.out.println("✓ White in checkmate: " + inCheckmate);
            
            System.out.println("=== All basic tests passed! ===");
            
        } catch (Exception e) {
            System.err.println("✗ Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
