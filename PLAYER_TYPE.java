/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kalahfx;

/**
 * Created by Bobby on 11/10/17.
 */
public enum PLAYER_TYPE {
    HUMAN,
    EASY_AI,
    MEDIUM_AI,
    HARD_AI;
    
    public static PLAYER_TYPE get(String s){
        if(s.toLowerCase().equals("human")){
            return HUMAN;
        }
        else if(s.toLowerCase().equals("easy_ai")) {
            return EASY_AI;
        }else if(s.toLowerCase().equals("medium_ai")) {
            return MEDIUM_AI;
        }else{
            return HARD_AI;
        }
    }
}
