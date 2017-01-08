/*
 * FEUP / MIEEC / SETEC / 2016 / Group B
 * http://fe.up.pt/
 *
 * 201202877 / Artur Antunes
 * 200907504 / Bruno Gonçalves
 * 201106784 / Eugenio Carvalhido
 * 201105402 / Fábio Cunha
 * 201206114 / Filipe Rocha
 * 201105621 / José Carvalho
 * 201100603 / Luís Pinto
 * 201200617 / Pedro Fonseca
 * 201201704 / Raquel Ribeiro
 * 201202703 / Rubens Figueiredo
 * 201109265 / Vânia Vieira
 */

package analysis.com;

import analysis.bl.Logic;
import tools.*;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The protocol used to comunicate with the AZGO mobile client.
 * 
 * @author Artur Antunes
 * @author Rubens Figueiredo
 */
public class Proto_AZGO extends Protocol {
    
    private static final ConcurrentHashMap<String,PrintWriter> USERS = new ConcurrentHashMap<>();
    private String currentUser = null;
    
    static{
        new Thread(){
            @Override
            public void run() {
                requestHandler();
            }
        }.start();
    }
    
    /**
     * Simple Constructor. Just calls the superclass' constructor.
     * 
     * @param out the uplink channel (PrintWriter)
     * @param in the downlink channel (BufferedReader)
     */
    public Proto_AZGO(PrintWriter out, BufferedReader in) {
        super(out, in);
    }
    
    @Override
    public String decode(String received){
        String[] tokens = received.split("\\$");
        //System.out.println("Received from AZGO: " + tokens[0]);
        switch (tokens[0]){
            case "Login": return handlerLogin(tokens); //Login$email$session_id
            case "Logout": return handlerLogout();
            case "Meet" : return handlerMeet(tokens);
            case "MeetRequest" : return handlerMeetRequest(tokens);
            case "Coordinates": return handlerCoordinates(tokens); 
            case "Friends": return handlerFriends(tokens);
            default: return received.concat("_OK");
        }
    }
    
    private String handlerLogin(String[] tokens){
        
        String email = tokens[1];
        String name = tokens[2];
        
        this.currentUser = email;
        
        USERS.put(this.currentUser, this.out);
              
        
        
        return "Login";
    }
    
    private String handlerLogout(){
        USERS.remove(this.currentUser);
        return null ;
    }
    
    private String handlerMeet(String[] tokens){
        Logic.requetsMeet(this.currentUser, tokens[1]);
        return " ";
    }
    
    private String handlerMeetRequest(String[] tokens){
        Logic.requetsMeet(this.currentUser, tokens[1], tokens[2].equals("OK"));
        return " ";
    }
    
    private String handlerCoordinates(String[] tokens){
        
        String user = tokens[1];
        Double lat = Double.parseDouble(tokens[2]);
        Double longi = Double.parseDouble(tokens[3]);        
        
        Logic.addLocation(lat, longi);
        
        return "Coordinates".concat("$").concat("OK") ;
    }   
    
    private String handlerFriends(String[] tokens){
       
       ArrayList<String> lAmigos = new ArrayList<String>();
        
       for(int i=1;i<tokens.length;i++){
           lAmigos.add(tokens[i]);
       }
       
       String envio = Logic.getFriendsInf(lAmigos);
       
       return envio;
    }  

    private static void requestHandler(){
        for(;;){
            String request = Logic.REQUESTS.poll();
            
            if (request == null)
                continue;
            
            String splitRequest[] = request.split("#");
            switch (splitRequest[0]){
                case "MeetRequest": requestMeetRequest(splitRequest);
                case "Meet": requestMeet(splitRequest);
            }
        }
    }
    
    private static void requestMeetRequest(String[] tokens){
        PrintWriter rout = USERS.get(tokens[1]);
        
        if (rout == null)
            return;
        
        sendTo("MeetRequest$" + tokens[2], rout);
    }
    
    private static void requestMeet(String[] tokens){
        PrintWriter rout = USERS.get(tokens[1]);
        
        if (rout == null)
            return;
        
        String resp = "Meet$" + tokens[2];
        for (int i = 3; i < tokens.length; i++) {
            resp = resp + "$" + tokens[i];
        }
        
        sendTo(resp, rout);
    }
    
    @Override
    public void kill() {
        handlerLogout();
    }
}
