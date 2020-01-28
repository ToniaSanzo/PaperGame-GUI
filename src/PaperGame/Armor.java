package PaperGame;


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

}
