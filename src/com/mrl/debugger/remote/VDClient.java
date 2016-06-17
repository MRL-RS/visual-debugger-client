package com.mrl.debugger.remote;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 6/4/2016.
 *
 * @author Pooya Deldar Gohardani
 */
public final class VDClient {
    public static final String DEFAULT_HOST_ADDRESS = "127.0.0.1";
    public static final int DEFAULT_PORT_NUMBER = 1099;

    private ViewerGateway viewerGateway;
    private ExecutorService executorService;
    private boolean enabled = true;
    private static VDClient instance;

    private VDClient() {
        executorService = Executors.newCachedThreadPool();
    }

    public synchronized static VDClient getInstance() {
        if (instance == null) {
            instance = new VDClient();
        }
        return instance;
    }


    public synchronized void init(String host, int port) {
        if (viewerGateway == null) {
            try {
                Registry registry = LocateRegistry.getRegistry(host, port);
                viewerGateway = (ViewerGateway) registry.lookup("com.mrl.debugger.remote.ViewerGateway");
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void init() {
        init(DEFAULT_HOST_ADDRESS, DEFAULT_PORT_NUMBER);
    }

    public void draw(int agentId, String layerTag, Serializable data) {
        if (!enabled) {
            return;
        }
        if (viewerGateway == null) {
            throw new RuntimeException("VDClinet is not initialized yet.");
        }
        try {
            viewerGateway.draw(agentId, layerTag, 0, data);
        } catch (RemoteException e) {
            viewerGateway = null;
            init();
        }
    }

    public void drawAsync(int agentId, String layerTag, Serializable data) {
        if (!enabled) {
            return;
        }
        executorService.execute(() -> draw(agentId, layerTag, data));
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
