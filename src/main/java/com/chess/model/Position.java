package com.chess.model;

/**
 * Represents a position on the chess board
 */
public class Position {
    private final int row;
    private final int col;
    
    public Position(int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            throw new IllegalArgumentException("Position must be within board bounds (0-7)");
        }
        this.row = row;
        this.col = col;
    }
    
    public Position(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Invalid chess notation: " + notation);
        }
        
        char file = notation.charAt(0);
        char rank = notation.charAt(1);
        
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Invalid chess notation: " + notation);
        }
        
        this.col = file - 'a';
        this.row = 8 - (rank - '0');
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public boolean isValid() {
        return row >= 0 && row <= 7 && col >= 0 && col <= 7;
    }
    
    public Position add(int deltaRow, int deltaCol) {
        int newRow = row + deltaRow;
        int newCol = col + deltaCol;
        
        // Check if the new position is within bounds
        if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7) {
            return null; // Return null for invalid positions
        }
        
        return new Position(newRow, newCol);
    }
    
    public Position add(Position delta) {
        return add(delta.row, delta.col);
    }
    
    public int distanceTo(Position other) {
        return Math.max(Math.abs(row - other.row), Math.abs(col - other.col));
    }
    
    public boolean isAdjacent(Position other) {
        return distanceTo(other) == 1;
    }
    
    public boolean isOnSameRow(Position other) {
        return row == other.row;
    }
    
    public boolean isOnSameCol(Position other) {
        return col == other.col;
    }
    
    public boolean isOnSameDiagonal(Position other) {
        return Math.abs(row - other.row) == Math.abs(col - other.col);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return row == position.row && col == position.col;
    }
    
    @Override
    public int hashCode() {
        return 31 * row + col;
    }
    
    @Override
    public String toString() {
        char file = (char) ('a' + col);
        char rank = (char) ('8' - row);
        return String.valueOf(file) + rank;
    }
    
    public String toAlgebraicNotation() {
        return toString();
    }
} 