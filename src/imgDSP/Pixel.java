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
    Character R;
    Character G;
    Character B;
    Character A;

    public Pixel(int ARGB) {
        A = (char) ((ARGB >> 24) & 0xFF);
        R = (char) ((ARGB >> 16) & 0xFF);
        G = (char) ((ARGB >> 8) & 0xFF);
        B = (char) (ARGB & 0xFF);
    }
    
    
    
}
