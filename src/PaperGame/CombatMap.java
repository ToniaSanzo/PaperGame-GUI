package PaperGame;

import java.io.*;
import java.util.HashMap;

public class CombatMap implements Serializable{

    // Wrapper class for Creature-Champion's, keeps track of the dynamic locations of the Creature-Champion's,
    // the life status of the Creature-Champion's, the allies and enemies of the Creature-Champion's, and which user the
    // Creature-Champion's correspond too
    /*private static Class Combatant{
        Creature creature;
        Long coordinate;
        boolean alive;
    }*/

    // Two different HashMap's are used because the String ID, provides information on the object type. Allowing easy
    // type recognition within a generic HashMap
    private HashMap<Long, String> coordinateMap;  // Maps an XY pair coordinate long to an objects String ID
    private HashMap<String, Object> stringMap;    // Maps a String ID to a Object
    private boolean [][] grid;                    // 2-D boolean array, false values are empty locations, and true are
                                                  // non-empty locations
    private int cols, rows;                       // Dimensions of the combat map
    private String name;                          // The name of the combat map


    /**
     * Combat Map, 2-Dimensional grid representing a combat zone. Containing Creatures, Champions, TreasureChests,
     * Traps, Walls, and Doors.
     *
     * @param col How wide the 2-D grid is
     * @param row How tall the 2-D grid is
     * @param name The name of the CombatMap
     */
    public CombatMap(int col, int row, String name){
        grid = new boolean[row][col];
        coordinateMap = new HashMap<Long, String>();
        stringMap = new HashMap<String, Object>();
        cols = col;
        rows = row;
        this.name = name;
    }


    /**
     * Add a creature to the grid, at the XY coordinate
     *
     * @param x X-Coordinate of the object
     * @param y Y-Coordinate of the object
     * @param creature Creature object to be added to the CombatMap
     */
    private void addCreature(Creature creature, int x, int y){
        int hashCode;             // HashCode generated from the creatures name
        Long coordinate;          // Long representation of the XY-Coordinate
        String uniqueIdentifier;  // String identifier

        // Print Statement, if an Object is added to a filled position
        if(grid[y][x]){
            System.out.println("POSITION CURRENTLY FILLED CANNOT ADD TO THIS THE LOCATION");
            return;
        }
        coordinate = convertToCoordinate(x, y);      // Convert the XY coordinate to its long representation
        hashCode   = creature.getName().hashCode();  // Generate a HashCode based on the creature's name
        uniqueIdentifier = "CRE" + hashCode;         // Create the creature's unique identifier

        // Prevent duplicate keys being added to the HashMap's
        while(stringMap.containsKey(uniqueIdentifier)){ uniqueIdentifier = uniqueIdentifier.concat("0"); }

        // Add the creature to the CombatMap
        coordinateMap.put(coordinate, uniqueIdentifier);
        stringMap.put(uniqueIdentifier, creature);
        grid[y][x] = true;
    }


    /**
     * Add champion to the grid, at the XY coordinate
     *
     * @param x X-Coordinate of the object
     * @param y Y-Coordinate of the object
     * @param champ Champion object to be added to the CombatMap
     */
    private void addChampion(Champion champ, int x, int y){
        int hashCode;             // HashCode generated from the champions name
        Long coordinate;          // Long representation of the XY-Coordinate
        String uniqueIdentifier;  // String identifier

        // Print Statement, if an Object is added to a filled position
        if(grid[y][x]){
            System.out.println("POSITION CURRENTLY FILLED CANNOT ADD TO THIS THE LOCATION");
            return;
        }
        coordinate = convertToCoordinate(x, y);   // Convert the XY coordinate to its long representation
        hashCode   = champ.getName().hashCode();  // Generate a HashCode based on the champion's name
        uniqueIdentifier = "CHA" + hashCode;      // Create the champion's unique identifier

        // Prevent duplicate keys being added to the HashMap's
        while(stringMap.containsKey(uniqueIdentifier)){ uniqueIdentifier = uniqueIdentifier.concat("0"); }

        // Add the champion to the CombatMap
        coordinateMap.put(coordinate, uniqueIdentifier);
        stringMap.put(uniqueIdentifier, champ);
        grid[y][x] = true;
    }


