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
import java.sql.SQLException;
import tools.FileSystem;

/**
 * Parent Class for the child classes that handle storing and retrieving of information to and from the database.
 * 
 * @author Bruno, Fábio Cunha
 */
public class MySQL {
     
    private static final String DB_URL = "jdbc:mysql://db.fe.up.pt:3306/setec16_17";
    private static final String USER = "setec16_17";
    private static final String PASS = "setec1617";
    static Connection con = null;
    
    /**
    * Method that sets up the connection to the database.
    */
    public static void setup() {
         
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(
                    FileSystem.getConfig("MYSQL.url"),
                    FileSystem.getConfig("MYSQL.user"),
                    FileSystem.getConfig("MYSQL.password")
            );
        } catch (ClassNotFoundException | SQLException ex){
            System.out.println("erro " + ex);
        }
    } 
    
}
