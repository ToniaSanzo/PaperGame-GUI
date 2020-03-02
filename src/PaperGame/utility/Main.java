package PaperGame.utility;

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
                "/UID/");
        if (directory.isDirectory()) {
            String[] files = directory.list();
            if (files.length > 0) {
                ThreadBridge.noUID();
            }
        }
    }
}