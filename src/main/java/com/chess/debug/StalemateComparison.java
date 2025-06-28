package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class StalemateComparison {
    public static void main(String[] args) {
        System.out.println("=== Comparing Queen Positions ===\n");
        
        // Test with queen at b4 (4,1)
        testQueenPosition(new Position(4, 1), "b4 (4,1)");
        
        System.out.println();
        
        // Test with queen at b3 (5,1)  
        testQueenPosition(new Position(5, 1), "b3 (5,1)");
    }
    
    static void testQueenPosition(Position queenPos, String description) {
        System.out.println("=== Queen at " + description + " ===");
        
        Board board = new Board();
        board.clear();
        
        // White king at a1, Black king at c3, Black queen at specified position
        board.setPiece(new Position(7, 0), new King("White", new Position(7, 0)));
        board.setPiece(new Position(5, 2), new King("Black", new Position(5, 2)));
        board.setPiece(queenPos, new Queen("Black", queenPos));
        
        board.setCurrentTurn("White");
        
        System.out.println("White king in check: " + board.isInCheck("White"));
        System.out.println("Is stalemate: " + board.isStalemate("White"));
        
        // Check king escape squares
        Position[] moves = {
            new Position(6, 0), // a2
            new Position(6, 1), // b2
            new Position(7, 1)  // b1
        };
        String[] names = {"a2", "b2", "b1"};
        
        System.out.println("King escape analysis:");
        for (int i = 0; i < moves.length; i++) {
            boolean underAttack = board.isSquareUnderAttack(moves[i], "White");
            System.out.println("  " + names[i] + " under attack: " + underAttack);
        }
        
        // Test game ending conditions
        String gameResult = board.checkGameEndingConditions();
        System.out.println("Game ending result: " + gameResult);
    }
}
