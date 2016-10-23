/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.com;

import tools.*;
import java.io.BufferedReader;
import java.io.PrintWriter;


/**
 *
 * @author artureca
 */
public class Proto_PHP extends Protocol {

    public Proto_PHP(PrintWriter out, BufferedReader in) {
        super(out, in);
    }

    @Override
    public String decode(String received){
        String[] tokens = received.split("@");
        switch (tokens[0]){
            case "itmap": return handlerItmap(tokens);
            //case "Logout": return handlerLogout(tokens);
            default: return null;
        }
    }
    
    private String handlerItmap(String[] tokens) {
        
        String dat1 = tokens[1];
        String dat2 = tokens[2];
        
        String fetch1 = "";
        String fetch2 = "";
        
        String itmapLocale = "";

        
        fetch1.concat(dat1);
        fetch2.concat(dat2);
        
        //Pass to Logic fetch1, fetch2, get the itmap location
        
        //itmapLocale = getItmap(fetch1, fetch2);
        
        return "ok";
    }
}
