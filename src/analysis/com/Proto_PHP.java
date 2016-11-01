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

import tools.*;
import java.io.BufferedReader;
import java.io.PrintWriter;


/**
 *
 * @author Artur Antunes
 * @author Rubens Figueiredo
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
                
        fetch1 = fetch1.concat(dat1);
        fetch2 = fetch2.concat(dat2);
        
        //Pass to Logic fetch1, fetch2, get the itmap location
        
        //itmapLocale = getItmap(fetch1, fetch2);
        
        return analysis.bl.Logic.getItmap(fetch1, fetch2);
    }
}
