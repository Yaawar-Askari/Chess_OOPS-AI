package com.chess.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.chess.model.pieces.*;

/**
 * Comprehensive test suite for move validation and checkmate detection
 */
public class MoveValidationTest {
    
    private Board board;
    
    @BeforeEach
    void setUp() {
        board = new Board();
    }
    
    @Test
    @DisplayName("Test basic pawn moves")
    void testBasicPawnMoves() {
        // White pawn at e2 can move to e3 and e4
        Piece whitePawn = board.getPiece(new Position(6, 4)); // e2
        assertNotNull(whitePawn);
        assertEquals("Pawn", whitePawn.getType());
        
        Move singleMove = new Move(new Position(6, 4), new Position(5, 4), whitePawn, null);
        Move doubleMove = new Move(new Position(6, 4), new Position(4, 4), whitePawn, null);
        
        assertTrue(board.isValidMove(singleMove), "Pawn should be able to move one square forward");
        assertTrue(board.isValidMove(doubleMove), "Pawn should be able to move two squares from starting position");
        
        // Invalid move - three squares
        Move invalidMove = new Move(new Position(6, 4), new Position(3, 4), whitePawn, null);
        assertFalse(board.isValidMove(invalidMove), "Pawn cannot move three squares");
    }
    
    @Test
    @DisplayName("Test knight moves")
    void testKnightMoves() {
        Piece knight = board.getPiece(new Position(7, 1)); // b1
        assertNotNull(knight);
        assertEquals("Knight", knight.getType());
        
        // Valid L-shaped moves
        Move validMove1 = new Move(new Position(7, 1), new Position(5, 0), knight, null); // a3
        Move validMove2 = new Move(new Position(7, 1), new Position(5, 2), knight, null); // c3
        
        assertTrue(board.isValidMove(validMove1), "Knight should be able to move in L-shape");
        assertTrue(board.isValidMove(validMove2), "Knight should be able to move in L-shape");
        
        // Invalid move - straight line
        Move invalidMove = new Move(new Position(7, 1), new Position(5, 1), knight, null);
        assertFalse(board.isValidMove(invalidMove), "Knight cannot move in straight line");
    }
    
    @Test
    @DisplayName("Test king in check prevention")
    void testKingInCheckPrevention() {
        // Set up a position where king would be in check
        board.clear();
        
        // White king at e1, Black rook at e8
        board.setPiece(new Position(7, 4), new King("White", new Position(7, 4)));
        board.setPiece(new Position(0, 4), new Rook("Black", new Position(0, 4)));
        board.setCurrentTurn("White");
        
        Piece king = board.getPiece(new Position(7, 4));
        
        // King cannot move to e2 (still on same file as rook)
        Move invalidMove = new Move(new Position(7, 4), new Position(6, 4), king, null);
        assertFalse(board.isValidMove(invalidMove), "King cannot move to square under attack");
        
        // King can move to f1 (safe square)
        Move validMove = new Move(new Position(7, 4), new Position(7, 5), king, null);
        assertTrue(board.isValidMove(validMove), "King should be able to move to safe square");
    }
    
    @Test
    @DisplayName("Test checkmate detection")
    void testCheckmateDetection() {
        // Set up a back-rank mate position
        board.clear();
        
        // White king at h1, Black rook at a1, Black rook at h8
        board.setPiece(new Position(7, 7), new King("White", new Position(7, 7)));
        board.setPiece(new Position(7, 0), new Rook("Black", new Position(7, 0)));
        board.setPiece(new Position(0, 7), new Rook("Black", new Position(0, 7)));
        
        // White pawns blocking escape
        board.setPiece(new Position(6, 6), new Pawn("White", new Position(6, 6)));
        board.setPiece(new Position(6, 7), new Pawn("White", new Position(6, 7)));
        
        board.setCurrentTurn("White");
        
        assertTrue(board.isInCheck("White"), "White king should be in check");
        assertTrue(board.isCheckmate("White"), "White should be in checkmate");
        assertFalse(board.isStalemate("White"), "This should be checkmate, not stalemate");
    }
    
