package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class CheckmateDebugDetailed {
    
    public static void main(String[] args) {
        System.out.println("=== Detailed Checkmate Debug ===");
        
        Board board = new Board();
        
        // Clear the board first
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board.setPiece(new Position(row, col), null);
            }
        }
        
        // Set up back-rank checkmate position
        // White king trapped on back rank
        King whiteKing = new King("White", new Position(7, 0));
        board.setPiece(new Position(7, 0), whiteKing);
        board.setPiece(new Position(6, 0), new Pawn("White", new Position(6, 0))); // a2 - blocks escape to a2
        board.setPiece(new Position(6, 1), new Pawn("White", new Position(6, 1))); // b2 - blocks escape to b2
        
        // Black rook delivering checkmate from the first rank
        Rook blackRook = new Rook("Black", new Position(7, 7)); // h1 - attacking along rank
        board.setPiece(new Position(7, 7), blackRook);
        
        board.setCurrentTurn("White");
        
        System.out.println("Board setup:");
        System.out.println("White king at a1: " + board.getPiece(new Position(7, 0)));
        System.out.println("Black rook at h1: " + board.getPiece(new Position(7, 7)));
        System.out.println("White pawns at a2, b2: " + 
                          board.getPiece(new Position(6, 0)) + ", " +
                          board.getPiece(new Position(6, 1)));
        
        // Check if the king is in check
        boolean isInCheck = board.isInCheck("White");
        System.out.println("\nWhite king in check: " + isInCheck);
        
        // Debug: Check if a1 is under attack by the rook
        boolean a1UnderAttack = board.isSquareUnderAttack(new Position(7, 0), "White");
        System.out.println("a1 under attack by Black: " + a1UnderAttack);
        
        // Check what squares the rook attacks
        var rookAttacks = blackRook.getPossibleMoves(board);
        System.out.println("Black rook can attack: " + rookAttacks);
        
        // Check what moves the king has
        var kingMoves = whiteKing.getPossibleMoves(board);
        System.out.println("White king possible moves: " + kingMoves);
        
        var kingValidMoves = whiteKing.getValidMoves(board);
        System.out.println("White king valid moves: " + kingValidMoves);
        
        // Check each potential king move
        System.out.println("\nChecking potential king moves:");
        for (Position pos : kingMoves) {
            boolean underAttack = board.isSquareUnderAttack(pos, "White");
            System.out.println("  " + positionToString(pos) + " under attack: " + underAttack);
        }
        
        boolean isCheckmate = board.isCheckmate("White");
        System.out.println("\nWhite in checkmate: " + isCheckmate);
        
        String gameResult = board.checkGameEndingConditions();
        System.out.println("Game result: " + gameResult);
    }
    
    private static String positionToString(Position pos) {
        char file = (char)('a' + pos.getCol());
        int rank = 8 - pos.getRow();
        return "" + file + rank;
    }
}
