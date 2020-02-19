package PaperGame.utility;

import PaperGame.entities.*;
import PaperGame.networking.DMServer;
import PaperGame.networking.UserID;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadBridge {
    private final String CONSUMABLE = "Consumable", WEAPON = "Weapon", ARMOR ="Armor", COMBAT_MAP = "Combat Map",
            CREATURE = "Creature", CHAMPION = "Champion", INVENTORY = "Inventory";

    private final Lock lock = new ReentrantLock();
    private Thread gui;
    private Thread server;
    private Thread client;
    private boolean incomingMessage = false;
    private boolean outgoingMessage = false;



    public ThreadBridge(Thread gui, Thread server, Thread Client){
        this.gui = gui;
        this.server = server;
        this.client = client;
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

}
