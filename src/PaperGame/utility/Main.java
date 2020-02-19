package PaperGame.utility;

import PaperGame.gui.GUI;
import PaperGame.networking.DMServer;
import PaperGame.networking.PlayerClient;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Initial Threads
        ThreadBridge tBridge = new ThreadBridge(
                new Thread(new GUI()), new Thread(new DMServer()), new Thread(new PlayerClient())
        );

        Scanner scanner = new Scanner(System.in);
        System.out.print("Server [Y/N]: ");
        String ans = scanner.next();

        tBridge.init();
    }
}