package PaperGame.networking;

import PaperGame.entities.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;

public class DMServer implements Runnable{
    private static DatagramSocket serverSocket;

    // Opcode's, used in packet header's to distinguish the purpose of a packet
    private static final byte RRQ  = 0;          // Read Request
    private static final byte WRQ  = 1;          // Write Request
    private static final byte ACK  = 2;          // Acknowledge
    private static final byte DATA = 3;          // Data Packet

    // Object Type Code's, used in packet header's to distinguish the object type of a given piece of Data
    private static final byte CMAP = 4;      // Combat Map Object
    private static final byte WPN  = 5;      // Weapon Object
    private static final byte AMR  = 6;      // Armor Object
    private static final byte CNSM = 7;      // Consumable Object
    private static final byte CHMP = 8;      // Champion Object
    private static final byte CRTR = 9;      // Creature Object
    private static final byte INV  = 10;     // Inventory Object

    private static final int WINDOW_SIZE = 4;    // Window size for Sliding Window Protocol
    private static final int PAYLOAD     = 503;  // Size of the Data payload per packet

    public void run(){
        ArrayList<String> ipAddresses = getIP();


        // Objects created to send to the Client
        CombatMap cMap = new CombatMap(3,5,"TEST_1394$");

        Weapon wpn = new Weapon((short)3,(short)3,(short)3, (short) 3, 30,
                "Testing Great$word", Item.KNIGHT,(short) 1);
        Consumable cnsm = new Consumable("Everything's Zero");
        Armor armr = new Armor((short)1, (short)2, (short)3, (short)4, 30, "@rm0r_9494",
                Armor.PANTS);
        Champion chmp = new Champion("Archer", "Elf", "$en$3i_T3$T");
        Creature crtr = new Creature();
        Inventory inv = new Inventory();
        inv.addItem(cnsm, armr, wpn);



        UserID userID;

        for(String tString: ipAddresses){ System.out.println("IP Address: " + tString); }

        openSocket(0);     // Open the server socket
        userID = clientJoin();      // Let the client join the server
        writeObject(userID, cMap);  // Write Combat Map to client
        writeObject(userID, wpn);   // Write Weapon to client
        writeObject(userID, cnsm);  // Write Consumable to client
        writeObject(userID, armr);  // Write Armor to client
        writeObject(userID, chmp);  // Write Champion to client
        writeObject(userID, crtr);  // Write Creature to client
        writeObject(userID, inv);   // Write Inventory to client
        closeSocket();              // Close the server socket
    }


