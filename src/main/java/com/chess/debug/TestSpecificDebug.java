package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class TestSpecificDebug {
    public static void main(String[] args) {
        System.out.println("=== Debugging specific test failure ===");
        
        Board board = new Board();
        
        // Test the exact scenario from the failing test
        Piece whitePawn = board.getPiece(new Position(6, 4)); // e2
        System.out.println("White pawn: " + whitePawn);
        
        // The failing test
        Move invalidMove = new Move(new Position(6, 4), new Position(3, 4), whitePawn, null);
        boolean result = board.isValidMove(invalidMove);
        
        System.out.println("Move from e2 to e5 (3 squares): " + result);
        System.out.println("Expected: false, Got: " + result);
        
        // Let's also test normal moves for comparison
        Move validMove1 = new Move(new Position(6, 4), new Position(5, 4), whitePawn, null);
        Move validMove2 = new Move(new Position(6, 4), new Position(4, 4), whitePawn, null);
        
        System.out.println("Move from e2 to e3 (1 square): " + board.isValidMove(validMove1));
        System.out.println("Move from e2 to e4 (2 squares): " + board.isValidMove(validMove2));
        
        // Debug the validation steps
        System.out.println("\n=== Debugging validation steps ===");
        
        // Check basic validation
        System.out.println("Piece: " + invalidMove.getPiece());
        System.out.println("From valid: " + invalidMove.getFrom().isValid());
        System.out.println("To valid: " + invalidMove.getTo().isValid());
        System.out.println("Current turn: " + board.getCurrentTurn());
        System.out.println("Piece color: " + invalidMove.getPiece().getColor());
        System.out.println("Color matches turn: " + invalidMove.getPiece().getColor().equals(board.getCurrentTurn()));
        
        // Check destination
        Piece destinationPiece = board.getPiece(invalidMove.getTo());
        System.out.println("Destination piece: " + destinationPiece);
        
        // Check possible moves
        var possibleMoves = whitePawn.getPossibleMoves(board);
        System.out.println("Possible moves: " + possibleMoves);
        System.out.println("Target in possible moves: " + possibleMoves.contains(invalidMove.getTo()));
    }
}
