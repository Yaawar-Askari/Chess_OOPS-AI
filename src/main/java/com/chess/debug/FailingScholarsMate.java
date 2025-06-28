package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

/**
 * Test the exact Scholar's mate sequence from the failing test
 */
public class FailingScholarsMate {
    public static void main(String[] args) {
        System.out.println("=== Testing Exact Scholar's Mate from Unit Test ===");
        
        Board board = new Board();
        
        // 1. e4
        board.makeMove(new Move(new Position(6, 4), new Position(4, 4), 
                              board.getPiece(new Position(6, 4)), null));
        
        // 1... e5
        board.makeMove(new Move(new Position(1, 4), new Position(3, 4), 
                              board.getPiece(new Position(1, 4)), null));
        
        // 2. Bc4
        board.makeMove(new Move(new Position(7, 5), new Position(4, 2), 
                              board.getPiece(new Position(7, 5)), null));
        
        // 2... Nc6
        board.makeMove(new Move(new Position(0, 1), new Position(2, 2), 
                              board.getPiece(new Position(0, 1)), null));
        
        // 3. Qh5
        board.makeMove(new Move(new Position(7, 3), new Position(3, 7), 
                              board.getPiece(new Position(7, 3)), null));
        
        // 3... d6 (weak move, not blocking)
        board.makeMove(new Move(new Position(1, 3), new Position(2, 3), 
                              board.getPiece(new Position(1, 3)), null));
        
        System.out.println("Position after 3...d6:");
        printBoard(board);
        System.out.println("Turn: " + board.getCurrentTurn());
        
        // 4. Qxf7# - Queen captures f7 pawn for checkmate
        Piece queen = board.getPiece(new Position(3, 7));
        Piece f7Pawn = board.getPiece(new Position(1, 5));
        System.out.println("\nQueen at h5: " + queen);
        System.out.println("Pawn at f7: " + f7Pawn);
        
        Move checkmate = new Move(new Position(3, 7), new Position(1, 5), queen, f7Pawn);
        System.out.println("Checkmate move valid: " + board.isValidMove(checkmate));
        
        board.makeMove(checkmate);
        
        System.out.println("\nAfter Qxf7+:");
        printBoard(board);
        System.out.println("Turn: " + board.getCurrentTurn());
        
        boolean blackInCheck = board.isInCheck("Black");
        boolean blackInCheckmate = board.isCheckmate("Black");
        
        System.out.println("\nBlack in check: " + blackInCheck);
        System.out.println("Black in checkmate: " + blackInCheckmate);
        
        if (blackInCheck && !blackInCheckmate) {
            System.out.println("\nBlack is in check but not checkmate. Analyzing escape routes:");
            
            // Check all black pieces for valid moves
            boolean hasValidMoves = false;
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = board.getPiece(new Position(row, col));
                    if (piece != null && piece.getColor().equals("Black")) {
                        var validMoves = piece.getValidMoves(board);
                        if (!validMoves.isEmpty()) {
                            hasValidMoves = true;
                            System.out.println("  " + piece + " at " + piece.getPosition() + " can move to: " + validMoves);
                        }
                    }
                }
            }
            System.out.println("Black has valid moves: " + hasValidMoves);
        }
    }
    
    private static void printBoard(Board board) {
        for (int row = 0; row < 8; row++) {
            System.out.print((8-row) + " ");
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece != null) {
                    System.out.print(piece.getSymbol() + " ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
        System.out.println("  a b c d e f g h");
    }
}
