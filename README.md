# Typing Relay Race (Tyrera)
A simple command line Typing Game using Java TCP Server - Author: [zerot69](http://github.com/zerot69) & [baohieu078](http://github.com/baohieu078)

## Features:

1. **User registration and login (The data is stored on-the-session only)**
2. **Playing Single Player**
3. **Playing Multi Player:**

   1. Matchmaking: Create a game between 4 players, divided into 2 teams.
   2. Phase 1 & Phase 2: The game is divided into 2 phases, in which each player of 2 teams will try to score as many as possible.
   3. Scoring: The score of each player will be calculated as:
       - *Time elapsed*
       - *WPM = ( ( (The length of the typed String) / 5 ) / Time elapsed ) * 60*
       - *Accuracy (%)*
       - *Score = WPM * Accuracy*
   4. Result: Send the players the result of the match.
   
## How to run:
1. Bulid and Run ServerTCP.
2. Run ClientTCP to join the Server (Multiple instances are available).
3. Hope you have a great time entertaining with this.
