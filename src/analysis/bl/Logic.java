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

package analysis.bl;

import analysis.db.*;
import analysis.dsp.Heatmap;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import tools.FileSystem;
import tools.Pair;


/**
 * A class with static methods used to process the clients requests.
 * 
 * @author Artur Antunes
 * @author Rubens Figueiredo
 */
public class Logic {
    
    
    private static String imgFolder;
    private static String graphFolder;
    private static String vidFolder;
    private static String url;
    private static final Double[][] TMATRIX= new Double[2][2];
    
    private static final ConcurrentSkipListSet<String> PROCESSING = new ConcurrentSkipListSet<>();
    private static final Object LOCK = new Object();
    
    private static Boolean checkFile(String fileName,String filePath){
        synchronized(LOCK){
            while (true){
                if (FileSystem.fileExists(filePath)) 
                    return true;
                
                if (PROCESSING.contains(fileName)) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException ex) {}
                }else{
                    PROCESSING.add(fileName);
                    return false;
                }
            }
        }
    }
    
    private static Heatmap generateHeatmap(Long date1, Long date2){
        
        ArrayList<Pair<Double,Double>> points = new Locations().getLocation(date1, date2);
        
        Double[][] values = new Double
            [Heatmap.getBackground().getWidth()]
            [Heatmap.getBackground().getHeight()];
        
        points.forEach((point) -> {
            Pair<Double,Double> tmp = new Pair<>(point);
            point.setK(TMATRIX[0][0]*tmp.getK() + TMATRIX[0][1]*tmp.getV());
            point.setV(TMATRIX[1][0]*tmp.getK() + TMATRIX[1][1]*tmp.getV());
            
            if (point.getK() < 0) 
                point.setK(0.0);
            if (point.getV() < 0) 
                point.setV(0.0);
            if (point.getK() > Heatmap.getBackground().getWidth() - 1) 
                point.setK(Heatmap.getBackground().getWidth()-1.0);
            if (point.getK() > Heatmap.getBackground().getWidth() - 1) 
                point.setK(Heatmap.getBackground().getWidth()-1.0);
            
            values[point.getK().intValue()][point.getV().intValue()] += 1/points.size();
        });
        
        Heatmap heatmap = new Heatmap(values);
        heatmap.generate();
        return heatmap;
    }
    
    /**
     * Gets the reqeusted heatmap path, creating it if it doesn't exist.
     * 
     * @author Rubens Figueiredo
     * @param date1
     * @param date2
     * @return
     */
    public static String getHeatmap(Long date1, Long date2){
        
        String fileName = MD5.crypt(date1.toString().concat(date2.toString()));
        String filePath = System.getenv("$HOME") + "/public_html/" + imgFolder + "/" + fileName + ".png";
        String fileURL = url + "/" + imgFolder + "/" + fileName + ".png";
        
        if (checkFile(fileName,filePath)) 
            return fileURL;
        
        // wait if being processed
        
        if (null == Heatmap.getBackground())
            return "ERROR : heatmap background null!!!";
        
        Heatmap heatmap = generateHeatmap(date1, date2);
        BufferedImage image = heatmap.toBufferedImage();
        
        //FileSystem.saveImage(filePath, image);
        
        // should work, not sure. Comment if problems arise! ///////////////////
        synchronized(LOCK){
            FileSystem.saveImage(filePath, image);
            PROCESSING.remove(fileName);
            LOCK.notifyAll();
        }
        ////////////////////////////////////////////////////////////////////////
        
        return fileURL;
    }
    
    private static Double[][] Smooth(Double[][] data,Integer w, Integer h){
        double[][] matriz= {
            {1,2,3,2,1},
            {2,3,4,3,2},
            {3,4,5,4,3},
            {2,3,4,3,2},
            {1,2,3,2,1}
        };
        double total = 73;
    
        Double[][] res= new Double[w][h];

        for (int i=0;i<w;i++){
            for (int j=0;j<h;j++){ 
                res[i][j]=0.0;
                for (int t=0;t<5;t++)
                    for (int l=0;l<5;l++)
                      res[i][j] += getMirroredValue(data, i+t-2, j+t-2, w, h) * matriz[t][l];
                res[i][j] /= total;
            }
        }
        return res;
    }
    
    private static Double getMirroredValue(Double[][]data, Integer i, Integer j, Integer w, Integer h){
        if (i<0)
            return getMirroredValue(data, i+1, j, w, h);
        if (j<0)
            return getMirroredValue(data, i, j+1, w, h);
        if (i>=w)
            return getMirroredValue(data, i-1, j, w, h);
        if (j>=h)
            return getMirroredValue(data, i, j-1, w, h);
        
        return data[i][j];
    }
    
    public static void setup() {
        imgFolder = FileSystem.getConfig("LOGIC.imgdir");
        graphFolder = FileSystem.getConfig("LOGIC.graphdir");
        vidFolder = FileSystem.getConfig("LOGIC.videodir");
        url = FileSystem.getConfig("LOGIC.url");
        
        Pair<Double,Double> from1, from2;
        Pair<Double,Double> to1, to2;
        
        String point;
        String [] points, p1, p2;
        
        point = FileSystem.getConfig("LOGIC.matrix.point1");
        point = point.replace('(', ' ');
        point = point.replace(')', ' ');
        points = point.split(">");
        p1 = points[0].split(",");
        p2 = points[1].split(",");
        from1 = new Pair(Double.valueOf(p1[0]),Double.valueOf(p1[1]));
        to1 = new Pair(Double.valueOf(p2[0]),Double.valueOf(p2[1]));
        
        point = FileSystem.getConfig("LOGIC.matrix.point2");
        point = point.replace('(', ' ');
        point = point.replace(')', ' ');
        points = point.split(">");
        p1 = points[0].split(",");
        p2 = points[1].split(",");
        from2 = new Pair(Double.valueOf(p1[0]),Double.valueOf(p1[1]));
        to2 = new Pair(Double.valueOf(p2[0]),Double.valueOf(p2[1]));
        
        TMATRIX[1][1] = (from1.getK()*to2.getV() - to2.getK()*from1.getV())/(from1.getK()*from2.getV() - from2.getK()*from1.getV());
        TMATRIX[0][1] = (from1.getK()*to1.getV() - to1.getK()*from1.getV())/(from1.getK()*from2.getV() - from2.getK()*from1.getV());
        TMATRIX[1][0] = (to2.getK() - TMATRIX[1][1]*from2.getK())/from1.getK();
        TMATRIX[0][0] = (to1.getK() - TMATRIX[0][1]*from2.getK())/from1.getK();
        
        Heatmap.setup();
        MySQL.setup();
    }
    
    public static int[] getNumberOfLocationsByHour (){
        
        int i, auxi;
        Long aux;
        int[] num = new int[24]; //Array com pessoas/hora
        Timestamp la, iniciots, fimts; //Variavel para converter string para timestamp
        
        String inicio = "2016-03-01 00:00:00"; //Data inicio para pesquisa(quary) na DB
        iniciots = Timestamp.valueOf(inicio);
        Long inicionum = iniciots.getTime()/1000; //Converte timestamp para inteiro e elimina 0s extra
        //System.out.println(inicionum);
        
        Long finnum = (inicionum+86399)*1000;  //Data final calculado (00:00:00+23:59:59) para pesquisa(quary) na DB
        fimts = new Timestamp(finnum);
        
        //String fin = "2016-03-01 23:59:59"; //Data final para pesquisa(quary) na DB
        //fimts = Timestamp.valueOf(fin);
        //Long finnum = fimts.getTime()/1000; //Converte timestamp para inteiro e elimina 0s extra
        //System.out.println(finnum);
        
        ArrayList<String> all = new ArrayList<String>(); //Inicializacao lista de timestamps(strings)
        ArrayList<Long> nmr = new ArrayList<Long>();    //Inicializacao lista de timestamps(long)
      /*  
       // --------------------------------------------------------------------------------------------
        all.add("2016-03-01 13:15:15");
        all.add("2016-03-01 13:00:00");
        all.add("2016-03-01 00:00:00");
        all.add("2016-03-01 16:00:00");         //Substituir por funcao da base de dados
        all.add("2016-03-01 20:10:15");         //Pode-se aproveitar a lista all na mesma
        all.add("2016-03-01 22:45:00");
        all.add("2016-03-01 08:30:00");
        all.add("2016-03-01 10:15:15");
        all.add("2016-03-01 23:59:59");
      // ---------------------------------------------------------------------------------------------
      */
        
        Locations loc = new Locations();
        //all = loc.getTimeLocation(iniciots, fimts);    //Busca a DB
        
        
        for (i=0; i<all.size(); i++){           //Converte lista de strings para long
            System.out.println(all.get(i));
            la = Timestamp.valueOf(all.get(i));
            nmr.add(la.getTime()/1000);         //Elimina 0s a mais
        }
        
        for (i=0; i<nmr.size(); i++){           //Contagem das pessoas/hora
            //System.out.println(nmr.get(i));
            aux=nmr.get(i)-inicionum;
            aux=aux/3600;
            auxi=aux.intValue();
            num[auxi]++;
        }
        return num;
    }
    
    public static int[] getNumberOfLocationsByDay (){ //Dias da semana
        
        int i, auxi;
        Long aux;
        int[] num = new int[7]; //Array com pessoas/hora
        Timestamp la, iniciots, fimts; //Variavel para converter string para timestamp
        
        String inicio = "2016-03-01 00:00:00"; //Data inicio para pesquisa(quary) na DB
        iniciots = Timestamp.valueOf(inicio);
        Long inicionum = iniciots.getTime()/1000; //Converte timestamp para inteiro e elimina 0s extra
        //System.out.println(inicionum);
        
        Long finnum = (inicionum+604799)*1000;  //Data final calculado (7 dias) para pesquisa(quary) na DB
        fimts = new Timestamp(finnum);
        
        //String fin = "2016-03-01 23:59:59"; //Data final para pesquisa(quary) na DB
        //fimts = Timestamp.valueOf(fin);
        //Long finnum = fimts.getTime()/1000; //Converte timestamp para inteiro e elimina 0s extra
        //System.out.println(finnum);
        
        ArrayList<String> all = new ArrayList<String>(); //Inicializacao lista de timestamps(strings)
        ArrayList<Long> nmr = new ArrayList<Long>();    //Inicializacao lista de timestamps(long)
    
        
        Locations loc = new Locations();
        //all = loc.getTimeLocation(iniciots, fimts);    //Busca a DB
        
        
        for (i=0; i<all.size(); i++){           //Converte lista de strings para long
            System.out.println(all.get(i));
            la = Timestamp.valueOf(all.get(i));
            nmr.add(la.getTime()/1000);         //Elimina 0s a mais
        }
        
        for (i=0; i<nmr.size(); i++){           //Contagem das pessoas/hora
            //System.out.println(nmr.get(i));
            aux=nmr.get(i)-inicionum;
            aux=aux/86400;
            auxi=aux.intValue();
            num[auxi]++;
        }
        return num;
    }
    
    /**
     *Gets number of locations in DB between a given interval, with a precision defined by the step
     * @param init initial date of the interval
     * @param fin final date of the interval
     * @param step precision of the interval
     * @return
     */
    public static String getNumberOfLocationsByInterval (Long init, Long fin, Long step){
        
        String fileName = MD5.crypt(init.toString() + fin.toString() + step.toString()) + ".dat";
        String filePath = System.getenv("$HOME") + "/public_html/" + graphFolder + "/" + fileName ;
        String fileURL = url + "/" + graphFolder + "/" + fileName ;
        
        // should work, not sure. Comment if problems arise! ///////////////////
        if (checkFile(fileName,filePath)) 
            return fileURL;
        ////////////////////////////////////////////////////////////////////////
        
        Long aux = ((fin-init)/step);
        Integer[] num = new Integer[aux.intValue()];
        ArrayList<Long> nmr = new Locations().getTimeLocation(init, fin);    //Busca a DB // BUSCA??? BUSCA??? PROCURA, puta de brasileirada do caralho!!!
        ArrayList<String> ret = new ArrayList<>();
        
        for (Long time : nmr){
            aux = (time - init) / step;
            num[aux.intValue()]++;
        }
        
        for (Integer i : num)
            ret.add(i.toString());
        
        //FileSystem.saveText(filePath, ret);
        
        // should work, not sure. Comment if problems arise! ///////////////////
        synchronized(LOCK){
            FileSystem.saveText(filePath, ret);
            PROCESSING.remove(fileName);
            LOCK.notifyAll();
        }
        ////////////////////////////////////////////////////////////////////////
        
        return fileURL;
    }
    
}
