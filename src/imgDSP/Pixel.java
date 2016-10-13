/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imgDSP;

/**
 *
 * @author Raquel
 */
public class Pixel {
    private Character R;
    private Character G;
    private Character B;
    private Character A;

    public Pixel(int ARGB) {
        A = (char) ((ARGB >> 24) & 0xFF);
        R = (char) ((ARGB >> 16) & 0xFF);
        G = (char) ((ARGB >> 8) & 0xFF);
        B = (char) (ARGB & 0xFF);
    }

    
    
    public void setR(Character R) {
        this.R = R;
    }

    public void setG(Character G) {
        this.G = G;
    }

    public void setB(Character B) {
        this.B = B;
    }

    public void setA(Character A) {
        this.A = A;
    }

    public Character getR() {
        return R;
    }

    public Character getG() {
        return G;
    }

    public Character getB() {
        return B;
    }

    public Character getA() {
        return A;
    }
    
    
    
    
}
