package com.chess.debug;

import com.chess.model.*;
import com.chess.model.pieces.*;

public class TestFailuresDebug {
    public static void main(String[] args) {
        System.out.println("=== Testing remaining failures ===\n");
        
        // Test 1: King in check prevention
        testKingInCheckPrevention();
        
        // Test 2: En passant capture
        testEnPassantCapture();
        
        // Test 3: Checkmate detection
        testCheckmateDetection();
        
        // Test 4: Stalemate detection
        testStalemateDetection();
        
        // Test 5: Game ending conditions
        testGameEndingConditions();
    }
    
    static void testKingInCheckPrevention() {
        System.out.println("=== Test: King in check prevention ===");
        Board board = new Board();
        board.clear();
        
        // White king at e1, Black rook at e8
        board.setPiece(new Position(7, 4), new King("White", new Position(7, 4)));
        board.setPiece(new Position(0, 4), new Rook("Black", new Position(0, 4)));
        board.setCurrentTurn("White");
        
        Piece king = board.getPiece(new Position(7, 4));
        
        // Test: King cannot move to e2 (expected: false, test expects: false)
        Move invalidMove = new Move(new Position(7, 4), new Position(6, 4), king, null);
        boolean result1 = board.isValidMove(invalidMove);
        System.out.println("King e1 to e2 valid: " + result1 + " (test expects: false)");
        
        // Test: King can move to f1 (expected: true, test expects: true)
        Move validMove = new Move(new Position(7, 4), new Position(7, 5), king, null);
        boolean result2 = board.isValidMove(validMove);
        System.out.println("King e1 to f1 valid: " + result2 + " (test expects: true)");
        
        // Analysis
        System.out.println("King currently in check: " + board.isInCheck("White"));
        System.out.println("e2 under attack: " + board.isSquareUnderAttack(new Position(6, 4), "White"));
        System.out.println("f1 under attack: " + board.isSquareUnderAttack(new Position(7, 5), "White"));
        
        if (result1 != false) {
            System.out.println("ISSUE: Test expects king e1->e2 to be invalid, but it's valid");
            System.out.println("   Analysis: King at e1 is in check from rook at e8.");
            System.out.println("   Moving to e2 would still leave king on same file as rook.");
            System.out.println("   e2 should be under attack from the rook. Move should be invalid.");
        }
        System.out.println();
    }
    
    static void testEnPassantCapture() {
        System.out.println("=== Test: En passant capture ===");
        Board board = new Board();
        board.clear();
        
        // White pawn at e5, Black pawn at f5 (simulating f7-f5 double move)
        board.setPiece(new Position(3, 4), new Pawn("White", new Position(3, 4)));
        board.setPiece(new Position(3, 5), new Pawn("Black", new Position(3, 5)));
        
        // Set en passant target
        board.setEnPassantTarget(new Position(2, 5)); // f6
        board.setCurrentTurn("White");
        
        Piece whitePawn = board.getPiece(new Position(3, 4));
        Move enPassantMove = new Move(new Position(3, 4), new Position(2, 5), whitePawn, null,
                                    Move.MoveType.EN_PASSANT, null, false, false);
        
        boolean result = board.isValidMove(enPassantMove);
        System.out.println("En passant e5->f6 valid: " + result + " (test expects: true)");
        
        if (!result) {
            System.out.println("ISSUE: En passant should be valid but returns false");
            System.out.println("   En passant target: " + board.getEnPassantTarget());
            System.out.println("   Move type: " + enPassantMove.getType());
        }
        System.out.println();
    }
    
