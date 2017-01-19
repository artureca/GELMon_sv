/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.dsp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import tools.Coord;
import tools.FileSystem;

/**
 *
 * @author Artur Antunes
 */
public class Heatmap extends Image{
    private static Image background = null;
    
    public Heatmap(Double[][] pop) {
        super(pop, background.getWidth(), background.getHeight());
        //super(pop, 1000, 1000);
    }

    public Heatmap(ConcurrentHashMap<Coord, AtomicLong> pop) {
        super(pop,  background.getWidth(), background.getHeight());
    }
    
    
    public void generate(){
        if (null == background)
            return;

        this.underlap(background);
    }

    public static Image getBackground() {
        return background;
    }
    
    public static void setup(){
        background = new Image(FileSystem.loadImage(FileSystem.getConfig("HEATMAP.background")));
    }
    
}
