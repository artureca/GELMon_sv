/*
 * FEUP / MIEEC / SETEC / 2016 / Group B
 * http://fe.up.pt/
 *
 * 201202877 / Artur Antunes
 * 200907504 / Bruno Gonçalves
 * 201106784 / Eugenio Carvalhido
 * 201105402 / Fábio Cunha
 * 201206114 / Filipe Rocha
 * 201105621 / José Carvalho
 * 201100603 / Luís Pinto
 * 201200617 / Pedro Fonseca
 * 201201704 / Raquel Ribeiro
 * 201202703 / Rubens Figueiredo
 * 201109265 / Vânia Vieira
 */

package analysis.bl;

import analysis.dsp.Heatmap;
import java.awt.image.BufferedImage;
import tools.FileSystem;


/**
 * A class with static methods used to process the clients requests.
 * 
 * @author Artur Antunes
 * @author Rubens Figueiredo
 */
public class Logic {
    
    /**
     * Gets the reqeusted heatmap path, creating it if it doesn't exist.
     * 
     * @author Rubens Figueiredo
     * @param date1
     * @param date2
     * @return
     */
    public static String getHeatmap(Long date1, Long date2){
        
        String imagePath = MD5.crypt(date1.toString().concat(date2.toString()));
        if (FileSystem.fileExists("./public_html/images/" + imagePath + ".png")) {
            return "http://paginas.fe.up.pt/~setec16_17/images/" + imagePath + ".png";
        }
        // verify if it already existes in DB
        // if so, return path
        // wait if being processed
        
        /*Double[][] values = new Double
        [Heatmap.getBackground().getWidth()]
        [Heatmap.getBackground().getHeight()];
        */
        
        Double[][] values = new Double
                [1000]
                [1000];
        
        for (int i = 0; i < 1000; i++)
            for (int j = 0; j < 1000; j++)
                values[i][j] = (i + j) * 1.0/2000;
            
        
        
        // Somehow get the values from the database
        // Convert them from coordenates
        
        Heatmap heatmap = new Heatmap(values);
        //heatmap.generate();
        BufferedImage image = heatmap.toBufferedImage();
        
        while(!FileSystem.saveImage("./public_html/images/" + imagePath + ".png", image))
            imagePath = MD5.crypt(imagePath);
        
        // update database
        // wake up waiting threads
        
        return "http://paginas.fe.up.pt/~setec16_17/images/" + imagePath + ".png";
    }
    
    public static void setup() {
        
    }
    
}
