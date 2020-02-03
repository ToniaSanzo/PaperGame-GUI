package PaperGame;


import java.io.*;

public class Armor extends Equipment {
    // A String that represents the type of armor being used (e.g. boots | gloves | etc.)
    private String armorType;


    /**
     * Constructor for a armor piece that establishes the armors stats, name and type of the piece of armor
     *
     * @param strength Measure of the stat change to strength this piece of armor provides
     * @param agility Measure of the stat change to agility this piece of armor provides
     * @param intelligence Measure of the stat change to intelligence this piece of armor provides
     * @param fortitude Measure of the stat change to fortitude this piece of armor provides
     * @param weight Measure's how much inventory weight this piece of armor takes up
     * @param name The name of the piece of armor
     * @param armorType Which type of armor this piece belongs too (e.g. boots | gloves | etc.)
     */
    public Armor(short strength, short agility, short intelligence, short fortitude, int weight, String name,
                 String armorType){
        super(name,strength,agility,intelligence,fortitude,weight);
        this.armorType = armorType;
    }


    /**
     * Default constructor, stats all set to 0 and the name and type are set to the [empty string := ""]
     */
    public Armor(){
        super();
        armorType = "";
    }


    /**
     * @return Returns the string "Armor" so other classes will know which class this item belongs to
     */
    public String getType() { return ARMOR; }


    /**
     * @return Returns a string that signifies what type of armor this armor piece is, (e.g. boots | gloves | etc.)
     */
    public String getArmorType() { return armorType; }


    /**
     * Convert Armor into a byte array
     *
     * @param armor Armor that will be converted into a byte array
     * @return Returns the byte array of the converted Armor
     */
    public static byte[] convertToBytes(Armor armor){
        byte [] byteBuffer = null;  // Instantiate the byte array

        try {
            // Open the output streams that will be used to convert the Armor into a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // Convert the object into a byte array
            oos.writeObject(armor);
            oos.flush();
            byteBuffer = baos.toByteArray();

            // Close the output streams
            baos.close();
            oos.close();
        } catch(IOException ex){
            ex.printStackTrace();
        }
        // Return the byte array of the Armor
        return byteBuffer;
    }


    /**
     * Convert a Serialized Armor's byte array into Armor
     *
     * @param byteBuffer A serialized byte array containing Armor
     * @return Returns a de-serialized Armor
     */
    public static Armor convertToArmor(byte [] byteBuffer){
        Object object = null; // Instantiate object

        try {
            // Open the input Streams that will be used to convert a byte array into Armor
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

        // Return Armor contained within the byte array
        return (Armor)object;
    }


    /**
     * Method prints the stats and the name of the Armor
     */
    public void printArmor(){
        // Print Armor information
        System.out.println("Strength: " + getStrength() + " Agility: " + getAgility() + " Intelligence " +
                getIntelligence() + " Fortitude: " + getFortitude() + "\nArmor Type: " + getArmorType() +
                " Weight: " + getWeight() + "\nName: " + getName());
    }
}
