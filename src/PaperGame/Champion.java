package PaperGame;

import java.lang.Math;
import java.util.HashMap;

/**
 * A Subclass of the Creature class, Represents a character
 */
public class Champion extends Creature {
    private static final String ARCHER = "Archer", WARRIOR = "Warrior", MAGE = "Mage", PALADIN = "Paladin";
    private static final String ELF = "Elf", DWARF = "Dwarf", ORC = "Orc", HUMAN = "Human";
    private static HashMap<String, Champion> championCollection;

    private String championClass;        // The Champions class (Archer, Warrior, Mage, or Paladin)
    private String championRace;         // The Champions race (Elf, Dwarf, Orc, or Human)
    private Armor champHeadGear;         // The Champions head piece of Armor
    private Armor champTorso;            // The Champions torso piece of Armor
    private Armor champPants;            // The Champions pants piece of Armor
    private Armor champBoots;            // The Champions boots piece of Armor
    private Armor champGloves;           // The Champions gloves piece of Armor
    private Armor champJewelry;          // The Champions jewelry piece of Armor
    private short gold;                  // The Champions total gold
    private short level;                 // The Champions current level
    private int experiencePts;           // The Champions current experience points
    private int currentInventoryWeight;  // The Champions current inventory weight
    private int totalInventoryWeight;    // The Champions total inventory capacity


    /**
     * Constructor for the Champion class, this will be used to create a unique champion.
     *
     * @param championClass Represents the champion's class, e.g) Archer
     * @param championRace Represents the champion's race, e.g) Dwarf
     * @param championName Represents the champion's name
     */
    public Champion(String championClass,String championRace, String championName){
        super((short)2,(short)2,(short)2,(short)2,championName);
        this.championClass  = championClass;
        this.championRace   = championRace;
        this.champHeadGear  = null;
        this.champTorso     = null;
        this.champPants     = null;
        this.champBoots     = null;
        this.champGloves    = null;
        this.champJewelry   = null;

        if(championClass.equals(ARCHER)) {
            changeAgility((short)2);
            // add equipment here eventually
        }

        if(championClass.equals(WARRIOR)) {
            changeStrength((short)2);
            // add equipment here eventually
        }

        if(championClass.equals(MAGE)) {
            changeIntelligence((short)2);
            // add equipment here eventually
        }

        if(championClass.equals(PALADIN)) {
            changeFortitude((short)2);
            // add equipment here eventually
        }

        if(championRace.equals(ELF)) {
            changeAgility((short)2);
            decrementFortitude();
        }

        if(championRace.equals(DWARF)) {
            changeFortitude((short)2);
            decrementAgility();
        }

        if(championRace.equals(ORC)) {
            changeStrength((short)2);
            decrementIntelligence();
        }

        if(championRace.equals(HUMAN)) {
            changeIntelligence((short)2);
            decrementStrength();
        }
    }


    /**
     * Given any piece of equipment (Weapon or Armor) will unequip the currently equipped corresponding
     * equipment piece and replace it with the equipment specified in the parameter
     *
     * @param equipment A piece of equipment that belongs to either the Armor subclass, or the Weapon subclass
     */
    public void equip(Equipment equipment){
        if(equipment.getType().equals("Weapon")) {
            if (getWeapon() != null)
                unequipWeapon();
            equipWeapon((Weapon)equipment);
        }
        else if(equipment.getType().equals("Armor")) {
            equipArmor((Armor)equipment);
        }

        // updates the total health to reflect the changes made by equipping the new equipment object
        updateTotalHealth();
    }


    /**
     * Removes all the weapon stats bonuses and resets the flags of the weapon, and sets the champions weapon
     * equal to null.
     */
    public void unequipWeapon(){
        // Reset the champions stats
        changeStrength((short)(getWeapon().getStrength()*-1));
        changeAgility((short)(getWeapon().getAgility()*-1));
        changeIntelligence((short)(getWeapon().getIntelligence()*-1));
        changeFortitude((short)(getWeapon().getFortitude()*-1));

        // sets the champions health to reflect the changes made to fortitude
        updateTotalHealth();

        // sets the champion's weapon to null
        setWeapon(null);
    }


    /**
     * Given any equipment updates the characters stats to reflect the changes, and sets the champions
     * weapon to the weapon given in the parameter
     *
     * @param weapon Equips the parameter weapon to the champion
     */
    @Override
    public void equipWeapon(Weapon weapon){
        // Sets the champions stats
        changeStrength(weapon.getStrength());
        changeAgility(weapon.getAgility());
        changeIntelligence(weapon.getIntelligence());
        changeFortitude(weapon.getFortitude());

        // Champion weapon is updated to the parameter weapon
        setWeapon(weapon);
    }


