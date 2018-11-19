/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.common.eh;

import java.util.Objects;
import net.almightshell.ecache.common.SlaveNode;

/**
 *
 * @author Shell Writable
 */
public class Bucket {

    private SlaveNode slaveNode;

    public Bucket() {
    }

//    @Override
//    public void write(DataOutput out) throws IOException {
//        slaveNode.write(out);
//    }
//
//    @Override
//    public void readFields(DataInput in) throws IOException {
//        slaveNode = new SlaveNode();
//        slaveNode.readFields(in);
//    }
    public void setSlaveNode(SlaveNode slaveNode) {
        this.slaveNode = slaveNode;
    }

    public SlaveNode getSlaveNode() {
        return slaveNode;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.slaveNode);
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
        final Bucket other = (Bucket) obj;
        if (!Objects.equals(this.slaveNode, other.slaveNode)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Bucket{" + "slaveNode=" + slaveNode + '}';
    }

}
