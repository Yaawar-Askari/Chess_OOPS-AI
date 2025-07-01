package com.chess.gui;

import com.chess.model.*;
import com.chess.engine.ChessEngine;
import com.chess.network.Client;
import com.chess.utils.Logger;
import com.chess.utils.GameStateManager;
import com.chess.Main;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.File;

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
    
    public ChessGUI() {
        logger.info("Creating ChessGUI...");
        this.board = new Board();
        logger.info("Board created successfully");

        // Constructor now directly initializes the GUI.
        // The calling method will be responsible for ensuring it's on the EDT.
        initializeGUI();

        logger.info("ChessGUI initialization completed");
    }
    
    private void initializeGUI() {
        logger.info("Initializing GUI components...");
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create menu bar
        createMenuBar();
        
        // Create main layout
        setLayout(new BorderLayout());
        
        // Create and add all components
        logger.info("Creating board panel...");
        boardPanel = new ChessBoardPanel(board, this);
        add(boardPanel, BorderLayout.CENTER);
        
        logger.info("Creating status panel...");
        statusPanel = new GameStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
        
        // Don't create chat panel by default - it will be created only for multiplayer modes
        
        // Initialize AI engine
        logger.info("Initializing AI engine...");
        try {
            aiEngine = new ChessEngine();
            logger.info("AI engine initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize AI engine: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Failed to initialize AI engine. AI features will be disabled.", 
                "Warning", JOptionPane.WARNING_MESSAGE);
        }
        
        updateStatus();
        
        // Launch in a maximized state by default
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setPreferredSize(new Dimension(1200, 800)); // Set a preferred size for when un-maximized
        setLocationRelativeTo(null); // Center on screen
        
        logger.info("GUI initialization completed");
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
        // Let user choose their color
        String[] options = {"Play as White", "Play as Black"};
        int choice = JOptionPane.showOptionDialog(null,
            "Choose your color:",
            "AI Game Setup",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice == -1) {
            // User cancelled, return to main menu
            return;
        }
        
        gameMode = GameMode.AI;
        playerColor = (choice == 0) ? "White" : "Black";
        
        // Reset the board for a new game
        board = new Board();
        
        // Initialize AI engine
        try {
            aiEngine = new ChessEngine();
            logger.info("AI engine initialized for AI game mode");
        } catch (Exception e) {
            logger.error("Failed to initialize AI engine: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Failed to initialize AI engine. Please check Stockfish installation.", 
                "AI Engine Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Initialize GUI components
        getContentPane().removeAll();
        boardPanel = new ChessBoardPanel(board, this);
        statusPanel = new GameStatusPanel("AI");
        
        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        setTitle("Chess Game - vs AI (You: " + playerColor + ")");
        
        // Set board orientation based on player color
        if (boardPanel != null) {
            boardPanel.setPlayerColor(playerColor);
        }
        
        updateStatus();
        
        // If player chose Black, AI (White) makes the first move
        if (playerColor.equals("Black")) {
            SwingUtilities.invokeLater(() -> {
                handleAiMove();
            });
        }
        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        revalidate();
        repaint();
        
        logger.info("AI game started - Player: " + playerColor + ", AI: " + (playerColor.equals("White") ? "Black" : "White"));
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
            // Start chat disabled until connection is established
            chatPanel.setConnectionActive(false);
            // Add welcome message
            SwingUtilities.invokeLater(() -> {
                chatPanel.addSystemMessage("Connecting to multiplayer game...");
            });
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
        if (isAITurn) {
            logger.info("Move attempt blocked - AI is thinking");
            return; // Prevent moves during AI's turn
        }

        Piece pieceToMove = board.getPiece(from);
        if (pieceToMove == null) {
            clearSelection();
            return; // No piece to move
        }

        // Check if the piece belongs to the current player in multiplayer mode
        if (gameMode == GameMode.MULTIPLAYER_HOST || gameMode == GameMode.MULTIPLAYER_CLIENT) {
            // In multiplayer, check against player's assigned color, not board's current turn
            if (this.playerColor != null && !pieceToMove.getColor().equals(this.playerColor)) {
                logger.info("Attempted to move opponent's piece (" + pieceToMove.getColor() + ") - your color is " + this.playerColor);
                clearSelection();
                return; // Not your piece
            }
        } else {
            // For local/AI games, check against board's current turn
            if (!pieceToMove.getColor().equals(board.getCurrentTurn())) {
                logger.info("Attempted to move " + pieceToMove.getColor() + " piece on " + board.getCurrentTurn() + "'s turn");
                clearSelection();
                return; // Not your turn
            }
        }
        
        // In AI mode, prevent player from moving AI's pieces
        if (gameMode == GameMode.AI && this.playerColor != null) {
            if (!pieceToMove.getColor().equals(this.playerColor)) {
                logger.info("Attempted to move AI's piece (" + pieceToMove.getColor() + ") in AI mode");
                clearSelection();
                return;
            }
        }

        Move move = createMove(from, to, pieceToMove, board.getPiece(to));
        
        // For multiplayer modes, skip local validation and let server decide
        if (gameMode == GameMode.MULTIPLAYER_HOST || gameMode == GameMode.MULTIPLAYER_CLIENT) {
            // In multiplayer, send move to server for validation
            // The server will validate and either apply or reject the move
            logger.info("Multiplayer mode: sending move to server for validation: " + move);
            this.pendingMove = move;
            
            // Send move to server immediately without local validation
            sendMoveToNetwork(move);
            
            // Clear selection but don't animate yet - wait for server response
            clearSelection();
        } else {
            // For local/AI games, validate locally first
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
    }

    /**
     * Handles a user clicking on a square (for click-to-move).
     */
    public void onSquareClicked(Position position) {
        if (isAITurn) {
            logger.info("Square click blocked - AI is thinking");
            return; // Prevent clicks during AI's turn
        }

        if (selectedFrom != null) {
            // A piece is already selected, try to move it to the new position
            onMoveAttempted(selectedFrom, position);
        } else if (board.getPiece(position) != null) {
            // No piece is selected, so select the one clicked (if it's the current player's)
            Piece clickedPiece = board.getPiece(position);
            
            // Check if it's the current player's turn and their piece
            if (clickedPiece.getColor().equals(board.getCurrentTurn())) {
                // In AI mode, only allow selection of player's pieces
                if (gameMode == GameMode.AI && this.playerColor != null) {
                    if (!clickedPiece.getColor().equals(this.playerColor)) {
                        logger.info("Attempted to select AI's piece in AI mode");
                        return;
                    }
                }
                
                selectedFrom = position;
                boardPanel.highlightValidMoves(clickedPiece);
                logger.info("Selected piece: " + clickedPiece.getType() + " at " + position.toString());
            }
        }
    }

    private void clearSelection() {
        selectedFrom = null;
        boardPanel.clearHighlights();
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
                logger.info("Chat message received and displayed: " + message);
            } else {
                // For non-multiplayer modes, log the message
                logger.info("Chat message received (no chat panel): " + message);
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
        
        // Check for AI's turn based on player color, but don't re-trigger if it's already thinking
        if (gameMode == GameMode.AI && !isAITurn) {
            String aiColor = playerColor.equals("White") ? "Black" : "White";
            if (board.getCurrentTurn().equals(aiColor)) {
                handleAiMove();
            }
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
        if (isAITurn) {
            // Prevent multiple AI move calculations
            return;
        }
        
        isAITurn = true;
        String aiColor = playerColor.equals("White") ? "Black" : "White";
        statusPanel.setStatus("AI (" + aiColor + ") is thinking...");
        logger.info("AI (" + aiColor + ") is calculating move...");

        aiMoveWorker = new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() throws Exception {
                // Add a small delay for a more natural feel
                Thread.sleep(500);
                
                // Get best move from Stockfish with 2 second time limit
                Move bestMove = aiEngine.getBestMove(board, 2000);
                logger.info("Stockfish returned move: " + (bestMove != null ? bestMove.toString() : "null"));
                return bestMove;
            }

            @Override
            protected void done() {
                try {
                    Move bestMove = get();
                    if (bestMove != null && board.isValidMove(bestMove)) {
                        logger.info("AI (" + aiColor + ") played move: " + bestMove.getAlgebraicNotation());
                        makeMove(bestMove);
                    } else {
                        // AI returned null or invalid move, check game state
                        isAITurn = false;
                        logger.warn("AI has no legal moves or returned invalid move. Checking game state...");
                        
                        if (board.isCheckmate(aiColor)) {
                            String winner = aiColor.equals("White") ? "Black" : "White";
                            showEndGameScreen("Checkmate! " + winner + " wins.");
                        } else if (board.isStalemate(aiColor)) {
                            showEndGameScreen("Stalemate! It's a draw.");
                        } else {
                            // This shouldn't happen, but handle gracefully
                            logger.error("AI returned no move but game is not over. This may be a bug.");
                            statusPanel.setStatus("AI error. Game continues.");
                        }
                    }
                } catch (java.util.concurrent.CancellationException e) {
                    logger.warn("AI move task was cancelled.");
                    isAITurn = false;
                    statusPanel.setStatus("AI move cancelled.");
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
            // Only send if this is NOT a multiplayer mode (for local/AI games only)
            // For multiplayer, we already sent the move in onMoveAttempted()
            if (gameMode != GameMode.MULTIPLAYER_HOST && gameMode != GameMode.MULTIPLAYER_CLIENT) {
                // This is for local/AI games only
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
                this.dispose();
                SwingUtilities.invokeLater(() -> {
                    ChessGUI newGui = new ChessGUI();
                    switch (gameMode) {
                        case LOCAL:
                            newGui.startLocalGame();
                            break;
                        case AI:
                            newGui.startAIGame();
                            break;
                        case HOST:
                            // Note: Host and server functionality needs to be re-implemented
                            // For now, start local game
                            newGui.startLocalGame();
                            break;
                        case CLIENT:
                            // Note: Client functionality needs to be re-implemented
                            // For now, start local game
                            newGui.startLocalGame();
                            break;
                        case MULTIPLAYER_HOST:
                            // Note: Multiplayer host functionality needs to be re-implemented
                            // For now, start local game
                            newGui.startLocalGame();
                            break;
                        case MULTIPLAYER_CLIENT:
                            // Note: Multiplayer client functionality needs to be re-implemented
                            // For now, start local game
                            newGui.startLocalGame();
                            break;
                    }
                    newGui.setVisible(true);
                });
            } else if (choice == JOptionPane.NO_OPTION) {
                // Main Menu - restart the application
                this.dispose();
                SwingUtilities.invokeLater(() -> {
                    try {
                        com.chess.Main.main(new String[]{});
                    } catch (Exception e) {
                        logger.error("Error restarting main menu: " + e.getMessage());
                        System.exit(0);
                    }
                });
            } else {
                // Exit
                System.exit(0);
            }
        });
    }
    
    private void requestHint() {
        // Check if hints are available
        if (hintsRemaining <= 0) {
            JOptionPane.showMessageDialog(this, 
                "No hints remaining for this game.", 
                "Hint System", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Check if it's the player's turn (not during AI thinking)
        if (isAITurn) {
            JOptionPane.showMessageDialog(this, 
                "Please wait for the AI to make its move.", 
                "Hint System", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Disable hint button temporarily to prevent spam
        if (statusPanel.getHintButton() != null) {
            statusPanel.getHintButton().setEnabled(false);
            statusPanel.getHintButton().setText("🔄 Thinking...");
        }
        
        logger.info("Hint requested for " + board.getCurrentTurn() + " in " + gameMode + " mode");
        
        new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() throws Exception {
                if (aiEngine != null && aiEngine.isEngineAvailable()) {
                    // Use Stockfish for a deeper analysis (reasonable search time for hints)
                    return aiEngine.getBestMove(board, 2000); // 2 seconds for hint
                } else {
                    // Fallback to simple evaluation or random move
                    logger.warn("Stockfish not available, using fallback for hint");
                    return aiEngine != null ? aiEngine.getRandomMove(board) : null;
                }
            }

            @Override
            protected void done() {
                try {
                    Move hintMove = get();
                    if (hintMove != null) {
                        // Highlight the suggested move
                        boardPanel.highlightHint(hintMove);
                        
                        // Log the hint
                        String moveNotation = hintMove.getFrom().toString() + " → " + hintMove.getTo().toString();
                        logger.info("Hint generated for " + board.getCurrentTurn() + ": " + moveNotation);
                        
                        // Decrement hints remaining
                        hintsRemaining--;
                        statusPanel.setHintsRemaining(hintsRemaining);
                        
                        // Show hint information to user
                        String pieceInfo = hintMove.getPiece().getType() + " " + hintMove.getPiece().getColor();
                        JOptionPane.showMessageDialog(ChessGUI.this, 
                            "💡 Hint: Move " + pieceInfo + " from " + 
                            hintMove.getFrom().toString().toUpperCase() + " to " + 
                            hintMove.getTo().toString().toUpperCase() + "\n\n" +
                            "The highlighted squares show the suggested move.\n" +
                            "Hints remaining: " + hintsRemaining, 
                            "Game Teacher", JOptionPane.INFORMATION_MESSAGE);
                            
                    } else {
                        logger.warn("Unable to generate hint - no valid move found");
                        JOptionPane.showMessageDialog(ChessGUI.this, 
                            "Unable to generate hint at this time.\nPlease try again.", 
                            "Hint System", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    logger.error("Error getting hint: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(ChessGUI.this, 
                        "Unable to generate hint at this time.\nError: " + e.getMessage(), 
                        "Hint System Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Re-enable hint button
                    if (statusPanel.getHintButton() != null) {
                        statusPanel.getHintButton().setEnabled(true);
                        statusPanel.getHintButton().setText("💡 Get Hint");
                    }
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
        super.dispose();
    }

    public void setPlayerColor(String color) {
        this.playerColor = color;
        logger.info("Player color set to: " + color);
        if (boardPanel != null) {
            boardPanel.setPlayerColor(color);
        }
    }
    
    /**
     * Get the chat panel for connection status updates
     */
    public ChatPanel getChatPanel() {
        return chatPanel;
    }

    public void onChatMessageSent(String message) {
        logger.info("Chat message sent by user: " + message);
        
        if (networkClient != null && networkClient.isConnected()) {
            // Send in background to avoid blocking UI
            SwingWorker<Void, Void> sendWorker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    networkClient.sendChatMessage(message);
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        get(); // Check for exceptions
                        logger.debug("Chat message sent successfully via network");
                    } catch (Exception e) {
                        logger.error("Failed to send chat message: " + e.getMessage());
                        SwingUtilities.invokeLater(() -> {
                            if (chatPanel != null) {
                                chatPanel.addMessage("[X] Failed to send: " + e.getMessage());
                                chatPanel.setConnectionActive(false);
                            }
                        });
                    }
                }
            };
            
            sendWorker.execute();
        } else {
            logger.warn("Cannot send chat message - no network connection");
            if (chatPanel != null) {
                chatPanel.addMessage("[X] No network connection");
                chatPanel.setConnectionActive(false);
            }
        }
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
    
    /**
     * Creates the menu bar with File menu containing Save/Load options
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        
        // Save Game item
        JMenuItem saveGameItem = new JMenuItem("Save Game");
        saveGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveGameItem.addActionListener(e -> saveGame());
        fileMenu.add(saveGameItem);
        
        // Load Game item
        JMenuItem loadGameItem = new JMenuItem("Load Game");
        loadGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        loadGameItem.addActionListener(e -> loadGame());
        fileMenu.add(loadGameItem);
        
        fileMenu.addSeparator();
        
        // New Game item
        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newGameItem.addActionListener(e -> startNewGame());
        fileMenu.add(newGameItem);
        
        menuBar.add(fileMenu);
        
        // View menu with theme options
        JMenu viewMenu = new JMenu("View");
        
        // Theme submenu
        JMenu themeMenu = new JMenu("Theme");
        
        ButtonGroup themeGroup = new ButtonGroup();
        
        JRadioButtonMenuItem lightTheme = new JRadioButtonMenuItem("Light");
        lightTheme.addActionListener(e -> {
            Main.applyTheme(Main.Theme.LIGHT);
            SwingUtilities.updateComponentTreeUI(this);
        });
        themeGroup.add(lightTheme);
        themeMenu.add(lightTheme);
        
        JRadioButtonMenuItem darkTheme = new JRadioButtonMenuItem("Dark");
        darkTheme.addActionListener(e -> {
            Main.applyTheme(Main.Theme.DARK);
            SwingUtilities.updateComponentTreeUI(this);
        });
        themeGroup.add(darkTheme);
        themeMenu.add(darkTheme);
        
        JRadioButtonMenuItem intellijTheme = new JRadioButtonMenuItem("IntelliJ");
        intellijTheme.addActionListener(e -> {
            Main.applyTheme(Main.Theme.INTELLIJ);
            SwingUtilities.updateComponentTreeUI(this);
        });
        themeGroup.add(intellijTheme);
        themeMenu.add(intellijTheme);
        
        JRadioButtonMenuItem darculaTheme = new JRadioButtonMenuItem("Darcula");
        darculaTheme.addActionListener(e -> {
            Main.applyTheme(Main.Theme.DARCULA);
            SwingUtilities.updateComponentTreeUI(this);
        });
        themeGroup.add(darculaTheme);
        themeMenu.add(darculaTheme);
        
        // Select current theme
        Main.Theme currentTheme = Main.getCurrentTheme();
        switch (currentTheme) {
            case LIGHT: lightTheme.setSelected(true); break;
            case DARK: darkTheme.setSelected(true); break;
            case INTELLIJ: intellijTheme.setSelected(true); break;
            case DARCULA: darculaTheme.setSelected(true); break;
        }
        
        viewMenu.add(themeMenu);
        menuBar.add(viewMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Saves the current game state to a file
     */
    private void saveGame() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Chess Game");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Chess Game Files (*.json)", "json"));
            fileChooser.setSelectedFile(new File("chess_game_" + System.currentTimeMillis() + ".json"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                
                // Ensure .json extension
                if (!fileToSave.getName().toLowerCase().endsWith(".json")) {
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".json");
                }
                
                // Determine current game status
                String gameStatus = "ongoing";
                if (board.isCheckmate(board.getCurrentTurn())) {
                    gameStatus = "checkmate";
                } else if (board.isStalemate(board.getCurrentTurn())) {
                    gameStatus = "stalemate";
                } else if (board.isInCheck(board.getCurrentTurn())) {
                    gameStatus = "check";
                }
                
                // Get current player color
                String currentPlayerColor = (playerColor != null) ? playerColor : "White";
                
                // Get current game mode
                String currentGameMode = (gameMode != null) ? gameMode.toString() : "LOCAL";
                
                // Save the game
                GameStateManager.GameState gameState = GameStateManager.createGameState(
                    board,
                    board.getMoveHistoryInAlgebraicNotation(),
                    gameStatus,
                    currentPlayerColor,
                    currentGameMode
                );
                
                try {
                    GameStateManager.saveGameToFile(gameState, fileToSave);
                    JOptionPane.showMessageDialog(this, 
                        "Game saved successfully to:\n" + fileToSave.getAbsolutePath(), 
                        "Game Saved", 
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception saveEx) {
                    throw saveEx; // Re-throw to be caught by outer catch block
                }
            }
        } catch (Exception e) {
            logger.error("Error saving game: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "An error occurred while saving the game:\n" + e.getMessage(), 
                "Save Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Loads a game state from a file
     */
    private void loadGame() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Load Chess Game");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Chess Game Files (*.json)", "json"));
            
            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToLoad = fileChooser.getSelectedFile();
                
                GameStateManager.GameState gameState = GameStateManager.loadGameFromFile(fileToLoad);
                
                if (gameState != null) {
                    // Confirm with user before loading
                    int confirm = JOptionPane.showConfirmDialog(this,
                        "Loading this game will replace the current game.\nAre you sure you want to continue?",
                        "Confirm Load",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Apply the loaded game state
                        applyGameState(gameState);
                        
                        JOptionPane.showMessageDialog(this, 
                            "Game loaded successfully!\nSaved: " + gameState.getSavedAt(), 
                            "Game Loaded", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to load game. The file may be corrupted or invalid.", 
                        "Load Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            logger.error("Error loading game: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "An error occurred while loading the game:\n" + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Applies a loaded game state to the current game
     */
    private void applyGameState(GameStateManager.GameState gameState) {
        try {
            // Create a new board from the FEN state
            Board newBoard = Board.fromFEN(gameState.getBoardFEN());
            if (newBoard != null) {
                // Successfully loaded board state
                this.board = newBoard;
                
                // Update the board panel with the new board
                if (boardPanel != null) {
                    boardPanel.setBoard(this.board);
                }
                
                // Set the current turn
                board.setCurrentTurn(gameState.getCurrentTurn());
                
                // Restore move history if available
                if (gameState.getMoveHistory() != null && !gameState.getMoveHistory().isEmpty()) {
                    // Note: The move history in algebraic notation is for display purposes
                    // The actual board state is restored from FEN
                    // We could parse and reconstruct Move objects if needed for full functionality
                }
                
                // Update player color and game mode if available
                if (gameState.getPlayerColor() != null) {
                    this.playerColor = gameState.getPlayerColor();
                }
                
                if (gameState.getGameMode() != null) {
                    try {
                        this.gameMode = GameMode.valueOf(gameState.getGameMode());
                    } catch (IllegalArgumentException e) {
                        // Default to LOCAL if invalid game mode
                        this.gameMode = GameMode.LOCAL;
                    }
                }
                
                // Update the GUI
                updateStatus();
                if (boardPanel != null) {
                    boardPanel.repaint();
                }
                
                // Reset AI state if needed
                if (gameMode == GameMode.AI) {
                    isAITurn = "Black".equals(board.getCurrentTurn());
                }
                
            } else {
                throw new RuntimeException("Failed to load board state from FEN: " + gameState.getBoardFEN());
            }
        } catch (Exception e) {
            logger.error("Error applying game state: " + e.getMessage());
            throw new RuntimeException("Failed to apply loaded game state: " + e.getMessage());
        }
    }
    
    /**
     * Starts a new game by resetting the board
     */
    private void startNewGame() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Starting a new game will lose the current game.\nAre you sure you want to continue?",
            "Confirm New Game",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Reset the board
            this.board = new Board();
            
            // Update the board panel
            if (boardPanel != null) {
                boardPanel.setBoard(this.board);
            }
            
            // Reset game state
            selectedFrom = null;
            isAITurn = false;
            
            // Update GUI
            updateStatus();
            if (boardPanel != null) {
                boardPanel.repaint();
            }
        }
    }

    public void onMoveRejectedByServer(String reason) {
        SwingUtilities.invokeLater(() -> {
            logger.warn("Server rejected move: " + reason);
            
            // Show visual feedback for rejected move
            if (pendingMove != null) {
                Position to = pendingMove.getTo();
                // Flash the destination square red to indicate invalid move
                boardPanel.highlightSquare(to, new Color(255, 0, 0, 180));
                
                // Play system beep for audio feedback
                java.awt.Toolkit.getDefaultToolkit().beep();
                
                // Clear highlights after a short delay
                Timer timer = new Timer(500, e -> {
                    boardPanel.clearHighlights();
                });
                timer.setRepeats(false);
                timer.start();
                
                pendingMove = null;
            }
            
            // Show error message in chat or status
            if (chatPanel != null) {
                chatPanel.addMessage("[!] Move rejected: " + reason);
            } else {
                statusPanel.setStatus("Invalid move: " + reason);
            }
        });
    }
}