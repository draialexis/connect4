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
        C4Game game  = null;

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
            case ('n') -> game = new C4Game();
            case ('l') -> {
                try
                {
                    game = new C4Game(PATH);
                } catch (Exception ignored)
                {
                    System.out.println(ANSI_PURPLE +
                                       "could not load from " + PATH + ANSI_CYAN +
                                       "\ncreating new game instead" +
                                       ANSI_RESET);
                    game = new C4Game();
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

        while (!game.isOver())
        {
            C4Game.C4Player player = game.getCurrentPlayer();
            game.displayBoard();
            int chosenMove = player.chooseMove();
            if (chosenMove < 0)
            {
                switch (chosenMove)
                {
                    case (SAVE_CODE) -> {
                        try
                        {
                            game.save(PATH);
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
                            game = new C4Game(PATH);
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
                game.registerMove(chosenMove);
                input = 0;

                if (game.isOver())
                {
                    System.out.println("~~~~~~~~~~~~~~ Game over ~~~~~~~~~~~~~~");
                    if (game.isWon())
                    {
                        System.out.println("Winner: " + player.getColorfulName());
                    }
                    game.displayBoard();
                    System.out.println("Play again?\n" +
                                       "y...: yes\n" +
                                       "l...: reload\n" +
                                       "else: quit");
                    if (scanner.hasNext())
                    {
                        input = scanner.next().charAt(0);
                        scanner.nextLine(); // purge scanner's buffer, including the leftover '\n'
                    }
                    switch (input)
                    {
                        case ('y') -> {
                            game = new C4Game();
                            input = 0;
                        }

                        case ('l') -> {
                            try
                            {
                                game = new C4Game(PATH);
                            } catch (Exception ignored)
                            {
                                System.out.println(ANSI_PURPLE +
                                                   "could not load from " + PATH + ANSI_CYAN +
                                                   "\ncreating new game instead" +
                                                   ANSI_RESET);
                                game = new C4Game();
                            }
                        }
                    }
                }
                if (!game.isOver() && input != 'l')
                {
                    game.switchPlayer();
                }
            }
        }
        System.out.println("Thanks for playing!");
    }
}
