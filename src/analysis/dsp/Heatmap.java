/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.dsp;

/**
 *
 * @author Artur Antunes
 */
public class Heatmap extends Image{
    private static Image background = null;
    
    public Heatmap(Double[][] pop) {
        super(pop, background.getWidth(), background.getHeight());
    }
    
    public void generate(){
        if (null == background)
            return;

        this.underlap(background);
    }

    public static Image getBackground() {
        return background;
    }

    public static void setBackground(Image background) {
        Heatmap.background = background;
    }
    
}
