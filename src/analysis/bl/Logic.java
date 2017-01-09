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
import java.util.concurrent.*;
import java.util.HashMap;
import tools.FileSystem;
import tools.Pair;
import java.util.Random;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

        
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
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final ConcurrentSkipListSet<String> PROCESSING = new ConcurrentSkipListSet<>();
    private static final HashMap<String, User> LOGGEDIN = new HashMap<String, User>();
    private static final Object LOCK = new Object();
    public static final ConcurrentLinkedQueue<String> REQUESTS = new ConcurrentLinkedQueue<>();
    
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
    
    public static String debugMatrix (Double k, Double v){
        Pair<Double,Double> tmp = new Pair<>(k,v);
        Pair<Double,Double> point = new Pair<>(0.0, 0.0);
        point.setK(TMATRIX[0][0]*tmp.getK() + TMATRIX[0][1]*tmp.getV());
        point.setV(TMATRIX[1][0]*tmp.getK() + TMATRIX[1][1]*tmp.getV());

        if (point.getK() < 0) 
            point.setK(0.0);
        if (point.getV() < 0) 
            point.setV(0.0);
        if (point.getK() > Heatmap.getBackground().getWidth() - 1) 
            point.setK(Heatmap.getBackground().getWidth()-1.0);
        if (point.getV() > Heatmap.getBackground().getHeight() - 1) 
            point.setV(Heatmap.getBackground().getHeight()-1.0);
        
        return point.toString();
    }
    
    private static Heatmap generateHeatmap(Long date1, Long date2){
        
        System.out.println("Generating heatmap");
        
        ArrayList<Pair<Double,Double>> points = new Locations().getLocation(date1, date2);
        
        Double[][] values = new Double
            [Heatmap.getBackground().getWidth()]
            [Heatmap.getBackground().getHeight()];
        Double max = 0.0;
        
        for (int i = 0; i < Heatmap.getBackground().getWidth(); i++)
            for (int j = 0; j < Heatmap.getBackground().getHeight(); j++)
                values[i][j] = 0.0;
        
        for (Pair<Double,Double> point : points){
            Pair<Double,Double> tmp = new Pair<>(point);
            point.setK(TMATRIX[0][0]*tmp.getK() + TMATRIX[0][1]*tmp.getV());
            point.setV(TMATRIX[1][0]*tmp.getK() + TMATRIX[1][1]*tmp.getV());
            
            if (point.getK() < 0) 
                point.setK(0.0);
            if (point.getV() < 0) 
                point.setV(0.0);
            if (point.getK() > Heatmap.getBackground().getWidth() - 1) 
                point.setK(Heatmap.getBackground().getWidth()-1.0);
            if (point.getV() > Heatmap.getBackground().getHeight() - 1) 
                point.setV(Heatmap.getBackground().getHeight()-1.0);
                        
            if (max < values[point.getK().intValue()][point.getV().intValue()]++)
               max = values[point.getK().intValue()][point.getV().intValue()];
            
        }
        if (max != 0)
            for (int i = 0; i < Heatmap.getBackground().getWidth(); i++)
                for (int j = 0; j < Heatmap.getBackground().getHeight(); j++)
                    values[i][j] /= max;
                
        Heatmap heatmap = new Heatmap(Smooth(values,Heatmap.getBackground().getWidth(),Heatmap.getBackground().getHeight()));
        //Heatmap heatmap = new Heatmap(values);
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
        String filePath = System.getenv("HOME") + "/public_html/" + imgFolder + "/" + fileName + ".png";
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

    public static void requetsMeet(String origin, String target) {
        
        User user = LOGGEDIN.get(target);
        if (user == null)
            return;
                
        REQUESTS.add("MeetRequest#" + target + "#" + origin);
        
    }
    
    public static void requetsMeet(String target, String origin, Boolean resp) {
        
        User user = LOGGEDIN.get(target);
        if (user == null)
            return ;
        
        if(!resp){
            REQUESTS.add("Meet#" + origin + "#" + target + "#FAIL");
            return;
        }
        
        Double lat = 0.0; // get target coordinates
        Double lon = 0.0; // from the database
        
        REQUESTS.add("Meet#" + origin + "#" + target + "#OK#" + lat + "#" + lon);
    }

    private static void generateVideo(Long d){
        int i=0;
        d=d/1000;
        String fileName = MD5.crypt(d.toString());
        String filePath = System.getenv("HOME") + "/public_html/" + vidFolder + "/" + fileName;
        
        for (long t=d; t<d+84600;t=t+600){ 
            Heatmap img = generateHeatmap(t,t+1740);
            BufferedImage image = img.toBufferedImage();
            FileSystem.saveImage(filePath + "/" + String.valueOf(i) + ".png", image);
            i++;
        }
        
        try {
            Runtime.getRuntime().exec("ffmpeg -framerate 24 -i " + filePath + "/%d.png -c:v libx264 -vf fps=24 -pix_fmt yuv420p " + filePath +".mp4");
            Runtime.getRuntime().exec("rm -f " + filePath + "/*");
            Runtime.getRuntime().exec("rmdir -f " + filePath);
        } catch (IOException ex) {
            Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
        }
 }

    private static Double[][] Smooth(Double[][] data,Integer w, Integer h){
        double[][] matriz= {
            {0,0,0,0,1,0,0,0,0},
            {0,0,1,1,2,1,1,0,0},
            {0,1,1,2,3,2,1,1,0},
            {0,1,2,3,4,3,2,1,0},
            {1,2,3,4,5,4,3,2,1},
            {0,1,2,3,4,3,2,1,0},
            {0,1,1,2,3,2,1,1,0},
            {0,0,1,1,2,1,1,0,0},
            {0,0,0,0,1,0,0,0,0}
        };
        //double total = 73; //73;
    
        Double max = 0.0;
        
        Double[][] res= new Double[w][h];

        System.out.println("Smothing heatmap");
        
        for (int i=0;i<w;i++){
            for (int j=0;j<h;j++){ 
                res[i][j]=0.0;
                for (int t=0;t<9;t++)
                    for (int l=0;l<9;l++)
                      res[i][j] += getMirroredValue(data, i+t-4, j+l-4, w, h) * matriz[t][l];
                //res[i][j] /= total;
                if (max < res[i][j])
                    max = res[i][j];
            }
        }
        
        System.out.println("Normalizing heatmap");
        
        for (int i=0;i<w;i++){
            for (int j=0;j<h;j++){ 
                res[i][j] /= max;
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
                
        TMATRIX[1][1] = (from1.getK()*to2.getV() - to1.getV()*from2.getK())/(from1.getK()*from2.getV() - from1.getV()*from2.getK());
        TMATRIX[0][1] = (from1.getK()*to2.getK() - to1.getK()*from2.getK())/(from1.getK()*from2.getV() - from1.getV()*from2.getK());
        TMATRIX[1][0] = (to1.getV() - TMATRIX[1][1]*from1.getV())/from1.getK();
        TMATRIX[0][0] = (to1.getK() - TMATRIX[0][1]*from1.getV())/from1.getK();
        
        System.out.println();
        System.out.println(from1.toString() + ">" + to1.toString());
        System.out.println(from2.toString() + ">" + to2.toString());
        System.out.println(TMATRIX[0][0] + "\t | \t" + TMATRIX[0][1]);
        System.out.println(TMATRIX[1][0] + "\t | \t" + TMATRIX[1][1]);
        
        Heatmap.setup();
        MySQL.setup();
        
        // DELAY: 24h - horaActual + 2, executa o runDaily todos os dias as 2h
        SCHEDULER.scheduleAtFixedRate(new Thread(){
            @Override
            public void run(){
                runDaily();
            }
//        }, 24 - TimeUnit.HOURS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS) + 2, 24, TimeUnit.HOURS);
        }, 5, 7200, TimeUnit.SECONDS);
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
        String filePath = System.getenv("HOME") + "/public_html/" + graphFolder + "/" + fileName ;
        String fileURL = url + "/" + graphFolder + "/" + fileName ;
        
        // should work, not sure. Comment if problems arise! ///////////////////
        if (checkFile(fileName,filePath)) 
            return fileURL;
        ////////////////////////////////////////////////////////////////////////
        
        Long aux = ((fin-init)/step);
        Integer[] num = new Integer[aux.intValue()];
        
        for (int i = 0; i < aux.intValue(); i++)
            num[i] = 0;
        
        ArrayList<Long> nmr = new Locations().getTimeLocation(init, fin);    //Busca a DB // BUSCA??? BUSCA??? PROCURA, puta de brasileirada do caralho!!!
        ArrayList<String> ret = new ArrayList<>();
        
        for (Long time : nmr){
            aux = (time - init) / step;
            System.out.println("aux.intValue() = "+aux.intValue());
            num[aux.intValue()]++;
            System.out.println("num["+aux.intValue()+"] = "+num[aux.intValue()]);
        }
        
        System.out.println("Fim do primeiro ciclo for");
        
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
//        (41.177628,-8.597396)>(182.0,351.0)
//        (41.178021,-8.864874)>(844.0,205.0)
//        1459.2030922137008       |      -1454.7693321062093
//        1446.1753940042763       |      -1425.6652207026052

        
        return fileURL;
    }


    public String loginUser(String name, String email){

        Random sessionid = new Random();
        
        Integer inte = sessionid.nextInt();
        
        String ret= MD5.crypt(inte.toString());
        
        Users cenas = new Users();
        cenas.vfLogin(name, email, ret);

        return ret;
    }
    
    /**
     * Determines the building to which a set of coordinates belong to
     * @param latitude
     * @param longitude
     * @return BlocoB, Eletro, Mecanica, Biblioteca, Info, Other
     */
    public static String getsBuilding(double latitude, double longitude){
                
        if((41.177965<latitude)&&(latitude<41.179073)&&(longitude>-8.594829)&&(longitude<-8.594320)) {
            return "Mecanica";
        } else if((41.177968<latitude)&&(latitude<41.178830)&&(longitude>-8.595530)&&(longitude<-8.594838)) {
            return "Eletro";
        } else if((41.177285<latitude)&&(latitude<41.177627)&&(longitude>-8.594891)&&(longitude<-8.594468)) {
            return "Biblioteca";
        } else if((41.177162<latitude)&&(latitude<41.177965)&&(longitude>-8.597400)&&(longitude<-8.595041)) {
            return "BlocoB";
        } else if((41.177847<latitude)&&(latitude<41.178405)&&(longitude>-8.598041)&&(longitude<-8.597439)) {
            return "Info";
        } else return "Other";
        
    }
    
    public static void addLocation(double latitude, double longitude){
        new Locations().setLocation(latitude, longitude, "0", System.currentTimeMillis());
    }

    public static void runDaily(){
        System.out.println("Current Time: " + System.currentTimeMillis());
        generateVideo(System.currentTimeMillis());
    }
    
    public static String getFriendsInf (ArrayList<String> lAmigos){
        
        int i,n;
        
        ArrayList<String> lAmigosTemp = new ArrayList<String>();
        lAmigosTemp=lAmigos;
        
        for(i=0;i<lAmigosTemp.size();i++){ //verifica se ha repeticao de dados e elimina dados repetidos
            //System.out.println(lAmigosTemp.get(i));
            for(n=i+1;n<lAmigosTemp.size();n++){
                if (lAmigosTemp.get(i).equals(lAmigosTemp.get(n)))
                    lAmigosTemp.remove(n);
            }
        }
        
        ArrayList<String> sInfo = new Users().getFriendsInfo(lAmigosTemp); //Acede a DB para buscar info
        
        String envio="Friends$";  //Gera string formatada para return
        int sep=2;
        for(i=0;i<sInfo.size();i++){
            if (i==sep){
                if ((sInfo.get(i)==null)||(sInfo.get(i).equals("0")))
                    envio = envio + " " + "$"; 
                else envio = envio + sInfo.get(i) + "$";
                sep=sep+3;
            }
            else {
                if ((sInfo.get(i)==null)||(sInfo.get(i).equals("0")))
                    envio = envio + " " + "#";
                else envio = envio + sInfo.get(i) + "#";
            }
        }
        
        return envio;
    }
}
