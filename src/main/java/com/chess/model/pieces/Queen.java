package com.chess.model.pieces;

import com.chess.model.Board;
import com.chess.model.Piece;
import com.chess.model.Position;

import java.util.List;

/**
 * Represents a Queen piece in chess
 */
public class Queen extends Piece {
    
    public Queen(String color, Position position) {
        super(color, position);
    }
    
    @Override
    public String getType() {
        return "Queen";
    }
    
    @Override
    public List<Position> getAttackMoves(Board board) {
        return getPossibleMoves(board);
    }
    
    @Override
    public int getValue() {
        return 900;
    }
    
    @Override
    public String getSymbol() {
        return color.equals("White") ? "♕" : "♛";
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
        return true; // Queen can move any number of squares
    }
} 