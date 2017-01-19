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
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.*;
import tools.FileSystem;
import java.util.Random;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.PerspectiveTransform;

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

    private static PerspectiveTransform toPoint;
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final ConcurrentSkipListSet<String> PROCESSING = new ConcurrentSkipListSet<>();
    private static final ConcurrentHashMap<String, User> LOGGEDIN = new ConcurrentHashMap<String, User>();
    private static final Object LOCK = new Object();
    public static final ConcurrentLinkedQueue<String> REQUESTS = new ConcurrentLinkedQueue<>();

    public static final void printLOGGEDIN() {
        System.out.println("LOGGEDIN: ");
        synchronized (LOGGEDIN) {
            LOGGEDIN.entrySet().stream().forEach((pair) -> {
                System.out.println("\t" + pair.getKey() + " | " + pair.getValue().toString());
            });
        }
    }

    private static Boolean checkFile(String fileName, String filePath) {
        synchronized (LOCK) {
            while (true) {
                if (FileSystem.fileExists(filePath)) {
                    return true;
                }

                if (PROCESSING.contains(fileName)) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException ex) {
                    }
                } else {
                    PROCESSING.add(fileName);
                    return false;
                }
            }
        }
    }

    public static String debugMatrix(Double k, Double v) {

        Point2D.Double coord = new Point2D.Double(k, v);

        toPoint.transform(coord, coord);

        if (coord.getX() < 0) {
            coord.setLocation(0, coord.getY());
        }
        if (coord.getY() < 0) {
            coord.setLocation(coord.getX(), 0);
        }
        if (coord.getX() > Heatmap.getBackground().getWidth() - 1) {
            coord.setLocation(Heatmap.getBackground().getWidth() - 1, coord.getY());
        }
        if (coord.getY() > Heatmap.getBackground().getHeight() - 1) {
            coord.setLocation(coord.getX(), Heatmap.getBackground().getHeight() - 1);
        }

        return coord.toString();
    }

    private static Heatmap generateHeatmap(Long date1, Long date2) {

        System.out.println("Generating heatmap");

        ArrayList<Point2D.Double> points = new Locations().getLocation(date1, date2);

        Double[][] values = new Double[Heatmap.getBackground().getWidth()][Heatmap.getBackground().getHeight()];

        for (int i = 0; i < Heatmap.getBackground().getWidth(); i++) {
            for (int j = 0; j < Heatmap.getBackground().getHeight(); j++) {
                values[i][j] = 0.0;
            }
        }

//        points.parallelStream().map((point) -> {
//            Point2D old = new Point2D.Double(point.getX(),point.getY());
//            toPoint.transform(point, point);
//            if (point.getX() == 658 && point.getY() == 191) {
//                System.out.println("( " + old.getX() + " , " + old.getY() + " ) -> ( " + point.getX() + " , " + point.getY() + " )");
//            }
//            return point;
//        }).map((point) -> {
//            if (point.getX() < 0) {
//                point.setLocation(0, point.getY());
//            }
//            return point;
//        }).map((point) -> {
//            if (point.getY() < 0) {
//                point.setLocation(point.getX(), 0);
//            }
//            return point;
//        }).map((point) -> {
//            if (point.getX() > Heatmap.getBackground().getWidth() - 1) {
//                point.setLocation(Heatmap.getBackground().getWidth() - 1, point.getY());
//            }
//            return point;
//        }).map((point) -> {
//            if (point.getY() > Heatmap.getBackground().getHeight() - 1) {
//                point.setLocation(point.getX(), Heatmap.getBackground().getHeight() - 1);
//            }
//            return point;
//        }).forEachOrdered((point) -> {
//            synchronized (values) {
//                values[(int) Math.round(point.getX())][(int) Math.round(point.getY())] += 1.0;
//            }
//        });
        points.parallelStream().map((point) -> {
            Point2D old = new Point2D.Double(point.getX(), point.getY());
            toPoint.transform(point, point);
            if (point.getX() == 658 && point.getY() == 191) {
                System.out.println("( " + old.getX() + " , " + old.getY() + " ) -> ( " + point.getX() + " , " + point.getY() + " )");
            }
            return point;
        }).forEachOrdered((point) -> {
            if (point.getX() > 0
                    && point.getY() > 0
                    && point.getX() < Heatmap.getBackground().getWidth() - 1
                    && point.getY() < Heatmap.getBackground().getHeight() - 1) {

                synchronized (values) {
                    values[(int) Math.round(point.getX())][(int) Math.round(point.getY())] += 1.0;
                }
            }else{
                System.out.println("Image Overflow");
            }
        });

        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                if (values[i][j] >= 100) {
                    values[i][j] = 1.0;
                    System.out.println("Image Unknown Error");
                }
            }
        }

        Heatmap heatmap = new Heatmap(Smooth(values, Heatmap.getBackground().getWidth(), Heatmap.getBackground().getHeight()));
