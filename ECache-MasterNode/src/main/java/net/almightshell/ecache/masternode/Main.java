/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.masternode;

import java.io.IOException;
import java.util.Scanner;
import net.almightshell.ecache.common.utils.ECacheConstants;

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

            if (cmd.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }
            if (cmd.equalsIgnoreCase("start")) {
                try {
                    ECacheMaster.start();
                } catch (IOException ex) {
                    System.err.println("Start failed : " + ex.getMessage());
                }
                continue;
            }
            if (cmd.equalsIgnoreCase("stop")) {
                try {
                    ECacheMaster.stop();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                continue;
            }
            if (cmd.startsWith("port")) {
                try {
                    ECacheMaster.setPort(Integer.valueOf(cmd.split(" ")[1].trim()));
                } catch (Exception e) {
                    System.err.println("Error : Invalide value");
                }
                continue;
            }

            if (cmd.startsWith("info")) {
                try {
                    ECacheMaster.info();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                continue;
            }

            if (cmd.equalsIgnoreCase("exit")) {
                exit();
                continue;
            }

            printHelp();
        } while (true);
    }

    public static void printHelp() {
        System.out.println("Usage :");
        System.out.println("    start       : to start the Master");
        System.out.println("    stop        : to stop the Master");
        System.out.println("    info        : to show all information");
        System.out.println("    port VALUE  : to set the port number. By default the port " + ECacheConstants.DEFAULT_PORT + " is used");
        System.out.println("    help        : to show the usage informations");
        System.out.println("    exit        : to quit\n");
    }

    private static void exit() {
        System.exit(0);
    }
}
