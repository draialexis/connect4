package com.alexisdrai.connect4;

import java.util.Scanner;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;

/**
 * a Connect-4 board.
 * <br>rows are numbered from 0 to HEIGHT-1, from top to bottom.
 * <br>columns are numbered from 0 to WIDTH-1, from left to right.
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
    private static final int     WIDTH   = 7;
    private static final int     HEIGHT  = 6;
    private static       Scanner scanner = new Scanner(System.in);

    private final int[] topFreeSlots = new int[WIDTH];

    private final EnumMap<Color, C4Player> players;

    private final Slot[][] slots;

    private int botCount;

    C4Board()
    {
        // registering all players
        this.botCount = 0;
        this.players = new EnumMap<>(Color.class);
        scanner.useDelimiter(System.lineSeparator());

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
                this.botCount++;
                this.players.put(color, new C4Player_CPU(name + "_" + this.botCount, color));
            }
            else
            {
                this.players.put(color, new C4Player(name, color));
            }
        }
        // marking all the bottom slots as free
        Arrays.fill(this.topFreeSlots, HEIGHT - 1);

        // putting the board itself together
        this.slots = new Slot[HEIGHT][];
        for (int i = 0; i < HEIGHT; i++)
        {
            this.slots[i] = new Slot[WIDTH];
            for (int j = 0; j < WIDTH; j++)
            {
                this.slots[i][j] = new Slot();
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
            throw new IllegalArgumentException("column not part of the board");
        }
        if (this.topFreeSlots[columnIdx] <= 0)
        {
            throw new IllegalStateException("column is already full");
        }

        int row = this.topFreeSlots[columnIdx];
        this.slots[row][columnIdx].setColor(player.getColor());

        this.topFreeSlots[columnIdx]--;
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
     * the sequence of actions that make up a turn
     */
    void play()
    {
        for (C4Player player : this.players.values())
        {
            if (Arrays.stream(this.topFreeSlots).sum() == (HEIGHT - 1) * WIDTH) // turn 1
            {
                this.displayBoard();
            }
            int columnIdx = player.chooseColumn();
            this.takeMove(columnIdx, player);
            this.displayBoard();
            // TODO interrupt game if win
        }
    }

    void displayBoard()
    {
        for (int i = 0; i < HEIGHT; i++)
        {
            for (int j = 0; j < WIDTH; j++)
            {
                System.out.print(" ");
                Optional<Color> color = this.slots[i][j].getColor();
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
     * represents a slot in a Connect-4 board
     */
    private class Slot
    {
        private final EnumMap<Direction, Color> neighbors;

        private Color color;

        Slot()
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

        Optional<Color> getNeighbor(Direction direction)
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

        int chooseColumn()
        {
            int column = -1;
            System.out.println(
                    this.getName() + " (" + this.getColor() + "): Please choose a column between 1 and " + WIDTH
            );

            while (1 > column || column > WIDTH)
            {
                column = scanner.nextInt();
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
        int chooseColumn()
        {
            // TODO this
            return super.chooseColumn();
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
