/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author artureca
 */
public class Main {
    private static final Boolean HOLDER = false;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
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
        
        System.out.println("Running!");
        while(true);
        /*
        synchronized(HOLDER) {
            while (true) {
                try {
                    HOLDER.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        */
    }
    
}
