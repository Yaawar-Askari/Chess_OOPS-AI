package com.chess.network;

import com.chess.model.Piece;
import com.chess.model.Position;
import com.chess.engine.ChessEngine;
import com.chess.model.Board;
import com.chess.model.Move;
import com.chess.utils.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server for handling online chess games
 */
public class Server {
    private static final Logger logger = Logger.getLogger(Server.class);
    
    private int port;
    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService executorService;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final Board gameBoard = new Board();
    
    public Server(int preferredPort) {
        this.port = findAvailablePort(preferredPort);
        this.executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * Find an available port starting from the preferred port
     */
    private int findAvailablePort(int preferredPort) {
        int port = preferredPort;
        while (port < preferredPort + 100) { // Try up to 100 ports
            try (ServerSocket testSocket = new ServerSocket(port)) {
                logger.info("Found available port: " + port);
                return port;
            } catch (IOException e) {
                port++;
            }
        }
        // If no port found in range, use port 0 to let the system assign one
        logger.warn("No available port found in range " + preferredPort + "-" + (preferredPort + 99) + ", using system-assigned port");
        return 0;
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        // If port was 0, get the actual assigned port
        if (port == 0) {
            port = serverSocket.getLocalPort();
        }
        running = true;
        logger.info("Server started on port " + port + ". Waiting for players...");
        
        while (running && clients.size() < 2) {
            try {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connecting: " + clientSocket.getInetAddress());
                
                synchronized (clients) {
                    if (clients.size() < 2) {
                        String color = (clients.isEmpty()) ? "White" : "Black";
                        ClientHandler clientHandler = new ClientHandler(clientSocket, this, color);
                        clients.add(clientHandler);
                        executorService.execute(clientHandler);
                        logger.info("Client connected as " + color + ". Total clients: " + clients.size());

                        // If both players are now connected, start the game
                        if (clients.size() == 2) {
                            logger.info("Two players connected. Starting game.");
                            broadcast("START");
                            broadcast("BOARD:" + gameBoard.toFEN());
                            broadcast("TURN:" + gameBoard.getCurrentTurn());
                        }
                    } else {
                        // This case is for safety, though the outer loop condition should prevent it.
                        rejectClient(clientSocket);
                    }
                }
            } catch (IOException e) {
                if (running) {
                    logger.error("Error accepting client connection: " + e.getMessage());
                }
            }
        }
        
        // If more than 2 people try to connect, reject them.
        while(running) {
            try {
                rejectClient(serverSocket.accept());
            } catch (IOException e) {
                if (running) {
                    logger.error("Error accepting client connection for rejection: " + e.getMessage());
                }
            }
        }
    }

    private void rejectClient(Socket clientSocket) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println("ERROR:Game is full.");
            clientSocket.close();
            logger.info("Rejected a client because the game is full.");
        } catch (IOException e) {
            logger.error("Error rejecting client: " + e.getMessage());
        }
    }
    
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Error closing server socket: " + e.getMessage());
            }
        }
        executorService.shutdown();
        logger.info("Server stopped");
    }
    
    public synchronized void handleMove(ClientHandler client, Move move) {
        logger.info("Server handling move from " + client.getPlayerColor() + ": " + move.getFrom() + " to " + move.getTo());
        logger.info("Current turn: " + gameBoard.getCurrentTurn());
        
        // Validate that it's the correct player's turn
        if (!gameBoard.getCurrentTurn().equals(client.getPlayerColor())) {
            logger.warn("Turn validation failed: " + client.getPlayerColor() + " tried to move on " + gameBoard.getCurrentTurn() + "'s turn");
            client.sendMessage("ERROR:Not your turn.");
            return;
        }
        
        // The move from the client might not have the correct piece instance from our board
        Piece pieceOnBoard = gameBoard.getPiece(move.getFrom());
        if (pieceOnBoard == null || !pieceOnBoard.getColor().equals(client.getPlayerColor())) {
            logger.warn("Piece validation failed: piece at " + move.getFrom() + " is " + (pieceOnBoard == null ? "null" : pieceOnBoard.getColor()));
            client.sendMessage("ERROR:Invalid piece.");
            return;
        }

        // Create a new move object with the server's piece instance
        Move serverMove = new Move(move.getFrom(), move.getTo(), pieceOnBoard, gameBoard.getPiece(move.getTo()));

        // Validate the move
        logger.info("Attempting to make move on server board...");
        if (gameBoard.makeMove(serverMove)) {
            logger.info("Move successful, broadcasting BOARD:FEN: " + gameBoard.toFEN());
            // Broadcast the updated board state to all clients using FEN
            // This ensures both players always have identical board states
            broadcast("BOARD:" + gameBoard.toFEN());

            // Check for all game end conditions
            String endCondition = gameBoard.checkGameEndingConditions();
            if (endCondition != null) {
                if (endCondition.startsWith("CHECKMATE_WHITE")) {
                    broadcast("GAMEOVER:Checkmate! White wins!");
                } else if (endCondition.startsWith("CHECKMATE_BLACK")) {
                    broadcast("GAMEOVER:Checkmate! Black wins!");
                } else if (endCondition.equals("STALEMATE")) {
                    broadcast("GAMEOVER:Stalemate! It's a draw.");
                } else if (endCondition.equals("DRAW")) {
                    broadcast("GAMEOVER:Draw! (50-move rule or insufficient material)");
                }
            } else {
                broadcast("TURN:" + gameBoard.getCurrentTurn());
                if(gameBoard.isInCheck(gameBoard.getCurrentTurn())) {
                    broadcast("CHAT:Check!");
                }
            }
        } else {
            logger.warn("Move validation failed: " + move.getFrom() + " to " + move.getTo() + " is not a valid move");
            client.sendMessage("ERROR:Invalid move.");
        }
    }
    
    public synchronized void handleChatMessage(ClientHandler client, String message) {
        logger.info("Chat message sent by " + client.getPlayerColor() + ": " + message);
        
        // Validate message (basic security check)
        if (message == null || message.trim().isEmpty()) {
            logger.warn("Empty chat message received from " + client.getPlayerColor());
            return;
        }
        
        // Limit message length to prevent spam
        String sanitizedMessage = message.length() > 200 ? message.substring(0, 200) + "..." : message;
        
        String formattedMessage = "CHAT:" + client.getPlayerColor() + ": " + sanitizedMessage;
        broadcast(formattedMessage);
        logger.info("Chat message broadcast: " + formattedMessage);
    }
    
    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        logger.info("Client " + client.getPlayerColor() + " disconnected. Remaining clients: " + clients.size());
        
        if (running) {
            broadcast("GAMEOVER:A player has disconnected. Game over.");
            stop();
        }
    }
    
    public synchronized void broadcast(String message) {
        logger.info("Broadcasting to " + clients.size() + " clients: " + message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    
    public int getPort() {
        return port;
    }
    
    /**
     * Inner class to handle individual client connections
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private Server server;
        private final String playerColor;
        private BufferedReader reader;
        private PrintWriter writer;
        
        public ClientHandler(Socket socket, Server server, String playerColor) throws IOException {
            this.socket = socket;
            this.server = server;
            this.playerColor = playerColor;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);

            // Send assigned color to the client
            writer.println("COLOR:" + playerColor);
        }
        
        @Override
        public void run() {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.debug("Received from " + playerColor + ": " + line);
                    handleMessage(line);
                }
            } catch (IOException e) {
                if (server.running) {
                    logger.error("Error reading from client " + playerColor + ": " + e.getMessage());
                }
            } finally {
                server.removeClient(this);
            }
        }
        
        private void handleMessage(String message) {
            if (message.startsWith("MOVE:")) {
                // Handle move
                String moveString = message.substring(5);
                logger.info("Server received move from " + playerColor + ": " + moveString);
                try {
                    Move move = parseMove(moveString);
                    logger.info("Server parsed move successfully: " + move.getFrom() + " to " + move.getTo());
                    server.handleMove(this, move);
                } catch (Exception e) {
                    logger.error("Server failed to parse move: " + moveString + " - " + e.getMessage());
                    sendMessage("ERROR: Invalid move format");
                }
            } else if (message.startsWith("CHAT:")) {
                // Handle chat message
                String chatMessage = message.substring(5);
                server.handleChatMessage(this, chatMessage);
            }
        }
        
        private Move parseMove(String moveString) {
            // This method now parses the positions and looks up the piece on the server's board
            if (moveString.length() != 4) {
                throw new IllegalArgumentException("Invalid move format: " + moveString);
            }
            
            Position from = new Position(moveString.substring(0, 2));
            Position to = new Position(moveString.substring(2, 4));
            Piece piece = server.gameBoard.getPiece(from);
            Piece captured = server.gameBoard.getPiece(to);
            if (piece == null) {
                throw new IllegalArgumentException("No piece at " + from);
            }
            return new Move(from, to, piece, captured);
        }
        
        public void sendMessage(String message) {
            writer.println(message);
        }
        
        public String getPlayerColor() {
            return playerColor;
        }

        private void cleanup() {
            // This is now handled in the run() method's finally block
        }
    }
} 