/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.slavenode;

import java.io.IOException;
import java.util.Scanner;
import net.almightshell.ecache.common.utils.ECacheConstants;
import net.almightshell.ecache.common.utils.ECacheUtil;

/**
 *
 * @author Shell
 */
public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        String cmd = "";

        System.err.println("RAM : " + ECacheUtil.format(Runtime.getRuntime().maxMemory(), 2));
        
        printHelp();
        do {
            cmd = sc.nextLine().trim();

            if (cmd.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }
            if (cmd.equalsIgnoreCase("start")) {
                try {
                    ECacheSlave.start();
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
                continue;
            }
            if (cmd.equalsIgnoreCase("stop")) {
                try {
                    ECacheSlave.stop();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                continue;
            }
            if (cmd.startsWith("port")) {
                try {
                    ECacheSlave.setPort(Integer.valueOf(cmd.split(" ")[1].trim()));
                } catch (Exception e) {
                    System.err.println("Error : Invalide value");
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
        System.out.println("    start       : to start the Slave");
        System.out.println("    stop        : to stop the Slave");
        System.out.println("    port VALUE  : to set the port number. By default the port " + ECacheConstants.DEFAULT_PORT + " is used");
        System.out.println("    help        : to show the usage informations");
        System.out.println("    exit        : to quit\n");
    }

    private static void exit() {
        System.exit(0);
    }
}
