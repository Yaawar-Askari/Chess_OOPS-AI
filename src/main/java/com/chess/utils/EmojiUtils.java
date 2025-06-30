package com.chess.utils;

/**
 * Utility class for handling emoji support in a cross-platform way
 */
public class EmojiUtils {
    
    // Common chess and gaming related emojis that work across platforms
    public static final String CHESS_PIECE = "\u2659"; // ♙
    public static final String CROWN = "\u2656"; // ♖
    public static final String SMILEY = "\u263A"; // ☺
    public static final String CHECK_MARK = "\u2713"; // ✓
    public static final String X_MARK = "\u2717"; // ✗
    public static final String WARNING = "\u26A0"; // ⚠
    public static final String STAR = "\u2605"; // ★
    public static final String ARROW_RIGHT = "\u2192"; // →
    
    /**
     * Convert simple text emojis to Unicode when possible
     */
    public static String enhanceWithEmojis(String text) {
        if (text == null) return null;
        
        return text
            .replace(":)", SMILEY)
            .replace("<chess>", CHESS_PIECE)
            .replace(":D", SMILEY)
            .replace("[X]", X_MARK)
            .replace("[!]", WARNING)
            .replace("[*]", STAR)
            .replace(">>", ARROW_RIGHT);
    }
    
    /**
     * Check if the system supports Unicode emojis properly
     */
    public static boolean supportsUnicodeEmojis() {
        try {
            // Try to create a string with Unicode emoji
            String test = "\u2659";
            return test.length() == 1;
        } catch (Exception e) {
            return false;
        }
    }
}
