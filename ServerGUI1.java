/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multithreading.socket;


import javafx.scene.control.*;
import javafx.beans.value.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.beans.property.IntegerProperty;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import javafx.beans.property.SimpleIntegerProperty;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;

public class ServerGUI1 extends Application {
    int NUMBER_OF_HOUSES_PER_SIDE;
    int SEEDS_PER_HOUSE;
    boolean IS_RANDOM_DISTRIBUTION_OF_SEEDS;

    boolean is_first_turn = true;
    boolean is_second_turn = false;

    String player1name;
    String player2name;

    PLAYER_TYPE playerType = PLAYER_TYPE.HUMAN;

    Stage globalStage;

    ServerManager sm;

    StackPane[] topPane;

    StackPane[] bottomPane;

    StackPane turnPane;

    StackPane pieRulePane;

    /*client/server stuff*/
    public boolean oneClientMode = true;

    public boolean isGameStart = false;

    public final int SERVER = 1;

    public final int CLIENT = 2;

    public final int CLIENT1 = 1;

    public final int CLIENT2 = 2;

    final int TURN = SERVER;

    String dosMessage = "";

    String dosMessage2 = "";

    boolean hasMessage = false;

    boolean hasMessage2 = false;

    ServerSocket server = null;

    Socket client = null;

    Socket serverOfClient = null;

    DataOutputStream dos = null;

    DataInputStream dis = null;

    boolean isServer = false;

    boolean myTurn;

    boolean client1Turn;

    boolean isHuman = true;

    long delay;

    long delay1;

    long delay2;

    //game state for client
    //ArrayList<int[]> gameState;

    //Timer stuff
    private static Integer START_TIME = 30;
    private Timeline timerTimeline;
    private Label timerLabel = new Label();
    private IntegerProperty timeSeconds = new SimpleIntegerProperty(START_TIME);

    String reason_for_game_over = "example";

    public synchronized boolean hasDataToProcess(){
        return hasMessage;
    }

    public synchronized void setDataToProcess(boolean hasData, String data){
        dosMessage = data;
        hasMessage = hasData;
    }

    public synchronized boolean hasDataToProcess2(){
        return hasMessage2;
    }

    public synchronized void setDataToProcess2(boolean hasData, String data){
        dosMessage2 = data;
        hasMessage2 = hasData;
    }

