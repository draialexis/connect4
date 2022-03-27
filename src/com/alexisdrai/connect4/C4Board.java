package com.alexisdrai.connect4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;

public class C4Board
{
    private static final int WIDTH     = 7;
    private static final int HEIGHT    = 6;
    private static final int NUM_SLOTS = WIDTH * HEIGHT;

    private final int[] topFreeSlots = new int[NUM_SLOTS];

    private final ArrayList<ArrayList<Slot>> slots;

    C4Board()
    {
        // rows are numbered 0->HEIGHT-1 starting from top
        Arrays.fill(this.topFreeSlots, HEIGHT - 1);

        this.slots = new ArrayList<>(HEIGHT);
        for (int i = 0; i < HEIGHT; i++)
        {
            this.slots.set(i, new ArrayList<>(WIDTH));
            for (int j = 0; j < WIDTH; j++)
            {
                this.slots.get(i).set(j, new Slot());
            }
        }
    }

    int insertCoin(int column, int playerNum)
    {
        if (!(0 <= column && column < WIDTH))
        {
            System.out.println("column not part of the board");
            // TODO make proper exception
            // maybe use a try-catch in the Main?
        }
        if (this.topFreeSlots[column] <= 0)
        {
            System.out.println("column is already full");
            // TODO make proper exception
            // maybe use a try-catch in the Main?
        }
        int row = this.gravity();
        if (this.hasNeighbors(column, row))
        {
            if (this.hasWon(column, row))
            {
                return playerNum;
            }
        }
        this.topFreeSlots[column]--;
        return 0;
    }

    boolean hasWon(int column, int row)
    {
        return (
                this.hHasWon(column, row)          //   ←   (-)     →
                || this.vHasWon(column, row)       //   ↑   (|)     ↓
                || this.fwdDiagHasWon(column, row) //   ←↓  (/)     ↑→
                || this.bckDiagHasWon(column, row) //   ←↑  (\)     ↓→
        );
    }

    boolean hasNeighbors(int column, int row)
    {
        return false;
    }

    int play()
    {
        return 1; // return winning player number
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

        Color getColor()
        {
            return this.color;
        }

        void setColor(Color color)
        {
            this.color = color;
        }

        Optional<Color> getNeighbor(Direction direction)
        {
            return Optional.ofNullable(neighbors.get(direction));
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
