package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class ScholarsMateCheckmate {
    public static void main(String[] args) {
        System.out.println("=== Scholar's Mate Checkmate Debug ===");
        Board board = new Board();
        
        // Simulate the scholar's mate sequence
        // 1. e4
        Move move1 = new Move(new Position(6, 4), new Position(4, 4), 
                             board.getPiece(new Position(6, 4)), null);
        board.makeMove(move1);
        
        // 1... e5
        Move move2 = new Move(new Position(1, 4), new Position(3, 4), 
                             board.getPiece(new Position(1, 4)), null);
        board.makeMove(move2);
        
        // 2. Bc4
        Move move3 = new Move(new Position(7, 5), new Position(4, 2), 
                             board.getPiece(new Position(7, 5)), null);
        board.makeMove(move3);
        
        // 2... Nc6
        Move move4 = new Move(new Position(0, 1), new Position(2, 2), 
                             board.getPiece(new Position(0, 1)), null);
        board.makeMove(move4);
        
        // 3. Qh5
        Move move5 = new Move(new Position(7, 3), new Position(3, 7), 
                             board.getPiece(new Position(7, 3)), null);
        board.makeMove(move5);
        
        // 3... Nf6
        Move move6 = new Move(new Position(0, 6), new Position(2, 5), 
                             board.getPiece(new Position(0, 6)), null);
        board.makeMove(move6);
        
        // 4. Qxf7#
        Piece queen = board.getPiece(new Position(3, 7));
        Piece f7Pawn = board.getPiece(new Position(1, 5));
        Move checkmate = new Move(new Position(3, 7), new Position(1, 5), queen, f7Pawn);
        board.makeMove(checkmate);
        
        System.out.println("After Qxf7#:");
        System.out.println("Queen at f7: " + board.getPiece(new Position(1, 5)));
        System.out.println("Black king at e8: " + board.getPiece(new Position(0, 4)));
        System.out.println("Black in check: " + board.isInCheck("Black"));
        System.out.println("Black in checkmate: " + board.isCheckmate("Black"));
        
        // Debug: Check what legal moves black has
        System.out.println("\nAnalyzing black pieces and their legal moves:");
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece != null && piece.getColor().equals("Black")) {
                    var legalMoves = piece.getValidMoves(board);
                    System.out.println("  " + piece + " legal moves: " + legalMoves);
                }
            }
        }
        
        // Check if king can escape
        King blackKing = (King) board.getPiece(new Position(0, 4));
        var kingMoves = blackKing.getPossibleMoves(board);
        System.out.println("\nBlack king possible moves: " + kingMoves);
        for (Position move : kingMoves) {
            boolean underAttack = board.isSquareUnderAttack(move, "Black");
            System.out.println("  " + move + " under attack: " + underAttack);
        }
    }
}
