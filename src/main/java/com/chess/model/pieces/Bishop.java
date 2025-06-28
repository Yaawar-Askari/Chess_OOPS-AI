package com.chess.model.pieces;

import com.chess.model.Board;
import com.chess.model.Piece;
import com.chess.model.Position;

import java.util.List;

/**
 * Represents a Bishop piece in chess
 */
public class Bishop extends Piece {
    
    public Bishop(String color, Position position) {
        super(color, position);
    }
    
    @Override
    public String getType() {
        return "Bishop";
    }
    
    @Override
    public List<Position> getAttackMoves(Board board) {
        return getPossibleMoves(board);
    }
    
    @Override
    public int getValue() {
        return 330;
    }
      @Override
    public String getSymbol() {
        return color.equals("White") ? "B" : "b";
    }
    
    @Override
    protected int[][] getMoveDirections() {
        return new int[][] {
            {-1, -1},  // Up-Left
            {-1, 1},   // Up-Right
            {1, -1},   // Down-Left
            {1, 1}     // Down-Right
        };
    }
    
    @Override
    protected boolean canMoveMultipleSquares() {
        return true; // Bishop can move any number of squares
    }
} 