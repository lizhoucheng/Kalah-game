/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multithreading.socket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AI {
    //GameManager gm;
    ArrayList<Integer> initialBoard = new ArrayList<>();
    
    ArrayList<Integer> resultPits = new ArrayList<>();
    
    ArrayList<int[]> movements = new ArrayList<>(); //record which house AI moves
    
    MovementNode movementRoot = new MovementNode(-1);
     
    boolean isBoardChange = false;
    
    int turn, mancalaIndex1, mancalaIndex2, maxPlayer, minPlayer, lastSeedIndex;
    int depth = 4;
    
    public AI(){}
    
    public AI(int[] player1, int[] player2, int turn, PLAYER_TYPE playerType) throws Exception {
        //put player 1 into the array
        for(int i : player1){
            initialBoard.add(i);
        }
        mancalaIndex1 = player1.length - 1;
        //put player 2 into the array
        for(int i = player2.length - 1; i >=0; i--){
            initialBoard.add(player2[i]);
        }
        mancalaIndex2 = initialBoard.size() - 1;
        
        resultPits = new ArrayList<>(initialBoard);


        this.turn = turn;
        maxValue(initialBoard, 0, turn, Integer.MIN_VALUE, Integer.MAX_VALUE, playerType.ordinal() + 1);
        if(!isBoardChange){
            try {
                ArrayList<ArrayList<Integer>> succ = Successor(initialBoard, turn, false, null);
                resultPits = succ.size() > 0 ? succ.get(0) : resultPits;
            } catch (Exception ex) {
                Logger.getLogger(AI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

  /*  public AI(ClientManager cm, int numOfHouse, int turn) throws Exception{
        //put player 1 into the array
        for(int i : cm.getPlayer1()){
            initialBoard.add(i);
        }
        mancalaIndex1 = cm.getPlayer1().length - 1;
        //put player 2 into the array
        int[] player2 = cm.getPlayer2();
        for(int i = player2.length - 1; i >=0; i--){
            initialBoard.add(player2[i]);
        }
        mancalaIndex2 = initialBoard.size() - 1;

        PLAYER_TYPE playerType = cm.getPlayerType();

        resultPits = initialBoard;

        this.turn = turn;

        maxValue(initialBoard, 0, cm.getTurn(), Integer.MIN_VALUE, Integer.MAX_VALUE, playerType.ordinal() * 2);
    }

    public AI(ServerManager sm, int numOfHouse, int turn) throws Exception{
        //put player 1 into the array
        for(int i : sm.getPlayer1()){
            initialBoard.add(i);
        }
        mancalaIndex1 = sm.getPlayer1().length - 1;
        //put player 2 into the array
        int[] player2 = sm.getPlayer2();
        for(int i = player2.length - 1; i >=0; i--){
            initialBoard.add(player2[i]);
        }
        mancalaIndex2 = initialBoard.size() - 1;

        PLAYER_TYPE playerType = sm.getPlayerType();

        resultPits = initialBoard;

        this.turn = turn;

        maxValue(initialBoard, 0, sm.getTurn(), Integer.MIN_VALUE, Integer.MAX_VALUE, playerType.ordinal() * 2);

    }  */
    
    //return the result board state to game manager
    public ArrayList<int[]> outputBoardState(){
        
        int[] player1 = new int[resultPits.size()/2];
        for(int i = 0; i < player1.length; i++){
            player1[i] = resultPits.get(i);
        }
        int[] player2 = new int[resultPits.size()/2];
        for(int i = 0; i < player2.length; i++){
            player2[i] = resultPits.get(mancalaIndex2 - i);
        }
        
        ArrayList<int[]> outputBoard = new ArrayList<>();
        outputBoard.add(player1);
        outputBoard.add(player2);
        
        return outputBoard;
    }
    
    public ArrayList<Integer> getResultPits(){
        System.out.println("in the get function");
        for(int i : resultPits){
                System.out.print(i + " ");
            }
            System.out.println();
        return resultPits;
    }
    
    public void setResultPits(ArrayList<Integer> board){
        resultPits.clear();
        resultPits.addAll(board);
    }
    
    public boolean terminalTest(ArrayList<Integer> board, int currLevel){
        return (isGameOver(board) || currLevel >= depth);
    }
    
    public boolean isGameOver(ArrayList<Integer> board){
        int player1Seed = 0;
        int player2Seed = 0;
        
        for(int i = 0; i < mancalaIndex1; i++){
            player1Seed += board.get(i);
        }
        for(int i = mancalaIndex1+1; i < mancalaIndex2; i++){
            player2Seed += board.get(i);
        }
        
        if(player1Seed == 0 || player2Seed == 0){
//            if(player1Seed == 0)
//                board.set(mancalaIndex2, board.get(mancalaIndex2)+player2Seed);
//            else
//                board.set(mancalaIndex1, board.get(mancalaIndex1)+player1Seed);
//            
            return true;
        }
        return false;
    }
    
    public ArrayList<ArrayList<Integer>> Successor(ArrayList<Integer> board, int turn, boolean recordMove, MovementNode move) throws Exception{
        ArrayList<ArrayList<Integer>> successor = new ArrayList<>();
        
        int start = 0;
        int end = mancalaIndex2;
        if(turn == 1){
            end = mancalaIndex1;
        }
        else{
            start = mancalaIndex1+1;
        }
        
        for(int i = start; i < end; i++){
            if(board.get(i) != 0){
                MovementNode m = new MovementNode(i);
                if(recordMove){
                    move.add(m);
                }
                
            ArrayList<Integer> nextBoard = move(board, i, turn);
            
            if(!isGameOver(nextBoard) && addtionalTurn(lastSeedIndex, turn)){
                successor.addAll(Successor(nextBoard, turn, recordMove, m));
            }
            else if(checkSnatch(nextBoard, lastSeedIndex, turn)){
                nextBoard = snatch(nextBoard, lastSeedIndex);
                successor.add(nextBoard);
            }
            else{
                successor.add(move(board, i, turn));
            }
        }
        }
        return successor;
    }
    
    public ArrayList<Integer> move(ArrayList<Integer> board, int index, int turn) throws Exception{
        if(index < 0 || index >= board.size()){
            throw new Exception("out of boundary in move()");
        }
        if(board.get(index) == 0){
            throw new Exception("invalid move in move()");
        }
        
        ArrayList<Integer> temp = new ArrayList<>(board.size());
        temp.addAll(board);
        
        int seeds = temp.get(index);
        temp.set(index, 0);
        
        while(seeds > 0){
            seeds--;
            index++;
            if(index >= temp.size()){
                index = 0;
            }
            temp.set(index, temp.get(index)+1);
        }
        lastSeedIndex = index;
        
        return temp;
    }
    
    public boolean addtionalTurn(int index, int turn){
        return (index == mancalaIndex1 && turn == 1)||(index == mancalaIndex2 && turn == 2);
    }
    
    public boolean checkSnatch(ArrayList<Integer> board, int lastIndex, int turn){
        if(turn == 1 && lastIndex >=0 && lastIndex < mancalaIndex1){
            int opponentIndex = mancalaIndex1 - lastIndex;
            opponentIndex = mancalaIndex1 + opponentIndex;
            if(board.get(lastIndex) == 1 && board.get(opponentIndex) != 0)
                return true;
        }
        if(turn == 2 && lastIndex > mancalaIndex1 && lastIndex < mancalaIndex2){
            int opponentIndex = lastIndex - mancalaIndex1;
            opponentIndex = mancalaIndex1 - opponentIndex;
            
            if(board.get(lastIndex) == 1 && board.get(opponentIndex) != 0)
                return true;
        }
        return false;
    }
    
    public ArrayList<Integer> snatch(ArrayList<Integer> board, int lastIndex){
//        System.out.println("before snatch board:" + board + "\n last index: " + lastIndex);
        if(lastIndex < mancalaIndex1){ //player 1
            board.set(lastIndex, 0);
            
            int opponentIndex = mancalaIndex1 - lastIndex;
            opponentIndex = mancalaIndex1 + opponentIndex;
            
            int seed = board.get(opponentIndex)+1;
            
            board.set(mancalaIndex1, board.get(mancalaIndex1)+seed);
            
            board.set(opponentIndex, 0);
        }
        else if(lastIndex > mancalaIndex1 && lastIndex < mancalaIndex2){
            board.set(lastIndex, 0);
            
            int opponentIndex = lastIndex - mancalaIndex1;
            opponentIndex = mancalaIndex1 - opponentIndex;
            
            int seed = board.get(opponentIndex)+1;
            
            board.set(mancalaIndex2, board.get(mancalaIndex2)+seed);
            
            board.set(opponentIndex, 0);
        }
//        System.out.println("snatch board:" + board);
        return board;
    }
        
    public int maxValue(ArrayList<Integer> board, int currLevel, int turn, int alpha, int beta, int maxDepth) throws Exception{
        if(terminalTest(board, currLevel) || maxDepth < 0)
            return Utility(board, turn);
        
        int v = Integer.MIN_VALUE;
        int max = Integer.MIN_VALUE;
        
        boolean record = false;
        MovementNode m = null;
        if(currLevel == 0) {
            record = true;
            m = new MovementNode(-1);
        }
        ArrayList<ArrayList<Integer>> successor = Successor(board, turn, record, movementRoot);
        
        
        for(int i = 0; i < successor.size(); i++){
            if(currLevel == 0){
            System.out.println("successor: " + successor.get(i));
        }
            v = minValue(successor.get(i), currLevel+1, turn, alpha, beta, maxDepth - 1);
            //alpha = Math.max(alpha, v);
            
            if(v > max){
                
//                System.out.println("ideal result pit: ");
//                for(int k : successor.get(i)){
//                    System.out.print(k + " ");
//                }
//                System.out.println();

                max = v;
                
//                resultPits.clear();
//                resultPits.addAll(successor.get(i));
                if(currLevel == 0){
//                    System.out.println("v: " + v);
                    setResultPits(successor.get(i));
                    isBoardChange = true;
               // System.out.println("actual result pit in max: ");
//                for(int k : resultPits){
//                    System.out.print(k + " ");
//                }
//                System.out.println();
                }
                //return v;
            }
            alpha = Math.max(alpha, max);
            if(max > beta){
                return max;
            }
        }
//        System.out.println("max: " + max);
        return max;
    }
    
    public int minValue(ArrayList<Integer> board, int currLevel, int turn, int alpha, int beta, int maxDepth) throws Exception{
        if(terminalTest(board, currLevel) || maxDepth < 0)
            return Utility(board, turn);
        
        int v = Integer.MAX_VALUE;
        int min = Integer.MAX_VALUE;
        ArrayList<ArrayList<Integer>> successor = Successor(board, turn, false, null);
        
        for(int i = 0; i < successor.size(); i++){
            v = Math.min(v,maxValue(successor.get(i), currLevel+1, turn, alpha, beta, maxDepth - 1));
            beta = Math.min(beta, v);
            if(v < alpha){
//                min = v;
//                setResultPits(successor.get(i));
                
//                System.out.println("actual result pit in min: ");
//                for(int k : resultPits){
//                    System.out.print(k + " ");
//                }
//                System.out.println();
            
//                resultPits.clear();
//                resultPits.addAll(successor.get(i));
                  return v;
            }
//            if(min < alpha){
//                return min;
//            }
        }
//        System.out.println("min: " + v);
        return v;
    }
    
    int Utility(ArrayList<Integer> board, int turn){
        if(turn == 1)
            return board.get(mancalaIndex1) - board.get(mancalaIndex2);
        else
            return board.get(mancalaIndex2) - board.get(mancalaIndex1);
    }
    
    private static class MovementNode{
        int house;
        ArrayList<MovementNode> successors;
        
        public MovementNode(int h) {
            house = h;
            successors = new ArrayList<>();
        }
        
        public void add(MovementNode m) {
            successors.add(m);
        }
        
        public int getHouse() {
            return house;
        }
        
        public boolean hasNext(){
            return successors.size() > 0;
        }
        
        public ArrayList<MovementNode> getChildMovements(){
            return successors;
        }
        
        public void print() {
            System.out.println(house);
            for(MovementNode m : successors){
                System.out.print(m.getHouse() + " ");
            }
            System.out.println();
            
            for(MovementNode m : successors){
                m.print();
            }
        }
    }
    
    /*return which house to move in reverse order */
    public boolean getMovementFromResultBoard(ArrayList<Integer> movements, MovementNode currMove, ArrayList<Integer> board, int turn) throws Exception {
        if(compareWithResult(board)){
            return true;
        }
        if(!currMove.hasNext()){
            return false;
        }
        for(MovementNode m : currMove.getChildMovements()){
            ArrayList<Integer> currBoard = move(board, m.getHouse(), turn);
            
            if(checkSnatch(currBoard, lastSeedIndex, turn)){
//                
                currBoard = snatch(currBoard, lastSeedIndex);
            }
            
            boolean res = getMovementFromResultBoard(movements, m, currBoard, turn);
            
            if(res){
                movements.add(m.getHouse());
                return true;
            }
        }
        return false;
    }
    
    public ArrayList<Integer> getMovement() throws Exception {
        ArrayList<Integer> movements = new ArrayList<Integer>();
        
        getMovementFromResultBoard(movements, movementRoot, initialBoard, turn);
        
        Collections.reverse(movements);
        
        if(movements.size() < 1){
            movements.add(-1);
        }
        System.out.println("inside get movement()" + movements);
        
        return movements;
    }

    
    public boolean compareWithResult(ArrayList<Integer> board) throws Exception{
        if(board.size() != resultPits.size()){
            throw new Exception("error in size in compareInitialAndResult()");
        }
        for(int i = 0; i < board.size(); i++){
            if(!board.get(i).equals(resultPits.get(i))){
                return false;
            }
        }
        return true;
    }
    
    public static void main(String[] args) throws Exception{
        /*MovementNode root = new MovementNode(-1);
//        ArrayList<Integer> arr = new ArrayList<>(Arrays.asList(1,2,1,0,1,1,1,0));
//        mancalaIndex1 = 4;
//        mancalaIndex2 = 9;
        AI ai = new AI(new int[]{0,0,0,1,3}, new int[]{3,0,1,0,0}, 1);
        ArrayList<ArrayList<Integer>> successor = ai.Successor(ai.initialBoard, 1, false, null);
        for(ArrayList<Integer> s : successor){
            System.out.println("successors: " + s);
        }
//        AI ai2 = new AI();
//        ai2.Successor(ai.initialBoard, 1, true, root);
//        root.print();
        ArrayList<int[]> result = ai.outputBoardState();
            System.out.println("player 2 :");
        for(int i : result.get(1)){
            System.out.print(i + " ");
        }
        System.out.println("\n player 1:");
        for(int i : result.get(0)){
            System.out.print(i + " ");
        }
//        System.out.println("intialboard:");
//        for(int in : ai.initialBoard){
//            System.out.print(in + " ");
//        }
//        System.out.println();
        ArrayList<Integer> moves = new ArrayList<>();
        //ai.getMovementFromResultBoard(moves, ai.movementRoot, ai.initialBoard, 1);
//        System.out.println("moves:");
//        for(int m : moves){
//            System.out.print(m);
//        }
        moves = ai.getMovement();
        System.out.println("moves:" + moves);
//        ai.Successor(arr, 1, true, root);
//        root.print();
        
//        ArrayList<Integer> arr = new ArrayList<>(Arrays.asList(4,4,4,4,4,4,0,0,5,5,5,5,4,0));
//        mancalaIndex1 = 6;
//        mancalaIndex2 = 13;
        //System.out.println(new AI().isGameOver(arr));
//        GameManager game = new GameManager(new int[]{5},new int[]{5},1);
//        System.out.println("player 2 :");
//        for(int i : game.getPlayer2()){
//            System.out.print(i + " ");
//        }
//        System.out.println("\n player 1:");
//        for(int i : game.getPlayer1()){
//            System.out.print(i + " ");
//        }
//        
//        try {
//            AI ai = new AI(game, 6);
            
//            int max = ai.maxValue(arr, 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
//            
//            System.out.println("result initialBoard in main:");
//            for(int i  : ai.getResultPits()){
//                System.out.print(i + " ");
//            }
//            
//            System.out.println("result board");
//            ArrayList<int[]> result = ai.outputBoardState();
//            System.out.println("player 2 :");
//        for(int i : result.get(1)){
//            System.out.print(i + " ");
//        }
//        System.out.println("\n player 1:");
//        for(int i : result.get(0)){
//            System.out.print(i + " ");
//        }
            
//            System.out.println();
//            ArrayList<ArrayList<Integer>> succ = ai.Successor(arr, 1);
//            for(ArrayList<Integer> a : succ){
//                for(int i : a)
//                    System.out.print(i + " ");
//                System.out.println();
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(AI.class.getName()).log(Level.SEVERE, null, ex);
//        }*/
    }

}