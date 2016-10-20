/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.dsp;

/**
 *
 * @author Raquel
 */
public class Pixel {
    private Integer R;
    private Integer G;
    private Integer B;
    private Double A;

    public Pixel(){
        R = G = B = 0;
        A = 1.0;
    }
    
    public Pixel(Integer argb) {
        Integer a = (argb >> 24) & 0xFF;
        R = (argb >> 16) & 0xFF;
        G = (argb >> 8) & 0xFF;
        B = argb & 0xFF;
        A = a * 1.0/0xFF;
    }
    
    public Pixel(Double pop){
        if (pop < 1/3){
            this.A = 3 * pop;
            this.R = 0;
            this.G = 0;
            this.B = new Double(3 * pop * 0xFF).intValue();
        }else if (pop < 2/3){
            this.A = 1.0;
            this.R = 0;
            this.G = new Double(3 * (pop - 1/3) * 0xFF).intValue();
            this.B = new Double(-3 * (pop - 2/3) * 0xFF).intValue();
        }else {
            this.A = 1.0;
            this.R = new Double(3 * (pop - 2/3) * 0xFF).intValue();
            this.G = new Double(-3 * (pop - 1) * 0xFF).intValue();
            this.B = 0;
        }
    }

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
    
    /*
    public Pixel add(Pixel other){
        Pixel res = new Pixel();
        res.R = new Double(this.A*this.R + other.A*other.R).intValue();
        res.G = new Double(this.A*this.G + other.A*other.G).intValue();
        res.B = new Double(this.A*this.B + other.A*other.B).intValue();
        if (res.R > 0xFF) res.R = 0xFF;
        if (res.G > 0xFF) res.G = 0xFF;
        if (res.B > 0xFF) res.B = 0xFF;
        return res;
    }
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
