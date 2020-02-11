package PaperGame.networking;

import java.net.InetAddress;

public class UserID {
    private int port;            // This user's Port
    private String name;         // This user's name
    private long hashCode;       // This user's hash code
    private InetAddress ipAddr;  // This user's IP address


    /**
     * Construct a UserID object that connects a user's name, hash code, IP address, and port
     *
     * @param name User's name
     * @param hashCode User's hash code
     * @param ipAddr User's IP address
     * @param port User's port
     */
    public UserID(String name, long hashCode, InetAddress ipAddr, int port){
        this.name = name;
        this.hashCode = hashCode;
        this.ipAddr = ipAddr;
        this.port = port;
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
}
