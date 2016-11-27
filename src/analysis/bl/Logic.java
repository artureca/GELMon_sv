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

import analysis.dsp.Heatmap;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.ArrayList;
import tools.FileSystem;


/**
 * A class with static methods used to process the clients requests.
 * 
 * @author Artur Antunes
 * @author Rubens Figueiredo
 */
public class Logic {
    
    /**
     * Gets the reqeusted heatmap path, creating it if it doesn't exist.
     * 
     * @author Rubens Figueiredo
     * @param date1
     * @param date2
     * @return
     */
    public static String getHeatmap(Long date1, Long date2){
        
        String imagePath = MD5.crypt(date1.toString().concat(date2.toString()));
        if (FileSystem.fileExists("./public_html/images/" + imagePath + ".png")) {
            return "http://paginas.fe.up.pt/~setec16_17/images/" + imagePath + ".png";
        }
        // verify if it already existes in DB
        // if so, return path
        // wait if being processed
        
        if (null == Heatmap.getBackground())
            return "ERROR : heatmap background null!!!";
        
            
        
        Double[][] values = new Double
            [Heatmap.getBackground().getWidth()]
            [Heatmap.getBackground().getHeight()];
        
        
        
        
        for (int i = 0; i < Heatmap.getBackground().getWidth(); i++)
            for (int j = 0; j < Heatmap.getBackground().getHeight(); j++)
                values[i][j] = (i + j) * 1.0/(Heatmap.getBackground().getWidth()+Heatmap.getBackground().getHeight());
           
    
        
        
        // Somehow get the values from the database
        // Convert them from coordenates
        
        Heatmap heatmap = new Heatmap(values);
        heatmap.generate();
        BufferedImage image = heatmap.toBufferedImage();
        
        while(!FileSystem.saveImage("./public_html/images/" + imagePath + ".png", image))
            imagePath = MD5.crypt(imagePath);
        
        // update database
        // wake up waiting threads
        
        return "http://paginas.fe.up.pt/~setec16_17/images/" + imagePath + ".png";
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
        Heatmap.setup();
    }
    
    public static int[] getNumberOfLocationsByHour (){
        
        int i;
        int[] num = new int[24]; //Array com pessoas/hora
        Timestamp la; //Variavel para converter string para timestamp
        
        String inicio = "2016-03-01 00:00:00"; //Data inicio para pesquisa(query) na DB
        la = Timestamp.valueOf(inicio);
        Long inicionum = la.getTime()/1000; //Converte timestamp para inteiro e elimina 0s extra
        //System.out.println(inicionum);
        
        String fin = "2016-03-01 23:59:59"; //Data final para pesquisa(query) na DB
        la = Timestamp.valueOf(fin);
        Long finnum = la.getTime()/1000; //Converte timestamp para inteiro e elimina 0s extra
        //System.out.println(finnum);
        
        ArrayList<String> all = new ArrayList<String>(); //Inicializacao lista de timestamps(strings)
        ArrayList<Long> nmr = new ArrayList<Long>();    //Inicializacao lista de timestamps(long)
        
       // --------------------------------------------------------------------------------------------
        all.add("2016-03-01 13:15:15");
        all.add("2016-03-01 13:00:00");
        all.add("2016-03-01 00:00:00");
        all.add("2016-03-01 16:00:00");         //Substituir por funcao da base de dados
        all.add("2016-03-01 20:10:15");         //Pode-se aproveitar a lista all na mesma
        all.add("2016-03-01 22:45:00");
        all.add("2016-03-01 08:30:00");
        all.add("2016-03-01 10:15:15");
      // ---------------------------------------------------------------------------------------------
        
        
        for (i=0; i<all.size(); i++){           //Converte lista de strings para long
            la = Timestamp.valueOf(all.get(i));
            nmr.add(la.getTime()/1000);         //Elimina 0s a mais
        }
        
        for (i=0; i<nmr.size(); i++){           //Contagem das pessoas/hora
            //System.out.println(nmr.get(i));
            if((nmr.get(i)>=inicionum) && (nmr.get(i)<inicionum+3600))
                num[0]++;
            if((nmr.get(i)>=inicionum+3600) && (nmr.get(i)<inicionum+(3600*2)))
                num[1]++;
            if((nmr.get(i)>=inicionum+(3600*2)) && (nmr.get(i)<inicionum+(3600*3)))
                num[2]++;
            if((nmr.get(i)>=inicionum+(3600*3)) && (nmr.get(i)<inicionum+(3600*4)))
                num[3]++;
            if((nmr.get(i)>=inicionum+(3600*4)) && (nmr.get(i)<inicionum+(3600*5)))
                num[4]++;
            if((nmr.get(i)>=inicionum+(3600*5)) && (nmr.get(i)<inicionum+(3600*6)))
                num[5]++;
            if((nmr.get(i)>=inicionum+(3600*6)) && (nmr.get(i)<inicionum+(3600*7)))
                num[6]++;
            if((nmr.get(i)>=inicionum+(3600*7)) && (nmr.get(i)<inicionum+(3600*8)))
                num[7]++;
            if((nmr.get(i)>=inicionum+(3600*8)) && (nmr.get(i)<inicionum+(3600*9)))
                num[8]++;
            if((nmr.get(i)>=inicionum+(3600*9)) && (nmr.get(i)<inicionum+(3600*10)))
                num[9]++;
            if((nmr.get(i)>=inicionum+(3600*10)) && (nmr.get(i)<inicionum+(3600*11)))
                num[10]++;
            if((nmr.get(i)>=inicionum+(3600*11)) && (nmr.get(i)<inicionum+(3600*12)))
                num[11]++;
            if((nmr.get(i)>=inicionum+(3600*12)) && (nmr.get(i)<inicionum+(3600*13)))
                num[12]++;
            if((nmr.get(i)>=inicionum+(3600*13)) && (nmr.get(i)<inicionum+(3600*14)))
                num[13]++;
            if((nmr.get(i)>=inicionum+(3600*14)) && (nmr.get(i)<inicionum+(3600*15)))
                num[14]++;
            if((nmr.get(i)>=inicionum+(3600*15)) && (nmr.get(i)<inicionum+(3600*16)))
                num[15]++;
            if((nmr.get(i)>=inicionum+(3600*16)) && (nmr.get(i)<inicionum+(3600*17)))
                num[16]++;
            if((nmr.get(i)>=inicionum+(3600*17)) && (nmr.get(i)<inicionum+(3600*18)))
                num[17]++;
            if((nmr.get(i)>=inicionum+(3600*18)) && (nmr.get(i)<inicionum+(3600*19)))
                num[18]++;
            if((nmr.get(i)>=inicionum+(3600*19)) && (nmr.get(i)<inicionum+(3600*20)))
                num[19]++;
            if((nmr.get(i)>=inicionum+(3600*20)) && (nmr.get(i)<inicionum+(3600*21)))
                num[20]++;
            if((nmr.get(i)>=inicionum+(3600*21)) && (nmr.get(i)<inicionum+(3600*22)))
                num[21]++;
            if((nmr.get(i)>=inicionum+(3600*22)) && (nmr.get(i)<inicionum+(3600*23)))
                num[22]++;
            if((nmr.get(i)>=inicionum+(3600*23)) && (nmr.get(i)<inicionum+(3600*24)))
                num[23]++;
        }
        
        //Timestamp la = Timestamp.valueOf(test);
        /*for(i=0; i<24; i++){
           System.out.println(i + ":" + num[i]);
        }*/
        //System.out.println(la.getTime()/1000);
        return num;
    }
    
}
