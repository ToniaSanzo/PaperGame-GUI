package PaperGame.entities;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This Class is used as a super class for creatures that are going to be used in battle.
 */
public class Creature implements Serializable, TransferredObject {
    private static HashMap<String, Creature> creatureCollection;  // Class HashMap containing every saved creature

    // The stats of a creature used in combat encounters
    private String name;                // The creatures name
    private short strength;             // How much damage a creature does with a knight weapon
    private short agility;              // Determine when you start your turn, damage with rogue weapons
    private short intelligence;         // Damage with wizard items, and number of spells you can learn
    private short fortitude;            // Amount of health, and general resistance
    private short totalHealth;          // totalHealth is the maximum amount of life you can have
    private short currentHealth;        // currentHealth is the amount of life you currently have
    private short totalMana;            // totalMana is the maximum amount of mana you can have
    private short currentMana;          // currentMana is the amount of mana you currently have
    private short totalEnergy;          // totalEnergy is the maximum amount of energy you can have
    private short currentEnergy;        // currentEnergy is the amount of energy you currently have

    // Creature's items
    private Inventory inventory;  // Items in the creature's possession
    private Weapon weapon;              // Weapon the creature uses in combat


    /**
     * Constructor, for creating a new creature, and establishing all its stats
     *
     * @param strength Strength determines damage dealt with knight weapons, and inventory weight
     * @param agility Agility determines turn order, damage with rogue weapons, and movement speed
     * @param intelligence Intelligence determines damage dealt with wizard items, and trap detection
     * @param fortitude Fortitude is used as a measure of total health and resistance.
     * @param name Name is the creatures name
     */
    public Creature(short strength,short agility, short intelligence, short fortitude, String name){
        inventory         = new Inventory();
        this.strength     = strength;
        this.agility      = agility;
        this.intelligence = intelligence;
        this.fortitude    = fortitude;
        this.name         = name;

        updateTotalHealth();
        currentHealth = totalHealth;
    }


    /**
     * Constructor, for creating a new creature, establishing all its stats, and the weapon it will use
     *
     * @param strength Strength determines damage dealt with knight weapons, and inventory weight
     * @param agility Agility determines turn order, damage with rogue weapons, and movement speed
     * @param intelligence Intelligence determines damage dealt with wizard items, and trap detection
     * @param fortitude Fortitude is used as a measure of total health and resistance
     * @param weapon Weapon the creature will have equipped
     * @param name Name is the creatures name
     */
    public Creature(short strength,short agility, short intelligence, short fortitude, Weapon weapon, String name){
        inventory         = new Inventory();
        this.strength     = strength;
        this.agility      = agility;
        this.intelligence = intelligence;
        this.fortitude    = fortitude;
        this.name         = name;

        equipWeapon(weapon);
        updateTotalHealth();
        currentHealth = totalHealth;
    }


    /**
     * Default Constructor sets all the stats equal to one
     */
    public Creature(){
        inventory         = new Inventory();
        this.strength     = 0;
        this.agility      = 0;
        this.intelligence = 0;
        this.fortitude    = 1;
        this.weapon       = null;
        this.name         = "wild turtle";

        updateTotalHealth();
        currentHealth = totalHealth;
    }


    /**
     * @return returns the creatures strength
     */
    public short getStrength(){
        return strength;
    }


    /**
     * @return returns the creatures agility
     */
    public short getAgility(){
        return agility;
    }


    /**
     * @return returns the creatures intelligence
     */
    public short getIntelligence(){
        return intelligence;
    }


    /**
     * @return returns the creatures fortitude
     */
    public short getFortitude(){
        return fortitude;
    }


    /**
     * @return returns the creatures total health
     */
    public short getTotalHealth() { return totalHealth; }


    /**
     * @return returns the creatures current health
     */
    public short getCurrentHealth() { return currentHealth; }

    /**
     * @return returns the creatures total mana
     */
    public short getTotalMana() { return totalMana; }


    /**
     * @return returns the creature's current mana
     */
    public short getCurrentMana() { return currentMana; }


    /**
     * @return returns the creature's total energy
     */
    public short getTotalEnergy() { return totalEnergy; }


    /**
     * @return returns the creature's current energy
     */
    public short getCurrentEnergy() { return currentEnergy; }


    /**
     * @return returns the creature's inventory
     */
    public Inventory getInventory() { return inventory; }


