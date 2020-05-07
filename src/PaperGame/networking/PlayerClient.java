package PaperGame.networking;

import PaperGame.entities.*;
import PaperGame.utility.SaveLoad;
import PaperGame.utility.ThreadBridge;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class PlayerClient implements Runnable
{
    private static DatagramSocket clientSocket;
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
    private static final byte UID         = 11;     // UserID Object
    private static final byte MSG         = 12;     // ChatMessage Object


    private static final int WINDOW_SIZE  = 4;    // Window size for Sliding Window Protocol
    private static final int PAYLOAD      = 503;  // Size of the Data payload per packet
    private static final int PAYLOAD_HEAD = 9;    // Index the payload begin's in a data packet


    //- CLEAN UP -------------------------------------------------------------------------------------------------------
    public void run() {
        userIDs = new ArrayList<UserID>();
        openSocket(1000);

        // Used to join the DM's party
        try {
            joinParty();
            writeChampionName();
        } catch(Exception ex){
            closeSocket();
            return;
        }

        // Main network loop, handle's writing and listening
        clientRun();

        closeSocket();
    }
    //------------------------------------------------------------------------------------------------------------------


    /**
     * Send the Server this user's name and hashcode, after receiving a successful ack from the server the method will
     * return true, otherwise false.
     * Below is a model of the joinPacket byte array, i.e.
     *
     *              [byte][char][char][char][char][char][byte][int]
     *               ----------------------------------------------
     *              |  0  | 'u' | 's' | 'e' | 'r' |'NUL'|  0  | 42 |
     *               ----------------------------------------------
     *
     * @param ipAddr The Server's IP address
     * @return True after successful communication with server, otherwise false
     */
    public static boolean joinServer(String ipAddr) throws Exception{
            String userName = ((UserID)SaveLoad.readObjectFromFile(System.getProperty("user.dir") +
                    "/src/PaperGame/res/UID/myUID")).getName();
            int hashCode;
            byte[] receiveData = new byte[4];

            // Create a joinPacket byte array:
            // [byte][char][char][char][char][char][byte][int]
            //  ----------------------------------------------
            // |  0  | 'u' | 's' | 'e' | 'r' |'NUL'|  0  | 42 |
            //  ----------------------------------------------
            ByteBuffer byteBuffer = ByteBuffer.allocate(128);
            byteBuffer.put((byte)0);
            for(int i = 0; i < userName.length(); i++){ byteBuffer.putChar(userName.charAt(i)); }
            byteBuffer.putChar((char)0);
            byteBuffer.put((byte)0);
            hashCode = userName.hashCode();
            byteBuffer.putInt(hashCode);
            byte[] sendData = byteBuffer.array();

            // Open client socket, and establish the InetAddress ip object
            InetAddress IPAddress = InetAddress.getByName(ipAddr);

            // Send the joinPacket to the Server
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            clientSocket.send(sendPacket);
            // Receive the joinAckPacket from the Server
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            byteBuffer = ByteBuffer.wrap(receivePacket.getData());
            int hashCode2 = byteBuffer.getInt();

            // After successful communication, create the server's User ID and return true
            if(hashCode == hashCode2){
                userIDs.add(new UserID("Server ID", "Server ID".hashCode(), IPAddress, 9876));
                userIDs.get(0).setChampion("Dungeon Master");
                return true;
            } else return false;
    }


    /**
     * Write the Player's champion's name to the server
     * Below is a model of the joinPacket byte array, i.e.
     *
     *              [byte][char][char][char][char][char][byte][int]
     *               ----------------------------------------------
     *              |  0  | 'c' | 'h' | 'm' | 'p' |'NUL'|  0  | 42 |
     *               ----------------------------------------------
     *
     * @return True after successful communication with server, otherwise false
     */
    public static boolean writeChampionName() throws Exception{
        String championName;
        String userName = ((UserID)SaveLoad.readObjectFromFile(System.getProperty("user.dir") +
                "/src/PaperGame/res/UID/myUID")).getName();
        int hashCode;
        byte[] receiveData = new byte[4];

        // Wait until the Player chooses a champion, than get the champion name and send it to the server
        while(!ThreadBridge.checkChampionName()){ Thread.sleep(300); }
        championName = ThreadBridge.getChampionName();

        // Create a joinPacket byte array:
        // [byte][char][char][char][char][char][byte][int]
        //  ----------------------------------------------
        // |  0  | 'c' | 'h' | 'm' | 'p' |'NUL'|  0  | 42 |
        //  ----------------------------------------------
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        byteBuffer.put((byte)0);
        for(int i = 0; i < championName.length(); i++){ byteBuffer.putChar(championName.charAt(i)); }
        byteBuffer.putChar((char)0);
        byteBuffer.put((byte)0);
        hashCode = userName.hashCode();
        byteBuffer.putInt(hashCode);
        byte[] sendData = byteBuffer.array();

        // Send the joinPacket to the Server
        DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, userIDs.get(0).getIpAddr(), userIDs.get(0).getPort());

        clientSocket.send(sendPacket);
        // Receive the joinAckPacket from the Server
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        byteBuffer = ByteBuffer.wrap(receivePacket.getData());
        int hashCode2 = byteBuffer.getInt();

        // After successful communication, create the server's User ID and return true
        if(hashCode == hashCode2){
            return true;
        } else return false;
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

        // Receive request from server (e.g. Write Request, Read Request, etc.)
        clientSocket.setSoTimeout(2000);
        rqPacket = new DatagramPacket(requestData, requestData.length);
        clientSocket.receive(rqPacket);
        clientSocket.setSoTimeout(450);

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
            case WRQ:  // WRQ: The server is attempting to write an object to
                TransferredObject rtnObj;
                ackPacket = new DatagramPacket(ackData, ackData.length, rqPacket.getAddress(), rqPacket.getPort());
                clientSocket.send(ackPacket);
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

                    // Creature
                    case INV:
                        rtnObj = Inventory.convertToInventory(object);
                        Inventory.convertToInventory(object).printInventory();
                        return rtnObj;

                    // UserID
                    case UID:
                        rtnObj = UserID.convertToUserID(object);
                        UserID.convertToUserID(object).printUID();
                        return rtnObj;

                    // ChatMessage
                    case MSG:
                        rtnObj = ChatMessage.convertToChatMessage(object);
                        ChatMessage.convertToChatMessage(object).printChatMessage();
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
     * The server has sent a write request to the client, the client in turn reads the data from the server and creates
     * a byte array from the payloads of the Server's messages
     *
     * @param objType The type of object being sent from the server
     * @param objSize The number of bits that make the object
     * @param ipAddr IP address of the server
     * @param port Port the Server is communicating on
     * @return The object sent from the server in the form of a byte array
     */
    public static byte[] writeRequest(byte objType, int objSize, InetAddress ipAddr, int port){
        byte [] data = new byte[512];
        int finalBlockNo, rcvdPacketNo, block = 1;
        DatagramPacket dataPacket;

        ByteBuffer buffer = ByteBuffer.allocate(objSize);

        // Determine the number of blocks being sent from the server
        if(objSize % PAYLOAD == 0) finalBlockNo = objSize / PAYLOAD;
        else finalBlockNo = (objSize / PAYLOAD) + 1;

        // While data's being received
        while(block <= finalBlockNo){
            // Receive Packet from Server
            dataPacket = new DatagramPacket(data, data.length, ipAddr, port);
            try { clientSocket.receive(dataPacket); }
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

                // Send the Ack back to the server, and increment the block number
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
     * @param objType What type of object is being communicated between the server and the client
     * @param ipAddr Ip address of the server
     * @param port Port of the server
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
        try { clientSocket.send(ackPacket); } catch(IOException ex){ ex.printStackTrace(); }
    }


    /**
     * Network loop, checks to see if the client needs to write to the server, if not listen to the server,
     * than check to make sure the program's still on
     */
    public static void clientRun(){
        TransferredObject transferredObject; // Used

        while(true){

            // If the client has a write request
            if(ThreadBridge.checkOfferFlag()){
                // Attempt to write the offerInventory to the server, If the WRQ fails reset the getOfferInventory
                try{
                    writeObject(userIDs.get(0), ThreadBridge.getOfferInventory());
                } catch(RuntimeException ex){
                    System.err.println("WRQ failed, will attempt WRQ later");
                    ThreadBridge.resetTradeOfferFlag();
                } catch(Exception ex){
                    ex.printStackTrace();
                }
            }

            // Send a chat message
            if(ThreadBridge.checkMessageSendFlag()){
                writeMessage(ThreadBridge.getMessageSend());
            }

            // Listen to Server
            try{
                transferredObject = listen();

                if(transferredObject.getType() == TransferredObject.INVENTORY){
                    ThreadBridge.setReceiveInventory((Inventory)transferredObject);
                }

                if(transferredObject.getType() == TransferredObject.USER_ID){
                    userIDs.add((UserID)transferredObject);
                    ThreadBridge.pushUser(userIDs.get(userIDs.size() - 1));
                }

                if(transferredObject.getType() == TransferredObject.CHAT_MESSAGE){
                    ThreadBridge.receiveMessage((ChatMessage)transferredObject);
                }
            } catch(Exception ex){ }

            // Sleep
            try { Thread.sleep(313); } catch(InterruptedException ex){ }

            // Terminate, if GUI's closed
            if (!ThreadBridge.isGuiOn()) { return; }
        }

    }


    /**
     * Write ChatMessage to every UserID
     */
    public static void writeMessage(ChatMessage message){
        UserID currID;

        for(int i = 0; i < userIDs.size(); i++){
            currID = userIDs.get(i);

            System.out.println("Writing message to " + currID.getName() + "-UserID");
            try {
                writeObject(currID, message);
                try { Thread.sleep(200); } catch(InterruptedException ex){}
            } catch(Exception ex){
                System.out.println("Failed to write message to " + currID.getName() + "-UserID");
                i--;
            }
        }
    }


    /**
     * Write an object to a playerClient Socket, This is done by sending a write request packet to the client, receiving
     * an Ack packet and than sequentially sending the objects data in packets sized to 512 bytes with a 503 byte
     * payload.
     *
     * @param userID UserID of the client
     * @param inventory Inventory sent to the client
     * @throws Exception
     */
    public static void writeObject(UserID userID, Inventory inventory) throws Exception{
        byte [] data, wrqData = new byte[10], ackData = new byte[10];
        int index, ackPacketNo, objSize, finalBlockNo, block, windowHead = 1, wrqFailCount = 0;
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
            clientSocket.send(sendPacket);

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            clientSocket.receive(ackPacket);

            // Confirm the Ack packet
            buffer = ByteBuffer.wrap(ackPacket.getData());
            if (buffer.get(3) == INV && buffer.getInt(4) == objSize){
                ackFail = true;
                wrqFailCount++;
            }
            else {
                ackFail = false;
            }

            // If the WRQ failed 3 times, throw a RuntimeException
            if(wrqFailCount > 2){ throw new RuntimeException(); }

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
                clientSocket.send(dataPacket);

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { clientSocket.receive(ackPacket); } catch (SocketTimeoutException ex) { break; }

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
     * @param message (ChatMessage) - ChatMessage being wrote
     * @throws Exception
     */
    public static void writeObject(UserID userID, ChatMessage message) throws Exception{
        byte [] data, wrqData = new byte[10], ackData = new byte[10];
        int index, ackPacketNo, objSize, finalBlockNo, block, windowHead = 1;
        boolean ackFail;
        DatagramPacket dataPacket, sendPacket, ackPacket;

        // Convert the ChatMessage into a byte array
        byte [] objBytes = ChatMessage.convertToBytes(message);
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
        wrqData[3] = MSG;  // Set object type to ChatMessage

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
            clientSocket.send(sendPacket);

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            clientSocket.receive(ackPacket);

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
                clientSocket.send(dataPacket);

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { clientSocket.receive(ackPacket); } catch (SocketTimeoutException ex) { break; }

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
     * Attempts to connect a client to a DM's party
     */
    public static void joinParty(){
        // Loop until PlayerClient successfully joins the DMServer
        while(true){
            ThreadBridge.setAttemptedPartyJoin(false);
            ThreadBridge.setJoinFail(false);
            ThreadBridge.setIPFlag(false);
            while (!ThreadBridge.checkIP()) {

                // Terminate, if GUI's closed
                if (!ThreadBridge.isGuiOn()) {
                    throw new RuntimeException();
                }

                // Sleep for a 1/3 of a second, than check again
                try { Thread.sleep(333); } catch (InterruptedException ex) { ex.printStackTrace(); }
            }

            try {
                // Grab IP from ThreadBridge, attempt to connect to the Server
                joinServer(ThreadBridge.getIpAddress());
            } catch (Exception e) {
                ThreadBridge.setAttemptedPartyJoin(true);
                ThreadBridge.setJoinFail(true);
                try {
                    Thread.sleep(333);
                } catch(InterruptedException ex){
                    ex.printStackTrace();
                }
                System.err.println("PlayerClient failed to join the DMServer");
                continue;
            }
            ThreadBridge.setAttemptedPartyJoin(true);
            ThreadBridge.setJoinFail(false);
            break;
        }
    }


    /**
     * Open the client socket, and set the timeout to be equal to 20 seconds
     */
    public static void openSocket(int timeout){
        try {
            clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(timeout);
        } catch (SocketException ex){ ex.printStackTrace(); }
    }


    /**
     * Close the client socket
     */
    public static void closeSocket(){ clientSocket.close(); }
}