/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Bruno
 */
public class Users extends MySQL{
            
    PreparedStatement st1;
    ResultSet rs;
    
    
    public boolean vfLogin(String name, String email, int number){      
             
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
                String query2= "INSERT INTO users (email,name,number) values(?,?,?) ";
                st1 = con.prepareStatement(query2);
                st1.setString(1,email);
                st1.setString(2, name);
                st1.setInt(3, number);
                st1.executeUpdate();
                return true;
            }
            
        }catch(Exception ex){
            System.out.println("vfLogin error:"+ex);
        }
        
     return true;   
    }

    public static boolean isInteger(String s){ //Funcao para ver se a string é um numero inteiro (numero telefone)
            try{
                Integer.parseInt(s);
            } catch(NumberFormatException e){
                return false;
            } catch(NullPointerException e){
                return false;
            }
            return true;
        }
    
    public ArrayList<String> getFriendsInfo(ArrayList<String> lAmigos){
        
        ArrayList<String> sInfo = new ArrayList<String>();
        int j;
        
        for(j=0;j<lAmigos.size();j++){
            if (isInteger(lAmigos.get(j))){
                try{ //pesquisa SQL para extrair dados caso seja numero de telefone
                    String query = "SELECT * FROM users WHERE number = ?";
                    st1 = con.prepareStatement(query);
                    st1.setInt(1, Integer.parseInt(lAmigos.get(j)));
                    rs = st1.executeQuery();
                    if(rs.next()) {
                        String la1 = rs.getString("email"); //busca sql para obter do nickname do user
                        String la2 = rs.getString("name");//busca sql para obter do nome do user
                        String la3 = rs.getString("number");//busca sql para obter do email do user
                        sInfo.add(la1);
                        sInfo.add(la2);
                        sInfo.add(la3);
                    }
                }catch (SQLException e) {
                    e.getMessage();
                    e.printStackTrace();
                }
            }
            else {
                try{ //pesquisa SQL para extrair dados caso seja email
                    String query = "SELECT * FROM users WHERE email LIKE ?";
                    st1 = con.prepareStatement(query);
                    st1.setString(1, lAmigos.get(j));
                    rs = st1.executeQuery();
                    if(rs.next()) {
                        String la1 = rs.getString("email"); //busca sql para obter do nickname do user
                        String la2 = rs.getString("name");//busca sql para obter do nome do user
                        String la3 = rs.getString("number");//busca sql para obter do email do user
                        sInfo.add(la1);
                        sInfo.add(la2);
                        sInfo.add(la3);
                    }
                }catch (SQLException e) {
                    e.getMessage();
                    e.printStackTrace();
                }
            }
        }
        return sInfo;
    }
}
