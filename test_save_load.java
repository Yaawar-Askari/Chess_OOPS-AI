import com.chess.model.Board;
import com.chess.utils.GameStateManager;
import java.io.File;

/**
 * Simple test to verify save/load functionality
 */
public class test_save_load {
    public static void main(String[] args) {
        try {
            System.out.println("Testing Save/Load functionality...");
            
            // Create a board and make some moves
            Board board = new Board();
            System.out.println("Initial board FEN: " + board.toFEN());
            
            // Create a game state
            GameStateManager.GameState gameState = GameStateManager.createGameState(
                board,
                board.getMoveHistoryInAlgebraicNotation(),
                "ongoing",
                "White",
                "LOCAL"
            );
            
            // Save to file
            File testFile = new File("test_game.json");
            GameStateManager.saveGameToFile(gameState, testFile);
            System.out.println("Game saved to: " + testFile.getAbsolutePath());
            
            // Load from file
            GameStateManager.GameState loadedState = GameStateManager.loadGameFromFile(testFile);
            System.out.println("Game loaded successfully!");
            System.out.println("Loaded FEN: " + loadedState.getBoardFEN());
            System.out.println("Current turn: " + loadedState.getCurrentTurn());
            System.out.println("Game mode: " + loadedState.getGameMode());
            System.out.println("Saved at: " + loadedState.getSavedAt());
            
            // Clean up
            testFile.delete();
            System.out.println("Test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
