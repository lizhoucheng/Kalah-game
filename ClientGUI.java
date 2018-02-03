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

public class ClientGUI extends Application {
    int NUMBER_OF_HOUSES_PER_SIDE;
    int SEEDS_PER_HOUSE;
    boolean IS_RANDOM_DISTRIBUTION_OF_SEEDS;
    int TURN;

    boolean is_first_turn = true;
    boolean is_second_turn = false;

    String player1name;
    String player2name;

    PLAYER_TYPE playerType = PLAYER_TYPE.HUMAN;

    Stage globalStage;

    ClientManager cm;

    StackPane[] topPane;

    StackPane[] bottomPane;

    StackPane turnPane;

    StackPane pieRulePane;

    /*client/server stuff*/
    public final int SERVER = 2;

    public final int CLIENT = 1;

    String dosMessage = "";

    boolean hasMessage = false;

    ServerSocket server = null;

    Socket client = null;

    Socket serverOfClient = null;

    DataOutputStream dos = null;

    DataInputStream dis = null;

    boolean isServer = false;

    boolean myTurn;

    boolean isHuman = true;

    //game state for client
    //ArrayList<int[]> gameState;

    //Timer stuff
    private static Integer START_TIME = -1;
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

                changeScene(new Scene(clientGameScene()));
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

    public Parent clientWaitingScene(){
       /* Pane root = new Pane();
        root.setPrefSize(1024, 600);
        Text clientWaiting = new Text("waiting for server to set up the game");
        clientWaiting.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 18));
        clientWaiting.setLayoutX(400);
        clientWaiting.setLayoutY(200);
        Button settingBtn = new Button("get game setting");
        settingBtn.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        settingBtn.setLayoutX(300);
        settingBtn.setLayoutY(300);
        settingBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String s1;
        try {
            s1 = dis.readUTF();
            System.out.println(s1);
            //Parse s1 for info
            char[] c = s1.toCharArray();
            String t = "";
            int playerIndex = 0;
            for (int i = 9; i < c.length; i++){
                if (c[i] != 32) { //if not space character
                    t += c[i];
                }
                else {
                    playerIndex = i+1;
                    break;
                }
            }
            NUMBER_OF_HOUSES_PER_SIDE = c[5] - 48;
            SEEDS_PER_HOUSE = c[7] - 48;
            START_TIME = Integer.parseInt(t);
            if (c[playerIndex] == 'F'){
                TURN = 1;
                myTurn = true;
            }
            else {
                TURN = 2;
                myTurn = false;
            }
            if (c[playerIndex + 2] == 'S'){
                cm = new ClientManager(NUMBER_OF_HOUSES_PER_SIDE, SEEDS_PER_HOUSE, IS_RANDOM_DISTRIBUTION_OF_SEEDS, player1name, playerType);
            }
            else{
                int[] player1 = new int[NUMBER_OF_HOUSES_PER_SIDE+1];
                player1[NUMBER_OF_HOUSES_PER_SIDE] = 0;
                int[] player2 = new int[NUMBER_OF_HOUSES_PER_SIDE+1];
                player2[0] = 0;
                int x = playerIndex + 4;
                for(int i = 0; i < NUMBER_OF_HOUSES_PER_SIDE; i++) {
                    while(c[x] == 32) {
                        x++;
                    }
                    String str_seeds = Character.toString(c[x]);
                    int num_seeds = Integer.parseInt(str_seeds);
                    player1[i] = num_seeds;
                    player2[NUMBER_OF_HOUSES_PER_SIDE-i] = num_seeds;
                    x++;
                }
                cm = new ClientManager(player1, player2, player1name, playerType);
            }
            System.out.println("Num pits: " + NUMBER_OF_HOUSES_PER_SIDE);
            System.out.println("Num seeds: " + SEEDS_PER_HOUSE);
            System.out.println("Time: " + START_TIME);
            //System.out.println("Player to go first: " + playerToGoFirst);
            System.out.println("Board (player 1 on bottom player 2 on top): ");
            //player 2
            for(int i = 0; i < NUMBER_OF_HOUSES_PER_SIDE + 1; i++) {
                System.out.print(cm.getPlayer2()[i]);
            }
            System.out.println();
            //player 1
            for(int i = 0; i < NUMBER_OF_HOUSES_PER_SIDE + 1; i++) {
                System.out.print(cm.getPlayer1()[i]);
            }
            changeScene(new Scene(instructionsScene()));
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
            }
        });
        root.getChildren().addAll(settingBtn, clientWaiting);
        */
        Parent root = new StackPane();
        return root;
    }

