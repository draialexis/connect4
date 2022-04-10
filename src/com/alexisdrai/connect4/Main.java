package com.alexisdrai.connect4;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.alexisdrai.connect4.C4Game.*;
import static com.alexisdrai.util.Misc.*;

public class Main
{
    public static final Path PATH = Paths.get(".").resolve("save.txt");

    public static void main(String[] args)
    {
        System.out.println("Welcome to my Connect4 prototype!");

        char   input = 0;
        C4Game board = null;

        while (!(input == 'n' || input == 'l' || input == 'q'))
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

        switch (input)
        {
            case ('n') -> board = new C4Game();
            case ('l') -> {
                try
                {
                    board = new C4Game(PATH);
                } catch (Exception ignored)
                {
                    System.out.println(ANSI_PURPLE +
                                       "could not load from " + PATH + ANSI_CYAN +
                                       "\ncreating new game instead" +
                                       ANSI_RESET);
                    board = new C4Game();
                }
            }
            case ('q') -> {
                System.out.println("Thanks, goodbye!");
                return;
            }
        }

        System.out.println("During the game:\n" +
                           "s...: save\n" +
                           "l...: load\n" +
                           "q...: quit");

        while (!board.isOver())
        {
            while (board.isEmpty() && board.getCurrentPlayer().getColor() != board.getPlayers()[0].getColor())
            {
                board.switchPlayer();
            }
            C4Game.C4Player player = board.getCurrentPlayer();
            board.displayBoard();
            int chosenMove = player.chooseMove();
            if (chosenMove < 0)
            {
                switch (chosenMove)
                {
                    case (SAVE_CODE) -> {
                        try
                        {
                            board.save(PATH);
                        } catch (Exception ignored)
                        {
                            System.out.println(ANSI_PURPLE +
                                               "could not save to " + PATH + " \n" +
                                               ANSI_CYAN +
                                               "please ensure path is valid and/or create " + PATH +
                                               ANSI_RESET);
                        }
                    }
                    case (LOAD_CODE) -> {
                        try
                        {
                            board = new C4Game(PATH);
                        } catch (Exception e)
                        {
                            System.out.println(ANSI_RED + e);
                            System.out.println(ANSI_PURPLE + "could not load from " + PATH + ANSI_RESET);
                        }
                    }
                    case (QUIT_CODE) -> {
                        System.out.println("Thanks for playing, goodbye!");
                        return;
                    }
                    default -> throw new RuntimeException("chooseMove failed to return a valid signal or column index");
                }
            }
            else // player chose a move above 0
            {
                board.registerMove(chosenMove);

                if (board.isOver())
                {
                    System.out.println("~~~~~~~~~~~~~~ Game over ~~~~~~~~~~~~~~");
                    if (board.isWon())
                    {
                        System.out.println("Winner: " + player.getColorfulName());
                    }
                    board.displayBoard();
                    System.out.println("Play again?\n" +
                                       "y...: yes\n" +
                                       "l...: reload\n" +
                                       "else: quit");
                    input = 0;
                    if (scanner.hasNext())
                    {
                        input = scanner.next().charAt(0);
                        scanner.nextLine(); // purge scanner's buffer, including the leftover '\n'
                    }
                    switch (input)
                    {
                        case ('y') -> board = new C4Game();

                        case ('l') -> {
                            try
                            {
                                board = new C4Game(PATH);
                            } catch (Exception ignored)
                            {
                                System.out.println(ANSI_PURPLE +
                                                   "could not load from " + PATH + ANSI_CYAN +
                                                   "\ncreating new game instead" +
                                                   ANSI_RESET);
                                board = new C4Game();
                            }
                        }
                    }
                }
                if (!board.isOver())
                {
                    board.switchPlayer();
                }
            }
        }
        System.out.println("Thanks for playing!");
    }
}
