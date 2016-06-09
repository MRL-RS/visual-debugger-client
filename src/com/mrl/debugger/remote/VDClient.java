package com.mrl.debugger.remote;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created on 6/4/2016.
 *
 * @author Pooya Deldar Gohardani
 */
public class VDClient {

    private static ViewerGateway viewerGateway;
    private static String host = "127.0.0.1";
    private static int port = 1099;
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static boolean enabled = false;


    public static synchronized void init(String host, int port) {
        if (viewerGateway == null) {
            try {
                VDClient.host = host;
                VDClient.port = port;
                Registry registry = LocateRegistry.getRegistry(host, port);
                viewerGateway = (ViewerGateway) registry.lookup("com.mrl.debugger.remote.ViewerGateway");
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        VDClient.enabled = enabled;
    }

    public static void init() {
        init(host, port);
    }

    public static void draw(int agentId, String layerTag, Serializable data) {
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

    public static void drawAsync(int agentId, String layerTag, Serializable data) {
        if (!enabled) {
            return;
        }
        executorService.execute(() -> draw(agentId, layerTag, data));
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) VDClient.executorService;
        System.out.println(threadPoolExecutor.getLargestPoolSize() + " : " + threadPoolExecutor.getPoolSize());
    }

    public static void shutdown() {
        executorService.shutdown();
    }
}
