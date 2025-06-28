package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class TestIntentionDebug {
    public static void main(String[] args) {
        System.out.println("=== Debugging test intention ===");
        
        Board board = new Board();
        board.clear();
        
        // White king at e1, Black rook at e8
        board.setPiece(new Position(7, 4), new King("White", new Position(7, 4)));
        board.setPiece(new Position(0, 4), new Rook("Black", new Position(0, 4)));
        board.setCurrentTurn("White");
        
        Piece king = board.getPiece(new Position(7, 4));
        
        // Test different moves
        System.out.println("King at e1, Rook at e8");
        
        // Move to d1 (actual test move)
        Position d1 = new Position(7, 3);
        Move moveToD1 = new Move(new Position(7, 4), d1, king, null);
        boolean d1Valid = board.isValidMove(moveToD1);
        boolean d1UnderAttack = board.isSquareUnderAttack(d1, "White");
        System.out.println("Move to d1: valid=" + d1Valid + ", under attack=" + d1UnderAttack);
        
        // Move to e2 (staying on same file as rook)
        Position e2 = new Position(6, 4);
        Move moveToE2 = new Move(new Position(7, 4), e2, king, null);
        boolean e2Valid = board.isValidMove(moveToE2);
        boolean e2UnderAttack = board.isSquareUnderAttack(e2, "White");
        System.out.println("Move to e2: valid=" + e2Valid + ", under attack=" + e2UnderAttack);
        
        // Move to f1 (test says this should be valid)
        Position f1 = new Position(7, 5);
        Move moveToF1 = new Move(new Position(7, 4), f1, king, null);
        boolean f1Valid = board.isValidMove(moveToF1);
        boolean f1UnderAttack = board.isSquareUnderAttack(f1, "White");
        System.out.println("Move to f1: valid=" + f1Valid + ", under attack=" + f1UnderAttack);
        
        System.out.println("\nPosition analysis:");
        System.out.println("d1 = row 7, col 3 (d-file)");
        System.out.println("e1 = row 7, col 4 (e-file) - king position");
        System.out.println("e8 = row 0, col 4 (e-file) - rook position");
        System.out.println("f1 = row 7, col 5 (f-file)");
    }
}