    /**
     * @return returns the creatures weapon
     */
    public Weapon getWeapon() { return weapon; }


    /**
     * @return returns the creatures name
     */
    public String getName() { return name; }


    /**
     * @param strength1 sets strength equal to strength1
     */
    public void setStrength(short strength1){
        strength = strength1;
    }


    /**
     * @param agility1 sets the agility equal to agility1
     */
    public void setAgility(short agility1){
        agility = agility1;
    }


    /**
     * @param intelligence1 sets the intelligence equal to intelligence1
     */
    public void setIntelligence(short intelligence1){
        intelligence = intelligence1;
    }


    /**
     * @param fortitude1 sets the fortitude equal to fortitude1
     */
    public void setFortitude(short fortitude1){
        fortitude = fortitude1;
        updateTotalHealth();
    }


    /**
     * @param weapon1 the creature's new weapon is weapon1
     */
    public void setWeapon(Weapon weapon1) { weapon = weapon1; }


    /**
     * @param name1 the creatures name is equal to name1
     */
    public void setName(String name1){
        name = name1;
    }


    /**
     * @param totalHealth1 the creatures total health is set equal to totalHealth1
     */
    public void setTotalHealth(short totalHealth1){
        if(totalHealth1 < currentHealth)
            currentHealth = totalHealth1;
        totalHealth = totalHealth1;
    }


    /**
     * @param currentHealth1 the creatures current health is set equal to currentHealth1
     */
    public void setCurrentHealth(short currentHealth1){
        currentHealth = currentHealth1;
    }


    /**
     * @param totalMana1 the creatures total mana is set equal to totalMana1
     */
    public void setTotalMana(short totalMana1) {
        if(totalMana1 < currentMana)
            currentMana = totalMana1;
        totalMana = totalMana1;
    }


    /**
     * @param currentMana1 the creatures currentMana is set equal to currentMana1
     */
    public void setCurrentMana(short currentMana1) { currentMana = currentMana1; }


    /**
     * @param totalEnergy1 the creatures totalEnergy is set equal to totalEnergy1
     */
    public void setTotalEnergy(short totalEnergy1){
        if(totalEnergy1 < currentEnergy)
            currentEnergy = totalEnergy1;
        totalEnergy = totalEnergy1;
    }


    /**
     * @param currentEnergy1 the creatures currentEnergy is set equal to currentEnergy1
     */
    public void setCurrentEnergy(short currentEnergy1) { currentEnergy = currentEnergy1; }


    /**
     * Method that increments the strength stat by a specified amount
     *
     * @param temp a short that is used to change the strength stat
     *             by a specified amount.
     */
    public void changeStrength(short temp){
        strength += temp;
        updateTotalEnergy();
    }


    /**
     * Method that increments the agility stat by a specified amount
     *
     * @param temp a short that is used to change the agility stat
     *             by a specified amount.
     */
    public void changeAgility(short temp){
        agility += temp;
        updateTotalEnergy();
    }


    /**
     * Method that changes the intelligence stat by a specified amount
     *
     * @param temp a short that is used to change the intelligence stat
     *             by a specified amount.
     */
    public void changeIntelligence(short temp){
        intelligence += temp;
        updateTotalMana();
    }


    /**
     * Method that changes the fortitude stat by a specified amount
     *
     * @param temp a short that is used to change the fortitude stat
     *             by a specified amount.
     */
    public void changeFortitude(short temp){
        fortitude += temp;
        updateTotalHealth();
    }


    /**
     * Depending on whether the argument is positive or negative number calls the necessary method to increase or
     * decrease the creature's health
     *
     * @param temp Value used to change the health of the creature
     */
    public void changeHealth(short temp){
        // Increase or decrease the current health based on the sign of the parameter variable
        if(temp > 0)
            increaseCurrentHealth(temp);
        else if(decreaseCurrentHealth((short)(temp * -1)))
            return;
        else
            System.out.println(name + " HAS BEEN KILLED!");
    }


    /**
     * Depending on whether the argument is positive or negative number calls the necessary method to increase or
     * decrease the creature's energy
     *
     * @param temp Value used to change the energy of the creature
     */
    public void changeEnergy(short temp){
        // Increase or decrease the current energy based on the sign of the parameter variable
        if(temp > 0)
            increaseCurrentEnergy(temp);
        else if(decreaseCurrentEnergy((short)(temp * -1)))
            return;
        else {
            currentEnergy = 0;
            System.out.println(name + " energy's drained");
        }
    }


