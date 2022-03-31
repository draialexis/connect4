package com.alexisdrai.connect4;

import java.util.Scanner;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;

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

    C4Board()
    {
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

        // registering neighbors
        // TODO ^
    }

    /**
     * @param columnIdx must be between 0 and {@link C4Board#WIDTH} - 1
     * @param player
     */
    void takeMove(int columnIdx, C4Player player)
    {
        if (!(0 <= columnIdx && columnIdx < WIDTH))
        {
            throw new IllegalArgumentException("column not part of the board\nchooseColumn should have prevented this");
        }
        if (this.topFreeCells[columnIdx] < 0)
        {
            throw new IllegalStateException("column is already full\nchooseColumn should have prevented this");
        }

        // marking all the bottom cells as free
        Arrays.fill(this.topFreeCells, TTL_ROWS - 1);
    }


    /*
    boolean hasWon(int column, int row)
    {
        return (
                this.isHorizWin(column, row)            //   l   (-)     r
                || this.isVertWin(column, row)          //   u   (|)     d
                || this.isFwdDiagWin(column, row)       //   ld  (/)     ur
                || this.isBackDiagWin(column, row)      //   lu  (\)     dr
        );
    }

    /**
     * runs the sequence of actions that make up a turn
     */
    void playTurn()
    {
        for (C4Player player : this.players.values())
        {
            this.displayBoard();
            this.registerMove(player);
            // TODO interrupt game if win, displayboard before interrupt
        }
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
            throw new IllegalStateException("column is already full\nchooseColumn should have prevented this");
        }

        int row = this.topFreeCells[columnIdx];
        this.cells[row][columnIdx].setColor(player.getColor());

        this.topFreeCells[columnIdx]--;
    }


    /*
    boolean hasWon(int column, int row)
    {
        return (
                this.isHorizWin(column, row)            //   l   (-)     r
                || this.isVertWin(column, row)          //   u   (|)     d
                || this.isFwdDiagWin(column, row)       //   ld  (/)     ur
                || this.isBackDiagWin(column, row)      //   lu  (\)     dr
        );
    }
    */

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
}
