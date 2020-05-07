package PaperGame.gui;

import PaperGame.entities.ChatMessage;
import PaperGame.entities.Inventory;
import PaperGame.entities.Item;
import PaperGame.networking.UserID;
import PaperGame.utility.SaveLoad;
import PaperGame.entities.Champion;
import PaperGame.utility.ThreadBridge;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.util.Callback;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GUI extends Application implements Runnable {
    public static Champion currentChamp      = null;  // The Player's current Champion
    public static Inventory offerInventory   = null;  // Items being offered in trade
    public static Inventory receiveInventory = null;  // Items being received in trade
    public static ArrayList<UserID> userIDs  = null;  // UserIDs within the party


    // List item containing the name of an item and the quantity to trade
    static class InventoryCell extends ListCell<String>{
        HBox hBox = new HBox();
        Label itemNameLbl = new Label("(empty)");
        Label qtyLbl = new Label("Quantity:");
        Label currQtyLbl = new Label(" / #");
        Pane hSpacePane = new Pane();
        TextField textField = new TextField("0");
        String lastItem;


        /**
         * Constructor, initialize the HBox panel
         */
        public InventoryCell() {
            super();
            hBox.getChildren().addAll(itemNameLbl, hSpacePane, qtyLbl, textField, currQtyLbl);
            hBox.setSpacing(5);
            hBox.setHgrow(hSpacePane, Priority.ALWAYS);
        }


        /**
         * Given a String, create TradeCell object associated with the item the String maps too
         */
        @Override
        protected void updateItem(String item, boolean empty){
            if(currentChamp == null){ throw new NullPointerException("currentChamp not initialized"); }
            Inventory inventory = currentChamp.getInventory();
            super.updateItem(item, empty);
            setText(null);
            if(empty) {
                lastItem = null;
                setGraphic(null);
            } else {
                lastItem = item;
                if(item != null && inventory != null){
                    itemNameLbl.setText(item);
                    currQtyLbl.setText(" / " + inventory.getQuantityList().get(inventory.indexOf(item)));
                } else { itemNameLbl.setText("<null>"); }

                setGraphic(hBox);
            }
        }


        /**
         * Return quantity of a Item in a InventoryCell TextField
         *
         * @return Non-negative integer
         */
        public int getQuantity(){
            try {
                int rtnVal = Integer.parseInt(textField.getText());
                int maxVal = Integer.parseInt(currQtyLbl.getText().substring(3));

                if(rtnVal < 0){
                    rtnVal = 0;
                }

                if(rtnVal > maxVal){
                    rtnVal = maxVal;
                }

                return rtnVal;
            } catch (NumberFormatException ex){
                return 0;
            }
        }


        /**
         * Return the Item's name
         *
         * @return String item name
         */
        public String getItemName(){ return itemNameLbl.getText(); }
    }


    // List item containing the name of an item and the quantity being offered or received
    static class OfferCell extends ListCell<String>{
        HBox hBox = new HBox();
        Label itemNameLbl = new Label("(empty)");
        Label qtyLbl = new Label("Quantity: #");
        Pane hSpacePane = new Pane();
        String lastItem;


        /**
         * Constructor, initialize the HBox panel
         */
        public OfferCell() {
            super();
            hBox.getChildren().addAll(itemNameLbl, hSpacePane, qtyLbl);
            hBox.setSpacing(5);
            hBox.setHgrow(hSpacePane, Priority.ALWAYS);
        }


        /**
         * Given a String, create TradeCell object associated with the item the String maps too
         */
        @Override
        protected void updateItem(String item, boolean empty){
            if(currentChamp == null){ throw new NullPointerException("currentChamp not initialized"); }
            if(offerInventory == null){ return; }
            Inventory inventory = currentChamp.getInventory();
            super.updateItem(item, empty);
            setText(null);
            if(empty) {
                lastItem = null;
                setGraphic(null);
            } else {
                lastItem = item;
                if(item != null && inventory != null){
                    itemNameLbl.setText(item);
                    qtyLbl.setText("Quantity: " + offerInventory.getQuantityList().get(offerInventory.indexOf(item)));
                } else {
                    itemNameLbl.setText("<null>");
                    setGraphic(null);
                }

                setGraphic(hBox);
            }
        }
    }


    // List item containing the name of an item and the quantity being offered or received
    static class ReceiveCell extends ListCell<String>{
        HBox hBox = new HBox();
        Label itemNameLbl = new Label("(empty)");
        Label qtyLbl = new Label("Quantity: #");
        Pane hSpacePane = new Pane();
        String lastItem;


        /**
         * Constructor, initialize the HBox panel
         */
        public ReceiveCell() {
            super();
            hBox.getChildren().addAll(itemNameLbl, hSpacePane, qtyLbl);
            hBox.setSpacing(5);
            hBox.setHgrow(hSpacePane, Priority.ALWAYS);
        }


        /**
         * Given a String, create TradeCell object associated with the item the String maps too
         */
        @Override
        protected void updateItem(String item, boolean empty){
            if(currentChamp == null){ throw new NullPointerException("currentChamp not initialized"); }
            if(receiveInventory == null){ return; }
            Inventory inventory = currentChamp.getInventory();
            super.updateItem(item, empty);
            setText(null);
            if(empty) {
                lastItem = null;
                setGraphic(null);
            } else {
                lastItem = item;
                if(item != null && inventory != null){
                    itemNameLbl.setText(item);
                    qtyLbl.setText("Quantity: "
                            + receiveInventory.getQuantityList().get(receiveInventory.indexOf(item)));
                } else { itemNameLbl.setText("<null>"); }

                setGraphic(hBox);
            }
        }
    }


    //----------------- CONSTANTS --------------------------------------------------------------------------------------
    public static final String ORC = "Orc", ELF = "Elf", HUMAN = "Human", DWARF = "Dwarf",
            ARCHER = "Archer", WARRIOR = "Warrior", PALADIN = "Paladin", MAGE = "Mage",
            D4 = "D4", D6 = "D6", D12 = "D12", D20 = "D20",
            STRENGTH = "Strength", AGILITY = "Agility", INTELLIGENCE = "Intelligence", FORTITUDE = "Fortitude";


    //----------------- VARIABLES --------------------------------------------------------------------------------------
    // Main JavaFX stage
    Stage stage;

    // Timeline action handler, used by both the DM and Player
    Timeline timeline;
    boolean timelineRunning = false;

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
    String nameStr, raceStr, classStr, crtChmpLabelStr;
    BufferedImage champImage = null;

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
    Menu plyrMstrTradeMenu, plyrMstrInventoryMenu, plyrMstrSCMenu, plyrMstrChatMenu;
    Image plyrMstrChmpImg;
    ImageView plyrMstrChmpImgView;

    // Objects used in the Player-Fluid-Inventory sub screen
    ListView<String> inventoryList;
    Button invUseBtn, invDetBtn, invRemBtn;
    Label invWeightLbl;
    HBox invBtnPane;
    VBox invPane;

    // Objects used in the DM-Player-Fluid trade sub screen
    ListView<String> dmPlyrTradeInvLV, dmPlyrTradeOfferingLV, dmPlyrTradeReceiveLV;
    ObservableList<String> dmPlyrTradeInvOList, dmPlyrTradeOfferingOList, dmPlyrTradeRecieveOList;
    Label dmPlyrTradeItemLbl, dmPlyrTradeOfferingLbl, dmPlyrTradeReceiveLbl;
    Button dmPlyrTradeUpdateBtn, dmPlyrTradeAcceptBtn, dmPlyrTradeCancelBtn;
    VBox dmPlyrTradeInvVBox, dmPlyrTradeOfferingVBox, dmPlyrTradeReceiveVBox, dmPlyrTradeVBox;
    HBox dmPlyrTradeBtnHBox, dmPlyrLVHBox;
    BorderPane dmPlyrTradeBPane;

    // Objects used in the Player-Fluid skill check sub screen
    Label plyrSCDiceLbl, plyrSCSkillLbl, plyrSCOutputLbl;
    Button plyrSCD4Btn, plyrSCD6Btn, plyrSCD12Btn, plyrSCD20Btn, plyrSCIntBtn, plyrSCAgiBtn, plyrSCStrBtn, plyrSCFrtBtn,
            plyrSCCheckBtn;
    String skillCheckDice, skillCheckStat, skillCheckOutput;
    VBox plyrSCDiceVB, plyrSCSkillVB, plyrSCVB;
    HBox plyrSCVBHB, plyrSCOutputHB;

    // Objects used in the Player-DM-Fluid chat sub screen
    ListView<String> chatBox;
    TextField chatField;
    Button chatBtn;
    HBox chatHPane;
    VBox chatPane;
    ChatMessage message;
    Scene dmChatScene;


    //----------------- METHODS ----------------------------------------------------------------------------------------
    /**
     * When the program runs the main method, the main method launches the GUI used in javaFX
     */
    public void run(){
        launch();
    }


    /**
     * Launches the javaFX GUI, essentially the "main" method for the GUI
     *
     * @param primaryStage The stage that is used to conatain the main part of the GUI
     * @throws Exception Default error checking
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        userIDs = new ArrayList<UserID>();
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
        crtChmpBtnPane.setAlignment(Pos.CENTER);
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
        timeline = new Timeline(new KeyFrame(Duration.seconds(.3), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!ThreadBridge.userEmpty()){
                    userIDs.add(ThreadBridge.popUser());
                    dmRoomJoinStartUID.getItems().add(userIDs.get(userIDs.size() - 1).getChampion() +
                            " joined the party");
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
            timeline.stop();
            timelineRunning = false;
            dmChatScreen();
        });
        dmRoomJoinPanel = new VBox(50, dmRoomJoinIPAddr, dmRoomJoinStartUID, dmRoomJoinStart);
        dmRoomJoinPanel.setAlignment(Pos.CENTER);
        dmRoomJoinScene = new Scene(dmRoomJoinPanel, 1200, 750);
        stage.setScene(dmRoomJoinScene);
        stage.setTitle("Join Room");
        stage.show();

        timeline.setCycleCount(Timeline.INDEFINITE);
        timelineRunning = true;
        timeline.play();
    }


    /**
     * Set Dungeon Master chat screen
     */
    private void dmChatScreen(){
        timeline = new Timeline(new KeyFrame(Duration.seconds(.3), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(ThreadBridge.checkMessageRcvdFlag()){
                    message = ThreadBridge.getMessageRcvd();

                    chatBox.getItems().add(message.toString());
                }
            }
        }));

        // Initialize DM chat screen
        chatBox = new ListView<String>();
        chatBox.setMaxWidth(900);
        chatField = new TextField();
        chatField.setPrefWidth(700);
        chatField.setMaxWidth(700);
        chatBtn = new Button("Send");
        chatBtn.setOnAction(e -> sendMessage());
        chatHPane = new HBox(50, chatField, chatBtn);
        chatPane = new VBox(40, chatBox, chatHPane);
        dmChatScene = new Scene(chatPane, 1000, 700);
        stage.setScene(dmChatScene);
        stage.setTitle("Chat");
        stage.show();

        timeline.setCycleCount(Timeline.INDEFINITE);
        timelineRunning = true;
        timeline.play();
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
            if(timelineRunning){ timeline.stop(); }
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
     *
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
     * Send message to users
     */
    private void sendMessage(){
        // If the message is empty return
        if(chatField.getText().length() == 0){
            return;
        }

        // Send message as DM
        if(currentChamp == null){
            ThreadBridge.sendMessage(new ChatMessage("GM", chatField.getText()));
            return;
        }

        // Send message as champion
        ThreadBridge.sendMessage(new ChatMessage(currentChamp.getName(), chatField.getText()));

        // update chatField to reflect message sent
        chatBox.getItems().add(new ChatMessage("[Me]", chatField.getText()).toString());
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

        if (champImage != null) {
            currentChamp = new Champion(classStr, raceStr, nameStr);
            currentChamp.setImagePath(
                    "file:" + System.getProperty("user.dir") +
                            "/src/PaperGame/res/Pictures/" + currentChamp.toString() + ".png"
            );
            SaveLoad.writeImageToFile(champImage, currentChamp.toString());

        }
        else {
            currentChamp = new Champion(classStr, raceStr, nameStr);
        }

        Item tempItem;
        switch(classStr){
            case ARCHER:
                tempItem = (Item)SaveLoad.readObjectFromFile(System.getProperty("user.dir") +
                        "/src/PaperGame/res/ItemsFolder/" + SaveLoad.sanitizeFilename("Wooden Bow & Arrow"));
                if(tempItem != null) {
                    currentChamp.addItem(tempItem);
                }
                break;
            case WARRIOR:
                tempItem = (Item)SaveLoad.readObjectFromFile(System.getProperty("user.dir") +
                        "/src/PaperGame/res/ItemsFolder/Iron Sword");
                if(tempItem != null) {
                    currentChamp.addItem(tempItem);
                }
                break;
            case PALADIN:
                tempItem = (Item)SaveLoad.readObjectFromFile(System.getProperty("user.dir") +
                        "/src/PaperGame/res/ItemsFolder/" + SaveLoad.sanitizeFilename("Club & Wooden Shield"));
                if(tempItem != null) {
                    currentChamp.addItem(tempItem);
                }
                break;
            case MAGE:
                tempItem = (Item)SaveLoad.readObjectFromFile(System.getProperty("user.dir") +
                        "/src/PaperGame/res/ItemsFolder/Basic Elemental Tome");
                if(tempItem != null) {
                    currentChamp.addItem(tempItem);
                }
                break;
        }

        SaveLoad.writeChmpToFile(currentChamp);

        createMainChmpScene();
    }


    /**
     * Generate Player Master Screen
     */
    private void createMainChmpScene(){
        // Prevent null pointer exceptions
        if(currentChamp == null){ return; }

        // Handles asynchronous events
        timeline = new Timeline(new KeyFrame(Duration.seconds(.3), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!ThreadBridge.userEmpty()){
                    userIDs.add(ThreadBridge.popUser());
                }

                if(ThreadBridge.checkMessageRcvdFlag()){
                    message = ThreadBridge.getMessageRcvd();

                    chatBox.getItems().add(message.toString());
                }
            }
        }));

        // Update ThreadBridge with championName
        if(!ThreadBridge.checkChampionName()){ ThreadBridge.setChampionName(currentChamp.getName()); }

        // Generate the Character info on the left side of the scene
        plyrMstrChmpImg = new Image(currentChamp.getImagePath());
        plyrMstrChmpImgView = new ImageView(plyrMstrChmpImg);
        plyrMstrChmpImgView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event){
                champImage = null;
                imageOpt();
                if(champImage != null){
                    SaveLoad.writeImageToFile(champImage, currentChamp.toString());
                }
                createMainChmpScene();
                event.consume();
            }
        });
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
        plyrMstrHealthLbl = new Label("Health: " + currentChamp.getCurrentHealth() + "/" +
                currentChamp.getTotalHealth());
        plyrMstrExpLbl = new Label("Experience Pts: " + currentChamp.getExperiencePts() + "/" +
                currentChamp.getLevel() * 7);
        plyrMstrManaLbl = new Label("Mana: " + currentChamp.getCurrentMana() + "/" + currentChamp.getTotalMana());
        plyrMstrEneryLbl = new Label("Energy: " + currentChamp.getCurrentEnergy() + "/" +
                currentChamp.getTotalEnergy());
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

        // Set up the Champion Inventory sub-screen
        inventoryList = new ListView<String>();
        for(Item tmpItm: currentChamp.getInventory()){
            inventoryList.getItems().add(tmpItm.getName());
        }
        invUseBtn = new Button("Use");
        invUseBtn.setOnAction(e -> System.out.println("Use Item"));
        invDetBtn = new Button("Details");
        invDetBtn.setOnAction(e -> System.out.println("Detail Pressed"));
        invRemBtn = new Button("Remove");
        invRemBtn.setOnAction(e -> System.out.println("Remove item"));
        invWeightLbl = new Label("Inventory Weight: " + currentChamp.getCurrentInventoryWeight() + "/" +
                currentChamp.getTotalInventoryWeight());
        invBtnPane = new HBox(65, invUseBtn, invDetBtn, invRemBtn, invWeightLbl);
        invBtnPane.setAlignment(Pos.CENTER);
        invPane = new VBox(50, inventoryList, invBtnPane);
        invPane.setAlignment(Pos.CENTER);
        VBox.setMargin(inventoryList, new Insets(30));


        // Set up the Champion Trade sub-screen
        dmPlyrTradeInvOList = FXCollections.observableArrayList(currentChamp.getInventory().getItemNameList());
        dmPlyrTradeInvLV = new ListView<>(dmPlyrTradeInvOList);
        dmPlyrTradeInvLV.setItems(dmPlyrTradeInvOList);
        dmPlyrTradeInvLV.setCellFactory(new Callback<ListView<String>,
                        ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                return new InventoryCell();
            }
        });
        dmPlyrTradeOfferingOList = FXCollections.observableArrayList();
        dmPlyrTradeOfferingLV = new ListView<>(dmPlyrTradeOfferingOList);
        dmPlyrTradeOfferingLV.setItems(dmPlyrTradeInvOList);
        dmPlyrTradeOfferingLV.setCellFactory(new Callback<ListView<String>,
                ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                return new OfferCell();
            }
        });
        dmPlyrTradeRecieveOList = FXCollections.observableArrayList();
        dmPlyrTradeReceiveLV = new ListView<>(dmPlyrTradeRecieveOList);
        dmPlyrTradeReceiveLV.setItems(dmPlyrTradeInvOList);
        dmPlyrTradeReceiveLV.setCellFactory(new Callback<ListView<String>,
                ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> list) {
                return new ReceiveCell();
            }
        });
        dmPlyrTradeItemLbl = new Label("Choose Items");
        dmPlyrTradeItemLbl.setFont(Font.font("Cambria", 28));
        dmPlyrTradeOfferingLbl = new Label("You are Offering");
        dmPlyrTradeOfferingLbl.setFont(Font.font("Cambria", 28));
        dmPlyrTradeReceiveLbl = new Label("You are Receiving");
        dmPlyrTradeReceiveLbl.setFont(Font.font("Cambria", 28));
        dmPlyrTradeInvVBox = new VBox(20, dmPlyrTradeItemLbl, dmPlyrTradeInvLV);
        dmPlyrTradeOfferingVBox = new VBox(20, dmPlyrTradeOfferingLbl, dmPlyrTradeOfferingLV);
        dmPlyrTradeReceiveVBox = new VBox(20, dmPlyrTradeReceiveLbl, dmPlyrTradeReceiveLV);
        dmPlyrTradeUpdateBtn = new Button("Update Offering");
        dmPlyrTradeUpdateBtn.setOnAction(e -> updateOffering());
        dmPlyrTradeAcceptBtn = new Button("Accept");
        dmPlyrTradeAcceptBtn.setOnAction(e -> System.out.println("Accept"));
        dmPlyrTradeCancelBtn = new Button("Cancel");
        dmPlyrTradeCancelBtn.setOnAction(e -> System.out.println("Cancel"));
        dmPlyrLVHBox = new HBox(50, dmPlyrTradeInvVBox, dmPlyrTradeOfferingVBox, dmPlyrTradeReceiveVBox);
        dmPlyrTradeBtnHBox = new HBox(30, dmPlyrTradeUpdateBtn, dmPlyrTradeAcceptBtn, dmPlyrTradeCancelBtn);
        dmPlyrTradeBtnHBox.setAlignment(Pos.CENTER);
        dmPlyrTradeVBox = new VBox(40, dmPlyrLVHBox, dmPlyrTradeBtnHBox);
        dmPlyrTradeVBox.setAlignment(Pos.CENTER);
        dmPlyrTradeBPane = new BorderPane();
        dmPlyrTradeBPane.setCenter(dmPlyrTradeVBox);

        // Set up the Player-Fluid skill check sub screen
        plyrSCDiceLbl = new Label("Dice");
        plyrSCDiceLbl.setFont(Font.font("Cambria", 28));
        plyrSCSkillLbl = new Label("Skill");
        plyrSCSkillLbl.setFont(Font.font("Cambria", 28));
        plyrSCD4Btn = new Button("D4");
        plyrSCD4Btn.setStyle("-fx-background-color: #808080");
        plyrSCD4Btn.setOnAction(e -> skillCheckDiceSelection(D4));
        plyrSCD6Btn = new Button("D6");
        plyrSCD6Btn.setStyle("-fx-background-color: #808080");
        plyrSCD6Btn.setOnAction(e -> skillCheckDiceSelection(D6));
        plyrSCD12Btn = new Button("D12");
        plyrSCD12Btn.setStyle("-fx-background-color: #808080");
        plyrSCD12Btn.setOnAction(e -> skillCheckDiceSelection(D12));
        plyrSCD20Btn = new Button("D20");
        plyrSCD20Btn.setStyle("-fx-background-color: #808080");
        plyrSCD20Btn.setOnAction(e -> skillCheckDiceSelection(D20));
        plyrSCAgiBtn = new Button("Agility");
        plyrSCAgiBtn.setStyle("-fx-background-color: #808080");
        plyrSCAgiBtn.setOnAction(e -> skillCheckStatSelection(AGILITY));
        plyrSCStrBtn = new Button("Strength");
        plyrSCStrBtn.setStyle("-fx-background-color: #808080");
        plyrSCStrBtn.setOnAction(e -> skillCheckStatSelection(STRENGTH));
        plyrSCIntBtn = new Button("Intelligence");
        plyrSCIntBtn.setStyle("-fx-background-color: #808080");
        plyrSCIntBtn.setOnAction(e -> skillCheckStatSelection(INTELLIGENCE));
        plyrSCFrtBtn = new Button("Fortitude");
        plyrSCFrtBtn.setStyle("-fx-background-color: #808080");
        plyrSCFrtBtn.setOnAction(e -> skillCheckStatSelection(FORTITUDE));
        plyrSCOutputLbl = new Label();
        plyrSCCheckBtn = new Button("Check");
        plyrSCCheckBtn.setStyle("-fx-background-color: #808080");
        plyrSCCheckBtn.setOnAction(e -> processSkillCheck());
        plyrSCDiceVB = new VBox(30, plyrSCDiceLbl, plyrSCD4Btn,plyrSCD6Btn, plyrSCD12Btn, plyrSCD20Btn);
        plyrSCDiceVB.setAlignment(Pos.CENTER);
        plyrSCSkillVB = new VBox(30, plyrSCSkillLbl, plyrSCAgiBtn, plyrSCStrBtn, plyrSCIntBtn, plyrSCFrtBtn);
        plyrSCSkillVB.setAlignment(Pos.CENTER);
        plyrSCVBHB = new HBox(140, plyrSCDiceVB, plyrSCSkillVB);
        plyrSCVBHB.setAlignment(Pos.CENTER);
        plyrSCOutputHB = new HBox(70, plyrSCCheckBtn, plyrSCOutputLbl);
        plyrSCOutputHB.setAlignment(Pos.CENTER);
        plyrSCVB = new VBox(40, plyrSCVBHB, plyrSCOutputHB);

        // Set up Player-DM-Fluid chat sub screen
        chatBox = new ListView<String>();
        chatBox.setMaxWidth(900);
        chatField = new TextField();
        chatField.setPrefWidth(700);
        chatField.setMaxWidth(700);
        chatBtn = new Button("Send");
        chatBtn.setOnAction(e -> sendMessage());
        chatHPane = new HBox(50, chatField, chatBtn);
        chatPane = new VBox(40, chatBox, chatHPane);

        // Set up the Champion Master MenuBar
        MenuItem dummyItemA = new MenuItem();
        MenuItem dummyItemB = new MenuItem();
        MenuItem dummyItemC = new MenuItem();
        MenuItem dummyItemD = new MenuItem();
        plyrMstrTradeMenu = new Menu("Trade");
        plyrMstrTradeMenu.getItems().add(dummyItemA);
        plyrMstrTradeMenu.addEventHandler(Menu.ON_SHOWN, e -> plyrMstrTradeMenu.hide());
        plyrMstrTradeMenu.addEventHandler(Menu.ON_SHOWING, e -> plyrMstrTradeMenu.fire());
        plyrMstrTradeMenu.setOnAction( e -> plyrMstrBPanel.setCenter(dmPlyrTradeBPane));
        plyrMstrInventoryMenu = new Menu("Inventory");
        plyrMstrInventoryMenu.getItems().add(dummyItemB);
        plyrMstrInventoryMenu.addEventHandler(Menu.ON_SHOWN, e -> plyrMstrInventoryMenu.hide());
        plyrMstrInventoryMenu.addEventHandler(Menu.ON_SHOWING, e -> plyrMstrInventoryMenu.fire());
        plyrMstrInventoryMenu.setOnAction(e -> plyrMstrBPanel.setCenter(invPane));
        plyrMstrSCMenu = new Menu("Skill Check");
        plyrMstrSCMenu.getItems().add(dummyItemC);
        plyrMstrSCMenu.addEventHandler(Menu.ON_SHOWN, e -> plyrMstrSCMenu.hide());
        plyrMstrSCMenu.addEventHandler(Menu.ON_SHOWING, e -> plyrMstrSCMenu.fire());
        plyrMstrSCMenu.setOnAction( e -> plyrMstrBPanel.setCenter(plyrSCVB));
        plyrMstrChatMenu = new Menu("Chat");
        plyrMstrChatMenu.getItems().add(dummyItemD);
        plyrMstrChatMenu.addEventHandler(Menu.ON_SHOWN, e -> plyrMstrChatMenu.hide());
        plyrMstrChatMenu.addEventHandler(Menu.ON_SHOWING, e -> plyrMstrChatMenu.fire());
        plyrMstrChatMenu.setOnAction( e -> plyrMstrBPanel.setCenter(chatPane));
        plyrMstrMenuBar = new MenuBar();
        plyrMstrMenuBar.getMenus().addAll(plyrMstrTradeMenu, plyrMstrInventoryMenu, plyrMstrSCMenu, plyrMstrChatMenu);

        // Set up the Champion Master panel
        plyrMstrBPanel = new BorderPane();
        plyrMstrBPanel.setLeft(plyrMstrVPanel);
        plyrMstrBPanel.setTop(plyrMstrMenuBar);

        // Init Player Master Scene
        plyrMstrScene = new Scene(plyrMstrBPanel, 1070, 650);

        // Set scene and show the stage
        stage.setScene(plyrMstrScene);
        stage.setTitle(currentChamp.getName());
        timeline.setCycleCount(Timeline.INDEFINITE);
        timelineRunning = true;
        stage.show();
        timeline.play();
    }


    /**
     * When the user clicks a skill check stat, highlight the skill check stat and update the skill check output
     *
     * @param stat Stat the user selected
     */
    public void skillCheckStatSelection(String stat){
        switch(stat){
            case AGILITY:
                skillCheckStat = AGILITY;
                plyrSCAgiBtn.setStyle("-fx-background-color: #bff7bc");
                plyrSCStrBtn.setStyle("-fx-background-color: #808080");
                plyrSCFrtBtn.setStyle("-fx-background-color: #808080");
                plyrSCIntBtn.setStyle("-fx-background-color: #808080");
                break;
            case STRENGTH:
                skillCheckStat = STRENGTH;
                plyrSCAgiBtn.setStyle("-fx-background-color: #808080");
                plyrSCStrBtn.setStyle("-fx-background-color: #bff7bc");
                plyrSCFrtBtn.setStyle("-fx-background-color: #808080");
                plyrSCIntBtn.setStyle("-fx-background-color: #808080");
                break;
            case INTELLIGENCE:
                skillCheckStat = INTELLIGENCE;
                plyrSCAgiBtn.setStyle("-fx-background-color: #808080");
                plyrSCStrBtn.setStyle("-fx-background-color: #808080");
                plyrSCFrtBtn.setStyle("-fx-background-color: #808080");
                plyrSCIntBtn.setStyle("-fx-background-color: #bff7bc");
                break;
            case FORTITUDE:
                skillCheckStat = FORTITUDE;
                plyrSCAgiBtn.setStyle("-fx-background-color: #808080");
                plyrSCStrBtn.setStyle("-fx-background-color: #808080");
                plyrSCFrtBtn.setStyle("-fx-background-color: #bff7bc");
                plyrSCIntBtn.setStyle("-fx-background-color: #808080");
                break;
        }
    }


    /**
     * When the user clicks a skill check die, highlight the skill check die and update the skill check output
     *
     * @param die user selected die
     */
    public void skillCheckDiceSelection(String die){
        switch(die){
            case D4:
                skillCheckDice = D4;
                plyrSCD4Btn.setStyle("-fx-background-color: #bff7bc");
                plyrSCD6Btn.setStyle("-fx-background-color: #808080");
                plyrSCD12Btn.setStyle("-fx-background-color: #808080");
                plyrSCD20Btn.setStyle("-fx-background-color: #808080");
                break;
            case D6:
                skillCheckDice = D6;
                plyrSCD6Btn.setStyle("-fx-background-color: #bff7bc");
                plyrSCD4Btn.setStyle("-fx-background-color: #808080");
                plyrSCD12Btn.setStyle("-fx-background-color: #808080");
                plyrSCD20Btn.setStyle("-fx-background-color: #808080");
                break;
            case D12:
                skillCheckDice = D12;
                plyrSCD12Btn.setStyle("-fx-background-color: #bff7bc");
                plyrSCD4Btn.setStyle("-fx-background-color: #808080");
                plyrSCD6Btn.setStyle("-fx-background-color: #808080");
                plyrSCD20Btn.setStyle("-fx-background-color: #808080");
                break;
            case D20:
                skillCheckDice = D20;
                plyrSCD20Btn.setStyle("-fx-background-color: #bff7bc");
                plyrSCD4Btn.setStyle("-fx-background-color: #808080");
                plyrSCD12Btn.setStyle("-fx-background-color: #808080");
                plyrSCD6Btn.setStyle("-fx-background-color: #808080");
                break;
        }
    }


    /**
     * Process skill check
     */
    public void processSkillCheck(){
        String output = "Choose dice and stat";
        if(skillCheckStat != null && skillCheckDice != null){
            switch(skillCheckDice){
                case D4:
                    output = String.valueOf(currentChamp.skillCheckd4(skillCheckStat));
                    break;
                case D6:
                    output = String.valueOf(currentChamp.skillCheckd6(skillCheckStat));
                    break;
                case D12:
                    output = String.valueOf(currentChamp.skillCheckd12(skillCheckStat));
                    break;
                case D20:
                    output = String.valueOf(currentChamp.skillCheckd20(skillCheckStat));
                    break;
            }
        }
        plyrSCOutputLbl.setText(output);
        plyrSCOutputLbl.setFont(Font.font("Cambria", 28));
    }


    /**
     * Update what the Dungeon Master or Player is offering in the trade subscreen
     */
    public void updateOffering(){
        // Clear or initialize the offerInventory
        if(offerInventory != null){
            offerInventory.clear();
        } else{
            offerInventory = new Inventory();
        }

        // Clear the Offering ListView
        dmPlyrTradeOfferingOList.clear();
        dmPlyrTradeOfferingLV.setItems(dmPlyrTradeOfferingOList);

        // Determine what the user wants to offer, update the offerInventory and Offering ListView
        Object [] cells = dmPlyrTradeInvLV.lookupAll(".cell").toArray();
        for(Object o: cells){
            InventoryCell tmp = (InventoryCell)o;
            if(tmp.getQuantity() != 0) {
                offerInventory.addItem(
                        currentChamp.getInventory().get(currentChamp.getInventory().indexOf(tmp.getItemName())),
                        tmp.getQuantity()
                );
                dmPlyrTradeOfferingOList.add(tmp.getItemName());
                dmPlyrTradeOfferingLV.fireEvent(
                        new ListView.EditEvent<>(
                                dmPlyrTradeOfferingLV, ListView.editCommitEvent(), tmp.getItemName(),
                                offerInventory.indexOf(tmp.getItemName())
                                )
                );
            }
        }

        // Send the offerInventory to the ThreadBridge so the network know's what to send to the server
        ThreadBridge.setOfferInventory(offerInventory);
    }


    /**
     * Browse files for an image file, store this image as a BufferedImage
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
            champImage = ImageIO.read(file);
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }
}