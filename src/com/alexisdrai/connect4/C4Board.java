package com.alexisdrai.connect4;

import java.util.Scanner;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import java.util.Objects;

/**
 * a Connect-4 board.
 * <br>rows are numbered from 0 to {@link C4Board#TTL_ROWS}-1, from top to bottom.
 * <br>columns are numbered from 0 to {@link C4Board#TTL_COLS}-1, from left to right.
 *
 * <table>
 * <tr> <td>.</td></th><th>0</th><th>1</th><th>2</th><th>3</th><th>4</th><th>5</th><th>6</th> </tr>
 * <tr> <th>0</th><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td> </tr>
 * <tr> <th>1</th><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td> </tr>
 * <tr> <th>2</th><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td> </tr>
 * <tr> <th>3</th><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td> </tr>
 * <tr> <th>4</th><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td> </tr>
 * <tr> <th>5</th><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td><td>*</td> </tr>
 * </table>
 */
public class C4Board
{
    private static final int TTL_COLS = 7;
    private static final int TTL_ROWS = 6;

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * an array of the indices of the topmost free cell of each column
     */
    private final int[]                    topFreeCells = new int[TTL_COLS];
    private final EnumMap<Color, C4Player> players      = new EnumMap<>(Color.class);
    private final Cell[][]                 cells        = new Cell[TTL_ROWS][];

    private boolean isWon;

    C4Board()
    {
        isWon = false;
        assignPlayers();
        resetBoard();
    }

    private void assignPlayers()
    {
        int botCount = 0;
        for (Color color : Color.values())
        {
            String name = null;

            while (name == null || name.equals(""))
            {
                System.out.println("Who shall play " + color + "?");
                System.out.println("Please enter your name, or \"bot\" (in lowercase) to assign them " + color);
                if (scanner.hasNextLine())
                {
                    name = scanner.nextLine();
                }
            }

            if (name.equals("bot"))
            {
                botCount++;
                this.players.put(color, new C4Player_CPU(name + "_" + botCount, color));
            }
            else
            {
                this.players.put(color, new C4Player(name, color));
            }
        }
    }

    private void resetBoard()
    {

        // putting the board itself together
        for (int i = 0; i < TTL_ROWS; i++)
        {
            this.cells[i] = new Cell[TTL_COLS];
            for (int j = 0; j < TTL_COLS; j++)
            {
                this.cells[i][j] = new Cell();
            }
        }

        //registering neighbors
        for (int i = 0; i < TTL_ROWS; i++)
        {
            for (int j = 0; j < TTL_COLS; j++)
            {
                if (i > 0)
                {
                    // not top row => add upper neighbors
                    this.cells[i][j].setNeighbor(
                            Direction.TOP,
                            this.cells[i - 1][j]
                    );
                }
                if (j < TTL_COLS - 1)
                {
                    // not rightmost col => add right neighbors
                    this.cells[i][j].setNeighbor(
                            Direction.RIGHT,
                            this.cells[i][j + 1]
                    );
                }
                if (i < TTL_ROWS - 1)
                {
                    // not bottom row => add lower neighbors
                    this.cells[i][j].setNeighbor(
                            Direction.BOTTOM,
                            this.cells[i + 1][j]
                    );
                }
                if (j > 0)
                {
                    // not leftmost col => add left neighbors
                    this.cells[i][j].setNeighbor(
                            Direction.LEFT,
                            this.cells[i][j - 1]
                    );
                }
            }
        }

        // marking all the bottom cells as free
        Arrays.fill(this.topFreeCells, TTL_ROWS - 1);
    }

    private boolean isFull()
    {
        return Arrays.stream(topFreeCells).sum() == -1 * TTL_COLS;
    }

    private void Win()
    {
        this.isWon = true;
    }

    boolean isWon()
    {
        return this.isWon;
    }

    /**
     * runs the sequence of actions that make up a turn
     */
    public boolean playTurn()
    {
        for (C4Player player : this.players.values())
        {
            this.displayBoard();
            this.registerMove(player);
            if (this.isWon() || this.isFull())
            {
                System.out.print("~~~~~~~~~~~~~~ Game over ~~~~~~~~~~~~~~\n"
                                 + player.getName() + " wins");
                //TODO use player.getColor() to display name in specific color
                this.displayBoard();
                return true;
            }
        }
        return false;
    }

