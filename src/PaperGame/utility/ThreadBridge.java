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
    private boolean incomingMessage = false;
    private boolean outgoingMessage = false;

    /**
     * Default Constructor
     */
    public ThreadBridge(){}

    public boolean writeMessage(UserID uID, TransferredObject object){
        String type = object.getType();  // Determine the class this object belongs too
        boolean success = false;

        if(lock.tryLock()) {
            try {
                switch (type) {
                    case ARMOR:
                        Armor tArm = (Armor) object;
                        try {
                            DMServer.writeObject(uID, tArm);
                        } catch (Exception e) {}
                        success = true;
                    case CHAMPION:
                        Champion tChp = (Champion) object;
                        try {
                            DMServer.writeObject(uID, tChp);
                        } catch (Exception e) {}
                        success = true;
                    case COMBAT_MAP:
                        CombatMap tCmp = (CombatMap) object;
                        try {
                            DMServer.writeObject(uID, tCmp);
                        } catch (Exception e) {}
                        success = true;
                    case CONSUMABLE:
                        Consumable tCns = (Consumable) object;
                        try {
                            DMServer.writeObject(uID, tCns);
                        } catch (Exception e) {}
                        success = true;
                    case CREATURE:
                        Creature tCtr = (Creature) object;
                        try {
                            DMServer.writeObject(uID, tCtr);
                        } catch (Exception e) {}
                        success = true;
                    case INVENTORY:
                        Inventory tInv = (Inventory)object;
                        try{
                            DMServer.writeObject(uID, tInv);
                        } catch(Exception e) {}
                        success = true;
                    case WEAPON:
                        Weapon tWpn = (Weapon)object;
                        try{
                            DMServer.writeObject(uID, tWpn);
                        } catch(Exception e) {}
                        break;
                    default:
                        break;
                }
            } finally { lock.unlock(); }
        }
        return success;
    }

}
