package PaperGame;

import java.io.*;

public class SaveLoad {
    final static String DELIMETER = "\n";


    /**
     * Serializes a champion object and writes it to a .ser file in the ChampionFolder
     *
     * @param serObj
     */
    public static void writeChmpToFile(Object serObj) {
        try {
            FileOutputStream fOut = new FileOutputStream(System.getProperty("user.dir") +"/src/sam" +
                    "ple/ChampionFolder/" + serObj.toString());
            ObjectOutputStream oOut = new ObjectOutputStream(fOut);
            oOut.writeObject(serObj);
            oOut.close();
            fOut.close();
            File chmpNameFile = new File(System.getProperty("user.dir") +"/src/sam" +
                    "ple/ChampionFolder/ChampNames.txt");
            FileWriter fWriter = new FileWriter(chmpNameFile,true);
            fWriter.write(serObj.toString() + DELIMETER);
            fWriter.close();
            System.out.println("Save Successful");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Saves a champion object to the Champion Folder
     *
     * @param serObj
     */
    public static void saveExistingChmp(Object serObj) {
        try {
            FileOutputStream fOut = new FileOutputStream(System.getProperty("user.dir") +"/src/sam" +
                    "ple/ChampionFolder/" + serObj.toString());
            ObjectOutputStream oOut = new ObjectOutputStream(fOut);
            oOut.writeObject(serObj);
            oOut.close();
            fOut.close();
            System.out.println("Save Successful");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Given a path to a serialized Object will return the saved Object
     *
     * @param path the path to the saved Object
     * @return If the Object is found will return the Object, else returns null
     */
    public static Object readObjectFromFile(String path){
        try {
            FileInputStream fIn = new FileInputStream(path);
            ObjectInputStream oIn = new ObjectInputStream(fIn);

            Object obj = oIn.readObject();

            oIn.close();
            fIn.close();
            System.out.println("Load Successful");
            return obj;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    /**
     * Using the name of a champion object file, will return the specified champion object
     *
     * @param chmpName The champion's file name
     * @return return's a champion object, or null if the file is not found
     */
    public static Champion readChampFromFile(String chmpName){
        String path = System.getProperty("user.dir") +"/src/PaperGame/ChampionFolder/" + chmpName;
        Object obj = readObjectFromFile(path);

        if(obj != null){
            Champion rtnChmp = (Champion)obj;
            return rtnChmp;
        }

        return null;
    }
    

}