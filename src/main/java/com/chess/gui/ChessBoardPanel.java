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
    private boolean isDragging = false;
    
    // Animation variables
    private Piece animatingPiece;
    private Point animationStartPos;
    private Point animationEndPos;
    private Point animationCurrentPos;
    private Timer animationTimer;
    private String playerColor = "White"; // Default to "White"
    
    private static final Logger logger = Logger.getLogger(ChessBoardPanel.class);
    
    public ChessBoardPanel(Board board, ChessGUI parent) {
        this.board = board;
        this.parent = parent;
        this.highlights = new HashMap<>();
        this.pieceImages = new HashMap<>();
        
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        setMinimumSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        
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
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Background
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, SQUARE_SIZE, SQUARE_SIZE);
        
        // Piece color
        Color pieceColor = pieceName.startsWith("White") ? Color.WHITE : Color.BLACK;
        Color borderColor = pieceName.startsWith("White") ? Color.BLACK : Color.WHITE;
        
        // Draw piece based on type
        String pieceType = pieceName.substring(pieceName.startsWith("White") ? 5 : 5);
        
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(2));
        
        int centerX = SQUARE_SIZE / 2;
        int centerY = SQUARE_SIZE / 2;
        int radius = SQUARE_SIZE / 3;
        
        switch (pieceType) {
            case "Pawn":
                drawPawn(g2d, centerX, centerY, radius, pieceColor, borderColor);
                break;
            case "Rook":
                drawRook(g2d, centerX, centerY, radius, pieceColor, borderColor);
                break;
            case "Knight":
                drawKnight(g2d, centerX, centerY, radius, pieceColor, borderColor);
                break;
            case "Bishop":
                drawBishop(g2d, centerX, centerY, radius, pieceColor, borderColor);
                break;
            case "Queen":
                drawQueen(g2d, centerX, centerY, radius, pieceColor, borderColor);
                break;
            case "King":
                drawKing(g2d, centerX, centerY, radius, pieceColor, borderColor);
                break;
        }
        
        g2d.dispose();
        return image;
    }
    
    private void drawPawn(Graphics2D g2d, int x, int y, int radius, Color fill, Color border) {
        g2d.setColor(fill);
        g2d.fillOval(x - radius/2, y - radius/2, radius, radius);
        g2d.setColor(border);
        g2d.drawOval(x - radius/2, y - radius/2, radius, radius);
    }
    
    private void drawRook(Graphics2D g2d, int x, int y, int radius, Color fill, Color border) {
        int[] xPoints = {x - radius/2, x + radius/2, x + radius/3, x + radius/4, x - radius/4, x - radius/3};
        int[] yPoints = {y - radius/2, y - radius/2, y - radius/4, y + radius/3, y + radius/3, y - radius/4};
        g2d.setColor(fill);
        g2d.fillPolygon(xPoints, yPoints, 6);
        g2d.setColor(border);
        g2d.drawPolygon(xPoints, yPoints, 6);
    }
    
    private void drawKnight(Graphics2D g2d, int x, int y, int radius, Color fill, Color border) {
        g2d.setColor(fill);
        g2d.fillOval(x - radius/2, y - radius/2, radius, radius);
        g2d.setColor(border);
        g2d.drawOval(x - radius/2, y - radius/2, radius, radius);
        // Add horse head shape
        g2d.setColor(border);
        g2d.drawLine(x + radius/4, y - radius/3, x + radius/2, y - radius/2);
    }
    
    private void drawBishop(Graphics2D g2d, int x, int y, int radius, Color fill, Color border) {
        g2d.setColor(fill);
        g2d.fillOval(x - radius/2, y - radius/2, radius, radius);
        g2d.setColor(border);
        g2d.drawOval(x - radius/2, y - radius/2, radius, radius);
        // Add cross
        g2d.drawLine(x - radius/4, y - radius/4, x + radius/4, y + radius/4);
        g2d.drawLine(x - radius/4, y + radius/4, x + radius/4, y - radius/4);
    }
    
    private void drawQueen(Graphics2D g2d, int x, int y, int radius, Color fill, Color border) {
        g2d.setColor(fill);
        g2d.fillOval(x - radius/2, y - radius/2, radius, radius);
        g2d.setColor(border);
        g2d.drawOval(x - radius/2, y - radius/2, radius, radius);
        // Add crown
        g2d.drawLine(x - radius/3, y - radius/3, x + radius/3, y - radius/3);
        g2d.drawLine(x - radius/4, y - radius/2, x - radius/4, y - radius/4);
        g2d.drawLine(x, y - radius/2, x, y - radius/4);
        g2d.drawLine(x + radius/4, y - radius/2, x + radius/4, y - radius/4);
    }
    
    private void drawKing(Graphics2D g2d, int x, int y, int radius, Color fill, Color border) {
        g2d.setColor(fill);
        g2d.fillOval(x - radius/2, y - radius/2, radius, radius);
        g2d.setColor(border);
        g2d.drawOval(x - radius/2, y - radius/2, radius, radius);
        // Add cross
        g2d.drawLine(x, y - radius/2, x, y + radius/2);
        g2d.drawLine(x - radius/3, y, x + radius/3, y);
    }
    
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int clickedCol = e.getX() / SQUARE_SIZE;
                int clickedRow = e.getY() / SQUARE_SIZE;
                
                if (clickedCol >= 0 && clickedCol < 8 && clickedRow >= 0 && clickedRow < 8) {
                    int logicalRow = "White".equals(playerColor) ? clickedRow : 7 - clickedRow;
                    int logicalCol = "White".equals(playerColor) ? clickedCol : 7 - clickedCol;

                    Position position = new Position(logicalRow, logicalCol);
                    Piece piece = board.getPiece(position);
                    
                    if (piece != null && piece.getColor().equals(board.getCurrentTurn())) {
                        selectedPosition = position;
                        // The drag offset needs to be calculated based on the potentially flipped view
                        int displayX = clickedCol * SQUARE_SIZE;
                        int displayY = clickedRow * SQUARE_SIZE;
                        dragOffset = new Point(e.getX() - displayX, e.getY() - displayY);
                        isDragging = true;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        
                        // Highlight valid moves
                        highlightValidMoves(piece);
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging && selectedPosition != null) {
                    int clickedCol = e.getX() / SQUARE_SIZE;
                    int clickedRow = e.getY() / SQUARE_SIZE;
                    
                    if (clickedCol >= 0 && clickedCol < 8 && clickedRow >= 0 && clickedRow < 8) {
                        int logicalRow = "White".equals(playerColor) ? clickedRow : 7 - clickedRow;
                        int logicalCol = "White".equals(playerColor) ? clickedCol : 7 - clickedCol;
                        Position targetPosition = new Position(logicalRow, logicalCol);
                        parent.onMoveAttempted(selectedPosition, targetPosition);
                    }
                    
                    // Clear drag state
                    isDragging = false;
                    selectedPosition = null;
                    dragOffset = null;
                    setCursor(Cursor.getDefaultCursor());
                    clearHighlights();
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
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
                if (isDragging) {
                    // Update cursor position for visual feedback
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }
            }
        });
    }
    
    public void highlightValidMoves(Piece piece) {
        clearHighlights();
        java.util.List<Position> validMoves = piece.getValidMoves(board);
        
        for (Position move : validMoves) {
            highlights.put(move, new Color(255, 255, 0, 100)); // Yellow highlight
        }
        
        // Highlight selected piece
        highlights.put(piece.getPosition(), new Color(0, 255, 0, 100)); // Green highlight
        repaint();
    }
    
    private void setupAnimationTimer() {
        animationTimer = new Timer(15, new ActionListener() {
            private int step = 0;
            private final int totalSteps = 20;

            @Override
            public void actionPerformed(ActionEvent e) {
                step++;
                int dx = animationEndPos.x - animationStartPos.x;
                int dy = animationEndPos.y - animationStartPos.y;
                animationCurrentPos.x = animationStartPos.x + (dx * step / totalSteps);
                animationCurrentPos.y = animationStartPos.y + (dy * step / totalSteps);
                
                repaint();

                if (step >= totalSteps) {
                    animationTimer.stop();
                    animatingPiece = null;
                    step = 0;
                    parent.onAnimationFinished();
                }
            }
        });
    }

    public void animateMove(Move move) {
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
        animationTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw squares and pieces
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
                if (highlights.containsKey(currentPos)) {
                    g2d.setColor(highlights.get(currentPos));
                } else {
                    g2d.setColor(squareColor);
                }
                g2d.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);

                // Draw piece
                Piece piece = board.getPiece(currentPos);
                if (piece != null && !(isDragging && currentPos.equals(selectedPosition)) && piece != animatingPiece) {
                     drawPiece(g2d, piece, x, y);
                }
            }
        }

        // Draw coordinates
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        for (int i = 0; i < 8; i++) {
            // File labels (a-h)
            String file = String.valueOf((char)('a' + ("White".equals(playerColor) ? i : 7 - i)));
            g2d.drawString(file, i * SQUARE_SIZE + SQUARE_SIZE / 2 - 4, getHeight() - 5);
            
            // Rank labels (1-8)
            String rank = String.valueOf("White".equals(playerColor) ? 8 - i : i + 1);
            g2d.drawString(rank, 5, i * SQUARE_SIZE + SQUARE_SIZE / 2 + 4);
        }
        
        // Draw dragged piece
        if (isDragging && selectedPosition != null) {
            Piece pieceToDrag = board.getPiece(selectedPosition);
            Point mousePos = getMousePosition();
            if (pieceToDrag != null && mousePos != null) {
                int x = mousePos.x - dragOffset.x;
                int y = mousePos.y - dragOffset.y;
                drawPiece(g2d, pieceToDrag, x, y);
            }
        }

        // Draw animating piece
        if (animationTimer.isRunning() && animatingPiece != null) {
            drawPiece(g2d, animatingPiece, animationCurrentPos.x, animationCurrentPos.y);
        }
    }
    
    private void drawPiece(Graphics2D g, Piece piece, int x, int y) {
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
        logger.info("ChessBoardPanel.setBoard called with board: " + board.toFEN());
        this.board = board;
        repaint();
    }
    
    public void setInteractionEnabled(boolean enabled) {
        setEnabled(enabled);
        if (!enabled) {
            clearHighlights();
            selectedPosition = null;
            isDragging = false;
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    public Board getBoard() {
        return board;
    }

    public void setPlayerColor(String color) {
        this.playerColor = color;
        repaint(); // Repaint the board with the new orientation
    }
} 