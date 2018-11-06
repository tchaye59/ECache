/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.common;

import java.util.Objects;

/**
 *
 * @author Shell
 */
public class SlaveNode extends Node{
    
    private String key = null;

    public SlaveNode(String key,String address, int port) {
        super(address, port);
        this.key = key;
    }
    
    public SlaveNode(String address, int port) {
        super(address, port);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SlaveNode other = (SlaveNode) obj;
        return Objects.equals(this.key, other.key);
    }

    @Override
    public String toString() {
        return "SlaveNode{" + "key=" + key + '}';
    }

}
