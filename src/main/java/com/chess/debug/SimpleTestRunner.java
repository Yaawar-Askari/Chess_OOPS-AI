package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class SimpleTestRunner {
    
    public static void main(String[] args) {
        System.out.println("=== Simple Test Runner ===");
        
        // Test 1: Castling functionality
        testCastling();
        
        // Test 2: Checkmate detection
        testCheckmate();
        
        // Test 3: Stalemate detection
        testStalemate();
        
        System.out.println("\n=== All tests completed ===");
    }
    
    private static void testCastling() {
        System.out.println("\n=== Testing Castling ===");
        Board board = new Board();
        
        // Clear path for castling
        board.setPiece(new Position(7, 1), null); // Remove knight
        board.setPiece(new Position(7, 2), null); // Remove bishop
        board.setPiece(new Position(7, 3), null); // Remove queen
        board.setPiece(new Position(7, 5), null); // Remove bishop
        board.setPiece(new Position(7, 6), null); // Remove knight
        
        // Test kingside castling with proper move type
        Move kingsideCastle = new Move(new Position(7, 4), new Position(7, 6), 
                                     board.getPiece(new Position(7, 4)), null, 
                                     Move.MoveType.CASTLE_KINGSIDE, null, false, false);
        boolean kingsideValid = board.isValidMove(kingsideCastle);
        System.out.println("Kingside castling valid: " + kingsideValid + " (expected: true)");
        
        // Test GUI-style move creation (NORMAL type, should still work due to king moving 2 squares)
        Move guiStyleCastle = new Move(new Position(7, 4), new Position(7, 6), 
                                     board.getPiece(new Position(7, 4)), null);
        boolean guiStyleValid = board.isValidMove(guiStyleCastle);
        System.out.println("GUI-style castling valid: " + guiStyleValid + " (expected: true)");
    }
    
    private static void testCheckmate() {
        System.out.println("\n=== Testing Checkmate ===");
        Board board = new Board();
        
        // Clear the board first
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board.setPiece(new Position(row, col), null);
            }
        }
        
        // Set up back-rank checkmate position
        // White king trapped on back rank
        board.setPiece(new Position(7, 0), new King("White", new Position(7, 0))); // a1
        board.setPiece(new Position(6, 0), new Pawn("White", new Position(6, 0))); // a2 - blocks escape
        board.setPiece(new Position(6, 1), new Pawn("White", new Position(6, 1))); // b2 - blocks escape
        
        // Black rook delivering checkmate from the first rank
        board.setPiece(new Position(7, 7), new Rook("Black", new Position(7, 7))); // h1 - attacking along rank
        
        board.setCurrentTurn("White");
        
        boolean isInCheck = board.isInCheck("White");
        boolean isCheckmate = board.isCheckmate("White");
        String gameResult = board.checkGameEndingConditions();
        
        System.out.println("White in check: " + isInCheck + " (expected: true)");
        System.out.println("White in checkmate: " + isCheckmate + " (expected: true)");
        System.out.println("Game result: " + gameResult + " (expected: CHECKMATE_BLACK)");
    }
    
    private static void testStalemate() {
        System.out.println("\n=== Testing Stalemate ===");
        Board board = new Board();
        
        // Clear the board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board.setPiece(new Position(row, col), null);
            }
        }
        
        // Set up stalemate position
        board.setPiece(new Position(7, 0), new King("White", new Position(7, 0))); // White king in corner
        board.setPiece(new Position(5, 1), new Queen("Black", new Position(5, 1))); // Black queen controlling escape squares
        board.setPiece(new Position(6, 2), new King("Black", new Position(6, 2))); // Black king
        
        board.setCurrentTurn("White");
        
        boolean isInCheck = board.isInCheck("White");
        boolean isStalemate = board.isStalemate("White");
        String gameResult = board.checkGameEndingConditions();
        
        System.out.println("White in check: " + isInCheck + " (expected: false)");
        System.out.println("White in stalemate: " + isStalemate + " (expected: true)");
        System.out.println("Game result: " + gameResult + " (expected: STALEMATE)");
    }
}