    /**
     * Depending on whether the argument is positive or negative number calls the necessary method to increase or
     * decrease the creature's mana
     *
     * @param temp Value used to change the mana of the creature
     */
    public void changeMana(short temp){
        // Increase or decrease the current mana based on the sign of the parameter variable
        if(temp > 0)
            increaseCurrentMana(temp);
        else if(decreaseCurrentMana((short)(temp * -1)))
            return;
        else {
            currentMana = 0;
            System.out.println(name + " mana's drained");
        }
    }


    /**
     * Increases the strength stat by one
     */
    public void incrementStrength(){
        strength++;
        updateTotalEnergy();
    }


    /**
     * Increases the agility stat by one
     */
    public void incrementAgility(){
        agility++;
        updateTotalEnergy();
    }


    /**
     * Increases the intelligence stat by one
     */
    public void incrementIntelligence(){
        intelligence++;
        updateTotalMana();
    }


    /**
     * Increases the fortitude stat by one
     */
    public void incrementFortitude(){
        fortitude++;
        updateTotalHealth();
    }


    /**
     * Decreases the strength stat by one
     */
    public void decrementStrength() {
        strength--;
        updateTotalEnergy();
    }


    /**
     * Decreases the agility stat by one
     */
    public void decrementAgility(){
        agility--;
        updateTotalEnergy();

    }


    /**
     * Decreases the intelligence stat by one
     */
    public void decrementIntelligence(){
        intelligence--;
        updateTotalMana();
    }


    /**
     * Decreases the fortitude stat by one
     */
    public void decrementFortitude(){
        fortitude--;
        updateTotalHealth();
    }


    /**
     * Given any equipment updates the creatures stats to reflect the changes, and sets the creatures
     * weapon to the weapon given in the parameter
     *
     * @param weapon Equips the parameter weapon to the champion
     */
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
     * Methof that changes the totalEnergy relative to changes made to the strength, or agility stat
     */
    public void updateTotalEnergy(){
        totalEnergy = (short)(((agility + strength) >> 1) * 3);
        if(currentEnergy > totalEnergy)
            currentEnergy = totalEnergy;
    }


    /**
     * Method that changes the totalMana relative to changes made to the intelligence stat
     */
    public void updateTotalMana(){
        totalMana = (short)(intelligence * 3);
        if(currentMana > totalMana)
            currentMana = totalMana;
    }


    /**
     * Method that changes the totalHealth relative to changes made to the fortitude stat
     */
    public void updateTotalHealth(){
        totalHealth = (short)(fortitude * 3);
        if(currentHealth > totalHealth)
            currentHealth = totalHealth;
    }


    /**
     * Increases the health by the amount specified in the parameters
     *
     * @param healthRecovery a positive short, that represents the amount of health recovered.
     */
    public void increaseCurrentHealth(short healthRecovery){
        // Make sure that healthRecovery is a positive short
        if(healthRecovery < 1)
            return;

        // If the creature recovered more health than the max than the current health's set to the max health
        if(currentHealth + healthRecovery > totalHealth) {
            currentHealth = totalHealth;
            return;
        }

        // The current health is incremented by the healthRecovery
        currentHealth += healthRecovery;
    }


    /**
     * Decreases the health by the amount specified in the parameters
     *
     * @param healthLost a positive short, that represents the amount of health lost
     * @return If the health drops below 0, the method returns false, otherwise return true.
     */
    public boolean decreaseCurrentHealth(short healthLost){
        // Make sure that healthLost is a positive short
        if(healthLost < 1)
            return true;

        // The current health is decremented by the healthLost
        currentHealth -= healthLost;

        // If the currentHealth is 0 or less returns false, otherwise
        // it will return true
        if(currentHealth < 1)
            return false;
        else
            return true;
    }


    /**
     * Increases the mana by the amount specified in the parameters
     *
     * @param manaRecovery a positive short, that represents the amount of mana recovered.
     */
    public void increaseCurrentMana(short manaRecovery){
        // Make sure that manaRecovery is a positive short
        if(manaRecovery < 1)
            return;

        // If the creature recovered more mana than the max than the current mana's set to the max mana
        if(currentMana + manaRecovery > totalMana) {
            currentMana = totalMana;
            return;
        }

        // The current health is incremented by the manaRecovery
        currentMana += manaRecovery;
    }


