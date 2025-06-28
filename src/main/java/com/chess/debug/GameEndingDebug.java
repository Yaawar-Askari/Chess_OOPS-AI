package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class GameEndingDebug {
    public static void main(String[] args) {
        System.out.println("=== Game Ending Conditions Debug ===");
        
        Board board = new Board();
        board.clear();
        
        // Set up stalemate position: White king at a1, Black king at c3, Black queen at b3
        board.setPiece(new Position(7, 0), new King("White", new Position(7, 0)));
        board.setPiece(new Position(5, 2), new King("Black", new Position(5, 2)));
        board.setPiece(new Position(5, 1), new Queen("Black", new Position(5, 1)));
        board.setCurrentTurn("Black");
        
        System.out.println("Current turn: " + board.getCurrentTurn());
        System.out.println("White in check: " + board.isInCheck("White"));
        System.out.println("White in stalemate: " + board.isStalemate("White"));
        System.out.println("White in checkmate: " + board.isCheckmate("White"));
        System.out.println("Black in check: " + board.isInCheck("Black"));
        System.out.println("Black in stalemate: " + board.isStalemate("Black"));
        System.out.println("Black in checkmate: " + board.isCheckmate("Black"));
        
        String result = board.checkGameEndingConditions();
        System.out.println("Game ending result: " + result);
        
        // Step through the method logic
        String currentPlayer = board.getCurrentTurn(); // "Black"
        String opponent = currentPlayer.equals("White") ? "Black" : "White"; // "White"
        System.out.println("\nMethod logic:");
        System.out.println("currentPlayer: " + currentPlayer);
        System.out.println("opponent: " + opponent);
        System.out.println("isCheckmate(opponent): " + board.isCheckmate(opponent));
        System.out.println("isStalemate(opponent): " + board.isStalemate(opponent));
    }
}
