package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

/**
 * Debug tool to test checkmate detection
 */
public class CheckmateDebug {
    public static void main(String[] args) {
        System.out.println("=== Checkmate Detection Debug ===");
        
        // Test case 1: Simple back-rank checkmate
        testBackRankCheckmate();
        
        // Test case 2: Game ending conditions
        testGameEndingConditions();
    }
    
    static void testBackRankCheckmate() {
        System.out.println("\n=== Test: Back-rank checkmate ===");
        Board board = new Board();
        board.clear();
        
        // White king at h1, trapped by own pawns
        board.setPiece(new Position(7, 7), new King("White", new Position(7, 7))); // h1
        board.setPiece(new Position(6, 6), new Pawn("White", new Position(6, 6))); // g2
        board.setPiece(new Position(6, 7), new Pawn("White", new Position(6, 7))); // h2
        
        // Black queen delivers checkmate
        board.setPiece(new Position(7, 0), new Queen("Black", new Position(7, 0))); // a1
        
        board.setCurrentTurn("White");
        
        System.out.println("White king in check: " + board.isInCheck("White"));
        System.out.println("White in checkmate: " + board.isCheckmate("White"));
        
        // Debug: check all of White's possible moves
        King whiteKing = (King) board.getPiece(new Position(7, 7));
        System.out.println("King possible moves: " + whiteKing.getPossibleMoves(board));
        System.out.println("King valid moves: " + whiteKing.getValidMoves(board));
        
        // Check if any white piece has legal moves
        boolean hasLegalMoves = false;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece != null && piece.getColor().equals("White")) {
                    var validMoves = piece.getValidMoves(board);
                    if (!validMoves.isEmpty()) {
                        hasLegalMoves = true;
                        System.out.println("   " + piece + " at " + piece.getPosition() + " has moves: " + validMoves);
                    }
                }
            }
        }
        System.out.println("White has legal moves: " + hasLegalMoves);
        
        String gameResult = board.checkGameEndingConditions();
        System.out.println("Game ending result: " + gameResult);
    }
    
    static void testGameEndingConditions() {
        System.out.println("\n=== Test: Game ending conditions ===");
        Board board = new Board();
        board.clear();
        
        // Set up a position where Black just checkmated White
        board.setPiece(new Position(7, 7), new King("White", new Position(7, 7))); // h1
        board.setPiece(new Position(6, 6), new Pawn("White", new Position(6, 6))); // g2  
        board.setPiece(new Position(6, 7), new Pawn("White", new Position(6, 7))); // h2
        board.setPiece(new Position(7, 0), new Queen("Black", new Position(7, 0))); // a1 - checkmate piece
        board.setPiece(new Position(0, 4), new King("Black", new Position(0, 4))); // e8
        
        // If it's White's turn, White should be in checkmate
        board.setCurrentTurn("White");
        String result1 = board.checkGameEndingConditions();
        System.out.println("With White to move: " + result1);
        
        // If it's Black's turn, Black just delivered checkmate
        board.setCurrentTurn("Black");
        String result2 = board.checkGameEndingConditions();
        System.out.println("With Black to move: " + result2);
    }
}