//        Heatmap heatmap = new Heatmap(values);
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
    public static String getHeatmap(Long date1, Long date2) {

        String fileName = MD5.crypt(date1.toString().concat(date2.toString()));
        String filePath = System.getenv("HOME") + "/public_html/" + imgFolder + "/" + fileName + ".png";
        String fileURL = url + "/" + imgFolder + "/" + fileName + ".png";

        if (checkFile(fileName, filePath)) {
            return fileURL;
        }

        // wait if being processed
        if (null == Heatmap.getBackground()) {
            return "ERROR : heatmap background null!!!";
        }

        Heatmap heatmap = generateHeatmap(date1, date2);
        BufferedImage image = heatmap.toBufferedImage();

        //FileSystem.saveImage(filePath, image);
        // should work, not sure. Comment if problems arise! ///////////////////
        synchronized (LOCK) {
            FileSystem.saveImage(filePath, image);
            PROCESSING.remove(fileName);
            LOCK.notifyAll();
        }
        ////////////////////////////////////////////////////////////////////////

        return fileURL;
    }

    public static void requetsMeet(String emailA, String emailB, String room) {

        User user = LOGGEDIN.get(emailB);
        if (user == null) {
            System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Meet error: " + emailB + " not on-line.");
            return;
        }

        addRequest("MeetRequest#" + user.getSessionid() + "#" + emailA + "#" + room);

    }

    private static void addRequest(String req) {
        synchronized (REQUESTS) {
            REQUESTS.add(req);
            REQUESTS.notify();
        }
    }

    public static void requetsMeet(String emailA, String emailB, String room, String resp) {

        User user = LOGGEDIN.get(emailA);
        if (user == null) {
            return;
        }

        addRequest("Meet#" + user.getSessionid() + "#" + emailB + "#" + room + "#" + resp);
    }

    private static void hourlyHeatmap(Long d) {
        d -= TimeUnit.DAYS.toMillis(1);

        for (long t = d; t < d + TimeUnit.HOURS.toMillis(24); t = t + TimeUnit.HOURS.toMillis(1)) {
            long hour1 = TimeUnit.MILLISECONDS.toHours(t);
            Long date1 = TimeUnit.HOURS.toMillis(hour1);
            Long date2 = TimeUnit.HOURS.toMillis((hour1 + 1) % 24);
            String fileName = MD5.crypt(date1.toString().concat(date2.toString()));
            String filePath = System.getenv("HOME") + "/public_html/" + imgFolder + "/" + fileName;
            Heatmap img = generateHeatmap(t, t + 3540);
            BufferedImage image = img.toBufferedImage();
            synchronized (LOCK) {
                FileSystem.saveImage(filePath, image);
                PROCESSING.remove(fileName);
                LOCK.notifyAll();
            }
        }
        System.out.println("Finished HEATMAP Hourly");
    }

    private static void generateVideo(Long d) {
        int i = 0;
        d = (d / 1000) - 84600;
        String fileName = MD5.crypt(d.toString());
        String filePath = System.getenv("HOME") + "/public_html/" + vidFolder + "/" + fileName;

        for (long t = d; t < d + /*84600*/ 5 * 600; t = t + 600) {
            Heatmap img = generateHeatmap(t, t + 1740);
            BufferedImage image = img.toBufferedImage();
            FileSystem.saveImage(filePath + "/" + String.valueOf(i) + ".png", image);
            i++;
        }

        System.out.println("Finished HEATMAP");

        try {
            Runtime.getRuntime().exec("ffmpeg -framerate 24 -i " + filePath + "/%d.png -c:v libx264 -vf fps=24 -pix_fmt yuv420p " + filePath + ".mp4");
            Runtime.getRuntime().exec("rm -f " + filePath + "/*");
            Runtime.getRuntime().exec("rmdir -f " + filePath);
            System.out.println("Finished video");

        } catch (IOException ex) {
            Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Double[][] Smooth(Double[][] data, Integer w, Integer h) {
        // 13x13
        double[][] matriz = {
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 2, 1, 1, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 2, 3, 2, 1, 1, 1, 0, 0},
            {0, 0, 1, 2, 2, 3, 4, 3, 2, 2, 1, 0, 0},
            {0, 1, 1, 2, 3, 4, 5, 4, 3, 2, 1, 1, 0},
            {0, 1, 2, 3, 4, 5, 6, 5, 4, 3, 2, 1, 0},
            {1, 2, 3, 4, 5, 6, 7, 6, 5, 4, 3, 2, 1},
            {0, 1, 2, 3, 4, 5, 6, 5, 4, 3, 2, 1, 0},
            {0, 1, 1, 2, 3, 4, 5, 4, 3, 2, 1, 1, 0},
            {0, 0, 1, 2, 2, 3, 4, 3, 2, 2, 1, 0, 0},
            {0, 0, 1, 1, 1, 2, 3, 2, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 1, 1, 2, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}
        };
        //double total = 73; //73;

        Double max = 0.0;

        Double[][] res = new Double[w][h];

        System.out.println("Smothing heatmap");

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                res[i][j] = 0.0;
//                res[i][j] = getMirroredValue(data, i , j , w, h);
                for (int t = 0; t < 13; t++) {
                    for (int l = 0; l < 13; l++) {
                        res[i][j] += getMirroredValue(data, i + t - 6, j + l - 6, w, h) * matriz[t][l];
                    }
                }
                //res[i][j] /= total;
                if (max < res[i][j]) {
                    max = res[i][j];
                }
            }
        }
        System.out.println("Normalizing heatmap");

        if (max != 0) {
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    res[i][j] /= max;
                }
            }
        }

        return res;
    }

    private static Double getMirroredValue(Double[][] data, Integer i, Integer j, Integer w, Integer h) {
        if (i < 0) {
            return getMirroredValue(data, i + 1, j, w, h);
        }
        if (j < 0) {
            return getMirroredValue(data, i, j + 1, w, h);
        }
        if (i >= w) {
            return getMirroredValue(data, i - 1, j, w, h);
        }
        if (j >= h) {
            return getMirroredValue(data, i, j - 1, w, h);
        }

        return data[i][j];
    }

    public static void setup() {
        imgFolder = FileSystem.getConfig("LOGIC.imgdir");
        graphFolder = FileSystem.getConfig("LOGIC.graphdir");
        vidFolder = FileSystem.getConfig("LOGIC.videodir");
        url = FileSystem.getConfig("LOGIC.url");

        Point2D.Double from1, from2, from3, from4;
        Point2D.Double to1, to2, to3, to4;

        String point;
        String[] points, p1, p2;

        point = FileSystem.getConfig("LOGIC.matrix.point1");
        point = point.replace('(', ' ');
        point = point.replace(')', ' ');
        points = point.split(">");
        p1 = points[0].split(",");
        p2 = points[1].split(",");
        from1 = new Point2D.Double(Double.valueOf(p1[0]), Double.valueOf(p1[1]));
        to1 = new Point2D.Double(Double.valueOf(p2[0]), Double.valueOf(p2[1]));

        point = FileSystem.getConfig("LOGIC.matrix.point2");
        point = point.replace('(', ' ');
        point = point.replace(')', ' ');
        points = point.split(">");
        p1 = points[0].split(",");
        p2 = points[1].split(",");
        from2 = new Point2D.Double(Double.valueOf(p1[0]), Double.valueOf(p1[1]));
        to2 = new Point2D.Double(Double.valueOf(p2[0]), Double.valueOf(p2[1]));

        point = FileSystem.getConfig("LOGIC.matrix.point3");
        point = point.replace('(', ' ');
        point = point.replace(')', ' ');
        points = point.split(">");
        p1 = points[0].split(",");
        p2 = points[1].split(",");
        from3 = new Point2D.Double(Double.valueOf(p1[0]), Double.valueOf(p1[1]));
        to3 = new Point2D.Double(Double.valueOf(p2[0]), Double.valueOf(p2[1]));

        point = FileSystem.getConfig("LOGIC.matrix.point4");
        point = point.replace('(', ' ');
        point = point.replace(')', ' ');
        points = point.split(">");
        p1 = points[0].split(",");
        p2 = points[1].split(",");
        from4 = new Point2D.Double(Double.valueOf(p1[0]), Double.valueOf(p1[1]));
        to4 = new Point2D.Double(Double.valueOf(p2[0]), Double.valueOf(p2[1]));

        toPoint = PerspectiveTransform.getQuadToQuad(
                from1.getX(), from1.getY(),
                from2.getX(), from2.getY(),
                from3.getX(), from3.getY(),
                from4.getX(), from4.getY(),
                to1.getX(), to1.getY(),
                to2.getX(), to2.getY(),
                to3.getX(), to3.getY(),
                to4.getX(), to4.getY()
        );

        Heatmap.setup();
        MySQL.setup();

        // DELAY: 24h - horaActual + 2, executa o runDaily todos os dias as 2h
        SCHEDULER.scheduleAtFixedRate(new Thread() {
            @Override
            public void run() {
                //runDaily();
            }
//        }, 24 - TimeUnit.HOURS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS) + 2, 24, TimeUnit.HOURS);
        }, 5, TimeUnit.HOURS.toHours(24), TimeUnit.SECONDS);
    }

    public static int[] getNumberOfLocationsByHour() {

        int i, auxi;
        Long aux;
        int[] num = new int[24]; //Array com pessoas/hora
        Timestamp la, iniciots, fimts; //Variavel para converter string para timestamp

        String inicio = "2016-03-01 00:00:00"; //Data inicio para pesquisa(quary) na DB
        iniciots = Timestamp.valueOf(inicio);
        Long inicionum = iniciots.getTime() / 1000; //Converte timestamp para inteiro e elimina 0s extra
        //System.out.println(inicionum);

        Long finnum = (inicionum + 86399) * 1000;  //Data final calculado (00:00:00+23:59:59) para pesquisa(quary) na DB
        fimts = new Timestamp(finnum);

        //String fin = "2016-03-01 23:59:59"; //Data final para pesquisa(quary) na DB
        //fimts = Timestamp.valueOf(fin);
        //Long finnum = fimts.getTime()/1000; //Converte timestamp para inteiro e elimina 0s extra
        //System.out.println(finnum);
        ArrayList<String> all = new ArrayList<>(); //Inicializacao lista de timestamps(strings)
        ArrayList<Long> nmr = new ArrayList<>();    //Inicializacao lista de timestamps(long)
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

        for (i = 0; i < all.size(); i++) {           //Converte lista de strings para long
            System.out.println(all.get(i));
            la = Timestamp.valueOf(all.get(i));
            nmr.add(la.getTime() / 1000);         //Elimina 0s a mais
        }

        for (i = 0; i < nmr.size(); i++) {           //Contagem das pessoas/hora
            //System.out.println(nmr.get(i));
            aux = nmr.get(i) - inicionum;
            aux = aux / 3600;
            auxi = aux.intValue();
            num[auxi]++;
        }
        return num;
    }

    public static int[] getNumberOfLocationsByDay() { //Dias da semana

        int i, auxi;
        Long aux;
        int[] num = new int[7]; //Array com pessoas/hora
        Timestamp la, iniciots, fimts; //Variavel para converter string para timestamp

        String inicio = "2016-03-01 00:00:00"; //Data inicio para pesquisa(quary) na DB
        iniciots = Timestamp.valueOf(inicio);
        Long inicionum = iniciots.getTime() / 1000; //Converte timestamp para inteiro e elimina 0s extra
        //System.out.println(inicionum);

        Long finnum = (inicionum + 604799) * 1000;  //Data final calculado (7 dias) para pesquisa(quary) na DB
        fimts = new Timestamp(finnum);

        //String fin = "2016-03-01 23:59:59"; //Data final para pesquisa(quary) na DB
        //fimts = Timestamp.valueOf(fin);
        //Long finnum = fimts.getTime()/1000; //Converte timestamp para inteiro e elimina 0s extra
        //System.out.println(finnum);
        ArrayList<String> all = new ArrayList<>(); //Inicializacao lista de timestamps(strings)
        ArrayList<Long> nmr = new ArrayList<>();    //Inicializacao lista de timestamps(long)

        Locations loc = new Locations();
        //all = loc.getTimeLocation(iniciots, fimts);    //Busca a DB

        for (i = 0; i < all.size(); i++) {           //Converte lista de strings para long
            System.out.println(all.get(i));
            la = Timestamp.valueOf(all.get(i));
            nmr.add(la.getTime() / 1000);         //Elimina 0s a mais
        }

        for (i = 0; i < nmr.size(); i++) {           //Contagem das pessoas/hora
            //System.out.println(nmr.get(i));
            aux = nmr.get(i) - inicionum;
            aux = aux / 86400;
            auxi = aux.intValue();
            num[auxi]++;
        }
        return num;
    }

    /**
     * Gets number of locations in DB between a given interval, with a precision
     * defined by the step
     *
     * @param init initial date of the interval
     * @param fin final date of the interval
     * @param step precision of the interval
     * @return
     */
    public static String getNumberOfLocationsByInterval(Long init, Long fin, Long step) {

        String fileName = MD5.crypt(init.toString() + fin.toString() + step.toString()) + ".dat";
        String filePath = System.getenv("HOME") + "/public_html/" + graphFolder + "/" + fileName;
        String fileURL = url + "/" + graphFolder + "/" + fileName;

        // should work, not sure. Comment if problems arise! ///////////////////
        if (checkFile(fileName, filePath)) {
            return fileURL;
        }
        ////////////////////////////////////////////////////////////////////////

        Long aux = ((fin - init) / step);
        Integer[] num = new Integer[aux.intValue()];

        for (int i = 0; i < aux.intValue(); i++) {
            num[i] = 0;
        }

        ArrayList<Long> nmr = new Locations().getTimeLocation(init, fin);    //Busca a DB // BUSCA??? BUSCA??? PROCURA, puta de brasileirada do caralho!!!
        ArrayList<String> ret = new ArrayList<>();

        for (Long time : nmr) {
            aux = (time - init) / step;
            System.out.println("aux.intValue() = " + aux.intValue());
            num[aux.intValue()]++;
            System.out.println("num[" + aux.intValue() + "] = " + num[aux.intValue()]);
        }

        System.out.println("Fim do primeiro ciclo for");

        for (Integer i : num) {
            ret.add(i.toString());
        }

        //FileSystem.saveText(filePath, ret);
        // should work, not sure. Comment if problems arise! ///////////////////
        synchronized (LOCK) {
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

    public static String loginUser(String name, String email, String num) {
        Random sessionid = new Random();
        Integer inte = sessionid.nextInt();
        String sid = MD5.crypt(inte.toString());
        synchronized (LOGGEDIN) {
            User util = LOGGEDIN.get(email);
            if (util == null) {
                util = new User(name, email, num, sid);
                LOGGEDIN.put(email, util);
            } else {
                addRequest("KillMe#" + util.getSessionid());
                util.setName(name);
                util.setPhoneNumber(num);
                util.setSessionid(sid);
            }
        }
        return sid;
    }

    public static void logoutUser(String sid, String email) {
        synchronized (LOGGEDIN) {
            User util = LOGGEDIN.get(email);
            if (util == null) {
                return;
            }
            if (util.getSessionid().equals(sid)) {
                LOGGEDIN.remove(email);
            }
        }
    }

    /**
     * Determines the building to which a set of coordinates belong to
     *
     * @param latitude
     * @param longitude
     * @return BlocoB, Eletro, Mecanica, Biblioteca, Info, Other
     */
    public static String getsBuilding(double latitude, double longitude) {

        if ((41.177965 < latitude) && (latitude < 41.178443) && (longitude > -8.594829) && (longitude < -8.594320)) {
            return "Mecanica";
        } else if ((41.177968 < latitude) && (latitude < 41.178463) && (longitude > -8.595530) && (longitude < -8.594838)) {
            return "Eletro";
        } else if ((41.177285 < latitude) && (latitude < 41.177627) && (longitude > -8.594891) && (longitude < -8.594468)) {
            return "Biblioteca";
        } else if ((41.177162 < latitude) && (latitude < 41.177965) && (longitude > -8.597400) && (longitude < -8.595041)) {
            return "BlocoB";
        } else if ((41.177847 < latitude) && (latitude < 41.178405) && (longitude > -8.598041) && (longitude < -8.597439)) {
            return "Info";
        } else {
            return "Other";
        }

    }

    public static void addLocation(double latitude, double longitude) {
        //new Locations().setLocation(latitude, longitude, "0", System.currentTimeMillis());
    }

    public static void runDaily() {
        System.out.println("Current Time: " + System.currentTimeMillis());
        generateVideo(System.currentTimeMillis());
        hourlyHeatmap(System.currentTimeMillis());

    }

    public static String getFriendsInf(ArrayList<String> lAmigos) {

        int i, n;

        ArrayList<String> sInfo = new Users().getFriendsInfo(lAmigos); //Acede a DB para buscar info

        for (i = 0; i < sInfo.size(); i++) { //verifica se ha repeticao de dados e elimina dados repetidos
            //System.out.println(sInfo.get(i));
            for (n = i + 1; n < sInfo.size(); n++) {
                if (sInfo.get(i).equals(sInfo.get(n))) {
                    sInfo.remove(n);
                }
            }
        }

        String envio = "Friends$";  //Gera string formatada para return
        User user1;

        for (i = 0; i < sInfo.size(); i++) {
            user1 = LOGGEDIN.get(sInfo.get(i));
            if (user1 != null) {
                if (user1.getName() == null) {
                    envio = envio + " " + "#";
                } else {
                    envio = envio + user1.getName() + "#";
                }
                envio = envio + user1.getEmail() + "#";
                if (user1.getPhoneNumber().equals("0")) {
                    envio = envio + " " + "$";
                } else {
                    envio = envio + user1.getPhoneNumber() + "$";
                }
            }
        }

        return envio;
    }
}
