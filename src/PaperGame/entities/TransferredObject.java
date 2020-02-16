package PaperGame.entities;

import java.io.Serializable;

// Interface for objects transferred between client and server
public interface TransferredObject extends Serializable {
    String INVENTORY = "Inventory", CONSUMABLE = "Consumable", WEAPON = "Weapon", ARMOR ="Armor", TRAP = "Trap",
            CREATURE = "Creature", CHAMPION = "Champion",COMBAT_MAP = "Combat Map";


    public String getType(); /* Return's Object Type*/
}