    /**
     * @return Return's the Public IP address of the local machine in String format
     */
    public ArrayList<String> getIP() {
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
     * Receive a player join packet and send an ack packet
     *
     * @return Return a User's ID
     */
    public UserID clientJoin(){

        byte[] data = new byte[128];
        char tChar;
        String userName = "";
        int hashCode, index = 1;

        try{
            // Receive a Datagram Packet from the client, transfer that data into a byte buffer
            DatagramPacket receivePacket = new DatagramPacket(data, data.length);
            serverSocket.receive(receivePacket);
            ByteBuffer byteBuffer = ByteBuffer.wrap(receivePacket.getData());

            // Retrieve the player's user name and hash code
            while (true) {
                tChar = byteBuffer.getChar(index);
                if (tChar == (char) 0) break;  // End the loop in the event of a delimiter being received
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

            return userID;  // Return the client's UserID
        } catch(IOException ex){
            ex.printStackTrace();
            return null;
        }
    }


    /**
     * Open the server socket, and set the server sockets timeout
     *
     * @param timeout Integer value specifying the socket timeout
     */
    public void openSocket(int timeout){
        try {
            serverSocket = new DatagramSocket(9876);  // open server socket
            serverSocket.setSoTimeout(timeout);            // set timeout to 300 milliseconds
        } catch(SocketException ex) { ex.printStackTrace(); }
    }


    /**
     * Close the server socket
     */
    public void closeSocket(){ serverSocket.close(); }


    /**
     * Create an ack packet when a player joins the group
     *
     * @param hashCode The new player's hash code
     * @return A byte array containing the new player's hash code within a 4 byte-array
     */
    public byte[] clientJoinAckArray(int hashCode){
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
     */
    public void writeObject(UserID userID, CombatMap combatMap){
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
            try{ serverSocket.send(sendPacket); }
            catch(IOException ex) { ex.printStackTrace(); }

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            try{ serverSocket.receive(ackPacket); }
            catch(IOException ex){ ex.printStackTrace(); }

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
                try{ serverSocket.send(dataPacket); }
                catch(IOException ex){ ex.printStackTrace(); }

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try{ serverSocket.receive(ackPacket); }
                catch(SocketTimeoutException ex){ break; }
                catch(IOException ex){ ex.printStackTrace(); }

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
    public void writeObject(UserID userID, Weapon weapon){
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
            try{ serverSocket.send(sendPacket); }
            catch(IOException ex){ ex.printStackTrace(); }

            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            try{ serverSocket.receive(ackPacket); }
            catch(IOException ex){ ex.printStackTrace(); }


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
                try{ serverSocket.send(dataPacket); }
                catch(IOException ex){ ex.printStackTrace(); }

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try{ serverSocket.receive(ackPacket); }
                catch(SocketTimeoutException ex){ break; }
                catch(IOException ex){ ex.printStackTrace(); }


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
    public void writeObject(UserID userID, Armor armor){
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
            try{ serverSocket.send(sendPacket); }
            catch(IOException ex){ ex.printStackTrace(); }


            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            try{ serverSocket.receive(ackPacket); }
            catch(IOException ex){ ex.printStackTrace(); }


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
                try{ serverSocket.send(dataPacket); }
                catch(IOException ex){ ex.printStackTrace(); }


                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { serverSocket.receive(ackPacket); }
                catch(SocketTimeoutException ex){ break; }
                catch(IOException ex){ ex.printStackTrace(); }


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
     */
    public void writeObject(UserID userID, Consumable consumable){
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
            try{ serverSocket.send(sendPacket); }
            catch(IOException ex){ ex.printStackTrace(); }


            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            try{ serverSocket.receive(ackPacket); }
            catch(IOException ex){ ex.printStackTrace(); }


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
                try{ serverSocket.send(dataPacket); }
                catch(IOException ex){ ex.printStackTrace(); }

                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try{ serverSocket.receive(ackPacket); }
                catch(SocketTimeoutException ex){ break; }
                catch(IOException ex){ ex.printStackTrace(); }


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
     */
    public void writeObject(UserID userID, Champion champion){
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
            try{ serverSocket.send(sendPacket); }
            catch(IOException ex){ ex.printStackTrace(); }


            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            try{ serverSocket.receive(ackPacket); }
            catch(IOException ex){ ex.printStackTrace(); }


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
                try{ serverSocket.send(dataPacket); }
                catch(IOException ex){ ex.printStackTrace(); }


                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try{ serverSocket.receive(ackPacket); }
                catch(SocketTimeoutException ex){ break; }
                catch(IOException ex){ ex.printStackTrace(); }


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
     */
    public void writeObject(UserID userID, Creature creature){
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
            try{ serverSocket.send(sendPacket); }
            catch(IOException ex){ ex.printStackTrace(); }


            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            try{ serverSocket.receive(ackPacket); }
            catch(IOException ex){ ex.printStackTrace(); }


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
                try{ serverSocket.send(dataPacket); }
                catch(IOException ex){ ex.printStackTrace(); }


                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try { serverSocket.receive(ackPacket); }
                catch(SocketTimeoutException ex){ break; }
                catch(IOException ex){ ex.printStackTrace(); }


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
     */
    public void writeObject(UserID userID, Inventory inventory){
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
            try{ serverSocket.send(sendPacket); }
            catch(IOException ex){ ex.printStackTrace(); }


            // Receive Ack from client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            try{ serverSocket.receive(ackPacket); }
            catch(IOException ex){ ex.printStackTrace(); }


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
                try{ serverSocket.send(dataPacket); }
                catch(IOException ex){ ex.printStackTrace(); }


                block++;  // Increment the block number
            }

            // Receive client ack, and update window head
            while (true) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive Ack
                try{ serverSocket.receive(ackPacket); }
                catch(SocketTimeoutException ex){ break; }
                catch(IOException ex){ ex.printStackTrace(); }


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
     * Print each element within a byte array
     *
     * @param bytes An array of bytes
     */
    public static void printByteArray(byte [] bytes){
        for(int i = 0; i < bytes.length; i++) { System.out.print(bytes[i]); }
        System.out.println();
    }
}