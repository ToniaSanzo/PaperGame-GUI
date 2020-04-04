package PaperGame.utility;

import PaperGame.entities.Inventory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Stack;


public class ThreadBridge {
    private final String CONSUMABLE = "Consumable", WEAPON = "Weapon", ARMOR ="Armor", COMBAT_MAP = "Combat Map",
            CREATURE = "Creature", CHAMPION = "Champion", INVENTORY = "Inventory";

    // Threads
    private Thread gui;
    private Thread server;
    private Thread client;

    // Flags
    private static boolean uidFlag            = false;           // Sets if myUID has not been set yet
    private static boolean serverFlag         = false;           // Sets when User chooses DM option
    private static boolean clientFlag         = false;           // Sets when User chooses Player option
    private static boolean guiOn              = false;           // True while GUI is running
    private static boolean gameFlag           = false;           // Sets when the DM starts the game
    private static boolean ipFlag             = false;           // Sets when GUI receives a IP address
    private static boolean partyFlag          = false;           // Sets when Client successfully joins party
    private static boolean joinFail           = false;           // Sets when Client fails party join
    private static boolean attemptedPartyJoin = false;           // Sets when Client attempts to join party
    private static boolean tradeOfferFlag     = false;           // Sets when User has a trade offer
    private static boolean tradeReceiveFlag   = false;           // Sets when User has receives a trade offer
    private static boolean championNameFlag   = false;           // Sets when User knows what champion they are playing

    // Data communicated internally between the Network and GUI
    private static String ipAddress           = null;            // IP Address received from GUI
    private static String championName        = null;            // Current user's champion name
    private static Stack<String> userIDs      = new Stack<String>();  // DM's party
    private static Inventory offerInventory   = new Inventory(); // Item's being offered in trade
    private static Inventory receiveInventory = new Inventory(); // Item's being received in trade




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
     * true  = GUI's on
     * false = GUI's off
     *
     * @return boolean
     */
    public static synchronized boolean isGuiOn(){ return guiOn; }


    /**
     * @return boolean, true if Client attempted to join party
     */
    public static boolean isAttemptedPartyJoin() { return attemptedPartyJoin; }


    /**
     * @param attemptedPartyJoin boolean, true if attempted to join party
     */
    public static void setAttemptedPartyJoin(boolean attemptedPartyJoin) {
        ThreadBridge.attemptedPartyJoin = attemptedPartyJoin;
    }


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
     * Has an IP addressed been entered by the Player
     *
     * @return ipFlag value
     */
    public static synchronized boolean checkIP(){ return ipFlag; }


    /**
     * Set the ipFlag to the parameter value
     *
     * @param ipFlag boolean value
     */
    public static synchronized void setIPFlag(boolean ipFlag){ ThreadBridge.ipFlag = ipFlag; }


    /**
     * Get the IP address the Player has entered, reset the IP address flag
     *
     * @return ipAddress entered by Player
     */
    public static synchronized String getIpAddress(){
        ipFlag = false;
        return ipAddress;
    }


    /**
     * Set an IP address, set the IP address flag to true
     *
     * @param ipAddress entered by Player
     */
    public static synchronized void ipReceived(String ipAddress){
        ThreadBridge.ipAddress = ipAddress;
        ipFlag = true;
    }


    /**
     * Set the partyFlag to the parameter
     *
     * @param partyFlag boolean
     */
    public static synchronized void setPartyFlag(boolean partyFlag){ ThreadBridge.partyFlag = partyFlag; }


    /**
     * Return status of the Player joining the server
     *
     * @return boolean
     */
    public static synchronized boolean joinFailed(){ return joinFail; }


    /**
     * Set whether joining the room was successful or not
     *
     * @param joinFail boolean
     */
    public static synchronized void setJoinFail(boolean joinFail){ ThreadBridge.joinFail = joinFail; }


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


    /**
     * Check tradeOfferFlag's value
     *
     * @return boolean value
     */
    public static synchronized boolean checkOfferFlag(){ return tradeOfferFlag; }


    /**
     * Return current offerInventory, reset the tradeOfferFlag. If tradeOfferFlag is false, will throw a
     * NullPointerException instead
     *
     * @return offerInventory, or throws a NullPointerException
     */
    public static synchronized Inventory getOfferInventory(){
        if(tradeOfferFlag == false){
            throw new NullPointerException();
        } else {
            tradeOfferFlag = false;
            return offerInventory;
        }
    }


    /**
     * Update the offerInventory, set the tradeOfferFlag
     *
     * @param offerInventory Inventory being offered by the user
     */
    public static synchronized void setOfferInventory(Inventory offerInventory){
        ThreadBridge.offerInventory = offerInventory;
        tradeOfferFlag = true;
    }


    /**
     * Set tradeOfferFlag to true, used when a Write Request fails in the PlayerClient Class
     */
    public static synchronized void resetTradeOfferFlag(){ tradeOfferFlag = true; }


    /**
     * Check tradeReceiveFlag's value
     *
     * @return boolean value
     */
    public static synchronized boolean checkReceiveFlag(){ return tradeReceiveFlag; }


    /**
     * Return current receiveInventory, reset the tradeReceiveFlag. If tradeReceiveFlag is false, will throw a
     * NullPointerException instead
     *
     * @return receiveInventory, or throws a NullPointerException
     */
    public static synchronized Inventory getReceiveInventory(){
        if(tradeReceiveFlag == false){
            throw new NullPointerException();
        } else {
            tradeReceiveFlag = false;
            return receiveInventory;
        }
    }


    /**
     * Update the receiveInventory, set the tradeReceiveFlag
     *
     * @param receiveInventory Inventory being received by the user
     */
    public static synchronized void setReceiveInventory(Inventory receiveInventory){
        ThreadBridge.receiveInventory = offerInventory;
        tradeReceiveFlag = true;
    }


    /**
     * Check the championNameFlag
     *
     * @return boolean
     */
    public static synchronized boolean checkChampionName(){ return championNameFlag; }


    /**
     * Get championName
     *
     * @return champion name
     */
    public static synchronized String getChampionName(){ return championName; }


    /**
     * Set championName, set championName flag to true
     *
     * @param championName champion name
     */
    public static synchronized void setChampionName(String championName){
        ThreadBridge.championName = championName;
        championNameFlag = true;
    }
}
