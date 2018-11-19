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
                    ex.printStackTrace();
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
            if (cmd.startsWith("master-adress")) {
                try {
                    ECacheSlave.setMasterAdress(cmd.split(" ")[1].trim());
                } catch (Exception e) {
                    System.err.println("Error : Invalide value");
                }
                continue;
            }
            if (cmd.startsWith("master-port")) {
                try {
                    ECacheSlave.setMasterPort(Integer.valueOf(cmd.split(" ")[1].trim()));
                } catch (Exception e) {
                    System.err.println("Error : Invalide value");
                }
                continue;
            }
            if (cmd.startsWith("capacity")) {
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
        System.out.println("    capacity <VALUE>       : to set the capacity of the salve");
        System.out.println("    port <VALUE>  : to set the slave port number. By default the port " + ECacheConstants.DEFAULT_PORT + " is used");
        System.out.println("    master-adress <VALUE>       : to set master adress");
        System.out.println("    master-port <VALUE>       : to set master port");
        System.out.println("    start       : to start the Slave");
        System.out.println("    stop        : to stop the Slave");
        System.out.println("    help        : to show the usage informations");
        System.out.println("    exit        : to quit\n");
    }

    private static void exit() {
        System.exit(0);
    }
}