    /**
     * Add a TreasureChest to the grid, at the XY coordinate
     *
     * @param x X-Coordinate of the object
     * @param y Y-Coordinate of the object
     * @param treasureChest TreasureChest object to be added to the CombatMap
     */
    private void addChest(TreasureChest treasureChest, int x, int y){
        int hashCode;             // HashCode generated from the TreasureChest object
        Long coordinate;          // Long representation of the XY-Coordinate
        String uniqueIdentifier;  // String identifier

        // Print Statement, if an Object's added to a filled position
        if(grid[y][x]){
            System.out.println("POSITION CURRENTLY FILLED CANNOT ADD TO THIS THE LOCATION");
            return;
        }
        coordinate = convertToCoordinate(x, y);  // Convert the XY coordinate to its long representation
        hashCode   = treasureChest.hashCode();   // Generate a HashCode based on the treasure chest object
        uniqueIdentifier = "TRE" + hashCode;     // Create the treasure chest's unique identifier

        // Prevent duplicate keys being added to the HashMap's
        while(stringMap.containsKey(uniqueIdentifier)){ uniqueIdentifier = uniqueIdentifier.concat("0"); }

        // Add the treasure chest to the CombatMap
        coordinateMap.put(coordinate, uniqueIdentifier);
        stringMap.put(uniqueIdentifier, treasureChest);
        grid[y][x] = true;
    }


    /**
     * Add a trap to the grid, at the XY coordinate
     *
     * @param x X-Coordinate of the object
     * @param y Y-Coordinate of the object
     * @param trap Trap object to be added to the CombatMap
     */
    private void addTrap(Trap trap, int x, int y){
        int hashCode;             // HashCode generated from the trap object
        Long coordinate;          // Long representation of the XY-Coordinate
        String uniqueIdentifier;  // String identifier

        // Print Statement, if an Object is added to a filled position
        if(grid[y][x]){
            System.out.println("POSITION CURRENTLY FILLED CANNOT ADD TO THIS THE LOCATION");
            return;
        }
        coordinate = convertToCoordinate(x, y);  // Convert the XY coordinate to its long representation
        hashCode   = trap.hashCode();            // Generate a HashCode based on the trap object
        uniqueIdentifier = "TRA" + hashCode;     // Create the trap's unique identifier

        // Prevent duplicate keys being added to the HashMap's
        while(stringMap.containsKey(uniqueIdentifier)){ uniqueIdentifier = uniqueIdentifier.concat("0"); }

        // Add the trap to the CombatMap
        coordinateMap.put(coordinate, uniqueIdentifier);
        stringMap.put(uniqueIdentifier, trap);
        grid[y][x] = true;
    }


    /**
     * Add a wall to the grid, at the XY coordinate
     *
     * @param x X-Coordinate of the object
     * @param y Y-Coordinate of the object
     */
    private void addWall(int x, int y){
        String uniqueIdentifier = "WAL";  // String identifier
        Long coordinate;                  // Long representation of the XY-Coordinate

        // Print Statement, if an Object is added to a filled position
        if(grid[y][x]){
            System.out.println("POSITION CURRENTLY FILLED CANNOT ADD TO THIS THE LOCATION");
            return;
        }
        coordinate = convertToCoordinate(x, y);  // Convert the XY coordinate to its long representation

        // Add the wall to the CombatMap
        coordinateMap.put(coordinate, uniqueIdentifier);
        grid[y][x] = true;
    }


