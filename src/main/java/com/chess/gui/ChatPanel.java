package com.chess.gui;

import com.chess.utils.Logger;
import com.chess.utils.EmojiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * Panel for chat functionality in online games
 * Supports Unicode emojis and real-time messaging
 */
public class ChatPanel extends JPanel {
    private static final Logger logger = Logger.getLogger(ChatPanel.class);
    
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private Consumer<String> messageCallback;
    private boolean connectionActive = false;
    
    public ChatPanel() {
        this(null);
    }
    
    public ChatPanel(Consumer<String> messageCallback) {
        this.messageCallback = messageCallback;
        this.connectionActive = false; // Start disabled
        setPreferredSize(new Dimension(300, 250));
        setMinimumSize(new Dimension(300, 250));
        setBorder(BorderFactory.createTitledBorder("Chat"));
        setLayout(new BorderLayout());
        
        initializeComponents();
        logger.info("ChatPanel initialized with emoji support");
    }
    
    private void initializeComponents() {
        // Chat display area with emoji support
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Enable Unicode emoji support
        String welcomeMessage = EmojiUtils.enhanceWithEmojis("Welcome to the chat! Use emojis like :) <chess> :D\n");
        chatArea.setText(welcomeMessage);
        
        scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        add(scrollPane, BorderLayout.CENTER);
        
        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageField.setToolTipText("Type your message here (emojis supported: :) <chess> :D +1)");
        messageField.setPreferredSize(new Dimension(0, 30));
        
        sendButton = new JButton("📨 Send");
        sendButton.setPreferredSize(new Dimension(90, 30));
        sendButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sendButton.setFocusPainted(false);
        
        // Start disabled
        sendButton.setEnabled(false);
        messageField.setEnabled(false);
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        add(inputPanel, BorderLayout.SOUTH);
        
        // Add action listeners with enhanced error handling
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        // Add key listener for Enter key
        messageField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
    }
    
    private void sendMessage() {
        if (!connectionActive) {
            showConnectionError();
            return;
        }
        
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            logger.info("Sending chat message: " + message);
            
            // Send in background thread to avoid UI blocking
            SwingWorker<Void, Void> sendWorker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (messageCallback != null) {
                        messageCallback.accept(message);
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        get(); // Check for exceptions
                        messageField.setText("");
                        messageField.requestFocus();
                        logger.debug("Chat message sent successfully");
                    } catch (Exception e) {
                        logger.error("Failed to send chat message: " + e.getMessage());
                        SwingUtilities.invokeLater(() -> {
                            showConnectionError();
                            // Restore the message so user can try again
                            messageField.setText(message);
                        });
                    }
                }
            };
            
            sendWorker.execute();
        }
    }
    
    public void addMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            // Add timestamp to messages for better chat experience
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String enhancedMessage = EmojiUtils.enhanceWithEmojis(message);
            String formattedMessage = "[" + timestamp + "] " + enhancedMessage;
            
            chatArea.append(formattedMessage + "\n");
            // Auto-scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            
            logger.debug("Chat message displayed: " + message);
        });
    }
    
    public void clearChat() {
        SwingUtilities.invokeLater(() -> {
            String welcomeMessage = EmojiUtils.enhanceWithEmojis("Welcome to the chat! Use emojis like :) <chess> :D\n");
            chatArea.setText(welcomeMessage);
            logger.info("Chat cleared");
        });
    }
    
    public JTextField getMessageField() {
        return messageField;
    }
    
    /**
     * Set connection status for error handling
     */
    public void setConnectionActive(boolean active) {
        this.connectionActive = active;
        SwingUtilities.invokeLater(() -> {
            sendButton.setEnabled(active);
            messageField.setEnabled(active);
            
            if (!active) {
                addMessage(EmojiUtils.enhanceWithEmojis("[X] Connection lost. Unable to send/receive chat messages."));
                sendButton.setText("Disconnected");
                sendButton.setBackground(Color.GRAY);
            } else {
                sendButton.setText(EmojiUtils.enhanceWithEmojis("Send >>"));
                sendButton.setBackground(new Color(70, 130, 180));
            }
        });
        
        logger.info("Chat connection status changed to: " + (active ? "active" : "inactive"));
    }
    
    /**
     * Show connection error message
     */
    private void showConnectionError() {
        SwingUtilities.invokeLater(() -> {
            addMessage(EmojiUtils.enhanceWithEmojis("[!] Connection error. Please check your network connection."));
            setConnectionActive(false);
        });
    }
    
    /**
     * Add a system message (different formatting)
     */
    public void addSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String enhancedMessage = EmojiUtils.enhanceWithEmojis("[*] " + message);
            chatArea.append(enhancedMessage + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            logger.info("System message: " + message);
        });
    }
} 