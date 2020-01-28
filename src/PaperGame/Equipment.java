package PaperGame;

import java.io.Serializable;

public class Equipment implements Item, Serializable {
    // The equipment stats
    private String name;
    private short strength;
    private short agility;
    private short intelligence;
    private short fortitude;
    private int weight;

    /**
     * Constructor, the stat changes and name this piece of equipment is established within this method.
     * @param name Refers to the equipments name
     * @param strength Refers to the equipments strength change
     * @param agility Refers to the equipments agility change
     * @param intelligence Refers to the equipments intelligence change
     * @param fortitude Refers to the equipments fortitude change
     * @param weight Refes to the equipments weight
     */
    public Equipment(String name, short strength, short agility, short intelligence, short fortitude, int weight){
        this.strength = strength;
        this.agility = agility;
        this.intelligence = intelligence;
        this.fortitude = fortitude;
        this.weight = weight;
        this.name = name;
    }

    /**
     * Default Constructor, The equipments stat changes are set to 0, and the name of the equipment is set to "" by
     * default
     */
    public Equipment(){
        this.strength     = 0;
        this.agility      = 0;
        this.intelligence = 0;
        this.fortitude    = 0;
        this.weight       = 0;
        this.name         = null;
    }

    /**
     * @return Returns the equipments name
     */
    public String getName(){
        return name;
    }

    /**
     * @return Returns the empty string because the equipment class is a layer of abstraction
     */
    public String getType() { return ""; }

    /**
     * @return Returns the strength stat of this piece of equipment
     */
    public short getStrength() { return strength; }

    /**
     * @return Returns the agility stat of this piece of equipment
     */
    public short getAgility() { return agility; }

    /**
     * @return Returns the intelligence stat of this piece of equipment
     */
    public short getIntelligence() { return intelligence; }

    /**
     * @return Returns the fortitude stat of this piece of equipment
     */
    public short getFortitude() { return fortitude; }


    /**
     * @return Returns the weight stat of this piece of equipment
     */
    public int getWeight() { return weight; }
}
