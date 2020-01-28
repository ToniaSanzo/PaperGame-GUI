package PaperGame;


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

}