    /**
     * Equips any given piece of armor, and if the champion already has the same type of armor equipped,
     * this function will replace the already equipped armor piece.
     *
     * @param armor Armor being equipped to the champion
     */
    public void equipArmor(Armor armor){
        String type = armor.getArmorType();
        switch(type) {
            case "Head":
                unequipHead();
                changeStrength((short)(armor.getStrength()));
                changeAgility((short)(armor.getAgility()));
                changeFortitude((short)(armor.getFortitude()));
                changeIntelligence((short)(armor.getIntelligence()));
                champHeadGear = armor;
                break;
            case "Torso":
                unequipTorso();
                changeStrength((short)(armor.getStrength()));
                changeAgility((short)(armor.getAgility()));
                changeFortitude((short)(armor.getFortitude()));
                changeIntelligence((short)(armor.getIntelligence()));
                champTorso = armor;
                break;
            case "Pants":
                unequipPants();
                changeStrength((short)(armor.getStrength()));
                changeAgility((short)(armor.getAgility()));
                changeFortitude((short)(armor.getFortitude()));
                changeIntelligence((short)(armor.getIntelligence()));
                champPants = armor;
                break;
            case "Boots":
                unequipBoots();
                changeStrength((short)(armor.getStrength()));
                changeAgility((short)(armor.getAgility()));
                changeFortitude((short)(armor.getFortitude()));
                changeIntelligence((short)(armor.getIntelligence()));
                champBoots = armor;
                break;
            case "Gloves":
                unequipGloves();
                changeStrength((short)(armor.getStrength()));
                changeAgility((short)(armor.getAgility()));
                changeFortitude((short)(armor.getFortitude()));
                changeIntelligence((short)(armor.getIntelligence()));
                champGloves = armor;
                break;
            case "Jewellery":
                unequipJewelry();
                changeStrength((short)(armor.getStrength()));
                changeAgility((short)(armor.getAgility()));
                changeFortitude((short)(armor.getFortitude()));
                changeIntelligence((short)(armor.getIntelligence()));
                champJewelry = armor;
                break;
        }
    }


    /**
     * Removes all the stat changes any given armor piece would provide
     *
     * @param armor The armor that the champion would like to remove
     */
    public void unequipArmor(Armor armor, int scenario){
        changeStrength((short)(armor.getStrength()*-1));
        changeAgility((short)(armor.getAgility()*-1));
        changeIntelligence((short)(armor.getIntelligence()*-1));
        changeFortitude((short)(armor.getFortitude()*-1));
        updateTotalHealth();
        switch(scenario){
            case 0:
                champHeadGear = null;
                break;
            case 1:
                champTorso    = null;
                break;
            case 2:
                champPants    = null;
                break;
            case 3:
                champBoots    = null;
                break;
            case 4:
                champGloves   = null;
                break;
            case 5:
                champJewelry  = null;
                break;
        }
    }


    /**
     * Easily remove the head armor piece
     */
    public void unequipHead(){
        if(champHeadGear != null){ unequipArmor(champHeadGear,0); }
    }


    /**
     * Easily removes the torso armor piece
     */
    public void unequipTorso(){
        if(champTorso != null){ unequipArmor(champTorso,1); }
    }


    /**
     * Easily removes the pants armor piece
     */
    public void unequipPants(){
        if(champPants != null){ unequipArmor(champPants,2); }
    }


    /**
     * Easily removes the boots armor piece
     */
    public void unequipBoots(){
        if(champBoots != null){ unequipArmor(champBoots,3); }
    }


    /**
     * Easily removes the gloves armor piece
     */
    public void unequipGloves(){
        if(champGloves != null){ unequipArmor(champGloves,4); }
    }


    /**
     * Easily removes the jewellery armor piece
     */
    public void unequipJewelry(){
        if(champJewelry != null){ unequipArmor(champJewelry,5); }
    }


    /**
     * Method that changes the gold stat by a specified amount
     *
     * @param temp Amount to change the gold stat by
     */
    public void changeGold(short temp){ gold += temp; }


    /**
     * Method that changes the experiencePts stat by a specified amount
     *
     * @param temp Amount to change the experiencePts stat by
     */
    public void changeExperiencePts(int temp){ experiencePts += temp; }


    /**
     * Given a string that is a character stat will simulate rolling a d20, and adding the stat modifier
     * on top of that roll
     *
     * @param checkType A String that is one of the champion stats(e.g "Agility")
     * @return returns a random integer between 1-20 with the stat modifier added on top of it
     */
    public int skillCheckd20(String checkType){
        int skillCheck = -1;
        switch(checkType){
            case "Strength":
                skillCheck = ((int)(Math.random() * 20) + 1 + getStrength());
                break;
            case "Agility":
                skillCheck = ((int)(Math.random() * 20) + 1 + getAgility());
                break;
            case "Intelligence":
                skillCheck = ((int)(Math.random() * 20) + 1 + getIntelligence());
                break;
            case "Fortitude":
                skillCheck = ((int)(Math.random() * 20) + 1 + getFortitude());
                break;
            default:
                System.out.println("Invalid Skill Check, Parameter not a champion stat");
                break;
        }
        return skillCheck;
    }


