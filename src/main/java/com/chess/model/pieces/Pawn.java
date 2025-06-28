package com.chess.model.pieces;

import com.chess.model.Board;
import com.chess.model.Piece;
import com.chess.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Pawn piece in chess
 */
public class Pawn extends Piece {
    
    public Pawn(String color, Position position) {
        super(color, position);
    }
    
    @Override
    public String getType() {
        return "Pawn";
    }
    
    @Override
    public List<Position> getAttackMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        int direction = color.equals("White") ? -1 : 1;
        
        // Pawns attack diagonally
        // Left diagonal
        Position leftDiagonal = position.add(direction, -1);
        if (leftDiagonal != null && leftDiagonal.isValid()) {
            Piece targetPiece = board.getPiece(leftDiagonal);
            if (targetPiece != null && !targetPiece.getColor().equals(color)) {
                moves.add(leftDiagonal);
            }
        }
        
        // Right diagonal
        Position rightDiagonal = position.add(direction, 1);
        if (rightDiagonal != null && rightDiagonal.isValid()) {
            Piece targetPiece = board.getPiece(rightDiagonal);
            if (targetPiece != null && !targetPiece.getColor().equals(color)) {
                moves.add(rightDiagonal);
            }
        }
        
        return moves;
    }
    
    @Override
    public int getValue() {
        return 100;
    }
      @Override
    public String getSymbol() {
        return color.equals("White") ? "P" : "p";
    }
    
    @Override
    protected int[][] getMoveDirections() {
        // Pawns have special movement rules, so we override getPossibleMoves
        return new int[0][0];
    }
    
    @Override
    protected boolean canMoveMultipleSquares() {
        return false;
    }
    
    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        
        int direction = color.equals("White") ? -1 : 1;
        int startRow = color.equals("White") ? 6 : 1;
        
        // Forward move (one square)
        Position oneForward = position.add(direction, 0);
        if (oneForward != null && oneForward.isValid() && board.getPiece(oneForward) == null) {
            moves.add(oneForward);
            
            // Double move from starting position
            if (position.getRow() == startRow) {
                Position twoForward = position.add(2 * direction, 0);
                if (twoForward != null && twoForward.isValid() && board.getPiece(twoForward) == null) {
                    moves.add(twoForward);
                }
            }
        }
        
        // Diagonal captures
        Position leftDiagonal = position.add(direction, -1);
        if (leftDiagonal != null && leftDiagonal.isValid()) {
            Piece targetPiece = board.getPiece(leftDiagonal);
            if (targetPiece != null && !targetPiece.getColor().equals(color)) {
                moves.add(leftDiagonal);
            }
        }
        
        Position rightDiagonal = position.add(direction, 1);
        if (rightDiagonal != null && rightDiagonal.isValid()) {
            Piece targetPiece = board.getPiece(rightDiagonal);
            if (targetPiece != null && !targetPiece.getColor().equals(color)) {
                moves.add(rightDiagonal);
            }
        }
        
        // Add en passant moves
        moves.addAll(getEnPassantMoves(board));
        
        return moves;
    }
    
    /**
     * Get en passant moves if they are legal
     */
    private List<Position> getEnPassantMoves(Board board) {
        List<Position> enPassantMoves = new ArrayList<>();
        
        // Check if en passant is possible
        Position enPassantTarget = board.getEnPassantTarget();
        if (enPassantTarget == null) {
            return enPassantMoves;
        }
        
        int direction = color.equals("White") ? -1 : 1;
        
        // Check if this pawn is adjacent to the en passant target
        Position leftCapture = position.add(direction, -1);
        Position rightCapture = position.add(direction, 1);
        
        if ((leftCapture != null && enPassantTarget.equals(leftCapture)) || 
            (rightCapture != null && enPassantTarget.equals(rightCapture))) {
            enPassantMoves.add(enPassantTarget);
        }
        
        return enPassantMoves;
    }
    
    /**
     * Check if this pawn can be promoted
     */
    public boolean canPromote() {
        int promotionRow = color.equals("White") ? 0 : 7;
        return position.getRow() == promotionRow;
    }
    
    /**
     * Get the promotion row for this pawn
     */
    public int getPromotionRow() {
        return color.equals("White") ? 0 : 7;
    }
} 