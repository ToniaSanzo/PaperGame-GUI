package PaperGame.entities;

public class Trap {
    private static final String TRAP = "Trap";  // String used to represent the type of object it is
    private short visibility;                   // How easily the trap can be spotted
    private short damage;                       // Damage inflicted when the trap is activated
    private short range;                        // Range around the trap that damage will be inflicted too
    private String name;                        // The Traps unique name


    /**
     * Constructs a Trap item with a specified name, visiblity, damage, and range value.
     *
     * @param name String representing the traps unique name
     * @param visibility Short representing how easily the trap can be spotted
     * @param damage Short representing damage inflicted when the trap is activated
     * @param range Short representing range around the trap that damage will be inflicted too
     */
    public Trap(String name, short visibility, short damage, short range){
        this.name       = name;        // The Traps unique name
        this.visibility = visibility;  // How difficult it is to spot the trap in a combat encounter
        this.damage     = damage;      // Damage inflicted when the trap is activated
        this.range      = range;       // Range around the Trap that damage will be inflicted too
    }


    /**
     * @return Returns the visibility modifier of the trap, how easily the trap is spotted
     */
    public short getVisibility(){ return visibility; }


    /**
     * @return Returns the damage done by the trap
     */
    public short getDamage(){ return damage; }


    /**
     * @return Returns the range around the trap that damage will be inflicted too
     */
    public short getRange(){ return range; }


    /**
     * @return Return the traps name
     */
    public String getName(){ return name; }


    /**
     * @return Return the object type
     */
    public String getType(){ return TRAP; }


    /**
     * @param visibility Update the visibility modifier to the parameter value
     */
    public void setVisibility(short visibility){ this.visibility = visibility; }


    /**
     * @param damage Update the damage modifier to the parameter value
     */
    public void setDamage(short damage){ this.damage = damage; }


    /**
     * @param range Update the range modifier to the parameter value
     */
    public void setRange(short range){ this.range = range; }


    /**
     * @param name Update the name modifier to the parameter value
     */
    public void setName(String name){ this.name = name; }
}