    /**
     * Decreases the mana by the amount specified in the parameters
     *
     * @param manaLost a positive short, that represents the amount of mana lost
     * @return If the mana would drop below 0 the method returns false, otherwise return true and decrement the mana.
     */
    public boolean decreaseCurrentMana(short manaLost){
        // Make sure that manaLost is a positive short
        if(manaLost < 1)
            return false;

        // If the currentMana is 0 or less returns false, otherwise it will return true
        if((currentMana - manaLost) < 1) {
            return false;
        }
        else {
            currentMana -= manaLost;
            return true;
        }
    }


    /**
     * Increases the energy by the amount specified in the parameters
     *
     * @param energyRecovery a positive short, that represents the amount of energy recovered.
     */
    public void increaseCurrentEnergy(short energyRecovery){
        // Make sure that energyRecovery is a positive short
        if(energyRecovery < 1)
            return;

        // If the creature recovered more energy than the max than the current energy's set to the max energy
        if((currentEnergy + energyRecovery) > totalEnergy) {
            currentEnergy = totalEnergy;
            return;
        }

        // The current energy is incremented by the energyRecovery
        currentEnergy += energyRecovery;
    }


    /**
     * Decreases the energy by the amount specified in the parameters
     *
     * @param energyLost a positive short, that represents the amount of energy lost
     * @return If the energy would drop below 0 the method returns false, otherwise return true and decrement the energy
     */
    public boolean decreaseCurrentEnergy(short energyLost){
        // Make sure that energyLost is a positive short
        if(energyLost < 1)
            return false;

        // If the currentEnergy is 0 or less returns false, otherwise it will return true
        if((currentEnergy - energyLost) < 1) {
            return false;
        }
        else {
            currentEnergy -= energyLost;
            return true;
        }
    }


    /**
     * Add an item to the inventory
     *
     * @param item The item to be added to the inventory
     */
    public void addItem(Item item){ inventory.addItem(item); }


    /**
     * Remove an item from the inventory
     *
     * @param item The item to be removed from the inventory
     */
    public void removeItem(Item item){ inventory.removeItem(item, 1); }


    /**
     * Remove an item from the inventory
     *
     * @param item The item to be removed from the inventory
     * @param quantity The number of items to remove
     */
    public void removeItem(Item item, int quantity){ inventory.removeItem(item, quantity); }


    /**
     * @return String "Creature"
     */
    @Override
    public String getType(){ return CREATURE; }


    /**
     * Convert a Creature into a byte array
     *
     * @param creature Creature that will be converted into a byte array
     * @return Returns the byte array of the converted Creature
     */
    public static byte[] convertToBytes(Creature creature){
        byte [] byteBuffer = null;  // Instantiate the byte array

        try {
            // Open the output streams that will be used to convert the Creature into a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // Convert the object into a byte array
            oos.writeObject(creature);
            oos.flush();
            byteBuffer = baos.toByteArray();

            // Close the output streams
            baos.close();
            oos.close();
        } catch(IOException ex){
            ex.printStackTrace();
        }
        // Return the byte array of the Creature
        return byteBuffer;
    }


    /**
     * Convert a Serialized Creature's byte array into a Creature
     *
     * @param byteBuffer A serialized byte array containing a creature
     * @return Returns a de-serialized Creature
     */
    public static Creature convertToCreature(byte [] byteBuffer){
        Object object = null; // Instantiate object

        try {
            // Open the input Streams that will be used to convert a byte array into a Creature
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

        // Return Creature contained within the byte array
        return (Creature)object;
    }


    /**
     * Method prints the stats and the name of the Creature
     */
    public void printCreature(){
        // Print Creature information
        System.out.println("Strength: " + getStrength() + " Agility: " + getAgility() + " Intelligence " +
                getIntelligence() + " Fortitude: " + getFortitude() + "\nTotal Health: " + getTotalHealth() +
                " Current Health: " + getCurrentHealth() + " Total Mana: " + getTotalMana() + " Current Mana: " +
                getCurrentMana() + " Total Energy: " + getTotalEnergy() + " Current Energy: " + getCurrentEnergy() +
                "\nName: " + getName());
    }
}