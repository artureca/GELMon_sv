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

import analysis.bl.*;
import tools.*;
import java.io.BufferedReader;
import java.io.PrintWriter;


/**
 * The protocol used to comunicate with the PHP server.
 * 
 * @author Artur Antunes
 * @author Rubens Figueiredo
 */
public class Proto_PHP extends Protocol {

    /**
     * Simple Constructor. Just calls the superclass' constructor.
     * 
     * @param out the uplink channel (PrintWriter)
     * @param in the downlink channel (BufferedReader)
     */
    public Proto_PHP(PrintWriter out, BufferedReader in) {
        super(out, in);
    }

    @Override
    public String decode(String received){
        String[] tokens = received.split("@");
        switch (tokens[0]){
            case "heatmap": return handlerHeatmap(tokens);
            case "graph": return handlerGraph(tokens);
            case "matrix": return handlerDebugMatrix(tokens);
            //case "video": return handlerVideo(tokens);
            //case "Logout": return handlerLogout(tokens);
            default: return "UNKNOWN REQUEST";
        }
    }
    
    private String handlerHeatmap(String[] tokens) {
        // no need to complicate stuff
        // the logic must be passed with the correct format!!!
        // no more Strings from this point forward, unless no other option
        return Logic.getHeatmap(Long.decode(tokens[1]), Long.decode(tokens[2]));
    }
    
    private String handlerGraph(String[] tokens) {
        // no need to complicate stuff
        // the logic must be passed with the correct format!!!
        // no more Strings from this point forward, unless no other option
        /*ArrayList<Pair<Double,Double>> points = new Locations().getLocation(Long.decode(tokens[1]), Long.decode(tokens[2]));
        
        points.stream().forEach((point) -> {
            System.out.println(Logic.getsBuilding(point.getK(),point.getV()));
        });*/
       
        
        return Logic.getNumberOfLocationsByInterval(Long.decode(tokens[1]), Long.decode(tokens[2]), Long.decode(tokens[3]));
    }
    
    private String handlerDebugMatrix(String[] tokens) {
        // no need to complicate stuff
        // the logic must be passed with the correct format!!!
        // no more Strings from this point forward, unless no other option
        return Logic.debugMatrix(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
    }

    @Override
    public void kill() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
