package PaperGame;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;

class PlayerClient
{
    //- CLEAN UP -------------------------------------------------------------------------------------------------------
    private static String userName;
    private static Scanner scanner;
    //------------------------------------------------------------------------------------------------------------------

    private static DatagramSocket clientSocket;  // Client Socket used for network communication
    private static UserID serverID;              // Object containing essential info on communication with server

    // Opcode's, used in packet header's to distinguish the purpose of a packet
    private static final byte RRQ  = 0;      // Read Request
    private static final byte WRQ  = 1;      // Write Request
    private static final byte ACK  = 2;      // Acknowledge
    private static final byte DATA = 3;      // Data Packet

    // Object Type Code's, used in packet header's to distinguish the object type of a given piece of Data
    private static final byte CMAP = 4;      // Combat Map Object
    private static final byte WPN  = 5;      // Weapon Object
    private static final byte AMR  = 6;      // Armor Object
    private static final byte CNSM = 7;      // Consumable Object
    private static final byte CHMP = 8;      // Champion Object
    private static final byte CRTR = 9;      // Creature Object


    private static final int PAYLOAD      = 503;  // Size of the Data payload per packet
    private static final int PAYLOAD_HEAD = 9;    // Index the payload begin's in a data packet


    //- CLEAN UP -------------------------------------------------------------------------------------------------------
    public static void main(String args[]) throws Exception {
        // Create the clients user id
        scanner = new Scanner(System.in);
        System.out.print("Enter user name: ");
        userName = scanner.nextLine();
        openSocket(20000);

        System.out.print("Enter IP address: ");
        String ipAddr = scanner.nextLine();

        if(joinServer(ipAddr)){
            System.out.println("Success");
        }

        listen();

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
    public static boolean joinServer(String ipAddr){
        try {
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
                serverID = new UserID("Server ID", "Server ID".hashCode(), IPAddress, 9876);
                return true;
            }
            else return false;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


    /**
     * Listens to a potential request, and continues with the appropriate sequence of operations based on the request
     * @exception Exception
     */
    public static Object listen() throws Exception{
        byte [] ackData, requestData = new byte[10];
        byte opCode, objType;
        int objSize;
        DatagramPacket rqPacket, ackPacket;


        // Receive request from server (e.g. Write Request, Read Request, etc.)
        clientSocket.setSoTimeout(0);
        rqPacket = new DatagramPacket(requestData, requestData.length);
        clientSocket.receive(rqPacket);
        clientSocket.setSoTimeout(300);

        // Ack message is a copy of the request from server
        ackData = rqPacket.getData();

        // Determine the request's opCode, object type, and object size
        opCode = ackData[1];
        objType = ackData[3];
        objSize = (((int) ackData[5]) << 24) + (((int)ackData[6]) << 16) + (((int)ackData[7]) << 8) + (int) ackData[8];

        // Determine which logic branch to execute as determined by the request's opCode
        switch(opCode){
            // Read Request
            case RRQ:
                break;
            // Write Request
            case WRQ:  // WRQ: The server is attempting to write an object to
                Object rtnObj;
                ackPacket = new DatagramPacket(ackData, ackData.length, rqPacket.getAddress(), rqPacket.getPort());
                clientSocket.send(ackPacket);
                byte [] object = writeRequest(objType, objSize, rqPacket.getAddress(), rqPacket.getPort());

                // Convert received byte array's into Objects
                switch(objType){

                    // CombatMap
                    case CMAP:
                        rtnObj = CombatMap.convertToCombatMap(object);
                        return rtnObj;

                    // Weapon
                    case WPN:
                        rtnObj = Weapon.convertToWeapon(object);
                        return rtnObj;

                    // Armor
                    case AMR:
                        rtnObj = Armor.convertToArmor(object);
                        return rtnObj;

                    // Consumable
                    case CNSM:
                        rtnObj = Consumable.convertToConsumable(object);
                        return rtnObj;

                    // Champion
                    case CHMP:
                        rtnObj = Champion.convertToChampion(object);
                        return rtnObj;

                    // Creature
                    case CRTR:
                        rtnObj = Creature.convertToCreature(object);
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


        // TEMPORARY
        return null;
        // TEMPORARY
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