package com.chess;

import com.chess.gui.ChessGUI;
import com.chess.network.Server;
import com.chess.utils.Logger;
import com.chess.utils.NetworkUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Main entry point for the Chess Game application
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("Chess Game starting...");
        
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            logger.info("Look and feel set successfully");
        } catch (Exception e) {
            logger.warn("Could not set system look and feel: " + e.getMessage());
        }
        
        // Parse command line arguments
        if (args.length > 0) {
            logger.info("Starting with argument: " + args[0]);
            switch (args[0].toLowerCase()) {
                case "server":
                    startServer();
                    break;
                case "client":
                    startClient();
                    break;
                case "ai":
                    startAIGame();
                    break;
                case "test":
                    testGUI();
                    break;
                default:
                    showMainMenu();
                    break;
            }
        } else {
            logger.info("Starting main menu...");
            showMainMenu();
        }
    }
    
    private static void showMainMenu() {
        logger.info("Creating main menu...");
        SwingUtilities.invokeLater(() -> {
            try {
                // Create the main frame
                JFrame frame = new JFrame("Chess Game - Main Menu");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 300);
                frame.setLocationRelativeTo(null);
                frame.setResizable(false);
                
                // Create a simple panel with FlowLayout
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                // Add title
                JLabel title = new JLabel("Chess Game");
                title.setAlignmentX(Component.CENTER_ALIGNMENT);
                title.setFont(new Font("Arial", Font.BOLD, 20));
                panel.add(title);
                panel.add(Box.createVerticalStrut(20));
                
                // Create buttons with simple styling
                JButton localGameBtn = new JButton("Hot-Seat Game (2 Players)");
                JButton aiGameBtn = new JButton("Play vs AI");
                JButton hostGameBtn = new JButton("Host Online Game");
                JButton joinGameBtn = new JButton("Join Online Game");
                
                // Set button properties
                localGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                aiGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                hostGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                joinGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                localGameBtn.setMaximumSize(new Dimension(200, 40));
                aiGameBtn.setMaximumSize(new Dimension(200, 40));
                hostGameBtn.setMaximumSize(new Dimension(200, 40));
                joinGameBtn.setMaximumSize(new Dimension(200, 40));
                
                // Add action listeners
                localGameBtn.addActionListener(e -> {
                    logger.info("Local Game button clicked");
                    frame.dispose();
                    startLocalGame();
                });
                
                aiGameBtn.addActionListener(e -> {
                    logger.info("AI Game button clicked");
                    frame.dispose();
                    startAIGame();
                });
                
                hostGameBtn.addActionListener(e -> {
                    logger.info("Host Game button clicked");
                    frame.dispose();
                    startServer();
                });
                
                joinGameBtn.addActionListener(e -> {
                    logger.info("Join Game button clicked");
                    frame.dispose();
                    startClient();
                });
                
                // Add buttons to panel
                panel.add(localGameBtn);
                panel.add(Box.createVerticalStrut(10));
                panel.add(aiGameBtn);
                panel.add(Box.createVerticalStrut(10));
                panel.add(hostGameBtn);
                panel.add(Box.createVerticalStrut(10));
                panel.add(joinGameBtn);
                
                // Add panel to frame
                frame.add(panel);
                
                // Make frame visible
                frame.setVisible(true);
                frame.toFront();
                
                logger.debug("Main menu window is now visible with " + panel.getComponentCount() + " components");
                
                // Print component info for debugging
                for (int i = 0; i < panel.getComponentCount(); i++) {
                    Component comp = panel.getComponent(i);
                    logger.debug("Component " + i + ": " + comp.getClass().getSimpleName() + 
                                     (comp instanceof JButton ? " - " + ((JButton)comp).getText() : ""));
                }
                
            } catch (Exception e) {
                System.err.println("Error creating main menu: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    public static void startLocalGame() {
        logger.info("Starting local game...");
        SwingUtilities.invokeLater(() -> {
            try {
                ChessGUI gui = new ChessGUI();
                gui.startLocalGame();
                gui.setVisible(true);
                gui.toFront();
                gui.requestFocus();
                logger.info("Local game started successfully");
            } catch (Exception e) {
                logger.error("Error starting local game: " + e.getMessage(), e);
            }
        });
    }
    
    public static void startAIGame() {
        logger.info("Starting AI game...");
        SwingUtilities.invokeLater(() -> {
            try {
                ChessGUI gui = new ChessGUI();
                gui.startAIGame();
                gui.setVisible(true);
                gui.toFront();
                gui.requestFocus();
                logger.info("AI game started successfully");
            } catch (Exception e) {
                logger.error("Error starting AI game: " + e.getMessage(), e);
            }
        });
    }
    
    public static void startServer() {
        logger.info("Starting server...");
        SwingUtilities.invokeLater(() -> {
            try {
                // Create and display the GUI for the host immediately
                ChessGUI gui = new ChessGUI();
                gui.setVisible(true);
                gui.setTitle("Chess Game - Waiting for Connection...");
                logger.info("Host GUI is visible");

                // Create the server first to get the actual port
                Server server = new Server(NetworkUtils.PORT);
                int actualPort = server.getPort();
                
                // Start the server in a background thread
                new Thread(() -> {
                    try {
                        server.start(); 
                    } catch (IOException e) {
                        logger.error("Failed to start server: " + e.getMessage());
                        // Show error on the EDT
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(gui, "Failed to start server: " + e.getMessage(), "Server Error", JOptionPane.ERROR_MESSAGE));
                    }
                }).start();

                // Show the join code with the actual port
                String lanIp = NetworkUtils.getLocalIpAddress();
                String joinMessage = "Share one of these Join Codes with your friend:\n\n" +
                                     "For a friend on the same WiFi/LAN:\n" +
                                     "  " + lanIp + ":" + actualPort + "\n\n" +
                                     "If testing on the SAME computer:\n" +
                                     "  " + "127.0.0.1:" + actualPort + "\n";

                JOptionPane.showMessageDialog(gui,
                    new JTextArea(joinMessage), // Use JTextArea for better formatting
                    "Game Hosted",
                    JOptionPane.INFORMATION_MESSAGE);

                // Now, connect the host's client to their own server using the actual port
                gui.startGame(ChessGUI.GameMode.MULTIPLAYER_HOST, "127.0.0.1:" + actualPort);
                gui.setTitle("Chess Game - Host");
                logger.info("Host game connected to server successfully");

            } catch (Exception e) {
                logger.error("An unexpected error occurred during server setup: " + e.getMessage(), e);
            }
        });
    }
    
    public static void startClient() {
        logger.info("Starting client...");
        String joinCode = JOptionPane.showInputDialog(
            null, 
            "Enter Join Code (IP:PORT format, e.g., 192.168.1.100:9999):", 
            "Join Game",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (joinCode != null && !joinCode.trim().isEmpty()) {
            // Parse the join code to extract IP and port
            String[] parts = joinCode.trim().split(":");
            if (parts.length != 2) {
                JOptionPane.showMessageDialog(null, 
                    "Invalid join code format. Please use IP:PORT format (e.g., 192.168.1.100:9999)", 
                    "Invalid Format", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String ipAddress = parts[0];
            int port;
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, 
                    "Invalid port number. Please use a valid number.", 
                    "Invalid Port", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            SwingUtilities.invokeLater(() -> {
                try {
                    ChessGUI gui = new ChessGUI();
                    gui.startGame(ChessGUI.GameMode.MULTIPLAYER_CLIENT, ipAddress + ":" + port);
                    gui.setVisible(true);
                    logger.info("Client game started successfully");
                } catch (Exception e) {
                    logger.error("Error starting client game: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(null, 
                        "Could not connect to server: " + e.getMessage(), 
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
    
    private static void testGUI() {
        logger.info("Testing GUI components...");
        SwingUtilities.invokeLater(() -> {
            JFrame testFrame = new JFrame("GUI Test");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setSize(300, 200);
            testFrame.setLocationRelativeTo(null);
            
            JPanel panel = new JPanel();
            JButton testButton = new JButton("Test Button");
            testButton.addActionListener(e -> {
                logger.info("Test button clicked");
                JOptionPane.showMessageDialog(testFrame, "GUI is working!");
            });
            
            panel.add(testButton);
            testFrame.add(panel);
            testFrame.setVisible(true);
            
            logger.info("Test GUI window created");
        });
    }
} 