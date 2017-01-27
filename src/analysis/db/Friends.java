/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.db;

import static analysis.db.MySQL.con;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Class that handles the storing and retrieving of data from the friends table.
 * @author Fábio Cunha
 */
public class Friends extends MySQL {
    
    PreparedStatement st1;
    ResultSet rs;
    
    /**
     * Locations class constructor.
     */
    public Friends() {
        Friends.setup();
    }
    
    /**
    * Method that retrieves the users' friends.
    * @param userEmail User's email for which the friends are to be retrieved.
    * @return Returns the friends in a ArrayList{@literal <}String{@literal >} form.
    */
    public ArrayList<String> getFriends(String userEmail) {
        
        ArrayList<String> sInfo;
        sInfo = new ArrayList<>();
        int i, j;

        try {
            String query = "SELECT email2 FROM friends WHERE email1 = ? ";
            st1 = con.prepareStatement(query);
            st1.setString(1, userEmail);
            rs = st1.executeQuery();

            while (rs.next()) {
                sInfo.add(rs.getString("email2"));
            }

        } catch (SQLException ex) {
            System.out.println("getLocation error:" + ex);
        }
        
        for(i = 0; i < sInfo.size(); i++) {
            for (j = i + 1; j < sInfo.size(); j++) {
                if (sInfo.get(i).equals(sInfo.get(j))) {
                    sInfo.remove(j);
                }
            }
        }
        
        return sInfo;
    }
    
    /**
    * Method that inserts a friends combination record.
    * @param userEmail1 User 1 email.
    * @param userEmail2 User 2 email.
    * @return Returns the true if successful.
    */
    public Boolean addFriend(String userEmail1, String userEmail2) {
        
        if(!(isEmailValid(userEmail1) || isEmailValid(userEmail2))) return false;
        
        try {
            String query = "INSERT INTO friends (email1, email2) VALUES(?, ?) ON DUPLICATE KEY UPDATE email1=?, email2=?";
            st1 = con.prepareStatement(query);
            st1.setString(1,userEmail1);
            st1.setString(2,userEmail2);
            
            st1.executeUpdate();
            
        } catch(SQLException ex) {
            System.out.println("vfLogin error:" + ex);
            return false;
        }
        
        return true;
    }
    
    /**
    *  Check if email string is valid.
    * @param email String to evaluate.
    * @return Returns true if valid and false otherwise.
    */
    private static boolean isEmailValid(String email) {
        
        try {
           InternetAddress emailAddr = new InternetAddress(email);
           emailAddr.validate();
        } catch (AddressException ex) {
           return false;
        }
        return true;
    }
    
}
