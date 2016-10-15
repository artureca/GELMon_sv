/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

/**
 *
 * @author artureca
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        // Start AZGO tcp interface server
        TCP_sv<Proto_AZGO> tcp_azgo_sv = new TCP_sv<>(Proto_AZGO.class,21111);
        tcp_azgo_sv.start();
        
        // Start PHP tcp interface server
        TCP_sv<Proto_PHP> tcp_php_sv = new TCP_sv<>(Proto_PHP.class,21112);
        tcp_php_sv.start();
    }
    
}
