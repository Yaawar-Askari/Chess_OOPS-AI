package com.chess.gui;

import com.chess.utils.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for GUI stability improvements and common operations
 */
public class GuiUtils {
    private static final Logger logger = Logger.getLogger(GuiUtils.class);
    
    /**
     * Safely executes a GUI operation on the EDT
     */
    public static void runOnEDT(Runnable operation) {
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                operation.run();
            } catch (Exception e) {
                logger.error("Error in EDT operation: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    operation.run();
                } catch (Exception e) {
                    logger.error("Error in EDT operation: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }
    
    /**
     * Creates optimized rendering hints for smooth graphics
     */
    public static void setOptimalRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
    
    /**
     * Safely repaints a component with error handling
     */
    public static void safeRepaint(Component component) {
        if (component != null && component.isDisplayable()) {
            runOnEDT(component::repaint);
        }
    }
    
    /**
     * Safely repaints a specific region of a component
     */
    public static void safeRepaint(Component component, int x, int y, int width, int height) {
        if (component != null && component.isDisplayable()) {
            runOnEDT(() -> component.repaint(x, y, width, height));
        }
    }
    
    /**
     * Checks if a point is within component bounds
     */
    public static boolean isWithinBounds(Component component, Point point) {
        if (component == null || point == null) return false;
        Rectangle bounds = component.getBounds();
        return point.x >= 0 && point.y >= 0 && 
               point.x < bounds.width && point.y < bounds.height;
    }
    
    /**
     * Calculates distance between two points
     */
    public static double distance(Point p1, Point p2) {
        if (p1 == null || p2 == null) return Double.MAX_VALUE;
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
