package com.chess.model;

import com.chess.utils.Logger;

/**
 * Manages the chess game state and rules
 */
public class Game {
    private static final Logger logger = Logger.getLogger(Game.class);
    
    private Board board;
    private GameState state;
    private String winner;
    private GameMode mode;
    
    public enum GameState {
        PLAYING,
        CHECKMATE,
        STALEMATE,
        DRAW,
        RESIGNED
    }
    
    public enum GameMode {
        LOCAL_TWO_PLAYER,
        AI_VS_HUMAN,
        ONLINE_MULTIPLAYER
    }
    
    public Game(GameMode mode) {
        this.board = new Board();
        this.state = GameState.PLAYING;
        this.mode = mode;
        this.winner = null;
    }
    
    public Board getBoard() {
        return board;
    }
    
    public GameState getState() {
        return state;
    }
    
    public String getWinner() {
        return winner;
    }
    
    public GameMode getMode() {
        return mode;
    }
    
    public String getCurrentTurn() {
        return board.getCurrentTurn();
    }
    
    /**
     * Make a move and check for game ending conditions
     */
    public boolean makeMove(Move move) {
        if (state != GameState.PLAYING) {
            logger.warn("Cannot make move: game is not in playing state");
            return false;
        }
        
        if (!board.makeMove(move)) {
            return false;
        }
        
        // Check for game ending conditions
        checkGameEndingConditions();
        
        return true;
    }
    
    /**
     * Check if the game should end due to checkmate, stalemate, or draw
     */
    private void checkGameEndingConditions() {
        String currentPlayer = board.getCurrentTurn();
        String opponent = currentPlayer.equals("White") ? "Black" : "White";
        
        // Check for checkmate
        if (board.isCheckmate(opponent)) {
            state = GameState.CHECKMATE;
            winner = currentPlayer;
            logger.info("Checkmate! " + winner + " wins!");
            return;
        }
        
        // Check for stalemate
        if (board.isStalemate(opponent)) {
            state = GameState.STALEMATE;
            winner = null; // No winner in stalemate
            logger.info("Stalemate! Game is a draw.");
            return;
        }
        
        // Check for insufficient material (simplified)
        if (isInsufficientMaterial()) {
            state = GameState.DRAW;
            winner = null;
            logger.info("Insufficient material! Game is a draw.");
            return;
        }
        
        // Check for threefold repetition (simplified - would need move history analysis)
        // Check for fifty-move rule
        if (board.getHalfMoveClock() >= 50) {
            state = GameState.DRAW;
            winner = null;
            logger.info("Fifty-move rule! Game is a draw.");
            return;
        }
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
                Piece piece = board.getPiece(new Position(row, col));
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
        
        // King and Bishop vs King and Bishop (same colored squares)
        // This is a simplified check - a full implementation would check bishop square colors
        
        return false;
    }
    
    /**
     * Resign the game
     */
    public void resign(String resigningPlayer) {
        if (state == GameState.PLAYING) {
            state = GameState.RESIGNED;
            winner = resigningPlayer.equals("White") ? "Black" : "White";
            logger.info(resigningPlayer + " resigned. " + winner + " wins!");
        }
    }
    
    /**
     * Check if the game is over
     */
    public boolean isGameOver() {
        return state != GameState.PLAYING;
    }
    
    /**
     * Get a description of the game ending
     */
    public String getGameEndDescription() {
        switch (state) {
            case CHECKMATE:
                return "Checkmate! " + winner + " wins!";
            case STALEMATE:
                return "Stalemate! The game is a draw.";
            case DRAW:
                return "Draw! The game is a draw.";
            case RESIGNED:
                return winner + " wins by resignation!";
            default:
                return "Game is still in progress.";
        }
    }
    
    /**
     * Create a new game with the same mode
     */
    public void newGame() {
        this.board = new Board();
        this.state = GameState.PLAYING;
        this.winner = null;
        logger.info("New game started");
    }
} 