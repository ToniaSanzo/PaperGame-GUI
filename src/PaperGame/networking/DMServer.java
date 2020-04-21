package PaperGame.networking;

import PaperGame.entities.*;
import PaperGame.utility.ThreadBridge;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ThreadLocalRandom;

public class DMServer implements Runnable
{
    private static DatagramSocket serverSocket;
    private static ArrayList<UserID> userIDs;

    // Opcode's, used in packet header's to distinguish the purpose of a packet
    private static final byte RRQ         = 0;      // Read Request
    private static final byte WRQ         = 1;      // Write Request
    private static final byte ACK         = 2;      // Acknowledge
    private static final byte DATA        = 3;      // Data Packet

    // Object Type Code's, used in packet header's to distinguish the object type of a given piece of Data
    private static final byte CMAP        = 4;      // Combat Map Object
    private static final byte WPN         = 5;      // Weapon Object
    private static final byte AMR         = 6;      // Armor Object
    private static final byte CNSM        = 7;      // Consumable Object
    private static final byte CHMP        = 8;      // Champion Object
    private static final byte CRTR        = 9;      // Creature Object
    private static final byte INV         = 10;     // Inventory Object
    private static final byte UID         = 11;     // UserID object

    private static final int WINDOW_SIZE  = 4;      // Window size for Sliding Window Protocol
    private static final int PAYLOAD      = 503;    // Size of the Data payload per packet
    private static final int PAYLOAD_HEAD = 9;      // Index the payload begin's in a data packet


    /**
     * DMServer driver method, this is executed when a DMServer is started
     */
    public void run()
    {
        // Open the server socket
        openSocket(300);

        // Initialize every player's user ID
        partyJoin();

        // Write party information to each party member
        updateParty();

        // Server's network loop
        //serverRun();

        closeSocket(); // Close server socket
    }


    /**
     * Write UserID information to every UserID
     */
    public static void updateParty(){
        // Communicate every UserID to each party member
        for(UserID currID : userIDs){
            for(UserID commID : userIDs){
                // Prevent communicating the current UserID to itself
                if(commID != currID){
                    System.out.println("Attempting to write " + commID.getName() + "-UserID to " + currID.getName() +
                            "-UserID");
                    try {
                        writeObject(currID, commID);
                    } catch(Exception ex){
                        System.out.println("Failed to write " + commID.getName() + "-UserID to " + currID.getName() +
                                "-UserID");
                    }
                }
            }
        }
    }