    void displayBoard()
    {
        for (int i = 0; i < TTL_ROWS; i++)
        {
            for (int j = 0; j < TTL_COLS; j++)
            {
                System.out.print(" ");
                Optional<Color> color = this.cells[i][j].getColor();
                if (color.isEmpty())
                {
                    System.out.print("O");
                }
                else
                {
                    color.ifPresent(clr -> System.out.print("" + (clr == Color.RED ? "@" : "X")));
                    //TODO update with colors
                }
            }
            System.out.println();
        }
    }

    /**
     * @param player
     */
    void registerMove(C4Player player)
    {
        int columnIdx = Objects.requireNonNull(player).chooseMove();

        if (!(0 <= columnIdx && columnIdx < TTL_COLS))
        {
            throw new IllegalArgumentException("column not part of the board\nchooseColumn should have prevented this");
        }
        if (this.topFreeCells[columnIdx] < 0)
        {
            throw new FullColumnException(columnIdx);
        }

        int row = this.topFreeCells[columnIdx];
        this.cells[row][columnIdx].setColor(player.getColor());

        this.check(this.cells[row][columnIdx]);

        this.topFreeCells[columnIdx]--;
    }

    private void check(Cell cell)
    {
        // TODO
        // this.Win()
    }

    /**
     * represents a cell in a Connect-4 board
     */
    private class Cell
    {
        private final EnumMap<Direction, Cell> neighbors;

        private Color color;

        Cell()
        {
            this.color = null;
            this.neighbors = new EnumMap<>(Direction.class);
            this.neighbors.put(Direction.TOP, null);
            this.neighbors.put(Direction.RIGHT, null);
            this.neighbors.put(Direction.BOTTOM, null);
            this.neighbors.put(Direction.LEFT, null);
        }

        Optional<Color> getColor()
        {
            return Optional.ofNullable(this.color);
        }

        void setColor(Color color)
        {
            this.color = color;
        }

        Optional<Cell> getNeighbor(Direction direction)
        {
            return Optional.ofNullable(this.neighbors.get(direction));
        }

        void setNeighbor(Direction direction, Cell cell)
        {
            Objects.requireNonNull(direction);
            Objects.requireNonNull(cell);
            this.neighbors.put(direction, cell);
        }
    }

    private class C4Player
    {
        private final String name;
        private final Color  color;

        C4Player(String name, Color color)
        {
            this.name = name;
            this.color = color;
        }

        String getName()
        {
            return this.name;
        }

        Color getColor()
        {
            return this.color;
        }

        int chooseMove()
        {
            int column = -1;

            while (column < 1 || column > TTL_COLS || topFreeCells[column - 1] < 0)
            {
                System.out.printf("%s (%s): Please choose a non-full column between 1 and %d%n",
                                  this.getName(),
                                  this.getColor(),
                                  TTL_COLS);
                try
                {
                    column = scanner.nextInt();
                } catch (Exception ignored)
                {
                    if (scanner.hasNextLine()) // purging the scanner for the next attempt
                    {
                        scanner.nextLine();
                    }
                }
            }

            return column - 1; // the index of said column
        }
    }

    private class C4Player_CPU extends C4Player
    {
        C4Player_CPU(String name, Color color)
        {
            super(name, color);
        }

        @Override
        int chooseMove()
        {
            // TODO this
            return super.chooseMove();
        }
    }

    private enum Color
    {
        RED, YELLOW
    }

    private enum Direction
    {
        TOP, RIGHT, BOTTOM, LEFT
    }

    private static class FullColumnException extends IllegalArgumentException
    {
        FullColumnException(int columnIdx)
        {
            super(String.format("column at index %d is already full -- chooseMove() should have prevented this",
                                columnIdx));
        }
    }

    private static class OutOfBoardException extends IllegalArgumentException
    {
        OutOfBoardException(int columnIdx)
        {
            super(String.format("column %d not part of the board\nchooseColumn should have prevented this",
                                columnIdx));
        }
    }
}
