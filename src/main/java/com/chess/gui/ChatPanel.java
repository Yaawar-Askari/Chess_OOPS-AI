package com.chess.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * Panel for chat functionality in online games
 */
public class ChatPanel extends JPanel {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private Consumer<String> messageCallback;
    
    public ChatPanel() {
        this(null);
    }
    
    public ChatPanel(Consumer<String> messageCallback) {
        this.messageCallback = messageCallback;
        setPreferredSize(new Dimension(300, 200));
        setMinimumSize(new Dimension(300, 200));
        setBorder(BorderFactory.createTitledBorder("Chat"));
        setLayout(new BorderLayout());
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Chat display area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 12));
        
        scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 12));
        
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 25));
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        add(inputPanel, BorderLayout.SOUTH);
        
        // Add action listeners
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
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            if (messageCallback != null) {
                messageCallback.accept(message);
            }
            messageField.setText("");
        }
    }
    
    public void addMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            // Auto-scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    public void clearChat() {
        chatArea.setText("");
    }
    
    public JTextField getMessageField() {
        return messageField;
    }
} 