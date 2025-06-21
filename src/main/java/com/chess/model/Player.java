package com.chess.model;

import com.chess.engine.ChessEngine;
import java.io.IOException;

public abstract class Player {
    protected final String color;
    public Player(String color) { this.color = color; }
    public String getColor() { return color; }
    public abstract Move makeMove(Board board) throws IOException;
}

class HumanPlayer extends Player {
    public HumanPlayer(String color) { super(color); }
    @Override
    public Move makeMove(Board board) {
        // Human move is handled by GUI/input, so return null here
        return null;
    }
}

class AIPlayer extends Player {
    private final ChessEngine engine;
    private final int depth;
    public AIPlayer(String color, ChessEngine engine, int depth) {
        super(color);
        this.engine = engine;
        this.depth = depth;
    }
    @Override
    public Move makeMove(Board board) throws IOException {
        return engine.getBestMove(board, depth);
    }
} 