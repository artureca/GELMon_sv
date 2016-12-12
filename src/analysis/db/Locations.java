/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import tools.Pair;

/**
 *
 * @author Bruno
 */
public class Locations extends MySQL{
    
    
    PreparedStatement st1;
    ResultSet rs;
    
    public boolean setLocation(double latitude, double longitude, String tmsi,long l_time){
        
        try{
            String query= "INSERT INTO locations (latitude,longitude,tmsi,l_time) values(?,?,?,?) ";
            st1 = con.prepareStatement(query);
            st1.setDouble(1,latitude);
            st1.setDouble(2,longitude);
            st1.setString(3,tmsi);
            st1.setTimestamp(4,new Timestamp(l_time));
            st1.executeUpdate();
         
        }catch(Exception ex){
            System.out.println("setLocation error:"+ex);
        }
        
        return true;   
    }
    
    /**
     *  Metam a merda do javadoc para um gajo saber como trabalhar com isto!!!.
     * @param i_time
     * @param f_time
     * @return Pair<Latitude,Longitude>
     */
    public ArrayList<Pair<Double,Double>> getLocation(long i_time,long f_time){
       
        ArrayList<Pair<Double,Double>> arrayList = new ArrayList<>(); 
        
        try{
            String query= "SELECT latitude,longitude FROM locations WHERE l_time >= ? and l_time <= ? ";
            st1 = con.prepareStatement(query);
            st1.setTimestamp(1,new Timestamp(i_time));
            st1.setTimestamp(2,new Timestamp(f_time));
            rs=st1.executeQuery();
            
            while(rs.next())
                arrayList.add(new Pair<>(rs.getDouble("latitude"),rs.getDouble("longitude")));
            
        }catch(Exception ex){
            System.out.println("getLocation error:"+ex);
        }
        
        return arrayList;   
    }
    
    public  ArrayList<Long> getTimeLocation(long i_time,long f_time){
       
        ArrayList<Long> arrayList = new ArrayList<>(); 
        
        try{
            String query= "SELECT l_time FROM locations WHERE l_time >= ? and l_time <= ? ";
            st1 = con.prepareStatement(query);
            st1.setTimestamp(1,new Timestamp(i_time));
            st1.setTimestamp(2,new Timestamp(f_time));
            rs=st1.executeQuery();
            
            while(rs.next()){
                    arrayList.add(rs.getTimestamp("l_time").getTime()/1000);
            }
         
        }catch(Exception ex){
            System.out.println("getLocation error:"+ex);
        }
        
        return arrayList;   
    }
    
        public  ArrayList<Long> getTimeLocationAsLong(long i_time,long f_time){
       
        ArrayList<Long> arrayList = new ArrayList<>();
        int i=0; //for testing
        
        try{
            String query= "SELECT l_time FROM locations WHERE l_time >= ? and l_time <= ? ";
            st1 = con.prepareStatement(query);
            st1.setLong(1, i_time);
            st1.setLong(2, f_time);
            rs=st1.executeQuery();
            
            while(rs.next()){
                    arrayList.add(rs.getLong("l_time"));
                    System.out.println(arrayList.get(i));//for testing
                    i++;//for testing
            }
         
        }catch(Exception ex){
            System.out.println("getLocation error:"+ex);
        }
        
        return arrayList;   
    }
    
    
    
}
