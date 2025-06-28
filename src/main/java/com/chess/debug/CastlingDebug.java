package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

/**
 * Debug tool to test castling move detection and validation
 */
public class CastlingDebug {
    public static void main(String[] args) {
        System.out.println("=== Castling Debug Test ===");
        
        // Create a new board in starting position
        Board board = new Board();
        
        // Clear the middle squares to allow castling
        // Remove pieces between king and rook
        board.setPiece(new Position(7, 1), null); // Knight
        board.setPiece(new Position(7, 2), null); // Bishop  
        board.setPiece(new Position(7, 3), null); // Queen
        board.setPiece(new Position(7, 5), null); // Bishop
        board.setPiece(new Position(7, 6), null); // Knight
        
        System.out.println("Initial board state (cleared for castling):");
        printBoardSection(board);
        
        King whiteKing = (King) board.getPiece(new Position(7, 4));
        
        // Test kingside castling move creation
        Position from = new Position(7, 4); // King's position
        Position toKingside = new Position(7, 6); // Kingside castling target
        
        System.out.println("\n=== Testing Kingside Castling ===");
        System.out.println("King has moved: " + whiteKing.hasMoved());
        System.out.println("Board can castle kingside: " + board.canCastleKingside("White"));
        System.out.println("Board is in check: " + board.isInCheck("White"));
        
        // Create move as GUI would create it
        Move castlingMove = new Move(from, toKingside, whiteKing, board.getPiece(toKingside));
        System.out.println("Move type: " + castlingMove.getType());
        System.out.println("Is valid move (GUI call): " + board.isValidMove(castlingMove));
        
        // Test what the King piece thinks about this move
        boolean kingCanMove = whiteKing.getValidMoves(board).contains(toKingside);
        System.out.println("King can move to target: " + kingCanMove);
        
        // Check what moves the king says are valid
        System.out.println("King's valid moves:");
        for (Position pos : whiteKing.getValidMoves(board)) {
            System.out.println("  " + pos);
        }
        
        // Test if we manually create a castling move with correct type
        System.out.println("\n=== Testing with Correct Move Type ===");
        Move properCastlingMove = new Move(from, toKingside, whiteKing, null, 
                                          Move.MoveType.CASTLE_KINGSIDE, null, false, false);
        System.out.println("Proper castling move type: " + properCastlingMove.getType());
        System.out.println("Is valid move (with correct type): " + board.isValidMove(properCastlingMove));
        
        // Test queenside too
        System.out.println("\n=== Testing Queenside Castling ===");
        Position toQueenside = new Position(7, 2);
        Move queensideCastling = new Move(from, toQueenside, whiteKing, board.getPiece(toQueenside));
        System.out.println("Queenside move type: " + queensideCastling.getType());
        System.out.println("Is valid move (GUI call): " + board.isValidMove(queensideCastling));
        
        Move properQueensideCastling = new Move(from, toQueenside, whiteKing, null,
                                               Move.MoveType.CASTLE_QUEENSIDE, null, false, false);
        System.out.println("Proper queenside move type: " + properQueensideCastling.getType());
        System.out.println("Is valid move (with correct type): " + board.isValidMove(properQueensideCastling));
    }
    
    private static void printBoardSection(Board board) {
        // Print just the back rank to see the castling setup
        for (int col = 0; col < 8; col++) {
            Piece piece = board.getPiece(new Position(7, col));
            if (piece != null) {
                System.out.print(piece.getSymbol() + " ");
            } else {
                System.out.print(". ");
            }
        }
        System.out.println();
    }
}
