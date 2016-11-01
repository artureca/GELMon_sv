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

package analysis.dsp;

import java.awt.image.BufferedImage;

/**
 *
 * @author Artur Antunes
 * @author Eugenio Carvalhido
 * @author Raquel Ribeiro
 * @author Vânia Vieira
 */
public class Image {
    private final Pixel[][] pixelMap;
    private final Integer width;
    private final Integer height;
       
    
    //public Image(BufferedImage im) {
    //}
   
    
    public Image(Double[][] pop, Integer width, Integer height) {
      Integer w;
      Integer h;
      this.pixelMap = new Pixel[width][height]; 
      for (w=0;w<width;w++)
          for (h=0;h<height;h++)
            this.pixelMap[w][h]= new Pixel(pop[w][h]);
      this.width=width;
      this.height=height;
    }
    
    public BufferedImage toBufferedImage() {
        Integer w;
        Integer h;
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        for (w=0;w<width;w++)
            for (h=0;h<height;h++)
                image.setRGB(w, h,this.pixelMap[w][h].toARGB());
        return image;
    }
}
