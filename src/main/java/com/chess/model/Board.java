package com.chess.model;

import com.chess.model.pieces.*;
import com.chess.utils.Logger;
import com.chess.utils.FENUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chess board and manages game state
 */
public class Board {
    private static final Logger logger = Logger.getLogger(Board.class);
    
    private Piece[][] grid;
    private String currentTurn;
    private Position enPassantTarget;
    private boolean whiteKingsideCastle;
    private boolean whiteQueensideCastle;
    private boolean blackKingsideCastle;
    private boolean blackQueensideCastle;
    private List<Move> moveHistory;
    private int halfMoveClock;
    private int fullMoveNumber;
    private List<Piece> whiteCapturedPieces;
    private List<Piece> blackCapturedPieces;
    
    public Board() {
        this.grid = new Piece[8][8];
        this.currentTurn = "White";
        this.moveHistory = new ArrayList<>();
        this.halfMoveClock = 0;
        this.fullMoveNumber = 1;
        this.whiteCapturedPieces = new ArrayList<>();
        this.blackCapturedPieces = new ArrayList<>();
        initializeBoard();
    }
    
    /**
     * Initialize the board with pieces in their starting positions
     */
    private void initializeBoard() {
        // Initialize pawns
        for (int col = 0; col < 8; col++) {
            grid[6][col] = new Pawn("White", new Position(6, col));
            grid[1][col] = new Pawn("Black", new Position(1, col));
        }
        
        // Initialize other pieces
        // White pieces
        grid[7][0] = new Rook("White", new Position(7, 0));
        grid[7][1] = new Knight("White", new Position(7, 1));
        grid[7][2] = new Bishop("White", new Position(7, 2));
        grid[7][3] = new Queen("White", new Position(7, 3));
        grid[7][4] = new King("White", new Position(7, 4));
        grid[7][5] = new Bishop("White", new Position(7, 5));
        grid[7][6] = new Knight("White", new Position(7, 6));
        grid[7][7] = new Rook("White", new Position(7, 7));
        
        // Black pieces
        grid[0][0] = new Rook("Black", new Position(0, 0));
        grid[0][1] = new Knight("Black", new Position(0, 1));
        grid[0][2] = new Bishop("Black", new Position(0, 2));
        grid[0][3] = new Queen("Black", new Position(0, 3));
        grid[0][4] = new King("Black", new Position(0, 4));
        grid[0][5] = new Bishop("Black", new Position(0, 5));
        grid[0][6] = new Knight("Black", new Position(0, 6));
        grid[0][7] = new Rook("Black", new Position(0, 7));
        
        // Initialize castling rights
        whiteKingsideCastle = true;
        whiteQueensideCastle = true;
        blackKingsideCastle = true;
        blackQueensideCastle = true;
    }
    
    public Piece getPiece(Position position) {
        if (!position.isValid()) {
            return null;
        }
        return grid[position.getRow()][position.getCol()];
    }
    
    public void move(Position from, Position to) {
        Piece piece = getPiece(from);
        setPiece(to, piece);
        setPiece(from, null);
    }
    
    public void setPiece(Position position, Piece piece) {
        if (position.isValid()) {
            grid[position.getRow()][position.getCol()] = piece;
            if (piece != null) {
                piece.setPosition(position);
            }
        }
    }
    
    public String getCurrentTurn() {
        return currentTurn;
    }
    
    public void setCurrentTurn(String currentTurn) {
        this.currentTurn = currentTurn;
    }
    
    public Position getEnPassantTarget() {
        return enPassantTarget;
    }
    
