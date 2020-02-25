package PaperGame.utility;

import PaperGame.gui.GUI;
import PaperGame.networking.DMServer;
import PaperGame.networking.PlayerClient;

public class Main {
    public static void main(String[] args) {
        // Contruct Threads wrapper class
        ThreadBridge tBridge = new ThreadBridge(
                new Thread(new GUI()), new Thread(new DMServer()), new Thread(new PlayerClient())
        );

        tBridge.init();
    }
}