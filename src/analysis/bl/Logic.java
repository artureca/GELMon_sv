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
        Double[][] values = new Double
                [Heatmap.getBackground().getWidth()]
                [Heatmap.getBackground().getHeight()];
        
        // Somehow get the values from the database
        // Convert them from coordenates
        
        Heatmap heatmap = new Heatmap(values);
        heatmap.generate();
        BufferedImage image = heatmap.toBufferedImage();
        String imagePath = MD5.crypt(date1.toString().concat(date2.toString()));
        
        while(!FileSystem.saveImage(imagePath.concat(".PNG"), image))
            imagePath = MD5.crypt(imagePath);
        
        return imagePath;
    }
    
    public static void setup() {
        
    }
    
}
