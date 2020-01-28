package PaperGame;

import java.io.Serializable;

public class Consumable implements Item, Serializable {
    private static final int WEIGHT = 1;  // All consumables weight is one

    private String name;
    private short strength, agility, intelligence, fortitude, health, energy, mana, gold;
    private int experiencePts;


    /**
     * Construct a Consumable, the Consumable's stat changes are specified through other methods
     *
     * @param name The consumable's name
     */
    public Consumable(String name){
        this.name          = name;
        this.strength      = 0;
        this.agility       = 0;
        this.intelligence  = 0;
        this.fortitude     = 0;
        this.health        = 0;
        this.energy        = 0;
        this.mana          = 0;
        this.gold          = 0;
        this.experiencePts = 0;
    }


    /**
     * @param strength Value strength is set too
     */
    public void setStrength(short strength){ this.strength = strength; }


    /**
     * @param agility Value agility is set too
     */
    public void setAgility(short agility){ this.agility = agility; }


    /**
     * @param intelligence Value intelligence is set too
     */
    public void setIntelligence(short intelligence){ this.intelligence = intelligence; }


    /**
     * @param fortitude Value fortitude is set too
     */
    public void setFortitude(short fortitude){ this.fortitude = fortitude; }


    /**
     * @param health Value health is set too
     */
    public void setHealth(short health){ this.health = health; }


    /**
     * @param energy Value energy is set too
     */
    public void setEnergy(short energy){ this.energy = energy; }


    /**
     * @param mana Value mana is set too
     */
    public void setMana(short mana){ this.mana = mana; }


    /**
     * @param gold Value gold is set too
     */
    public void setGold(short gold){ this.gold = gold; }


    /**
     * @param experiencePts Values experiencePts is set too
     */
    public void setExperiencePts(int experiencePts){ this.experiencePts = experiencePts; }


    /**
     * @return Returns the consumable's strength stat
     */
    public short getStrength(){ return strength; }


    /**
     * @return Returns the consumable's agility stat
     */
    public short getAgility(){ return agility; }


    /**
     * @return Returns the consumable's intelligence stat
     */
    public short getIntelligence(){ return intelligence; }


    /**
     * @return Returns the consumable's fortitude stat
     */
    public short getFortitude(){ return fortitude; }


    /**
     * @return Returns the consumable's health stat
     */
    public short getHealth(){ return health; }


    /**
     * @return Returns the consumable's energy stat
     */
    public short getEnergy(){ return energy; }


    /**
     * @return Returns the consumable's mana stat
     */
    public short getMana(){ return mana; }


    /**
     * @return Returns the consumable's gold stat
     */
    public short getGold(){ return gold; }


    /**
     * @return Returns the consumable's experiencePts stat
     */
    public int getExperiencePts(){ return experiencePts; }


    /**
     * @return Returns the weight of the consumable
     */
    public int getWeight(){
        return WEIGHT;
    }


    /**
     * @return Returns "Consumable" in order to know which subclass this Item belongs too
     */
    public String getType(){
        return CONSUMABLE;
    }


    /**
     * @return Returns the consumable's name
     */
    public String getName(){
        return name;
    }
}
