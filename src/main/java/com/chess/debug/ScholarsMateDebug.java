package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class ScholarsMateDebug {
    public static void main(String[] args) {
        System.out.println("=== Scholar's Mate Debug ===");
        Board board = new Board();
        
        System.out.println("Initial queen position: " + board.getPiece(new Position(7, 3)));
        
        // 1. e4
        System.out.println("\n1. e4");
        Move move1 = new Move(new Position(6, 4), new Position(4, 4), 
                             board.getPiece(new Position(6, 4)), null);
        board.makeMove(move1);
        
        // 1... e5
        System.out.println("1... e5");
        Move move2 = new Move(new Position(1, 4), new Position(3, 4), 
                             board.getPiece(new Position(1, 4)), null);
        board.makeMove(move2);
        
        // 2. Bc4
        System.out.println("2. Bc4");
        Move move3 = new Move(new Position(7, 5), new Position(4, 2), 
                             board.getPiece(new Position(7, 5)), null);
        board.makeMove(move3);
        
        // 2... Nc6
        System.out.println("2... Nc6");
        Move move4 = new Move(new Position(0, 1), new Position(2, 2), 
                             board.getPiece(new Position(0, 1)), null);
        board.makeMove(move4);
        
        // Check queen position before move 5
        System.out.println("\nBefore move 5 - Queen at (7,3): " + board.getPiece(new Position(7, 3)));
        
        // 3. Qh5 - ISSUE: h5 is (4,7), but that's row 4 col 7 which is h4, not h5
        // h5 should be row 3, col 7 -> (3,7)
        System.out.println("3. Qh5 - moving to (3,7)");
        Piece queen = board.getPiece(new Position(7, 3));
        if (queen != null) {
            Move move5 = new Move(new Position(7, 3), new Position(3, 7), queen, null);
            boolean canMove = board.isValidMove(move5);
            System.out.println("Queen can move to h5: " + canMove);
            if (canMove) {
                board.makeMove(move5);
                System.out.println("Queen now at h5: " + board.getPiece(new Position(3, 7)));
            }
        } else {
            System.out.println("ERROR: No queen at starting position!");
        }
        
        // 3... Nf6
        System.out.println("3... Nf6");
        Move move6 = new Move(new Position(0, 6), new Position(2, 5), 
                             board.getPiece(new Position(0, 6)), null);
        board.makeMove(move6);
        
        // Check final positions
        System.out.println("\nFinal positions:");
        System.out.println("Queen at h5 (3,7): " + board.getPiece(new Position(3, 7)));
        System.out.println("Pawn at f7 (1,5): " + board.getPiece(new Position(1, 5)));
    }
}