    public Parent gameOverScene() {

        Pane root = new Pane();

        root.setStyle("-fx-background-color: #87CEFA;");

        root.setPrefSize(1024, 600);

        Text turn = new Text("Game Over: " + reason_for_game_over);
        turn.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));


        Button backToMenuButton = new Button("Return to Main Menu");
        backToMenuButton.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        backToMenuButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //    changeScene(new Scene(menuScene()));
            }
        });

        VBox vb = new VBox(20);
        vb.setAlignment(Pos.CENTER);
        vb.setPrefWidth(1024);
        vb.getChildren().addAll(turn, backToMenuButton);
        vb.setLayoutY(300);

        root.getChildren().add(vb);

        return root;

    }

    public Parent instructionsScene() {

        Pane root = new Pane();
        root.setPrefSize(1024, 600);

        Text rule1 = new Text("Rule #1: If a player's last stone ends in its mancala, they get a free turn.");
        Text rule2 = new Text("Rule #2: If a player's last stone ends in one of their empty pits, they can steal the opponent's stones.");
        Text rule3 = new Text("Rule #3: If there are no stones in either one of the two players' pits, the game is over.");
        rule1.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 18));
        rule2.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 18));
        rule3.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 18));


        Button startPlayingButton = new Button("Start Playing!");
        startPlayingButton.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        startPlayingButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                timeSeconds.set(START_TIME);
                timerTimeline = new Timeline();
                timerTimeline.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(START_TIME + 1),
                                new KeyValue(timeSeconds, 0)));
                timerTimeline.playFromStart();

                changeScene(new Scene(serverGameScene()));
            }
        });

        VBox vb = new VBox(20);
        vb.setAlignment(Pos.CENTER);
        vb.setPrefWidth(1024);
        vb.getChildren().addAll(rule1, rule2, rule3, startPlayingButton);
        vb.setLayoutY(175);

        root.getChildren().add(vb);

        root.setStyle("-fx-background-color: #87CEFA;");

        return root;

    }

    Text turnText = new Text("Turn: Player " + Integer.toString(1));
    public Parent serverGameScene() {
        Pane root = new Pane();

        StackPane labelPane = new StackPane();

        Text serverLabel = new Text("Server");
        Rectangle labelRect = new Rectangle();
        labelRect.setHeight(50);
        labelRect.setWidth(100);
        labelRect.setFill(Color.BURLYWOOD);
        labelPane.getChildren().addAll(labelRect, serverLabel);

        root.getChildren().add(labelPane);

        root.setStyle("-fx-background-color: #87CEFA;");

        root.setPrefSize(1024, 600);
        //gm = new GameManager(NUMBER_OF_HOUSES_PER_SIDE, SEEDS_PER_HOUSE, IS_RANDOM_DISTRIBUTION_OF_SEEDS, player1name, player2name, player1Type, player2Type);
        turnPane = new StackPane();
        topPane = new StackPane[sm.getPlayer2().length]; //first element is the left pit
        bottomPane = new StackPane[sm.getPlayer1().length]; //last element is the right pit

        //create the board to show the turn
        //Text turn = new Text("Turn: Player " + Integer.toString(gm.getTurn()));


        String name = "";
//        if(TURN == 1) {
//            name = gm.getPlayer1Name();
//        }
//        else {
//            name = gm.getPlayer2Name();
//        }

        turnText.setText("Turn: " + name);


        Rectangle turnRect = new Rectangle();
        turnRect.setHeight(50);
        turnRect.setWidth(100);
        turnRect.setFill(Color.AQUAMARINE);

        turnPane.setLayoutX(50);
        turnPane.setLayoutY(500);
        turnPane.getChildren().addAll(turnRect,turnText);

        root.getChildren().addAll(turnPane);

        /*
            Create left pit
         */
//        StackPane leftPit = new StackPane();
//        leftPit.setLayoutX(145);
//        leftPit.setLayoutY(150);

        int boardWidth = sm.getPlayer1().length * 90;
        int margin = (int)((root.getPrefWidth() - boardWidth) / 2) - 45;

        System.out.println(margin + " -- " + boardWidth + " -- " + root.getPrefWidth());


        topPane[0] = new StackPane();
        // topPane[0].setLayoutX(145);
        topPane[0].setLayoutX(margin);
        topPane[0].setLayoutY(150);

        Rectangle pitRect = new Rectangle();
        pitRect.setHeight(166);
        pitRect.setWidth(66);

        Text pitText = new Text("0");
        pitText.setFill(Color.WHITE);

        //leftPit.getChildren().addAll(pitRect, pitText);
        topPane[0].getChildren().addAll(pitRect,pitText);

        //root.getChildren().add(leftPit);
        root.getChildren().add(topPane[0]);

//        StackPane rightPit = new StackPane();
//        rightPit.setLayoutX(780);
//        rightPit.setLayoutY(150);
        bottomPane[bottomPane.length-1] = new StackPane();
        bottomPane[bottomPane.length-1].setLayoutX(margin + boardWidth);
        bottomPane[bottomPane.length-1].setLayoutY(150);

        System.out.println(margin + boardWidth - 90);

        pitRect = new Rectangle();
        pitRect.setHeight(166);
        pitRect.setWidth(66);

        pitText = new Text("0");
        pitText.setFill(Color.WHITE);

//        rightPit.getChildren().addAll(pitRect, pitText);
        bottomPane[bottomPane.length-1].getChildren().addAll(pitRect, pitText);

//         root.getChildren().add(rightPit);
        root.getChildren().add(bottomPane[bottomPane.length-1]);

        int lastX = 0;

        /*
            Create houses
         */

        try{
            for (int i = 0; i < sm.getPlayer2().length; i++) {
                int nextX = (margin + 90) + (90 * i);
            /*
                TOP PANE
             */
//            StackPane topPane = new StackPane();
//            topPane.setLayoutX(nextX);
//            topPane.setLayoutY(150);
                //the first element of topPane is the left pit
                topPane[i+1] = new StackPane();
                topPane[i+1].setLayoutX(nextX);
                topPane[i+1].setLayoutY(150);

                Rectangle rect = new Rectangle();
                rect.setFill(Color.BLUE);
                rect.setHeight(66.0);
                rect.setWidth(66.0);

                Text text = new Text();
                //text.setText(Integer.toString(i));
                text.setText(Integer.toString(sm.getPlayer2()[i+1]));
                text.setFill(Color.WHITE);

                //topPane.getChildren().addAll(rect, text);
                topPane[i+1].getChildren().addAll(rect, text);

//                topPane[i+1].setOnMouseClicked(new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent e) {
//                        serverHandleBoardHouseSelected(e);
//                    }
//                });

            /*
                BOTTOM PANE
             */
//            StackPane bottomPane = new StackPane();
//            bottomPane.setLayoutX(nextX);
//            bottomPane.setLayoutY(250);

                bottomPane[i] = new StackPane();
                bottomPane[i].setLayoutX(nextX);
                bottomPane[i].setLayoutY(250);

                Rectangle bottomRect = new Rectangle();
                bottomRect.setFill(Color.BLUE);
                bottomRect.setHeight(66.0);
                bottomRect.setWidth(66.0);

                Text bottomText = new Text();
                //bottomText.setText(Integer.toString(i));
                bottomText.setText(Integer.toString(sm.getPlayer1()[i]));
                bottomText.setFill(Color.WHITE);

//            bottomPane.getChildren().addAll(bottomRect, bottomText);4
                bottomPane[i].getChildren().addAll(bottomRect, bottomText);

                if(oneClientMode){
                    bottomPane[i].setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent e) {
                            serverHandleBoardHouseSelected(e);
                        }
                    });
                }
            /*
                Add panes to root
             */
                root.getChildren().addAll(topPane[i+1], bottomPane[i]);

                //Timer Stuff

                timerLabel.textProperty().bind(timeSeconds.asString());
                timerLabel.setTextFill(Color.RED);
                timerLabel.setStyle("-fx-font-size: 4em;");


                Button aiButton = new Button("AI");
                if(!isHuman){
                    aiButton.setVisible(false);
                }
                aiButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        serverHandleAIButton(event);
                    }
                });

                VBox vb = new VBox(20);
                vb.setAlignment(Pos.CENTER);

                vb.setPrefWidth(1024);
                vb.getChildren().addAll(timerLabel, aiButton);
                vb.setLayoutY(30);


                root.getChildren().add(vb);




                Button pieRuleButton = new Button("Pi Rule");
                pieRuleButton.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
                pieRuleButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {

                        if(myTurn) {

                            serverHandlePieRuleButton(event);


                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Pie Rule");
                            alert.setContentText("Pie Rule! Player's positions have been swapped! It is " + name + "'s turn again!");
                            alert.show();

                            pieRulePane.setVisible(false);

                            resetTimer();
                        }

                    }
                });


                pieRulePane = new StackPane();
                pieRulePane.setLayoutX(155);
                pieRulePane.setLayoutY(505);
                pieRulePane.getChildren().addAll(pieRuleButton);

                pieRulePane.setVisible(false);

                root.getChildren().addAll(pieRulePane);

            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("3**");
        }

        return root;
    }

    //Button readyButton = new Button("ready");
    Text waitingText = new Text("Waiting");
    Button gameSettingButton = new Button("setting");
    Button gameSettingButton2 = new Button("setting2");
    public Parent serverMenuScene() {
        Pane root = new Pane();

        root.setStyle("-fx-background-color: #87CEFA;");

        root.setPrefSize(1024, 600);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(150, 256, 150, 256));

//        Label portLabel = new Label("port:");
//        TextField port = new TextField ();

        waitingText.setVisible(false);

        gameSettingButton.setVisible(false);
        gameSettingButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                oneClientMode = true;
                changeScene(new Scene(gameSettingScene()));
            }
        });

        gameSettingButton2.setVisible(false);
        gameSettingButton2.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                oneClientMode = false;
                changeScene(new Scene(gameSettingSceneTwoClients()));
            }
        });

        Button oneClientButton = new Button("one client");
        Button twoClientButton = new Button("two client");

        oneClientButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Thread socketServerThread = new Thread(new SocketServerThread());
                socketServerThread.setDaemon(true);
                socketServerThread.start();

                waitingText.setVisible(true);
                oneClientButton.setDisable(true);
                twoClientButton.setDisable(true);
            }
        });

        twoClientButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Thread socketServerThread = new Thread(new SocketServerThreadTwoClients());
                socketServerThread.setDaemon(true);
                socketServerThread.start();

                waitingText.setVisible(true);
                oneClientButton.setDisable(true);
                twoClientButton.setDisable(true);
            }
        });

        grid.add(oneClientButton, 1, 4);
        grid.add(twoClientButton, 1, 8);

        grid.add(waitingText, 1, 6);
        grid.add(gameSettingButton,2,7);
        grid.add(gameSettingButton2,2,8);
