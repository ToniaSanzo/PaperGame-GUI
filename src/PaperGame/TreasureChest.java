package PaperGame;

import java.util.HashMap;

public class TreasureChest {
    private static final String TREASURE_CHEST = "TreasureChest";  // String used to represent the type of object it is
    private HashMap<String, Item> itemMap;                         // Collection of items in the chest


    /**
     * Constructor Method, initializes the Map collection for use
     */
    public TreasureChest(){
        itemMap = new HashMap<String, Item>();
    }


    /**
     * Add a item to the TreasureChest
     *
     * @param item Item added to the TreasureChest
     */
    public void addItem(Item item){
        itemMap.put(item.getName(), item);
    }


    /**
     * Get the TreasureChest's HashMap
     *
     * @return Return TreasureChest's HashMap
     */
    public HashMap<String, Item> getItemMap(){ return itemMap; }


    /**
     * Set the TreasureChest's HashMap
     *
     * @param itemMap Updates the TreasureChest's HashMap to the parameter collection
     */
    public void setItemMap(HashMap<String, Item> itemMap){ this.itemMap = itemMap; }
}