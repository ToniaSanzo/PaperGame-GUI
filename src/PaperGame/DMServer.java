package PaperGame;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;

class DMServer
{
    private static DatagramSocket serverSocket;

    // Opcode's, used in packet header's to distinguish the purpose of a packet
    private static final byte RRQ  = 0;          // Read Request
    private static final byte WRQ  = 1;          // Write Request
    private static final byte ACK  = 2;          // Acknowledge
    private static final byte DATA = 3;          // Data Packet

    // Object Type Code's, used in packet header's to distinguish the object type of a given piece of Data
    private static final byte CMP  = 4;          // Combat Map Object
    private static final byte ITM  = 5;          // Item Object
    private static final byte CHMP = 6;          // Champion Object

    private static final int WINDOW_SIZE = 4;    // Window size for Sliding Window Protocol
    private static final int PAYLOAD     = 503;  // Size of the Data payload per packet

    public static void main(String args[]) throws Exception
    {
        ArrayList<String> ipAddresses = getIP();
        CombatMap cMap = new CombatMap(3,5,"TEST_1394$");
        UserID userID;

        for(String tString: ipAddresses){ System.out.println("IP Address: " + tString); }

        openSocket(0);      // Open the server socket
        userID = clientJoin();      // Let the client join the server
        cMap.printMap();            // Print the map
        writeObject(userID, cMap);  // Write Combat Map to the server
        closeSocket();              // Close the server socket
    }


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

        // Check the user name
        System.out.println("\nUser name: " + userName + "\nHashCode: " + hashCode + "\n");
        UserID userID = new UserID(userName, hashCode, receivePacket.getAddress(), receivePacket.getPort());

        // Create the Ack packet to send to the user
        data = clientJoinAckArray(hashCode);

        // Send the Ack to the client
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, userID.getIpAddr(), userID.getPort());
        serverSocket.send(sendPacket);

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
     * Close the server socket
     */
    public static void closeSocket(){ serverSocket.close(); }


    /**
     * Create a ack packet for when a player joins the group
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
     * an Ack packet and than sequentially sending the objects data in packets sized to 500 bytes with a 491 byte payload.
     *
     * @param userID UserID of the client
     * @param combatMap Combat Map sent to the client
     * @throws Exception
     */
    public static void writeObject(UserID userID, CombatMap combatMap) throws Exception{
        byte [] data, wrqData = new byte[10], ackData = new byte[10];
        int index, ackPacketNo, objSize, finalBlockNo = 0, sequenceNo = 0, windowHead = 1, block = 1;
        boolean ackFail, pendingMessage = true, finalAckNotReceived = true;
        DatagramPacket dataPacket, sendPacket, ackPacket;



        // Convert the Combat Map into a byte array
        byte [] cMapBytes = CombatMap.convertToBytes(combatMap);
        objSize = cMapBytes.length;

        // TESTING: Print Statement to see if the Client and server's message's are consistent
        System.out.println("\nObject Size: " + objSize);
        for(int i = 0; i < objSize; i++){ System.out.print(cMapBytes[i]); }
        System.out.println();
        //------------------------------------------------------------------------------------

        // Determine the number of blocks needed to be sent to the client
        if(cMapBytes.length % 503 == 0) finalBlockNo = objSize / 503;
        else finalBlockNo = (objSize / 503) + 1;

        // Create write request packet:
        // [byte][byte][byte][byte][byte]          [int]         [byte]
        //  -----------------------------------------------------------
        // |  0  | opc |  0  | type|  0  |      object size      |  0  |
        //  -----------------------------------------------------------
        wrqData[1] = WRQ;  // Set the opcode to write request
        wrqData[3] = CMP;  // Set object type to Combat Map

        // Add the object's length to the write request packet
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(cMapBytes.length);
        wrqData[5] = buffer.get(0);
        wrqData[6] = buffer.get(1);
        wrqData[7] = buffer.get(2);
        wrqData[8] = buffer.get(3);

        do {
            // Write Request byte array is put into a datagram packet, than sent to the client
            sendPacket = new DatagramPacket(wrqData, wrqData.length, userID.getIpAddr(), userID.getPort());
            System.out.print("SENDING INITIAL WRITE REQUEST: ");
            printByteArray(sendPacket.getData());
            serverSocket.send(sendPacket);

            // Receive the Ack packet from the client
            ackPacket = new DatagramPacket(ackData, ackData.length);
            serverSocket.receive(ackPacket);
            buffer = ByteBuffer.wrap(ackPacket.getData());

            // Confirm the Ack packet
            if (buffer.get(3) == CMP && buffer.getInt(4) == objSize) ackFail = true;
            else ackFail = false;
        } while(ackFail);  // Reattempt to send the write request on the event of a failure
        System.out.println("Ack received from the client");

        // Send Combat Maps to Client
        while(finalAckNotReceived) {

            // Send the entire window to the client
            block = windowHead;
            while (block < WINDOW_SIZE + windowHead && block <= finalBlockNo) {
                // Create the Data packet:
                //  0 1 2 3  4 5 6 7 8    9   10   11   12   13 ...  511
                //  ----------------------------------------------------
                // |0|#|0|#|Packet #|0|DATA|DATA|DATA|DATA|DATA|...|DATA|
                //  ----------------------------------------------------
                //
                // 1) Create the data packet's header
                data = new byte[512];
                data[1] = DATA;
                data[3] = CMP;
                data[4] = (byte) (block >> 24);
                data[5] = (byte) (block >> 16);
                data[6] = (byte) (block >> 8);
                data[7] = (byte) (block);
                // 2) Create the data packet's payload
                index = 9;
                for (int i = (block - 1) * PAYLOAD; i < block * PAYLOAD && i + ((block - 1) * PAYLOAD) < objSize; i++) {
                    data[index] = cMapBytes[i];
                    System.out.print(cMapBytes[i]);
                    ++index;
                }

                // Send the block to the Client
                dataPacket = new DatagramPacket(data, data.length, userID.getIpAddr(), userID.getPort());
                System.out.print("SENDING: ");
                printByteArray(dataPacket.getData());
                serverSocket.send(dataPacket);

                System.out.println("Combat Map Data Packet: " + block + " sent");
                block++;  // Increment the block number
            }

            // Receive the client's ack, and update the window head index appropriately
            while (pendingMessage) {
                // Create the ackPacket
                ackData = new byte[10];
                ackPacket = new DatagramPacket(ackData, ackData.length, userID.getIpAddr(), userID.getPort());

                // Receive an Ack message from the client
                try {
                    serverSocket.receive(ackPacket);
                } catch (SocketTimeoutException ex) {
                    // Exit the "while(pendingMessage)" loop, when a socket timeout exception occurs
                    pendingMessage = false;
                    continue;
                }

                // Check the Ack packet number
                buffer = ByteBuffer.wrap(ackPacket.getData());
                ackPacketNo = buffer.getInt(5);

                System.out.println("Ack Number: " + ackPacketNo);

                // Increment the window head, after a successful Ack, stop sending packets after final ack
                if (ackPacketNo == windowHead) windowHead++;
                if(windowHead == finalBlockNo) finalAckNotReceived = false;
            }
            pendingMessage = true;  // Reset the pending message flag
        }

        System.out.println("End of object transfer");
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