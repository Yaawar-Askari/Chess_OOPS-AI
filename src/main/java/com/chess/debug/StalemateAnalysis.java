package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class StalemateAnalysis {
    public static void main(String[] args) {
        Board board = new Board();
        board.clear();
        
        // Set up the stalemate position from the test
        // White king at a1 (7,0), Black king at c2 (5,2), Black queen at b3 (5,1)
        board.setPiece(new Position(7, 0), new King("White", new Position(7, 0)));
        board.setPiece(new Position(5, 2), new King("Black", new Position(5, 2)));
        board.setPiece(new Position(5, 1), new Queen("Black", new Position(5, 1)));
        
        board.setCurrentTurn("White");
        
        System.out.println("=== Stalemate Analysis ===");
        System.out.println("White King at a1 (7,0)");
        System.out.println("Black King at c2 (5,2)"); 
        System.out.println("Black Queen at b3 (5,1)");
        System.out.println();
        
        Position kingPos = new Position(7, 0);
        King whiteKing = (King) board.getPiece(kingPos);
        
        System.out.println("White king in check: " + board.isInCheck("White"));
        System.out.println();
        
        // Check all possible king moves
        System.out.println("Checking king moves from a1:");
        Position[] possibleMoves = {
            new Position(6, 0), // a2
            new Position(6, 1), // b2  
            new Position(7, 1)  // b1
        };
        
        String[] moveNames = {"a2", "b2", "b1"};
        
        for (int i = 0; i < possibleMoves.length; i++) {
            Position move = possibleMoves[i];
            String moveName = moveNames[i];
            
            if (move.isValid()) {
                boolean underAttack = board.isSquareUnderAttack(move, "White");
                System.out.println(moveName + " (" + move.getRow() + "," + move.getCol() + ") under attack: " + underAttack);
                
                // Test the actual move
                Move testMove = new Move(kingPos, move, whiteKing, null, Move.MoveType.NORMAL, null, false, false);
                boolean isValid = board.isValidMove(testMove);
                System.out.println(moveName + " move valid: " + isValid);
                System.out.println();
            }
        }
        
        System.out.println("Is stalemate: " + board.isStalemate("White"));
    }
}
