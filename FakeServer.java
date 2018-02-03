/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kalahfx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import static kalahfx.ClientGUI.isNumeric;

/**
 *
 * @author aiyamickey
 */
public class FakeServer extends Application {
    
    Stage globalStage;
    Text rule1;
    ServerSocket serverSocket;
    
    int threadCount = 0;
    
    boolean hasMessage = false;
    String dosMessage = "";
    
    public synchronized boolean hasDataToProcess(){
        return hasMessage;
  }

    public synchronized void setDataToProcess(boolean hasData, String data){
        dosMessage = data;
        hasMessage = hasData;  
  }
    
    public Parent instructionsScene() {

        Pane root = new Pane();
        root.setPrefSize(1024, 600);

        rule1 = new Text("waiting for client");
        rule1.setVisible(false);
        rule1.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 18));
        

        Button startPlayingButton = new Button("connect!");
        startPlayingButton.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        startPlayingButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                rule1.setVisible(true);
                //startPlayingButton.setDisable(true);
                Thread socketServerThread = new Thread(new SocketServerThread());
                socketServerThread.setDaemon(true);
                socketServerThread.start();
            }
        });

        Label commandLabel = new Label("command:");
        TextField comm = new TextField ();
        
        Button connectButton = new Button("send");
        connectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if((comm.getText() != null && !comm.getText().isEmpty())){
                    setDataToProcess(true, comm.getText());
                }
            }
        });
        
        VBox vb = new VBox(20);
        vb.setAlignment(Pos.CENTER);
        vb.setPrefWidth(1024);
        vb.getChildren().addAll(rule1, startPlayingButton, commandLabel, comm, connectButton);
        vb.setLayoutY(175);

        root.getChildren().add(vb);

        root.setStyle("-fx-background-color: #87CEFA;");

        return root;

    }
    
    @Override
    public void start(Stage stage) {
        globalStage = stage;
        
        Text text = new Text("waiting for client");
        text.setLayoutY(100);
        text.setVisible(false);
        
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                text.setVisible(true);
                changeScene(new Scene(instructionsScene()));
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().addAll(btn, text);
        
        Scene scene = new Scene(root, 300, 250);
        
        stage.setTitle("server");
        stage.setScene(scene);
        stage.show();
    }

    public void changeScene(Scene scene) {
        
        globalStage.setScene(scene);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public void processNotification(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("client send: " + content);
        alert.show();
    }
    
    private class SocketServerThread extends Thread{
        static final int socketServerPort = 2000;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        
        @Override
        public void run() {
            threadCount++;
            
            try {
                serverSocket = new ServerSocket(socketServerPort);
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        rule1.setText("still waiting" + Integer.toString(threadCount));
                    }
                });
                
                Socket socket = serverSocket.accept();
                
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        rule1.setText("connected");
                    }
                });
                
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF("WELCOME");
                dos.flush();
                
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
                    }
                });
                
                boolean clientFirst = true;
                if(clientFirst) {
                    System.out.println("stay here 1");
                    //receive the client movement
                    String s = dis.readUTF();
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            processNotification(s);
                        }
                    });
                    //send notification
                    while(!hasDataToProcess()){}
                
                    dos.writeUTF(dosMessage);
                    dos.flush();
                    setDataToProcess(false, "");
                }
                
                while(true) {
                    System.out.println("stay here 2");

                    //send server move
                    while(!hasDataToProcess()){}
                
                    dos.writeUTF(dosMessage);
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
                    
                    //wait for client move
                    System.out.println("stay here 3");
                    String s = dis.readUTF();
                    
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            processNotification(s);
                        }
                    });
                    
                    //send notification
                    while(!hasDataToProcess()){}
                
                    dos.writeUTF(dosMessage);
                    dos.flush();
                    setDataToProcess(false, "");
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
            }
        }
    }
}
