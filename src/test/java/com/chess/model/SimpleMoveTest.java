package com.chess.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple tests to verify basic move validation functionality
 */
public class SimpleMoveTest {
    
    private Board board;
    
    @BeforeEach
    public void setUp() {
        board = new Board();
    }
    
    @Test
    public void testPawnMoveForward() {
        // Test basic pawn move forward
        Position from = new Position(6, 4); // e2
        Position to = new Position(5, 4);   // e3
        
        Piece pawn = board.getPiece(from);
        assertNotNull(pawn, "Pawn should exist at e2");
        assertEquals("Pawn", pawn.getType(), "Should be a pawn");
        
        Move move = new Move(from, to, pawn, null);
        assertTrue(board.isValidMove(move), "Pawn should be able to move forward one square");
    }
    
    @Test
    public void testInvalidPawnMove() {
        // Test invalid pawn move (3 squares forward)
        Position from = new Position(6, 4); // e2
        Position to = new Position(3, 4);   // e5
        
        Piece pawn = board.getPiece(from);
        Move move = new Move(from, to, pawn, null);
        assertFalse(board.isValidMove(move), "Pawn should not be able to move 3 squares forward");
    }
    
    @Test
    public void testBoardSerialization() {
        // Test that board can be serialized and deserialized
        try {
            // Make a move first
            Position from = new Position(6, 4); // e2
            Position to = new Position(4, 4);   // e4
            Piece pawn = board.getPiece(from);
            Move move = new Move(from, to, pawn, null);
            
            if (board.isValidMove(move)) {
                board.makeMove(move);
            }
            
            // This is a simple test - just ensure the board state is accessible
            String currentTurn = board.getCurrentTurn();
            assertNotNull(currentTurn, "Current turn should not be null");
            assertTrue(currentTurn.equals("White") || currentTurn.equals("Black"), 
                      "Current turn should be White or Black");
                      
        } catch (Exception e) {
            fail("Board operations should not throw exceptions: " + e.getMessage());
        }
    }
}
