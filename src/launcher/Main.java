/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launcher;

import analysis.com.Proto_AZGO;
import analysis.com.Proto_PHP;
import tools.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import sdr.com.Proto_PI;

/**
 *
 * @author artureca
 */
public class Main {
    private static final Boolean HOLDER = false;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        // TODO code application logic here
        
        // Start AZGO tcp interface server
        TCP_sv<Proto_AZGO> tcp_azgo_sv = new TCP_sv<>(Proto_AZGO.class,21111,"AZGO TCP/IP Server");
        try {
            tcp_azgo_sv.start();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Start PHP tcp interface server
        TCP_sv<Proto_PHP> tcp_php_sv = new TCP_sv<>(Proto_PHP.class,21112,"PHP TCP/IP Server");
        try {
            tcp_php_sv.start();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        // Start SDR udp server
        UDP_sv<Proto_PI> udp_pi_sv = new UDP_sv<>(Proto_PI.class,21113,"Raspberry PI UDP Server");
        try {
            udp_pi_sv.start();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
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