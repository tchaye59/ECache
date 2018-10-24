/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.masternode;

import java.util.ArrayList;
import java.util.List;
import net.almightshell.ecache.common.ECacheConstants;
import net.almightshell.ecache.common.Node;

/**
 *
 * @author Shell
 */
public class ECacheMaster {
    private static int port =  ECacheConstants.DEFAULT_PORT;
    private static int globalDepth;
    
    private List<Node> directory = new ArrayList<>();
     private List<Node> pendingNodes = new ArrayList<>();
    
    public void start(){
    
    } 
    
    public void stop(){
    
    } 

    public static void setPort(int port) {
        ECacheMaster.port = port;
    }

    public static int getPort() {
        return port;
    }
    
}
