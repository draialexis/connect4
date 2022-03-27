package com.alexisdrai.connect4;

public class Main
{
    public static void main(String[] args)
    {
        C4Board board = new C4Board();
        int     winner;

        while (true)
        {
            winner = board.play();
            if (winner != 0)
            {
                System.out.println("Player " + winner + "wins!");
                return;
            }
        }
    }
}
