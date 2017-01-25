/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.db;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

/**
 * Class that handles the storing and retrieving of data from the images
 * table.
 *
 *
 * @author Bruno, Fábio Cunha
 */
public class Image extends MySQL {
    
    /**
     * Image class constructor.
     */
    public Image() {
        Image.setup();
    }
    
    PreparedStatement st1;
    
    /**
     * Inserts a row into the images table.
     *
     * @param path Path of the image.
     * @param i_time Timestamp of the initial time.
     * @param f_time Timestamp of the final time.
     * @return Returns True if successful.
     */
    public boolean setImage(String path, Timestamp i_time, Timestamp f_time) {
        
        try{
            String query = "INSERT INTO images (path,i_time,f_time) values(?,?,?) ";
            st1 = con.prepareStatement(query);
            st1.setString(1,path);
            st1.setTimestamp(2,i_time);
            st1.setTimestamp(3,f_time);
            st1.executeUpdate();
         
        }catch(Exception ex){
            System.out.println("setImage error:"+ex);
        }
        
     return true;   
    }
    
}
