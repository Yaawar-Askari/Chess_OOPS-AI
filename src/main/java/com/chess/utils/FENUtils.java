package com.chess.utils;

import com.chess.model.Board;
import com.chess.model.Piece;
import com.chess.model.Position;
import com.chess.model.pieces.*;

public class FENUtils {

    public static String toFEN(Board board) {
        StringBuilder fen = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            int empty = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece == null) {
                    empty++;
                } else {
                    if (empty > 0) {
                        fen.append(empty);
                        empty = 0;
                    }
                    fen.append(pieceToFENChar(piece));
                }
            }
            if (empty > 0) fen.append(empty);
            if (row < 7) fen.append('/');
        }

        fen.append(' ').append(board.getCurrentTurn().equals("White") ? 'w' : 'b');
        
        StringBuilder castling = new StringBuilder();
        if (board.canCastleKingside("White")) castling.append('K');
        if (board.canCastleQueenside("White")) castling.append('Q');
        if (board.canCastleKingside("Black")) castling.append('k');
        if (board.canCastleQueenside("Black")) castling.append('q');
        fen.append(' ').append(castling.length() > 0 ? castling.toString() : "-");
        
        fen.append(' ').append(board.getEnPassantTarget() != null ? board.getEnPassantTarget().toAlgebraicNotation() : "-");
        fen.append(' ').append(board.getHalfMoveClock());
        fen.append(' ').append(board.getFullMoveNumber());

        return fen.toString();
    }

    public static Board fromFEN(String fen) {
        Board board = new Board();
        board.clear(); // Clear the default initial board
        
        String[] parts = fen.split(" ");
        String piecePlacement = parts[0];
        
        int row = 0, col = 0;
        for (char c : piecePlacement.toCharArray()) {
            if (c == '/') {
                row++;
                col = 0;
            } else if (Character.isDigit(c)) {
                col += Character.getNumericValue(c);
            } else {
                Piece piece = fenCharToPiece(c, new Position(row, col));
                board.setPiece(new Position(row, col), piece);
                col++;
            }
        }
        
        board.setCurrentTurn(parts[1].equals("w") ? "White" : "Black");
        
        String castling = parts[2];
        // This is a simplified setting. A full implementation would need setters in Board.
        // For now, we assume the rights are based on piece positions.
        
        if (!parts[3].equals("-")) {
            board.setEnPassantTarget(new Position(parts[3]));
        }
        
        board.setHalfMoveClock(Integer.parseInt(parts[4]));
        board.setFullMoveNumber(Integer.parseInt(parts[5]));
        
        return board;
    }

    private static char pieceToFENChar(Piece piece) {
        char c = ' ';
        switch (piece.getType()) {
            case "King": c = 'k'; break;
            case "Queen": c = 'q'; break;
            case "Rook": c = 'r'; break;
            case "Bishop": c = 'b'; break;
            case "Knight": c = 'n'; break;
            case "Pawn": c = 'p'; break;
        }
        return piece.getColor().equals("White") ? Character.toUpperCase(c) : c;
    }

    private static Piece fenCharToPiece(char c, Position pos) {
        String color = Character.isUpperCase(c) ? "White" : "Black";
        char type = Character.toLowerCase(c);
        switch (type) {
            case 'k': return new King(color, pos);
            case 'q': return new Queen(color, pos);
            case 'r': return new Rook(color, pos);
            case 'b': return new Bishop(color, pos);
            case 'n': return new Knight(color, pos);
            case 'p': return new Pawn(color, pos);
            default: throw new IllegalArgumentException("Unknown FEN character: " + c);
        }
    }
} 