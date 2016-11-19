/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 *
 * @author Bruno
 */
public class Locations {
    
    public Locations() {
        
       
    }
    
    PreparedStatement st1;
    ResultSet rs;
    
    public boolean setLocation(Connection con,double latitude, double longitude,String tmsi,Timestamp l_time){
        
        try{
            String query= "INSERT INTO locations (latitude,longitude,tmsi,l_time) values(?,?,?,?) ";
            st1 = con.prepareStatement(query);
            st1.setDouble(1,latitude);
            st1.setDouble(2,longitude);
            st1.setString(3,tmsi);
            st1.setTimestamp(4,l_time);
            st1.executeUpdate();
         
        }catch(Exception ex){
            System.out.println("setLocation error:"+ex);
        }
        
     return true;   
    }
    
    
    public  ArrayList<String> getLocation(Connection con,Timestamp i_time,Timestamp f_time){
       
        ArrayList<String> arrayList = new ArrayList<String>(); 
        
        try{
            String query= "SELECT latitude,longitude FROM locations WHERE l_time >= ? and l_time <= ? ";
            st1 = con.prepareStatement(query);
            st1.setTimestamp(1,i_time);
            st1.setTimestamp(2,f_time);
            rs=st1.executeQuery();
            
             
            
            while(rs.next()){
                
                int i = 1;
                while(i <= 2) {
                    
                    arrayList.add(rs.getString(i++));
                    
                }    
                
           // System.out.println(rs.getString("latitude"));
           // System.out.println(rs.getString("longitude"));
         

            }
           
         
        }catch(Exception ex){
            System.out.println("getLocation error:"+ex);
        }
        
     return (arrayList);   
    }
    
    public  ArrayList<String> getTimeLocation(Connection con,Timestamp i_time,Timestamp f_time){
       
        ArrayList<String> arrayList = new ArrayList<String>(); 
        
        try{
            String query= "SELECT l_time FROM locations WHERE l_time >= ? and l_time <= ? ";
            st1 = con.prepareStatement(query);
            st1.setTimestamp(1,i_time);
            st1.setTimestamp(2,f_time);
            rs=st1.executeQuery();
            
            while(rs.next()){
            
                
                    
                    arrayList.add(rs.getString("l_time"));
                    
             

            }
           
         
        }catch(Exception ex){
            System.out.println("getLocation error:"+ex);
        }
        
     return (arrayList);   
    }
    
    
    
}
