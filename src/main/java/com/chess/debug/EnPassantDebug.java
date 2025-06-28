package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class EnPassantDebug {
    public static void main(String[] args) {
        System.out.println("=== Debugging En Passant ===");
        
        Board board = new Board();
        board.clear();
        
        // White pawn at e5, Black pawn at f5
        board.setPiece(new Position(3, 4), new Pawn("White", new Position(3, 4)));
        board.setPiece(new Position(3, 5), new Pawn("Black", new Position(3, 5)));
        
        // Set en passant target
        board.setEnPassantTarget(new Position(2, 5)); // f6
        board.setCurrentTurn("White");
        
        Piece whitePawn = board.getPiece(new Position(3, 4));
        Move enPassantMove = new Move(new Position(3, 4), new Position(2, 5), whitePawn, null,
                                    Move.MoveType.EN_PASSANT, null, false, false);
        
        System.out.println("White pawn: " + whitePawn);
        System.out.println("En passant target: " + board.getEnPassantTarget());
        System.out.println("Move type: " + enPassantMove.getType());
        System.out.println("Move from: " + enPassantMove.getFrom());
        System.out.println("Move to: " + enPassantMove.getTo());
        
        // Debug validation steps
        System.out.println("\n=== Validation Steps ===");
        
        // Basic validation
        System.out.println("1. Piece: " + (enPassantMove.getPiece() != null));
        System.out.println("2. From valid: " + enPassantMove.getFrom().isValid());
        System.out.println("3. To valid: " + enPassantMove.getTo().isValid());
        System.out.println("4. Correct turn: " + enPassantMove.getPiece().getColor().equals(board.getCurrentTurn()));
        System.out.println("5. Not same position: " + !enPassantMove.getFrom().equals(enPassantMove.getTo()));
        
        // Destination check
        Piece destinationPiece = board.getPiece(enPassantMove.getTo());
        System.out.println("6. Destination piece: " + destinationPiece);
        
        // Pseudo-legal check
        var possibleMoves = whitePawn.getPossibleMoves(board);
        System.out.println("7. Pawn possible moves: " + possibleMoves);
        System.out.println("8. Target in possible moves: " + possibleMoves.contains(enPassantMove.getTo()));
        
        // Special move validation
        System.out.println("9. Move type: " + enPassantMove.getType());
        
        // Manual en passant validation
        boolean enPassantValid = board.getEnPassantTarget() != null && 
                                whitePawn.getType().equals("Pawn") && 
                                enPassantMove.getTo().equals(board.getEnPassantTarget());
        System.out.println("10. En passant valid: " + enPassantValid);
        
        // Final validation
        boolean isValid = board.isValidMove(enPassantMove);
        System.out.println("\nFinal result: " + isValid);
    }
}
