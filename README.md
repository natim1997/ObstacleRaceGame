# Obstacle Race Game

A dynamic Android game developed as part of a mobile development course assignment.

## Game Description

The player controls a car moving between **five lanes** at the bottom of the screen.  
The game is structured as a **matrix-based endless runner**, where:

- **Stones** (obstacles) and **coins** randomly appear at the top and descend row by row.
- The player must avoid **hitting stones** and try to **collect coins**.
- The car starts at the **bottom center**, and can move **left or right** using buttons or phone **tilt sensors** (user choice).
- The game ends after **three collisions** with stones.

## Features

-  **Five-lane endless grid**
-  **Collision detection** with effects (blinking, sound)
-  **Coin collection** for bonus points
-  **Lives system** (3 hearts)
-  **Score counter** and **distance meter**
-  **Two control modes**: Buttons or Phone Tilt (Accelerometer)
-  **High Scores screen**:
  - Shows top 10 scores ever recorded
  - Displays score name and value
  - Includes a map showing the location of each score
-  **Game Over screen**:
  - Prompts user to enter name
  - Saves score and redirects to main menu

## Technologies Used

- Kotlin
- Android Studio
- FrameLayout & GridLayout
- SharedPreferences
- Fragments
- Google Maps SDK

## How to Play

1. Launch the app and choose **control mode** (buttons or sensors).
2. Press **Start** to begin.
3. **Move the car left/right** to avoid falling stones.
4. **Collect coins** to increase score.
5. Game ends after **3 crashes**.
6. At the end, **enter your name** to save your score.
7. View **top 10 scores** and see where each score was recorded on a map.




