package edu.bu.jgram.server;

import edu.bu.jgram.server.assessment.*;
import edu.bu.jgram.server.security.JWT;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Represents the main entry point for JGRAM application
 */
public class Launcher {

    private static final Logger LOGGER = Logger.getInstance();

    public static void main(String... pArgs) {
        //PostCondition 1: Accept User input for action
        //PostCondition 2: Execute task based on selection
        try {
            LOGGER.info("Welcome to Application JGRAM");
            System.out.println("\n---------------------------------[ INPUT ]-------------------------------------\n");
            String action = prompt("Select Task : " +
                                                            "\n\t 1 : New Document Test " +
                                                            "\n\t 2 : Generate Grade" +
                                                            "\n\t 3 : Tamper Test" +
                                                            "\n\t\t (Example 1): ");
            String secret = prompt("Enter secret (Example mysecret): ");
            LOGGER.info("Save this secret somewhere safe, you will require it during tamper test");

            String documentStorePath = prompt("Enter absolute path to directory containing assignment document(s) (Example /sample/assignments): ");
            System.out.println("\n-------------------------------------------------------------------------------\n");

            switch (action) {
                case "1":
                    Task.newDocumentTestTask(documentStorePath);
                    break;
                case "2":
                    Task.evaluationTask(secret, documentStorePath);
                    break;
                case "3":
                    Task.tamperTestTask(secret, documentStorePath);
                    break;
                default:
                    LOGGER.warn("Invalid task selection");
            }

        } catch (IllegalArgumentException iae){
            LOGGER.fatal("Exception occur", iae);
        }
        finally {
            LOGGER.info("Goodbye...");
        }
    }

    /**
     * Prompts user for input, and retrieves user provided value as string.
     *
     * @param pMessage message to be printed while prompting user for input
     */
    private static String prompt(String pMessage) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(pMessage);
        String value = scanner.next();
        return value;
    }
}