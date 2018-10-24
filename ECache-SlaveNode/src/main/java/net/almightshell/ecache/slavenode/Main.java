/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.slavenode;

import java.util.Scanner;

/**
 *
 * @author Shell
 */
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String cmd = "";

        printHelp();
        do {
            cmd = sc.nextLine().trim();

            switch (cmd) {
                case "help":
                    printHelp();
                    break;
                case "start":
                    printHelp();
                    break;
                case "stop":
                    printHelp();
                    break;
                case "exit":
                    exit();
                    break;
                default:
                    printHelp();
            }

        } while (true);
    }

    public static void printHelp() {
        System.out.println("Usage :");
        System.out.println("start : to start the slave");
        System.out.println("stop : to stop the slave");
        System.out.println("help : to show the usage informations");
        System.out.println("exit : to quit");
    }

    private static void exit() {
        System.exit(0);
    }
}
