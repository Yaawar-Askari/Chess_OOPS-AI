package com.chess.model.pieces;

import com.chess.model.Board;
import com.chess.model.Piece;
import com.chess.model.Position;

import java.util.List;

/**
 * Represents a Rook piece in chess
 */
public class Rook extends Piece {
    
    public Rook(String color, Position position) {
        super(color, position);
    }
    
    @Override
    public String getType() {
        return "Rook";
    }
    
    @Override
    public List<Position> getValidMoves(Board board) {
        return getPossibleMoves(board);
    }
    
    @Override
    public List<Position> getAttackMoves(Board board) {
        return getPossibleMoves(board);
    }
    
    @Override
    public int getValue() {
        return 500;
    }
    
    @Override
    public String getSymbol() {
        return color.equals("White") ? "♖" : "♜";
    }
    
    @Override
    protected int[][] getMoveDirections() {
        return new int[][] {
            {-1, 0},  // Up
            {1, 0},   // Down
            {0, -1},  // Left
            {0, 1}    // Right
        };
    }
    
    @Override
    protected boolean canMoveMultipleSquares() {
        return true; // Rook can move any number of squares
    }
} 