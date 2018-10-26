/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.masternode.services;

/**
 *
 * @author Shell
 */
public interface IMasterNodeServices {
    
    public int registerSlave(int position);
    
    public void getClientMetadata();
    
    public int getGlobalDepth();
    
    public void doubleDirectory();
    
    public void requestSplit();
}
