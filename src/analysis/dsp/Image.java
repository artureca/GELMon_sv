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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import tools.Coord;

/**
 * Represents an image transformed in a matrix of pixels
 * @author Eugénio Carvalhido
 * @author Raquel Ribeiro
 * @author Vânia Vieira
 */
public class Image {
    protected final Pixel[][] pixelMap;
    
    /**
     * Creates a matrix of pixels
     * @author Eugénio Carvalhido
     * @author Raquel Ribeiro
     * @author Vânia Vieira
     * @param width represents the widht of a matrix
     * @param height represents the height of a matrix
     */
    public Image(Integer width, Integer height){
        this.pixelMap = new Pixel[width][height];
        for (int j = 0; j < width; j++)
            for (int i = 0; i < height; i++)
                this.pixelMap[j][i] = new Pixel();
    }
    
    public Image(ConcurrentHashMap<Coord,AtomicLong> pop, Integer width, Integer height) {
        this.pixelMap = new Pixel[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Coord coord = new Coord();
                coord.setLocation(i, j);
                AtomicLong tmp = pop.get(coord);
                if (tmp != null) {
                    this.pixelMap[i][j] = new Pixel(Double.longBitsToDouble(tmp.get()));
                }else{
                    this.pixelMap[i][j] = new Pixel(0.0);
                }
            }
        }
    }
    
    /**
     * Creates a Image from a BufferedImage.
     * @author Raquel Ribeiro
     * @author Vânia Vieira
     * @param img represents a buffer of image data
     */
    public Image(BufferedImage img) {
        Integer width = img.getWidth();
        Integer height = img.getHeight();
        this.pixelMap = new Pixel[width][height];
        
        for (int j = 0; j < width; j++){
            for (int i = 0; i < height; i++){
                try{
                    this.pixelMap[j][i] = new Pixel(img.getRGB(j, i));
                }catch(Exception ex){
                    System.out.println("i: "+i+"| j: "+j);
                    throw ex;
                }
            }
        }
        
    }

    /**
     * Fill the pixel matrix with the population values.
     * @author Eugénio Carvalhido
     * @author Raquel Ribeiro
     * @author Vânia Vieira
     * @param pop represents the value of population in a certain coordinate
     * @param width represents the width of the matrix pop
     * @param height represents the height of the matrix pop
     */
    public Image(Double[][] pop, Integer width, Integer height) {
      Integer w;
      Integer h;
      this.pixelMap = new Pixel[width][height]; 
      for (w=0;w<width;w++){
          for (h=0;h<height;h++){
            this.pixelMap[w][h]= new Pixel(pop[w][h]);
          }}
    }

    /**
     * Obtain a width
     * @author Artur Antunes
     * @return the width in a integer value 
     */
    public Integer getWidth() {
        return this.pixelMap.length;
    }

    /**
     * Obtain a height
     * @author Artur Antunes
     * @return the height in a integer value
     */
    public Integer getHeight() {
        return this.pixelMap[0].length;
    }
    
    /**
     * Obtain a buffered image in RGB
     * @author Raquel Ribeiro
     * @author Vânia Vieira
     * @return a RGB image in a buffered image type
     */
    public BufferedImage toBufferedImage() {
        Integer w;
        Integer h;
        BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
       
        for (w=0;w<this.pixelMap.length;w++){
            for (h=0;h<this.pixelMap[w].length;h++){
                image.setRGB(w, h,this.pixelMap[w][h].toARGB());
            }
        }
         System.out.println();
        return image;
    }
    
    /**
     * Obtain a pixel in localization x and y
     * @author Raquel Ribeiro
     * @author Vânia Vieira
     * @param x correspond to local width-axis
     * @param y correspond to local height-axis
     * @return a pixel with coordinates x and y
     */
    public Pixel getPixel(Integer x, Integer y){
        return this.pixelMap[x][y];
    }    
    /**
     * Overlaps the current Image with another image (fg).
     * @author Raquel Ribeiro
     * @author Vânia Vieira
     * @param fg the foreground image to overlap
     */
    public void overlap(Image fg){
        Integer w;
        Integer h;
        for (w=0;w<this.pixelMap.length;w++)
            for (h=0;h<this.pixelMap[0].length;h++)
                this.pixelMap[w][h].overlap(fg.getPixel(w, h));
    }
    
    /**
     * Underlaps the current Image with another image (bg).
     * @author Raquel Ribeiro
     * @author Vânia Vieira
     * @param bg the background image to overlap
     */
    public void underlap(Image bg){
        Integer w;
        Integer h;
        Double racio = 0.5;
        for (w=0;w<this.pixelMap.length;w++)
            for (h=0;h<this.pixelMap[0].length;h++)
                this.pixelMap[w][h].underlap(bg.getPixel(w, h),racio);   
    }

}
