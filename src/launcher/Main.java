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

package launcher;

import analysis.com.*;
import sdr.com.*;
import tools.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds the main function. Which serves as a daemon launcher.
 *  
 * @author Artur Antunes
 */
public class Main {
    private static final Boolean HOLDER = false;  
    /**
     * A simple daemon launcher.
     * @param args the command line arguments
     * @author Artur Antunes
     */
    public static void main(String[] args){
        // TODO code application logic here
          
        FileSystem.loadConfig("~/.config/gelmon/gelmon_sv.conf");
        FileSystem.displayCurrentConfig();
        
        analysis.bl.Logic.setup();
        
        // THIS IS COMPLETLY RIGHT !!!
//        System.out.println("DATABASE FUNCTIONS TEST");
//        System.out.println("-----------------------");
//        Timestamp iniciots = Timestamp.valueOf("2016-03-01 00:00:00");
//        Timestamp finalts = Timestamp.valueOf("2016-03-01 23:59:59");
//        Timestamp step = Timestamp.valueOf("2016-03-01 23:59:59");
//        System.out.println(Logic.getNumberOfLocationsByInterval(iniciots, finalts, 60));   
//        System.out.println("-----------------------");
        
                
        TCP_sv<Proto_AZGO> tcp_azgo_sv = new TCP_sv<>(
                Proto_AZGO.class,
                Integer.decode(FileSystem.getConfig("AZGO.port")),
                "AZGO TCP/IP Server"
        );
        
        TCP_sv<Proto_PHP> tcp_php_sv = new TCP_sv<>(
                Proto_PHP.class,
                Integer.decode(FileSystem.getConfig("PHP.port")),
                "PHP TCP/IP Server"
        );
        
        UDP_sv<Proto_PI> udp_pi_sv = new UDP_sv<>(Proto_PI.class,
                Integer.decode(FileSystem.getConfig("PI.port")),
                "Raspberry PI UDP Server"
        );
        
        // Start AZGO tcp interface server
        if (FileSystem.getConfig("AZGO.status").equals("ON")) {
            try {
                tcp_azgo_sv.start();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // Start PHP tcp interface server
        if (FileSystem.getConfig("PHP.status").equals("ON")) {
            try {
                tcp_php_sv.start();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // Start SDR udp server
        if (FileSystem.getConfig("PI.status").equals("ON")) {
            try {
                udp_pi_sv.start();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        
        
        synchronized(HOLDER) {
            while (true) {
                try {
                    HOLDER.wait(5000);
                    //System.out.println("Running!");
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
}
