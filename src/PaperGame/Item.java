package PaperGame;


public interface Item {
    // What subclass the item belongs too
    String CONSUMABLE = "Consumable", WEAPON = "Weapon", ARMOR ="Armor";
    // Different types of armor pieces
    String HEAD = "Head", TORSO = "Torso", PANTS = "Pants", BOOTS= "Boots", GLOVES = "Gloves", JEWELRY = "Jewelry";
    // Different types of weapons that can be used
    String KNIGHT = "Knight", ROGUE = "Rogue", WIZARD = "WIZARD";

    int getWeight();   // Returns the weight of the Item
    String getType();  // Returns the subclass of the Item
    String getName();  // Returns Item's name
}
