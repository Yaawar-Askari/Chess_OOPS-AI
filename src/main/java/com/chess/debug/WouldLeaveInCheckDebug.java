package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class WouldLeaveInCheckDebug {
    public static void main(String[] args) {
        System.out.println("=== Debugging wouldLeaveKingInCheck ===");
        
        Board board = new Board();
        board.clear();
        
        // White king at e1, Black rook at e8
        board.setPiece(new Position(7, 4), new King("White", new Position(7, 4)));
        board.setPiece(new Position(0, 4), new Rook("Black", new Position(0, 4)));
        board.setCurrentTurn("White");
        
        Piece king = board.getPiece(new Position(7, 4));
        
        // Test the move king e1 to d1
        Move move = new Move(new Position(7, 4), new Position(7, 3), king, null);
        
        System.out.println("Original board:");
        System.out.println("King at: " + king.getPosition());
        System.out.println("King in check: " + board.isInCheck("White"));
        
        // Manually test the wouldLeaveKingInCheck logic
        Board tempBoard = board.clone();
        System.out.println("\nAfter cloning:");
        Piece clonedKing = tempBoard.getPiece(new Position(7, 4));
        System.out.println("Cloned king at: " + clonedKing.getPosition());
        System.out.println("Cloned board king in check: " + tempBoard.isInCheck("White"));
        
        // Create move with cloned piece
        Move clonedMove = new Move(new Position(7, 4), new Position(7, 3), clonedKing, null);
        
        // Execute the move
        tempBoard.setPiece(new Position(7, 3), clonedKing);
        tempBoard.setPiece(new Position(7, 4), null);
        
        System.out.println("\nAfter move execution:");
        System.out.println("King now at: " + clonedKing.getPosition());
        System.out.println("King in check after move: " + tempBoard.isInCheck("White"));
        
        // Check if d1 is under attack
        Position d1 = new Position(7, 3);
        boolean d1UnderAttack = tempBoard.isSquareUnderAttack(d1, "White");
        System.out.println("d1 under attack: " + d1UnderAttack);
        
        // Find the king
        Position kingPos = null;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = tempBoard.getPiece(new Position(row, col));
                if (p != null && p.getType().equals("King") && p.getColor().equals("White")) {
                    kingPos = p.getPosition();
                    break;
                }
            }
        }
        System.out.println("Found king at: " + kingPos);
    }
}
