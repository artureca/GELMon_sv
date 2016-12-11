///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
package analysis.dsp;

import java.util.ArrayList;

/**
 *
 * @Artur Antunes
 * @author Raquel Ribeiro
 * @author Eugénio Carvalhido
 */
public class Video { 
    private ArrayList<Heatmap> images;
    private long d;

    public Video(long d) {
        this.images=new ArrayList();
        this.d=d;
    }
    
    public Boolean getHeatmaps(){
        for (long t=d; t<d+84600;t=t+600){ 
        } 
    }
    
    private Heatmap getHeatmap(long i,long f){
    }
}