//        grid.add(portLabel, 1, 5);
//        grid.add(port, 2, 5);

        root.getChildren().addAll(grid);

        return root;
    }

    public Parent gameSettingScene() {
        Pane root = new Pane();

        root.setStyle("-fx-background-color: #87CEFA;");

        root.setPrefSize(1024, 600);

        Text welcome = new Text("Welcome");
        welcome.setFont(new Font("Comic Sans MS", 36.0));
        welcome.setX(404);
        welcome.setY(85);

        Text player1 = new Text("Player 1");
        player1.setFont(new Font("Comic Sans MS", 32.0));
        player1.setX(211);
        player1.setY(165);
        player1.setUnderline(true);

        Text player2 = new Text("Player 2");
        player2.setFont(new Font("Comic Sans MS", 32.0));
        player2.setX(386);
        player2.setY(164);
        player2.setUnderline(true);

        Text rules = new Text("Number of Pits");
        rules.setFont(new Font("Comic Sans MS", 32.0));
        rules.setX(446.0);
        rules.setY(162.0);
        rules.setUnderline(true);

//        Button startGame = new Button("Start Game");
//        startGame.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                changeScene(new Scene(serverGameScene()));
//            }
//        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(150, 256, 150, 256));

        // Category in column 2, row 1
        Text category = new Text("Welcome!");
        category.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        grid.add(category, 1, 0);

        // Left label in column 1 (bottom), row 3
        ObservableList<String> options =
                FXCollections.observableArrayList(
                        "Human",
                        "Easy AI",
                        "Medium AI",
                        "Hard AI"
                );
        ComboBox playerBox = new ComboBox(options);

        GridPane.setValignment(playerBox, VPos.BOTTOM);


        Text serverLabel = new Text("Server");
        serverLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

        TextField name1 = new TextField();
        name1.setPromptText("Enter Player 1 name");

        TextField name2 = new TextField();
        name2.setPromptText("Enter Player 2 name");

        grid.add(serverLabel, 0, 2);
        grid.add(name1, 0, 3);
        grid.add(playerBox, 0, 4);

        Text rulesLabel = new Text("Rules");
        rulesLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

        grid.add(rulesLabel, 2, 2);

        Slider NUMBER_OF_HOUSES_PER_SIDESlider = new Slider();
        NUMBER_OF_HOUSES_PER_SIDESlider.setMin(4);
        NUMBER_OF_HOUSES_PER_SIDESlider.setMax(9);
        NUMBER_OF_HOUSES_PER_SIDESlider.setValue(6);
        NUMBER_OF_HOUSES_PER_SIDESlider.setSnapToTicks(true);
        NUMBER_OF_HOUSES_PER_SIDESlider.setShowTickLabels(true);
        NUMBER_OF_HOUSES_PER_SIDESlider.setShowTickMarks(true);
        NUMBER_OF_HOUSES_PER_SIDESlider.setMajorTickUnit(1);
        NUMBER_OF_HOUSES_PER_SIDESlider.setMinorTickCount(0);
        NUMBER_OF_HOUSES_PER_SIDESlider.setBlockIncrement(1);

        Text NUMBER_OF_HOUSES_PER_SIDELabel = new Text("Number of pits: ");
        NUMBER_OF_HOUSES_PER_SIDELabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

        grid.add(NUMBER_OF_HOUSES_PER_SIDELabel, 2, 3);
        grid.add(NUMBER_OF_HOUSES_PER_SIDESlider, 2, 4);

        Slider seedsPerPitSlider = new Slider();
        seedsPerPitSlider.setMin(1);
        seedsPerPitSlider.setMax(10);
        seedsPerPitSlider.setValue(4);
        seedsPerPitSlider.setSnapToTicks(true);
        seedsPerPitSlider.setShowTickLabels(true);
        seedsPerPitSlider.setShowTickMarks(true);
        seedsPerPitSlider.setMajorTickUnit(1);
        seedsPerPitSlider.setMinorTickCount(0);
        seedsPerPitSlider.setBlockIncrement(1);

        Text seedsPerPitLabel = new Text("Seeds per pit: ");
        seedsPerPitLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

        grid.add(seedsPerPitLabel, 4, 3);
        grid.add(seedsPerPitSlider, 4, 4);

        Slider timeSecondsSlider = new Slider();
        timeSecondsSlider.setMin(10);
        timeSecondsSlider.setMax(30);
        timeSecondsSlider.setValue(15);
        timeSecondsSlider.setSnapToTicks(true);
        timeSecondsSlider.setShowTickLabels(true);
        timeSecondsSlider.setShowTickMarks(true);
        timeSecondsSlider.setMajorTickUnit(1);
        timeSecondsSlider.setMinorTickCount(0);
        timeSecondsSlider.setBlockIncrement(1);

        Text timeSecondsLabel = new Text("Seocnds per move: ");
        timeSecondsLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

        grid.add(timeSecondsLabel, 2, 8);
        grid.add(timeSecondsSlider, 2, 9);

        Pane cbPane = new Pane();
        CheckBox randomSeedGen = new CheckBox();
        Text seedGenLabel = new Text("Random seed generation? ");
        seedGenLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

        cbPane.getChildren().addAll(seedGenLabel, randomSeedGen);

        Pane cbPane1 = new Pane();
        CheckBox clientFirst = new CheckBox();
        Text turnLabel = new Text("client 1 first? ");
        turnLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        cbPane1.getChildren().addAll(turnLabel, clientFirst);

        grid.add(cbPane, 2, 6);
        grid.add(cbPane1, 2, 7);

        Button startGameButton = new Button("Start Game");
        startGameButton.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        startGameButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (playerBox.getValue() == null) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Warning");
                    alert.setHeaderText("Invalid Input");
                    alert.setContentText("Please select server type.");

                    alert.showAndWait();
                } else {
                    System.out.println("server: " + playerBox.getValue().toString());
                    System.out.println("Num seeds: " + NUMBER_OF_HOUSES_PER_SIDESlider.getValue());
                    System.out.println("Seeds per pit: " + seedsPerPitSlider.getValue());
                    System.out.println("Random seed generation? " + (randomSeedGen.isSelected() ? " yes" : " no"));

                    NUMBER_OF_HOUSES_PER_SIDE = (int)NUMBER_OF_HOUSES_PER_SIDESlider.getValue();
                    SEEDS_PER_HOUSE = (int)seedsPerPitSlider.getValue();
                    START_TIME = (int)timeSecondsSlider.getValue();
                    timeSeconds.set(START_TIME);
                    IS_RANDOM_DISTRIBUTION_OF_SEEDS = randomSeedGen.isSelected();

                    playerType = PLAYER_TYPE.get(playerBox.getValue().toString()); //server type

                    if(playerBox.getValue() != null && !playerBox.getValue().equals("Human")){
                        isHuman = false;
                    }
                    else{
                        isHuman = true;
                    }

                    myTurn = (!clientFirst.isSelected());

                    if (name1.getText() == null || name1.getText().trim().isEmpty()) {
                        player1name = "Player 1";
                    }
                    else {
                        player1name = name1.getText();
                    }
                    if (name2.getText() == null || name2.getText().trim().isEmpty()) {
                        player2name = "Player 2";
                    }
                    else {
                        player2name = name2.getText();
                    }
                    // System.out.println(name2.getText()+"!");

                    StringBuilder gameSetting = new StringBuilder("INFO");
                    gameSetting.append(" ");
                    gameSetting.append(NUMBER_OF_HOUSES_PER_SIDE);
                    gameSetting.append(" ");
                    gameSetting.append(SEEDS_PER_HOUSE);
                    gameSetting.append(" ");
                    //set the client to player 1 in default
                    gameSetting.append(START_TIME);
                    gameSetting.append(" ");
                    if(clientFirst.isSelected()){
                        gameSetting.append("F"); //client first
                    }
                    else{
                        gameSetting.append("S");
                    }
                    gameSetting.append(" ");

                    sm = new ServerManager(NUMBER_OF_HOUSES_PER_SIDE, SEEDS_PER_HOUSE, IS_RANDOM_DISTRIBUTION_OF_SEEDS, "", playerType);

                    if(!IS_RANDOM_DISTRIBUTION_OF_SEEDS){
                        gameSetting.append("S");
                        gameSetting.append(" ");
                    }
                    else{
                        gameSetting.append("R");
                        gameSetting.append(" ");
                        int[] player1 = sm.getPlayer1();

                        for(int i : player1){
                            gameSetting.append(i);
                            gameSetting.append(" ");
                        }
                    }
                    setDataToProcess(true, gameSetting.toString());
                    //changeScene(new Scene(instructionsScene()));
                    changeScene(new Scene(serverGameScene()));
                }
            }
        });

        grid.add(startGameButton, 0, 6);

        root.getChildren().add(grid);

        return root;
    }

    public Parent gameSettingSceneTwoClients() {
        Pane root = new Pane();

        root.setStyle("-fx-background-color: #87CEFA;");

        root.setPrefSize(1024, 600);

        Text welcome = new Text("Welcome");
        welcome.setFont(new Font("Comic Sans MS", 36.0));
        welcome.setX(404);
        welcome.setY(85);
//
//        Text player1 = new Text("Player 1");
//        player1.setFont(new Font("Comic Sans MS", 32.0));
//        player1.setX(211);
//        player1.setY(165);
//        player1.setUnderline(true);
//
//        Text player2 = new Text("Player 2");
//        player2.setFont(new Font("Comic Sans MS", 32.0));
//        player2.setX(386);
//        player2.setY(164);
//        player2.setUnderline(true);

        Text rules = new Text("Number of Pits");
        rules.setFont(new Font("Comic Sans MS", 32.0));
        rules.setX(446.0);
        rules.setY(162.0);
        rules.setUnderline(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(150, 256, 150, 256));

        // Category in column 2, row 1
        Text category = new Text("Welcome!");
        category.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        grid.add(category, 1, 0);

        // Left label in column 1 (bottom), row 3



        Text serverLabel = new Text("Server");
        serverLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

//        TextField name1 = new TextField();
//        name1.setPromptText("Enter Player 1 name");
//
//        TextField name2 = new TextField();
//        name2.setPromptText("Enter Player 2 name");

        grid.add(serverLabel, 0, 2);
//        grid.add(name1, 0, 3);

        Text rulesLabel = new Text("Rules");
        rulesLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

        grid.add(rulesLabel, 2, 2);

        Slider NUMBER_OF_HOUSES_PER_SIDESlider = new Slider();
        NUMBER_OF_HOUSES_PER_SIDESlider.setMin(4);
        NUMBER_OF_HOUSES_PER_SIDESlider.setMax(9);
        NUMBER_OF_HOUSES_PER_SIDESlider.setValue(6);
        NUMBER_OF_HOUSES_PER_SIDESlider.setSnapToTicks(true);
        NUMBER_OF_HOUSES_PER_SIDESlider.setShowTickLabels(true);
        NUMBER_OF_HOUSES_PER_SIDESlider.setShowTickMarks(true);
        NUMBER_OF_HOUSES_PER_SIDESlider.setMajorTickUnit(1);
        NUMBER_OF_HOUSES_PER_SIDESlider.setMinorTickCount(0);
        NUMBER_OF_HOUSES_PER_SIDESlider.setBlockIncrement(1);

        Text NUMBER_OF_HOUSES_PER_SIDELabel = new Text("Number of pits: ");
        NUMBER_OF_HOUSES_PER_SIDELabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

        grid.add(NUMBER_OF_HOUSES_PER_SIDELabel, 2, 3);
        grid.add(NUMBER_OF_HOUSES_PER_SIDESlider, 2, 4);

        Slider seedsPerPitSlider = new Slider();
        seedsPerPitSlider.setMin(1);
        seedsPerPitSlider.setMax(10);
        seedsPerPitSlider.setValue(4);
        seedsPerPitSlider.setSnapToTicks(true);
        seedsPerPitSlider.setShowTickLabels(true);
        seedsPerPitSlider.setShowTickMarks(true);
        seedsPerPitSlider.setMajorTickUnit(1);
        seedsPerPitSlider.setMinorTickCount(0);
        seedsPerPitSlider.setBlockIncrement(1);

        Text seedsPerPitLabel = new Text("Seeds per pit: ");
        seedsPerPitLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

        grid.add(seedsPerPitLabel, 4, 3);
        grid.add(seedsPerPitSlider, 4, 4);

        Slider timeSecondsSlider = new Slider();
        timeSecondsSlider.setMin(10);
        timeSecondsSlider.setMax(30);
        timeSecondsSlider.setValue(15);
        timeSecondsSlider.setSnapToTicks(true);
        timeSecondsSlider.setShowTickLabels(true);
        timeSecondsSlider.setShowTickMarks(true);
        timeSecondsSlider.setMajorTickUnit(1);
        timeSecondsSlider.setMinorTickCount(0);
        timeSecondsSlider.setBlockIncrement(1);

        Text timeSecondsLabel = new Text("Seocnds per move: ");
        timeSecondsLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

        grid.add(timeSecondsLabel, 2, 8);
        grid.add(timeSecondsSlider, 2, 9);

        Pane cbPane = new Pane();
        CheckBox randomSeedGen = new CheckBox();
        Text seedGenLabel = new Text("Random seed generation? ");
        seedGenLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));

        cbPane.getChildren().addAll(seedGenLabel, randomSeedGen);

        Pane cbPane1 = new Pane();
        CheckBox clientFirst1 = new CheckBox();
        Text turnLabel = new Text("client 1 first? ");
        turnLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        cbPane1.getChildren().addAll(turnLabel, clientFirst1);

        grid.add(cbPane, 2, 6);
        grid.add(cbPane1, 2, 7);

        Button startGameButton = new Button("Start Game");
        startGameButton.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        startGameButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Num seeds: " + NUMBER_OF_HOUSES_PER_SIDESlider.getValue());
                System.out.println("Seeds per pit: " + seedsPerPitSlider.getValue());
                System.out.println("Random seed generation? " + (randomSeedGen.isSelected() ? " yes" : " no"));

                NUMBER_OF_HOUSES_PER_SIDE = (int)NUMBER_OF_HOUSES_PER_SIDESlider.getValue();
                SEEDS_PER_HOUSE = (int)seedsPerPitSlider.getValue();
                START_TIME = (int)timeSecondsSlider.getValue();
                IS_RANDOM_DISTRIBUTION_OF_SEEDS = randomSeedGen.isSelected();

                client1Turn = (clientFirst1.isSelected());

