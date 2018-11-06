/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.client;

import java.io.Serializable;
import net.almightshell.ecache.common.utils.ECacheConstants;

/**
 *
 * @author Shell
 */
public class Main {

    static class Record implements Serializable {

        int x = 10;
        int y = 20;

        public Record() {
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return "Record{" + "x=" + x + ", y=" + y + '}';
        }

    }

    public static void main(String[] args) throws Exception {
        ECacheClient<Integer, Record> client = new ECacheClient<>("test", ECacheConstants.DEFAULT_PORT, "localhost", false);
        for (int i = 0; i < 1000; i++) {
            client.put(i, new Record());
        }

        for (int i = 0; i < 1000; i++) {
            System.out.println(i+" ->"+client.get(i));
        }
    }

}
