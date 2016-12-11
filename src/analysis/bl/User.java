/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.bl;

/**
 *
 * @author rubinhus
 */
public class User {
    
    String email;
    Integer userid;
    Integer sessionid;
    String name;
   
    public User(String emailR, Integer useridR, Integer sessionidR, String nameR){
        email = emailR;
        userid = useridR;
        sessionid = sessionidR;
        name = nameR;
    }
    
    
}
