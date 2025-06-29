package com.chess.gui;

import com.chess.model.*;
import com.chess.engine.ChessEngine;
import com.chess.network.Client;
import com.chess.network.Server;
import com.chess.utils.Logger;
import com.chess.utils.NetworkUtils;
import com.chess.Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;

/**
 * Main GUI for the chess game
 */
public class ChessGUI extends JFrame {
    private static final Logger logger = Logger.getLogger(ChessGUI.class);
    
    private Board board;
    private ChessBoardPanel boardPanel;
    private GameStatusPanel statusPanel;
    private ChatPanel chatPanel;
    private ChessEngine aiEngine;
    private Client networkClient;
    private Server networkServer;
    
    private GameMode gameMode;
    private Position selectedFrom;
    private boolean isAITurn = false;
    private Move pendingMove;
    private SwingWorker<Move, Void> aiMoveWorker;
    private int hintsRemaining = 3;
    private String playerColor; // "White" or "Black"
    
    public enum GameMode {
        LOCAL, AI, HOST, CLIENT, MULTIPLAYER_HOST, MULTIPLAYER_CLIENT
    }
    
    /**
     * Safely updates board panel on EDT
     */
    private void safeUpdateBoardPanel() {
        if (SwingUtilities.isEventDispatchThread()) {
            if (boardPanel != null) {
                boardPanel.repaint();
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                if (boardPanel != null) {
                    boardPanel.repaint();
                }
            });
        }
    }
    
    /**
     * Safely updates status on EDT
     */
    private void safeUpdateStatus() {
        if (SwingUtilities.isEventDispatchThread()) {
            updateStatus();
        } else {
            SwingUtilities.invokeLater(this::updateStatus);
        }
    }
    
    public ChessGUI() {
        System.out.println("Creating ChessGUI...");
        this.board = new Board();
        System.out.println("Board created successfully");

        // Constructor now directly initializes the GUI.
        // The calling method will be responsible for ensuring it's on the EDT.
        initializeGUI();

        System.out.println("ChessGUI initialization completed");
    }
    
    private void initializeGUI() {
        System.out.println("Initializing GUI components...");
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create main layout
        setLayout(new BorderLayout());
        
        // Create and add all components
        System.out.println("Creating board panel...");
        boardPanel = new ChessBoardPanel(board, this);
        add(boardPanel, BorderLayout.CENTER);
        
        System.out.println("Creating status panel...");
        statusPanel = new GameStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
        
        // Don't create chat panel by default - it will be created only for multiplayer modes
        
        // Initialize AI engine
        System.out.println("Initializing AI engine...");
        try {
            aiEngine = new ChessEngine();
            System.out.println("AI engine initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize AI engine: " + e.getMessage());
            System.err.println("Failed to initialize AI engine: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Failed to initialize AI engine. AI features will be disabled.", 
                "Warning", JOptionPane.WARNING_MESSAGE);
        }
        
        updateStatus();
        
        // Launch in a maximized state by default
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setPreferredSize(new Dimension(1200, 800)); // Set a preferred size for when un-maximized
        setLocationRelativeTo(null); // Center on screen
        
        System.out.println("GUI initialization completed");
        setVisible(true); // Make the frame visible

        // Force the layout manager to re-validate and repaint all components.
        // This is a robust way to fix rendering issues.
        revalidate();
        repaint();

        // Add listener for the hint button (only if it exists)
        if (statusPanel.getHintButton() != null) {
            statusPanel.getHintButton().addActionListener(e -> requestHint());
        }
    }
    
    public void startLocalGame() {
        gameMode = GameMode.LOCAL;
        setTitle("Chess Game - Local (2 Players)");
        pack();
        setVisible(true);
    }
    
    public void startAIGame() {
        gameMode = GameMode.AI;
        board = new Board();
        boardPanel = new ChessBoardPanel(board, this);
        statusPanel = new GameStatusPanel("AI");
        
        // Don't create chat panel for AI mode
        
        aiEngine = new ChessEngine();
        
        initializeGUI();
    }
    
    public void startHostGame(String serverAddress, int port) {
        gameMode = GameMode.HOST;
        setTitle("Chess Game - Host (Port: " + port + ")");
        
        // Connect to the server in a new thread to avoid blocking the GUI
        new Thread(() -> {
            try {
                this.networkClient = new Client(serverAddress, port, this);
                this.networkClient.connect();
            } catch (IOException e) {
                logger.error("Failed to connect as host: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Could not connect to the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }
    
    public void startClientGame(String serverAddress, int port) {
        gameMode = GameMode.CLIENT;
        setTitle("Chess Game - Client");
        
        // Connect to the server in a new thread to avoid blocking the GUI
        new Thread(() -> {
            try {
                this.networkClient = new Client(serverAddress, port, this);
                this.networkClient.connect();
            } catch (IOException e) {
                logger.error("Failed to connect as client: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Could not connect to " + serverAddress, "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }
    
    public void startGame(GameMode mode, String ipAddress) {
        this.gameMode = mode;
        getContentPane().removeAll();

        Board board = new Board();
        boardPanel = new ChessBoardPanel(board, this);
        statusPanel = new GameStatusPanel(mode.name());
        
        // Only create chat panel for multiplayer modes
        if (mode == GameMode.MULTIPLAYER_HOST || mode == GameMode.MULTIPLAYER_CLIENT) {
            chatPanel = new ChatPanel(this::onChatMessageSent);
        }

        statusPanel.updateStatus(board.getCurrentTurn() + "'s turn");

        getContentPane().add(boardPanel, BorderLayout.CENTER);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);
        
        // Only add chat panel for multiplayer modes
        if (mode == GameMode.MULTIPLAYER_HOST || mode == GameMode.MULTIPLAYER_CLIENT) {
            getContentPane().add(chatPanel, BorderLayout.EAST);
        }

        if (gameMode == GameMode.MULTIPLAYER_HOST) {
            // Server is already created in Main.startServer(), just connect to it
            try {
                joinServer(ipAddress);
            } catch (IOException e) {
                logger.error("Host failed to connect to own server", e);
                JOptionPane.showMessageDialog(this, "Failed to start local server connection.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (gameMode == GameMode.MULTIPLAYER_CLIENT) {
            try {
                joinServer(ipAddress);
            } catch (IOException e) {
                logger.error("Client failed to connect", e);
                JOptionPane.showMessageDialog(this, "Failed to connect to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(e);
            }
        }

        pack();
        setSize(1200, 800); // Set a fixed size
        setLocationRelativeTo(null);
        setVisible(true);
        revalidate();
        repaint();
    }
    
    private void startServer() {
        networkServer = new Server(NetworkUtils.PORT);
        new Thread(() -> {
            try {
                networkServer.start();
            } catch (IOException e) {
                logger.error("Server failed to start", e);
            }
        }).start();
        
        // Get the actual port the server is using
        int actualPort = networkServer.getPort();
        JOptionPane.showMessageDialog(this, 
            "Server started. Your join code is: " + NetworkUtils.getLocalIpAddress() + ":" + actualPort, 
            "Server Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void joinServer(String ipAddress) throws IOException {
        // Parse IP:PORT format
        String[] parts = ipAddress.split(":");
        if (parts.length != 2) {
            throw new IOException("Invalid server address format. Expected IP:PORT");
        }
        
        String serverIP = parts[0];
        int serverPort;
        try {
            serverPort = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid port number: " + parts[1]);
        }
        
        networkClient = new Client(serverIP, serverPort, this);
        networkClient.connect();
    }
    
    /**
     * Handles a move attempt, either from a click or a drag-and-drop.
     */
    public void onMoveAttempted(Position from, Position to) {
        if (isAITurn) return; // Prevent moves during AI's turn

        Piece pieceToMove = board.getPiece(from);
        if (pieceToMove == null) {
            clearSelection();
            return; // No piece to move
        }

        // Check if the piece belongs to the current player's turn
        logger.info("Move attempt - Piece color: " + pieceToMove.getColor() + ", Current turn: " + board.getCurrentTurn() + ", Player color: " + this.playerColor);
        if (!pieceToMove.getColor().equals(board.getCurrentTurn())) {
            logger.info("Attempted to move " + pieceToMove.getColor() + " piece on " + board.getCurrentTurn() + "'s turn");
            clearSelection();
            return; // Not your turn
        }

        Move move = createMove(from, to, pieceToMove, board.getPiece(to));        // First, check if the move is valid. Only animate valid moves.
        if (board.isValidMove(move)) {
            this.pendingMove = move;
            boardPanel.animateMove(move);
        } else {
            // If the move is invalid, provide visual feedback
            logger.info("Invalid move attempted: " + move);
            
            // Flash the destination square red to indicate invalid move
            boardPanel.highlightSquare(to, new Color(255, 0, 0, 180));
            
            // Play system beep for audio feedback
            java.awt.Toolkit.getDefaultToolkit().beep();
            
            // Clear highlights after a short delay
            Timer timer = new Timer(500, e -> {
                boardPanel.clearHighlights();
                clearSelection();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
     * Handles a user clicking on a square (for click-to-move).
     */
    public void onSquareClicked(Position position) {
        if (isAITurn) return; // Prevent clicks during AI's turn

        if (selectedFrom != null) {
            // A piece is already selected, try to move it to the new position
            onMoveAttempted(selectedFrom, position);
        } else if (board.getPiece(position) != null) {
            // No piece is selected, so select the one clicked (if it's the current player's)
            Piece clickedPiece = board.getPiece(position);
            if (clickedPiece.getColor().equals(board.getCurrentTurn())) {
                selectedFrom = position;
                boardPanel.highlightValidMoves(clickedPiece);
            }
        }
    }

    private void clearSelection() {
        selectedFrom = null;
        boardPanel.clearHighlights();
    }
    
    private void showValidMoves(Piece piece) {
        List<Position> validMoves = piece.getValidMoves(board);
        for (Position pos : validMoves) {
            boardPanel.highlightSquare(pos, Color.GREEN);
        }
    }
    
    private void makeAIMove() {
        if (aiEngine == null) return;
        
        isAITurn = true;
        statusPanel.setStatus("AI is thinking...");
        
        new Thread(() -> {
            try {
                Move aiMove = aiEngine.getBestMove(board, 12);
                if (aiMove != null) {
                    SwingUtilities.invokeLater(() -> {
                        makeMove(aiMove);
                        
                        // Check if user is dragging before updating board
                        if (boardPanel.isDragInProgress()) {
                            // If user is dragging, just update the underlying board state
                            // The visual update will happen when drag completes
                            board = boardPanel.getBoard();
                        } else {
                            // Safe to update the board visually
                            boardPanel.updateBoard();
                        }
                        
                        updateStatus();
                        isAITurn = false;
                    });
                } else {
                    // AI returned null move, check for game end.
                    SwingUtilities.invokeLater(() -> {
                        isAITurn = false;
                        logger.warn("AI returned null move in makeAIMove. Checking for game end.");
                        updateStatus();
                    });
                }
            } catch (Exception e) {
                logger.error("AI move failed: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    isAITurn = false;
                    statusPanel.setStatus("AI error. Your turn.");
                    updateStatus();
                });
            }
        }).start();
    }
    
    private void sendMoveToNetwork(Move move) {
        if (networkClient != null) {
            networkClient.sendMove(move);
        }
    }
    
    public void receiveMoveFromNetwork(Move move) {
        SwingUtilities.invokeLater(() -> {
            logger.info("GUI received move from network: " + move);
            board.makeMove(move);
            
            // Check for game ending conditions after the move
            String gameResult = board.checkGameEndingConditions();
            if (gameResult != null) {
                // Game is over, show summary dialog
                String displayMessage = getGameEndDisplayMessage(gameResult);
                GameSummaryDialog.showGameSummary(this, displayMessage);
                return; // Don't continue with normal updates
            }
            
            boardPanel.repaint();
            updateStatus();
        });
    }
    
    public void updateBoard(Board board) {
        SwingUtilities.invokeLater(() -> {
            logger.info("GUI received full board update. New FEN: " + board.toFEN());
            logger.info("Old board FEN: " + this.board.toFEN());
            this.board = board;
            boardPanel.setBoard(board);
            boardPanel.repaint();
            updateStatus();
        });
    }

    public void updateTurn(String turn) {
        SwingUtilities.invokeLater(() -> {
            logger.info("updateTurn called with turn: " + turn + ", playerColor: " + this.playerColor);
            logger.info("Board current turn before update: " + board.getCurrentTurn());
            board.setCurrentTurn(turn);
            logger.info("Board current turn after update: " + board.getCurrentTurn());
            boolean isMyTurn = turn.equals(this.playerColor);
            statusPanel.updateStatus(isMyTurn ? "Your turn" : "Waiting for opponent's move...");
            boardPanel.setInteractionEnabled(isMyTurn);
        });
    }

    public void receiveChatMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (chatPanel != null) {
                chatPanel.addMessage(message);
            }
        });
    }
    
    private void updateStatus() {
        logger.info("updateStatus called. Current turn: " + board.getCurrentTurn());
        logger.info("White in check: " + board.isInCheck("White"));
        logger.info("Black in check: " + board.isInCheck("Black"));
        logger.info("White checkmate: " + board.isCheckmate("White"));
        logger.info("Black checkmate: " + board.isCheckmate("Black"));
        logger.info("White stalemate: " + board.isStalemate("White"));
        logger.info("Black stalemate: " + board.isStalemate("Black"));
          if (board.isCheckmate(board.getCurrentTurn())) {
            String winner = board.getCurrentTurn().equals("White") ? "Black" : "White";
            logger.info("CHECKMATE DETECTED! Winner: " + winner);
            showEndGameScreen("Checkmate! " + winner + " wins.");
            return; // Stop further updates
        } else if (board.isStalemate(board.getCurrentTurn())) {
            logger.info("STALEMATE DETECTED!");
            showEndGameScreen("Stalemate! It's a draw.");
            return; // Stop further updates
        } else if (board.isInCheck(board.getCurrentTurn())) {
            statusPanel.setStatus(board.getCurrentTurn() + " is in Check!");
        } else {
            statusPanel.setStatus(board.getCurrentTurn() + "'s turn");
        }
          statusPanel.setMoveCount(board.getMoveHistory().size());
        statusPanel.updateCapturedPieces(board.getWhiteCapturedPieces(), board.getBlackCapturedPieces());
        
        // Show last move if available
        if (!board.getMoveHistory().isEmpty()) {
            Move lastMove = board.getMoveHistory().get(board.getMoveHistory().size() - 1);
            statusPanel.setLastMove(lastMove.getAlgebraicNotation());
        }
        
        // Check for AI's turn, but don't re-trigger if it's already thinking
        if (gameMode == GameMode.AI && board.getCurrentTurn().equals("Black") && !isAITurn) {
            handleAiMove();
        }
    }
    
    public void sendChatMessage(String message) {
        if (networkClient != null) {
            networkClient.sendChatMessage(message);
        }
        if (chatPanel != null) {
            chatPanel.addMessage("You: " + message);
        }
    }
    
    private void handleAiMove() {
        isAITurn = true;
        statusPanel.setStatus("AI is thinking...");

        aiMoveWorker = new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() throws Exception {
                // Add a delay for a more natural feel
                Thread.sleep(1000);
                return aiEngine.getBestMove(board, 12);
            }

            @Override
            protected void done() {
                try {
                    Move bestMove = get();
                    if (bestMove != null) {
                        makeMove(bestMove);
                    } else {
                        // AI returned null, meaning no legal moves exist.
                        // The game is over, so we determine if it's checkmate or stalemate.
                        isAITurn = false;
                        logger.warn("AI has no legal moves. Game is over.");
                        if (board.isInCheck(board.getCurrentTurn())) {
                            String winner = board.getCurrentTurn().equals("White") ? "Black" : "White";
                            showEndGameScreen("Checkmate! " + winner + " wins.");
                        } else {
                            showEndGameScreen("Stalemate! It's a draw.");
                        }
                    }
                } catch (java.util.concurrent.CancellationException e) {
                    logger.warn("AI move task was cancelled.");
                    isAITurn = false;
                } catch (Exception e) {
                    logger.error("Error getting AI move: " + e.getMessage(), e);
                    // Reset AI turn on error
                    isAITurn = false;
                    statusPanel.setStatus("AI error. Your turn.");
                }
            }
        };
        aiMoveWorker.execute();
    }
    
    private void makeMove(Move move) {
        if (move != null) {
            this.pendingMove = move;
            
            // If user is currently dragging, delay the animation until drag completes
            // This prevents visual conflicts between drag and AI move animations
            if (boardPanel.isDragInProgress()) {
                // Store the pending move but don't animate yet
                // The animation will be triggered when drag completes
                return;
            }
            
            boardPanel.animateMove(move);
        }
    }
    
    public void onAnimationFinished() {
        if (pendingMove != null) {
            // In multiplayer mode, don't update the board locally
            // Wait for the server's BOARD:FEN response instead
            if (gameMode != GameMode.MULTIPLAYER_HOST && gameMode != GameMode.MULTIPLAYER_CLIENT) {
                board.makeMove(pendingMove);
                
                // Check for game ending conditions after the move
                String gameResult = board.checkGameEndingConditions();
                if (gameResult != null) {
                    // Game is over, show summary dialog
                    String displayMessage = getGameEndDisplayMessage(gameResult);
                    SwingUtilities.invokeLater(() -> {
                        GameSummaryDialog.showGameSummary(this, displayMessage);
                    });
                    return; // Don't continue with normal updates
                }
            }

            // If in a multiplayer game, send the completed move to the network.
            if (gameMode == GameMode.MULTIPLAYER_HOST || gameMode == GameMode.MULTIPLAYER_CLIENT) {
                sendMoveToNetwork(pendingMove);
            }

            pendingMove = null;

            // If an AI move was just made, unlock the board for the player
            if (isAITurn) {
                isAITurn = false;
            }

            // Only repaint if user is not currently dragging a piece
            // This prevents the board from resetting during drag operations
            if (!boardPanel.isDragInProgress()) {
                boardPanel.repaint();
            } else {
                // Mark the board as needing update after drag completes
                boardPanel.forceUpdateBoard();
            }
            
            updateStatus();
        }
    }
    
    /**
     * Convert game result to display message
     */
    private String getGameEndDisplayMessage(String gameResult) {
        switch (gameResult) {
            case "CHECKMATE_WHITE":
                return "Checkmate! White wins!";
            case "CHECKMATE_BLACK":
                return "Checkmate! Black wins!";
            case "STALEMATE":
                return "Stalemate! The game is a draw.";
            case "DRAW":
                return "Draw! The game is a draw.";
            default:
                return "Game Over";
        }
    }
    
    public void showEndGameScreen(String message) {
        // Build the summary message
        StringBuilder summary = new StringBuilder();
        summary.append(message).append("\n\n");
        summary.append("Total Moves: ").append(board.getMoveHistory().size()).append("\n\n");

        summary.append("Pieces Captured by White:\n");
        for (Piece p : board.getWhiteCapturedPieces()) {
            summary.append(p.getSymbol()).append(" ");
        }
        summary.append("\n\n");

        summary.append("Pieces Captured by Black:\n");
        for (Piece p : board.getBlackCapturedPieces()) {
            summary.append(p.getSymbol()).append(" ");
        }
        summary.append("\n");

        // Use JTextArea for better formatting in JOptionPane
        JTextArea textArea = new JTextArea(summary.toString());
        textArea.setEditable(false);
        textArea.setBackground(null);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));

        // Show the dialog on the EDT to be safe
        SwingUtilities.invokeLater(() -> {
            Object[] options = {"Play Again", "Main Menu", "Exit"};
            int choice = JOptionPane.showOptionDialog(this,
                    textArea,
                    "Game Over",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                // Play Again - restart the same game mode
                this.dispose();                switch (gameMode) {
                    case LOCAL:
                        Main.startLocalGame();
                        break;
                    case AI:
                        Main.startAIGame();
                        break;
                    case HOST:
                        Main.startServer();
                        break;
                    case CLIENT:
                        Main.startClient();
                        break;
                    case MULTIPLAYER_HOST:
                        Main.startServer();
                        break;
                    case MULTIPLAYER_CLIENT:
                        Main.startClient();
                        break;
                }
            } else if (choice == JOptionPane.NO_OPTION) {
                // Main Menu
                this.dispose();
                Main.main(new String[]{}); // Restart the main menu
            } else {
                // Exit
                System.exit(0);
            }
        });
    }
    
    private void requestHint() {
        if (gameMode != GameMode.AI || !board.getCurrentTurn().equals("White") || hintsRemaining <= 0) {
            return; // Only allow hints for the player in AI mode
        }

        new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() throws Exception {
                return aiEngine.getBestMove(board, 5); // Use a lower depth for a quick hint
            }

            @Override
            protected void done() {
                try {
                    Move hintMove = get();
                    if (hintMove != null) {
                        boardPanel.highlightHint(hintMove);
                        hintsRemaining--;
                        statusPanel.setHintsRemaining(hintsRemaining);
                    }
                } catch (Exception e) {
                    logger.error("Error getting hint: " + e.getMessage(), e);
                }
            }
        }.execute();
    }
    
    @Override
    public void dispose() {
        if (aiMoveWorker != null && !aiMoveWorker.isDone()) {
            aiMoveWorker.cancel(true); // Cancel the background task
        }
        if (aiEngine != null) {
            try {
                aiEngine.close();
            } catch (IOException e) {
                logger.error("Error closing AI engine: " + e.getMessage());
            }

        }
        if (networkClient != null) {
            networkClient.disconnect();
        }
        if (networkServer != null) {
            networkServer.stop();
        }
        super.dispose();
    }

    public void setPlayerColor(String color) {
        this.playerColor = color;
        logger.info("Player color set to: " + color);
        if (boardPanel != null) {
            boardPanel.setPlayerColor(color);
        }
    }
    
    private void handleAITurn() {
        statusPanel.updateStatus("AI is thinking...");
        // Implementation of handleAITurn method
    }

    public void onChatMessageSent(String message) {
        if (networkClient != null) {
            networkClient.sendChatMessage(message);
        }
        // The message will be displayed when it's broadcast back from the server.
        // chatPanel.addMessage("You: " + message);
    }

    /**
     * Create a move with proper move type detection
     */
    private Move createMove(Position from, Position to, Piece piece, Piece capturedPiece) {
        // Check if this is a castling move
        if (piece.getType().equals("King")) {
            int colDiff = to.getCol() - from.getCol();
            if (Math.abs(colDiff) == 2) {
                // This is a castling move
                if (colDiff > 0) {
                    return new Move(from, to, piece, capturedPiece, Move.MoveType.CASTLE_KINGSIDE, null, false, false);
                } else {
                    return new Move(from, to, piece, capturedPiece, Move.MoveType.CASTLE_QUEENSIDE, null, false, false);
                }
            }
        }
        
        // Check if this is an en passant move
        if (piece.getType().equals("Pawn") && board.getEnPassantTarget() != null && 
            board.getEnPassantTarget().equals(to) && capturedPiece == null) {
            return new Move(from, to, piece, capturedPiece, Move.MoveType.EN_PASSANT, null, false, false);
        }
        
        // Check if this is a promotion move
        if (piece.getType().equals("Pawn")) {
            if ((piece.getColor().equals("White") && to.getRow() == 0) || 
                (piece.getColor().equals("Black") && to.getRow() == 7)) {
                // For now, default to Queen promotion - can be enhanced later
                return new Move(from, to, piece, capturedPiece, Move.MoveType.PAWN_PROMOTION, "Queen", false, false);
            }
        }
        
        // Regular move
        return new Move(from, to, piece, capturedPiece);
    }

    /**
     * Called when a drag operation completes, triggers any pending AI animations
     */
    public void onDragCompleted() {
        // If there's a pending move that was delayed due to drag, animate it now
        if (pendingMove != null && !boardPanel.isDragInProgress()) {
            boardPanel.animateMove(pendingMove);
        }
    }
}