//                    if (name1.getText() == null || name1.getText().trim().isEmpty()) {
//                        player1name = "Player 1";
//                    }
//                    else {
//                        player1name = name1.getText();
//                    }
//                    if (name2.getText() == null || name2.getText().trim().isEmpty()) {
//                        player2name = "Player 2";
//                    }
//                    else {
//                        player2name = name2.getText();
//                    }
                // System.out.println(name2.getText()+"!");

                StringBuilder gameSetting = new StringBuilder("INFO");
                gameSetting.append(" ");
                gameSetting.append(NUMBER_OF_HOUSES_PER_SIDE);
                gameSetting.append(" ");
                gameSetting.append(SEEDS_PER_HOUSE);
                gameSetting.append(" ");
                //set the client to player 1 in default
                gameSetting.append(START_TIME);
                gameSetting.append(" ");
                if(clientFirst1.isSelected()){
                    gameSetting.append("F"); //client1 first
                }
                else{
                    gameSetting.append("S");
                }
                gameSetting.append(" ");

                sm = new ServerManager(NUMBER_OF_HOUSES_PER_SIDE, SEEDS_PER_HOUSE, IS_RANDOM_DISTRIBUTION_OF_SEEDS, "", playerType);

                if(!IS_RANDOM_DISTRIBUTION_OF_SEEDS){
                    gameSetting.append("S");
                    gameSetting.append(" ");
                }
                else{
                    gameSetting.append("R");
                    gameSetting.append(" ");
                    int[] player1 = sm.getPlayer1();

                    for(int i : player1){
                        gameSetting.append(i);
                        gameSetting.append(" ");
                    }
                }
                System.out.println("before set data 1");
                setDataToProcess(true, gameSetting.toString());
                System.out.println("after set data 2");

                StringBuilder gameSetting2 = new StringBuilder("INFO");
                gameSetting2.append(" ");
                gameSetting2.append(NUMBER_OF_HOUSES_PER_SIDE);
                gameSetting2.append(" ");
                gameSetting2.append(SEEDS_PER_HOUSE);
                gameSetting2.append(" ");
                //set the client to player 1 in default
                gameSetting2.append(START_TIME);
                gameSetting2.append(" ");
                if(clientFirst1.isSelected()){
                    gameSetting2.append("S"); //client1 first
                }
                else{
                    gameSetting2.append("F");
                }
                gameSetting2.append(" ");

                if(!IS_RANDOM_DISTRIBUTION_OF_SEEDS){
                    gameSetting2.append("S");
                    gameSetting2.append(" ");
                }
                else{
                    gameSetting2.append("R");
                    gameSetting2.append(" ");
                    int[] player1 = sm.getPlayer1();

                    for(int i : player1){
                        gameSetting2.append(i);
                        gameSetting2.append(" ");
                    }
                }
                System.out.println("before set data 2");
                setDataToProcess2(true, gameSetting2.toString());
                System.out.println("after set data 2");
                changeScene(new Scene(instructionsScene()));
                changeScene(new Scene(serverGameScene()));

            }
        });

        grid.add(startGameButton, 0, 6);

        root.getChildren().add(grid);

        return root;
    }

    @Override
    public void start(Stage stage) throws IOException
    {
        globalStage = stage;

        //Parent root = FXMLLoader.load(getClass().getResource("SplashScreen.fxml"));
        Parent root = new StackPane();

        Scene scene = new Scene(root, 1024, 600);

        /*
        timeSeconds.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (timeSeconds.getValue().intValue() == 0) {
                    //send "Time" to client
//                    if(isServer){
//                        updateBoard();
//                    }
                }
            }
        });
        */


        globalStage.setScene(scene);
        globalStage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2000), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                changeScene(new Scene(serverMenuScene()));
            }
        }));

        timeline.play();
    }

    public void handleStartGame(ActionEvent e) {
        System.exit(0);
    }

    public static boolean isNumeric(String str)  {
        try
        {
            int d = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public void resetTimer(){
        if (timerTimeline != null) {
            timerTimeline.stop();
        }
        timeSeconds.set(START_TIME);
        //timerTimeline = new Timeline();
        timerTimeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(START_TIME+1),
                        new KeyValue(timeSeconds, 0)));
        timerTimeline.playFromStart();
    }

    ArrayList<Integer> movements = new ArrayList<Integer>();
    public void serverHandleBoardHouseSelected(MouseEvent event) {
        if(!myTurn || !isGameStart){
            return;
        }
        else{
            try {
                //move the seeds in server pits
                StackPane[] player = bottomPane;
                StackPane selected = (StackPane) event.getSource();
                int index = Arrays.asList(player).indexOf(selected);

                Alert alert = new Alert(AlertType.INFORMATION);
                //rules checking
                String message = "";
                if(!sm.isMoveLegal(SERVER, index)){
                    message = "ILLEGAL WINNER";
                    alert.setTitle("LOSER");
                    alert.showAndWait();
                    changeScene(new Scene(gameOverScene()));
                }
                else if(!sm.isOnTime()){
                    message = "TIME WINNER";
                    alert.setTitle("LOSER");
                    alert.showAndWait();
                    changeScene(new Scene(gameOverScene()));
                }

                sm.move(index, SERVER);
                movements.add(index);
                updateBoardByServerManager();

                if(sm.checkGameOver() && message.toUpperCase().equals("")){
                    if(sm.winner.equals("SERVER")){
                        message = "LOSER"; // send to the client
                        alert.setTitle("WINNER");
                        alert.showAndWait();
                    }
                    else if(sm.winner.equals("CLIENT")){
                        message = "WINNER";
                        alert.setTitle("LOSER");
                        alert.showAndWait();
                    }
                    else{
                        message = "TIE";
                        alert.setTitle("TIE");
                        alert.showAndWait();
                    }
                    changeScene(new Scene(gameOverScene()));
                }

                if(!sm.hasAdditionalMove()){
                    updateMyTurn();
                    StringBuilder moves = new StringBuilder();
                    for(int i : movements){
                        moves.append(i + 1);
                        moves.append(" ");
                    }
                    if(message != null && message.length() > 0){
                        moves.append(message);
                    }
                    setDataToProcess(true, moves.toString());
                    //updateBoardByServerManager();
                    movements.clear();
                }

//                    resetTimer();
            } catch (Exception ex) {
                Logger.getLogger(ServerGUI1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void handleKeyPressed(KeyEvent event){
        if(!myTurn || !isGameStart){
            return;
        }
        System.out.println("Key Pressed: " + event.getText());
        String key = event.getText();
        if(key.length() ==1 && Character.isDigit(key.charAt(0))){
            try {
                int index = key.charAt(0)-'1';

                Alert alert = new Alert(AlertType.INFORMATION);
                //rules checking
                String message = "";
                if(!sm.isMoveLegal(SERVER, index)){
                    message = "ILLEGAL WINNER";
                    alert.setTitle("LOSER");
                    alert.showAndWait();
                    changeScene(new Scene(gameOverScene()));
                }
                else if(!sm.isOnTime()){
                    message = "TIME WINNER";
                    alert.setTitle("LOSER");
                    alert.showAndWait();
                    changeScene(new Scene(gameOverScene()));
                }

                sm.move(index, SERVER);
                movements.add(index);
                updateBoardByServerManager();

                if(sm.checkGameOver() && message.toUpperCase().equals("OK")){
                    if(sm.winner.equals("SERVER")){
                        message = "LOSER"; // send to the client
                        alert.setTitle("WINNER");
                        alert.showAndWait();
                    }
                    else if(sm.winner.equals("CLIENT")){
                        message = "WINNER";
                        alert.setTitle("LOSER");
                        alert.showAndWait();
                    }
                    else{
                        message = "TIE";
                        alert.setTitle("TIE");
                        alert.showAndWait();
                    }
                    changeScene(new Scene(gameOverScene()));
                }

                if(!sm.hasAdditionalMove()){
                    updateMyTurn();
                    StringBuilder moves = new StringBuilder();
                    for(int i : movements){
                        moves.append(i + 1);
                        moves.append(" ");
                    }

                    if(message != null && message.length() > 0){
                        moves.append(message);
                    }

                    setDataToProcess(true, moves.toString());
                    //updateBoardByServerManager();
                    movements.clear();
                }
            } catch (Exception ex) {
                Logger.getLogger(ServerGUI1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void serverHandleAIButton(MouseEvent event) {
        if(!isGameStart){
            return;
        }
        serverAIMove();
    }

    public void serverHandlePieRuleButton(MouseEvent event) {

        if(!myTurn || !isGameStart) {
            return;
        }
        else {
            sm.doPieRule();
            updateMyTurn();
            updateBoardByServerManager();
            setDataToProcess(true, "P");
        }

    }

    public void serverAIMove() {
        if(!myTurn){
            return;
        }
        else{
            try {
                sm.AIMove(SERVER);
                movements.addAll(sm.getAIMovement());
                updateMyTurn();
                StringBuilder moves = new StringBuilder();
                for(int i : movements){
                    moves.append(i + 1);
                    moves.append(" ");
                }

                if(sm.checkGameOver()){
                    Alert alert = new Alert(AlertType.INFORMATION);

                    if(sm.winner.equals("SERVER")){
                        moves.append("LOSER "); // send to the client
                        alert.setTitle("WINNER");
                    }
                    else if(sm.winner.equals("CLIENT")){
                        moves.append("WINNER ");
                        alert.setTitle("LOSER");
                    }
                    else{
                        moves.append("TIE ");
                        alert.setTitle("TIE");
                    }
                    alert.showAndWait();
                    TimeUnit.SECONDS.sleep(8);
                    changeScene(new Scene(gameOverScene()));
                }
                updateBoardByServerManager();
                setDataToProcess(true, moves.toString());
                movements.clear();
            } catch (Exception ex) {
                Logger.getLogger(ServerGUI1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void moveClient(String index) {
        try {
            if(index == null || index.length () == 0){
                updateMyTurn();
                throw new Exception("error in moveServer()");
            }

            String[] indexes = index.split(" ");

            String message = "OK";
            if(index.equals("P")) {
                sm.doPieRule();
            }
            else {
                int i = 0;
                while(i < indexes.length){
                    int in = Integer.parseInt(indexes[i]);

                    Alert alert = new Alert(AlertType.INFORMATION);
                    //rules checking\
                    if(!sm.isMoveLegal(CLIENT, sm.getPlayer2().length - in)){
                        message = "ILLEGAL LOSER";
                        alert.setTitle("WINNER");
                        alert.show();
                        TimeUnit.SECONDS.sleep(8);
                        changeScene(new Scene(gameOverScene()));
                        break;
                    }
                    else if(!sm.isOnTime()){
                        message = "TIME LOSER";
                        alert.setTitle("WINNER");
                        alert.show();
                        TimeUnit.SECONDS.sleep(8);
                        changeScene(new Scene(gameOverScene()));
                        break;
                    }

                    sm.move(sm.getPlayer2().length - in, CLIENT);  //the top pane is reverse

                    if(sm.checkGameOver() && message.toUpperCase().equals("OK")){
                        if(sm.winner.equals("SERVER")){
                            message = "LOSER"; // send to the client
                            alert.setTitle("WINNER");
                            alert.show();
                            TimeUnit.SECONDS.sleep(8);
                            changeScene(new Scene(gameOverScene()));
                            break;
                        }
                        else if(sm.winner.equals("CLIENT")){
                            message = "WINNER";
                            alert.setTitle("LOSER");
                            alert.show();
                            TimeUnit.SECONDS.sleep(8);
                            changeScene(new Scene(gameOverScene()));
                        }
                        else{
                            message = "TIE";
                            alert.setTitle("TIE");
                            alert.show();
                            TimeUnit.SECONDS.sleep(8);
                            changeScene(new Scene(gameOverScene()));
                        }
                    }

                    i++;
                }
            }


            setDataToProcess(true, message);
            updateBoardByServerManager();
            updateMyTurn();
        } catch (Exception ex) {
            Logger.getLogger(ServerGUI1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void moveTwoClients(String index, int turn) {
        try {
            if(index == null || index.length () == 0){
                updateMyTurn();
                throw new Exception("error in moveServer()");
            }

            if(turn == CLIENT1){
                String[] indexes = index.split(" ");

                String message = "OK";
                String message2 = index;
                int i = 0;
                while(i < indexes.length){
                    int in = Integer.parseInt(indexes[i]);

                    //rules checking\
                    if(!sm.isMoveLegal(CLIENT1, in - 1)){
                        message = " ILLEGAL LOSER";
                        message2 += " ILLEGAL WINNER";
                        break;
                    }
                    else if(!sm.isOnTime()){
                        message = "TIME LOSER";
                        message2 += " TIME WINNER";
                        break;
                    }

                    sm.move(in - 1, CLIENT1);  //the top pane is reverse

                    if(sm.checkGameOver() && message.toUpperCase().equals("OK")){
                        if(sm.winner.equals("SERVER")){
                            message = " WINNVER";
                            message2 += " LOSER"; // send to the client
                            break;
                        }
                        else if(sm.winner.equals("CLIENT")){
                            message = "LOSER";
                            message2 += " WINNER";
                        }
                        else{
                            message = "TIE";
                            message2 += " TIE";
                        }
                    }
                    i++;
                }

                setDataToProcess(true, message);
                setDataToProcess2(true, message2);
                updateBoardByServerManager();
            }

            else if(turn == CLIENT2){
                String[] indexes = index.split(" ");

                String message = index;
                String message2 = "OK";
                int i = 0;
                while(i < indexes.length){
                    int in = Integer.parseInt(indexes[i]);

                    //rules checking\
                    if(!sm.isMoveLegal(CLIENT2, sm.getPlayer2().length - in)){
                        message += " ILLEGAL WINNER";
                        message2 = "ILLEGAL LOSER";
                        break;
                    }
                    else if(!sm.isOnTime()){
                        message += " TIME WINNER";
                        message2 = "ILLEGAL LOSER";
                        break;
                    }

                    sm.move(sm.getPlayer2().length - in, CLIENT2);  //the top pane is reverse

                    if(sm.checkGameOver() && message2.toUpperCase().equals("OK")){
                        if(sm.winner.equals("SERVER")){
                            message += " WINNVER";
                            message2 = "LOSER"; // send to the client
                            break;
                        }
                        else if(sm.winner.equals("CLIENT")){
                            message += " LOSER";
                            message2 = "WINNER";
                        }
                        else{
                            message += " TIE";
                            message2 = "TIE";
                        }
                    }
                    i++;
                }

                System.out.println("message: " + message);
                System.out.println("message2: " + message2);
                setDataToProcess(true, message);
                setDataToProcess2(true, message2);
                updateBoardByServerManager();
            }

        } catch (Exception ex) {
            Logger.getLogger(ServerGUI1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void updateMyTurn() {
        myTurn = myTurn == false ? true : false;

        if(myTurn){
            turnText.setText("Turn: Myself");
        }
        else{
            turnText.setText("Turn: Client");
        }
    }

    public void updateBoardByServerManager() {
        //update the  pane
        for(int i = 0; i < bottomPane.length; i++){
            try {
                Text t = (Text)bottomPane[i].getChildren().get(bottomPane[i].getChildren().size() - 1);
                t.setText(Integer.toString((sm.getSeedNumber(SERVER, i))));
            } catch (Exception ex) {
                Logger.getLogger(ServerGUI1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //update top pane
        for(int i = 0; i < topPane.length; i++){
            try {
                Text t = (Text)topPane[i].getChildren().get(topPane[i].getChildren().size() - 1);
                t.setText(Integer.toString((sm.getSeedNumber(CLIENT, i))));
            } catch (Exception ex) {
                Logger.getLogger(ServerGUI1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }



        if(is_second_turn) {
            pieRulePane.setVisible(false);
            is_second_turn = false;
        }
        if(is_first_turn) {
            is_first_turn = false;
            pieRulePane.setVisible(true);
            is_second_turn = true;
        }
    }

    public void changeScene(Scene scene) {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent e) {
                handleKeyPressed(e);
            }
        });
        globalStage.setScene(scene);
    }

    public void processNotification(String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("client send: " + content);
        alert.show();

        if(content.toUpperCase().equals("ILLEGAL") || content.toUpperCase().equals("TIME") || content.toUpperCase().equals("LOSER") ||
                content.toUpperCase().equals("WINNER") || content.toUpperCase().equals("TIE")){
            reason_for_game_over = content;
            changeScene(new Scene(gameOverScene()));
        }
    }

    public void processNotificationTwoClients(String content, int turn) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("client" + turn +" send: " + content);
        alert.show();

        if(content.toUpperCase().equals("ILLEGAL") || content.toUpperCase().equals("TIME") || content.toUpperCase().equals("LOSER") ||
                content.toUpperCase().equals("WINNER") || content.toUpperCase().equals("TIE")){
            changeScene(new Scene(gameOverScene()));
        }
    }

    private class SocketServerThread extends Thread{
        static final int socketServerPort = 2000;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        ServerSocket serverSocket = null;

        @Override
        public void run() {

            try {
                serverSocket = new ServerSocket(socketServerPort);

                Socket socket = serverSocket.accept();

                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        waitingText.setText("connected");
                        gameSettingButton.setVisible(true);
                    }
                });

                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());

                timeSeconds.addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if (timeSeconds.getValue().intValue() == 0) {
                            try{
                                dos.flush();
                                dos.writeUTF("TIME");
                                dos.flush();
                                processNotification("TIME");
                            } catch (Exception e) {
                                return;
                            }

                        }
                    }
                });

                //get delay between server and client
                long startTime = System.nanoTime();
                dos.writeUTF("WELCOME");
                dos.flush();
                String x = dis.readUTF();
                delay = System.nanoTime() - startTime;
                System.out.println("Delay:" + delay);

                //send game config
                while(!hasDataToProcess()){}

                dos.writeUTF(dosMessage);
                dos.flush();
                setDataToProcess(false, "");
                //get READY from client
                String res = dis.readUTF();
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        processNotification(res);
                        isGameStart = true;
                    }
                });

                timerTimeline = new Timeline();

                if(!myTurn) {

                    //account for network delay by waiting for time to send and recieve something to client
                    try {
                        TimeUnit.NANOSECONDS.sleep(2*delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    timeSeconds.set(START_TIME );
                    timerTimeline.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(START_TIME + 1),
                                    new KeyValue(timeSeconds, 0)));
                    timerTimeline.playFromStart();

                    System.out.println("stay here 1");
                    //receive the client movement
                    String s = dis.readUTF();

                    timerTimeline.stop();


                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            moveClient(s);
                        }
                    });
                    //send notification
                    while(!hasDataToProcess()){}

                    dos.writeUTF(dosMessage);


                    dos.flush();
                    setDataToProcess(false, "");
                }

                while(true) {

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            resetTimer();
                        }
                    });

                    System.out.println("stay here 2");

                    //check AI move
                    if(!isHuman){
                        TimeUnit.SECONDS.sleep(4);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run(){
                                serverAIMove();
                            }
                        });
                    }

                    //send server move
                    while(!hasDataToProcess()){}

                    dos.writeUTF(dosMessage);

                    timerTimeline.stop();

                    dos.flush();
                    setDataToProcess(false, "");

                    //wait for reply from client
                    String s0 =dis.readUTF();

                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            processNotification(s0);
                        }
                    });

                    //account for network delay
                    try {
                        TimeUnit.NANOSECONDS.sleep(2*delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            resetTimer();
                        }
                    });

                    //wait for client move
                    System.out.println("stay here 3");
                    String s = dis.readUTF();
                    timerTimeline.stop();

                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            moveClient(s);
                        }
                    });

                    //send notification
                    while(!hasDataToProcess()){}

                    dos.writeUTF(dosMessage);
                    dos.flush();
                    setDataToProcess(false, "");

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            resetTimer();
                        }
                    });
