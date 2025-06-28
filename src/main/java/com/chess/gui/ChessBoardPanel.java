package com.chess.gui;

import com.chess.model.Board;
import com.chess.model.Move;
import com.chess.model.Piece;
import com.chess.model.Position;
import com.chess.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Panel that displays the chess board and handles piece interactions
 */
public class ChessBoardPanel extends JPanel {
    private static final int SQUARE_SIZE = 60;
    private static final int BOARD_SIZE = 8 * SQUARE_SIZE;
    
    private Board board;
    private ChessGUI parent;
    private Map<Position, Color> highlights;
    private Map<String, BufferedImage> pieceImages;
    
    // Drag and drop variables
    private Position selectedPosition;
    private Point dragOffset;
    private Point currentDragPosition;
    private Point lastDragPosition; // Track last position to avoid unnecessary repaints
    private boolean isDragging = false;
    
    // Animation variables
    private Piece animatingPiece;
    private Point animationStartPos;
    private Point animationEndPos;
    private Point animationCurrentPos;
    private Timer animationTimer;
    private String playerColor = "White"; // Default to "White"
    
    // Performance optimization variables
    private boolean interactionEnabled = true;
    private Rectangle lastRepaintRegion;
    
    // Additional performance optimizations
    private volatile boolean isRepaintInProgress = false;
    private final Object repaintLock = new Object();
    private BufferedImage offscreenBuffer;
    private Graphics2D offscreenGraphics;
    private boolean bufferNeedsUpdate = true;
    
    // Visual enhancement variables
    private boolean showCoordinates = true;
    private boolean smoothAnimations = true;
    private int animationSpeed = 20; // milliseconds per frame
    
    private static final Logger logger = Logger.getLogger(ChessBoardPanel.class);
    
    public ChessBoardPanel(Board board, ChessGUI parent) {
        this.board = board;
        this.parent = parent;
        this.highlights = new HashMap<>();
        this.pieceImages = new HashMap<>();
        
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        setMinimumSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        
        // Enable double buffering for smoother rendering
        setDoubleBuffered(true);
        
        // Initialize offscreen buffer for performance
        initializeOffscreenBuffer();
        
        loadPieceImages();
        setupMouseListeners();
        setupAnimationTimer();
    }
    
