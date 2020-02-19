package PaperGame.utility;

import PaperGame.entities.*;
import PaperGame.networking.DMServer;
import PaperGame.networking.UserID;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadBridge {
    private final String CONSUMABLE = "Consumable", WEAPON = "Weapon", ARMOR ="Armor", COMBAT_MAP = "Combat Map",
            CREATURE = "Creature", CHAMPION = "Champion", INVENTORY = "Inventory";

    private Thread gui;
    private Thread server;
    private Thread client;
    private static boolean serverFlag = false;
    private static boolean clientFlag = false;
    private static boolean guiOn      = true;



    public ThreadBridge(Thread gui, Thread server, Thread client){
        this.gui = gui;
        this.server = server;
        this.client = client;
    }


    public void init(){

        while(guiOn) {
            if (serverFlag) {
                server.start();
                //clientRole()
            }

            if (clientFlag) {
                client.start();
                //serverRole();
            }
        }
    }

    /**
     * Start GUI thread
     */
    public void startGui(){ gui.start(); }


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
    public static void guiOff(){ guiOn = false;}


    /**
     * Called when user chooses DM role
     */
    public static void serverOn(){ serverFlag = true; }


    /**
     * Called when user chooses Player role
     */
    public static void clientOn(){ clientFlag = true; }
}
