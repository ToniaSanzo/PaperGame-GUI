package PaperGame.gui;

import PaperGame.utility.SaveLoad;
import PaperGame.entities.Champion;
import PaperGame.utility.ThreadBridge;
import javafx.application.*;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.stage.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class GUI extends Application implements Runnable {
    //----------------- CONSTANTS --------------------------------------------------------------------------------------
    public static final String ORC = "Orc", ELF = "Elf", HUMAN = "Human", DWARF = "Dwarf";
    public static final String ARCHER = "Archer", WARRIOR = "Warrior", PALADIN = "Paladin", MAGE = "Mage";
    //----------------- VARIABLES --------------------------------------------------------------------------------------
    Stage stage;

    // Objects used in the Dungeon Master or Player Scene
    Scene dmOrPlyrScene;
    Button dmBtn;
    Button plyrBtn;
    Label dmOrPlyrLbl;

    // DM-Room-Join_Screen objects
    Scene dmRoomJoinScene;
    Button dmRoomJoinStart;
    Label dmRoomJoinIPAddr;
    ListView<String> dmRoomJoinStartUID;


    // Objects used in the Load Character Scene
    Scene loadCharacterScene;
    MenuBar menuBar;
    Menu menuSlctChmp;
    MenuItem tempMenuItem, crtChmpItem;

    // Objects used in the Create a Champion Scene
    Scene crtChmpScene;
    Button acceptBtn;
    Button orcBtn, elfBtn, dwarfBtn, humanBtn;
    Button archerBtn, warriorBtn, mageBtn, paladinBtn;
    Tooltip orcTT, elfTT, dwarfTT, humanTT;
    Tooltip archerTT, warriorTT, mageTT, paladinTT;
    Label crtChmpLbl,enterChmpName;
    TextField chmpName;
    String nameStr,raceStr,classStr,crtChmpLabelStr;
    Champion currentChamp = null;

    // Objects used in the Main Champion Page Screen
    Scene mainChmpScene;
    Button lvlUpBtn, consumableBtn, equipmentBtn, tempBtn;
    Label chmpNameLbl, chmpRaceLbl, chmpClassLbl, lvlLbl, goldLbl, invWeightLbl;
    Label strengthLbl, agilityLbl, intelligenceLbl, fortitudeLbl;
    Image chmpImg;


    //----------------- METHODS ----------------------------------------------------------------------------------------
    public GUI(){}


    /**
     * When the program runs the main method, the main method launches the GUI used in javaFX
     */
    public void run(){
        launch();
    }


    /**
     * Launches the javaFX GUI, essentially the "main" method for the GUI
     * @param primaryStage The stage that is used to conatain the main part of the GUI
     * @throws Exception Default error checking
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;

        // Creates the Dungeon Master or Player Scene
        dmBtn = new Button("Dungeon Master");
        dmBtn.setOnAction(e -> dmOption());
        plyrBtn = new Button("Player");
        plyrBtn.setOnAction(e -> plyrOption());
        dmOrPlyrLbl = new Label("Choose Role");
        dmBtn.setMaxWidth(Double.MAX_VALUE);
        plyrBtn.setMaxWidth(Double.MAX_VALUE);
        HBox panel1 = new HBox(35,dmBtn,plyrBtn);
        panel1.setAlignment(Pos.CENTER);
        VBox panel2 = new VBox(20);
        panel2.getChildren().addAll(dmOrPlyrLbl,panel1);
        panel2.setAlignment(Pos.CENTER);
        dmOrPlyrScene = new Scene(panel2,400,250);

        // Creates the Load Character Scene
        crtChmpItem = new MenuItem("_Create a Character");
        crtChmpItem.setOnAction(e -> crtChmpOption());
        menuBar = new MenuBar();
        menuSlctChmp = new Menu("_Select a Character");
        //loadMenuItems();
        //for(int i = 0; i < 3; i++)
        tempMenuItem = new MenuItem("Tonia the Human Skateboarder");
        tempMenuItem.setOnAction(e -> selectChmp("Tonia the Human Warrior"));
        menuSlctChmp.getItems().add(tempMenuItem);
        menuSlctChmp.getItems().add(new SeparatorMenuItem());
        menuSlctChmp.getItems().add(crtChmpItem);
        menuBar.getMenus().add(menuSlctChmp);
        VBox panel3 = new VBox(75,menuBar);
        panel3.setAlignment(Pos.TOP_CENTER);
        loadCharacterScene = new Scene(panel3,260,320);

        // Creates the Create a Champion Scene
        crtChmpLbl = new Label("Click accept to continue");
        enterChmpName = new Label("Character's Name: ");
        chmpName = new TextField();
        chmpName.setMinWidth(200);
        chmpName.setMaxWidth(200);
        HBox chmpNamePanel = new HBox(50,enterChmpName,chmpName);
        orcTT = new Tooltip();
        orcTT.setText("+2 Strength\n-1 Intelligence");
        orcBtn = new Button(ORC);
        orcBtn.setTooltip(orcTT);
        orcBtn.setOnAction(e -> raceSelection(ORC));
        orcBtn.setStyle("-fx-background-color: #808080");
        elfTT = new Tooltip();
        elfTT.setText("+2 Agility\n-1 Fortitude");
        elfBtn = new Button(ELF);
        elfBtn.setTooltip(elfTT);
        elfBtn.setOnAction(e -> raceSelection(ELF));
        elfBtn.setStyle("-fx-background-color: #808080");
        humanTT = new Tooltip();
        humanTT.setText("+2 Intelligence\n-1 Strength");
        humanBtn = new Button(HUMAN);
        humanBtn.setTooltip(humanTT);
        humanBtn.setOnAction(e -> raceSelection(HUMAN));
        humanBtn.setStyle("-fx-background-color: #808080");
        dwarfTT = new Tooltip();
        dwarfTT.setText("+2 Fortitude\n-1 Agility");
        dwarfBtn = new Button(DWARF);
        dwarfBtn.setTooltip(dwarfTT);
        dwarfBtn.setOnAction(e -> raceSelection(DWARF));
        dwarfBtn.setStyle("-fx-background-color: #808080");
        HBox racePanel = new HBox(50,humanBtn,dwarfBtn,elfBtn,orcBtn);
        archerTT = new Tooltip();
        archerTT.setText("+2 Agility\nWeapon: Wooden Bow & Arrows");
        archerBtn = new Button(ARCHER);
        archerBtn.setTooltip(archerTT);
        archerBtn.setOnAction(e -> classSelection(ARCHER));
        archerBtn.setStyle("-fx-background-color: #808080");
        warriorTT = new Tooltip();
        warriorTT.setText("+2 Strength\nWeapon: Iron Sword");
        warriorBtn = new Button(WARRIOR);
        warriorBtn.setTooltip(warriorTT);
        warriorBtn.setOnAction(e -> classSelection(WARRIOR));
        warriorBtn.setStyle("-fx-background-color: #808080");
        paladinTT = new Tooltip();
        paladinTT.setText("+2 Fortitude\nWeapon: Club & Wooden Shield");
        paladinBtn = new Button(PALADIN);
        paladinBtn.setTooltip(paladinTT);
        paladinBtn.setOnAction(e -> classSelection(PALADIN));
        paladinBtn.setStyle("-fx-background-color: #808080");
        mageTT = new Tooltip();
        mageTT.setText("+2 Intelligence\nWeapon: Basic Elemental Tome");
        mageBtn = new Button(MAGE);
        mageBtn.setTooltip(mageTT);
        mageBtn.setOnAction(e -> classSelection(MAGE));
        mageBtn.setStyle("-fx-background-color: #808080");
        acceptBtn = new Button("Accept");
        acceptBtn.setOnAction(e -> accept());
        HBox classPanel = new HBox(50, archerBtn, warriorBtn, paladinBtn, mageBtn);
        VBox panel4 = new VBox(65,crtChmpLbl, chmpNamePanel, racePanel, classPanel, acceptBtn);
        panel4.setAlignment(Pos.CENTER);
        classPanel.setAlignment(Pos.CENTER);
        racePanel.setAlignment(Pos.CENTER);
        chmpNamePanel.setAlignment(Pos.CENTER);
        acceptBtn.setAlignment(Pos.CENTER);
        crtChmpScene = new Scene(panel4,730,430);


        primaryStage.setScene(dmOrPlyrScene);
        primaryStage.setTitle("Choose Role");
        primaryStage.setOnCloseRequest(e ->{
            e.consume();
            closeOnYes();
        });
        primaryStage.show();
    }

    /**
     * Method called when the user chooses the Dungeon Master button, in the Dungeon Master or Player scene
     */
    private void dmOption() {
        ThreadBridge.serverOn();
        String lblStr = "IP Address\n";
        for(String str: ThreadBridge.getIP()){ lblStr = lblStr.concat(str + "\n"); }
        dmRoomJoinIPAddr = new Label(lblStr);
        dmRoomJoinStartUID = new ListView<String>();
        dmRoomJoinStart  = new Button("Start");
        dmRoomJoinStart.setOnAction(e -> { System.out.println("start button pressed"); });
        HBox dmRoomJoinPanel = new HBox(50, dmRoomJoinIPAddr, dmRoomJoinStartUID, dmRoomJoinStart);
        dmRoomJoinScene = new Scene(dmRoomJoinPanel, 730, 430);
        stage.setScene(dmRoomJoinScene);
        stage.setTitle("Join Room");
        stage.show();
    }

    /**
     * Method called when the user chooses the Player button, in the Dungeon Master or Player scene
     */
    private void plyrOption() {
        stage.setScene(loadCharacterScene);
        stage.setTitle("Load/Create Character");
        stage.show();
    }

    /**
     * Method called when theuser chooses the Create a Character button, in the Load Character scene
     */
    private void crtChmpOption() {
        stage.setScene(crtChmpScene);
        stage.setTitle("Create a Champion");
        stage.show();
    }

    /**
     * Displays a tiny User Interface that prompts the user to confirm exiting the program
     */
    private void closeOnYes() {
        boolean close = ConfirmationBox.show("Close the Program?","Exit","Yes","No");
        if(close) {
            stage.close();
            ThreadBridge.guiOff();
        }
    }

    /**
     * Sets the champion race while creating a champion in the create a champion scene
     * @param race the string that will represent the champion race
     */
    private void raceSelection(String race){
        switch(race){
            case ORC:
                raceStr = ORC;
                orcBtn.setStyle("-fx-background-color: #bff7bc");
                elfBtn.setStyle("-fx-background-color: #808080");
                humanBtn.setStyle("-fx-background-color: #808080");
                dwarfBtn.setStyle("-fx-background-color: #808080");
                crtChmpLbl.setText("Orc\n+2 Strength\n-1 Intelligence");
                break;
            case ELF:
                raceStr = ELF;
                orcBtn.setStyle("-fx-background-color: #808080");
                elfBtn.setStyle("-fx-background-color: #bff7bc");
                humanBtn.setStyle("-fx-background-color: #808080");
                dwarfBtn.setStyle("-fx-background-color: #808080");
                crtChmpLbl.setText("Elf\n+2 Agility\n-1 Fortitude");
                break;
            case HUMAN:
                raceStr = HUMAN;
                orcBtn.setStyle("-fx-background-color: #808080");
                elfBtn.setStyle("-fx-background-color: #808080");
                humanBtn.setStyle("-fx-background-color: #bff7bc");
                dwarfBtn.setStyle("-fx-background-color: #808080");
                crtChmpLbl.setText("Human\n+2 Intelligence\n-1 Strength");
                break;
            case DWARF:
                raceStr = DWARF;
                orcBtn.setStyle("-fx-background-color: #808080");
                elfBtn.setStyle("-fx-background-color: #808080");
                humanBtn.setStyle("-fx-background-color: #808080");
                dwarfBtn.setStyle("-fx-background-color: #bff7bc");
                crtChmpLbl.setText("Dwarf\n+2 Fortitude\n-1 Agility");
                break;
        }
    }

    /**
     * Sets the champion class while creating a champion in the create a champion scene
     * @param chmpClass the string that will represent the champion class
     */
    private void classSelection(String chmpClass){
        switch(chmpClass){
            case ARCHER:
                classStr = ARCHER;
                archerBtn.setStyle("-fx-background-color: #bff7bc");
                paladinBtn.setStyle("-fx-background-color: #808080");
                mageBtn.setStyle("-fx-background-color: #808080");
                warriorBtn.setStyle("-fx-background-color: #808080");
                crtChmpLbl.setText("Archer\n+2 Agility\nWeapon: Wooden Bow & Arrows");
                break;
            case PALADIN:
                classStr = PALADIN;
                archerBtn.setStyle("-fx-background-color: #808080");
                paladinBtn.setStyle("-fx-background-color: #bff7bc");
                mageBtn.setStyle("-fx-background-color: #808080");
                warriorBtn.setStyle("-fx-background-color: #808080");
                crtChmpLbl.setText("Paladin\n+2 Fortitude\nWeapon: Club & Wooden Shield");
                break;
            case MAGE:
                classStr = MAGE;
                archerBtn.setStyle("-fx-background-color: #808080");
                paladinBtn.setStyle("-fx-background-color: #808080");
                mageBtn.setStyle("-fx-background-color: #bff7bc");
                warriorBtn.setStyle("-fx-background-color: #808080");
                crtChmpLbl.setText("Mage\n+2 Intelligence\nWeapon: Basic Elemental Tome");
                break;
            case WARRIOR:
                classStr = WARRIOR;
                archerBtn.setStyle("-fx-background-color: #808080");
                paladinBtn.setStyle("-fx-background-color: #808080");
                mageBtn.setStyle("-fx-background-color: #808080");
                warriorBtn.setStyle("-fx-background-color: #bff7bc");
                crtChmpLbl.setText("Warrior\n+2 Strength\nWeapon: Iron Sword");
                break;
        }
    }

    /**
     * Given a option, this method will load a already created champion, and change the scene
     * to reflect that change
     * @param fileName The name of the champion's object file contained inside ChampionFolder
     */
    private void selectChmp(String fileName){
        currentChamp = SaveLoad.readChampFromFile(fileName);
        crtChmpLabelStr = fileName;

        try {
            createMainChmpScene(currentChamp,crtChmpLabelStr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is the function called when the user clicks the accept button in the Create a Champ Scene
     */
    private void accept(){
        String tempStr;

        if(chmpName.getText().length() == 0) {
            MessageBox.show("Character has no name","You need a name");
            return;
        }

        if(classStr == null){
            MessageBox.show("Choose a character class","You need a class");
            return;
        }

        if(raceStr == null){
            MessageBox.show("Choose a character race", "You need a race");
            return;
        }

        tempStr = nameStr + " the " + raceStr + " " + classStr;
        if(!(tempStr.equals(crtChmpLabelStr))){
            nameStr = chmpName.getText();
            crtChmpLabelStr = nameStr + " the " + raceStr + " " + classStr;
            crtChmpLbl.setText(crtChmpLabelStr);
            return;
        }

        currentChamp = new Champion(classStr,raceStr,nameStr);
        SaveLoad.writeChmpToFile(currentChamp);

        try {
            createMainChmpScene(currentChamp,crtChmpLabelStr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void createMainChmpScene(Champion tempChmp,String title) throws FileNotFoundException {
        // Generates the Top Panel for the Border Panel
        chmpNameLbl = new Label("Name - " + tempChmp.getName());
        lvlLbl = new Label("Level: 0");
        goldLbl = new Label("Gold: $100");
        chmpRaceLbl = new Label("Race - " + tempChmp.getRace());
        chmpClassLbl = new Label("Class - " + tempChmp.getChampionClass());
        //yournode.setStyle("-fx-background-color: #" + enteredByUser);

        // Generates the Left Panel for the Border Panel
        strengthLbl = new Label("Strength: " + tempChmp.getStrength());
        agilityLbl = new Label("Agility: " + tempChmp.getAgility());
        intelligenceLbl = new Label("Intelligence: " + tempChmp.getIntelligence());
        fortitudeLbl = new Label("Fortitude: " + tempChmp.getFortitude());
        lvlUpBtn = new Button("LEVEL UP!");
        lvlUpBtn.setOnAction(e -> MessageBox.show("Under Construction","Tonia"));
        VBox leftVBox1 = new VBox(20,strengthLbl,agilityLbl,intelligenceLbl,fortitudeLbl,lvlUpBtn);

        // Generate the Center Panel for the Border Panel
        tempBtn = new Button("PAPER GAME \n user interface\n\n here?");
        tempBtn.setOnAction(e -> MessageBox.show("Under Construction","Tonia"));

        //Generate the Right Panel for the Border Panel
        chmpImg = new Image(new FileInputStream("/media/toniasanzo/ToniaSanzo_1/Pictures/Elf.jpg"));
        ImageView imgView = new ImageView(chmpImg);
        imgView.setFitHeight(100);
        imgView.setFitWidth(100);
        imgView.setPreserveRatio(true);
        Group imgRoot = new Group(imgView);
        consumableBtn = new Button("Consumables");
        consumableBtn.setOnAction(e -> MessageBox.show("Under Construction","Tonia"));
        equipmentBtn = new Button("Equipment");
        equipmentBtn.setOnAction(e -> MessageBox.show("Under Construction","Tonia"));
        invWeightLbl = new Label("Inventory Weight: 5/16");
        VBox rightVBox1 = new VBox(10,consumableBtn,equipmentBtn,invWeightLbl);
        VBox rightVBox2 = new VBox(45,imgRoot,rightVBox1);

        // Putting the Border Pane together
        GridPane gPanel = new GridPane();
        gPanel.add(chmpNameLbl,0,0);
        gPanel.setHalignment(chmpNameLbl, HPos.LEFT);
        gPanel.add(lvlLbl,1,0);
        gPanel.setHalignment(lvlLbl,HPos.CENTER);
        gPanel.add(goldLbl,2,0);
        gPanel.setHalignment(goldLbl,HPos.RIGHT);
        gPanel.add(chmpRaceLbl,0,1);
        gPanel.setHalignment(chmpRaceLbl,HPos.LEFT);
        gPanel.add(chmpClassLbl,2,1);
        gPanel.setHalignment(chmpClassLbl,HPos.RIGHT);
        gPanel.add(leftVBox1,0,2);
        gPanel.setHalignment(leftVBox1,HPos.LEFT);
        gPanel.add(tempBtn,1,2);
        gPanel.setHalignment(tempBtn,HPos.CENTER);
        gPanel.add(rightVBox2,2,2);
        gPanel.setHalignment(rightVBox2,HPos.RIGHT);
        gPanel.setHgap(50);
        gPanel.setVgap(10);

        mainChmpScene = new Scene(gPanel,460,350);
        stage.setScene(mainChmpScene);
        stage.setTitle(title);
        stage.show();
    }

    private void loadMenuItems(){
        String path = "./ChampionFolder/ChampNames.txt";
    }
}