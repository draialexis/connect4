package com.alexisdrai.connect4;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static com.alexisdrai.connect4.C4Board.scanner;

public class Main
{
    public static void main(String[] args)
    {
        System.out.println("Welcome to my Connect4 clone!");

        char                 input   = 0;
        ArrayList<Character> options = new ArrayList<>();
        options.add('n');
        options.add('l');
        options.add('q');
        while (!options.contains(input))
        {
            System.out.println("n...: new game\n" +
                               "l...: load game\n" +
                               "q...: quit");
            if (scanner.hasNext())
            {
                input = scanner.next().charAt(0);
                scanner.nextLine(); // purge scanner's buffer, including the leftover '\n'
            }
        }

        C4Board board;

        switch (input)
        {
            case ('l') -> {
                System.out.println("loading game");
                Path path = Paths.get("/data/save.txt");
                board = new C4Board(path);
            }
            case ('q') -> {
                System.out.println("Thanks, goodbye!");
                return;
            }
            default -> {
                System.out.println("chose new game");
                board = new C4Board();
            }
        }

        System.out.println("During the game:\n" +
                           "s...: quicksave\n" +
                           "l...: quickload\n" +
                           "q...: quit");
        while (!board.isOver())
        {
            board.playTurn();
        }
        System.out.println("Thanks for playing!");
    }
}
