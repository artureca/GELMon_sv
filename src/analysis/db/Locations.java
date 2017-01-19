/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.db;

import java.awt.geom.Point2D;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
//import javax.mail.internet.AddressException;
//import javax.mail.internet.InternetAddress;

/**
 * Class that handles the storing and retrieving of data from the locations table.
 * @author Bruno, Fábio Cunha
 */
public class Locations extends MySQL {
    
    PreparedStatement st1;
    ResultSet rs;
    
    /**
     *  Locations class constructor.
     */
    public Locations() {
        Locations.setup();
    }
    
    /**
     *  Inserts a row into the locations table.
     * @param latitude Latitude of the location.
     * @param longitude Longitude of the location.
     * @param l_time Timestamp representing the time at which the position was recorded.
     * @param email Email of the user of which the position is being store.
     * @param tmsi TMSI of the user of which the position is being store.
     * @return Returns True if successful.
     */
    public boolean setLocation(double latitude, double longitude, long l_time, String email, String tmsi) {
        
        System.out.println("Inserting location: " + latitude + " ," + longitude + " @" + l_time);
        
        try {
            String query= "INSERT INTO locations (latitude, longitude, l_time, email, tmsi) values(?, ?, ?, ?, ?) ";
            st1 = con.prepareStatement(query);
            st1.setDouble(1, latitude);
            st1.setDouble(2, longitude);
            st1.setLong(3, l_time);
            st1.setString(4, email);
            st1.setString(5, tmsi);
            st1.executeUpdate();
         
        } catch(SQLException ex){
            System.out.println("setLocation error: " + ex);
        }
        
        return true;   
    }
    
    /**
     *  Inserts a row into the locations table.
     * @param latitude Latitude of the location.
     * @param longitude Longitude of the location.
     * @param l_time Timestamp representing the time at which the position was recorded.
     * @param email Email of the user of which the position is being store.
     * @return Returns True if successful.
     */
    public boolean setLocation(double latitude, double longitude, long l_time, String email) {
        
        //if(!isEmailValid(email)) return false;
        
        System.out.println("Inserting location: " + latitude + " ," + longitude + " @" + l_time);
        
        try {
            String query = "INSERT INTO locations (latitude, longitude, l_time, email) values(?, ?, ?, ?) ";
            st1 = con.prepareStatement(query);
            st1.setDouble(1, latitude);
            st1.setDouble(2, longitude);
            st1.setLong(3, l_time);
            st1.setString(4, email);
            st1.executeUpdate();
         
        } catch(SQLException ex){
            System.out.println("setLocation error: " + ex);
            return false;
        }
        
        return true;   
    }
    
    /**
     *  Retrieves the coordinates of the recorded positions between two moments in time..
     * @param i_time Timestamp of the initial time.
     * @param f_time Timestamp of the final time.
     * @return Returns an ArrayList of Pair<Double,Double> with positions recorded.
     */
    public ArrayList<Point2D.Double> getLocation(long i_time, long f_time){
       
        ArrayList<Point2D.Double> arrayList = new ArrayList<>(); 
        
        if(i_time > f_time) return arrayList;
        
        try {
            String query = "SELECT latitude, longitude FROM locations WHERE l_time >= ? and l_time <= ? ";
            st1 = con.prepareStatement(query);
            st1.setLong(1, i_time);
            st1.setLong(2, f_time);
            rs = st1.executeQuery();
            
            while(rs.next())
                arrayList.add(new Point2D.Double(rs.getDouble("latitude"),rs.getDouble("longitude")));
            
        } catch(SQLException ex){
            System.out.println("getLocation error:" + ex);
        }
        
        return arrayList;
    }

    /**
     *  Retrieves the timestamps of the recorded positions between two moments in time..
     * @param i_time Timestamp of the initial time.
     * @param f_time Timestamp of the final time.
     * @return Returns an ArrayList of timestamps of positions recorded.
     */
    public ArrayList<Long> getTimeLocation(long i_time, long f_time){
       
        ArrayList<Long> arrayList = new ArrayList<>();
        String query = "SELECT l_time FROM locations WHERE l_time >= ? and l_time <= ?";
        
        if(i_time > f_time) return arrayList;
        
        try {
            st1 = con.prepareStatement(query);
            st1.setLong(1, i_time);
            st1.setLong(2, f_time);
            rs = st1.executeQuery();
            
            while(rs.next()) {
                arrayList.add(rs.getLong("l_time"));
            }
            
        } catch(SQLException ex){
            System.out.println("getLocation error:" + ex);
        }
        
        return arrayList;   
    }
    

    
}
