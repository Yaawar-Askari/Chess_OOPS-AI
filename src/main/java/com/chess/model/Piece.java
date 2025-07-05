package com.chess.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all chess pieces
 */
public abstract class Piece {
    protected final String color;
    protected Position position;
    protected boolean hasMoved;
    
    public Piece(String color, Position position) {
        this.color = color;
        this.position = position;
        this.hasMoved = false;
    }
    
    public String getColor() {
        return color;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public void setPosition(Position position) {
        this.position = position;
        this.hasMoved = true;
    }
    
    public boolean hasMoved() {
        return hasMoved;
    }
    
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
    
    public abstract String getType();
    
    /**
     * Get all valid moves for this piece (excluding moves that would leave king in check)
     */
    public List<Position> getValidMoves(Board board) {
        List<Position> possibleDestinations = getPossibleMoves(board);
        List<Position> validMoves = new ArrayList<>();
        
        for (Position destination : possibleDestinations) {
            Move.MoveType type = board.determineMoveType(this, this.position, destination);
            Piece capturedPiece = board.getPiece(destination);
            Move move = new Move(this.position, destination, this, capturedPiece, type, null, false, false);

            // Use the board's validation which correctly simulates the move
            if (board.isValidMove(move)) {
                validMoves.add(destination);
            }
        }
        
        return validMoves;
    }
    
    public abstract List<Position> getAttackMoves(Board board);
    
    public abstract int getValue();
    
    public abstract String getSymbol();
    
    /**
     * Get all possible moves for this piece (including moves that might leave king in check)
     */
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        
        // Get all possible directions for this piece
        int[][] directions = getMoveDirections();
        
        for (int[] direction : directions) {
            int deltaRow = direction[0];
            int deltaCol = direction[1];
            
            Position current = position.add(deltaRow, deltaCol);
            
            // Check if piece can move in this direction
            if (current != null && canMoveInDirection(board, deltaRow, deltaCol)) {
                while (current != null && current.isValid()) {
                    Piece targetPiece = board.getPiece(current);
                    
                    if (targetPiece == null) {
                        // Empty square
                        moves.add(current);
                    } else if (!targetPiece.getColor().equals(color)) {
                        // Enemy piece - can capture
                        moves.add(current);
                        break;
                    } else {
                        // Own piece - cannot move through
                        break;
                    }
                    
                    // For pieces that can only move one square, break after first move
                    if (!canMoveMultipleSquares()) {
                        break;
                    }
                    
                    current = current.add(deltaRow, deltaCol);
                }
            }
        }
        
        return moves;
    }

    /**
     * A wrapper for getPossibleMoves to be used publicly
     */
    public List<Position> getPseudoLegalMoves(Board board) {
        return getPossibleMoves(board);
    }
    
    /**
     * Check if this piece can move in the given direction
     */
    protected boolean canMoveInDirection(Board board, int deltaRow, int deltaCol) {
        Position target = position.add(deltaRow, deltaCol);
        return target != null && target.isValid();
    }
    
    /**
     * Get the move directions for this piece type
     */
    protected abstract int[][] getMoveDirections();
    
    /**
     * Check if this piece can move multiple squares in one direction
     */
    protected abstract boolean canMoveMultipleSquares();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Piece piece = (Piece) obj;
        return color.equals(piece.color) && position.equals(piece.position) && 
               getType().equals(piece.getType());
    }
    
    @Override
    public int hashCode() {
        return 31 * color.hashCode() + position.hashCode() + getType().hashCode();
    }
    
    @Override
    public String toString() {
        return color + " " + getType() + " at " + position;
    }
}