# GameOfLife
My implementation of Conway's Game of Life

This program simulates Conway's Game of Life and adds an additional game option called High Life. The game allows a user to select cells from grid in which they "come alive" and plays an animation based on a set of rules. These rules cause the cells to either stay alive, or die. Alive cells are green, and dead cells are black. The animation continues endlessly and if the animation causes the alive cells to "move" then, the edges of the grid are wrapped to the other side like in pac-man and other games. High Life does the same thing, only with a different set of rules that cause the cells to either stay alive or die. The game also has save/load features that allows the user to save the state of the cells + the type of game they're playing.

Additional Features: Accelerators, Suggested Games, Revert Button that displays the original state (useful if your messing around and don't remember the original state but liked the animation)
