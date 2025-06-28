package com.chess.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.chess.model.pieces.*;

/**
 * Test suite for checkmate and stalemate detection edge cases
 */
public class CheckmateDetectionTest {
    
    private Board board;
    
    @BeforeEach
    void setUp() {
        board = new Board();
    }
    
    @Test
    @DisplayName("Test fool's mate")
    void testFoolsMate() {
        // Fastest possible checkmate in chess
        board = new Board();
        
        // 1. f3 e5 2. g4 Qh4#
        Move move1 = new Move(new Position(6, 5), new Position(5, 5), 
                             board.getPiece(new Position(6, 5)), null);
        board.makeMove(move1);
        
        Move move2 = new Move(new Position(1, 4), new Position(3, 4), 
                             board.getPiece(new Position(1, 4)), null);
        board.makeMove(move2);
        
        Move move3 = new Move(new Position(6, 6), new Position(4, 6), 
                             board.getPiece(new Position(6, 6)), null);
        board.makeMove(move3);
        
        Move move4 = new Move(new Position(0, 3), new Position(4, 7), 
                             board.getPiece(new Position(0, 3)), null);
        board.makeMove(move4);
        
        assertTrue(board.isInCheck("White"), "White king should be in check");
        assertTrue(board.isCheckmate("White"), "White should be in checkmate (fool's mate)");
    }
    
    @Test
    @DisplayName("Test scholar's mate")
    void testScholarsMate() {
        // Classic scholar's mate: 1.e4 e5 2.Bc4 Nc6 3.Qh5 d6?? 4.Qxf7#
        board = new Board();
        
        // 1. e4
        Move move1 = new Move(new Position(6, 4), new Position(4, 4), 
                             board.getPiece(new Position(6, 4)), null);
        board.makeMove(move1);
        
        // 1... e5
        Move move2 = new Move(new Position(1, 4), new Position(3, 4), 
                             board.getPiece(new Position(1, 4)), null);
        board.makeMove(move2);
        
        // 2. Bc4
        Move move3 = new Move(new Position(7, 5), new Position(4, 2), 
                             board.getPiece(new Position(7, 5)), null);
        board.makeMove(move3);
        
        // 2... Nc6
        Move move4 = new Move(new Position(0, 1), new Position(2, 2), 
                             board.getPiece(new Position(0, 1)), null);
        board.makeMove(move4);
        
        // 3. Qh5
        Move move5 = new Move(new Position(7, 3), new Position(3, 7), 
                             board.getPiece(new Position(7, 3)), null);
        board.makeMove(move5);
        
        // 3... d6 (weak move, not blocking)
        Move move6 = new Move(new Position(1, 3), new Position(2, 3), 
                             board.getPiece(new Position(1, 3)), null);
        board.makeMove(move6);
        
        // 4. Qxf7# - Queen captures f7 pawn for checkmate
        Piece queen = board.getPiece(new Position(3, 7));
        Piece f7Pawn = board.getPiece(new Position(1, 5));
        assertNotNull(queen, "Queen should be at h5");
        assertNotNull(f7Pawn, "Pawn should be at f7");
        
        Move checkmate = new Move(new Position(3, 7), new Position(1, 5), queen, f7Pawn);
        board.makeMove(checkmate);
        
        assertTrue(board.isInCheck("Black"), "Black king should be in check");
        assertTrue(board.isCheckmate("Black"), "Black should be in checkmate (scholar's mate)");
    }
    
    @Test
    @DisplayName("Test insufficient material draws")
    void testInsufficientMaterial() {
        // King vs King
        board.clear();
        board.setPiece(new Position(3, 3), new King("White", new Position(3, 3)));
        board.setPiece(new Position(5, 5), new King("Black", new Position(5, 5)));
        
        String result = board.checkGameEndingConditions();
        assertEquals("DRAW", result, "King vs King should be a draw");
        
        // King and Bishop vs King
        board.clear();
        board.setPiece(new Position(3, 3), new King("White", new Position(3, 3)));
        board.setPiece(new Position(3, 4), new Bishop("White", new Position(3, 4)));
        board.setPiece(new Position(5, 5), new King("Black", new Position(5, 5)));
        
        result = board.checkGameEndingConditions();
        assertEquals("DRAW", result, "King and Bishop vs King should be a draw");
        
        // King and Knight vs King
        board.clear();
        board.setPiece(new Position(3, 3), new King("White", new Position(3, 3)));
        board.setPiece(new Position(3, 4), new Knight("White", new Position(3, 4)));
        board.setPiece(new Position(5, 5), new King("Black", new Position(5, 5)));
        
        result = board.checkGameEndingConditions();
        assertEquals("DRAW", result, "King and Knight vs King should be a draw");
    }
    
