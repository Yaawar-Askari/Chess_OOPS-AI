package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class RookAttackDebug {
    public static void main(String[] args) {
        System.out.println("=== Debugging rook attack moves ===");
        
        Board board = new Board();
        board.clear();
        
        // White king at e1, Black rook at e8
        board.setPiece(new Position(7, 4), new King("White", new Position(7, 4)));
        board.setPiece(new Position(0, 4), new Rook("Black", new Position(0, 4)));
        
        Rook rook = (Rook) board.getPiece(new Position(0, 4));
        System.out.println("Rook: " + rook);
        
        // Get rook's attack moves
        var attackMoves = rook.getAttackMoves(board);
        System.out.println("Rook attack moves: " + attackMoves);
        System.out.println("Number of attack moves: " + attackMoves.size());
        
        // Check if d1 is in the attack moves
        Position d1 = new Position(7, 3);
        boolean d1InAttacks = attackMoves.contains(d1);
        System.out.println("d1 in attack moves: " + d1InAttacks);
        
        // Also get possible moves for comparison
        var possibleMoves = rook.getPossibleMoves(board);
        System.out.println("Rook possible moves: " + possibleMoves);
        System.out.println("d1 in possible moves: " + possibleMoves.contains(d1));
        
        // Let's also check what squares the rook can see
        System.out.println("\n=== Debugging rook move generation ===");
        for (Position move : possibleMoves) {
            System.out.println("Rook can move to: " + move);
        }
    }
}
