/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.imageio.ImageIO;

/**
 *
 * @author Artur Antunes
 */
public class FileSystem {
    private static final HashMap<String,String> CONFIG = new HashMap<>();
    
    public static Boolean fileExists(String path){
        File f = new File(path);
        if(f.exists() && !f.isDirectory())
            return true;
        return false;
    }
    
    public static void loadDefaultConfig(){
        BufferedReader txtReader = new BufferedReader(new InputStreamReader(FileSystem.class.getResourceAsStream("default.conf")));
        try {
            while(true){
                String line = txtReader.readLine();
                if (null == line)
                    break;
                String[] tokens = line.split("=");
                CONFIG.put(tokens[0], tokens[1]);
            }
        } catch (IOException ex) {
            //Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static BufferedImage loadImage(String path){
        BufferedImage img;
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException ex) {
            //Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
            img = null;
        }
        return img;
    }
    
    public static Boolean saveImage(String path, BufferedImage img){
        Boolean flag;
        try {
            File imgFile = new File(path);
            imgFile.getParentFile().mkdirs();
            imgFile.createNewFile();
            ImageIO.write(img, "png", imgFile);
            flag = true;
        } catch (IOException ex) {
            //Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
            flag = false;
        }
        return flag;
    }
    
    public static ArrayList<String> loadText(String path){
        ArrayList<String> text = new ArrayList<>();
        try {
            for (Iterator<String> it = Files.readAllLines(Paths.get(path)).iterator(); it.hasNext();)
                text.add(it.next());
        } catch (IOException ex) {
            //Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return text;
    }
    
    public static Boolean saveText(String path, ArrayList<String> data){
        Boolean flag;
        try {
            Files.write(Paths.get(path), data, StandardCharsets.UTF_8);
            flag = true;
        } catch (IOException ex) {
            //Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
            flag = false;
        }
        return flag;        
    }
    
    public static void loadConfig(String path){
        CONFIG.clear();
        ArrayList<String> text = loadText(path);
        if (text.isEmpty()){
            loadDefaultConfig();
            return;
        }
        text.forEach((line) -> {
            String[] tokens = line.split("=");
            CONFIG.put(tokens[0], tokens[1]);
        });
    }
    
    public static String getConfig(String key){
        if (CONFIG.isEmpty())
            loadDefaultConfig();
        
        if (!CONFIG.containsKey(key)) 
            System.err.println("Loading null config from key: " + key);
            
        return CONFIG.get(key);
    }
    
    public static void displayCurrentConfig(){
        CONFIG.forEach((k,v) -> System.out.println(k + " | " + v));
    }
}
