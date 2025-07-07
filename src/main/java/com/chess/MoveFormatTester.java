package com.chess;

import com.chess.model.Position;
import com.chess.model.Move;
import com.chess.model.pieces.Pawn;

public class MoveFormatTester {
    public static void main(String[] args) {
        System.out.println("Testing move format...");
        
        // Test a simple pawn move from e2 to e4
        Position from = new Position("e2");
        Position to = new Position("e4");
        Pawn pawn = new Pawn("White", from);
        Move move = new Move(from, to, pawn, null);
        
        String moveString = move.toSimpleString();
        System.out.println("Move string: " + moveString);
        
        // Test parsing the move string back
        try {
            Position parsedFrom = new Position(moveString.substring(0, 2));
            Position parsedTo = new Position(moveString.substring(2, 4));
            
            System.out.println("Original from: " + from + ", parsed from: " + parsedFrom);
            System.out.println("Original to: " + to + ", parsed to: " + parsedTo);
            
            if (from.equals(parsedFrom) && to.equals(parsedTo)) {
                System.out.println("✓ Move format test PASSED");
            } else {
                System.out.println("✗ Move format test FAILED");
            }
        } catch (Exception e) {
            System.out.println("✗ Move format test FAILED with exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test position format
        System.out.println("\nTesting position format...");
        Position pos = new Position("e2");
        System.out.println("Position e2 toString: " + pos.toString());
        System.out.println("Position e2 toAlgebraicNotation: " + pos.toAlgebraicNotation());
        
        Position pos2 = new Position("h8");
        System.out.println("Position h8 toString: " + pos2.toString());
        System.out.println("Position h8 toAlgebraicNotation: " + pos2.toAlgebraicNotation());
    }
} 