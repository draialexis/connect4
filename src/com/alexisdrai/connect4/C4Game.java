package com.alexisdrai.connect4;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import static com.alexisdrai.util.Misc.*;

public class C4Game implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    static final Scanner scanner   = new Scanner(System.in);
    static final int     SAVE_CODE = -2;
    static final int     LOAD_CODE = -3;
    static final int     QUIT_CODE = -4;

    private static final int TTL_COLS      = 7;
    private static final int TTL_ROWS      = 6;
    private static final int TTL_PLAYERS   = 2;
    private static final int WIN_CONDITION = 4;

    //an array of the indices of the topmost free cell of each column
    private       int[]      topFreeCells = new int[TTL_COLS];
    private       C4Player[] players      = new C4Player[TTL_PLAYERS];
    private final Cell[][]   board        = new Cell[TTL_ROWS][TTL_COLS];

    private int      tokensLeft;
    private boolean  isWon;
    private boolean  isFull;
    private C4Player currentPlayer;

    C4Game()
    {
        this.tokensLeft = TTL_COLS * TTL_ROWS;
        Arrays.fill(this.topFreeCells, TTL_ROWS - 1); // marking all free cells as bottom cells
        this.isWon = false;
        this.isFull = false;
        this.assignPlayers();
        this.currentPlayer = this.players[0];
        this.resetBoard();
    }

    C4Game(Path path) throws IOException, ClassNotFoundException
    {
        C4Game loaded = this.load(Objects.requireNonNull(path));

        this.tokensLeft = loaded.getTokensLeft();
        this.isWon = loaded.isWon();
        this.isFull = loaded.isFull();
        this.currentPlayer = loaded.getCurrentPlayer();

        this.topFreeCells = loaded.getTopFreeCells();
        this.players = loaded.getPlayers();

        Cell[][] loadedBoard = loaded.getBoard();
        this.resetBoard();
        for (int i = 0; i < TTL_ROWS; i++)
        {
            for (int j = 0; j < TTL_COLS; j++)
            {
                this.board[i][j].color = loadedBoard[i][j].getColor();
                this.board[i][j].neighbors.putAll(loadedBoard[i][j].getAllNeighbors());
            }
        }
        displayBoard();
    }

    void save(@SuppressWarnings("SameParameterValue") Path path) throws IOException
    {
        Objects.requireNonNull(path);
        System.out.println("saving game...");

        FileOutputStream   fos = new FileOutputStream(String.valueOf(path));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.flush();
        oos.close();

        System.out.println("game saved");
    }

    private C4Game load(Path path) throws IOException, ClassNotFoundException
    {
        Objects.requireNonNull(path);
        System.out.println("loading game...");

        FileInputStream   fis = new FileInputStream(String.valueOf(path));
        ObjectInputStream ois = new ObjectInputStream(fis);

        C4Game newBoard = (C4Game) ois.readObject();
        ois.close();

        System.out.println("game loaded");
        return newBoard;
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
                    colorStr = ANSI_RED + colorStr + ANSI_RESET;
                }
                if (color == Color.YELLOW)
                {
                    colorStr = ANSI_YELLOW + colorStr + ANSI_RESET;
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
                    // not the top row => add upper neighbors
                    this.board[i][j].setNeighbor(
                            Direction.UP,
                            this.board[i - 1][j]
                    );
                }
                if (j < TTL_COLS - 1)
                {
                    // not the rightmost col => add right neighbors
                    this.board[i][j].setNeighbor(
                            Direction.RIGHT,
                            this.board[i][j + 1]
                    );
                }
                if (i < TTL_ROWS - 1)
                {
                    // not the bottom row => add lower neighbors
                    this.board[i][j].setNeighbor(
                            Direction.DOWN,
                            this.board[i + 1][j]
                    );
                }
                if (j > 0)
                {
                    // not the leftmost col => add left neighbors
                    this.board[i][j].setNeighbor(
                            Direction.LEFT,
                            this.board[i][j - 1]
                    );
                }
            }
        }
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
        return this.board;
    }

    int[] getTopFreeCells()
    {
        return this.topFreeCells;
    }

    C4Player[] getPlayers()
    {
        return this.players;
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

    void switchPlayer()
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
        String cellStr = "WUH-OH"; // this default string should never get printed
        System.out.println();
        for (int i = 0; i < TTL_ROWS; i++)
        {
            for (int j = 0; j < TTL_COLS; j++)
            {
                Color color = this.board[i][j].getColor();
                // nice to have: account for more players, later down the line?
                if (color == null)
                {
                    cellStr = ANSI_RESET + "0";
                }
                if (color == Color.RED)
                {
                    cellStr = ANSI_RED + "#" + ANSI_RESET;
                }
                if (color == Color.YELLOW)
                {
                    cellStr = ANSI_YELLOW + "@" + ANSI_RESET;
                }
                System.out.print(" " + cellStr);
            }
            System.out.println();
        }
        System.out.println();
    }

    void registerMove(int columnIdx)
    {
        Color color = this.getCurrentPlayer().getColor();

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
        this.board[row][columnIdx].setColor(color);
        // checking for victory
        this.check(this.board[row][columnIdx], color);
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
            throws InvalidDiagonalException
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

    private static class Cell implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 1L;

        private final EnumMap<Direction, Cell> neighbors = new EnumMap<>(Direction.class);

        private Color color;

        Cell()
        {
            this.color = null;
            for (Direction direction : Direction.values())
            {
                this.neighbors.put(direction, null);
            }
        }

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

        Cell getNeighbor(Direction direction)
        {
            Objects.requireNonNull(direction);
            return this.neighbors.get(direction);
        }

        void setNeighbor(Direction direction, Cell cell)
        {
            Objects.requireNonNull(direction);
            Objects.requireNonNull(cell);
            this.neighbors.put(direction, cell);
        }

        EnumMap<Direction, Cell> getAllNeighbors()
        {
            return this.neighbors;
        }
    }

    public class C4Player implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 1L;

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

        // can be expanded with other colors from com.alexisdrai.util.Misc
        String getColorfulName()
        {
            if (this.color == Color.RED)
            {
                return ANSI_RED + this.getName() + ANSI_RESET;
            }
            if (this.color == Color.YELLOW)
            {
                return ANSI_YELLOW + this.getName() + ANSI_RESET;
            }
            System.out.println("Color " + this.color + " not accounted for in getColorfulName()");
            return this.getName();
        }

        int chooseMove()
        {
            int column = -1;
            int input;
            while (1 > column || column > C4Game.TTL_COLS || getTopFreeCells()[column - 1] < 0)
            {
                System.out.printf("%s : please choose a non-full column between 1 and %d%n",
                                  this.getColorfulName(),
                                  TTL_COLS);
                try
                {
                    column = scanner.nextInt();
                } catch (Exception ignored)
                {
                    if (scanner.hasNext())
                    {
                        input = scanner.next().charAt(0);
                        switch (input)
                        {
                            case ('s') -> {
                                return SAVE_CODE;
                            }
                            case ('l') -> {
                                return LOAD_CODE;
                            }
                            case ('q') -> {
                                return QUIT_CODE;
                            }
                        }
                        scanner.nextLine();// purging the scanner for the next attempt
                    }
                }
            }
            return column - 1;// the index of said column
        }
    }

    private final class C4Player_CPU extends C4Player
    {
        @Serial
        private final static long serialVersionUID = 1L;

        C4Player_CPU(String name, Color color)
        {
            super(name, color);
            Objects.requireNonNull(name);
            Objects.requireNonNull(color);
        }

        @Override
        int chooseMove()
        {
            System.out.println(this.getColorfulName() + "'s turn");
            try
            {
                // sleeping to give player a chance to see the board change
                Thread.sleep(250);
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
                    otherScore = testAligned(cell, otherColor);
                    if (score == WIN_CONDITION)
                    {
                        return i;
                    }
                    if (otherScore == WIN_CONDITION)
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
            }
            else
            {
                choiceIdx = maxIdxFromArray(columnScores);
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

    private static class FullColumnException extends IllegalArgumentException
    {
        @Serial
        private static final long serialVersionUID = 1L;

        FullColumnException(int columnIdx)
        {
            super(String.format("column at index %d is already full -- chooseMove() should have prevented this",
                                columnIdx));
        }
    }

    private static class OutOfBoardException extends IllegalArgumentException
    {
        @Serial
        private static final long serialVersionUID = 1L;

        OutOfBoardException(int columnIdx)
        {
            super(String.format("column %d not part of the board -- chooseColumn should have prevented this",
                                columnIdx));
        }
    }

    private static class InvalidDiagonalException extends IllegalArgumentException
    {
        @Serial
        private static final long serialVersionUID = 1L;

        InvalidDiagonalException(Direction leftRight, Direction upDown)
        {
            super(String.format("%s + %s is not a valid diagonal",
                                Objects.requireNonNull(leftRight),
                                Objects.requireNonNull(upDown)));
        }
    }
}
