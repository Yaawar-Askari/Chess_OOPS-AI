package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class SmotheredMateDebug {
    public static void main(String[] args) {
        System.out.println("=== Smothered Mate Debug ===");
        Board board = new Board();
        board.clear();
        
        // Black king at h8 surrounded by its own pieces
        board.setPiece(new Position(0, 7), new King("Black", new Position(0, 7))); // h8
        board.setPiece(new Position(0, 6), new Rook("Black", new Position(0, 6))); // g8
        board.setPiece(new Position(1, 7), new Pawn("Black", new Position(1, 7))); // h7
        board.setPiece(new Position(1, 6), new Pawn("Black", new Position(1, 6))); // g7
        
        // White knight at g6
        board.setPiece(new Position(2, 6), new Knight("White", new Position(2, 6))); // g6
        board.setCurrentTurn("Black");
        
        System.out.println("Setup:");
        System.out.println("Black king at h8 (0,7): " + board.getPiece(new Position(0, 7)));
        System.out.println("Black rook at g8 (0,6): " + board.getPiece(new Position(0, 6)));
        System.out.println("Black pawn at h7 (1,7): " + board.getPiece(new Position(1, 7)));
        System.out.println("Black pawn at g7 (1,6): " + board.getPiece(new Position(1, 6)));
        System.out.println("White knight at g6 (2,6): " + board.getPiece(new Position(2, 6)));
        
        System.out.println("\nChecking knight attacks:");
        Knight knight = (Knight) board.getPiece(new Position(2, 6));
        var knightMoves = knight.getPossibleMoves(board);
        System.out.println("Knight at g6 can move to: " + knightMoves);
        
        // Check if knight attacks the king position
        Position kingPos = new Position(0, 7);
        boolean attacksKing = knightMoves.contains(kingPos);
        System.out.println("Knight attacks king at h8: " + attacksKing);
        
        // Manual calculation: Knight at g6 (2,6) to h8 (0,7)
        // Delta: row = 0-2 = -2, col = 7-6 = 1
        // Knight moves: (±2,±1) or (±1,±2)
        // (-2,1) is a valid knight move!
        System.out.println("Manual check: g6 to h8 is delta (-2,1) - valid knight move");
        
        System.out.println("\nGame state:");
        System.out.println("Black in check: " + board.isInCheck("Black"));
        System.out.println("Black in checkmate: " + board.isCheckmate("Black"));
        
        // Check if king has any escape squares
        System.out.println("\nKing escape analysis:");
        King king = (King) board.getPiece(kingPos);
        var kingMoves = king.getPossibleMoves(board);
        System.out.println("King possible moves: " + kingMoves);
        
        for (Position move : kingMoves) {
            boolean underAttack = board.isSquareUnderAttack(move, "Black");
            Piece occupying = board.getPiece(move);
            System.out.println("  " + move + " under attack: " + underAttack + ", occupied by: " + occupying);
        }
        
        // Check all black pieces and their legal moves
        System.out.println("\nAll black pieces and their legal moves:");
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece != null && piece.getColor().equals("Black")) {
                    var legalMoves = piece.getValidMoves(board);
                    System.out.println("  " + piece + " at (" + row + "," + col + ") legal moves: " + legalMoves);
                }
            }
        }
    }
}
