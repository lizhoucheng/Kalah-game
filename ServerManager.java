/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multithreading.socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aiyamickey
 */
public class ServerManager {
    public ArrayList<int[]> gameState;
    private int currSeedNum;
    // private int turn;
    private boolean additionalMove;
    private boolean gameOver = false;
    public String winner;

    private int numHousesPerSide;
    private int numSeedsPerHouse;

    private String player1name;
    private String player2name;

    //    private PLAYER_TYPE player1Type;
//    private PLAYER_TYPE player2Type;
    private PLAYER_TYPE playerType;

    private final int CLIENT = 2;
    private final int SERVER = 1;
    private final int TURN = SERVER;

    private ArrayList<Integer> AIMovement = new ArrayList<>();

    private GameStateNode aiRoot;

    public ServerManager(int numHousesPerSide, int numSeedsPerHouse, boolean randDistribution, String name1, PLAYER_TYPE playerType) {
        gameState = new ArrayList<int[]>();

        this.numHousesPerSide = numHousesPerSide;
        this.numSeedsPerHouse = numSeedsPerHouse;

        player1name = name1;

        this.playerType = playerType;

        gameState.add(new int[this.numHousesPerSide + 1]);
        gameState.add(new int[this.numHousesPerSide + 1]);

        if(randDistribution) {

            Random r = new Random();

            //get the random seeds
            int[] randNums = new int[numHousesPerSide];
            for (int i = 0; i < numHousesPerSide; i++) {
                randNums[i] = 0;
            }
            /*
            for (int i = 0; i < numSeedsPerHouse; i++) {
                int x = r.nextInt(numHousesPerSide);
                randNums[x]++;
            }
            */

            int totalNumSeeds = numHousesPerSide * numSeedsPerHouse;
            while(totalNumSeeds > 0) {
                int x = r.nextInt(numHousesPerSide);
                randNums[x]++;
                totalNumSeeds--;
            }


            //create the randomized board
            for(int i = 0; i < numHousesPerSide; i++) {
                gameState.get(0)[i] = randNums[i];
                gameState.get(1)[numHousesPerSide-i] = randNums[i];
            }
        }
        else {

            gameState.get(0)[0] = this.numSeedsPerHouse;
            gameState.get(1)[0] = 0;

            for (int i = 1; i < this.numHousesPerSide; i++) {
                gameState.get(0)[i] = this.numSeedsPerHouse;
                gameState.get(1)[i] = this.numSeedsPerHouse;
            }

            gameState.get(0)[this.numHousesPerSide] = 0;
            gameState.get(1)[this.numHousesPerSide] = this.numSeedsPerHouse;

        }


        //gameState.get(0) = new int[this.numHousesPerSide + 1]; //the last element is store, player 1 is at the bottom of the scene
        //player2 = new int[this.numHousesPerSide + 1]; //the first element is store, pplayer 2 is at the top of the scene

        currSeedNum = 0;
        //this.turn = turn;
        additionalMove = false;
        gameOver = false;

        try {
            //AIHandler ai = new AIHandler(gameState);
        } catch (Exception e) {
            System.out.println("Failed to initialize AI");
        }
    }

    /*the player 2 needs to be reversed so that its mancala is at first before parsing to this function */
    public ServerManager(int[] player1, int[] player2, String name1, PLAYER_TYPE playerType) {
        gameState = new ArrayList<int[]>();

//        for (int i = 0; i < player2.length / 2; i++) {
//            int temp = player2[i];
//            player2[i] = player2[player2.length - 1 - i];
//            player2[player2.length - 1 - i] = temp;
//        }
        gameState.add(player1);
        gameState.add(player2);

        player1name = name1;

        this.playerType = playerType;

        currSeedNum = 0;
        additionalMove = false;
        gameOver = false;
    }

    //public int getTurn(){return turn;}

    public int getCurrentSeedNum(){return currSeedNum;}

    public void setCurrentSeedNum(int c){currSeedNum = c;}

    public String getPlayer1Name() {return this.player1name;}

    public String getPlayer2Name() {return this.player2name;}

