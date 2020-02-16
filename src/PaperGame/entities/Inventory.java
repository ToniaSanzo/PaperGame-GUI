package PaperGame.entities;

import java.io.*;
import java.util.ArrayList;

public class Inventory implements Serializable, TransferredObject {
    private ArrayList<Item> itemList;                     // Collection of items in the inventory


    /**
     * Constructor Method, initializes the Map collection for use
     */
    public Inventory(){
        itemList = new ArrayList<Item>();
    }


    /**
     * Add item/s to Inventory
     *
     * @param item 0 or more item objects
     */
    public void addItem(Item ... item){ for(Item i: item){ itemList.add(i); }}


    /**
     * Get Inventory's list
     *
     * @return Inventory's list
     */
    public ArrayList<Item> getItemList(){ return itemList; }


    /**
     * Set Inventory's list
     *
     * @param itemList Updates Inventory's list to the parameter
     */
    public void setItemList(ArrayList<Item> itemList){ this.itemList = itemList; }


    /**
     * @return Object type (String Representation)
     */
    @Override
    public String getType(){ return INVENTORY; }

    /**
     * Convert Inventory to a byte array
     *
     * @param inventory Inventory that will be converted into a byte array
     * @return Returns the byte array of the converted Inventory
     */
    public static byte[] convertToBytes(Inventory inventory){
        byte [] byteBuffer = null;  // Instantiate the byte array

        try {
            // Open the output streams that will be used to convert the Inventory into a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // Convert the object into a byte array
            oos.writeObject(inventory);
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
     * Convert a Serialized Inventory byte array into a Inventory
     *
     * @param byteBuffer A serialized Inventory byte array
     * @return A de-serialized Inventory
     */
    public static Inventory convertToInventory(byte [] byteBuffer){
        Object object = null; // Instantiate object

        try {
            // Open the input Streams that will be used to convert a byte array into a Inventory
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

        // Return Inventory contained within the byte array
        return (Inventory)object;
    }


    /**
     * Method prints items in Inventory
     */
    public void printInventory(){
        // Print Inventory information
        System.out.print("Inventory contains: ");
        for(int i = 0; i < itemList.size(); i++){ System.out.print(itemList.get(i).getName() + " "); }
        System.out.println();
    }
}