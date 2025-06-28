package com.chess.debug;

import com.chess.model.Board;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.model.Piece;
import com.chess.model.pieces.*;

public class FinalTestRunner2 {
    
    public static void main(String[] args) {
        System.out.println("=== Final Test Runner ===");
        
        // Test 1: Castling functionality
        testCastling();
        
        // Test 2: Checkmate detection
        testCheckmate();
        
        // Test 3: Stalemate detection
        testStalemate();
        
        // Test 4: Game ending conditions
        testGameEndingConditions();
        
        // Test 5: Scholar's Mate specific test
        testScholarsMate();
        
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
        
        // Test kingside castling
        Move kingsideCastle = new Move(new Position(7, 4), new Position(7, 6), 
                                     board.getPiece(new Position(7, 4)), null, MoveType.CASTLE_KINGSIDE);
        boolean kingsideValid = board.isValidMove(kingsideCastle);
        System.out.println("Kingside castling valid: " + kingsideValid + " (expected: true)");
        
        // Test queenside castling
        Move queensideCastle = new Move(new Position(7, 4), new Position(7, 2), 
                                      board.getPiece(new Position(7, 4)), null, MoveType.CASTLE_QUEENSIDE);
        boolean queensideValid = board.isValidMove(queensideCastle);
        System.out.println("Queenside castling valid: " + queensideValid + " (expected: true)");
        
        // Test with GUI-style move creation (NORMAL type, should still work)
        Move guiStyleCastle = new Move(new Position(7, 4), new Position(7, 6), 
                                     board.getPiece(new Position(7, 4)), null);
        boolean guiStyleValid = board.isValidMove(guiStyleCastle);
        System.out.println("GUI-style castling valid: " + guiStyleValid + " (expected: true)");
    }
    
    private static void testCheckmate() {
        System.out.println("\n=== Testing Checkmate ===");
        Board board = new Board();
        
        // Set up back-rank checkmate position
        // Clear the board and set up specific position
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board.setPiece(new Position(row, col), null);
            }
        }
        
        // White king trapped on back rank
        board.setPiece(new Position(7, 0), new King(Color.WHITE));
        board.setPiece(new Position(6, 0), new Pawn(Color.WHITE));
        board.setPiece(new Position(6, 1), new Pawn(Color.WHITE));
        board.setPiece(new Position(6, 2), new Pawn(Color.WHITE));
        
        // Black rook delivering checkmate
        board.setPiece(new Position(0, 0), new Rook(Color.BLACK));
        
        board.setCurrentTurn(Color.WHITE);
        
        boolean isInCheck = board.isInCheck(Color.WHITE);
        boolean isCheckmate = board.isCheckmate(Color.WHITE);
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
        board.setPiece(new Position(7, 0), new King(Color.WHITE)); // White king in corner
        board.setPiece(new Position(5, 1), new Queen(Color.BLACK)); // Black queen controlling escape squares
        board.setPiece(new Position(6, 2), new King(Color.BLACK)); // Black king
        
        board.setCurrentTurn(Color.WHITE);
        
        boolean isInCheck = board.isInCheck(Color.WHITE);
        boolean isStalemate = board.isStalemate(Color.WHITE);
        String gameResult = board.checkGameEndingConditions();
        
        System.out.println("White in check: " + isInCheck + " (expected: false)");
        System.out.println("White in stalemate: " + isStalemate + " (expected: true)");
        System.out.println("Game result: " + gameResult + " (expected: STALEMATE)");
    }
    
    private static void testGameEndingConditions() {
        System.out.println("\n=== Testing Game Ending Conditions ===");
        Board board = new Board();
        
        // Set up checkmate for White (Black wins)
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board.setPiece(new Position(row, col), null);
            }
        }
        
        board.setPiece(new Position(7, 0), new King(Color.WHITE));
        board.setPiece(new Position(6, 0), new Pawn(Color.WHITE));
        board.setPiece(new Position(6, 1), new Pawn(Color.WHITE));
        board.setPiece(new Position(0, 0), new Rook(Color.BLACK));
        
        // Test with White to move (should be checkmate)
        board.setCurrentTurn(Color.WHITE);
        String resultWhiteToMove = board.checkGameEndingConditions();
        System.out.println("White to move result: " + resultWhiteToMove + " (expected: CHECKMATE_BLACK)");
        
        // Test with Black to move (should be null - game already over)
        board.setCurrentTurn(Color.BLACK);
        String resultBlackToMove = board.checkGameEndingConditions();
        System.out.println("Black to move result: " + resultBlackToMove + " (expected: null)");
    }
    
    private static void testScholarsMate() {
        System.out.println("\n=== Testing Scholar's Mate ===");
        Board board = new Board();
        
        // Play Scholar's Mate moves
        Move[] moves = {
            new Move(new Position(6, 4), new Position(4, 4), board.getPiece(new Position(6, 4)), null), // e4
            new Move(new Position(1, 4), new Position(3, 4), board.getPiece(new Position(1, 4)), null), // e5
            new Move(new Position(7, 5), new Position(4, 2), board.getPiece(new Position(7, 5)), null), // Bc4
            new Move(new Position(0, 1), new Position(2, 2), board.getPiece(new Position(0, 1)), null), // Nc6
            new Move(new Position(7, 3), new Position(3, 7), board.getPiece(new Position(7, 3)), null), // Qh5
            new Move(new Position(1, 3), new Position(2, 3), board.getPiece(new Position(1, 3)), null)  // d6
        };
        
        for (int i = 0; i < moves.length; i++) {
            System.out.println("Making move " + (i + 1) + ": " + moveToString(moves[i]));
            board.makeMove(moves[i]);
            
            String gameResult = board.checkGameEndingConditions();
            if (gameResult != null) {
                System.out.println("Game ended with result: " + gameResult);
                break;
            }
        }
        
        // Now play the checkmate move: Qxf7#
        Piece queen = board.getPiece(new Position(3, 7));
        if (queen != null) {
            Move checkmateMove = new Move(new Position(3, 7), new Position(1, 5), queen, 
                                        board.getPiece(new Position(1, 5)));
            System.out.println("Making checkmate move: Qxf7");
            board.makeMove(checkmateMove);
            
            String finalResult = board.checkGameEndingConditions();
            boolean blackInCheck = board.isInCheck(Color.BLACK);
            boolean blackInCheckmate = board.isCheckmate(Color.BLACK);
            
            System.out.println("Black in check: " + blackInCheck + " (expected: true)");
            System.out.println("Black in checkmate: " + blackInCheckmate + " (expected: true)");
            System.out.println("Final game result: " + finalResult + " (expected: CHECKMATE_WHITE)");
        }
    }
    
    private static String moveToString(Move move) {
        return positionToString(move.getFrom()) + " -> " + positionToString(move.getTo());
    }
    
    private static String positionToString(Position pos) {
        char file = (char)('a' + pos.getCol());
        int rank = 8 - pos.getRow();
        return "" + file + rank;
    }
}