    Text turnText = new Text("Turn: Player " + Integer.toString(1));
    public Parent clientGameScene(){
        Pane root = new Pane();

        root.setStyle("-fx-background-color: #87CEFA;");

        root.setPrefSize(1024, 600);

        StackPane labelPane = new StackPane();

        Text serverLabel = new Text("Client");
        Rectangle labelRect = new Rectangle();
        labelRect.setHeight(50);
        labelRect.setWidth(100);
        labelRect.setFill(Color.BURLYWOOD);
        labelPane.getChildren().addAll(labelRect, serverLabel);

        root.getChildren().add(labelPane);

        //gm = new GameManager(NUMBER_OF_HOUSES_PER_SIDE, SEEDS_PER_HOUSE, IS_RANDOM_DISTRIBUTION_OF_SEEDS, player1name, player2name, player1Type, player2Type);
        turnPane = new StackPane();
        topPane = new StackPane[cm.getPlayer2().length]; //first element is the left pit
        bottomPane = new StackPane[cm.getPlayer1().length]; //last element is the right pit

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

        int boardWidth = cm.getPlayer1().length * 90;
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
            for (int i = 0; i < cm.getPlayer2().length; i++) {
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
                text.setText(Integer.toString(cm.getPlayer2()[i+1]));
                text.setFill(Color.WHITE);

                //topPane.getChildren().addAll(rect, text);
                topPane[i+1].getChildren().addAll(rect, text);

//                topPane[i+1].setOnMouseClicked(new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent e) {
//                        clientHandleBoardHouseSelected(e);
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
                bottomText.setText(Integer.toString(cm.getPlayer1()[i]));
                bottomText.setFill(Color.WHITE);

//            bottomPane.getChildren().addAll(bottomRect, bottomText);4
                bottomPane[i].getChildren().addAll(bottomRect, bottomText);

                bottomPane[i].setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        clientHandleBoardHouseSelected(e);
                    }
                });
            /*
                Add panes to root
             */
                root.getChildren().addAll(topPane[i+1], bottomPane[i]);

                //Timer Stuff

                timerLabel.textProperty().bind(timeSeconds.asString());
                timerLabel.setTextFill(Color.RED);
                timerLabel.setStyle("-fx-font-size: 4em;");



                Button aiButton = new Button("AI");
                aiButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        clientHandleAIButton(event);
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
                            clientHandlePieRuleButton(event);

                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Pie Rule");
                            alert.setContentText("Pie Rule! Player's positions have been swapped! It is " + name + "'s turn again!");
                            alert.show();

                            pieRulePane.setVisible(false);

                            resetTimer();

                        }

                    }
                });

                //pieRuleButton.setVisible(false);

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

    Button readyButton = new Button("ready");
    public Parent clientMenuScene() {
        Pane root = new Pane();

        root.setStyle("-fx-background-color: #87CEFA;");

        root.setPrefSize(1024, 600);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(150, 256, 150, 256));

        ObservableList<String> options =
                FXCollections.observableArrayList(
                        "Human",
                        "Easy AI",
                        "Medium AI",
                        "Hard AI"
                );
        ComboBox playerBox = new ComboBox(options);

        Label portLabel = new Label("port:");
        TextField port = new TextField ("2000");

        Label addrLabel = new Label("address:");
        TextField addr = new TextField ("localhost");

        Button connectButton = new Button("connect");
        connectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if((addr.getText() != null && !addr.getText().isEmpty()) && (port.getText() != null && !port.getText().isEmpty()) && isNumeric(port.getText())){
                    Thread clientThread = new Thread(new clientThread(addr.getText(), Integer.parseInt(port.getText())));
                    clientThread.setDaemon(true);
                    clientThread.start();
                    connectButton.setDisable(true);

                    if(playerBox.getValue() != null && !playerBox.getValue().equals("Human")){
                        isHuman = false;
                    }
                    else{
                        isHuman = true;
                    }
                }
            }
        });

        GridPane.setValignment(playerBox, VPos.BOTTOM);
        grid.add(playerBox, 0, 4);
        grid.add(connectButton, 1, 4);

        grid.add(portLabel, 1, 5);
        grid.add(port, 2, 5);
        grid.add(addrLabel, 1, 6);
        grid.add(addr, 2, 6);



        readyButton.setVisible(false);
        readyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setDataToProcess(true, "READY");
                changeScene(new Scene(clientGameScene()));
            }
        });
        grid.add(readyButton,2,7);

        root.getChildren().addAll(grid);

        return root;
    }

    @Override
    public void start(Stage stage) throws IOException
    {
        globalStage = stage;

        //Parent root = FXMLLoader.load(getClass().getResource("SplashScreen.fxml"));
        Parent root = new StackPane();

        Scene scene = new Scene(root, 1024, 600);

        timeSeconds.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (timeSeconds.getValue().intValue() == 0) {

//                    if(isServer){
//                        updateBoard();
//                    }
                }
            }
        });

        globalStage.setScene(scene);
        globalStage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2000), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                changeScene(new Scene(clientMenuScene()));
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
    public void clientHandleBoardHouseSelected(MouseEvent event) {
        if(!myTurn){
            return;
        }
        else{
            try {
                //move the seeds in client pits
                StackPane[] player = bottomPane;
                StackPane selected = (StackPane) event.getSource();
                int index = Arrays.asList(player).indexOf(selected);

                cm.move(index, CLIENT);
                movements.add(index);
                updateBoardByClientManager();

                if(!cm.hasAdditionalMove()){
                    updateMyTurn();
                    StringBuilder moves = new StringBuilder();
                    for(int i : movements){
                        moves.append(i + 1);
                        moves.append(" ");
                    }
                    setDataToProcess(true, moves.toString());
                    movements.clear();
                }

//                    resetTimer();
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void handleKeyPressed(KeyEvent event){
        if(!myTurn){
            return;
        }
        System.out.println("Key Pressed: " + event.getText());
        String key = event.getText();
        if(key.length() ==1 && Character.isDigit(key.charAt(0))){
            try {
                int index = key.charAt(0)-'1';

                cm.move(index, CLIENT);
                movements.add(index);
                updateBoardByClientManager();

                if(!cm.hasAdditionalMove()){
                    updateMyTurn();
                    StringBuilder moves = new StringBuilder();
                    for(int i : movements){
                        moves.append(i + 1);
                        moves.append(" ");
                    }
                    setDataToProcess(true, moves.toString());
                    movements.clear();
                }
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void clientHandleAIButton(MouseEvent event) {
        clientAIMove();
    }

    public void clientHandlePieRuleButton(MouseEvent event) {

        if(!myTurn) {
            return;
        }
        else {
            cm.doPieRule();
            updateMyTurn();
            updateBoardByClientManager();
            setDataToProcess(true, "P");
        }
    }

    public void clientAIMove() {
        if(!myTurn){
            return;
        }
        else{
            try {
                cm.AIMove(CLIENT);
                movements.addAll(cm.getAIMovement());
                updateMyTurn();
                StringBuilder moves = new StringBuilder();
                for(int i : movements){
                    moves.append(i + 1);
                    moves.append(" ");
                }
                updateBoardByClientManager();
                setDataToProcess(true, moves.toString());
                movements.clear();
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void moveServer(String index) {

        try {
            if(index == null || index.length () == 0){
                updateMyTurn();
                throw new Exception("error in moveServer()");
            }

            if(index.equals("P")) {
                cm.doPieRule();
            }
            else {
                String[] indexes = index.split(" ");
                int i = 0;
                while(i < indexes.length){
                    if(!isNumeric(indexes[i])){

                        processNotification(indexes[i]);
                        i++;

                    }
                    else{
                        int in = Integer.parseInt(indexes[i]);

                        if(in < 0 || in >= cm.getPlayer2().length){
                            updateMyTurn();
                            throw new Exception("error in moveServer()");
                        }

                        cm.move(cm.getPlayer2().length - in, SERVER);  //the top pane is reverse
                        i++;
                    }
                }
            }
            updateBoardByClientManager();
            updateMyTurn();
        } catch (Exception ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void updateMyTurn() {
        myTurn = myTurn == false;

        if(myTurn){
            turnText.setText("Turn: Myself");
        }
        else{
            turnText.setText("Turn: Server");
        }
    }

    public void clientUpdateBoard(String[] res) {
      /*  int in = 0;
                    for(int i = 0; i < p.length; i++){
                        p[i] = Integer.parseInt(res[in]);
                        in++;
                    }
                for(int i = 0; i < bottomPane.length; i++){
                    Text t = (Text)bottomPane[i].getChildren().get(bottomPane[i].getChildren().size()-1);
                    t.setText(Integer.toString(gameState.get(0)[i]));
                    t = (Text)topPane[i].getChildren().get(bottomPane[i].getChildren().size()-1);
                    t.setText(Integer.toString(gameState.get(1)[i]));
                } */
    }

    public void updateBoardByClientManager() {
        //update the  pane
        for(int i = 0; i < bottomPane.length; i++){
            try {
                Text t = (Text)bottomPane[i].getChildren().get(bottomPane[i].getChildren().size() - 1);
                t.setText(Integer.toString((cm.getSeedNumber(1, i))));
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //update top pane
        for(int i = 0; i < topPane.length; i++){
            try {
                Text t = (Text)topPane[i].getChildren().get(topPane[i].getChildren().size() - 1);
                t.setText(Integer.toString((cm.getSeedNumber(2, i))));
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if(is_second_turn) {
            pieRulePane.setVisible(false);
            is_second_turn = false;
        }
        if(is_first_turn ) {
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
        alert.setTitle("server send: " + content);
        alert.show();

        if(content.toUpperCase().equals("ILLEGAL") || content.toUpperCase().equals("TIME") || content.toUpperCase().equals("LOSER") ||
                content.toUpperCase().equals("WINNER") || content.toUpperCase().equals("TIE")){
            reason_for_game_over = content;
            changeScene(new Scene(gameOverScene()));
        }
    }

    public void initClientManager(String config) {
        System.out.println(config);
        if(!config.substring(0, 4).equals("INFO")){
            System.out.println("command incorrect");
            return;
        }
        //Parse s1 for info

        char[] c = config.toCharArray();

        String t = "";
        int playerIndex = 0;
        for (int i = 9; i < c.length; i++){
            if (c[i] != 32) { //if not space character
                t += c[i];
            }
            else {
                playerIndex = i+1;
                break;
            }
        }

        NUMBER_OF_HOUSES_PER_SIDE = c[5] - 48;
        SEEDS_PER_HOUSE = c[7] - 48;
        START_TIME = Integer.parseInt(t);
        timeSeconds.set(START_TIME);
        if (c[playerIndex] == 'F'){
            TURN = 1;
            myTurn = true;
        }
        else {
            TURN = 2;
            myTurn = false;
        }
        if (c[playerIndex + 2] == 'S'){
            cm = new ClientManager(NUMBER_OF_HOUSES_PER_SIDE, SEEDS_PER_HOUSE, IS_RANDOM_DISTRIBUTION_OF_SEEDS, player1name, playerType);
        }
        else{
            int[] player1 = new int[NUMBER_OF_HOUSES_PER_SIDE+1];
            player1[NUMBER_OF_HOUSES_PER_SIDE] = 0;
            int[] player2 = new int[NUMBER_OF_HOUSES_PER_SIDE+1];
            player2[0] = 0;

            int x = playerIndex + 4;

            for(int i = 0; i < NUMBER_OF_HOUSES_PER_SIDE; i++) {

                while(c[x] == 32) {
                    x++;
                }

                String str_seeds = Character.toString(c[x]);
                int num_seeds = Integer.parseInt(str_seeds);

                player1[i] = num_seeds;
                player2[NUMBER_OF_HOUSES_PER_SIDE-i] = num_seeds;
                x++;
            }
            cm = new ClientManager(player1, player2, player1name, playerType);
        }

        System.out.println("Num pits: " + NUMBER_OF_HOUSES_PER_SIDE);
        System.out.println("Num seeds: " + SEEDS_PER_HOUSE);
        System.out.println("Time: " + START_TIME);
        System.out.println("is human: " + isHuman);
        //System.out.println("Player to go first: " + playerToGoFirst);


        System.out.println("Board (player 1 on bottom player 2 on top): ");
        //player 2
        for(int i = 0; i < NUMBER_OF_HOUSES_PER_SIDE + 1; i++) {
            System.out.print(cm.getPlayer2()[i]);
        }
        System.out.println();
        //player 1
        for(int i = 0; i < NUMBER_OF_HOUSES_PER_SIDE + 1; i++) {
            System.out.print(cm.getPlayer1()[i]);
        }

        readyButton.setVisible(true);
    }

    private class clientThread extends Thread{
        DataInputStream dis = null;
        DataOutputStream dos = null;
        Socket socket = null;

        String addr = "";
        int port = 0;

        public clientThread(String a, int p) {
            addr = a;
            port = p;
        }

        @Override
        public void run() {

            try {
                socket = new Socket(addr, port);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                //receive the welcome message
                String welcome = dis.readUTF();
                dos.writeUTF("Hello");
                dos.flush();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        processNotification(welcome);
                    }
                });
                //read game config
                String gameConfig = dis.readUTF();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        initClientManager(gameConfig);
                    }
                });

                //waiting for send ready notification
                while(!hasDataToProcess()){}
                dos.writeUTF(dosMessage);
                dos.flush();
                setDataToProcess(false, "");

                System.out.println("stay here -1");


                timerTimeline = new Timeline();

                //dealing with the first turn
                if(TURN == 1){

                    //!!!Timer
                    timeSeconds.set(START_TIME);
                    timerTimeline.getKeyFrames().add(
                            new KeyFrame(Duration.seconds(START_TIME + 1),
                                    new KeyValue(timeSeconds, 0)));
                    timerTimeline.playFromStart();

                    //check AI move
                    if(!isHuman){
                        TimeUnit.SECONDS.sleep(4);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run(){
                                clientAIMove();
                            }
                        });
                    }

                    //wait for client move
                    while(!hasDataToProcess()){}
                    dos.writeUTF(dosMessage);

                    //resetTimer();
                    timerTimeline.stop();

                    dos.flush();
                    setDataToProcess(false, "");
                    //wait for reply
                    System.out.println("stay here 0");
                    String res = dis.readUTF();

                    //resetTimer();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            processNotification(res);
                        }
                    });
                    //wait for server send its movement
                    System.out.println("stay here 1");

                    String serverMove = dis.readUTF();
                    System.out.println("server move: " + serverMove);
                    //update board after server move
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            moveServer(serverMove);
                        }
                    });

                    //reply server that client receives the move
                    dos.writeUTF("OK");
                    dos.flush();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            resetTimer();
                        }
                    });
                }
                else {
                    //wait for server send its movement
                    String serverMove = dis.readUTF();
                    System.out.println("server move: " + serverMove);

                    //update board after server move
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            moveServer(serverMove);
                        }
                    });

                    //reply server that client receives the move
                    dos.writeUTF("OK");
                    dos.flush();
                    System.out.println("stay here 4");

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            resetTimer();
                        }
                    });
                }

                while(true){
                    //check AI move
                    if(!isHuman){
                        TimeUnit.SECONDS.sleep(5);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run(){
                                clientAIMove();
                            }
                        });
                    }

                    //wait for client move
                    while(!hasDataToProcess()){}
                    dos.writeUTF(dosMessage);

                    //resetTimer();
                    timerTimeline.stop();

                    dos.flush();
                    setDataToProcess(false, "");
                    //wait for reply from server
                    System.out.println("stay here 2");
                    String res = dis.readUTF();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            processNotification(res);
                        }
                    });
                    System.out.println("stay here 3");
                    //wait for server send its movement
                    String serverMove = dis.readUTF();
                    //update board after server move
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            moveServer(serverMove);
                        }
                    });

                    //reply server that client receives the move
                    dos.writeUTF("OK");
                    dos.flush();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            resetTimer();
                        }
                    });

                }

            } catch (UnknownHostException ex){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        processNotification("cannot connect to server");
                    }
                });
            } catch (IOException ex) {
                Logger.getLogger(FakeServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
//       try{
//        GameManager gm = new GameManager();
//        gm.move(5);
//        gm.printPlayer();
//        gm.move(2);
//        gm.printPlayer();
//    }catch(Exception e){
//    System.out.println(e.getMessage());
//}

    }
}
