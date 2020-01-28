package PaperGame;

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

public class ConfirmationBox {

    static Stage stage;
    static boolean yBtnClicked;

    public static boolean show(String message, String title, String textYes, String textNo){
        yBtnClicked = false;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setMinWidth(250);
        stage.setMinHeight(125);

        Label lbl = new Label();
        lbl.setText(message);

        Button btnYes = new Button();
        btnYes.setText(textYes);
        btnYes.setOnAction(e -> {
            stage.close();
            yBtnClicked = true; });

        Button btnNo = new Button();
        btnNo.setText(textNo);
        btnNo.setOnAction(e -> {
            stage.close();
        });

        HBox paneBtn = new HBox(20);
        paneBtn.getChildren().addAll(btnYes,btnNo);
        paneBtn.setAlignment(Pos.BASELINE_CENTER);

        VBox pane = new VBox(20);
        pane.getChildren().addAll(lbl, paneBtn);
        pane.setAlignment(Pos.CENTER);

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.showAndWait();
        return yBtnClicked;
    }

}
