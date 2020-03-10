package PaperGame.gui;

import PaperGame.networking.UserID;
import PaperGame.utility.SaveLoad;
import PaperGame.entities.Champion;
import PaperGame.utility.ThreadBridge;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.*;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class GUI extends Application implements Runnable {
    //----------------- CONSTANTS --------------------------------------------------------------------------------------
    public static final String ORC = "Orc", ELF = "Elf", HUMAN = "Human", DWARF = "Dwarf";
    public static final String ARCHER = "Archer", WARRIOR = "Warrior", PALADIN = "Paladin", MAGE = "Mage";
    //----------------- VARIABLES --------------------------------------------------------------------------------------
    Stage stage;

    // Objects used in the CreateUID Scene
    Scene crtUIDScene;
    Button crtUIDBtn;
    TextField crtUIDTxtField;
    Label crtUIDWelcomeLbl, crtUIDNameLbl;
    VBox crtUIDPanel;

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
    VBox dmRoomJoinPanel;
    Timeline playerJoining;
    boolean timelineRunning = false;


    // Player-StartUp_Screen objects
    Scene plyrStartUpScene;
    Label plyrStartUpWelcomeLbl, plyrStartUpSlctChmpLbl, plyrStartIpLbl;
    ObservableList<String> chmpOptions;
    ComboBox plyrStartUpComboBox;
    TextField ipAddrTxtField;
    Button plyrStartUpBtn;
    VBox plyrStartUpPanel;


    // Objects used in the Create a Champion Scene
    Scene crtChmpScene;
    Button acceptBtn, imageBtn;
    Button orcBtn, elfBtn, dwarfBtn, humanBtn;
    Button archerBtn, warriorBtn, mageBtn, paladinBtn;
    Tooltip orcTT, elfTT, dwarfTT, humanTT;
    Tooltip archerTT, warriorTT, mageTT, paladinTT;
    Label crtChmpLbl,enterChmpName;
    TextField chmpName;
    String nameStr,raceStr,classStr,crtChmpLabelStr;
    Champion currentChamp = null;
    Image champImage = null;

    // Objects used in the Main Champion Page Screen
    Scene plyrMstrScene;
    VBox plyrMstrVPanel;
    HBox plyrMstrHPanel1, plyrMstrHPanel2;
    BorderPane plyrMstrBPanel;
    Label plyrMstrNameLbl, plyrMstrRaceLbl, plyrMstrClassLbl, plyrMstrExpLbl, plyrMstrGoldLbl, plyrMstrEneryLbl,
            plyrMstrAgiLbl, plyrMstrIntLbl, plyrMstrStrLbl, plyrMstrFrtLbl, plyrMstrEqpdLbl, plyrMstrWpnLbl,
            plyrMstrHeadLbl, plyrMstrChestLbl, plyrMstrPantsLbl, plyrMstrGlovesLbl, plyrMstrBootsLbl, plyrMstrLvlLbl,
            plyrMstrHealthLbl, plyrMstrManaLbl, plyrMstrJewelryLbl;
    MenuBar plyrMstrMenuBar;
    Menu plyrMstrTradeMenu, plyrMstrInventoryMenu;
    Image plyrMstrChmpImg;
    ImageView plyrMstrChmpImgView;


    //----------------- METHODS ----------------------------------------------------------------------------------------

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

        // CreateUID Scene
        crtUIDWelcomeLbl = new Label("Welcome!");
        crtUIDWelcomeLbl.setFont(Font.font("Cambria", 32));
        crtUIDNameLbl = new Label("Enter username:");
        crtUIDTxtField = new TextField();
        crtUIDTxtField.setMinWidth(200);
        crtUIDTxtField.setMaxWidth(200);
        crtUIDBtn = new Button("Next");
        crtUIDBtn.setOnAction(e -> processUser());
        crtUIDPanel = new VBox(50, crtUIDWelcomeLbl, crtUIDNameLbl, crtUIDTxtField, crtUIDBtn);
        crtUIDPanel.setAlignment(Pos.CENTER);
        crtUIDScene = new Scene(crtUIDPanel, 700, 500);

        // Player-StartUp_Screen
        plyrStartUpSlctChmpLbl = new Label("Select Champion");
        chmpOptions = FXCollections.observableArrayList(SaveLoad.getChampNameArray());
        plyrStartUpComboBox = new ComboBox(chmpOptions);
        plyrStartUpComboBox.setValue("Create a Champion");
        plyrStartIpLbl = new Label("Enter IP Address:");
        ipAddrTxtField = new TextField();
        ipAddrTxtField.setMaxWidth(240);
        plyrStartUpBtn = new Button("Join");
        plyrStartUpBtn.setOnAction(e -> selectChmp(plyrStartUpComboBox.getValue().toString()));

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
        imageBtn = new Button("Browse for Image");
        imageBtn.setOnAction(e -> imageOpt());
        HBox classPanel = new HBox(50, archerBtn, warriorBtn, paladinBtn, mageBtn);
        HBox crtChmpBtnPane = new HBox(50, acceptBtn, imageBtn);
        VBox panel4 = new VBox(65,crtChmpLbl, chmpNamePanel, racePanel, classPanel, crtChmpBtnPane);
        panel4.setAlignment(Pos.CENTER);
        classPanel.setAlignment(Pos.CENTER);
        racePanel.setAlignment(Pos.CENTER);
        chmpNamePanel.setAlignment(Pos.CENTER);
        acceptBtn.setAlignment(Pos.CENTER);
        crtChmpScene = new Scene(panel4,730,430);

        // launch the Start Screen or Create UID Screen
        if(ThreadBridge.checkUID()){
            crtUser();
        } else {
            initialScreen();
        }
    }

    /**
     * Initial DM or Player Screen
     */
    private void initialScreen(){
        stage.setScene(dmOrPlyrScene);
        stage.setTitle("Choose Role");
        stage.setOnCloseRequest(e ->{
            e.consume();
            closeOnYes();
        });
        stage.show();
    }

    /**
     * Method called when the user chooses the Dungeon Master button, in the Dungeon Master or Player scene
     */
    private void dmOption() {
        playerJoining = new Timeline(new KeyFrame(Duration.seconds(.3), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!ThreadBridge.userEmpty()){
                    dmRoomJoinStartUID.getItems().add(ThreadBridge.popUser() + " joined the party");
                }
            }
        }));

        ThreadBridge.serverOn();
        String lblStr = "IP Address\n";
        for(String str: ThreadBridge.getIP()){ lblStr = lblStr.concat(str + "\n"); }
        dmRoomJoinIPAddr = new Label(lblStr);
        dmRoomJoinIPAddr.setFont(Font.font("Cambria", 32));
        dmRoomJoinStartUID = new ListView<>();
        dmRoomJoinStartUID.setMaxWidth(630);
        dmRoomJoinStartUID.setMouseTransparent( true );
        dmRoomJoinStartUID.setFocusTraversable( false );
        dmRoomJoinStart  = new Button("Start");
        dmRoomJoinStart.setOnAction(e -> {
            ThreadBridge.gameOn();
            playerJoining.stop();
            timelineRunning = false;
        });
        dmRoomJoinPanel = new VBox(50, dmRoomJoinIPAddr, dmRoomJoinStartUID, dmRoomJoinStart);
        dmRoomJoinPanel.setAlignment(Pos.CENTER);
        dmRoomJoinScene = new Scene(dmRoomJoinPanel, 1200, 900);
        stage.setScene(dmRoomJoinScene);
        stage.setTitle("Join Room");
        stage.show();


        playerJoining.setCycleCount(Timeline.INDEFINITE);
        timelineRunning = true;
        playerJoining.play();
    }



    /**
     * Method called when the user chooses the Player button, in the Dungeon Master or Player scene
     */
    private void plyrOption() {
        UserID tmpID = (UserID)SaveLoad.readObjectFromFile(System.getProperty("user.dir") + "/src/PaperGame/re" +
                        "s/UID/myUID");

        plyrStartUpWelcomeLbl = new Label("Hello " + tmpID.getName());
        plyrStartUpWelcomeLbl.setFont(Font.font("Cambria", 32));
        plyrStartUpPanel = new VBox(50, plyrStartUpWelcomeLbl, plyrStartUpSlctChmpLbl,plyrStartUpComboBox,
                plyrStartIpLbl, ipAddrTxtField, plyrStartUpBtn);
        plyrStartUpPanel.setAlignment(Pos.CENTER);
        plyrStartUpScene = new Scene(plyrStartUpPanel,900,500);
        ThreadBridge.clientOn();
        stage.setScene(plyrStartUpScene);
        stage.setTitle("Join Party");
        stage.show();
    }

    /**
     * Method called when the user chooses the Create a Character button, in the Load Character scene
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
            if(timelineRunning){ playerJoining.stop(); }
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
     * Create a UserID, save the UserID, update the scene to the initial scene
     */
    private void processUser(){
        if(crtUIDTxtField.getText().length() == 0) {
            MessageBox.show("Empty Text Field","You need a username");
            return;
        }
        UserID uID = new UserID(crtUIDTxtField.getText());
        SaveLoad.writeUIDToFile(uID);
        ThreadBridge.resetUID();
        initialScreen();
    }


    /**
     * Create UID Scene
     */
    private void crtUser(){
        stage.setScene(crtUIDScene);
        stage.setTitle("New Player");
        stage.show();
    }


    /**
     * Given a champion name, this method will load an already created champion, connect to a GUI and change the scene
     * to reflect that change.
     * @param fileName The name of the champion's object file contained inside ChampionFolder
     */
    private void selectChmp(String fileName){
        // Quick check to make sure that IP address field is not empty
        if(ipAddrTxtField.getText().length() == 0){
            MessageBox.show("Enter IP address","You need an IP address ");
            return;
        }

        // Update ThreadBridge with the IP address the User entered
        ThreadBridge.ipReceived(ipAddrTxtField.getText());

        // Wait until the client notify's GUI of a server join attempt
        while(!ThreadBridge.isAttemptedPartyJoin()){
            try { Thread.sleep(333); } catch(InterruptedException ex){ ex.printStackTrace(); }
        }

        // Return to the previous screen if the join attempt failed
        if(ThreadBridge.joinFailed()){ return; }

        if(fileName.equals("Create a Champion")){
            crtChmpOption();
        } else {
            currentChamp = SaveLoad.readChampFromFile(fileName);
            createMainChmpScene();
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

        createMainChmpScene();
    }


    /**
     * Generate Player Master Screen
     */
    private void createMainChmpScene(){
        // Prevent null pointer exceptions
        if(currentChamp == null){ return; }

        // Generate the Character info on the left side of the scene
        plyrMstrChmpImg = new Image("file:" + System.getProperty("user.dir") + "/src/PaperGame/res/Pictures/Eric_K" +
                "oston.jpg");
        plyrMstrChmpImgView = new ImageView(plyrMstrChmpImg);
        plyrMstrChmpImgView.setFitHeight(180);
        plyrMstrChmpImgView.setFitWidth(180);
        plyrMstrChmpImgView.setPreserveRatio(true);
        plyrMstrNameLbl = new Label("Name: " + currentChamp.getName());
        plyrMstrRaceLbl = new Label(currentChamp.getRace());
        plyrMstrClassLbl = new Label(currentChamp.getChampionClass());
        plyrMstrHPanel1 = new HBox();
        plyrMstrHPanel1.setAlignment(Pos.CENTER);
        plyrMstrHPanel1.setSpacing(10);
        plyrMstrHPanel1.getChildren().addAll(plyrMstrRaceLbl, plyrMstrClassLbl);
        plyrMstrLvlLbl = new Label("Level: " + currentChamp.getLevel());
        plyrMstrGoldLbl = new Label("Gold: " + currentChamp.getGold());
        plyrMstrHPanel2 = new HBox();
        plyrMstrHPanel2.setAlignment(Pos.CENTER);
        plyrMstrHPanel2.setSpacing(10);
        plyrMstrHPanel2.getChildren().addAll(plyrMstrLvlLbl, plyrMstrGoldLbl);
        plyrMstrHealthLbl = new Label("Health: " + currentChamp.getCurrentHealth() + "/" + currentChamp.getTotalHealth());
        plyrMstrExpLbl = new Label("Experience Pts: " + currentChamp.getExperiencePts() + "/" + currentChamp.getLevel() * 7);
        plyrMstrManaLbl = new Label("Mana: " + currentChamp.getCurrentMana() + "/" + currentChamp.getTotalMana());
        plyrMstrEneryLbl = new Label("Energy: " + currentChamp.getCurrentEnergy() + "/" + currentChamp.getTotalEnergy());
        plyrMstrAgiLbl = new Label("Agility: " + currentChamp.getAgility());
        plyrMstrIntLbl = new Label("Intelligence: " + currentChamp.getIntelligence());
        plyrMstrStrLbl = new Label("Strength: " + currentChamp.getStrength());
        plyrMstrFrtLbl = new Label("Fortitude: " + currentChamp.getFortitude());
        plyrMstrEqpdLbl = new Label("Equipped:");
        if(currentChamp.getWeapon() != null) {
            plyrMstrWpnLbl = new Label("Weapon: " + currentChamp.getWeapon().getName());
        } else {
            plyrMstrWpnLbl = new Label("Weapon: none");
        }
        if(currentChamp.getChampJewelry() != null) {
            plyrMstrJewelryLbl = new Label("Jewelry: " + currentChamp.getChampJewelry().getName());
        } else {
            plyrMstrJewelryLbl = new Label("Jewelry: none");
        }
        if(currentChamp.getChampHeadGear() != null) {
            plyrMstrHeadLbl = new Label("Head: " + currentChamp.getChampHeadGear().getName());
        } else {
            plyrMstrHeadLbl = new Label("Head: none");
        }
        if(currentChamp.getChampTorso() != null){
            plyrMstrChestLbl = new Label("Chest: " + currentChamp.getChampTorso().getName());
        } else {
            plyrMstrChestLbl = new Label("Chest: none");
        }
        if(currentChamp.getChampPants() != null){
            plyrMstrPantsLbl = new Label("Pants: " + currentChamp.getChampPants().getName());
        } else {
            plyrMstrPantsLbl = new Label("Pants: none");
        }
        if(currentChamp.getChampGloves() != null){
            plyrMstrGlovesLbl = new Label("Gloves: " + currentChamp.getChampGloves().getName());
        } else {
            plyrMstrGlovesLbl = new Label("Gloves: none");
        }
        if(currentChamp.getChampBoots() != null){
            plyrMstrBootsLbl = new Label("Boots: " + currentChamp.getChampGloves().getName());
        } else {
            plyrMstrBootsLbl = new Label("Boots: none");
        }

        // Add all the Champions stats node to the VBox
        plyrMstrVPanel = new VBox();
        plyrMstrVPanel.getChildren().addAll(
                plyrMstrChmpImgView, plyrMstrNameLbl, plyrMstrHPanel1, plyrMstrHPanel2, plyrMstrExpLbl,
                plyrMstrHealthLbl, plyrMstrEneryLbl, plyrMstrAgiLbl, plyrMstrIntLbl, plyrMstrStrLbl, plyrMstrFrtLbl,
                plyrMstrEqpdLbl, plyrMstrWpnLbl, plyrMstrHeadLbl, plyrMstrJewelryLbl, plyrMstrChestLbl,
                plyrMstrPantsLbl, plyrMstrGlovesLbl, plyrMstrBootsLbl
        );
        plyrMstrVPanel.setPadding(new Insets(10));
        plyrMstrVPanel.setSpacing(8);

        // Set up the Champion Master MenuBar
        MenuItem dummyItemA = new MenuItem();
        MenuItem dummyItemB = new MenuItem();
        plyrMstrTradeMenu = new Menu("Trade");
        plyrMstrTradeMenu.getItems().add(dummyItemA);
        plyrMstrTradeMenu.addEventHandler(Menu.ON_SHOWN, e -> plyrMstrTradeMenu.hide());
        plyrMstrTradeMenu.addEventHandler(Menu.ON_SHOWING, e -> plyrMstrTradeMenu.fire());
        plyrMstrTradeMenu.setOnAction( e -> System.out.println("Clicked Trade"));
        plyrMstrInventoryMenu = new Menu("Inventory");
        plyrMstrInventoryMenu.getItems().add(dummyItemB);
        plyrMstrInventoryMenu.addEventHandler(Menu.ON_SHOWN, e -> plyrMstrInventoryMenu.hide());
        plyrMstrInventoryMenu.addEventHandler(Menu.ON_SHOWING, e -> plyrMstrInventoryMenu.fire());
        plyrMstrInventoryMenu.setOnAction(e -> System.out.println("Inventory clicked"));
        plyrMstrMenuBar = new MenuBar();
        plyrMstrMenuBar.getMenus().addAll(plyrMstrTradeMenu, plyrMstrInventoryMenu);

        // Set up the Champion Master panel
        plyrMstrBPanel = new BorderPane();
        plyrMstrBPanel.setLeft(plyrMstrVPanel);
        plyrMstrBPanel.setTop(plyrMstrMenuBar);

        // Init Player Master Scene
        plyrMstrScene = new Scene(plyrMstrBPanel, 960, 650);

        // Set scene and show the stage
        stage.setScene(plyrMstrScene);
        stage.setTitle(currentChamp.getName());
        stage.show();
    }


    /**
     * Browse files for an image file
     * Code from Java-Buddy.blogspot.com
     */
    public void imageOpt(){
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilterJPG =
                new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG =
                new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");

        //Show open file dialog
        File file = fileChooser.showOpenDialog(null);

        try{
            BufferedImage bufferedImage = ImageIO.read(file);
            champImage = SwingFXUtils.toFXImage(bufferedImage, null);
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }
}