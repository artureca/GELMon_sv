/*
 * FEUP / MIEEC / SETEC / 2016 / Group B
 * http://fe.up.pt/
 *
 * 201202877 / Artur Antunes
 * 200907504 / Bruno GonÃ§alves
 * 201106784 / Eugenio Carvalhido
 * 201105402 / FÃ¡bio Cunha
 * 201206114 / Filipe Rocha
 * 201105621 / JosÃ© Carvalho
 * 201100603 / LuÃ­s Pinto
 * 201200617 / Pedro Fonseca
 * 201201704 / Raquel Ribeiro
 * 201202703 / Rubens Figueiredo
 * 201109265 / VÃ¢nia Vieira
 */

package analysis.db;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author ???
 */
public class MySQL {
    
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://db.fe.up.pt:3306/setec16_17";
    static final String USER = "setec16_17";
    static final String PASS = "setec1617";
    
    
     public  MySQL() {
            
        Connection con = null; 
         
        try{
            
            Class.forName(JDBC_DRIVER);
            con = DriverManager.getConnection(DB_URL,USER,PASS);
            
        }catch (Exception ex){
            System.out.println("erro " +ex);
        }
    
    
    }
    
}
