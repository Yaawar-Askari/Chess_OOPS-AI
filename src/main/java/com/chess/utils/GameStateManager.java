package com.chess.utils;

import com.chess.model.Board;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for saving and loading chess game states
 * Uses a simple JSON-like format without external dependencies
 */
public class GameStateManager {
    private static final Logger logger = Logger.getLogger(GameStateManager.class);
    
    /**
     * Represents a complete game state that can be saved/loaded
     */
    public static class GameState {
        private String boardFEN;
        private String currentTurn;
        private List<String> moveHistory;
        private String gameStatus;
        private String playerColor;
        private String gameMode;
        private String savedAt;
        private Map<String, Object> additionalData;
        
        // Default constructor for Jackson
        public GameState() {}
        
        public GameState(String boardFEN, String currentTurn, List<String> moveHistory, 
                        String gameStatus, String playerColor, String gameMode) {
            this.boardFEN = boardFEN;
            this.currentTurn = currentTurn;
            this.moveHistory = moveHistory;
            this.gameStatus = gameStatus;
            this.playerColor = playerColor;
            this.gameMode = gameMode;
            this.savedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            this.additionalData = new HashMap<>();
        }
        
        // Getters and setters
        public String getBoardFEN() { return boardFEN; }
        public void setBoardFEN(String boardFEN) { this.boardFEN = boardFEN; }
        
        public String getCurrentTurn() { return currentTurn; }
        public void setCurrentTurn(String currentTurn) { this.currentTurn = currentTurn; }
        
        public List<String> getMoveHistory() { return moveHistory; }
        public void setMoveHistory(List<String> moveHistory) { this.moveHistory = moveHistory; }
        
        public String getGameStatus() { return gameStatus; }
        public void setGameStatus(String gameStatus) { this.gameStatus = gameStatus; }
        
        public String getPlayerColor() { return playerColor; }
        public void setPlayerColor(String playerColor) { this.playerColor = playerColor; }
        
        public String getGameMode() { return gameMode; }
        public void setGameMode(String gameMode) { this.gameMode = gameMode; }
        
        public String getSavedAt() { return savedAt; }
        public void setSavedAt(String savedAt) { this.savedAt = savedAt; }
        
