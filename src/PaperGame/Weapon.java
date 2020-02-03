package PaperGame;


import java.io.*;

public class Weapon extends Equipment {
    // Class variables unique to the weapons class
    private String weaponType; // Weapon Type (Rogue, Wizard, Knight)
    private short weaponRange;


    /**
     * Constructor to create a weapon, given all the correct parameters
     * @param strength the weapons strength stat
     * @param agility the weapons agility stat
     * @param intelligence the weapons intelligence stat
     * @param fortitude the weapons fortitude stat
     * @param weight The weapons weight
     * @param name the weapons name
     * @param type the weapons type(Rogue, Wizard, Knight)
     * @param range how far the weapon can reach
     */
    public Weapon(short strength, short agility, short intelligence, short fortitude, int weight,
                  String name, String type, short range){
        super(name,strength,agility,intelligence,fortitude,weight);
        weaponType = type;
        weaponRange = range;
    }


    /**
     * Default constructor creates a weapon with all stats 0, and no name or type
     */
    public Weapon(){
        super();
        weaponType = "";
        weaponRange = 0;
    }


    /**
     * @return Returns that this item is a weapon
     */
    public String getType(){
        return WEAPON;
    }


    /**
     * @return Returns the type
     */
    public String getWeaponType() { return weaponType; }


    /**
     * Convert a Weapon into a byte array
     *
     * @param weapon Weapon that will be converted into a byte array
     * @return Returns the byte array of the converted Weapon
     */
    public static byte[] convertToBytes(Weapon weapon){
        byte [] byteBuffer = null;  // Instantiate the byte array

        try {
            // Open the output streams that will be used to convert the Weapon into a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // Convert the object into a byte array
            oos.writeObject(weapon);
            oos.flush();
            byteBuffer = baos.toByteArray();

            // Close the output streams
            baos.close();
            oos.close();
        } catch(IOException ex){
            ex.printStackTrace();
        }
        // Return the byte array of the Weapon
        return byteBuffer;
    }


    /**
     * Convert a Serialized Weapon's byte array into a Weapon
     *
     * @param byteBuffer A serialized byte array containing a weapon
     * @return Returns a de-serialized Weapon
     */
    public static Weapon convertToWeapon(byte [] byteBuffer){
        Object object = null; // Instantiate object

        try {
            // Open the input Streams that will be used to convert a byte array into a Weapon
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

        // Return Weapon contained within the byte array
        return (Weapon)object;
    }


    /**
     * Method prints the stats and the name of the Weapon
     */
    public void printWeapon(){
        // Print Weapon information
        System.out.println("Strength: " + getStrength() + " Agility: " + getAgility() + " Intelligence " +
                getIntelligence() + " Fortitude: " + getFortitude() + "\nWeapon Type: " + getWeaponType() +
                " Weight: " + getWeight() + "\nName: " + getName());
    }
}
