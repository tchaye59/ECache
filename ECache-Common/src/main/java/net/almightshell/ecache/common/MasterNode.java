/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.common;

/**
 *
 * @author Shell
 */
public class MasterNode extends Node{
    private int globalDepth;

    public MasterNode(String address, int port) {
        super(address, port);
    }
}
