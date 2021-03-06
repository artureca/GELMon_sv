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

/**
 * Represents a pixel in ARGB mode.
 * 
 * @author Artur Antunes
 * @author Eugénio Carvalhido
 * @author Raquel Ribeiro
 * @author Vânia Vieira
 */

public final class Pixel {
    private Integer R;
    private Integer G;
    private Integer B;
    private Double A;

    /**
     * Creates a white opaque pixel.
     * @author Artur Antunes
     */
    public Pixel(){
        R = G = B = 0;
        A = 1.0;
    }
    
    /**
     *
     * @param pixel
     */
    public Pixel(Pixel pixel){
        this.set(pixel);
    }
    
    /**
     * Creates a Pixel from an argb integer (TYPE_INT_ARGB).
     * 
     * @author Artur Antunes
     * @param argb The pixel's argb value
     */
    public Pixel(Integer argb) {
        Integer a = (argb >> 24) & 0xFF;
        R = (argb >> 16) & 0xFF;
        G = (argb >> 8) & 0xFF;
        B = argb & 0xFF;
        A = a * 1.0/0xFF;
    }
    
    /**
     * Creates a Pixel from a decimal value based on a color distribution. Based
     * on the values from 0 to 1, creates a pixel which ranges from fully
     * transparent to an opaque red, passing by green, blueish-green, blue,
     * yellow and orange.
     * 
     * @author Artur Antunes
     * @author Raquel Ribeiro
     * @author Vânia Vieira
     * @param pop Decimal value [0-1]
     */
    public Pixel(Double pop){
        if (pop < 1.0/5){
            this.A = 5 * pop;
            this.R = 0;
            this.G = 0;
            this.B = new Double(5 * pop * 0xFF).intValue();
        }else if (pop < 2.0/5){
            this.A = 1.0;
            this.R = 0;
            this.G = new Double(5 * (pop - 1.0/5) * 0xFF).intValue();
            this.B = 0xFF;
        }else if (pop < 3.0/5){
            this.A = 1.0;
            this.R = 0;
            this.G = 0xFF;
            this.B = new Double(-5 * (pop - 3.0/5) * 0xFF).intValue();
        }else if (pop < 4.0/5){
            this.A = 1.0;
            this.R = new Double(5 * (pop - 3.0/5) * 0xFF).intValue();
            this.G = 0xFF;
            this.B = 0;
        }else {
            this.A = 1.0;
            this.R = 0xFF;
            this.G = new Double(-5 * (pop - 1.0) * 0xFF).intValue();
            this.B = 0;
        }
    }

    /**
     *
     * @param pixel
     */
    public void set(Pixel pixel){
        this.A = pixel.A;
        this.R = pixel.R;
        this.G = pixel.G;
        this.B = pixel.B;
    }
    
    /**
     * Overlaps this pixel with another pixel.
     * 
     * @author Artur Antunes
     * @param fg Foreground pixel
     */
    public void overlap(Pixel fg){
        if (fg.A.equals(0))
            return;
        
        if (fg.A >= 1.0){
            this.A = 1.0;
            this.R = fg.R;
            this.G = fg.G;
            this.B = fg.B;
        }
        
        Pixel bg = new Pixel(this.toARGB());
        this.A = fg.A + bg.A - fg.A*bg.A;
        this.R = new Double(fg.A*fg.R + bg.A*bg.R - fg.A*bg.A*bg.R).intValue();
        this.G = new Double(fg.A*fg.G + bg.A*bg.G - fg.A*bg.A*bg.G).intValue();
        this.B = new Double(fg.A*fg.B + bg.A*bg.B - fg.A*bg.A*bg.B).intValue();
    }
    
    /**
     *
     * @param bg
     */
    public void underlap(Pixel bg){
        Pixel copy = new Pixel(bg);
        copy.overlap(this);
        this.set(copy);
    }
    
    /**
     *
     * @param bg
     * @param racio
     */
    public void underlap(Pixel bg, Double racio){
        Pixel copy = new Pixel(bg);
        this.A *= racio;
        copy.overlap(this);
        this.set(copy);
    }

    /**
     * Reduce the transparency of an image and make it darker
     * @author Artur Antunes
     * @param factor has the purpose of decrease A and increase the RGB
     */
    public void darken(Double factor){
        if (factor > 1)
            return;
        
        this.A = factor * this.A;
        this.R = new Double(factor * this.R).intValue();
        this.G = new Double(factor * this.G).intValue();
        this.B = new Double(factor * this.B).intValue();
    }
    
    
    /**
     * Gets the Pixel's argb integer value (TYPE_INT_ARGB).
     * 
     * @author Artur Antunes
     * @return Pixel's argb integer value
     */
    public Integer toARGB(){
        Integer argb = 0;
        argb |= new Double(this.A * 0xFF).intValue();
        argb <<= 8;
        argb |= this.R;
        argb <<= 8;
        argb |= this.G;
        argb <<= 8;
        argb |= this.B;
        return argb;
    }
    
    
 
}
