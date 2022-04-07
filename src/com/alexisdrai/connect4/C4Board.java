package com.alexisdrai.connect4;

import java.nio.file.Path;
import java.util.Scanner;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Objects;

import static com.alexisdrai.util.Misc.*;

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
    private static final int TTL_COLS      = 7;
    private static final int TTL_ROWS      = 6;
    private static final int TTL_PLAYERS   = 2;
    private static final int WIN_CONDITION = 4;

    static final Scanner scanner = new Scanner(System.in);

    /**
     * an array of the indices of the topmost free cell of each column
     */
    private final int[]      topFreeCells = new int[TTL_COLS];
    private final C4Player[] players      = new C4Player[TTL_PLAYERS];
    private final Cell[][]   board        = new Cell[TTL_ROWS][];

    private C4Player currentPlayer;
    private boolean  isWon;
    private boolean  isFull;
    private int      tokensLeft;

    C4Board()
    {
        this(null);
        assignPlayers();
        resetBoard();
    }

    C4Board(Path path)
    {
        this.isWon = false;
        this.isFull = false;
        this.currentPlayer = null;
        this.tokensLeft = TTL_COLS * TTL_ROWS;
        if (path != null)
        {
            this.load(path);
        }
    }

    private void assignPlayers()
    {
        int botCount = 0;
        int allCount = 0;
        for (Color color : Color.values())
        {
            String name = null;

            while (name == null || name.equals(""))
            {
                String colorStr = color.toString().toLowerCase();
                if (color == Color.RED)
                {
                    colorStr = toRed(colorStr);
                }
                if (color == Color.YELLOW)
                {
                    colorStr = toYellow(colorStr);
                }
                System.out.println("Who shall play " + colorStr + "?");
                System.out.println(
                        "(please enter your name, or \"bot\" (in lowercase) to assign them " + colorStr + ")");
                if (scanner.hasNextLine())
                {
                    name = scanner.nextLine();
                }
            }

            if (name.equals("bot"))
            {
                botCount++;
                this.players[allCount] = new C4Player_CPU(name + "_" + botCount, color);
            }
            else
            {
                this.players[allCount] = new C4Player(name, color);
            }
            allCount++;
        }
        this.setCurrentPlayer(this.players[0]);
    }

    private void resetBoard()
    {
        // putting the board itself together
        for (int i = 0; i < TTL_ROWS; i++)
        {
            this.board[i] = new Cell[TTL_COLS];
            for (int j = 0; j < TTL_COLS; j++)
            {
                this.board[i][j] = new Cell();
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
                    this.board[i][j].setNeighbor(
                            Direction.UP,
                            this.board[i - 1][j]
                    );
                }
                if (j < TTL_COLS - 1)
                {
                    // not rightmost col => add right neighbors
                    this.board[i][j].setNeighbor(
                            Direction.RIGHT,
                            this.board[i][j + 1]
                    );
                }
                if (i < TTL_ROWS - 1)
                {
                    // not bottom row => add lower neighbors
                    this.board[i][j].setNeighbor(
                            Direction.DOWN,
                            this.board[i + 1][j]
                    );
                }
                if (j > 0)
                {
                    // not leftmost col => add left neighbors
                    this.board[i][j].setNeighbor(
                            Direction.LEFT,
                            this.board[i][j - 1]
                    );
                }
            }
        }

        // marking all the bottom cells as free
        Arrays.fill(this.topFreeCells, TTL_ROWS - 1);
    }

    C4Player getCurrentPlayer()
    {
        return this.currentPlayer;
    }

    private void setCurrentPlayer(C4Player currentPlayer)
    {
        this.currentPlayer = currentPlayer;
    }

    Cell[][] getBoard()
    {
        return this.board.clone();
    }

    int[] getTopFreeCells()
    {
        return this.topFreeCells.clone();
    }

    C4Player[] getPlayers()
    {
        return this.players.clone();
    }

    private boolean isFull()
    {
        return this.isFull;
    }

    void updateFull()
    {
        this.isFull = (this.getTokensLeft() == 0);
    }

    private void win()
    {
        this.isWon = true;
    }

    boolean isWon()
    {
        return this.isWon;
    }

    boolean isOver()
    {
        return this.isWon() || this.isFull();
    }

    private void takeCell(int columnIdx)
    {
        this.topFreeCells[columnIdx]--;
        this.useToken();
    }

    private void useToken()
    {
        this.tokensLeft--;
        this.updateFull();
    }

    int getTokensLeft()
    {
        return this.tokensLeft;
    }

    /**
     * runs the sequence of actions that make up a turn
     */
    public void playTurn()
    {
        this.displayBoard();
        this.registerMove(this.getCurrentPlayer());
        if (this.isOver())
        {
            System.out.println("~~~~~~~~~~~~~~ Game over ~~~~~~~~~~~~~~");
            if (this.isWon())
            {
                System.out.println("Winner: " + this.getCurrentPlayer().getColorfulName());
            }
            this.displayBoard();
        }
        this.switchPlayer();
    }

    private void switchPlayer()
    {
        // nice to have: iterate through whole player structure regardless of size,
        // to allow for scaling to more than 2 players

        C4Player next;
        if (this.currentPlayer == this.players[0])
        {
            next = this.players[1];
        }
        else
        {
            next = this.players[0];
        }
        this.setCurrentPlayer(next);
    }

    void displayBoard()
    {
        String cellStr = "WUH-OH"; // this default string should never be printed
        System.out.println();
        for (int i = 0; i < TTL_ROWS; i++)
        {
            for (int j = 0; j < TTL_COLS; j++)
            {
                Color color = this.board[i][j].getColor();
                if (color == null)
                {
                    cellStr = toOriginalColor("0");
                }
                if (color == Color.RED)
                {
                    cellStr = toRed("#");
                }
                if (color == Color.YELLOW)
                {
                    cellStr = toYellow("@");
                }
                System.out.print(" " + cellStr);
            }
            System.out.println();
        }
        System.out.println();
    }

    void registerMove(C4Player player)
    {
        Objects.requireNonNull(player);

        int columnIdx = player.chooseMove();

        if (!(0 <= columnIdx && columnIdx < TTL_COLS))
        {
            throw new OutOfBoardException(columnIdx);
        }
        if (this.getTopFreeCells()[columnIdx] < 0)
        {
            throw new FullColumnException(columnIdx);
        }

        // finding "altitude" of inserted token
        int row = this.getTopFreeCells()[columnIdx];
        // coloring in the token
        this.board[row][columnIdx].setColor(player.getColor());
        // checking for victory
        this.check(this.board[row][columnIdx], player.getColor());
        // updating trackers ("altitudes", number of turns left)
        this.takeCell(columnIdx);
    }

    private void check(Cell cell, Color color)
    {
        Objects.requireNonNull(cell);
        Objects.requireNonNull(color);

        int maxAlignment = this.aligned(cell, color);
        if (maxAlignment >= WIN_CONDITION)
        {
            this.win();
        }
    }

    private int aligned(Cell cell, Color color)
    {
        Objects.requireNonNull(cell);
        Objects.requireNonNull(color);
        int[] alignments = new int[4];

        alignments[0] = alignedStraight(upOrigin(cell, color), color, Direction.DOWN);                      // '|'
        alignments[1] = alignedDiag(topLeftOrigin(cell, color), color, Direction.RIGHT, Direction.DOWN);    // '\'
        alignments[2] = alignedStraight(leftOrigin(cell, color), color, Direction.RIGHT);                   // '-'
        alignments[3] = alignedDiag(bottomLeftOrigin(cell, color), color, Direction.RIGHT, Direction.UP);   // '/'
        //        System.out.println("| " + alignments[0]
        //                           + " \\ " + alignments[1]
        //                           + " - " + alignments[2]
        //                           + " / " + alignments[3]);
        return Arrays.stream(alignments).max().getAsInt();
    }

    private Cell upOrigin(Cell cell, Color color)
    {
        Objects.requireNonNull(cell);
        Objects.requireNonNull(color);
        return this.straightOrigin(cell, color, Direction.UP);
    }

    private Cell topLeftOrigin(Cell cell, Color color)
    {
        Objects.requireNonNull(cell);
        Objects.requireNonNull(color);
        return this.diagOrigin(cell, color, Direction.LEFT, Direction.UP);
    }

    private Cell leftOrigin(Cell cell, Color color)
    {
        Objects.requireNonNull(cell);
        Objects.requireNonNull(color);
        return this.straightOrigin(cell, color, Direction.LEFT);
    }

    private Cell bottomLeftOrigin(Cell cell, Color color)
    {
        Objects.requireNonNull(cell);
        Objects.requireNonNull(color);
        return this.diagOrigin(cell, color, Direction.LEFT, Direction.DOWN);
    }

    private Cell diagOrigin(Cell cell, Color color, Direction leftRight, Direction upDown)
    {
        Objects.requireNonNull(cell);
        Objects.requireNonNull(color);
        Objects.requireNonNull(leftRight);
        Objects.requireNonNull(upDown);

        if (upDown == Direction.LEFT
            || upDown == Direction.RIGHT
            || leftRight == Direction.UP
            || leftRight == Direction.DOWN)
        {
            throw new InvalidDiagonalException(leftRight, upDown);
        }

        Cell halfNext = cell.getNeighbor(leftRight);
        if (halfNext == null)
        {
            return cell;
        }
        Cell next = halfNext.getNeighbor(upDown);
        if (next == null || next.getColor() != color)
        {
            return cell;
        }
        return this.diagOrigin(next, color, leftRight, upDown);
    }

    private Cell straightOrigin(Cell cell, Color color, Direction direction)
    {
        Objects.requireNonNull(cell);
        Objects.requireNonNull(color);
        Objects.requireNonNull(direction);

        Cell next = cell.getNeighbor(direction);
        if (next == null || next.getColor() == null || next.getColor() != color)
        {
            return cell;
        }
        return this.straightOrigin(next, color, direction);
    }

    private int alignedStraight(Cell cell, Color color, Direction direction)
    {
        Objects.requireNonNull(color);
        Objects.requireNonNull(direction);

        if (cell == null || cell.getColor() != color)
        {
            return 0;
        }
        Cell next = cell.getNeighbor(direction);
        return 1 + alignedStraight(next, color, direction);
    }

    private int alignedDiag(Cell cell, Color color, Direction leftRight, Direction upDown)
    {
        Objects.requireNonNull(color);
        Objects.requireNonNull(leftRight);
        Objects.requireNonNull(upDown);

        if (cell == null || cell.getColor() != color)
        {
            return 0;
        }
        Cell halfNext = cell.getNeighbor(leftRight);
        if (halfNext == null)
        {
            return 1;
        }
        Cell next = halfNext.getNeighbor(upDown);
        return 1 + alignedDiag(next, color, leftRight, upDown);
    }

    private void save(Path path)
    {
        //call write functions from util misc
    }

    private void load(Path path)
    {
        //call write functions from util misc
    }

    /**
     * represents a cell in a Connect-4 board
     * <p><code>Cell</code>s can only access other <code>Cell</code>s, and the {@link Color} and {@link Direction}
     * <code>Enum</code>s, which are all static</p>
     */
    private static class Cell
    {
        private final EnumMap<Direction, Cell> neighbors;

        private Color color;

        Cell()
        {
            this.color = null;
            this.neighbors = new EnumMap<>(Direction.class);
            for (Direction direction : Direction.values())
            {
                this.neighbors.put(direction, null);
            }
        }

        /**
         * <strong>may return <code>null</code></strong>
         *
         * @return
         */
        Color getColor()
        {
            return this.color;
        }

        void setColor(Color color)
        {
            this.color = Objects.requireNonNull(color);
        }

        void removeColor()
        {
            this.color = null;
        }

        /**
         * <strong>may return <code>null</code></strong>
         *
         * @param direction
         * @return
         */
        Cell getNeighbor(Direction direction)
        {
            return this.neighbors.get(direction);
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
            this.name = Objects.requireNonNull(name);
            this.color = Objects.requireNonNull(color);
        }

        String getName()
        {
            return this.name;
        }

        Color getColor()
        {
            return this.color;
        }

        String getColorfulName()
        {
            if (this.color == Color.RED)
            {
                return toRed(this.getName());
            }
            if (this.color == Color.YELLOW)
            {
                return toYellow(this.getName());
            }
            System.out.println("Color " + this.color + " not accounted for in getColorfulName()");
            return this.getName();
        }

        int chooseMove()
        {
            int column = -1;

            while (column < 1 || column > TTL_COLS || getTopFreeCells()[column - 1] < 0)
            {
                System.out.printf("%s : please choose a non-full column between 1 and %d%n",
                                  this.getColorfulName(),
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
            System.out.println(this.getColorfulName() + "'s turn");
            // sleeping to give player a chance to see the board change
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }

            int topFreeIdx, score, otherScore, choiceIdx, blockIdx = -1;

            Cell[][] tmpBoard        = getBoard();
            int[]    tmpTopFreeCells = getTopFreeCells();
            int[]    columnScores    = new int[TTL_COLS];

            Color otherColor;
            if (getPlayers()[0].equals(this))
            {
                otherColor = getPlayers()[1].getColor();
            }
            else
            {
                otherColor = getPlayers()[0].getColor();
            }

            Objects.requireNonNull(tmpBoard);
            Objects.requireNonNull(tmpTopFreeCells);
            Objects.requireNonNull(otherColor);

            for (int i = 0; i < TTL_COLS; i++)
            {
                topFreeIdx = tmpTopFreeCells[i];
                if (topFreeIdx >= 0)
                {
                    Cell cell = tmpBoard[topFreeIdx][i];
                    score = testAligned(cell, this.getColor());
                    //                    System.out.printf("cell[%d][%d]; %s: %d\n", topFreeIdx, i, this.getColorfulName(), score);
                    otherScore = testAligned(cell, otherColor);
                    //                    System.out.printf("cell[%d][%d]; THEM: %d\n", topFreeIdx, i, otherScore);
                    if (score == WIN_CONDITION) // winning move
                    {
                        return i;
                    }
                    if (otherScore == WIN_CONDITION) // getting out of check
                    {
                        blockIdx = i;
                    }
                    columnScores[i] = score + otherScore;
                }
            }
            if (blockIdx >= 0)
            {
                return blockIdx;
            }
            boolean allEqual = true;
            for (int i = 0; i < TTL_COLS - 1; i++)
            {
                if (!Objects.equals(columnScores[i],
                                    columnScores[i + 1]))
                {
                    allEqual = false;
                    break;
                }
            }
            if (allEqual)
            {
                choiceIdx = (int) (Math.random() * TTL_COLS);
                //                System.out.println("idx at random... " + result);
            }
            else
            {
                choiceIdx = maxIdxFromArray(columnScores);
                //                System.out.println("idx of best is " + result + " with score of " + columnScores[result]);
            }
            return choiceIdx;
        }

        private int testAligned(Cell cell, Color color)
        {
            Objects.requireNonNull(cell);
            Objects.requireNonNull(color);

            cell.setColor(color);
            int score = aligned(cell, color);
            cell.removeColor();
            return score;
        }
    }

    private enum Color
    {
        RED, YELLOW
    }

    private enum Direction
    {
        UP, RIGHT, DOWN, LEFT
    }

    @SuppressWarnings("serial")
    private static class FullColumnException extends IllegalArgumentException
    {
        FullColumnException(int columnIdx)
        {
            super(String.format("column at index %d is already full -- chooseMove() should have prevented this",
                                columnIdx));
        }
    }

    @SuppressWarnings("serial")
    private static class OutOfBoardException extends IllegalArgumentException
    {
        OutOfBoardException(int columnIdx)
        {
            super(String.format("column %d not part of the board -- chooseColumn should have prevented this",
                                columnIdx));
        }
    }

    @SuppressWarnings("serial")
    private static class InvalidDiagonalException extends IllegalArgumentException
    {
        InvalidDiagonalException(Direction leftRight, Direction upDown)
        {
            super(String.format("%s + %s is not a valid diagonal",
                                Objects.requireNonNull(leftRight),
                                Objects.requireNonNull(upDown)));
        }
    }
}