    public void doPieRule() {

        int[] tempPlayer1;
        int[] tempPlayer2;

        tempPlayer1 = gameState.get(0);
        tempPlayer2 = gameState.get(1);

        //reverse the two temp board positions
        for(int i = 0; i < tempPlayer1.length/2; i++) {

            int temp = tempPlayer1[i];
            tempPlayer1[i] = tempPlayer1[tempPlayer1.length - 1 - i];
            tempPlayer1[tempPlayer1.length - 1 - i] = temp;

        }

        for(int i = 0; i < tempPlayer2.length/2; i++) {

            int temp = tempPlayer2[i];
            tempPlayer2[i] = tempPlayer2[tempPlayer2.length - 1 - i];
            tempPlayer2[tempPlayer2.length - 1 - i] = temp;

        }

        gameState.set(0, tempPlayer2);
        gameState.set(1, tempPlayer1);

    }

    public int getSeedNumber(int turn, int house) throws Exception{
        if(house < 0 || house >= gameState.get(0).length)
            throw new Exception("out of index in getSeedNumber()");
        if(turn != SERVER && turn != CLIENT)
            throw new Exception("no such player");

        return turn == SERVER ? gameState.get(0)[house] : gameState.get(1)[house];
    }

    public boolean hasAdditionalMove(){
        if(additionalMove){
            additionalMove = false;
            return true;
        }
        return false;
    }

    public void decreaseSeed(int house, int turn) throws Exception{
        int[] player = turn == SERVER ? gameState.get(0) : gameState.get(1);

        currSeedNum = player[house];
        player[house] = 0;
    }

    public void increaseSeed(int house, int turn) throws Exception{ //house is the one whose seeds are taken

        int[] player = turn == 1 ? gameState.get(0) : gameState.get(1);

        //distribute the seed counter clockwise
        house = turn == 1 ? house + 1 : house - 1;
        while(currSeedNum > 0){
            if(player == gameState.get(0)){
                if(house >= player.length){
                    player = gameState.get(1);  //switch player
                    house = gameState.get(1).length - 1;
                }
                else{
                    player[house]++;
                    currSeedNum--;
                    house++;
                }
            }
            else if(player == gameState.get(1)){
                if(house < 0){
                    player = gameState.get(0);
                    house = 0;
                }
                else{
                    player[house]++;
                    currSeedNum--;
                    house--;
                }
            }
            else{
                throw new Exception("something went wrong in increseSeed()");
            }
        }

        if(currSeedNum == 0){
            //check if last seed lands on the same player in the beginning of the movement
            if(turn == 1 && player == gameState.get(0)){
                //if the last seed lands on player 1, then substract 1 from house to get the location of last seed
                int lastLocation = house - 1;
                //check if the last seed lands on the pit of either player; if yes, then do not switch turn
                if(lastLocation == gameState.get(0).length - 1 && (!checkGameOver())){
                    additionalMove = true;
                }
                else if(player[lastLocation] == 1){
                    int[] opposite = gameState.get(1);
                    int oppositeLocation = lastLocation + 1;
                    if(opposite[oppositeLocation] != 0){
                        //moveToMancala
                        player[gameState.get(0).length-1] += opposite[oppositeLocation] + 1;
                        opposite[oppositeLocation] = 0;
                        player[lastLocation] = 0;
                    }
                }
            }
            else if(turn == 2 && player == gameState.get(1)){
                //if the last seed lands on player 2, then add 1 to house to get the location of last seed
                int lastLocation = house + 1;
                //check if the last seed lands on the pit of either player; if yes, then do not switch turn
                if(lastLocation == 0 && (!checkGameOver())){
                    additionalMove = true;
                }
                else if(player[lastLocation] == 1){
                    int[] opposite = gameState.get(0);
                    int oppositeLocation = lastLocation - 1;
                    if(opposite[oppositeLocation] != 0){
                        //moveToMancala
                        player[0] += opposite[oppositeLocation] + 1;
                        opposite[oppositeLocation] = 0;
                        player[lastLocation] = 0;
                    }

                }
            }
        }
    }

