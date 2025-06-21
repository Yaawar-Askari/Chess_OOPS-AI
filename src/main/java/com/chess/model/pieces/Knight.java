package com.chess.model.pieces;

import com.chess.model.Board;
import com.chess.model.Piece;
import com.chess.model.Position;

import java.util.List;

/**
 * Represents a Knight piece in chess
 */
public class Knight extends Piece {
    
    public Knight(String color, Position position) {
        super(color, position);
    }
    
    @Override
    public String getType() {
        return "Knight";
    }
    
    @Override
    public List<Position> getAttackMoves(Board board) {
        return getPossibleMoves(board);
    }
    
    @Override
    public int getValue() {
        return 320;
    }
    
    @Override
    public String getSymbol() {
        return color.equals("White") ? "♘" : "♞";
    }
    
    @Override
    protected int[][] getMoveDirections() {
        return new int[][] {
            {-2, -1}, {-2, 1},  // Up
            {-1, -2}, {-1, 2},  // Up
            {1, -2},  {1, 2},   // Down
            {2, -1},  {2, 1}    // Down
        };
    }
    
    @Override
    protected boolean canMoveMultipleSquares() {
        return false; // Knight can only move one L-shape at a time
    }
} 