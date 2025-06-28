package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class StalemateTestDebug {
    public static void main(String[] args) {
        System.out.println("=== Debugging stalemate test ===");
        
        Board board = new Board();
        board.clear();
        
        // Stalemate test setup from the test
        board.setPiece(new Position(7, 0), new King("White", new Position(7, 0))); // a1
        board.setPiece(new Position(5, 2), new King("Black", new Position(5, 2))); // c3  
        board.setPiece(new Position(5, 1), new Queen("Black", new Position(5, 1))); // b3
        board.setCurrentTurn("Black");
        
        System.out.println("Current turn: " + board.getCurrentTurn());
        
        // Check if White is in check
        boolean whiteInCheck = board.isInCheck("White");
        System.out.println("White in check: " + whiteInCheck);
        
        // Check if White is in stalemate
        boolean whiteInStalemate = board.isStalemate("White");
        System.out.println("White in stalemate: " + whiteInStalemate);
        
        // Debug White king's moves
        Piece whiteKing = board.getPiece(new Position(7, 0));
        var kingMoves = whiteKing.getPossibleMoves(board);
        System.out.println("White king possible moves: " + kingMoves);
        
        // Check if each move is under attack
        for (Position move : kingMoves) {
            boolean underAttack = board.isSquareUnderAttack(move, "White");
            System.out.println("  " + move + " under attack: " + underAttack);
        }
        
        // Check the game ending condition
        String result = board.checkGameEndingConditions();
        System.out.println("Game ending result: " + result);
        
        // Let me manually check what squares are controlled by black pieces
        System.out.println("\n=== Black piece attacks ===");
        Queen blackQueen = (Queen) board.getPiece(new Position(5, 1));
        King blackKing = (King) board.getPiece(new Position(5, 2));
        
        var queenAttacks = blackQueen.getAttackMoves(board);
        var kingAttacks = blackKing.getAttackMoves(board);
        
        System.out.println("Black queen attacks: " + queenAttacks);
        System.out.println("Black king attacks: " + kingAttacks);
    }
}