    @Test
    @DisplayName("Test stalemate detection")
    void testStalemateDetection() {
        // Set up a stalemate position
        board.clear();
        
        // White king at a1, Black king at c2, Black queen at b3
        board.setPiece(new Position(7, 0), new King("White", new Position(7, 0)));
        board.setPiece(new Position(5, 2), new King("Black", new Position(5, 2)));
        board.setPiece(new Position(5, 1), new Queen("Black", new Position(5, 1)));
        
        board.setCurrentTurn("White");
        
        assertFalse(board.isInCheck("White"), "White king should not be in check");
        assertTrue(board.isStalemate("White"), "White should be in stalemate");
        assertFalse(board.isCheckmate("White"), "This should be stalemate, not checkmate");
    }
    
    @Test
    @DisplayName("Test castling validation")
    void testCastlingValidation() {
        // Test that castling is blocked when squares are under attack
        board.clear();
        
        // White king and rook in starting positions
        board.setPiece(new Position(7, 4), new King("White", new Position(7, 4)));
        board.setPiece(new Position(7, 7), new Rook("White", new Position(7, 7)));
        
        // Black rook attacking f1
        board.setPiece(new Position(0, 5), new Rook("Black", new Position(0, 5)));
        
        board.setCurrentTurn("White");
        
        Piece king = board.getPiece(new Position(7, 4));
        Move castleMove = new Move(new Position(7, 4), new Position(7, 6), king, null, 
                                 Move.MoveType.CASTLE_KINGSIDE, null, false, false);
        
        assertFalse(board.isValidMove(castleMove), "Cannot castle through attacked square");
    }
    
    @Test
    @DisplayName("Test en passant capture")
    void testEnPassantCapture() {
        board.clear();
        
        // White pawn at e5, Black pawn moves from f7 to f5
        board.setPiece(new Position(3, 4), new Pawn("White", new Position(3, 4)));
        board.setPiece(new Position(3, 5), new Pawn("Black", new Position(3, 5)));
        
        // Set en passant target
        board.setEnPassantTarget(new Position(2, 5));
        board.setCurrentTurn("White");
        
        Piece whitePawn = board.getPiece(new Position(3, 4));
        Move enPassantMove = new Move(new Position(3, 4), new Position(2, 5), whitePawn, null,
                                    Move.MoveType.EN_PASSANT, null, false, false);
        
        assertTrue(board.isValidMove(enPassantMove), "En passant capture should be valid");
    }
    
    @Test
    @DisplayName("Test piece pinning")
    void testPiecePinning() {
        board.clear();
        
        // White king at e1, White bishop at d2, Black rook at a5
        board.setPiece(new Position(7, 4), new King("White", new Position(7, 4)));
        board.setPiece(new Position(6, 3), new Bishop("White", new Position(6, 3)));
        board.setPiece(new Position(3, 0), new Rook("Black", new Position(3, 0)));
        
        board.setCurrentTurn("White");
        
        Piece bishop = board.getPiece(new Position(6, 3));
        // Bishop is pinned and cannot move off the diagonal
        Move invalidMove = new Move(new Position(6, 3), new Position(5, 3), bishop, null);
        assertFalse(board.isValidMove(invalidMove), "Pinned piece cannot expose king to check");
        
        // Bishop can move along the pin line
        Move validMove = new Move(new Position(6, 3), new Position(5, 2), bishop, null);
        assertTrue(board.isValidMove(validMove), "Pinned piece can move along pin line");
    }
    
    @Test
    @DisplayName("Test game ending conditions")
    void testGameEndingConditions() {
        // Test checkmate
        board.clear();
        board.setPiece(new Position(7, 7), new King("White", new Position(7, 7)));
        board.setPiece(new Position(7, 0), new Rook("Black", new Position(7, 0)));
        board.setPiece(new Position(0, 7), new Rook("Black", new Position(0, 7)));
        board.setPiece(new Position(6, 6), new Pawn("White", new Position(6, 6)));
        board.setPiece(new Position(6, 7), new Pawn("White", new Position(6, 7)));
        board.setCurrentTurn("Black");
        
        String result = board.checkGameEndingConditions();
        assertEquals("CHECKMATE_BLACK", result, "Should detect checkmate for Black");
        
        // Test stalemate
        board.clear();
        board.setPiece(new Position(7, 0), new King("White", new Position(7, 0)));
        board.setPiece(new Position(5, 2), new King("Black", new Position(5, 2)));
        board.setPiece(new Position(5, 1), new Queen("Black", new Position(5, 1)));
        board.setCurrentTurn("Black");
        
        result = board.checkGameEndingConditions();
        assertEquals("STALEMATE", result, "Should detect stalemate");
    }
}
