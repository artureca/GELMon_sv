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
 * @author Raquel Ribeiro
 * @author Vânia Vieira
 */
public class Image {
    private final Pixel[][] pixelMap;
    private final Integer width;
    private final Integer height;
    
    /**
     * Creates a matrix of pixels
     * @param width
     * @param height
     */
    public Image(Integer width, Integer height){
        this.pixelMap = new Pixel[width][height];
        this.width = width;
        this.height = height;
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                this.pixelMap[i][j] = new Pixel();
    }
    
    /**
     * Creates a Image from a BufferedImage.
     * @param img
     */
    public Image(BufferedImage img) {
        this.pixelMap=null;
        this.width=0;
        this.height=0;
    }

    /**
     * Fill the pixel matrix with the population values.
     * @author Raquel Ribeiro
     * @author Vânia Vieira
     * @param pop
     * @param width
     * @param height
     */
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

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }
    
    /**
     *
     * @author Raquel Ribeiro
     * @author Vânia Vieira
     * @return
     */
    public BufferedImage toBufferedImage() {
        Integer w;
        Integer h;
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        for (w=0;w<width;w++)
            for (h=0;h<height;h++)
                image.setRGB(w, h,this.pixelMap[w][h].toARGB());
        return image;
    }
    
    /**
     * Obtain a pixel in localization x and y
     * @param x correspond to local width-axis
     * @param y correspond to local height-axis
     * @return a pixel with coordinates x and y
     */
    public Pixel getPixel(Integer x, Integer y){
        return this.pixelMap[x][y];
    }    
    /**
     * Overlaps the current Image with another image (fg).
     * @param fg the foreground image to overlap
     */
    public void overlap(Image fg){
        Integer w;
        Integer h;
        for (w=0;w<this.width;w++)
            for (h=0;h<this.height;h++)
                this.pixelMap[w][h].overlap(fg.getPixel(w, h));
    }
    
    /**
     * Underlaps the current Image with another image (bg).
     * @param bg the background image to overlap
     */
    public void underlap(Image bg){
        Integer w;
        Integer h;
        for (w=0;w<this.width;w++)
            for (h=0;h<this.height;h++)
                this.pixelMap[w][h].underlap(bg.getPixel(w, h));   
    }
    
    /**
     *
     */
    public void antiAlias(){
        
    }
}
