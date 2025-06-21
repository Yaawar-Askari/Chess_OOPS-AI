package com.chess.model;

/**
 * Represents a chess move
 */
public class Move {
    private final Position from;
    private final Position to;
    private final Piece piece;
    private final Piece capturedPiece;
    private final MoveType type;
    private final String promotionPiece;
    private final boolean isCheck;
    private final boolean isCheckmate;
    private final String algebraicNotation;
    
    public enum MoveType {
        NORMAL,
        CASTLE_KINGSIDE,
        CASTLE_QUEENSIDE,
        EN_PASSANT,
        PAWN_PROMOTION
    }
    
    public Move(Position from, Position to, Piece piece, Piece capturedPiece) {
        this(from, to, piece, capturedPiece, MoveType.NORMAL, null, false, false);
    }
    
    public Move(Position from, Position to, Piece piece, Piece capturedPiece, 
                MoveType type, String promotionPiece, boolean isCheck, boolean isCheckmate) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
        this.type = type;
        this.promotionPiece = promotionPiece;
        this.isCheck = isCheck;
        this.isCheckmate = isCheckmate;
        this.algebraicNotation = generateAlgebraicNotation();
    }
    
    public Position getFrom() {
        return from;
    }
    
    public Position getTo() {
        return to;
    }
    
    public Piece getPiece() {
        return piece;
    }
    
    public Piece getCapturedPiece() {
        return capturedPiece;
    }
    
    public MoveType getType() {
        return type;
    }
    
    public String getPromotionPiece() {
        return promotionPiece;
    }
    
    public boolean isCheck() {
        return isCheck;
    }
    
    public boolean isCheckmate() {
        return isCheckmate;
    }
    
    public String getAlgebraicNotation() {
        return algebraicNotation;
    }
    
    private String generateAlgebraicNotation() {
        StringBuilder notation = new StringBuilder();
        
        // Handle castling
        if (type == MoveType.CASTLE_KINGSIDE) {
            return "O-O";
        } else if (type == MoveType.CASTLE_QUEENSIDE) {
            return "O-O-O";
        }
        
        // Piece symbol (except for pawns)
        if (!piece.getType().equals("Pawn")) {
            notation.append(getPieceSymbol(piece.getType()));
        }
        
        // Capture indicator
        if (capturedPiece != null) {
            if (piece.getType().equals("Pawn")) {
                notation.append(from.toString().charAt(0)); // File
            }
            notation.append("x");
        }
        
        // Destination square
        notation.append(to.toString());
        
        // Promotion
        if (type == MoveType.PAWN_PROMOTION && promotionPiece != null) {
            notation.append("=").append(getPieceSymbol(promotionPiece));
        }
        
        // Check/Checkmate
        if (isCheckmate) {
            notation.append("#");
        } else if (isCheck) {
            notation.append("+");
        }
        
        return notation.toString();
    }
    
    private String getPieceSymbol(String pieceType) {
        switch (pieceType) {
            case "King": return "K";
            case "Queen": return "Q";
            case "Rook": return "R";
            case "Bishop": return "B";
            case "Knight": return "N";
            case "Pawn": return "";
            default: return "";
        }
    }
    
    public boolean isCapture() {
        return capturedPiece != null;
    }
    
    public boolean isCastling() {
        return type == MoveType.CASTLE_KINGSIDE || type == MoveType.CASTLE_QUEENSIDE;
    }
    
    public boolean isEnPassant() {
        return type == MoveType.EN_PASSANT;
    }
    
    public boolean isPromotion() {
        return type == MoveType.PAWN_PROMOTION;
    }
    
    @Override
    public String toString() {
        return algebraicNotation;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return from.equals(move.from) && to.equals(move.to) && 
               piece.equals(move.piece) && type == move.type;
    }
    
    @Override
    public int hashCode() {
        return 31 * from.hashCode() + to.hashCode();
    }

    public String toSimpleString() {
        return from.toAlgebraicNotation() + to.toAlgebraicNotation();
    }
} 