    @Test
    @DisplayName("Test complex stalemate positions")
    void testComplexStalemate() {
        // Classic stalemate with queen
        board.clear();
        board.setPiece(new Position(7, 0), new King("White", new Position(7, 0))); // a1
        board.setPiece(new Position(5, 2), new King("Black", new Position(5, 2))); // c3
        board.setPiece(new Position(5, 1), new Queen("Black", new Position(5, 1))); // b3
        board.setCurrentTurn("Black");
        
        assertFalse(board.isInCheck("White"), "White king should not be in check");
        assertTrue(board.isStalemate("White"), "Should be stalemate");
        
        String result = board.checkGameEndingConditions();
        assertEquals("STALEMATE", result, "Should detect stalemate");
    }
    
    @Test
    @DisplayName("Test discovered check")
    void testDiscoveredCheck() {
        board.clear();
        
        // White king at e1, White rook at e2, Black rook at e8
        // When rook moves, it discovers check from the black rook
        board.setPiece(new Position(7, 4), new King("White", new Position(7, 4))); // e1
        board.setPiece(new Position(6, 4), new Rook("White", new Position(6, 4))); // e2
        board.setPiece(new Position(0, 4), new Rook("Black", new Position(0, 4))); // e8
        board.setCurrentTurn("White");
        
        Piece rook = board.getPiece(new Position(6, 4));
        
        // Rook cannot move away and expose king to check
        Move exposingMove = new Move(new Position(6, 4), new Position(6, 3), rook, null);
        assertFalse(board.isValidMove(exposingMove), "Cannot expose king to discovered check");
        
        // Rook can move along the same file (blocking the black rook)
        Move blockingMove = new Move(new Position(6, 4), new Position(5, 4), rook, null);
        assertTrue(board.isValidMove(blockingMove), "Can move along file to maintain block");
    }
    
    @Test
    @DisplayName("Test smothered mate")
    void testSmotheredMate() {
        // Classic knight checkmate where king is trapped by its own pieces
        board.clear();
        
        // True smothered mate: Black king at h8, trapped by own pieces, knight at f7
        board.setPiece(new Position(0, 7), new King("Black", new Position(0, 7))); // h8
        board.setPiece(new Position(0, 6), new Rook("Black", new Position(0, 6))); // g8
        board.setPiece(new Position(1, 7), new Pawn("Black", new Position(1, 7))); // h7
        board.setPiece(new Position(1, 6), new Pawn("Black", new Position(1, 6))); // g7
        
        // White knight at f7 delivering checkmate (protected by queen)
        board.setPiece(new Position(1, 5), new Knight("White", new Position(1, 5))); // f7
        board.setPiece(new Position(3, 3), new Queen("White", new Position(3, 3))); // d5 protects knight
        board.setCurrentTurn("Black");
        
        assertTrue(board.isInCheck("Black"), "Black king should be in check from knight");
        assertTrue(board.isCheckmate("Black"), "Should be smothered mate");
    }
    
    @Test
    @DisplayName("Test back rank mate")
    void testBackRankMate() {
        board.clear();
        
        // White king trapped on back rank by its own pawns
        board.setPiece(new Position(7, 4), new King("White", new Position(7, 4))); // e1
        board.setPiece(new Position(6, 3), new Pawn("White", new Position(6, 3))); // d2
        board.setPiece(new Position(6, 4), new Pawn("White", new Position(6, 4))); // e2
        board.setPiece(new Position(6, 5), new Pawn("White", new Position(6, 5))); // f2
        
        // Black rook on back rank
        board.setPiece(new Position(7, 0), new Rook("Black", new Position(7, 0))); // a1
        board.setCurrentTurn("White");
        
        assertTrue(board.isInCheck("White"), "White king should be in check");
        assertTrue(board.isCheckmate("White"), "Should be back rank mate");
    }
}
