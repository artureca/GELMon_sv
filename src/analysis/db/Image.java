/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.db;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

/**
 *
 * @author Bruno
 */
public class Image extends MySQL {
    
    
    
    public Image() {
    }
    
    PreparedStatement st1;
    
    public boolean setImage(String path,Timestamp i_time,Timestamp f_time){
        
        try{
            String query= "INSERT INTO images (path,i_time,f_time) values(?,?,?) ";
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
