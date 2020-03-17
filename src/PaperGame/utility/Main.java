package PaperGame.utility;

import PaperGame.entities.Item;
import PaperGame.entities.Weapon;
import PaperGame.gui.GUI;
import PaperGame.networking.DMServer;
import PaperGame.networking.PlayerClient;

import java.io.File;

public class Main {
    public static void main(String[] args) {

        // Construct Threads wrapper class
        ThreadBridge tBridge = new ThreadBridge(
                new Thread(new GUI()), new Thread(new DMServer()), new Thread(new PlayerClient())
        );
        checkUserID();
        tBridge.init();
    }

    /**
     * Checks if the UID directory is empty, if it is empty, this method sets a flag in ThreadBridge which will change
     * the GUI path to run in such a way
     */
    public static void checkUserID(){
        // Confirm this user has a UserID
        File directory = new File(System.getProperty("user.dir") +"/src/PaperGame/res" +
                "/UID");
        if (directory.isDirectory()) {
            if (directory.list().length == 0) {
                ThreadBridge.noUID();
            }
        }
    }


    /**
     * Generate the classes starter items
     */
    public static void generateStarterItems(){
        Weapon woodenBowAndArrow = new Weapon((short)0, (short)1, (short)0, (short)0, 2,
                "Wooden Bow & Arrow", Item.ROGUE, (short)7);
        Weapon ironSword = new Weapon((short)1, (short)0, (short)0, (short)0, 4, "Iron Sword",
                Item.KNIGHT, (short)1);
        Weapon clubAndWoodenShield = new Weapon((short)0, (short)0, (short)0, (short)1, (short)4,
                "Club & Wooden Shield", Item.KNIGHT, (short)1);
        Weapon basicElementalTome = new Weapon((short)0, (short)1, (short)0, (short)0, 1,
                "Basic Elemental Tome", Item.WIZARD, (short)4);

        SaveLoad.writeItemToFile(woodenBowAndArrow);
        SaveLoad.writeItemToFile(ironSword);
        SaveLoad.writeItemToFile(clubAndWoodenShield);
        SaveLoad.writeItemToFile(basicElementalTome);
    }
}