package PaperGame.networking;

import PaperGame.entities.Inventory;
import PaperGame.entities.TransferredObject;

import java.io.*;
import java.net.InetAddress;

public class UserID implements TransferredObject {
    private int port;            // This user's Port
    private String name;         // This user's name
    private String champion;     // This user's champion name
    private int hashCode;        // This user's hash code
    private InetAddress ipAddr;  // This user's IP address


    /**
     * Construct a UserID object that connects a user's name, hash code, IP address, and port
     *
     * @param name User's name
     * @param hashCode User's hash code
     * @param ipAddr User's IP address
     * @param port User's port
     */
    public UserID(String name, int hashCode, InetAddress ipAddr, int port){
        this.name = name;
        this.hashCode = hashCode;
        this.ipAddr = ipAddr;
        this.port = port;
    }


    /**
     * Set the Champion Name for this userID
     *
     * @param champion - Champion Name
     */
    public void setChampion(String champion) { this.champion = champion; }


    /**
     * Return UserID's champion name
     *
     * @return champion name
     */
    public String getChampion(){ return champion; }


    /**
     * Construct a UserID object that connects a user's name and hash code
     *
     * @param name User's name
     */
    public UserID(String name){
        this.name = name;
        this.hashCode = name.hashCode();
    }


    /**
     * @return Return's the User's name
     */
    public String getName(){ return name; }


    /**
     * @return Return's the user's hash code
     */
    public long getHashCode(){ return hashCode; }


    /**
     * @return Return's the User's IP address
     */
    public InetAddress getIpAddr(){ return ipAddr; }


    /**
     * @return Return's the User's port
     */
    public int getPort() { return port; }


    /**
     * @return Return's that this object is a UserID
     */
    public String getType(){ return USER_ID; }


    /**
     * Convert UserID to byte array
     *
     * @param userID UserID that will be converted into a byte array
     * @return Returns the byte array of the converted UserID
     */
    public static byte[] convertToBytes(UserID userID){
        byte [] byteBuffer = null;  // Instantiate the byte array

        try {
            // Open the output streams that will be used to convert the UserID into a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // Convert the object into a byte array
            oos.writeObject(userID);
            oos.flush();
            byteBuffer = baos.toByteArray();

            // Close the output streams
            baos.close();
            oos.close();
        } catch(IOException ex){
            ex.printStackTrace();
        }
        // Return the byte array of the UserID
        return byteBuffer;
    }


    /**
     * Convert a Serialized UserID byte array into a UserID
     *
     * @param byteBuffer A serialized UserID byte array
     * @return A de-serialized UserID
     */
    public static UserID convertToUserID(byte [] byteBuffer){
        Object object = null; // Instantiate object

        try {
            // Open the input Streams that will be used to convert a byte array into a UserID
            ByteArrayInputStream bain = new ByteArrayInputStream(byteBuffer);
            ObjectInputStream in = new ObjectInputStream(bain);

            // Convert the byte array into the object
            object = in.readObject();

            // Close the input streams
            in.close();
            bain.close();
        }
        // Print the stack trace when either an IOException or ClassNotFoundException is caught
        catch(Exception ex){ ex.printStackTrace(); }

        // Return UserID contained within the byte array
        return (UserID)object;
    }


    /**
     * Method prints UserID
     */
    public void printUID(){
        // Print UserID information
        System.out.print("UserID Name: " + name + " Champion Name: " + champion + "\nipAddr: " + ipAddr.toString() +
                " port: " + port);
        System.out.println();
    }
}
