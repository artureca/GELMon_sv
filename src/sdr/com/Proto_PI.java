/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdr.com;

import java.io.BufferedReader;
import java.io.PrintWriter;
import tools.*;

/**
 *
 * @author artureca
 */
public class Proto_PI extends Protocol {

    public Proto_PI(PrintWriter out, BufferedReader in) {
        super(out, in);
    }

    @Override
    public String decode(String received) {
            String[] tokens = received.split("@");
        switch (tokens[0]){
            //case "Login": return handlerLogin(tokens);
            //case "Logout": return handlerLogout(tokens);
            default: return received.concat("_OK");
        }
    }
    
}
