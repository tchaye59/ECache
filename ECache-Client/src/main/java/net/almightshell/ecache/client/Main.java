/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.client;

import java.io.Serializable;
import net.almightshell.ecache.common.utils.ECacheConstants;
import net.almightshell.ecache.common.utils.ECacheUtil;

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

        public Record(int x, int y) {
            this.x = x;
            this.y = y;
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
        ECacheClient client = new ECacheClient("test", ECacheConstants.DEFAULT_PORT, "localhost", true,true);
        for (int i = 0; i < 90; i++) {
            client.put(i + 999999999, ECacheUtil.toObjectStream(new Record(8, 6)));
//            client.remove(i);
        }
//
        for (int i = 0; i < 100; i++) {
            System.out.println((i + 999999999) + " ->" + ECacheUtil.toObject(client.get(i + 999999999)));
        }
    }

}