    /**
     * Given a string that is a character stat will simulate rolling a d6, and adding the stat modifier
     * on top of that roll.
     *
     * @param checkType A String that is one of the champion stats(e.g "Agility")
     * @return returns a random integer between 1-6 with the stat modifier added on top of it
     */
    public int skillCheckd6(String checkType){
        int skillCheck = -1;
        switch(checkType){
            case "Strength":
                skillCheck = ((int)(Math.random() * 6) + 1 + getStrength());
                break;
            case "Agility":
                skillCheck = ((int)(Math.random() * 6) + 1 + getAgility());
                break;
            case "Intelligence":
                skillCheck = ((int)(Math.random() * 6) + 1 + getIntelligence());
                break;
            case "Fortitude":
                skillCheck = ((int)(Math.random() * 6) + 1 + getFortitude());
                break;
            default:
                System.out.println("Invalid Skill Check, Parameter not a champion stat");
                break;
        }
        return skillCheck;
    }


    /**
     * Given a string that is a character stat will simulate rolling a d12, and adding the stat modifier
     * on top of that roll.
     *
     * @param checkType A String that is one of the champion stats(e.g "Agility")
     * @return returns a random integer between 1-12 with the stat modifier added on top of it
     */
    public int skillCheckd12(String checkType){
        int skillCheck = -1;
        switch(checkType){
            case "Strength":
                skillCheck = ((int)(Math.random() * 12) + 1 + getStrength());
                break;
            case "Agility":
                skillCheck = ((int)(Math.random() * 12) + 1 + getAgility());
                break;
            case "Intelligence":
                skillCheck = ((int)(Math.random() * 12) + 1 + getIntelligence());
                break;
            case "Fortitude":
                skillCheck = ((int)(Math.random() * 12) + 1 + getFortitude());
                break;
            default:
                System.out.println("Invalid Skill Check, Parameter not a champion stat");
                break;
        }
        return skillCheck;
    }


    /**
     * Returns the champion's race.
     *
     * @return championRace - a String that represents the current champions race
     */
    public String getRace(){
        return championRace;
    }


    /**
     * Returns the champion's class
     *
     * @return championClass - a String that represents the currents champions class
     */
    public String getChampionClass(){
        return championClass;
    }


    /**
     * Depending on the instance variables of the consumable object, will update the Champions statistics based on those
     * values, and remove the consumable from the Champions inventory
     *
     * @param consumable Consumable object that will be used to change the stats of the champion and than be removed
     *                   from the Champions inventory
     */
    public void consumeConsumable(Consumable consumable){
        // Change the strength variable if necessary
        if(consumable.getStrength() != 0){ super.changeStrength(consumable.getStrength());}
        // Change the agility variable if necessary
        if(consumable.getAgility() != 0){ super.changeAgility(consumable.getAgility());}
        // Change the intelligence variable if necessary
        if(consumable.getIntelligence() != 0){ super.changeIntelligence(consumable.getIntelligence());}
        // Change the fortitude variable if necessary
        if(consumable.getFortitude() != 0){ super.changeFortitude(consumable.getFortitude());}
        // Change the health variable if necessary
        if(consumable.getHealth() != 0){ super.changeHealth(consumable.getHealth());}
        // Change the energy variable if necessary
        if(consumable.getEnergy() != 0){ super.changeEnergy(consumable.getEnergy());}
        // Change the mana variable if necessary
        if(consumable.getMana() != 0){ super.changeMana(consumable.getEnergy());}
        // Change the gold variable if necessary
        if(consumable.getGold() != 0){ changeGold(consumable.getGold());}
        // Change experiencePts variable if necessary
        if(consumable.getExperiencePts() != 0 ){ changeExperiencePts(consumable.getExperiencePts());}

        // Remove the consumable from the Champions inventory
        removeItem(consumable);
    }


    /**
     * Confirms that the champion has the capacity to carry the item, than adds the item to the inventory if they do
     *
     * @param item The item to be added to the inventory
     */
    @Override
    public void addItem(Item item){
        int tWeight = item.getWeight();
        if(tWeight + currentInventoryWeight > totalInventoryWeight){ return; }
        currentInventoryWeight += tWeight;
        super.addItem(item);
    }


    /**
     * Remove the item from the Champions inventory and reduce the current inventory weight capacity
     *
     * @param item The item to be removed from the inventory
     */
    @Override
    public void removeItem(Item item){
        int tWeight = item.getWeight();
        currentInventoryWeight -= tWeight;
        super.removeItem(item);
    }


    /**
     * Overrides the toString method, this will print an instantiated champion with
     * this style NAME the RACE CLASS.
     *
     * @return Returns a string that is the champion's title
     */
    @Override
    public String toString(){
        String rtnStr = getName() + " the " + championRace + " " +  championClass;
        return rtnStr;
    }
}
