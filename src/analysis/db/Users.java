/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Bruno
 */
public class Users {

    public Users() {
    }
    
            
    PreparedStatement st1;
    ResultSet rs;
    
    
    public boolean vfLogin(Connection con,String email){
        
             
        try{
            String query= "SELECT * FROM users WHERE email = ?";
            st1 = con.prepareStatement(query);
            st1.setString(1,email);
            rs=st1.executeQuery();
            
            if(rs.next()){
                System.out.println("Login OK");
               return true;
               
            }else{
                System.out.println("User não existe. Adicionado");
                String query2= "INSERT INTO users (email) values(?) ";
                st1 = con.prepareStatement(query2);
                st1.setString(1,email);
                st1.executeUpdate();
                return true;
            }
            
        }catch(Exception ex){
            System.out.println("vfLogin error:"+ex);
        }
        
     return true;   
    }
    
    
    
    
    
}
