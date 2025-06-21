# Chess Game with AI and Online Multiplayer

A comprehensive Java chess game featuring Stockfish AI integration, online multiplayer, and educational features.

## Features

### 🎮 Core Gameplay
- **Complete Chess Rules**: All standard chess rules including check, checkmate, stalemate, castling, en passant, and pawn promotion
- **Move Validation**: Prevents illegal moves and enforces chess rules
- **Turn-based Logic**: Proper White/Black alternation

### 🤖 AI Features
- **Stockfish Integration**: Uses the world's strongest chess engine for AI gameplay
- **Adjustable Difficulty**: Configurable search depth for different skill levels
- **Move Analysis**: AI can analyze positions and suggest best moves

### 🌐 Online Multiplayer
- **Client-Server Architecture**: TCP socket-based networking
- **Real-time Synchronization**: Moves update instantly across clients
- **Chat System**: In-game text chat between players
- **Lobby System**: Host and join games easily

### 🎨 User Interface
- **Swing-based GUI**: Clean, modern interface
- **Visual Piece Movement**: Click-to-move with highlighted valid moves
- **Game Status Panel**: Shows turn, move count, and game state
- **Coordinate System**: File (a-h) and rank (1-8) labels

### 💾 File Handling
- **FEN Support**: Save and load current board positions
- **PGN Support**: Save complete game history in standard format
- **Auto-save**: Automatic game state preservation

### 🧠 Educational Features
- **Teacher Module**: Provides hints and move analysis
- **Move Comparison**: Compare your moves with engine suggestions
- **Position Evaluation**: Get feedback on current position strength

## Project Structure

```
src/main/java/com/chess/
├── Main.java                 # Application entry point
├── engine/
│   └── ChessEngine.java      # Stockfish integration
├── gui/
│   ├── ChessGUI.java         # Main GUI window
│   ├── ChessBoardPanel.java  # Chess board display
│   ├── GameStatusPanel.java  # Game status information
│   └── ChatPanel.java        # Chat interface
├── model/
│   ├── Board.java            # Game board and state
│   ├── Move.java             # Move representation
│   ├── Piece.java            # Abstract piece class
│   ├── Position.java         # Board position
│   ├── Player.java           # Player representation
│   └── pieces/               # Individual piece implementations
│       ├── King.java
│       ├── Queen.java
│       ├── Rook.java
│       ├── Bishop.java
│       ├── Knight.java
│       └── Pawn.java
├── network/
│   ├── Server.java           # Multiplayer server
│   └── Client.java           # Multiplayer client
├── teacher/
│   └── TeacherModule.java    # Educational features
└── utils/
    ├── Logger.java           # Logging utility
    └── FileHandler.java      # File I/O operations
```

## Prerequisites

- **Java 11 or higher**
- **Maven** (for building)
- **Stockfish Engine** (included in the project)

## Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd Chess
   ```

2. **Build the project**:
   ```bash
   mvn clean compile
   ```

3. **Run the application**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.chess.Main"
   ```

## Usage

### Starting the Application

The application provides several startup modes:

```bash
# Main menu (default)
java -jar chess-game.jar

# Start as server (host game)
java -jar chess-game.jar server

# Start as client (join game)
java -jar chess-game.jar client

# Start AI game
java -jar chess-game.jar ai
```

### Game Modes

#### 1. Local Game (2 Players)
- Two players on the same machine
- Click to select pieces and make moves
- Valid moves are highlighted in green

#### 2. AI Game
- Play against Stockfish engine
- Adjustable difficulty levels
- AI provides move analysis

#### 3. Online Multiplayer
- **Hosting**: Start a server and wait for opponent
- **Joining**: Connect to a server by entering the address
- **Chat**: Communicate with opponent during the game

### Controls

- **Move Pieces**: Click on a piece to select it, then click on destination square
- **Deselect**: Click on the same piece again
- **Valid Moves**: Highlighted in green when a piece is selected
- **Chat**: Type messages in the chat panel (online games only)

### File Operations

#### Saving Games
- **FEN Format**: Saves current board state
- **PGN Format**: Saves complete game history
- **Auto-save**: Automatic backup with timestamps

#### Loading Games
- Load previously saved FEN files
- Continue games from any position

## Configuration

### Stockfish Engine
The Stockfish engine is included in the `stockfish-windows-x86-64-avx2/` directory. The application automatically detects and uses it.

### Network Settings
- **Default Port**: 8080
- **Connection Timeout**: 30 seconds
- **Max Players**: 2 per game

## Development

### Building from Source

```bash
# Compile
mvn compile

# Run tests
mvn test

# Create JAR
mvn package

# Run with Maven
mvn exec:java -Dexec.mainClass="com.chess.Main"
```

### Adding New Features

1. **New Piece Types**: Extend the `Piece` abstract class
2. **GUI Components**: Add new panels to the `gui` package
3. **Network Protocols**: Modify `Server` and `Client` classes
4. **AI Enhancements**: Extend `ChessEngine` class

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ChessEngineTest

# Generate test coverage report
mvn jacoco:report
```

## Troubleshooting

### Common Issues

1. **Stockfish not found**
   - Ensure Stockfish executable is in the correct path
   - Check file permissions on the executable

2. **Network connection failed**
   - Verify server address and port
   - Check firewall settings
   - Ensure server is running

3. **GUI not displaying properly**
   - Update Java version to 11 or higher
   - Check system display settings

### Logging

The application uses SLF4J for logging. Log levels can be configured in `logback.xml`:

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- **Stockfish Team**: For providing the chess engine
- **Java Swing**: For the GUI framework
- **Maven**: For build management

## Future Enhancements

- [ ] Drag-and-drop piece movement
- [ ] Advanced move animations
- [ ] Tournament mode
- [ ] Opening book integration
- [ ] Position analysis tools
- [ ] Game replay functionality
- [ ] Multiple AI engines support
- [ ] Mobile client support 