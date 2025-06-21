package com.chess.teacher;

import com.chess.model.Board;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.engine.ChessEngine;
import com.chess.utils.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Teacher module that provides hints and move analysis using Stockfish
 */
public class TeacherModule {
    private static final Logger logger = Logger.getLogger(TeacherModule.class);
    
    private ChessEngine engine;
    private boolean isEnabled;
    
    public TeacherModule() {
        try {
            this.engine = new ChessEngine();
            this.isEnabled = true;
            logger.info("Teacher module initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize chess engine: " + e.getMessage());
            this.isEnabled = false;
        }
    }
    
    /**
     * Get a hint for the current position
     */
    public CompletableFuture<Move> getHint(Board board, int depth) {
        if (!isEnabled) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return engine.getBestMove(board, depth);
            } catch (Exception e) {
                logger.error("Error getting hint: " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * Analyze a player's move and compare it with engine's best move
     */
    public CompletableFuture<MoveAnalysis> analyzeMove(Board board, Move playerMove, int depth) {
        if (!isEnabled) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get engine's best move
                Move engineMove = engine.getBestMove(board, depth);
                
                // Create analysis
                MoveAnalysis analysis = new MoveAnalysis();
                analysis.setPlayerMove(playerMove);
                analysis.setEngineMove(engineMove);
                analysis.setIsBestMove(playerMove.equals(engineMove));
                
                // Calculate move quality (simplified)
                analysis.setQuality(calculateMoveQuality(board, playerMove, engineMove));
                
                return analysis;
            } catch (Exception e) {
                logger.error("Error analyzing move: " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * Get multiple best moves for analysis
     */
    public CompletableFuture<List<Move>> getTopMoves(Board board, int count, int depth) {
        if (!isEnabled) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // For now, we'll just return the best move
                // In a real implementation, you'd need to modify the engine to return multiple moves
                Move bestMove = engine.getBestMove(board, depth);
                return bestMove != null ? List.of(bestMove) : List.of();
            } catch (Exception e) {
                logger.error("Error getting top moves: " + e.getMessage());
                return List.of();
            }
        });
    }
    
    /**
     * Evaluate the current position
     */
    public CompletableFuture<Double> evaluatePosition(Board board, int depth) {
        if (!isEnabled) {
            return CompletableFuture.completedFuture(0.0);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get the best move and use it as a proxy for position evaluation
                Move bestMove = engine.getBestMove(board, depth);
                if (bestMove != null) {
                    // This is a simplified evaluation - in reality, you'd get the actual score
                    return calculatePositionScore(board, bestMove);
                }
                return 0.0;
            } catch (Exception e) {
                logger.error("Error evaluating position: " + e.getMessage());
                return 0.0;
            }
        });
    }
    
    /**
     * Calculate move quality (0.0 to 1.0)
     */
    private double calculateMoveQuality(Board board, Move playerMove, Move engineMove) {
        if (playerMove.equals(engineMove)) {
            return 1.0; // Perfect move
        }
        
        // Simplified quality calculation
        // In a real implementation, you'd compare the resulting positions
        return 0.5; // Default quality
    }
    
    /**
     * Calculate position score based on material and position
     */
    private double calculatePositionScore(Board board, Move move) {
        // Simplified position evaluation
        // In a real implementation, you'd use Stockfish's evaluation
        return 0.0;
    }
    
    /**
     * Provide educational feedback for a move
     */
    public String getMoveFeedback(MoveAnalysis analysis) {
        if (analysis == null) {
            return "Unable to analyze move.";
        }
        
        if (analysis.isBestMove()) {
            return "Excellent move! That's the best move in this position.";
        } else {
            return "Good move, but the engine suggests " + 
                   analysis.getEngineMove().getFrom().toAlgebraicNotation() + 
                   analysis.getEngineMove().getTo().toAlgebraicNotation() + 
                   " as the best move.";
        }
    }
    
    /**
     * Check if the teacher module is enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Close the teacher module
     */
    public void close() {
        if (engine != null) {
            try {
                engine.close();
            } catch (Exception e) {
                logger.error("Error closing teacher module: " + e.getMessage());
            }
        }
    }
    
    /**
     * Inner class to hold move analysis results
     */
    public static class MoveAnalysis {
        private Move playerMove;
        private Move engineMove;
        private boolean isBestMove;
        private double quality;
        
        public Move getPlayerMove() {
            return playerMove;
        }
        
        public void setPlayerMove(Move playerMove) {
            this.playerMove = playerMove;
        }
        
        public Move getEngineMove() {
            return engineMove;
        }
        
        public void setEngineMove(Move engineMove) {
            this.engineMove = engineMove;
        }
        
        public boolean isBestMove() {
            return isBestMove;
        }
        
        public void setIsBestMove(boolean isBestMove) {
            this.isBestMove = isBestMove;
        }
        
        public double getQuality() {
            return quality;
        }
        
        public void setQuality(double quality) {
            this.quality = quality;
        }
    }
} 