//
//                Platform.runLater(new Runnable(){
//                    @Override
//                    public void run() {
//                        rule1.setText(confirm);
//                    }
//                });
                }
            } catch (IOException ex) {
                Logger.getLogger(FakeServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerGUI1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class SocketServerThreadTwoClients extends Thread {
        static final int socketServerPort = 2000;
        DataInputStream[] dis = new DataInputStream[2];
        DataOutputStream[] dos = new DataOutputStream[2];
        ServerSocket serverSocket = null;
        Socket[] sockets = new Socket[2];

        @Override
        public void run() {

            try {
                serverSocket = new ServerSocket(socketServerPort);


                sockets[0] = serverSocket.accept();
                dis[0] = new DataInputStream(sockets[0].getInputStream());
                dos[0] = new DataOutputStream(sockets[0].getOutputStream());

                timeSeconds.addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        if (timeSeconds.getValue().intValue() == 0) {
                            try{
                                dos[0].flush();
                                dos[0].writeUTF("TIME");
                                dos[1].flush();
                                dos[1].writeUTF("TIME");
                                processNotification("TIME");
                            } catch (Exception e) {
                                return;
                            }

                        }
                    }
                });


                long startTime1 = System.nanoTime();
                dos[0].writeUTF("WELCOME");
                dos[0].flush();
                String x = dis[0].readUTF();
                delay1 = System.nanoTime() - startTime1;
                System.out.println("Delay1:" + delay1);





                sockets[1] = serverSocket.accept();

                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        waitingText.setText("connected");
                        gameSettingButton2.setVisible(true);
                    }
                });

                dis[1] = new DataInputStream(sockets[1].getInputStream());
                dos[1] = new DataOutputStream(sockets[1].getOutputStream());

                long startTime2 = System.nanoTime();
                dos[1].writeUTF("WELCOME");
                dos[1].flush();
                String y = dis[1].readUTF();
                delay2 = System.nanoTime() - startTime2;
                System.out.println("Delay2:" + delay2);


                //send game config
                while(!hasDataToProcess()){}

                dos[0].writeUTF(dosMessage);
                dos[0].flush();

                setDataToProcess(false, "");

                while(!hasDataToProcess2()){}

                dos[1].writeUTF(dosMessage2);
                dos[1].flush();
                setDataToProcess2(false, "");

                //get READY from client 1
                String res = dis[0].readUTF();
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        processNotificationTwoClients(res, CLIENT1);
                    }
                });

                //get READY from client 2
                String res2 = dis[1].readUTF();
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        processNotificationTwoClients(res2, CLIENT2);
                    }
                });
                System.out.println("stay here 3");

                timerTimeline = new Timeline();

                if(client1Turn) {
                    while(true){
                        System.out.println("stay here 1");

                        //account for network delay
                        try {
                            TimeUnit.NANOSECONDS.sleep(2*delay1 );
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        timeSeconds.set(START_TIME );
                        timerTimeline.getKeyFrames().add(
                                new KeyFrame(Duration.seconds(START_TIME + 1),
                                        new KeyValue(timeSeconds, 0)));
                        timerTimeline.playFromStart();

                        //receive the client 1 movement
                        String s = dis[0].readUTF();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                resetTimer();
                            }
                        });
                        timerTimeline.stop();
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                moveTwoClients(s, CLIENT1);
                            }
                        });
                        //send notification to client1
                        while(!hasDataToProcess()){}

                        dos[0].writeUTF(dosMessage);
                        dos[0].flush();
                        setDataToProcess(false, "");

                        //send movement to client2
                        while(!hasDataToProcess2()){}

                        dos[1].writeUTF(dosMessage2);
                        dos[1].flush();
                        setDataToProcess2(false, "");

                        //receve the client2 reply
                        String s1 = dis[1].readUTF();
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                processNotificationTwoClients(s1, CLIENT2);
                            }
                        });

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                resetTimer();
                            }
                        });

                        //receive the client2 movement
                        String s2 = dis[1].readUTF();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                resetTimer();
                            }
                        });
                        timerTimeline.stop();
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                moveTwoClients(s2, CLIENT2);
                            }
                        });

                        //send notification to client2
                        while(!hasDataToProcess2()){}

                        dos[1].writeUTF(dosMessage2);
                        dos[1].flush();
                        setDataToProcess2(false, "");

                        //send movement to client1
                        while(!hasDataToProcess()){}

                        dos[0].writeUTF(dosMessage);
                        dos[0].flush();
                        setDataToProcess(false, "");

                        //receive the client1 reply
                        String s3 = dis[0].readUTF();
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                processNotificationTwoClients(s3, CLIENT1);
                            }
                        });

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                resetTimer();
                            }
                        });
                    }
                }
                else{
                    while(true){

                        try {
                            TimeUnit.NANOSECONDS.sleep(2*delay1 );
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        timeSeconds.set(START_TIME );
                        timerTimeline.getKeyFrames().add(
                                new KeyFrame(Duration.seconds(START_TIME + 1),
                                        new KeyValue(timeSeconds, 0)));
                        timerTimeline.playFromStart();

                        //receive the client 2 movement
                        String s = dis[1].readUTF();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                resetTimer();
                            }
                        });
                        timerTimeline.stop();
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                moveTwoClients(s, CLIENT2);
                            }
                        });
                        //send notification to client2
                        while(!hasDataToProcess()){}

                        dos[1].writeUTF(dosMessage2);
                        dos[1].flush();
                        setDataToProcess2(false, "");

                        //send movement to client1
                        while(!hasDataToProcess()){}

                        dos[0].writeUTF(dosMessage);
                        dos[0].flush();
                        setDataToProcess(false, "");

                        //receve the client1 reply
                        String s1 = dis[0].readUTF();
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                processNotificationTwoClients(s1, CLIENT1);
                            }
                        });

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                resetTimer();
                            }
                        });


                        //receive the client1 movement
                        String s2 = dis[0].readUTF();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                resetTimer();
                            }
                        });
                        timerTimeline.stop();
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                moveTwoClients(s2, CLIENT1);
                            }
                        });

                        //send notification to client1
                        while(!hasDataToProcess()){}

                        dos[0].writeUTF(dosMessage);
                        dos[0].flush();
                        setDataToProcess(false, "");

                        //send movement to client2
                        while(!hasDataToProcess2()){}

                        dos[1].writeUTF(dosMessage2);
                        dos[1].flush();
                        setDataToProcess2(false, "");

                        //receive the client2 reply
                        String s3 = dis[1].readUTF();
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                processNotificationTwoClients(s3, CLIENT2);
                            }
                        });

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                resetTimer();
                            }
                        });
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(FakeServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
