package com.chess.model.pieces;

import com.chess.model.Board;
import com.chess.model.Piece;
import com.chess.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a King piece in chess
 */
public class King extends Piece {
    
    public King(String color, Position position) {
        super(color, position);
    }
    
    @Override
    public String getType() {
        return "King";
    }
    
    @Override
    public List<Position> getValidMoves(Board board) {
        List<Position> validMoves = new ArrayList<>();
        
        // Get basic moves
        List<Position> possibleMoves = getPossibleMoves(board);
        for (Position move : possibleMoves) {
            if (isValidMove(board, move)) {
                validMoves.add(move);
            }
        }
        
        // Add castling moves
        validMoves.addAll(getCastlingMoves(board));
        
        return validMoves;
    }
    
    @Override
    public List<Position> getAttackMoves(Board board) {
        return getPossibleMoves(board);
    }
    
    @Override
    public int getValue() {
        return 10000; // King has infinite value, but we use a high number
    }
    
    @Override
    public String getSymbol() {
        return color.equals("White") ? "♔" : "♚";
    }
    
    @Override
    protected int[][] getMoveDirections() {
        return new int[][] {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
        };
    }
    
    @Override
    protected boolean canMoveMultipleSquares() {
        return false; // King can only move one square at a time
    }
    
    /**
     * Get castling moves if they are legal
     */
    private List<Position> getCastlingMoves(Board board) {
        List<Position> castlingMoves = new ArrayList<>();
        
        if (hasMoved || board.isInCheck(color)) {
            return castlingMoves; // Cannot castle if king has moved or is in check
        }
        
        // Kingside castling
        if (canCastleKingside(board)) {
            Position kingsideCastle = new Position(position.getRow(), position.getCol() + 2);
            castlingMoves.add(kingsideCastle);
        }
        
        // Queenside castling
        if (canCastleQueenside(board)) {
            Position queensideCastle = new Position(position.getRow(), position.getCol() - 2);
            castlingMoves.add(queensideCastle);
        }
        
        return castlingMoves;
    }
    
    /**
     * Check if kingside castling is possible
     */
    private boolean canCastleKingside(Board board) {
        if (!board.canCastleKingside(color)) {
            return false;
        }

        // Check if squares between king and rook are empty
        Position square1 = new Position(position.getRow(), 5);
        Position square2 = new Position(position.getRow(), 6);
        
        if (board.getPiece(square1) != null || board.getPiece(square2) != null) {
            return false;
        }
        
        // Check if king is not in check and squares are not under attack
        return !board.isInCheck(color) && !board.isSquareUnderAttack(square1, color) && 
               !board.isSquareUnderAttack(square2, color);
    }
    
    /**
     * Check if queenside castling is possible
     */
    private boolean canCastleQueenside(Board board) {
        if (!board.canCastleQueenside(color)) {
            return false;
        }
        
        // Check if squares between king and rook are empty
        Position square1 = new Position(position.getRow(), 1);
        Position square2 = new Position(position.getRow(), 2);
        Position square3 = new Position(position.getRow(), 3);
        
        if (board.getPiece(square1) != null || board.getPiece(square2) != null || 
            board.getPiece(square3) != null) {
            return false;
        }
        
        // Check if king is not in check and squares are not under attack
        return !board.isInCheck(color) && !board.isSquareUnderAttack(square2, color) && 
               !board.isSquareUnderAttack(square3, color);
    }
} 