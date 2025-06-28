package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class MoveValidationDebug {
    public static void main(String[] args) {
        System.out.println("Starting move validation debug...");
        
        Board board = new Board();
        
        // Test basic pawn move
        Piece whitePawn = board.getPiece(new Position(6, 4)); // e2
        System.out.println("White pawn at e2: " + whitePawn);
        System.out.println("Pawn type: " + whitePawn.getType());
        
        // Test invalid move - three squares
        Move invalidMove = new Move(new Position(6, 4), new Position(3, 4), whitePawn, null);
        System.out.println("Testing invalid move (3 squares forward):");
        System.out.println("From: " + invalidMove.getFrom());
        System.out.println("To: " + invalidMove.getTo());
        
        // Get possible moves for the pawn
        System.out.println("Possible moves for pawn:");
        var possibleMoves = whitePawn.getPossibleMoves(board);
        for (Position move : possibleMoves) {
            System.out.println("  - " + move);
        }
        
        // Check if the invalid move is in possible moves
        boolean inPossibleMoves = possibleMoves.contains(new Position(3, 4));
        System.out.println("Invalid move in possible moves: " + inPossibleMoves);
        
        // Check full validation
        boolean isValid = board.isValidMove(invalidMove);
        System.out.println("Move validation result: " + isValid);
        
        // Test the pseudo-legal check specifically
        boolean isPseudoLegal = whitePawn.getPossibleMoves(board).contains(new Position(3, 4));
        System.out.println("Is pseudo-legal: " + isPseudoLegal);
    }
}
