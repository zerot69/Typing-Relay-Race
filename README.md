# Typing Relay Race (Tyrera)

Tyrera is a command-line typing game that uses Java TCP Server. The game features user registration and login, single-player mode, and multiplayer mode.

## Game Features

### User Registration and Login
User registration and login are implemented in the game. The user's data is stored on the session only.

### Single Player Mode
In single-player mode, the player can practice typing skills.

### Multiplayer Mode
Multiplayer mode is divided into the following phases:

1. Matchmaking
The game creates a match between four players, divided into two teams.

2. Phase 1 & Phase 2
The game is divided into two phases, and each player in the two teams tries to score as many points as possible.

3. Scoring
The score of each player is calculated based on:

- Time elapsed
- WPM = ((The length of the typed string / 5) / Time elapsed) * 60
- Accuracy (%)
- Score = WPM * Accuracy

4. Result
The game sends the players the result of the match.

## How to Run

To run the game, follow these steps:

1. Build and run ServerTCP.
2. Run ClientTCP to join the server (multiple instances are available).
3. Enjoy the game and improve your typing skills.

## Credits

This game was created by [zerot69](http://github.com/zerot69) and [baohieu078](http://github.com/baohieu078).
