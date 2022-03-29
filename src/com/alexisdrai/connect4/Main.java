package com.alexisdrai.connect4;

public class Main
{
    public static void main(String[] args)
    {
        C4Board board = new C4Board();

        while (true)
        {
            board.playTurn();
        }
    }
}
