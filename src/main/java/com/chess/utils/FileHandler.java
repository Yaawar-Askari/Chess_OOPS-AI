package com.chess.utils;

import com.chess.model.Piece;
import com.chess.model.Position;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Queen;
import com.chess.model.pieces.Rook;
import com.chess.model.pieces.Bishop;
import com.chess.model.pieces.Knight;
import com.chess.model.pieces.Pawn;
import com.chess.model.Board;
import com.chess.model.Move;
import com.chess.utils.Logger;
import com.chess.utils.FENUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Handles saving and loading chess games in various formats
 */
public class FileHandler {
    private static final Logger logger = Logger.getLogger(FileHandler.class);
    
    /**
     * Save game in FEN format (current board state)
     */
    public static void saveFEN(Board board, String filename) throws IOException {
        String fen = FENUtils.toFEN(board);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(fen);
        }
        
        logger.info("Game saved in FEN format: " + filename);
    }
    
    /**
     * Load game from FEN format
     */
    public static Board loadFEN(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String fen = reader.readLine();
            if (fen == null || fen.trim().isEmpty()) {
                throw new IOException("Empty or invalid FEN file");
            }
            
            return FENUtils.fromFEN(fen.trim());
        }
    }
    
    /**
     * Save game in PGN format (move history)
     */
    public static void savePGN(Board board, String filename, String whitePlayer, String blackPlayer) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // PGN header
            writer.println("[Event \"Chess Game\"]");
            writer.println("[Site \"Java Chess Application\"]");
            writer.println("[Date \"" + new SimpleDateFormat("yyyy.MM.dd").format(new Date()) + "\"]");
            writer.println("[Round \"1\"]");
            writer.println("[White \"" + whitePlayer + "\"]");
            writer.println("[Black \"" + blackPlayer + "\"]");
            writer.println("[Result \"*\"]");
            writer.println();
            
            // Move text
            List<Move> moves = board.getMoveHistory();
            StringBuilder moveText = new StringBuilder();
            
            for (int i = 0; i < moves.size(); i++) {
                if (i % 2 == 0) {
                    // White's move
                    moveText.append((i / 2 + 1)).append(". ");
                }
                moveText.append(moveToAlgebraic(moves.get(i), board)).append(" ");
            }
            
            writer.println(moveText.toString().trim());
        }
        
        logger.info("Game saved in PGN format: " + filename);
    }
    
    /**
     * Convert a move to algebraic notation
     */
    private static String moveToAlgebraic(Move move, Board board) {
        // This is a simplified version - in a real implementation,
        // you'd need to handle all the special cases like check, checkmate,
        // disambiguation, etc.
        
        String pieceSymbol = getPieceSymbol(move.getPiece());
        String from = move.getFrom().toAlgebraicNotation();
        String to = move.getTo().toAlgebraicNotation();
        
        if (move.isCapture()) {
            return pieceSymbol + "x" + to;
        } else {
            return pieceSymbol + to;
        }
    }
    
    /**
     * Get the symbol for a piece
     */
    private static String getPieceSymbol(Piece piece) {
        switch (piece.getType()) {
            case "King": return "K";
            case "Queen": return "Q";
            case "Rook": return "R";
            case "Bishop": return "B";
            case "Knight": return "N";
            case "Pawn": return "";
            default: return "?";
        }
    }
    
    /**
     * Parse FEN string to create a Board
     */
    private static Board parseFEN(String fen) throws IOException {
        String[] parts = fen.split(" ");
        if (parts.length < 4) {
            throw new IOException("Invalid FEN format");
        }
        
        Board board = new Board();
        
        // Parse piece placement
        String[] ranks = parts[0].split("/");
        if (ranks.length != 8) {
            throw new IOException("Invalid FEN piece placement");
        }
        
        for (int row = 0; row < 8; row++) {
            int col = 0;
            for (char c : ranks[row].toCharArray()) {
                if (Character.isDigit(c)) {
                    col += Character.getNumericValue(c);
                } else {
                    Position pos = new Position(row, col);
                    Piece piece = createPieceFromFEN(c, pos);
                    board.setPiece(pos, piece);
                    col++;
                }
            }
        }
        
        // Parse active color
        if (parts.length > 1) {
            board.setCurrentTurn(parts[1].equals("w") ? "White" : "Black");
        }
        
        // Parse castling rights
        if (parts.length > 2) {
            parseCastlingRights(board, parts[2]);
        }
        
        // Parse en passant target
        if (parts.length > 3 && !parts[3].equals("-")) {
            try {
                Position enPassant = new Position(parts[3]);
                board.setEnPassantTarget(enPassant);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid en passant target in FEN: " + parts[3]);
            }
        }
        
        // Parse halfmove clock
        if (parts.length > 4) {
            try {
                int halfMoveClock = Integer.parseInt(parts[4]);
                // Set halfmove clock (would need to be implemented in Board)
            } catch (NumberFormatException e) {
                logger.warn("Invalid halfmove clock in FEN: " + parts[4]);
            }
        }
        
        // Parse fullmove number
        if (parts.length > 5) {
            try {
                int fullMoveNumber = Integer.parseInt(parts[5]);
                // Set fullmove number (would need to be implemented in Board)
            } catch (NumberFormatException e) {
                logger.warn("Invalid fullmove number in FEN: " + parts[5]);
            }
        }
        
        return board;
    }
    
    /**
     * Create a piece from FEN character
     */
    private static Piece createPieceFromFEN(char c, Position position) {
        boolean isWhite = Character.isUpperCase(c);
        char pieceChar = Character.toLowerCase(c);
        String color = isWhite ? "White" : "Black";
        
        switch (pieceChar) {
            case 'k': return new King(color, position);
            case 'q': return new Queen(color, position);
            case 'r': return new Rook(color, position);
            case 'b': return new Bishop(color, position);
            case 'n': return new Knight(color, position);
            case 'p': return new Pawn(color, position);
            default: throw new IllegalArgumentException("Invalid piece character: " + c);
        }
    }
    
    /**
     * Parse castling rights from FEN
     */
    private static void parseCastlingRights(Board board, String castling) {
        // This would need to be implemented in the Board class
        // For now, we'll just log it
        logger.debug("Castling rights: " + castling);
    }
    
    /**
     * Auto-save game with timestamp
     */
    public static void autoSave(Board board) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "autosave_" + timestamp + ".fen";
            saveFEN(board, filename);
        } catch (IOException e) {
            logger.error("Failed to auto-save game: " + e.getMessage());
        }
    }

    public static void saveGame(Board board, List<Move> moves, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Save current board state as FEN
            writer.println("BOARD:" + FENUtils.toFEN(board));
            
            // Save move history
            for (Move move : moves) {
                writer.println("MOVE:" + move.getFrom().toAlgebraicNotation() + 
                             move.getTo().toAlgebraicNotation());
            }
            logger.info("Game saved to " + filename);
        } catch (IOException e) {
            logger.error("Error saving game: " + e.getMessage());
        }
    }
    
    public static GameData loadGame(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            if (line == null || !line.startsWith("BOARD:")) {
                throw new IOException("Invalid game file format");
            }
            
            String fen = line.substring(6);
            Board board = FENUtils.fromFEN(fen);
            
            List<Move> moves = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MOVE:")) {
                    String moveStr = line.substring(5);
                    // Parse move (simplified)
                    Position from = new Position(moveStr.substring(0, 2));
                    Position to = new Position(moveStr.substring(2, 4));
                    Move move = new Move(from, to, null, null);
                    moves.add(move);
                }
            }
            
            logger.info("Game loaded from " + filename);
            return new GameData(board, moves);
        } catch (IOException e) {
            logger.error("Error loading game: " + e.getMessage());
            return null;
        }
    }
    
    public static class GameData {
        public final Board board;
        public final List<Move> moves;
        
        public GameData(Board board, List<Move> moves) {
            this.board = board;
            this.moves = moves;
        }
    }
} 