    private void loadPieceImages() {
        // Create simple colored pieces for now
        // In a real implementation, you'd load actual piece images
        String[] pieces = {"WhitePawn", "WhiteRook", "WhiteKnight", "WhiteBishop", "WhiteQueen", "WhiteKing",
                          "BlackPawn", "BlackRook", "BlackKnight", "BlackBishop", "BlackQueen", "BlackKing"};
        
        for (String piece : pieces) {
            pieceImages.put(piece, createPieceImage(piece));
        }
    }
      private BufferedImage createPieceImage(String pieceName) {
        BufferedImage image = new BufferedImage(SQUARE_SIZE, SQUARE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set high-quality rendering hints for piece images
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Transparent background
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, SQUARE_SIZE, SQUARE_SIZE);
        g2d.setComposite(AlphaComposite.SrcOver);
        
        // Use Unicode chess symbols for better visual representation
        String symbol = getUnicodeSymbol(pieceName);
        
        // Set font and color
        Font font = new Font("Segoe UI Symbol", Font.BOLD, SQUARE_SIZE - 8);
        g2d.setFont(font);
        
        // Color for pieces
        Color pieceColor = pieceName.startsWith("White") ? Color.WHITE : Color.BLACK;
        Color borderColor = pieceName.startsWith("White") ? Color.BLACK : Color.WHITE;
        
        // Draw border/shadow for better visibility
        FontMetrics fm = g2d.getFontMetrics();
        int x = (SQUARE_SIZE - fm.stringWidth(symbol)) / 2;
        int y = (SQUARE_SIZE - fm.getHeight()) / 2 + fm.getAscent();
        
        // Draw shadow/border with reduced complexity for performance
        g2d.setColor(borderColor);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g2d.drawString(symbol, x + dx, y + dy);
                }
            }
        }
        
        // Draw main piece
        g2d.setColor(pieceColor);
        g2d.drawString(symbol, x, y);
        
        g2d.dispose();
        return image;
    }
      private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!interactionEnabled) return; // Respect interaction enabled flag
                
                int clickedCol = e.getX() / SQUARE_SIZE;
                int clickedRow = e.getY() / SQUARE_SIZE;
                
                if (clickedCol >= 0 && clickedCol < 8 && clickedRow >= 0 && clickedRow < 8) {
                    int logicalRow = "White".equals(playerColor) ? clickedRow : 7 - clickedRow;
                    int logicalCol = "White".equals(playerColor) ? clickedCol : 7 - clickedCol;

                    Position position = new Position(logicalRow, logicalCol);
                    Piece piece = board.getPiece(position);
                    
                    if (piece != null && piece.getColor().equals(board.getCurrentTurn())) {
                        selectedPosition = position;
                        isDragging = true;
                        
                        // Calculate drag offset to center the piece on the cursor
                        int pieceX = clickedCol * SQUARE_SIZE;
                        int pieceY = clickedRow * SQUARE_SIZE;
                        dragOffset = new Point(e.getX() - pieceX, e.getY() - pieceY);
                        currentDragPosition = new Point(pieceX, pieceY);
                        
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        
                        // Highlight valid moves
                        highlightValidMoves(piece);
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!interactionEnabled) return; // Respect interaction enabled flag
                
                if (isDragging && selectedPosition != null) {
                    int clickedCol = e.getX() / SQUARE_SIZE;
                    int clickedRow = e.getY() / SQUARE_SIZE;
                    
                    if (clickedCol >= 0 && clickedCol < 8 && clickedRow >= 0 && clickedRow < 8) {
                        int logicalRow = "White".equals(playerColor) ? clickedRow : 7 - clickedRow;
                        int logicalCol = "White".equals(playerColor) ? clickedCol : 7 - clickedCol;
                        Position targetPosition = new Position(logicalRow, logicalCol);
                        parent.onMoveAttempted(selectedPosition, targetPosition);
                    }
                    
                    // Clear drag state completely
                    clearDragState();
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!interactionEnabled) return; // Respect interaction enabled flag
                
                // Only handle clicks if not dragging
                if (!isDragging) {
                    int clickedCol = e.getX() / SQUARE_SIZE;
                    int clickedRow = e.getY() / SQUARE_SIZE;
                    
                    if (clickedCol >= 0 && clickedCol < 8 && clickedRow >= 0 && clickedRow < 8) {
                        int logicalRow = "White".equals(playerColor) ? clickedRow : 7 - clickedRow;
                        int logicalCol = "White".equals(playerColor) ? clickedCol : 7 - clickedCol;
                        Position position = new Position(logicalRow, logicalCol);
                        parent.onSquareClicked(position);
                    }
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!interactionEnabled) return; // Respect interaction enabled flag
                
                if (isDragging && dragOffset != null) {
                    // Calculate new drag position
                    Point newDragPosition = new Point(e.getX() - dragOffset.x, e.getY() - dragOffset.y);
                    
                    // Only repaint if position has changed significantly (reduces flicker)
                    if (lastDragPosition == null || 
                        Math.abs(newDragPosition.x - lastDragPosition.x) > 2 || 
                        Math.abs(newDragPosition.y - lastDragPosition.y) > 2) {
                        
                        lastDragPosition = currentDragPosition != null ? new Point(currentDragPosition) : null;
                        currentDragPosition = newDragPosition;
                        
                        // Use optimized repaint that only updates necessary regions
                        repaintDragRegions();
                    }
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!interactionEnabled) return; // Respect interaction enabled flag
                
                // Provide visual feedback when hovering over pieces
                if (!isDragging) {
                    int clickedCol = e.getX() / SQUARE_SIZE;
                    int clickedRow = e.getY() / SQUARE_SIZE;
                    
                    if (clickedCol >= 0 && clickedCol < 8 && clickedRow >= 0 && clickedRow < 8) {
                        int logicalRow = "White".equals(playerColor) ? clickedRow : 7 - clickedRow;
                        int logicalCol = "White".equals(playerColor) ? clickedCol : 7 - clickedCol;
                        Position position = new Position(logicalRow, logicalCol);
                        Piece piece = board.getPiece(position);
                        
                        if (piece != null && piece.getColor().equals(board.getCurrentTurn())) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        } else {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    }
                }
            }
        });
    }
      public void highlightValidMoves(Piece piece) {
        clearHighlights();
        java.util.List<Position> validMoves = piece.getValidMoves(board);
        
        for (Position move : validMoves) {
            // Different colors for capture vs regular moves
            Piece targetPiece = board.getPiece(move);
            if (targetPiece != null && !targetPiece.getColor().equals(piece.getColor())) {
                // Red highlight for capture moves
                highlights.put(move, new Color(255, 100, 100, 150));
            } else {
                // Blue highlight for regular moves
                highlights.put(move, new Color(100, 150, 255, 120));
            }
        }
        
        // Highlight selected piece with green
        highlights.put(piece.getPosition(), new Color(100, 255, 100, 150));
        repaint();
    }
    
    private void setupAnimationTimer() {
        animationTimer = new Timer(animationSpeed, new ActionListener() { // Use configurable animation speed
            private int step = 0;
            private final int totalSteps = smoothAnimations ? 20 : 10; // Fewer steps if smooth animations disabled

            @Override
            public void actionPerformed(ActionEvent e) {
                step++;
                if (animationStartPos != null && animationEndPos != null && animationCurrentPos != null) {
                    int dx = animationEndPos.x - animationStartPos.x;
                    int dy = animationEndPos.y - animationStartPos.y;
                    
                    if (smoothAnimations) {
                        // Smooth easing animation
                        double progress = (double) step / totalSteps;
                        progress = 1 - Math.pow(1 - progress, 3); // Ease-out cubic
                        animationCurrentPos.x = animationStartPos.x + (int)(dx * progress);
                        animationCurrentPos.y = animationStartPos.y + (int)(dy * progress);
                    } else {
                        // Linear animation
                        animationCurrentPos.x = animationStartPos.x + (dx * step / totalSteps);
                        animationCurrentPos.y = animationStartPos.y + (dy * step / totalSteps);
                    }
                    
                    // Only repaint the animation region to reduce flicker
                    repaintAnimationRegion();
                }

                if (step >= totalSteps) {
                    animationTimer.stop();
                    animatingPiece = null;
                    step = 0;
                    bufferNeedsUpdate = true; // Mark buffer for update
                    if (parent != null) {
                        parent.onAnimationFinished();
                    }
                }
            }
        });
    }

    public void animateMove(Move move) {
        // Prevent multiple animations running simultaneously
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        this.animatingPiece = board.getPiece(move.getFrom());
        if (this.animatingPiece == null) return;

        int startRow = move.getFrom().getRow();
        int startCol = move.getFrom().getCol();
        int endRow = move.getTo().getRow();
        int endCol = move.getTo().getCol();

        int displayStartRow = "White".equals(playerColor) ? startRow : 7 - startRow;
        int displayStartCol = "White".equals(playerColor) ? startCol : 7 - startCol;
        int displayEndRow = "White".equals(playerColor) ? endRow : 7 - endRow;
        int displayEndCol = "White".equals(playerColor) ? endCol : 7 - endCol;

        animationStartPos = new Point(displayStartCol * SQUARE_SIZE, displayStartRow * SQUARE_SIZE);
        animationEndPos = new Point(displayEndCol * SQUARE_SIZE, displayEndRow * SQUARE_SIZE);

        animationCurrentPos = new Point(animationStartPos.x, animationStartPos.y);
        
        // Clear drag state to prevent conflicts
        clearDragState();
        
        animationTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Safety check for initialization
        if (repaintLock != null) {
            synchronized (repaintLock) {
                isRepaintInProgress = true;
                try {
                    renderWithOptimization(g);
                } finally {
                    isRepaintInProgress = false;
                }
            }
        } else {
            // Fallback during construction
            renderWithOptimization(g);
        }
    }
    
    private void renderWithOptimization(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Initialize buffer if needed
        if (offscreenBuffer == null) {
            initializeOffscreenBuffer();
        }
        
        // Use offscreen buffer for stable rendering
        if (bufferNeedsUpdate) {
            renderToOffscreenBuffer();
            bufferNeedsUpdate = false;
        }
        
        // Draw the offscreen buffer to screen
        if (offscreenBuffer != null) {
            g2d.drawImage(offscreenBuffer, 0, 0, this);
        }
        
        // Draw dynamic elements directly (drag, animation)
        drawDynamicElements(g2d);
    }
    
    private void renderToOffscreenBuffer() {
        if (offscreenGraphics == null) return;
        
        // Clear the buffer
        offscreenGraphics.setColor(getBackground());
        offscreenGraphics.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);
        
        // Set high-quality rendering hints
        GuiUtils.setOptimalRenderingHints(offscreenGraphics);

        // Draw squares and pieces to offscreen buffer
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int displayRow = "White".equals(playerColor) ? row : 7 - row;
                int displayCol = "White".equals(playerColor) ? col : 7 - col;

                int x = displayCol * SQUARE_SIZE;
                int y = displayRow * SQUARE_SIZE;
                
                // Determine square color
                Color squareColor = ((row + col) % 2 == 0) ? new Color(240, 217, 181) : new Color(181, 136, 99);

                // Apply highlights
                Position currentPos = new Position(row, col);
                Color finalColor = squareColor;
                
                // Check if king is in check and highlight it
                Piece piece = board.getPiece(currentPos);
                if (piece != null && piece.getType().equals("King") && board.isInCheck(piece.getColor())) {
                    finalColor = new Color(255, 100, 100); // Red background for king in check
                } else if (highlights.containsKey(currentPos)) {
                    finalColor = highlights.get(currentPos);
                }
                
                offscreenGraphics.setColor(finalColor);
                offscreenGraphics.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);

                // Draw piece (but not if it's being dragged or animated)
                if (piece != null && !(isDragging && currentPos.equals(selectedPosition)) && piece != animatingPiece) {
                    drawPieceToGraphics(offscreenGraphics, piece, x, y);
                }
            }
        }

        // Draw coordinates if enabled
        if (showCoordinates) {
            offscreenGraphics.setColor(Color.BLACK);
            offscreenGraphics.setFont(new Font("Arial", Font.BOLD, 12));
            for (int i = 0; i < 8; i++) {
                // File labels (a-h)
                String file = String.valueOf((char)('a' + ("White".equals(playerColor) ? i : 7 - i)));
                offscreenGraphics.drawString(file, i * SQUARE_SIZE + SQUARE_SIZE / 2 - 4, BOARD_SIZE - 5);
                
                // Rank labels (1-8)
                String rank = String.valueOf("White".equals(playerColor) ? 8 - i : i + 1);
                offscreenGraphics.drawString(rank, 5, i * SQUARE_SIZE + SQUARE_SIZE / 2 + 4);
            }
        }
    }
    
    private void drawDynamicElements(Graphics2D g2d) {
        // Set optimal rendering hints for dynamic elements
        GuiUtils.setOptimalRenderingHints(g2d);
        
        // Draw dragged piece
        if (isDragging && selectedPosition != null && currentDragPosition != null) {
            Piece pieceToDrag = board.getPiece(selectedPosition);
            if (pieceToDrag != null) {
                drawPieceToGraphics(g2d, pieceToDrag, currentDragPosition.x, currentDragPosition.y);
            }
        }

        // Draw animating piece
        if (animationTimer.isRunning() && animatingPiece != null && animationCurrentPos != null) {
            drawPieceToGraphics(g2d, animatingPiece, animationCurrentPos.x, animationCurrentPos.y);
        }
    }
    
    private void drawPieceToGraphics(Graphics2D g, Piece piece, int x, int y) {
        String pieceKey = piece.getColor() + piece.getType();
        BufferedImage image = pieceImages.get(pieceKey);
        if (image != null) {
            g.drawImage(image, x, y, this);
        }
    }
    
    public void highlightSquare(Position position, Color color) {
        highlights.put(position, color);
        repaint();
    }
    
    public void clearHighlights() {
        highlights.clear();
        repaint();
    }
    
    public void highlightHint(Move move) {
        clearHighlights();
        highlightSquare(move.getFrom(), new Color(0, 150, 255, 125)); // Blue for "from"
        highlightSquare(move.getTo(), new Color(0, 200, 255, 125)); // Lighter blue for "to"
    }

    public void updateBoard() {
        repaint();
    }
    
    public void setBoard(Board board) {
        if (board != null) {
            logger.info("ChessBoardPanel.setBoard called with board: " + board.toFEN());
            this.board = board;
            
            // Clear any ongoing operations when board changes
            clearDragState();
            
            // Stop any running animations to prevent conflicts
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
                animatingPiece = null;
            }
            
            // Mark buffer for update and repaint
            bufferNeedsUpdate = true;
            repaint();
        }
    }
    
    public void setInteractionEnabled(boolean enabled) {
        this.interactionEnabled = enabled;
        setEnabled(enabled);
        if (!enabled) {
            clearHighlights();
            clearDragState();
            // Mark buffer for update and repaint to clear any visual artifacts
            bufferNeedsUpdate = true;
            repaint();
        }
    }
    
    public Board getBoard() {
        return board;
    }

    public void setPlayerColor(String color) {
        this.playerColor = color;
        repaint(); // Repaint the board with the new orientation
    }
    
    /**
     * Get Unicode chess symbol for a piece
     */
    private String getUnicodeSymbol(String pieceName) {
        switch (pieceName) {
            case "WhiteKing": return "\u2654";
            case "WhiteQueen": return "\u2655";
            case "WhiteRook": return "\u2656";
            case "WhiteBishop": return "\u2657";
            case "WhiteKnight": return "\u2658";
            case "WhitePawn": return "\u2659";
            case "BlackKing": return "\u265A";
            case "BlackQueen": return "\u265B";
            case "BlackRook": return "\u265C";
            case "BlackBishop": return "\u265D";
            case "BlackKnight": return "\u265E";
            case "BlackPawn": return "\u265F";
            default: return "?";
        }
    }
    
    /**
     * Optimized repaint for drag operations - only repaints necessary regions
     */
    private void repaintDragRegions() {
        // Repaint the previous position to clear the old piece
        if (lastDragPosition != null) {
            int margin = 10; // Small margin to ensure clean clearing
            repaint(lastDragPosition.x - margin, lastDragPosition.y - margin, 
                   SQUARE_SIZE + 2 * margin, SQUARE_SIZE + 2 * margin);
        }
        
        // Repaint the current position to draw the piece
        if (currentDragPosition != null) {
            int margin = 10;
            repaint(currentDragPosition.x - margin, currentDragPosition.y - margin, 
                   SQUARE_SIZE + 2 * margin, SQUARE_SIZE + 2 * margin);
        }
    }
    
    /**
     * Optimized repaint for animations - only repaints the animation path
     */
    private void repaintAnimationRegion() {
        if (animationCurrentPos != null) {
            int margin = 15; // Margin to ensure clean animation
            repaint(animationCurrentPos.x - margin, animationCurrentPos.y - margin, 
                   SQUARE_SIZE + 2 * margin, SQUARE_SIZE + 2 * margin);
        }
    }
    
    /**
     * Cleanly clears all drag-related state and triggers minimal repaint
     */
    private void clearDragState() {
        if (isDragging || currentDragPosition != null) {
            // Repaint the area where the dragged piece was
            if (currentDragPosition != null) {
                int margin = 10;
                repaint(currentDragPosition.x - margin, currentDragPosition.y - margin, 
                       SQUARE_SIZE + 2 * margin, SQUARE_SIZE + 2 * margin);
            }
            
            isDragging = false;
            selectedPosition = null;
            dragOffset = null;
            currentDragPosition = null;
            lastDragPosition = null;
            setCursor(Cursor.getDefaultCursor());
            clearHighlights();
        }
    }
    
    /**
     * Initialize offscreen buffer for improved performance
     */
    private void initializeOffscreenBuffer() {
        if (offscreenBuffer == null || 
            offscreenBuffer.getWidth() != BOARD_SIZE || 
            offscreenBuffer.getHeight() != BOARD_SIZE) {
            
            if (offscreenGraphics != null) {
                offscreenGraphics.dispose();
            }
            
            offscreenBuffer = new BufferedImage(BOARD_SIZE, BOARD_SIZE, BufferedImage.TYPE_INT_RGB);
            offscreenGraphics = offscreenBuffer.createGraphics();
            GuiUtils.setOptimalRenderingHints(offscreenGraphics);
            bufferNeedsUpdate = true;
        }
    }
    
    /**
     * Enhanced repaint method with thread safety
     */
    @Override
    public void repaint() {
        // Safety check for initialization
        if (repaintLock != null) {
            synchronized (repaintLock) {
                if (!isRepaintInProgress) {
                    bufferNeedsUpdate = true;
                    super.repaint();
                }
            }
        } else {
            // Fallback during construction
            super.repaint();
        }
    }
    
    /**
     * Enhanced partial repaint for specific regions
     */
    @Override
    public void repaint(int x, int y, int width, int height) {
        // Safety check for initialization
        if (repaintLock != null) {
            synchronized (repaintLock) {
                if (!isRepaintInProgress) {
                    bufferNeedsUpdate = true;
                    super.repaint(x, y, width, height);
                }
            }
        } else {
            // Fallback during construction
            super.repaint(x, y, width, height);
        }
    }
    
    /**
     * Get animation speed setting
     */
    public int getAnimationSpeed() {
        return animationSpeed;
    }
    
    /**
     * Set animation speed (milliseconds per frame)
     */
    public void setAnimationSpeed(int speed) {
        this.animationSpeed = Math.max(10, Math.min(100, speed)); // Clamp between 10-100ms
    }
    
    /**
     * Toggle coordinate display
     */
    public void setShowCoordinates(boolean show) {
        if (this.showCoordinates != show) {
            this.showCoordinates = show;
            repaint();
        }
    }
    
    /**
     * Toggle smooth animations
     */
    public void setSmoothAnimations(boolean smooth) {
        this.smoothAnimations = smooth;
    }
}