    static void testCheckmateDetection() {
        System.out.println("=== Test: Checkmate detection ===");
        Board board = new Board();
        board.clear();
        
        // White king at h1, Black rook at a1, Black rook at h8
        board.setPiece(new Position(7, 7), new King("White", new Position(7, 7))); // h1
        board.setPiece(new Position(7, 0), new Rook("Black", new Position(7, 0))); // a1
        board.setPiece(new Position(0, 7), new Rook("Black", new Position(0, 7))); // h8
        
        // White pawns blocking escape
        board.setPiece(new Position(6, 6), new Pawn("White", new Position(6, 6))); // g2
        board.setPiece(new Position(6, 7), new Pawn("White", new Position(6, 7))); // h2
        
        board.setCurrentTurn("White");
        
        boolean inCheck = board.isInCheck("White");
        boolean isCheckmate = board.isCheckmate("White");
        
        System.out.println("King in check: " + inCheck + " (test expects: true)");
        System.out.println("Is checkmate: " + isCheckmate + " (test expects: true)");
        
        if (!inCheck || !isCheckmate) {
            System.out.println("ISSUE: Should be checkmate");
            
            // Debug king's possible moves
            Piece king = board.getPiece(new Position(7, 7));
            var kingMoves = king.getPossibleMoves(board);
            System.out.println("   King possible moves: " + kingMoves);
            
            // Check each possible move
            for (Position move : kingMoves) {
                boolean underAttack = board.isSquareUnderAttack(move, "White");
                System.out.println("   " + move + " under attack: " + underAttack);
            }
        }
        System.out.println();
    }
    
    static void testStalemateDetection() {
        System.out.println("=== Test: Stalemate detection ===");
        Board board = new Board();
        board.clear();
        
        // White king at a1, Black king at c3, Black queen at b3
        board.setPiece(new Position(7, 0), new King("White", new Position(7, 0))); // a1
        board.setPiece(new Position(5, 2), new King("Black", new Position(5, 2))); // c3
        board.setPiece(new Position(5, 1), new Queen("Black", new Position(5, 1))); // b3
        
        board.setCurrentTurn("White");
        
        boolean inCheck = board.isInCheck("White");
        boolean isStalemate = board.isStalemate("White");
        
        System.out.println("King in check: " + inCheck + " (test expects: false)");
        System.out.println("Is stalemate: " + isStalemate + " (test expects: true)");
        
        if (inCheck || !isStalemate) {
            System.out.println("ISSUE: Should be stalemate (not in check but no legal moves)");
            
            // Debug king's possible moves
            Piece king = board.getPiece(new Position(7, 0));
            var kingMoves = king.getPossibleMoves(board);
            System.out.println("   King possible moves: " + kingMoves);
            
            // Check each possible move
            for (Position move : kingMoves) {
                boolean underAttack = board.isSquareUnderAttack(move, "White");
                System.out.println("   " + move + " under attack: " + underAttack);
            }
            
            // Check if king has any legal moves
            boolean hasLegalMoves = false;
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = board.getPiece(new Position(row, col));
                    if (piece != null && piece.getColor().equals("White")) {
                        var validMoves = piece.getValidMoves(board);
                        if (!validMoves.isEmpty()) {
                            hasLegalMoves = true;
                            System.out.println("   " + piece + " has legal moves: " + validMoves);
                        }
                    }
                }
            }
            System.out.println("   Has legal moves: " + hasLegalMoves);
        }
        System.out.println();
    }
    
    static void testGameEndingConditions() {
        System.out.println("=== Test: Game ending conditions ===");
        Board board = new Board();
        board.clear();
        
        // Test checkmate detection through game ending conditions
        // Set up scenario where White just delivered checkmate to Black
        board.setPiece(new Position(0, 7), new King("Black", new Position(0, 7))); // h8
        board.setPiece(new Position(1, 6), new Pawn("Black", new Position(1, 6))); // g7
        board.setPiece(new Position(1, 7), new Pawn("Black", new Position(1, 7))); // h7
        board.setPiece(new Position(0, 0), new Rook("White", new Position(0, 0))); // a8 - delivers check
        board.setPiece(new Position(7, 7), new King("White", new Position(7, 7))); // h1
        
        // White just moved and now it's Black's turn, but Black is in checkmate
        board.setCurrentTurn("Black");
        
        String result = board.checkGameEndingConditions();
        System.out.println("Game ending result: " + result + " (test expects: CHECKMATE_WHITE)");
        
        if (!"CHECKMATE_WHITE".equals(result)) {
            System.out.println("ISSUE: Should detect checkmate, White wins");
            System.out.println("   Current turn: " + board.getCurrentTurn());
            System.out.println("   Black in check: " + board.isInCheck("Black"));
            System.out.println("   Black in checkmate: " + board.isCheckmate("Black"));
        }
        System.out.println();
    }
}
