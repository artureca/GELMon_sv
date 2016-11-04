/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Artur Antunes
 */
public class FileSystem {
    private static final HashMap<String,String> CONFIG = new HashMap<>();
    
    private static void loadDefaultConfig(){
        CONFIG.put("PHP.status" , "ON");
        CONFIG.put("PHP.port"   , "20501");
        CONFIG.put("AZHO.status", "ON");
        CONFIG.put("AZGO.port"  , "20502");
        CONFIG.put("PI.status"  , "ON");
        CONFIG.put("PI.port"    , "20503");
    }
    
    public static BufferedImage loadImage(String path){
        BufferedImage img;
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException ex) {
            Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
            img = null;
        }
        return img;
    }
    
    public static Boolean saveImage(String path, BufferedImage img){
        Boolean flag;
        try {
            ImageIO.write(img, "png", new File(path));
            flag = true;
        } catch (IOException ex) {
            Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return text;
    }
    
    public static Boolean saveText(String path, ArrayList<String> data){
        Boolean flag;
        try {
            Files.write(Paths.get(path), data, StandardCharsets.UTF_8);
            flag = true;
        } catch (IOException ex) {
            Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
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
        
        return CONFIG.get(key);
    }
}