    public void AIMove(int turn) throws Exception {
//        if((turn == SERVER && playerType == PLAYER_TYPE.HUMAN)) {
//            throw new Exception ("Human client using AIMove ");
//        }
        if (turn == CLIENT) {
            throw new Exception ("client using AIMove ");
        }
        try {
            AI ai = new AI(getPlayer1(), getPlayer2(), turn, playerType);
            AIMovement = ai.getMovement();
            gameState = ai.outputBoardState();

        } catch (Exception e) {
            System.out.println("error in AI move in move() for turn 1");
        }
    }

    public ArrayList<Integer> getAIMovement() {
        return AIMovement;
    }

    public void move(int house, int turn) throws Exception{
//        if(turn == CLIENT && playerType != PLAYER_TYPE.HUMAN){
//            throw new Exception("AI client try to call move function for human");
//        }
        if(house < 0 || house >= gameState.get(0).length)
            throw new ArrayIndexOutOfBoundsException("house index error in move() " + house);
        if((turn == 1 && house == gameState.get(0).length - 1) || (turn == 2 && house == 0))
            throw new Exception("cannot move the seeds in the mancala");

        decreaseSeed(house, turn);

        increaseSeed(house, turn);

    }

    public void printPlayer(){
        System.out.println("player 2:");
        for(int i : gameState.get(1))
            System.out.print(i + " ");
        System.out.println();

        System.out.println("player 1:");
        for(int i : gameState.get(0))
            System.out.print(i + " ");
        System.out.println();
    }

    public int[] getPlayer1() {
        return gameState.get(0);
    }

    public int[] getPlayer2() {
        return gameState.get(1);
    }

    /*
        ARTIFICIAL INTELLIGENCE
     */

    private class GameStateNode {
        public int[] human;
        public int[] ai;
        public GameStateNode[] nodes;
    }

    public void buildMiniMaxTree(GameStateNode gameState, boolean max) {
        for (int i = 0; i < gameState.human.length; i++) {

        }
    }

    int evaluationFunc() {
        return gameState.get(0)[numHousesPerSide - 1] - gameState.get(1)[0];
    }

    public boolean isMoveLegal(int turn, int index) throws Exception{
        if(turn == SERVER) {
            System.out.println("come here");
            boolean res = ((index >= 0) && (index < gameState.get(0).length - 1));
            return res && gameState.get(0)[index] != 0;
        }
        else if (turn == CLIENT){
            boolean res = ((index > 0) && (index < gameState.get(1).length));
            return res && gameState.get(1)[index] != 0;
        }
        else{
            throw new Exception("error about turn in isMoveLegal()");
        }
    }

    public boolean isOnTime(){return true;}

    public boolean checkGameOver(){
        boolean gameOver = false;

        int score1 = 0;
        int score2 = 0;

        boolean isOver1 = true;
        for(int i = 0; i < gameState.get(0).length-1; i++){ //the last element is the mancala
            if(gameState.get(0)[i] != 0){
                isOver1 =  false;
                score1 += gameState.get(0)[i];
                break;
            }
        }
        boolean isOver2 = true;
        for(int i = 1; i < gameState.get(1).length; i++){ //the first element is the mancala
            if(gameState.get(1)[i] != 0){
                isOver2 =  false;
                score2 += gameState.get(1)[i];
                break;
            }
        }
        if(isOver1 || isOver2){
            if(gameState.get(0)[gameState.get(0).length-1] + score1 == gameState.get(1)[0] + score2){
                System.out.println("they have the same score");
                winner = "tie";
                gameOver = true;
            }
            else{
                winner = gameState.get(0)[gameState.get(0).length-1]+score1 > gameState.get(1)[0]+score2 ? "SERVER" : "CLIENT";
                gameOver = true;
            }
        }
        return gameOver;
    }

    public static void main(String[] args) {
        try {
            ServerManager cm1 = new ServerManager(new int[]{0,1,2,3}, new int[]{2,2,2,0}, "", PLAYER_TYPE.HUMAN);
            cm1.printPlayer();
            System.out.println(cm1.isMoveLegal(2, 3));
//              cm1.move(1, 1);
//              cm1.printPlayer();
//              cm1.AIMove(1);
//              cm1.printPlayer();
//              cm1.AIMove(1);
//              cm1.AIMove(1);
        } catch (Exception ex) {
            Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
