package com.chessmates.model

/**
 * Represents a side color in a chess game.
 */
enum GameColor {
    BLACK,
    WHITE

    GameColor opposite() { this == BLACK ? WHITE : BLACK }

    @Override
    String toString() { this.name().toLowerCase() }
}