    /**
     * Method, initializes every userID in the party
     */
    public static void partyJoin(){
        try {
            userIDs = new ArrayList<UserID>();
            UserID uID;

            // While User's are joining the DM's game
            while(!ThreadBridge.gameStarted()){
                // Terminate, if GUI's closed
                if(!ThreadBridge.isGuiOn()){
                    closeSocket();
                    System.exit(0);
                }

                // Listen for a client to join
                try { uID = clientJoin(); } catch (SocketTimeoutException ex){ uID = null; }

                if(uID != null){
                    // Add UserIDs to the Server's UserID list
                    userIDs.add(uID);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Network loop, checks to see if the server needs to write to the client, if not listen to the client,
     * than check to make sure the program's still on
     */
    /*public static void clientRun(){
        TransferredObject transferredObject; // Used

        while(true){

            // If the client has a write request
            if(ThreadBridge.checkOfferFlag()){
                // Attempt to write the offerInventory to the server, If the WRQ fails reset the getOfferInventory
                try{
                    writeObject(serverID, ThreadBridge.getOfferInventory());
                } catch(RuntimeException ex){
                    System.err.println("WRQ failed, will attempt WRQ later");
                    ThreadBridge.resetTradeOfferFlag();
                } catch(Exception ex){
                    ex.printStackTrace();
                }
            }

            // Listen to Server
            try{
                transferredObject = listen();
                if(transferredObject.getType() == Inventory.INVENTORY){
                    ThreadBridge.setReceiveInventory((Inventory)transferredObject);
                }
            } catch(Exception ex){ }

            // Sleep
            try { Thread.sleep(313); } catch(InterruptedException ex){ }

            // Terminate, if GUI's closed
            if (!ThreadBridge.isGuiOn()) { return; }
        }

    }*/


    /**
     * Receive a player join packet and send an ack packet
     *
     * @return Return a User's ID
     * @throws Exception
     */
    public static UserID clientJoin() throws Exception{

        byte[] data = new byte[128];
        char tChar;
        String userName = "";
        int hashCode, index = 1;

        // Receive a Datagram Packet from the client, transfer that data into a byte buffer
        DatagramPacket receivePacket = new DatagramPacket(data, data.length);
        serverSocket.receive(receivePacket);
        ByteBuffer byteBuffer = ByteBuffer.wrap(receivePacket.getData());

        // Retrieve the player's user name and hash code
        while(true) {
            tChar = byteBuffer.getChar(index);
            if(tChar == (char) 0) break;  // End the loop in the event of a delimiter being received
            userName += tChar;
            index += 2;
        }
        index += 3;
        hashCode = byteBuffer.getInt(index);
        byteBuffer.clear();

        UserID userID = new UserID(userName, hashCode, receivePacket.getAddress(), receivePacket.getPort());

        // Create and send Ack packet to client
        data = clientJoinAckArray(hashCode);
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, userID.getIpAddr(), userID.getPort());
        serverSocket.send(sendPacket);

        // Check each userID to see if this is a new user or a championName update to a user
        for(int i = 0; i < userIDs.size(); i++){
            if(hashCode == userIDs.get(i).getHashCode()){
                userIDs.get(i).setChampion(userName);
                // send the userID's to the GUI
                ThreadBridge.pushUser(userIDs.get(i));
                return null;
            }
        }

        return userID;  // Return the client's UserID
    }


    /**
     * Open the server socket, and set the server sockets timeout
     *
     * @param timeout Integer value specifying the socket timeout
     */
    public static void openSocket(int timeout){
        try {
            serverSocket = new DatagramSocket(9876);  // open server socket
            serverSocket.setSoTimeout(timeout);            // set timeout to 300 milliseconds
        } catch(SocketException ex) { ex.printStackTrace(); }
    }


    /**
     * Set server socket's timeout
     *
     * @param timeout Integer specifying the socket timeout
     */
    public static void changeTimeout(int timeout){
        try{ serverSocket.setSoTimeout(timeout); } catch(SocketException ex) { ex.printStackTrace(); }
    }


    /**
     * Close the server socket
     */
    public static void closeSocket(){ serverSocket.close(); }


    /**
     * Create an ack packet when a player joins the group
     *
     * @param hashCode The new player's hash code
     * @return A byte array containing the new player's hash code within a 4 byte-array
     */
    public static byte[] clientJoinAckArray(int hashCode){
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(hashCode);
        return byteBuffer.array();
    }


    /**
     * Write an object to a playerClient Socket, This is done by sending a write request packet to the client, receiving
     * an Ack packet and than sequentially sending the objects data in packets sized to 512 bytes with a 503 byte payload.
     *
     * @param userID UserID of the client
     * @param combatMap Combat Map sent to the client
     * @throws Exception
     */
    public static void writeObject(UserID userID, CombatMap combatMap) throws Exception{
        byte [] data, wrqData = new byte[10], ackData = new byte[10];
        int index, ackPacketNo, objSize, finalBlockNo, block, windowHead = 1;
        boolean ackFail;
        DatagramPacket dataPacket, sendPacket, ackPacket;

        // Convert the Combat Map into a byte array
        byte [] objBytes = CombatMap.convertToBytes(combatMap);
        objSize = objBytes.length;

        // Determine the number of blocks needed to be sent to the client
        if(objBytes.length % 503 == 0) finalBlockNo = objSize / 503;
        else finalBlockNo = (objSize / 503) + 1;

        // Create write request packet:
        // [byte][byte][byte][byte][byte]          [int]         [byte]
        //  -----------------------------------------------------------
        // |  0  | opc |  0  | type|  0  |      object size      |  0  |
        //  -----------------------------------------------------------
        wrqData[1] = WRQ;  // Set the opcode to write request
        wrqData[3] = CMAP;  // Set object type to Combat Map

        // Add the object's length to the write request packet
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(objBytes.length);
        wrqData[5] = buffer.get(0);
        wrqData[6] = buffer.get(1);
        wrqData[7] = buffer.get(2);
        wrqData[8] = buffer.get(3);

        do {
            // Send Write Request to client
            sendPacket = new DatagramPacket(wrqData, wrqData.length, userID.getIpAddr(), userID.getPort());
            serverSocket.send(sendPacket);

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            serverSocket.receive(ackPacket);

            // Confirm the Ack packet
            buffer = ByteBuffer.wrap(ackPacket.getData());
            if (buffer.get(3) == CMAP && buffer.getInt(4) == objSize) ackFail = true;
            else ackFail = false;
        } while(ackFail);  // Resend Write Request, in the event of a failure


        // Send Combat Map to Client
        while(true) {
            // Send window to client
            block = windowHead;
            while (block < WINDOW_SIZE + windowHead && block <= finalBlockNo) {
                // Create the Data packet:
                //  0 1 2 3  4 5 6 7 8    9   10   11   12   13 ...  511
                //  ----------------------------------------------------
                // |0|#|0|#|Packet #|0|DATA|DATA|DATA|DATA|DATA|...|DATA|
                //  ----------------------------------------------------
                //
                // 1) Generate data packet heading
                data = new byte[512];
                data[1] = DATA;
                data[3] = CMAP;
                data[4] = (byte) (block >> 24);
                data[5] = (byte) (block >> 16);
                data[6] = (byte) (block >> 8);
                data[7] = (byte) (block);
                // 2) Generate data packet payload
                index = 9;
                for (int i = (block - 1) * PAYLOAD; i < block * PAYLOAD && i < objSize; i++) {
                    data[index] = objBytes[i];
                    ++index;
                }

                // Send Data Packet
                dataPacket = new DatagramPacket(data, data.length, userID.getIpAddr(), userID.getPort());
                serverSocket.send(dataPacket);

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { serverSocket.receive(ackPacket); } catch (SocketTimeoutException ex) { break; }

                // Check the Ack packet number
                buffer = ByteBuffer.wrap(ackPacket.getData());
                ackPacketNo = buffer.getInt(5);

                // Increment window head, after a successful Ack, end message after final ack is received
                if (ackPacketNo == windowHead) windowHead++;
                if(windowHead > finalBlockNo) return;
            }
        }
    }


    /**
     * Write an object to a playerClient Socket, This is done by sending a write request packet to the client, receiving
     * an Ack packet and than sequentially sending the objects data in packets sized to 512 bytes with a 503 byte payload.
     *
     * @param userID UserID of the client
     * @param weapon Weapon sent to the client
     * @throws Exception
     */
    public static void writeObject(UserID userID, Weapon weapon) throws Exception{
        byte [] data, wrqData = new byte[10], ackData = new byte[10];
        int index, ackPacketNo, objSize, finalBlockNo, block, windowHead = 1;
        boolean ackFail;
        DatagramPacket dataPacket, sendPacket, ackPacket;

        // Convert the Weapon into a byte array
        byte [] objBytes = Weapon.convertToBytes(weapon);
        objSize = objBytes.length;

        // Determine the number of blocks needed to be sent to the client
        if(objBytes.length % 503 == 0) finalBlockNo = objSize / 503;
        else finalBlockNo = (objSize / 503) + 1;

        // Create write request packet:
        // [byte][byte][byte][byte][byte]          [int]         [byte]
        //  -----------------------------------------------------------
        // |  0  | opc |  0  | type|  0  |      object size      |  0  |
        //  -----------------------------------------------------------
        wrqData[1] = WRQ;  // Set the opcode to write request
        wrqData[3] = WPN;  // Set object type to Weapon

        // Add the object's length to the write request packet
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(objBytes.length);
        wrqData[5] = buffer.get(0);
        wrqData[6] = buffer.get(1);
        wrqData[7] = buffer.get(2);
        wrqData[8] = buffer.get(3);

        do {
            // Send Write Request to client
            sendPacket = new DatagramPacket(wrqData, wrqData.length, userID.getIpAddr(), userID.getPort());
            serverSocket.send(sendPacket);

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            serverSocket.receive(ackPacket);

            // Confirm the Ack packet
            buffer = ByteBuffer.wrap(ackPacket.getData());
            if (buffer.get(3) == WPN && buffer.getInt(4) == objSize) ackFail = true;
            else ackFail = false;
        } while(ackFail);  // Resend Write Request, in the event of a failure


        // Send Weapon to Client
        while(true) {
            // Send window to client
            block = windowHead;
            while (block < WINDOW_SIZE + windowHead && block <= finalBlockNo) {
                // Create the Data packet:
                //  0 1 2 3  4 5 6 7 8    9   10   11   12   13 ...  511
                //  ----------------------------------------------------
                // |0|#|0|#|Packet #|0|DATA|DATA|DATA|DATA|DATA|...|DATA|
                //  ----------------------------------------------------
                //
                // 1) Generate data packet heading
                data = new byte[512];
                data[1] = DATA;
                data[3] = WPN;
                data[4] = (byte) (block >> 24);
                data[5] = (byte) (block >> 16);
                data[6] = (byte) (block >> 8);
                data[7] = (byte) (block);
                // 2) Generate data packet payload
                index = 9;
                for (int i = (block - 1) * PAYLOAD; i < block * PAYLOAD && i < objSize; i++) {
                    data[index] = objBytes[i];
                    ++index;
                }

                // Send Data Packet
                dataPacket = new DatagramPacket(data, data.length, userID.getIpAddr(), userID.getPort());
                serverSocket.send(dataPacket);

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { serverSocket.receive(ackPacket); } catch (SocketTimeoutException ex) { break; }

                // Check the Ack packet number
                buffer = ByteBuffer.wrap(ackPacket.getData());
                ackPacketNo = buffer.getInt(5);

                // Increment window head, after a successful Ack, end message after final ack is received
                if (ackPacketNo == windowHead) windowHead++;
                if(windowHead > finalBlockNo) return;
            }
        }
    }


    /**
     * Write an object to a playerClient Socket, This is done by sending a write request packet to the client, receiving
     * an Ack packet and than sequentially sending the objects data in packets sized to 512 bytes with a 503 byte payload.
     *
     * @param userID UserID of the client
     * @param armor Armor sent to the client
     * @throws Exception
     */
    public static void writeObject(UserID userID, Armor armor) throws Exception{
        byte [] data, wrqData = new byte[10], ackData = new byte[10];
        int index, ackPacketNo, objSize, finalBlockNo, block, windowHead = 1;
        boolean ackFail;
        DatagramPacket dataPacket, sendPacket, ackPacket;

        // Convert the Armor into a byte array
        byte [] objBytes = Armor.convertToBytes(armor);
        objSize = objBytes.length;

        // Determine the number of blocks needed to be sent to the client
        if(objBytes.length % 503 == 0) finalBlockNo = objSize / 503;
        else finalBlockNo = (objSize / 503) + 1;

        // Create write request packet:
        // [byte][byte][byte][byte][byte]          [int]         [byte]
        //  -----------------------------------------------------------
        // |  0  | opc |  0  | type|  0  |      object size      |  0  |
        //  -----------------------------------------------------------
        wrqData[1] = WRQ;  // Set the opcode to write request
        wrqData[3] = AMR;  // Set object type to Armor

        // Add the object's length to the write request packet
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(objBytes.length);
        wrqData[5] = buffer.get(0);
        wrqData[6] = buffer.get(1);
        wrqData[7] = buffer.get(2);
        wrqData[8] = buffer.get(3);

        do {
            // Send Write Request to client
            sendPacket = new DatagramPacket(wrqData, wrqData.length, userID.getIpAddr(), userID.getPort());
            serverSocket.send(sendPacket);

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            serverSocket.receive(ackPacket);

            // Confirm the Ack packet
            buffer = ByteBuffer.wrap(ackPacket.getData());
            if (buffer.get(3) == AMR && buffer.getInt(4) == objSize) ackFail = true;
            else ackFail = false;
        } while(ackFail);  // Resend Write Request, in the event of a failure


        // Send Armor to Client
        while(true) {
            // Send window to client
            block = windowHead;
            while (block < WINDOW_SIZE + windowHead && block <= finalBlockNo) {
                // Create the Data packet:
                //  0 1 2 3  4 5 6 7 8    9   10   11   12   13 ...  511
                //  ----------------------------------------------------
                // |0|#|0|#|Packet #|0|DATA|DATA|DATA|DATA|DATA|...|DATA|
                //  ----------------------------------------------------
                //
                // 1) Generate data packet heading
                data = new byte[512];
                data[1] = DATA;
                data[3] = AMR;
                data[4] = (byte) (block >> 24);
                data[5] = (byte) (block >> 16);
                data[6] = (byte) (block >> 8);
                data[7] = (byte) (block);
                // 2) Generate data packet payload
                index = 9;
                for (int i = (block - 1) * PAYLOAD; i < block * PAYLOAD && i  < objSize; i++) {
                    data[index] = objBytes[i];
                    ++index;
                }

                // Send Data Packet
                dataPacket = new DatagramPacket(data, data.length, userID.getIpAddr(), userID.getPort());
                serverSocket.send(dataPacket);

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { serverSocket.receive(ackPacket); } catch (SocketTimeoutException ex) { break; }

                // Check the Ack packet number
                buffer = ByteBuffer.wrap(ackPacket.getData());
                ackPacketNo = buffer.getInt(5);

                // Increment window head, after a successful Ack, end message after final ack is received
                if (ackPacketNo == windowHead) windowHead++;
                if(windowHead > finalBlockNo) return;
            }
        }
    }


    /**
     * Write an object to a playerClient Socket, This is done by sending a write request packet to the client, receiving
     * an Ack packet and than sequentially sending the objects data in packets sized to 512 bytes with a 503 byte payload.
     *
     * @param userID UserID of the client
     * @param consumable Consumable sent to the client
     * @throws Exception
     */
    public static void writeObject(UserID userID, Consumable consumable) throws Exception{
        byte [] data, wrqData = new byte[10], ackData = new byte[10];
        int index, ackPacketNo, objSize, finalBlockNo, block, windowHead = 1;
        boolean ackFail;
        DatagramPacket dataPacket, sendPacket, ackPacket;

        // Convert the Consumable into a byte array
        byte [] objBytes = Consumable.convertToBytes(consumable);
        objSize = objBytes.length;

        // Determine the number of blocks needed to be sent to the client
        if(objBytes.length % 503 == 0) finalBlockNo = objSize / 503;
        else finalBlockNo = (objSize / 503) + 1;

        // Create write request packet:
        // [byte][byte][byte][byte][byte]          [int]         [byte]
        //  -----------------------------------------------------------
        // |  0  | opc |  0  | type|  0  |      object size      |  0  |
        //  -----------------------------------------------------------
        wrqData[1] = WRQ;  // Set the opcode to write request
        wrqData[3] = CNSM;  // Set object type to Consumable

        // Add the object's length to the write request packet
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(objBytes.length);
        wrqData[5] = buffer.get(0);
        wrqData[6] = buffer.get(1);
        wrqData[7] = buffer.get(2);
        wrqData[8] = buffer.get(3);

        do {
            // Send Write Request to client
            sendPacket = new DatagramPacket(wrqData, wrqData.length, userID.getIpAddr(), userID.getPort());
            serverSocket.send(sendPacket);

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            serverSocket.receive(ackPacket);

            // Confirm the Ack packet
            buffer = ByteBuffer.wrap(ackPacket.getData());
            if (buffer.get(3) == CNSM && buffer.getInt(4) == objSize) ackFail = true;
            else ackFail = false;
        } while(ackFail);  // Resend Write Request, in the event of a failure


        // Send Consumable to Client
        while(true) {
            // Send window
            block = windowHead;
            while (block < WINDOW_SIZE + windowHead && block <= finalBlockNo) {
                // Create the Data packet:
                //  0 1 2 3  4 5 6 7 8    9   10   11   12   13 ...  511
                //  ----------------------------------------------------
                // |0|#|0|#|Packet #|0|DATA|DATA|DATA|DATA|DATA|...|DATA|
                //  ----------------------------------------------------
                //
                // 1) Generate data packet heading
                data = new byte[512];
                data[1] = DATA;
                data[3] = CNSM;
                data[4] = (byte) (block >> 24);
                data[5] = (byte) (block >> 16);
                data[6] = (byte) (block >> 8);
                data[7] = (byte) (block);
                // 2) Generate data packet payload
                index = 9;
                for (int i = (block - 1) * PAYLOAD; i < block * PAYLOAD && i < objSize; i++) {
                    data[index] = objBytes[i];
                    ++index;
                }

                // Send Data Packet
                dataPacket = new DatagramPacket(data, data.length, userID.getIpAddr(), userID.getPort());
                serverSocket.send(dataPacket);

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { serverSocket.receive(ackPacket); } catch (SocketTimeoutException ex) { break; }

                // Check the Ack packet number
                buffer = ByteBuffer.wrap(ackPacket.getData());
                ackPacketNo = buffer.getInt(5);

                // Increment window head, after a successful Ack, end message after final ack is received
                if (ackPacketNo == windowHead) windowHead++;
                if(windowHead > finalBlockNo) return;
            }
        }
    }


    /**
     * Write an object to a playerClient Socket, This is done by sending a write request packet to the client, receiving
     * an Ack packet and than sequentially sending the objects data in packets sized to 512 bytes with a 503 byte payload.
     *
     * @param userID UserID of the client
     * @param champion Champion sent to the client
     * @throws Exception
     */
    public static void writeObject(UserID userID, Champion champion) throws Exception{
        byte [] data, wrqData = new byte[10], ackData = new byte[10];
        int index, ackPacketNo, objSize, finalBlockNo, block, windowHead = 1;
        boolean ackFail;
        DatagramPacket dataPacket, sendPacket, ackPacket;

        // Convert the Champion into a byte array
        byte [] objBytes = Champion.convertToBytes(champion);
        objSize = objBytes.length;

        // Determine the number of blocks needed to be sent to the client
        if(objBytes.length % 503 == 0) finalBlockNo = objSize / 503;
        else finalBlockNo = (objSize / 503) + 1;

        // Create write request packet:
        // [byte][byte][byte][byte][byte]          [int]         [byte]
        //  -----------------------------------------------------------
        // |  0  | opc |  0  | type|  0  |      object size      |  0  |
        //  -----------------------------------------------------------
        wrqData[1] = WRQ;  // Set the opcode to write request
        wrqData[3] = CHMP;  // Set object type to Champion

        // Add the object's length to the write request packet
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(objBytes.length);
        wrqData[5] = buffer.get(0);
        wrqData[6] = buffer.get(1);
        wrqData[7] = buffer.get(2);
        wrqData[8] = buffer.get(3);

        do {
            // Send Write Request to client
            sendPacket = new DatagramPacket(wrqData, wrqData.length, userID.getIpAddr(), userID.getPort());
            serverSocket.send(sendPacket);

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            serverSocket.receive(ackPacket);

            // Confirm the Ack packet
            buffer = ByteBuffer.wrap(ackPacket.getData());
            if (buffer.get(3) == CHMP && buffer.getInt(4) == objSize) ackFail = true;
            else ackFail = false;
        } while(ackFail);  // Resend Write Request, in the event of a failure


        // Send Champion to Client
        while(true) {
            // Send window to client
            block = windowHead;
            while (block < WINDOW_SIZE + windowHead && block <= finalBlockNo) {
                // Create the Data packet:
                //  0 1 2 3  4 5 6 7 8    9   10   11   12   13 ...  511
                //  ----------------------------------------------------
                // |0|#|0|#|Packet #|0|DATA|DATA|DATA|DATA|DATA|...|DATA|
                //  ----------------------------------------------------
                //
                // 1) Generate data packet heading
                data = new byte[512];
                data[1] = DATA;
                data[3] = CHMP;
                data[4] = (byte) (block >> 24);
                data[5] = (byte) (block >> 16);
                data[6] = (byte) (block >> 8);
                data[7] = (byte) (block);
                // 2) Generate data packet payload
                index = 9;
                for (int i = (block - 1) * PAYLOAD; i < block * PAYLOAD && i < objSize; i++) {
                    data[index] = objBytes[i];
                    ++index;
                }

                // Send Data Packet
                dataPacket = new DatagramPacket(data, data.length, userID.getIpAddr(), userID.getPort());
                serverSocket.send(dataPacket);

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { serverSocket.receive(ackPacket); } catch (SocketTimeoutException ex) { break; }

                // Check the Ack packet number
                buffer = ByteBuffer.wrap(ackPacket.getData());
                ackPacketNo = buffer.getInt(5);

                // Increment window head, after a successful Ack, end message after final ack is received
                if (ackPacketNo == windowHead) windowHead++;
                if(windowHead > finalBlockNo) return;
            }
        }
    }


    /**
     * Write an object to a playerClient Socket, This is done by sending a write request packet to the client, receiving
     * an Ack packet and than sequentially sending the objects data in packets sized to 512 bytes with a 503 byte payload.
     *
     * @param userID UserID of the client
     * @param creature Creature sent to the client
     * @throws Exception
     */
    public static void writeObject(UserID userID, Creature creature) throws Exception{
        byte [] data, wrqData = new byte[10], ackData = new byte[10];
        int index, ackPacketNo, objSize, finalBlockNo, block, windowHead = 1;
        boolean ackFail;
        DatagramPacket dataPacket, sendPacket, ackPacket;

        // Convert the Creature into a byte array
        byte [] objBytes = Creature.convertToBytes(creature);
        objSize = objBytes.length;

        // Determine the number of blocks needed to be sent to the client
        if(objBytes.length % 503 == 0) finalBlockNo = objSize / 503;
        else finalBlockNo = (objSize / 503) + 1;

        // Create write request packet:
        // [byte][byte][byte][byte][byte]          [int]         [byte]
        //  -----------------------------------------------------------
        // |  0  | opc |  0  | type|  0  |      object size      |  0  |
        //  -----------------------------------------------------------
        wrqData[1] = WRQ;  // Set the opcode to write request
        wrqData[3] = CRTR;  // Set object type to Creature

        // Add the object's length to the write request packet
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(objBytes.length);
        wrqData[5] = buffer.get(0);
        wrqData[6] = buffer.get(1);
        wrqData[7] = buffer.get(2);
        wrqData[8] = buffer.get(3);

        do {
            // Send Write Request to client
            sendPacket = new DatagramPacket(wrqData, wrqData.length, userID.getIpAddr(), userID.getPort());
            serverSocket.send(sendPacket);

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            serverSocket.receive(ackPacket);

            // Confirm the Ack packet
            buffer = ByteBuffer.wrap(ackPacket.getData());
            if (buffer.get(3) == CRTR && buffer.getInt(4) == objSize) ackFail = true;
            else ackFail = false;
        } while(ackFail);  // Resend Write Request, in the event of a failure


        // Send Creature to Client
        while(true) {
            // Send window to client
            block = windowHead;
            while (block < WINDOW_SIZE + windowHead && block <= finalBlockNo) {
                // Create the Data packet:
                //  0 1 2 3  4 5 6 7 8    9   10   11   12   13 ...  511
                //  ----------------------------------------------------
                // |0|#|0|#|Packet #|0|DATA|DATA|DATA|DATA|DATA|...|DATA|
                //  ----------------------------------------------------
                //
                // 1) Generate data packet heading
                data = new byte[512];
                data[1] = DATA;
                data[3] = CRTR;
                data[4] = (byte) (block >> 24);
                data[5] = (byte) (block >> 16);
                data[6] = (byte) (block >> 8);
                data[7] = (byte) (block);
                // 2) Generate data packet payload
                index = 9;
                for (int i = (block - 1) * PAYLOAD; i < block * PAYLOAD && i < objSize; i++) {
                    data[index] = objBytes[i];
                    ++index;
                }

                // Send Data Packet
                dataPacket = new DatagramPacket(data, data.length, userID.getIpAddr(), userID.getPort());
                serverSocket.send(dataPacket);

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { serverSocket.receive(ackPacket); } catch (SocketTimeoutException ex) { break; }

                // Check the Ack packet number
                buffer = ByteBuffer.wrap(ackPacket.getData());
                ackPacketNo = buffer.getInt(5);

                // Increment window head, after a successful Ack, end message after final ack is received
                if (ackPacketNo == windowHead) windowHead++;
                if(windowHead > finalBlockNo) return;
            }
        }
    }


    /**
     * Write an object to a playerClient Socket, This is done by sending a write request packet to the client, receiving
     * an Ack packet and than sequentially sending the objects data in packets sized to 512 bytes with a 503 byte payload.
     *
     * @param userID UserID of the client
     * @param inventory Inventory sent to the client
     * @throws Exception
     */
    public static void writeObject(UserID userID, Inventory inventory) throws Exception{
        byte [] data, wrqData = new byte[10], ackData = new byte[10];
        int index, ackPacketNo, objSize, finalBlockNo, block, windowHead = 1;
        boolean ackFail;
        DatagramPacket dataPacket, sendPacket, ackPacket;

        // Convert the Inventory into a byte array
        byte [] objBytes = Inventory.convertToBytes(inventory);
        objSize = objBytes.length;

        // Determine the number of blocks needed to be sent to the client
        if(objBytes.length % 503 == 0) finalBlockNo = objSize / 503;
        else finalBlockNo = (objSize / 503) + 1;

        // Create write request packet:
        // [byte][byte][byte][byte][byte]          [int]         [byte]
        //  -----------------------------------------------------------
        // |  0  | opc |  0  | type|  0  |      object size      |  0  |
        //  -----------------------------------------------------------
        wrqData[1] = WRQ;  // Set the opcode to write request
        wrqData[3] = INV;  // Set object type to Inventory

        // Add the object's length to the write request packet
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(objBytes.length);
        wrqData[5] = buffer.get(0);
        wrqData[6] = buffer.get(1);
        wrqData[7] = buffer.get(2);
        wrqData[8] = buffer.get(3);

        do {
            // Send Write Request to client
            sendPacket = new DatagramPacket(wrqData, wrqData.length, userID.getIpAddr(), userID.getPort());
            serverSocket.send(sendPacket);

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            serverSocket.receive(ackPacket);

            // Confirm the Ack packet
            buffer = ByteBuffer.wrap(ackPacket.getData());
            if (buffer.get(3) == INV && buffer.getInt(4) == objSize) ackFail = true;
            else ackFail = false;
        } while(ackFail);  // Resend Write Request, in the event of a failure


        // Send Inventory to Client
        while(true) {
            // Send window to client
            block = windowHead;
            while (block < WINDOW_SIZE + windowHead && block <= finalBlockNo) {
                // Create the Data packet:
                //  0 1 2 3  4 5 6 7 8    9   10   11   12   13 ...  511
                //  ----------------------------------------------------
                // |0|#|0|#|Packet #|0|DATA|DATA|DATA|DATA|DATA|...|DATA|
                //  ----------------------------------------------------
                //
                // 1) Generate data packet heading
                data = new byte[512];
                data[1] = DATA;
                data[3] = INV;
                data[4] = (byte) (block >> 24);
                data[5] = (byte) (block >> 16);
                data[6] = (byte) (block >> 8);
                data[7] = (byte) (block);
                // 2) Generate data packet payload
                index = 9;
                for (int i = (block - 1) * PAYLOAD; i < block * PAYLOAD && i < objSize; i++) {
                    data[index] = objBytes[i];
                    ++index;
                }

                // Send Data Packet
                dataPacket = new DatagramPacket(data, data.length, userID.getIpAddr(), userID.getPort());
                serverSocket.send(dataPacket);

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { serverSocket.receive(ackPacket); } catch (SocketTimeoutException ex) { break; }

                // Check the Ack packet number
                buffer = ByteBuffer.wrap(ackPacket.getData());
                ackPacketNo = buffer.getInt(5);

                // Increment window head, after a successful Ack, end message after final ack is received
                if (ackPacketNo == windowHead) windowHead++;
                if(windowHead > finalBlockNo) return;
            }
        }
    }


    /**
     * Write an object to a playerClient Socket, This is done by sending a write request packet to the client, receiving
     * an Ack packet and than sequentially sending the objects data in packets sized to 512 bytes with a 503 byte
     * payload.
     *
     * @param userID UserID of the client
     * @param uID UserID sent to the client
     * @throws Exception
     */
    public static void writeObject(UserID userID, UserID uID) throws Exception{
        byte [] data, wrqData = new byte[10], ackData = new byte[10];
        int index, ackPacketNo, objSize, finalBlockNo, block, windowHead = 1;
        boolean ackFail;
        DatagramPacket dataPacket, sendPacket, ackPacket;

        // Convert the UserID into a byte array
        byte [] objBytes = UserID.convertToBytes(uID);
        objSize = objBytes.length;

        // Determine the number of blocks needed to be sent to the client
        if(objBytes.length % 503 == 0) finalBlockNo = objSize / 503;
        else finalBlockNo = (objSize / 503) + 1;

        // Create write request packet:
        // [byte][byte][byte][byte][byte]          [int]         [byte]
        //  -----------------------------------------------------------
        // |  0  | opc |  0  | type|  0  |      object size      |  0  |
        //  -----------------------------------------------------------
        wrqData[1] = WRQ;  // Set the opcode to write request
        wrqData[3] = UID;  // Set object type to UserID

        // Add the object's length to the write request packet
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(objBytes.length);
        wrqData[5] = buffer.get(0);
        wrqData[6] = buffer.get(1);
        wrqData[7] = buffer.get(2);
        wrqData[8] = buffer.get(3);

        do {
            // Send Write Request to client
            sendPacket = new DatagramPacket(wrqData, wrqData.length, userID.getIpAddr(), userID.getPort());
            serverSocket.send(sendPacket);

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            serverSocket.receive(ackPacket);

            // Confirm the Ack packet
            buffer = ByteBuffer.wrap(ackPacket.getData());
            if (buffer.get(3) == INV && buffer.getInt(4) == objSize) ackFail = true;
            else ackFail = false;
        } while(ackFail);  // Resend Write Request, in the event of a failure


        // Send UserID to Client
        while(true) {
            // Send window to client
            block = windowHead;
            while (block < WINDOW_SIZE + windowHead && block <= finalBlockNo) {
                // Create the Data packet:
                //  0 1 2 3  4 5 6 7 8    9   10   11   12   13 ...  511
                //  ----------------------------------------------------
                // |0|#|0|#|Packet #|0|DATA|DATA|DATA|DATA|DATA|...|DATA|
                //  ----------------------------------------------------
                //
                // 1) Generate data packet heading
                data = new byte[512];
                data[1] = DATA;
                data[3] = UID;
                data[4] = (byte) (block >> 24);
                data[5] = (byte) (block >> 16);
                data[6] = (byte) (block >> 8);
                data[7] = (byte) (block);
                // 2) Generate data packet payload
                index = 9;
                for (int i = (block - 1) * PAYLOAD; i < block * PAYLOAD && i < objSize; i++) {
                    data[index] = objBytes[i];
                    ++index;
                }

                // Send Data Packet
                dataPacket = new DatagramPacket(data, data.length, userID.getIpAddr(), userID.getPort());
                serverSocket.send(dataPacket);

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { serverSocket.receive(ackPacket); } catch (SocketTimeoutException ex) { break; }

                // Check the Ack packet number
                buffer = ByteBuffer.wrap(ackPacket.getData());
                ackPacketNo = buffer.getInt(5);

                // Increment window head, after a successful Ack, end message after final ack is received
                if (ackPacketNo == windowHead) windowHead++;
                if(windowHead > finalBlockNo) return;
            }
        }
    }


    /**
     * Listens to a potential request, and continues with the appropriate sequence of operations based on the request
     *
     * @exception Exception Thrown when a SocketTimeoutException occurs
     */
    public static TransferredObject listen() throws Exception{
        byte [] ackData, requestData = new byte[10];
        byte opCode, objType;
        int objSize;
        DatagramPacket rqPacket, ackPacket;

        // Receive request from client (e.g. Write Request, Read Request, etc.)
        serverSocket.setSoTimeout(2000);
        rqPacket = new DatagramPacket(requestData, requestData.length);
        serverSocket.receive(rqPacket);
        serverSocket.setSoTimeout(300);

        // Ack message is a copy of the request from server
        ackData = rqPacket.getData();
        ByteBuffer byteBuffer = ByteBuffer.wrap(ackData);

        // Determine the request's opCode, object type, and object size
        opCode = ackData[1];
        objType = ackData[3];
        objSize = byteBuffer.getInt(5);

        // Determine which logic branch to execute as determined by the request's opCode
        switch(opCode){
            // Read Request
            case RRQ:
                break;
            // Write Request
            case WRQ:  // WRQ: The client is attempting to write an object to
                TransferredObject rtnObj;
                ackPacket = new DatagramPacket(ackData, ackData.length, rqPacket.getAddress(), rqPacket.getPort());
                serverSocket.send(ackPacket);
                byte [] object = writeRequest(objType, objSize, rqPacket.getAddress(), rqPacket.getPort());

                // Convert received byte array's into Objects
                switch(objType){

                    // CombatMap
                    case CMAP:
                        rtnObj = CombatMap.convertToCombatMap(object);
                        CombatMap.convertToCombatMap(object).printMap();
                        return rtnObj;

                    // Weapon
                    case WPN:
                        rtnObj = Weapon.convertToWeapon(object);
                        Weapon.convertToWeapon(object).printWeapon();
                        return rtnObj;

                    // Armor
                    case AMR:
                        rtnObj = Armor.convertToArmor(object);
                        Armor.convertToArmor(object).printArmor();
                        return rtnObj;

                    // Consumable
                    case CNSM:
                        rtnObj = Consumable.convertToConsumable(object);
                        Consumable.convertToConsumable(object).printConsumable();
                        return rtnObj;

                    // Champion
                    case CHMP:
                        rtnObj = Champion.convertToChampion(object);
                        Champion.convertToChampion(object).printChampion();
                        return rtnObj;

                    // Creature
                    case CRTR:
                        rtnObj = Creature.convertToCreature(object);
                        Creature.convertToCreature(object).printCreature();
                        return rtnObj;

                    // Inventory
                    case INV:
                        rtnObj = Inventory.convertToInventory(object);
                        Inventory.convertToInventory(object).printInventory();
                        return rtnObj;

                    // UserID
                    case UID:
                        rtnObj = UserID.convertToUserID(object);
                        UserID.convertToUserID(object).printUID();
                        return rtnObj;

                    // Default
                    default:
                        break;
                }

                break;
            case DATA:
                break;
            default:
                break;
        }

        return null;
    }


    /**
     * The client has sent a write request to the server, the server in turn reads the data from the client and creates
     * a byte array from the payload of the client's messages
     *
     * @param objType The type of object being sent from the client
     * @param objSize The number of bits that make the object
     * @param ipAddr IP address of the client
     * @param port Port the client is communicating on
     * @return The object sent from the client in the form of a byte array
     */
    public static byte[] writeRequest(byte objType, int objSize, InetAddress ipAddr, int port){
        byte [] data = new byte[512];
        int finalBlockNo, rcvdPacketNo, block = 1;
        DatagramPacket dataPacket;

        ByteBuffer buffer = ByteBuffer.allocate(objSize);

        // Determine the number of blocks being sent
        if(objSize % PAYLOAD == 0) finalBlockNo = objSize / PAYLOAD;
        else finalBlockNo = (objSize / PAYLOAD) + 1;

        // While data's being received
        while(block <= finalBlockNo){
            // Receive Packet
            dataPacket = new DatagramPacket(data, data.length, ipAddr, port);
            try { serverSocket.receive(dataPacket); }
            catch (Exception ex){ continue; }

            // Convert the received packet into a byte array, determine packet no of the received packet
            data = dataPacket.getData();
            rcvdPacketNo = ((int)data[4] << 24) + ((int)data[5] << 16) + ((int)data[6] << 8) + (int)data[7];

            // Confirm that the received packet is next in the sequence
            if(rcvdPacketNo == block){
                // Insert the payload into the byte buffer
                if(block * 503 > objSize) {
                    buffer.put(Arrays.copyOfRange(data, PAYLOAD_HEAD, (objSize + PAYLOAD_HEAD) - ((block - 1) * PAYLOAD)));
                } else { buffer.put(Arrays.copyOfRange(data, PAYLOAD_HEAD, (PAYLOAD + PAYLOAD_HEAD))); }

                // Send the Ack to the client, and increment the block number
                sendAck(rcvdPacketNo, objType,ipAddr, port);
                block++;
            }
        }

        // Return the object as a byte array
        return buffer.array();
    }


    /**
     * Given a packet number, object type, ip address, and a port number. Create and send the Ack message to the
     * specified location
     *
     * @param packetNo Integer being sent within the ack packet
     * @param objType What type of object is being communicated between the server and client
     * @param ipAddr Ip address of the client
     * @param port Port of the client
     */
    public static void sendAck(int packetNo, byte objType, InetAddress ipAddr, int port){
        byte [] ackData = new byte[10];

        // Create the Ack message
        ackData[1] = ACK;
        ackData[3] = objType;
        ackData[5] = (byte)(packetNo >> 24);
        ackData[6] = (byte)(packetNo >> 16);
        ackData[7] = (byte)(packetNo >> 8);
        ackData[8] = (byte)packetNo;

        // Convert the Ack message into a datagramPacket, than send the Ack
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, ipAddr, port);
        try { serverSocket.send(ackPacket); } catch(IOException ex){ ex.printStackTrace(); }
    }


    /**
     * Print each element within a byte array
     *
     * @param bytes An array of bytes
     */
    public static void printByteArray(byte [] bytes){
        for(int i = 0; i < bytes.length; i++) { System.out.print(bytes[i]); }
        System.out.println();
    }
}