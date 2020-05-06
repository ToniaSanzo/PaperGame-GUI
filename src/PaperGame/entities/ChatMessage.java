package PaperGame.entities;

import java.io.*;

public class ChatMessage implements TransferredObject {

    private String name, message; // Name of user, and message being sent


    /**
     * Construct a chat message with a name and message
     *
     * @param name (String)    - Name of user sending message
     * @param message (String) - Message being sent
     */
    public ChatMessage(String name, String message){
        this.name = name;
        this.message = message;
    }


    /**
     * Determine data type of transferred object
     *
     * @return (String) - String representation of a ChatMessage
     */
    @Override
    public String getType(){
        return CHAT_MESSAGE;
    }


    /**
     * Convert ChatMessage to byte array
     *
     * @param chatMessage (ChatMessage) - ChatMessage being converted to a byte array
     * @return (byte[])                 - byte array of ChatMessage
     */
    public static byte[] convertToBytes(ChatMessage chatMessage){
        byte [] byteBuffer = null;  // Instantiate the byte array

        try {
            // Open the output streams that will be used to convert the ChatMessage to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // Convert the object into a byte array
            oos.writeObject(chatMessage);
            oos.flush();
            byteBuffer = baos.toByteArray();

            // Close the output streams
            baos.close();
            oos.close();
        } catch(IOException ex){
            ex.printStackTrace();
        }
        // Return the byte array of the Inventory
        return byteBuffer;
    }


    /**
     * Convert ChatMessage byte array to a ChatMessage
     *
     * @param byteBuffer (byte[]) - ChatMessage byte array
     * @return (ChatMessage)      - A de-serialized ChatMessage
     */
    public static ChatMessage convertToInventory(byte [] byteBuffer){
        Object object = null; // Instantiate object

        try {
            // Open the input Streams that will convert a byte array to a ChatMessage
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

        // Return ChatMessage contained within the byte array
        return (ChatMessage)object;
    }
}
