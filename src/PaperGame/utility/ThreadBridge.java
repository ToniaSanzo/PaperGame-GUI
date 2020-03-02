package PaperGame.utility;

import PaperGame.entities.*;
//import PaperGame.networking.DMServer;
import PaperGame.networking.UserID;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadBridge {
    private final String CONSUMABLE = "Consumable", WEAPON = "Weapon", ARMOR ="Armor", COMBAT_MAP = "Combat Map",
            CREATURE = "Creature", CHAMPION = "Champion", INVENTORY = "Inventory";

    private Thread gui;
    private Thread server;
    private Thread client;
    private static boolean uidFlag           = false;
    private static boolean serverFlag        = false;
    private static boolean clientFlag        = false;
    private static boolean guiOn             = true;
    private static boolean gameFlag          = false;
    private static Stack<String> userIDs     = new Stack<String>();



    public ThreadBridge(Thread gui, Thread server, Thread client){
        this.gui = gui;
        this.server = server;
        this.client = client;
    }


    public void init(){
        boolean networkOn = false;

        startGui();

        while(!networkOn) {
            try { Thread.sleep(300); } catch (InterruptedException ex){ ex.printStackTrace(); }
            if (serverFlag) {
                System.out.println("Server Started!");
                server.start();
                networkOn = true;
            }

            if (clientFlag) {
                System.out.println("Client Started!");
                client.start();
                networkOn = true;
            }
        }
        System.out.println("init exited");
    }


    /**
     * Start GUI thread
     */
    public void startGui(){
        guiOn = true;
        gui.start();
    }


    /**
     * Start Server thread
     */
    public void startServer(){ server.start(); }


    /**
     * Start Client thread
     */
    public void startClient(){ client.start(); }


    /**
     * Lets ThreadBridge know the GUI was closed
     */
    public static synchronized void guiOff(){ guiOn = false;}


    /**
     * Called when user chooses DM role
     */
    public static synchronized void serverOn(){ serverFlag = true; }


    /**
     * Called when user chooses Player role
     */
    public static synchronized void clientOn(){ clientFlag = true; }


    /**
     * Called when the User does not have a User ID
     */
    public static synchronized void noUID(){ uidFlag = true; }


    /**
     * Called when the User has set up their User ID
     */
    public static synchronized void resetUID(){ uidFlag = false;}


    /**
     * Called when the DM starts the game
     */
    public static synchronized void gameOn(){ gameFlag = true; }


    /**
     * @return True if myUID is missing, otherwise false
     */
    public static synchronized boolean checkUID(){ return uidFlag; }


    /**
     * @return True if DM started the game, otherwise false
     */
    public static synchronized boolean gameStarted(){ return gameFlag; }


    /**
     * @return True if stack is empty otherwise false
     */
    public static synchronized boolean userEmpty(){ return userIDs.empty();}


    /**
     * Push user to stack
     *
     * @param str user's name
     */
    public static synchronized void pushUser(String str){ userIDs.push(str); }


    /**
     * Pop user from stack
     *
     * @return user's name
     */
    public static synchronized String popUser(){ return userIDs.pop(); }


    /**
     * @return Return's the Public IP address of the local machine in String format
     */
    public static ArrayList<String> getIP() {
        ArrayList<String> returnList = new ArrayList<String>();
        String ip;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces

                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    if(ip.substring(ip.length() - 7, ip.length()).compareTo("%wlp3s0") != 0) returnList.add(ip);
                }

            }
        } catch (SocketException e) { throw new RuntimeException(e); }
        return returnList;  // Return the correct IP addresses
    }
}