    /**
     * Add a door to the grid, at the XY coordinate
     *
     * @param x X-Coordinate of the object
     * @param y Y-Coordinate of the object
     * @param open true for open, and false for closed
     */
    private void addDoor(boolean open, int x, int y){
        int hashCode;             // HashCode generated from the Boolean object
        Long coordinate;          // Long representation of the XY-Coordinate
        String uniqueIdentifier;  // String identifier
        Boolean doorBool = open;  // Boolean object for the door

        // Print Statement, if an Object is added to a filled position
        if(grid[y][x]){
            System.out.println("POSITION CURRENTLY FILLED CANNOT ADD TO THIS THE LOCATION");
            return;
        }
        coordinate       = convertToCoordinate(x, y);  // Convert the XY coordinate to its long representation
        hashCode         = doorBool.hashCode();        // Generate a HashCode based on the Boolean object
        uniqueIdentifier = "DOR" + hashCode;           // Create the door's unique identifier

        // Prevent duplicate keys being added to the HashMap's
        while(stringMap.containsKey(uniqueIdentifier)){ uniqueIdentifier = uniqueIdentifier.concat("0"); }

        // Add the door to the CombatMap
        coordinateMap.put(coordinate, uniqueIdentifier);
        stringMap.put(uniqueIdentifier, doorBool);
        grid[y][x] = true;
    }


    /**
     * @return Returns the name of the CombatMap
     */
    public String getName(){ return name; }


    /**
     * Convert a X Y integer pair into a single long coordinate representation
     *
     * @param X Integer representation of the column
     * @param Y Integer representation of the row
     * @return Long coordinate value containing the metadata of a X Y pair
     */
    private static long convertToCoordinate(int X, int Y){
        long rtnVal = (long)X << 32;
        return rtnVal | (long)Y;
    }


    /**
     * Convert a long coordinate value into a X Y integer pair
     *
     * @param coor Long coordinate value containing an X Y pair
     * @return Integer array of length 2, index 0 contains the X-coordinate, index 1 contains the Y-coordinate
     */
    private static int [] convertToXY(long coor){
        int y = (int)coor;
        int x = (int)(coor >> 32);
        int [] rtnArr = {x, y};
        return rtnArr;
    }


    /**
     * Convert a Combat Map into a byte array
     *
     * @param combatMap The CombatMap that you would like to be converted into a byte array
     * @return Returns the byte array of the converted CombatMap
     */
    public static byte[] convertToBytes(CombatMap combatMap){
        byte [] byteBuffer = null;  // Instantiate the byte array

        try {
            // Open the output streams that will be used to convert the CombatMap into a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // Convert the object into a byte array
            oos.writeObject(combatMap);
            oos.flush();
            byteBuffer = baos.toByteArray();

            // Close the output streams
            baos.close();
            oos.close();
        } catch(IOException ex){
            ex.printStackTrace();
        }
        // Return the byte array of the CombatMap
        return byteBuffer;
    }


    /**
     * Convert a Serialized CombatMap's byte array into a CombatMap
     *
     * @param byteBuffer A serialized byte array containing a CombatMap
     * @return Returns a de-serialized CombatMap
     */
    public static CombatMap convertToCombatMap(byte [] byteBuffer){
        Object object = null; // Instantiate object

        try {
            // Open the input Streams that will be used to convert a byte array into a CombatMap
            ByteArrayInputStream bain = new ByteArrayInputStream(byteBuffer);
            ObjectInputStream in = new ObjectInputStream(bain);

            // Convert the byte array into the object
            object = in.readObject();

            // Close the input streams
            if (in != null && bain != null) {
                in.close();
                bain.close();
            }
        }
        // Print the stack trace when either an IOException or ClassNotFoundException is caught
        catch(Exception ex){ ex.printStackTrace(); }

        // Return CombatMap contained within the byte array
        return (CombatMap)object;
    }


    /**
     * Method prints the dimensions and the name of the combat map
     */
    public void printMap(){
        System.out.println("Width: " + cols);
        System.out.println("Height: " + rows);
        System.out.println("Name: " + name);
    }
}
