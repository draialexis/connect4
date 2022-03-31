package com.alexisdrai.connect4;

public class Main
{
    public static void main(String[] args)
    {
        C4Board board = new C4Board();
        boolean isOver = false;
        while (!isOver)
        {
            isOver = board.playTurn();
        }
        System.out.println("Thanks for playing!");
    }
}
