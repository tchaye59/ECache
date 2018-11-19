/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.common;

/**
 *
 * @author Shell Writable
 */
public abstract class Node {

    private String address;
    private int port;

    public Node(String address, int port) {
        this.address = address;
        this.port = port;
    }

//    @Override
//    public void write(DataOutput out) throws IOException {
//        Text.writeString(out, address);
//        out.writeInt(port);
//    }
//
//    @Override
//    public void readFields(DataInput in) throws IOException {
//        address = Text.readString(in);
//        port = in.readInt();
//    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Node{" + "address=" + address + ", port=" + port + '}';
    }

}
