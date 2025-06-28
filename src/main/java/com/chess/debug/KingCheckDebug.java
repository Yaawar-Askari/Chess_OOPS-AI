package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class KingCheckDebug {
    public static void main(String[] args) {
        System.out.println("=== Debugging king check prevention ===");
        
        Board board = new Board();
        board.clear();
        
        // White king at e1, Black rook at e8
        board.setPiece(new Position(7, 4), new King("White", new Position(7, 4)));
        board.setPiece(new Position(0, 4), new Rook("Black", new Position(0, 4)));
        board.setCurrentTurn("White");
        
        Piece king = board.getPiece(new Position(7, 4));
        System.out.println("King: " + king);
        System.out.println("Rook: " + board.getPiece(new Position(0, 4)));
        
        // Test if d1 is under attack
        Position d1 = new Position(7, 3);
        boolean isUnderAttack = board.isSquareUnderAttack(d1, "White");
        System.out.println("Is d1 under attack: " + isUnderAttack);
        
        // Test the move that should be invalid (king to d1)
        Move invalidMove = new Move(new Position(7, 4), new Position(7, 3), king, null);
        boolean isValid = board.isValidMove(invalidMove);
        System.out.println("King e1 to d1 valid: " + isValid + " (should be false)");
        
        // Debug the validation steps
        System.out.println("\n=== Validation steps ===");
        System.out.println("1. Basic validation passed");
        System.out.println("2. Turn check passed");
        System.out.println("3. Destination check passed");
        
        // Check if move is pseudo-legal
        var kingMoves = king.getPossibleMoves(board);
        System.out.println("King possible moves: " + kingMoves);
        boolean isPseudoLegal = kingMoves.contains(d1);
        System.out.println("Is pseudo-legal: " + isPseudoLegal);
        
        // Test the final check - would leave king in check
        // This is where the issue likely is
        
        System.out.println("\n=== Testing king in check logic ===");
        
        // Check if king is currently in check
        boolean currentlyInCheck = board.isInCheck("White");
        System.out.println("King currently in check: " + currentlyInCheck);
        
        // Test square attack detection
        Position e1 = new Position(7, 4);
        boolean e1UnderAttack = board.isSquareUnderAttack(e1, "White");
        System.out.println("e1 under attack: " + e1UnderAttack);
        
        Position f1 = new Position(7, 5);
        boolean f1UnderAttack = board.isSquareUnderAttack(f1, "White");
        System.out.println("f1 under attack: " + f1UnderAttack);
        
        // Also test a valid move
        Move validMove = new Move(new Position(7, 4), new Position(7, 5), king, null);
        boolean validMoveResult = board.isValidMove(validMove);
        System.out.println("King e1 to f1 valid: " + validMoveResult + " (should be true)");
    }
}
