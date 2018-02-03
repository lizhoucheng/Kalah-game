//import com.sun.istack.internal.NotNull;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
//import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
import org.w3c.dom.css.Rect;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class KalahFX extends Application {

    final int NUMBER_OF_HOUSES_PER_SIDE = 6;

    Stage globalStage;

    ArrayList<Circle> gamePieces;

    Circle selectedGamePiece = null;
    
    GameManager gm;
    
    StackPane[] topPane;
    
    StackPane[] bottomPane;
    
    public Parent gameScene() {
        Pane root = new Pane();
        root.setPrefSize(1024, 600);
        gm = new GameManager();
        topPane = new StackPane[NUMBER_OF_HOUSES_PER_SIDE + 1]; //last element is the left pit
        bottomPane = new StackPane[NUMBER_OF_HOUSES_PER_SIDE + 1]; //last element is the right pit
        /*
            Create left pit
         */
//        StackPane leftPit = new StackPane();
//        leftPit.setLayoutX(145);
//        leftPit.setLayoutY(150);

        topPane[0] = new StackPane();
        topPane[0].setLayoutX(145);
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
        bottomPane[bottomPane.length-1].setLayoutX(780);
        bottomPane[bottomPane.length-1].setLayoutY(150);

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
        for (int i = 0; i < NUMBER_OF_HOUSES_PER_SIDE; i++) {
            int nextX = 235 + 90 * i;
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
            text.setText(Integer.toString(gm.getSeedNumber(2, i+1)));
            text.setFill(Color.WHITE);

            //topPane.getChildren().addAll(rect, text);
            topPane[i+1].getChildren().addAll(rect, text);

            topPane[i+1].setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    handleBoardHouseSelected(e);
                }
            });

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
            bottomText.setText(Integer.toString(gm.getSeedNumber(1, i)));
            bottomText.setFill(Color.WHITE);

//            bottomPane.getChildren().addAll(bottomRect, bottomText);4
            bottomPane[i].getChildren().addAll(bottomRect, bottomText);

            bottomPane[i].setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    handleBoardHouseSelected(e);
                }
            });
            /*
                Add panes to root
             */
            root.getChildren().addAll(topPane[i+1], bottomPane[i]);
        }
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("3**");
        }

        return root;
    }

    public Parent menuScene() {
        Pane root = new Pane();
        root.setPrefSize(1024, 600);

        Text welcome = new Text("Welcome");
        welcome.setFont(new Font("Comic Sans MS", 23.0));
        welcome.setX(220.0);
        welcome.setY(71.0);

        Text player1 = new Text("Player 1");
        player1.setFont(new Font("Comic Sans MS", 23.0));
        player1.setX(84.0);
        player1.setY(162.0);

        Text player2 = new Text("Player 2");
        player2.setFont(new Font("Comic Sans MS", 23.0));
        player2.setX(257.0);
        player2.setY(162.0);

        Text rules = new Text("Rules");
        rules.setFont(new Font("Comic Sans MS", 23.0));
        rules.setX(446.0);
        rules.setY(162.0);

        Button startGame = new Button("Start Game");
        startGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                changeScene(new Scene(gameScene()));
            }
        });

        root.getChildren().addAll(welcome, player1, player2, rules, startGame);

        return root;
    }

    @Override
    public void start(Stage stage) throws IOException
    {
        globalStage = stage;


        //Parent root = FXMLLoader.load(getClass().getResource("SplashScreen.fxml"));
        Parent root = new Group();

        Scene scene = new Scene(root, 1024, 600);

        globalStage.setScene(scene);
        globalStage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2000), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                changeScene(new Scene(menuScene()));
            }
        }));

        timeline.play();
    }

    public void handleStartGame(ActionEvent e) {
        System.exit(0);
    }

    public void handleGamePieceClick(ActionEvent e) {
        Circle selected = (Circle)e.getSource();

        selectedGamePiece = selected;
    }

   /* public void handleBoardHouseSelected(MouseEvent event) {
        StackPane selected = (StackPane) event.getSource();

        Text textBox = (Text)selected.getChildren().get(selected.getChildren().size() - 1);

        int value = Integer.parseInt(textBox.getText());
        if (value + 1 > 5) {
            System.exit(0);
        }

        textBox.setText(String.valueOf(value + 1));

    }
*/
    public void handleBoardHouseSelected(MouseEvent event){
        try{
        StackPane[] player = gm.getTurn() == 1 ? bottomPane : topPane;
        StackPane selected = (StackPane) event.getSource();
        int index = Arrays.asList(player).indexOf(selected);
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("error");
        
        if(index == -1){
            alert.setContentText("you cannot select the opponent's houses");
            alert.show();
        }
        else{
            int seed = gm.getSeedNumber(gm.getTurn(), index);
            if(seed <= 0){
                alert.setContentText("Don't select an empty house");
                alert.show();
            }
            else{
                gm.setCurrentSeedNum(seed);
                gm.move(index);
                
                updateBoard();
            }
        }
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("1**");
        }
    }
    
    public void updateBoard(){
        gm.printPlayer();
        try{
        //update bottom pane
        for(int i = 0; i < bottomPane.length; i++){
            Text t = (Text)bottomPane[i].getChildren().get(bottomPane[i].getChildren().size() - 1);
            t.setText(Integer.toString((gm.getSeedNumber(1, i))));
        }
        
        //update top pane
        for(int i = 0; i < topPane.length; i++){
            Text t = (Text)topPane[i].getChildren().get(topPane[i].getChildren().size() - 1);
            t.setText(Integer.toString((gm.getSeedNumber(2, i))));
        }
        
        if(gm.hasAdditionalMove()){
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("notification");
            alert.setContentText("you have another chance to move");
            alert.show();
        }
        
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("2**");
        }
    }
    
    public void changeScene(Scene scene) {
        globalStage.setScene(scene);
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