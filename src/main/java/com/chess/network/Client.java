package com.chess.network;

import com.chess.gui.ChessGUI;
import com.chess.model.Board;
import com.chess.model.Move;
import com.chess.model.Position;
import com.chess.model.Piece;
import com.chess.engine.ChessEngine;
import com.chess.utils.Logger;

import javax.swing.SwingUtilities;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for connecting to chess server
 */
public class Client {
    private static final Logger logger = Logger.getLogger(Client.class);
    
    private String serverAddress;
    private int port;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ChessGUI gui;
    private boolean connected;
    private Thread listenerThread;
    private String playerColor;
    private Board board;
    
    public Client(String serverAddress, int port, ChessGUI gui) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.gui = gui;
        this.board = new Board(); // The client maintains its own board state
        this.connected = false;
    }
    
    public void connect() throws IOException {
        socket = new Socket(serverAddress, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        connected = true;
        
        logger.info("Connected to server " + serverAddress + ":" + port);
        
        // Start listening for server messages
        listenerThread = new Thread(this::listenForMessages);
        listenerThread.start();
    }
    
    private void listenForMessages() {
        try {
            String line;
            while (connected && (line = reader.readLine()) != null) {
                handleServerMessage(line);
            }
        } catch (IOException e) {
            if (connected) {
                logger.error("Error reading from server: " + e.getMessage());
                gui.receiveChatMessage("[X] Connection lost to server");
                // Notify GUI about connection loss
                SwingUtilities.invokeLater(() -> {
                    if (gui.getChatPanel() != null) {
                        gui.getChatPanel().setConnectionActive(false);
                    }
                });
            }
        } finally {
            disconnect();
        }
    }
    
    private void handleServerMessage(String message) {
        if (message.startsWith("MOVE:")) {
            // Ignore individual move messages - we'll rely on BOARD:FEN for synchronization
            // This ensures both players always have identical board states
            logger.debug("Ignoring MOVE message, relying on BOARD:FEN for synchronization");
        } else if (message.startsWith("CHAT:")) {
            // Handle chat message
            String chatMessage = message.substring(5);
            gui.receiveChatMessage(chatMessage);
        } else if (message.startsWith("BOARD:")) {
            // Handle board state update - this is our primary synchronization method
            String fen = message.substring(6);
            try {
                this.board = Board.fromFEN(fen);
                gui.updateBoard(this.board);
                logger.info("Board state updated from FEN: " + fen);
            } catch (Exception e) {
                logger.error("Error parsing board state: " + fen, e);
            }
        } else if (message.startsWith("COLOR:")) {
            this.playerColor = message.substring(6);
            gui.setPlayerColor(this.playerColor);
            logger.info("Assigned color: " + this.playerColor);
        } else if (message.startsWith("TURN:")) {
            String turn = message.substring(5);
            gui.updateTurn(turn);
        } else if (message.startsWith("GAMEOVER:")) {
            String reason = message.substring(9);
            gui.showEndGameScreen(reason);
            disconnect();
        } else if (message.equals("START")) {
            gui.receiveChatMessage("[*] Game is starting! Good luck!");
            // Enable chat functionality
            SwingUtilities.invokeLater(() -> {
                if (gui.getChatPanel() != null) {
                    gui.getChatPanel().setConnectionActive(true);
                    gui.getChatPanel().addSystemMessage("You are now connected and ready to chat!");
                }
            });
        } else if (message.equals("GAME_FULL")) {
            gui.receiveChatMessage("[!] Game is full. Cannot join.");
            disconnect();
        } else if (message.startsWith("ERROR:")) {
            String error = message.substring(6);
            // Handle move-specific errors differently than general errors
            if (error.contains("Invalid move") || error.contains("Not your turn") || error.contains("Invalid piece")) {
                gui.onMoveRejectedByServer(error);
            } else {
                gui.receiveChatMessage("Error: " + error);
            }
        }
    }
    
    private Move parseMove(String moveString) {
        // Parse move in format "e2e4"
        if (moveString.length() != 4) {
            throw new IllegalArgumentException("Invalid move format: " + moveString);
        }
        
        Position from = new Position(moveString.substring(0, 2));
        Position to = new Position(moveString.substring(2, 4));
        
        // For network moves, we need to get the piece from our local board state
        Piece piece = board.getPiece(from);
        Piece captured = board.getPiece(to);
        
        if (piece == null) {
            throw new IllegalArgumentException("Invalid move: No piece at " + from.toAlgebraicNotation());
        }
        
        return new Move(from, to, piece, captured);
    }
    
    private Board parseFEN(String fen) {
        return Board.fromFEN(fen);
    }
    
    public void sendMove(Move move) {
        if (!connected) {
            logger.warn("Cannot send move - not connected to server");
            return;
        }
        
        String moveString = "MOVE:" + move.toSimpleString();
        logger.info("Sending move to server: " + moveString + " (from: " + move.getFrom() + ", to: " + move.getTo() + ")");
        writer.println(moveString);
        logger.debug("Sent move: " + moveString);
    }
    
    public void sendChatMessage(String message) {
        if (!connected) {
            logger.warn("Cannot send chat message - not connected to server");
            gui.receiveChatMessage("[X] Cannot send message: Not connected to server");
            return;
        }
        
        if (message == null || message.trim().isEmpty()) {
            logger.warn("Attempted to send empty chat message");
            return;
        }
        
        try {
            String chatString = "CHAT:" + message;
            writer.println(chatString);
            logger.info("Chat message sent to server: " + message);
        } catch (Exception e) {
            logger.error("Failed to send chat message: " + e.getMessage());
            gui.receiveChatMessage("[X] Failed to send message: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        connected = false;
        
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            logger.error("Error closing client connection: " + e.getMessage());
        }
        
        logger.info("Disconnected from server");
    }
    
    public String getPlayerColor() {
        return playerColor;
    }

    public boolean isConnected() {
        return connected;
    }
} 