    public void setEnPassantTarget(Position enPassantTarget) {
        this.enPassantTarget = enPassantTarget;
    }
    
    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }
    
    /**
     * Get move history in algebraic notation
     */
    public List<String> getMoveHistoryInAlgebraicNotation() {
        List<String> algebraicMoves = new ArrayList<>();
        for (Move move : moveHistory) {
            algebraicMoves.add(move.getAlgebraicNotation());
        }
        return algebraicMoves;
    }
    
    /**
     * Set the move history (used when loading games)
     */
    public void setMoveHistory(List<Move> moves) {
        this.moveHistory = new ArrayList<>(moves);
    }
    
    public int getHalfMoveClock() {
        return halfMoveClock;
    }
    
    public int getFullMoveNumber() {
        return fullMoveNumber;
    }
    
    /**
     * Make a move on the board
     */
    public boolean makeMove(Move move) {
        if (!isValidMove(move)) {
            logger.warn("Invalid move attempted: " + move);
            return false;
        }
        
        // Check for capture
        Piece capturedPiece = getPiece(move.getTo());
        if (capturedPiece != null) {
            if (capturedPiece.getColor().equals("White")) {
                blackCapturedPieces.add(capturedPiece);
            } else {
                whiteCapturedPieces.add(capturedPiece);
            }
        }
        
        // Store the move
        moveHistory.add(move);
        
        // Update half-move clock
        if (move.isCapture() || move.getPiece().getType().equals("Pawn")) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
        
        // Update full move number
        if (currentTurn.equals("Black")) {
            fullMoveNumber++;
        }
        
        // Execute the move
        executeMove(move);
        
        // Update castling rights
        updateCastlingRights(move);
        
        // Update en passant target
        updateEnPassantTarget(move);
        
        // Switch turns
        currentTurn = currentTurn.equals("White") ? "Black" : "White";
        
        return true;
    }
      /**
     * Check if a move is valid - centralized validation logic
     */
    public boolean isValidMove(Move move) {
        Piece piece = move.getPiece();
        Position from = move.getFrom();
        Position to = move.getTo();
        
        // Basic validation
        if (piece == null || !from.isValid() || !to.isValid()) {
            return false;
        }
        
        // Check if it's the correct player's turn
        if (!piece.getColor().equals(currentTurn)) {
            return false;
        }
        
        // Check if destination is the same as source
        if (from.equals(to)) {
            return false;
        }
        
        // Check if destination has friendly piece
        Piece destinationPiece = getPiece(to);
        if (destinationPiece != null && destinationPiece.getColor().equals(piece.getColor())) {
            return false;
        }
        
        // Check if the move is pseudo-legal for the piece type
        if (!isPseudoLegalMove(piece, from, to)) {
            return false;
        }
        
        // Handle special move types
        if (!isSpecialMoveValid(move)) {
            return false;
        }
        
        // Final check: ensure move doesn't leave own king in check
        return !wouldLeaveKingInCheck(move);
    }
    
    /**
     * Check if a move is pseudo-legal (ignoring check considerations)
     */
    private boolean isPseudoLegalMove(Piece piece, Position from, Position to) {
        // Special handling for castling moves
        if (piece.getType().equals("King")) {
            int colDiff = to.getCol() - from.getCol();
            if (Math.abs(colDiff) == 2) {
                // This is a castling move - check if castling is possible
                if (colDiff > 0) {
                    return canCastleKingside(piece.getColor());
                } else {
                    return canCastleQueenside(piece.getColor());
                }
            }
        }
        
        List<Position> possibleMoves = piece.getPossibleMoves(this);
        return possibleMoves.contains(to);
    }
    
    /**
     * Validate special move types (castling, en passant, promotion)
     */
    private boolean isSpecialMoveValid(Move move) {
        switch (move.getType()) {
            case CASTLE_KINGSIDE:
                return canCastleKingside(move.getPiece().getColor());
            case CASTLE_QUEENSIDE:
                return canCastleQueenside(move.getPiece().getColor());
            case EN_PASSANT:
                return isEnPassantValid(move);
            case PAWN_PROMOTION:
                return isPawnPromotionValid(move);
            default:
                return true; // Normal moves
        }
    }
    
    /**
     * Check if en passant move is valid
     */
    private boolean isEnPassantValid(Move move) {
        if (enPassantTarget == null) {
            return false;
        }
        
        Position to = move.getTo();
        Piece piece = move.getPiece();
        
        // Must be a pawn move to the en passant target square
        return piece.getType().equals("Pawn") && to.equals(enPassantTarget);
    }
    
    /**
     * Check if pawn promotion is valid
     */
    private boolean isPawnPromotionValid(Move move) {
        Piece piece = move.getPiece();
        Position to = move.getTo();
        
        if (!piece.getType().equals("Pawn")) {
            return false;
        }
        
        // Check if pawn reaches promotion rank
        if (piece.getColor().equals("White")) {
            return to.getRow() == 0;
        } else {
            return to.getRow() == 7;
        }
    }
      /**
     * Check if making this move would leave own king in check
     */
    private boolean wouldLeaveKingInCheck(Move move) {
        // Create a copy of the board and make the move
        Board tempBoard = this.clone();
        
        // Create a new move with the cloned piece
        Piece clonedPiece = tempBoard.getPiece(move.getFrom());
        Move clonedMove = new Move(move.getFrom(), move.getTo(), clonedPiece, 
                                   move.getCapturedPiece(), move.getType(), 
                                   move.getPromotionPiece(), false, false);
        
        // Execute the move on the temporary board
        tempBoard.executeMoveUnsafe(clonedMove);
        
        // Check if own king is in check
        return tempBoard.isInCheck(move.getPiece().getColor());
    }
    
    /**
     * Execute a move without validation (for internal use)
     */
    private void executeMoveUnsafe(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = move.getPiece();
        
        // Handle special moves
        switch (move.getType()) {
            case CASTLE_KINGSIDE:
                executeKingsideCastle(piece.getColor());
                break;
            case CASTLE_QUEENSIDE:
                executeQueensideCastle(piece.getColor());
                break;
            case EN_PASSANT:
                executeEnPassant(move);
                break;
            case PAWN_PROMOTION:
                executePawnPromotion(move);
                break;
            default:
                // Regular move
                setPiece(to, piece);
                setPiece(from, null);
                break;
        }
    }
    
    /**
     * Execute a move on the board
     */
    private void executeMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = move.getPiece();
        
        // Handle special moves
        switch (move.getType()) {
            case CASTLE_KINGSIDE:
                executeKingsideCastle(piece.getColor());
                break;
            case CASTLE_QUEENSIDE:
                executeQueensideCastle(piece.getColor());
                break;
            case EN_PASSANT:
                executeEnPassant(move);
                break;
            case PAWN_PROMOTION:
                executePawnPromotion(move);
                break;
            default:
                // Regular move
                setPiece(to, piece);
                setPiece(from, null);
                break;
        }
    }
    
    /**
     * Execute kingside castling
     */
    private void executeKingsideCastle(String color) {
        int row = color.equals("White") ? 7 : 0;
        
        // Move king
        Position kingFrom = new Position(row, 4);
        Position kingTo = new Position(row, 6);
        Piece king = getPiece(kingFrom);
        setPiece(kingTo, king);
        setPiece(kingFrom, null);
        
        // Move rook
        Position rookFrom = new Position(row, 7);
        Position rookTo = new Position(row, 5);
        Piece rook = getPiece(rookFrom);
        setPiece(rookTo, rook);
        setPiece(rookFrom, null);
    }
    
    /**
     * Execute queenside castling
     */
    private void executeQueensideCastle(String color) {
        int row = color.equals("White") ? 7 : 0;
        
        // Move king
        Position kingFrom = new Position(row, 4);
        Position kingTo = new Position(row, 2);
        Piece king = getPiece(kingFrom);
        setPiece(kingTo, king);
        setPiece(kingFrom, null);
        
        // Move rook
        Position rookFrom = new Position(row, 0);
        Position rookTo = new Position(row, 3);
        Piece rook = getPiece(rookFrom);
        setPiece(rookTo, rook);
        setPiece(rookFrom, null);
    }
    
    /**
     * Execute en passant capture
     */
    private void executeEnPassant(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = move.getPiece();
        
        // Move the pawn
        setPiece(to, piece);
        setPiece(from, null);
        
        // Remove the captured pawn
        int capturedRow = from.getRow();
        int capturedCol = to.getCol();
        setPiece(new Position(capturedRow, capturedCol), null);
    }
    
    /**
     * Execute pawn promotion
     */
    private void executePawnPromotion(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        String color = move.getPiece().getColor();
        String promotionType = move.getPromotionPiece();
        
        // Create the promoted piece
        Piece promotedPiece = createPiece(promotionType, color, to);
        setPiece(to, promotedPiece);
        setPiece(from, null);
    }
    
    /**
     * Create a piece of the specified type
     */
    private Piece createPiece(String type, String color, Position position) {
        switch (type) {
            case "Queen": return new Queen(color, position);
            case "Rook": return new Rook(color, position);
            case "Bishop": return new Bishop(color, position);
            case "Knight": return new Knight(color, position);
            default: return new Queen(color, position); // Default to queen
        }
    }
    
    /**
     * Update castling rights after a move
     */
    private void updateCastlingRights(Move move) {
        Piece piece = move.getPiece();
        String color = piece.getColor();
        
        if (piece.getType().equals("King")) {
            if (color.equals("White")) {
                whiteKingsideCastle = false;
                whiteQueensideCastle = false;
            } else {
                blackKingsideCastle = false;
                blackQueensideCastle = false;
            }
        } else if (piece.getType().equals("Rook")) {
            if (color.equals("White")) {
                if (move.getFrom().getCol() == 0) {
                    whiteQueensideCastle = false;
                } else if (move.getFrom().getCol() == 7) {
                    whiteKingsideCastle = false;
                }
            } else {
                if (move.getFrom().getCol() == 0) {
                    blackQueensideCastle = false;
                } else if (move.getFrom().getCol() == 7) {
                    blackKingsideCastle = false;
                }
            }
        }
    }
    
    /**
     * Update en passant target after a move
     */
    private void updateEnPassantTarget(Move move) {
        Piece piece = move.getPiece();
        
        if (piece.getType().equals("Pawn")) {
            int fromRow = move.getFrom().getRow();
            int toRow = move.getTo().getRow();
            
            // Check if it's a double pawn move
            if (Math.abs(fromRow - toRow) == 2) {
                int enPassantRow = (fromRow + toRow) / 2;
                enPassantTarget = new Position(enPassantRow, move.getTo().getCol());
            } else {
                enPassantTarget = null;
            }
        } else {
            enPassantTarget = null;
        }
    }
    
    /**
     * Check if a king is in check
     */
    public boolean isInCheck(String color) {
        Position kingPosition = findKing(color);
        if (kingPosition == null) {
            return false;
        }
        
        return isSquareUnderAttack(kingPosition, color);
    }
    
    /**
     * Check if a square is under attack by the opponent
     */
    public boolean isSquareUnderAttack(Position square, String defendingColor) {
        String attackingColor = defendingColor.equals("White") ? "Black" : "White";
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getColor().equals(attackingColor)) {
                    List<Position> attackMoves = piece.getAttackMoves(this);
                    if (attackMoves.contains(square)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Find the position of a king
     */
    private Position findKing(String color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getType().equals("King") && 
                    piece.getColor().equals(color)) {
                    return piece.getPosition();
                }
            }
        }
        return null;
    }
    
    /**
     * Check if the game is in checkmate
     */
    public boolean isCheckmate(String color) {
        if (!isInCheck(color)) {
            return false;
        }
        
        // Check if any legal move can get out of check
        return !hasLegalMoves(color);
    }
    
    /**
     * Check if the game is in stalemate
     */
    public boolean isStalemate(String color) {
        if (isInCheck(color)) {
            return false;
        }
        
        return !hasLegalMoves(color);
    }
    
    /**
     * Check if a player has any legal moves
     */
    private boolean hasLegalMoves(String color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getColor().equals(color)) {
                    List<Position> validMoves = piece.getValidMoves(this);
                    if (!validMoves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Get all valid moves for a player
     */
    public List<Move> getAllValidMoves(String color) {
        List<Move> allMoves = new ArrayList<>();
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];
                if (piece != null && piece.getColor().equals(color)) {
                    List<Position> validPositions = piece.getValidMoves(this);
                    for (Position to : validPositions) {
                        Move move = new Move(piece.getPosition(), to, piece, getPiece(to));
                        allMoves.add(move);
                    }
                }
            }
        }
        
        return allMoves;
    }
      /**
     * Create a deep copy of the board
     */
    @Override
    public Board clone() {
        Board clonedBoard = new Board();
        
        // Clear the default starting position
        clonedBoard.clear();
        
        // Copy the grid
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];
                if (piece != null) {
                    clonedBoard.grid[row][col] = createPieceCopy(piece);
                }
            }
        }
        
        // Copy other state
        clonedBoard.currentTurn = this.currentTurn;
        clonedBoard.enPassantTarget = this.enPassantTarget;
        clonedBoard.whiteKingsideCastle = this.whiteKingsideCastle;
        clonedBoard.whiteQueensideCastle = this.whiteQueensideCastle;
        clonedBoard.blackKingsideCastle = this.blackKingsideCastle;
        clonedBoard.blackQueensideCastle = this.blackQueensideCastle;
        clonedBoard.halfMoveClock = this.halfMoveClock;
        clonedBoard.fullMoveNumber = this.fullMoveNumber;
        
        return clonedBoard;
    }
    
    /**
     * Create a copy of a piece
     */
    private Piece createPieceCopy(Piece original) {
        String type = original.getType();
        String color = original.getColor();
        Position position = original.getPosition();
        
        switch (type) {
            case "King": return new King(color, position);
            case "Queen": return new Queen(color, position);
            case "Rook": return new Rook(color, position);
            case "Bishop": return new Bishop(color, position);
            case "Knight": return new Knight(color, position);
            case "Pawn": return new Pawn(color, position);
            default: throw new IllegalArgumentException("Unknown piece type: " + type);
        }
    }
    
    /**
     * Get the board as a string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  a b c d e f g h\n");
        sb.append("  ---------------\n");
        
        for (int row = 0; row < 8; row++) {
            sb.append(8 - row).append(" ");
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];
                if (piece == null) {
                    sb.append(". ");
                } else {
                    sb.append(piece.getSymbol()).append(" ");
                }
            }
            sb.append(8 - row).append("\n");
        }
        
        sb.append("  ---------------\n");
        sb.append("  a b c d e f g h\n");
        sb.append("Current turn: ").append(currentTurn).append("\n");
        
        return sb.toString();
    }
    
    public List<Piece> getWhiteCapturedPieces() {
        return whiteCapturedPieces;
    }
    
    public List<Piece> getBlackCapturedPieces() {
        return blackCapturedPieces;
    }

    public boolean canCastleKingside(String color) {
        return color.equals("White") ? whiteKingsideCastle : blackKingsideCastle;
    }

    public boolean canCastleQueenside(String color) {
        return color.equals("White") ? whiteQueensideCastle : blackQueensideCastle;
    }

    public String toFEN() {
        return FENUtils.toFEN(this);
    }

    public static Board fromFEN(String fen) {
        return FENUtils.fromFEN(fen);
    }
    
    /**
     * Clear the board (remove all pieces)
     */
    public void clear() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                grid[row][col] = null;
            }
        }
        // Reset game state
        currentTurn = "White";
        halfMoveClock = 0;
        fullMoveNumber = 1;
        enPassantTarget = null;
        whiteKingsideCastle = false;
        whiteQueensideCastle = false;
        blackKingsideCastle = false;
        blackQueensideCastle = false;
    }
    
    /**
     * Set the half-move clock
     */
    public void setHalfMoveClock(int halfMoveClock) {
        this.halfMoveClock = halfMoveClock;
    }
    
    /**
     * Set the full move number
     */
    public void setFullMoveNumber(int fullMoveNumber) {
        this.fullMoveNumber = fullMoveNumber;
    }

    /**
     * Check for game ending conditions after a move
     * Returns: "CHECKMATE_WHITE", "CHECKMATE_BLACK", "STALEMATE", "DRAW", or null if game continues
     */
    public String checkGameEndingConditions() {
        String currentPlayer = currentTurn;
        String opponent = currentPlayer.equals("White") ? "Black" : "White";
        
        // Check for checkmate of the current player (the player whose turn it is)
        if (isCheckmate(currentPlayer)) {
            return "CHECKMATE_" + opponent.toUpperCase();
        }
        
        // Check for stalemate of the current player
        if (isStalemate(currentPlayer)) {
            return "STALEMATE";
        }
        
        // Check for fifty-move rule
        if (halfMoveClock >= 50) {
            return "DRAW";
        }
        
        // Check for insufficient material (simplified)
        if (isInsufficientMaterial()) {
            return "DRAW";
        }
        
        return null; // Game continues
    }
    
    /**
     * Check if there's insufficient material for checkmate
     */
    private boolean isInsufficientMaterial() {
        int whitePieces = 0;
        int blackPieces = 0;
        boolean hasWhitePawn = false;
        boolean hasBlackPawn = false;
        boolean hasWhiteRook = false;
        boolean hasBlackRook = false;
        boolean hasWhiteQueen = false;
        boolean hasBlackQueen = false;
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = getPiece(new Position(row, col));
                if (piece != null) {
                    if (piece.getColor().equals("White")) {
                        whitePieces++;
                        if (piece.getType().equals("Pawn")) hasWhitePawn = true;
                        if (piece.getType().equals("Rook")) hasWhiteRook = true;
                        if (piece.getType().equals("Queen")) hasWhiteQueen = true;
                    } else {
                        blackPieces++;
                        if (piece.getType().equals("Pawn")) hasBlackPawn = true;
                        if (piece.getType().equals("Rook")) hasBlackRook = true;
                        if (piece.getType().equals("Queen")) hasBlackQueen = true;
                    }
                }
            }
        }
        
        // King vs King
        if (whitePieces == 1 && blackPieces == 1) {
            return true;
        }
        
        // King and Bishop vs King or King and Knight vs King
        if ((whitePieces == 2 && blackPieces == 1) || (whitePieces == 1 && blackPieces == 2)) {
            return !hasWhitePawn && !hasBlackPawn && !hasWhiteRook && !hasBlackRook && 
                   !hasWhiteQueen && !hasBlackQueen;
        }
        
        return false;
    }
}