package com.chess;

import com.chess.gui.ChessGUI;
import com.chess.network.Server;
import com.chess.utils.Logger;
import com.chess.utils.NetworkUtils;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Main entry point for the Chess Game application
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);
    
    // Available FlatLaf themes
    public enum Theme {
        LIGHT("Light", FlatLightLaf.class),
        DARK("Dark", FlatDarkLaf.class),
        INTELLIJ("IntelliJ", FlatIntelliJLaf.class),
        DARCULA("Darcula", FlatDarculaLaf.class);
        
        private final String displayName;
        private final Class<? extends LookAndFeel> lafClass;
        
        Theme(String displayName, Class<? extends LookAndFeel> lafClass) {
            this.displayName = displayName;
            this.lafClass = lafClass;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public Class<? extends LookAndFeel> getLafClass() {
            return lafClass;
        }
    }
    
    private static Theme currentTheme = Theme.LIGHT; // Default theme
    
    public static void main(String[] args) {
        logger.info("Chess Game starting...");
        
        // Initialize FlatLaf theme before creating any GUI components
        initializeFlatLaf();
        
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
    
    /**
     * Initialize FlatLaf Look and Feel
     */
    private static void initializeFlatLaf() {
        try {
            // Set system properties for better FlatLaf appearance
            System.setProperty("flatlaf.useWindowDecorations", "false");
            System.setProperty("flatlaf.menuBarEmbedded", "false");
            
            // Apply the default theme
            applyTheme(currentTheme);
            
            logger.info("FlatLaf " + currentTheme.getDisplayName() + " theme applied successfully");
        } catch (Exception e) {
            logger.warn("Failed to initialize FlatLaf, falling back to system look and feel: " + e.getMessage());
            // Fallback to system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                logger.warn("Could not set system look and feel: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Apply a specific FlatLaf theme
     */
    public static void applyTheme(Theme theme) {
        try {
            UIManager.setLookAndFeel(theme.getLafClass().getDeclaredConstructor().newInstance());
            currentTheme = theme;
            
            // Update all existing windows
            SwingUtilities.invokeLater(() -> {
                for (Window window : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(window);
                }
            });
            
            logger.info("Applied theme: " + theme.getDisplayName());
        } catch (Exception e) {
            logger.error("Failed to apply theme " + theme.getDisplayName() + ": " + e.getMessage());
            throw new RuntimeException("Failed to apply theme", e);
        }
    }
    
    /**
     * Get the current theme
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    private static void showMainMenu() {
        logger.info("Creating main menu...");
        SwingUtilities.invokeLater(() -> {
            try {
                // Create the main frame
                JFrame frame = new JFrame("Chess Game - Main Menu");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(500, 450);
                frame.setLocationRelativeTo(null);
                frame.setResizable(false);
                
                // Create main panel with better layout
                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
                
                // Header panel with title and theme selector
                JPanel headerPanel = new JPanel(new BorderLayout());
                
                // Add title
                JLabel title = new JLabel("Chess Game", SwingConstants.CENTER);
                title.setFont(new Font("Segoe UI", Font.BOLD, 28));
                headerPanel.add(title, BorderLayout.CENTER);
                
                // Add theme selector
                JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JLabel themeLabel = new JLabel("Theme:");
                JComboBox<Theme> themeComboBox = new JComboBox<>(Theme.values());
                themeComboBox.setSelectedItem(currentTheme);
                themeComboBox.addActionListener(e -> {
                    Theme selectedTheme = (Theme) themeComboBox.getSelectedItem();
                    if (selectedTheme != null && selectedTheme != currentTheme) {
                        applyTheme(selectedTheme);
                    }
                });
                
                themePanel.add(themeLabel);
                themePanel.add(themeComboBox);
                headerPanel.add(themePanel, BorderLayout.EAST);
                
                // Center panel with game mode buttons
                JPanel centerPanel = new JPanel();
                centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
                centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
                
                // Create stylized buttons
                JButton localGameBtn = createStyledButton("🏠 Hot-Seat Game", "Play with a friend on the same computer");
                JButton aiGameBtn = createStyledButton("🤖 Play vs AI", "Challenge the computer opponent");
                JButton hostGameBtn = createStyledButton("🌐 Host Online Game", "Create a game for others to join");
                JButton joinGameBtn = createStyledButton("🔗 Join Online Game", "Join an existing online game");
                
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
                
                // Add buttons to center panel
                centerPanel.add(localGameBtn);
                centerPanel.add(Box.createVerticalStrut(15));
                centerPanel.add(aiGameBtn);
                centerPanel.add(Box.createVerticalStrut(15));
                centerPanel.add(hostGameBtn);
                centerPanel.add(Box.createVerticalStrut(15));
                centerPanel.add(joinGameBtn);
                
                // Footer panel with version info
                JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JLabel versionLabel = new JLabel("Chess Game v1.0.0 - Modern UI with FlatLaf");
                versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                footerPanel.add(versionLabel);
                
                // Add all panels to main panel
                mainPanel.add(headerPanel, BorderLayout.NORTH);
                mainPanel.add(centerPanel, BorderLayout.CENTER);
                mainPanel.add(footerPanel, BorderLayout.SOUTH);
                
                // Add main panel to frame
                frame.add(mainPanel);
                
                // Make frame visible
                frame.setVisible(true);
                frame.toFront();
                
                logger.debug("Enhanced main menu window is now visible");
                
            } catch (Exception e) {
                System.err.println("Error creating main menu: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Create a styled button with modern appearance
     */
    private static JButton createStyledButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 50));
        button.setPreferredSize(new Dimension(300, 50));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return button;
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