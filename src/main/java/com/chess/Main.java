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
        System.out.println("Chess Game starting...");
        
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("Look and feel set successfully");
        } catch (Exception e) {
            logger.warn("Could not set system look and feel: " + e.getMessage());
            System.out.println("Warning: Could not set look and feel: " + e.getMessage());
        }
        
        // Parse command line arguments
        if (args.length > 0) {
            System.out.println("Starting with argument: " + args[0]);
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
            System.out.println("Starting main menu...");
            showMainMenu();
        }
    }
    
    private static void showMainMenu() {
        System.out.println("Creating main menu...");
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
                    System.out.println("Local Game button clicked!");
                    frame.dispose();
                    startLocalGame();
                });
                
                aiGameBtn.addActionListener(e -> {
                    System.out.println("AI Game button clicked!");
                    frame.dispose();
                    startAIGame();
                });
                
                hostGameBtn.addActionListener(e -> {
                    System.out.println("Host Game button clicked!");
                    frame.dispose();
                    startServer();
                });
                
                joinGameBtn.addActionListener(e -> {
                    System.out.println("Join Game button clicked!");
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
                
                System.out.println("Main menu window is now visible with " + panel.getComponentCount() + " components");
                
                // Print component info for debugging
                for (int i = 0; i < panel.getComponentCount(); i++) {
                    Component comp = panel.getComponent(i);
                    System.out.println("Component " + i + ": " + comp.getClass().getSimpleName() + 
                                     (comp instanceof JButton ? " - " + ((JButton)comp).getText() : ""));
                }
                
            } catch (Exception e) {
                System.err.println("Error creating main menu: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    public static void startLocalGame() {
        System.out.println("Starting local game...");
        SwingUtilities.invokeLater(() -> {
            try {
                ChessGUI gui = new ChessGUI();
                gui.startLocalGame();
                gui.setVisible(true);
                gui.toFront();
                gui.requestFocus();
                System.out.println("Local game started successfully");
            } catch (Exception e) {
                System.err.println("Error starting local game: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    public static void startAIGame() {
        System.out.println("Starting AI game...");
        SwingUtilities.invokeLater(() -> {
            try {
                ChessGUI gui = new ChessGUI();
                gui.startAIGame();
                gui.setVisible(true);
                gui.toFront();
                gui.requestFocus();
                System.out.println("AI game started successfully");
            } catch (Exception e) {
                System.err.println("Error starting AI game: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    public static void startServer() {
        System.out.println("Starting server...");
        SwingUtilities.invokeLater(() -> {
            try {
                // Create and display the GUI for the host immediately
                ChessGUI gui = new ChessGUI();
                gui.setVisible(true);
                gui.setTitle("Chess Game - Waiting for Connection...");
                System.out.println("Host GUI is visible.");

                // Start the server in a background thread
                new Thread(() -> {
                    try {
                        Server server = new Server(8080);
                        // This will now run in the background, accepting connections
                        server.start(); 
                    } catch (IOException e) {
                        logger.error("Failed to start server: " + e.getMessage());
                        // Show error on the EDT
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(gui, "Failed to start server: " + e.getMessage(), "Server Error", JOptionPane.ERROR_MESSAGE));
                    }
                }).start();

                // Since the server is starting in the background, we can immediately show the join code
                String joinCode = NetworkUtils.getLocalIpAddress();
                JOptionPane.showMessageDialog(gui,
                    "Share this Join Code with your friend:\n\n" + joinCode,
                    "Game Hosted",
                    JOptionPane.INFORMATION_MESSAGE);

                // Now, connect the host's client to their own server
                gui.startHostGame("localhost", 8080);
                gui.setTitle("Chess Game - Host");
                System.out.println("Host game connected to server successfully.");

            } catch (Exception e) {
                logger.error("An unexpected error occurred during server setup: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    public static void startClient() {
        System.out.println("Starting client...");
        String joinCode = JOptionPane.showInputDialog(
            null, 
            "Enter Join Code (your friend's IP address):", 
            "Join Game",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (joinCode != null && !joinCode.trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                try {
                    ChessGUI gui = new ChessGUI();
                    gui.startClientGame(joinCode.trim(), 8080);
                    gui.setVisible(true);
                    System.out.println("Client game started successfully");
                } catch (Exception e) {
                    System.err.println("Error starting client game: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, 
                        "Could not connect to server: " + e.getMessage(), 
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
    
    private static void testGUI() {
        System.out.println("Testing GUI components...");
        SwingUtilities.invokeLater(() -> {
            JFrame testFrame = new JFrame("GUI Test");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setSize(300, 200);
            testFrame.setLocationRelativeTo(null);
            
            JPanel panel = new JPanel();
            JButton testButton = new JButton("Test Button");
            testButton.addActionListener(e -> {
                System.out.println("Test button clicked!");
                JOptionPane.showMessageDialog(testFrame, "GUI is working!");
            });
            
            panel.add(testButton);
            testFrame.add(panel);
            testFrame.setVisible(true);
            
            System.out.println("Test GUI window created");
        });
    }
} 