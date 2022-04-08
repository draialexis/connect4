package com.alexisdrai.connect4;

import com.alexisdrai.util.Misc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.alexisdrai.connect4.C4Board.*;
import static com.alexisdrai.util.Misc.*;

public class Main
{
    public static final Path PATH = Paths.get(".").resolve("save.txt");

    public static void main(String[] args) throws IOException
    {
        System.out.println("Welcome to my Connect4 prototype!");

        char    input = 0;
        C4Board board = null;

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
            case ('n') -> board = new C4Board();
            case ('l') -> {
                try
                {
                    board = new C4Board(PATH);
                } catch (Exception ignored)
                {
                    System.out.println(ANSI_CYAN +
                                       "could not find " +
                                       PATH +
                                       "\ncreating new game instead" +
                                       ANSI_RESET);
                    board = new C4Board();
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
            C4Board.C4Player player = board.getCurrentPlayer();
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
                        } catch (Exception e)
                        {
                            System.out.println("could not find nor create file\n" +
                                               ANSI_CYAN +
                                               "please ensure path is valid and/or create save.txt in root folder" +
                                               ANSI_RESET);
                            throw e;
                        }
                    }
                    case (LOAD_CODE) -> {
                        try
                        {
                            board = new C4Board(PATH);
                        } catch (Exception ignored)
                        {
                            System.out.println(ANSI_CYAN + "could not find " + PATH + ANSI_RESET);
                        }
                    }
                    case (QUIT_CODE) -> {
                        System.out.println("Thanks for playing, goodbye!");
                        return;
                    }
                    default -> throw new RuntimeException("chooseMove failed to return a valid signal or column index");
                }
            }
            /*else*/
            if (chosenMove >= 0)
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
                }
                board.switchPlayer();
            }
        }
        System.out.println("Thanks for playing!");
    }
    // TODO replace clone() calls to better read-only copies
}
