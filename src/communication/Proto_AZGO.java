/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import java.io.BufferedReader;
import java.io.PrintWriter;


/**
 *
 * @author artureca
 */
public class Proto_AZGO extends Protocol {

    public Proto_AZGO(PrintWriter out, BufferedReader in) {
        super(out, in);
    }

    @Override
    String decode(String received){
        System.out.println(received);
        String[] tokens = received.split("@");
        switch (tokens[0]){
            //case "Login": return handlerLogin(tokens);
            //case "Logout": return handlerLogout(tokens);
            default: return received.concat("_OK");
        }
    }
    

}
