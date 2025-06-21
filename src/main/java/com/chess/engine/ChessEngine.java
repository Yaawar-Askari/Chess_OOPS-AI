package com.chess.engine;

import com.chess.model.Board;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.utils.Logger;
import com.chess.utils.FENUtils;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Chess engine using Stockfish
 */
public class ChessEngine {
    private static final Logger logger = Logger.getLogger(ChessEngine.class);
    private Process stockfishProcess;
    private BufferedReader reader;
    private PrintWriter writer;
    
    public ChessEngine() {
        initializeStockfish();
    }
    
    private void initializeStockfish() {
        try {
            // Try to find Stockfish executable
            String stockfishPath = findStockfishExecutable();
            if (stockfishPath == null) {
                logger.error("Stockfish executable not found");
                return;
            }
            
            ProcessBuilder pb = new ProcessBuilder(stockfishPath);
            stockfishProcess = pb.start();
            reader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
            writer = new PrintWriter(stockfishProcess.getOutputStream(), true);
            
            // Initialize Stockfish
            writer.println("uci");
            writer.println("isready");
            
            // Wait for ready
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("readyok")) {
                    break;
                }
            }
            
            logger.info("Stockfish initialized successfully");
        } catch (IOException e) {
            logger.error("Failed to initialize Stockfish: " + e.getMessage());
        }
    }
    
    private String findStockfishExecutable() {
        // Check common locations
        String[] paths = {
            "stockfish-windows-x86-64-avx2/stockfish/stockfish-windows-x86-64-avx2.exe",
            "stockfish",
            "stockfish.exe"
        };
        
        for (String path : paths) {
            File file = new File(path);
            if (file.exists() && file.canExecute()) {
                return path;
            }
        }
        return null;
    }
    
    public Move getBestMove(Board board, int timeMs) {
        if (stockfishProcess == null || !stockfishProcess.isAlive()) {
            logger.error("Stockfish not available");
            return null;
        }
        
        try {
            // Set up position
            String fen = FENUtils.toFEN(board);
            writer.println("position fen " + fen);
            
            // Get best move
            writer.println("go movetime " + timeMs);
            
            String bestMove = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    bestMove = line.split(" ")[1];
                    break;
                }
            }
            
            if (bestMove == null || bestMove.equals("(none)")) {
                logger.warn("No best move found");
                return null;
            }
            
            // Parse move
            try {
                Position from = new Position(bestMove.substring(0, 2));
                Position to = new Position(bestMove.substring(2, 4));
                // Find the piece on the board
                com.chess.model.Piece piece = board.getPiece(from);
                com.chess.model.Piece captured = board.getPiece(to);
                Move move = new Move(from, to, piece, captured);
                logger.info("Best move: " + move);
                return move;
            } catch (Exception e) {
                logger.error("Error parsing Stockfish move: " + bestMove, e);
                return null;
            }
        } catch (IOException e) {
            logger.error("Error communicating with Stockfish: " + e.getMessage());
            return null;
        }
    }

    public void close() throws IOException {
        if (stockfishProcess != null && stockfishProcess.isAlive()) {
            writer.println("quit");
            stockfishProcess.destroy();
            logger.info("Stockfish engine closed");
        }
    }

    /**
     * Test Stockfish integration end-to-end
     */
    public static void main(String[] args) {
        ChessEngine engine = new ChessEngine();
        Board board = new Board();
        
        // Test position
        String testFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        board = FENUtils.fromFEN(testFen);
        
        Move bestMove = engine.getBestMove(board, 1000);
        if (bestMove != null) {
            System.out.println("Best move: " + bestMove);
        } else {
            System.out.println("No move found");
        }
    }
} 