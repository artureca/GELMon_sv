/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imgDSP;

import java.awt.image.BufferedImage;

/**
 *
 * @author Raquel
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