        public Map<String, Object> getAdditionalData() { return additionalData; }
        public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }
    }
    
    /**
     * Save a game state to a JSON file
     */
    public static void saveGameToFile(GameState gameState, File file) throws IOException {
        logger.info("Saving game state to: " + file.getAbsolutePath());
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("{");
            writer.println("  \"boardFEN\": \"" + escapeJson(gameState.getBoardFEN()) + "\",");
            writer.println("  \"currentTurn\": \"" + escapeJson(gameState.getCurrentTurn()) + "\",");
            writer.println("  \"gameStatus\": \"" + escapeJson(gameState.getGameStatus()) + "\",");
            writer.println("  \"playerColor\": \"" + escapeJson(gameState.getPlayerColor()) + "\",");
            writer.println("  \"gameMode\": \"" + escapeJson(gameState.getGameMode()) + "\",");
            writer.println("  \"savedAt\": \"" + escapeJson(gameState.getSavedAt()) + "\",");
            
            // Write move history array
            writer.print("  \"moveHistory\": [");
            List<String> moves = gameState.getMoveHistory();
            if (moves != null && !moves.isEmpty()) {
                for (int i = 0; i < moves.size(); i++) {
                    writer.print("\"" + escapeJson(moves.get(i)) + "\"");
                    if (i < moves.size() - 1) {
                        writer.print(", ");
                    }
                }
            }
            writer.println("]");
            writer.println("}");
            
            logger.info("Game saved successfully");
        } catch (IOException e) {
            logger.error("Failed to save game: " + e.getMessage());
            throw new IOException("Failed to save game: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load a game state from a JSON file
     */
    public static GameState loadGameFromFile(File file) throws IOException {
        logger.info("Loading game state from: " + file.getAbsolutePath());
        
        if (!file.exists()) {
            throw new FileNotFoundException("Save file does not exist: " + file.getAbsolutePath());
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            GameState gameState = parseJsonGameState(content.toString());
            logger.info("Game loaded successfully");
            return gameState;
        } catch (Exception e) {
            logger.error("Failed to load game: " + e.getMessage());
            throw new IOException("Failed to load game: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse a simple JSON string into a GameState object
     */
    private static GameState parseJsonGameState(String json) throws IOException {
        GameState gameState = new GameState();
        
        try {
            // Simple JSON parsing - look for key-value pairs
            gameState.setBoardFEN(extractJsonValue(json, "boardFEN"));
            gameState.setCurrentTurn(extractJsonValue(json, "currentTurn"));
            gameState.setGameStatus(extractJsonValue(json, "gameStatus"));
            gameState.setPlayerColor(extractJsonValue(json, "playerColor"));
            gameState.setGameMode(extractJsonValue(json, "gameMode"));
            gameState.setSavedAt(extractJsonValue(json, "savedAt"));
            
            // Parse move history array
            List<String> moveHistory = extractJsonArray(json, "moveHistory");
            gameState.setMoveHistory(moveHistory);
            
            return gameState;
        } catch (Exception e) {
            throw new IOException("Invalid JSON format in save file", e);
        }
    }
    
    /**
     * Extract a string value from simple JSON
     */
    private static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\\s*\"([^\"]*?)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return unescapeJson(m.group(1));
        }
        return "";
    }
    
    /**
     * Extract an array from simple JSON
     */
    private static List<String> extractJsonArray(String json, String key) {
        List<String> result = new ArrayList<>();
        String pattern = "\"" + key + "\":\\s*\\[([^\\]]*?)\\]";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        
        if (m.find()) {
            String arrayContent = m.group(1).trim();
            if (!arrayContent.isEmpty()) {
                String[] items = arrayContent.split(",");
                for (String item : items) {
                    String cleaned = item.trim();
                    if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                        cleaned = cleaned.substring(1, cleaned.length() - 1);
                    }
                    result.add(unescapeJson(cleaned));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Escape JSON special characters
     */
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Unescape JSON special characters
     */
    private static String unescapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }
    
    /**
     * Create a GameState from the current board and game information
     */
    public static GameState createGameState(Board board, List<String> moveHistory, 
                                          String gameStatus, String playerColor, String gameMode) {
        String boardFEN = board.toFEN();
        String currentTurn = board.getCurrentTurn();
        
        return new GameState(boardFEN, currentTurn, moveHistory, gameStatus, playerColor, gameMode);
    }
    
    /**
     * Validate a game state for completeness
     */
    public static boolean validateGameState(GameState gameState) {
        if (gameState == null) {
            logger.warn("Game state is null");
            return false;
        }
        
        if (gameState.getBoardFEN() == null || gameState.getBoardFEN().trim().isEmpty()) {
            logger.warn("Game state has invalid board FEN");
            return false;
        }
        
        if (gameState.getCurrentTurn() == null || 
            (!gameState.getCurrentTurn().equals("White") && !gameState.getCurrentTurn().equals("Black"))) {
            logger.warn("Game state has invalid current turn: " + gameState.getCurrentTurn());
            return false;
        }
        
        return true;
    }
    
    /**
     * Convert move history to PGN format for compatibility
     */
    public static String moveHistoryToPGN(List<String> moveHistory) {
        if (moveHistory == null || moveHistory.isEmpty()) {
            return "";
        }
        
        StringBuilder pgn = new StringBuilder();
        for (int i = 0; i < moveHistory.size(); i++) {
            if (i % 2 == 0) {
                pgn.append((i / 2 + 1)).append(". ");
            }
            pgn.append(moveHistory.get(i));
            if (i % 2 == 0 && i < moveHistory.size() - 1) {
                pgn.append(" ");
            } else if (i % 2 == 1) {
                pgn.append(" ");
            }
        }
        
        return pgn.toString().trim();
    }
    
    /**
     * Export game state to PGN format
     */
    public static void exportToPGN(GameState gameState, File file) throws IOException {
        logger.info("Exporting game to PGN format: " + file.getAbsolutePath());
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // PGN headers
            writer.println("[Event \"Chess Game\"]");
            writer.println("[Site \"Local Game\"]");
            writer.println("[Date \"" + gameState.getSavedAt().substring(0, 10) + "\"]");
            writer.println("[Round \"1\"]");
            writer.println("[White \"Player\"]");
            writer.println("[Black \"" + ("AI".equals(gameState.getGameMode()) ? "AI" : "Player") + "\"]");
            writer.println("[Result \"*\"]");
            writer.println("[FEN \"" + gameState.getBoardFEN() + "\"]");
            writer.println();
            
            // Move history
            String pgn = moveHistoryToPGN(gameState.getMoveHistory());
            if (!pgn.isEmpty()) {
                writer.println(pgn);
            }
            
            logger.info("PGN export completed successfully");
        } catch (IOException e) {
            logger.error("Failed to export to PGN: " + e.getMessage());
            throw new IOException("Failed to export to PGN: " + e.getMessage(), e);
        }
    }
}
