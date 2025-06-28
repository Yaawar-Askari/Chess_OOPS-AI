package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class DiscoveredCheckDebug {
    public static void main(String[] args) {
        System.out.println("=== Discovered Check Debug ===");
        Board board = new Board();
        board.clear();
        
        // White king at e1, White bishop at e2, Black rook at e8
        board.setPiece(new Position(7, 4), new King("White", new Position(7, 4))); // e1
        board.setPiece(new Position(6, 4), new Bishop("White", new Position(6, 4))); // e2
        board.setPiece(new Position(0, 4), new Rook("Black", new Position(0, 4))); // e8
        board.setCurrentTurn("White");
        
        System.out.println("Setup:");
        System.out.println("White king at e1 (7,4): " + board.getPiece(new Position(7, 4)));
        System.out.println("White bishop at e2 (6,4): " + board.getPiece(new Position(6, 4)));
        System.out.println("Black rook at e8 (0,4): " + board.getPiece(new Position(0, 4)));
        
        Piece bishop = board.getPiece(new Position(6, 4));
        
        // Test exposing move: bishop e2 to d3 (5,3)
        System.out.println("\nTesting exposing move: e2 to d3");
        Move exposingMove = new Move(new Position(6, 4), new Position(5, 3), bishop, null);
        boolean exposingValid = board.isValidMove(exposingMove);
        System.out.println("Bishop e2->d3 valid: " + exposingValid + " (should be false)");
        
        if (exposingValid) {
            System.out.println("ISSUE: This move should be invalid as it exposes king to check");
        }
        
        // Test blocking move: bishop e2 to e3 (5,4) - stays on same file
        System.out.println("\nTesting blocking move: e2 to e3");
        Move blockingMove = new Move(new Position(6, 4), new Position(5, 4), bishop, null);
        boolean blockingValid = board.isValidMove(blockingMove);
        System.out.println("Bishop e2->e3 valid: " + blockingValid + " (should be true)");
        
        if (!blockingValid) {
            System.out.println("ISSUE: This move should be valid as it maintains the block");
            
            // Check if e3 is occupied
            Piece e3Piece = board.getPiece(new Position(5, 4));
            System.out.println("Piece at e3 (5,4): " + e3Piece);
            
            // Check if bishop can actually move to e3
            var bishopMoves = bishop.getPossibleMoves(board);
            System.out.println("Bishop possible moves: " + bishopMoves);
            
            // Test if king would be in check after this move
            Board testBoard = board.clone();
            testBoard.setPiece(new Position(5, 4), bishop);
            testBoard.setPiece(new Position(6, 4), null);
            boolean kingInCheck = testBoard.isInCheck("White");
            System.out.println("King in check after e2->e3: " + kingInCheck);
        }
    }
}
