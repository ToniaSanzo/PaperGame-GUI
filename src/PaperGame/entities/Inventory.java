package PaperGame.entities;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Inventory implements Serializable, TransferredObject, Iterable<Item> {

    // Iterator class
    class InventoryIterator implements Iterator<Item>{
        private int index = 0;


        /**
         * Return true if the iterator has more Items in it
         *
         * @return True if iterator has more Items in it, otherwise false
         */
        public boolean hasNext(){ return index < size(); }


        /**
         * Get the Item at index, increment index
         *
         * @return Item at index, increment index
         */
        public Item next(){ return get(index++); }


        /**
         * Unsupported Operation
         */
        public void remove() { throw new UnsupportedOperationException("not supported"); }
    }


    private ArrayList<Item> itemList;        // Collection of items in the inventory
    private ArrayList<String> itemNameList;  // Collection of items in the inventory
    private ArrayList<Integer> quantityList; // Quantity of each item in the inventory

    /**
     * Constructor Method, initializes the Map collection for use
     */
    public Inventory() {
        itemList = new ArrayList<Item>();
        itemNameList = new ArrayList<String>();
        quantityList = new ArrayList<Integer>();
    }


    /**
     * Add item/s to Inventory
     *
     * @param item 0 or more item objects
     */
    public void addItem(Item ... item){
        int index;

        for(Item i: item){
            if(itemList.contains(i)){
                // Increment the quantity of an item already in the inventory
                quantityList.set(itemList.indexOf(i), quantityList.get(itemList.indexOf(i)) + 1);
            } else {
                // Add a new item, set the quantity of the item to 1
                itemList.add(i);
                itemNameList.add(i.getName());
                quantityList.add(1);
            }
        }
    }


    /**
     * Add specified number of a single item to Inventory
     *
     * @param item item added
     * @param quantity quantity added
     */
    public void addItem(Item item, int quantity){

        if(itemList.contains(item)){
            // Increase the quantity of an item
            quantityList.set(itemList.indexOf(item), quantityList.get(itemList.indexOf(item)) + quantity);
        } else {
            // Add a new item, set the quantity of the item to the parameter "quantity"
            itemList.add(item);
            itemNameList.add(item.getName());
            quantityList.add(quantity);
        }
    }


    /**
     * Method to remove an item by a specified quantity
     *
     * @param item - Item being removed
     * @param quantity - quantity being removed
     * @return true if successfully removed the item, otherwise false
     */
    public boolean removeItem(Item item, int quantity){
        if(itemList.contains(item)){
            int index = itemList.indexOf(item);
            if(quantity > quantityList.get(index)){
                System.err.println("Attempting to remove a greater quantity, than the quantity in Inventory");
                return false;
            } else if(quantity == quantityList.get(index)){
                // Remove an item from the inventory
                quantityList.remove(index);
                itemList.remove(index);
                itemList.remove(index);
            } else {
                // Decrement the quantity of an item
                quantityList.set(index, quantityList.get(index) - quantity);
            }
            return true;
        } else {
            System.err.println("Inventory does not contain: " + item.getName());
            return false;
        }
    }


    /**
     * Return size of the Inventory
     *
     * @return Inventory size
     */
    public int size(){ return itemList.size(); }


    /**
     * Return's the Item at index i, otherwise throughs a NoSuchElementException
     *
     * @param i index of Item
     * @return The Item at index i
     */
    public Item get(int i){
        if(i >= 0 && i < size()){
            return itemList.get(i);
        }
        throw new NoSuchElementException();
    }


    /**
     * Returns the Inventory's iterator
     *
     * @return Inventory's iterator
     */
    public Iterator<Item> iterator(){
        return new InventoryIterator();
    }


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
     * Get Inventory's item name list
     *
     * @return Inventory's item name list
     */
    public ArrayList<String> getItemNameList(){ return itemNameList; }


    /**
     * Set Inventory's item name list
     *
     * @param itemNameList Updates Inventory's item name list to the parameter
     */
    public void setItemNameList(ArrayList<String> itemNameList){ this.itemNameList = itemNameList; }


    /**
     * Get Inventory's quantity list
     *
     * @return Inventory's quantity list
     */
    public ArrayList<Integer> getQuantityList(){ return quantityList; }


    /**
     * If an Item's name in the itemList matches the parameter string, return the index of the item. Otherwise, return
     * -1
     *
     * @param str Item's name
     * @return Index of item in itemList if present, otherwise -1 if not found
     */
    public int indexOf(String str){
        // Cycle through the itemList, to match str to an item's name
        for(String iStr: itemNameList){ if(str == iStr){ return itemNameList.indexOf(iStr); } }
        return -1;
    }


    /**
     * Set Inventory's quantity's list
     *
     * @param quantityList Updates Inventory's quantity list to the parameter
     */
    public void setQuantityList(ArrayList<Integer> quantityList){ this.itemList = itemList; }


    /**
     * @return Object type (String Representation)
     */
    @Override
    public String getType(){ return INVENTORY; }


    /**
     * Convert Inventory to byte array
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
     * Clear the content in an Inventory
     */
    public void clear(){
        itemList.clear();
        itemNameList.clear();
        quantityList.clear();
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