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
    
    public void registerSlave();
    
    public void getClientMetadata();
    
    public void getGlobalDepth();
    
    public void doubleDirectory();
}
