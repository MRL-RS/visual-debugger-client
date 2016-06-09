package com.mrl.debugger.remote;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Mahdi
 */
public interface ViewerGateway extends Remote {

    void draw(Integer agentId, String layerTag, int dataKey, Serializable data) throws RemoteException;

}
