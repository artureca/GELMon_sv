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
import analysis.db.Locations;
import analysis.dsp.Heatmap;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.*;
import tools.FileSystem;
import java.util.Random;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.media.jai.PerspectiveTransform;
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

    private static PerspectiveTransform toPoint;
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final ConcurrentSkipListSet<String> PROCESSING = new ConcurrentSkipListSet<>();
    private static final ConcurrentHashMap<String, User> LOGGEDIN = new ConcurrentHashMap<>();
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
        Double max = 0.0;

        for (int i = 0; i < Heatmap.getBackground().getWidth(); i++) {
            for (int j = 0; j < Heatmap.getBackground().getHeight(); j++) {
                values[i][j] = 0.0;
            }
        }

        points.stream().forEach(coord -> {
            Point2D point = new Point2D.Double();
            toPoint.transform(coord, point);
            Integer x = (int) Math.round(point.getX());
            Integer y = (int) Math.round(point.getY());
            Smooth(values, Heatmap.getBackground().getWidth(), Heatmap.getBackground().getHeight(), x, y);
        });

        for (int i = 0; i < Heatmap.getBackground().getWidth(); i++) {
            for (int j = 0; j < Heatmap.getBackground().getHeight(); j++) {
                if (max < values[i][j]) {
                    max = values[i][j];
                }
            }
        }

        if (max != 0) {
            for (int i = 0; i < Heatmap.getBackground().getWidth(); i++) {
                for (int j = 0; j < Heatmap.getBackground().getHeight(); j++) {
                    values[i][j] /= max;
                }
            }
        }
        
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
    public static String getHeatmap(Long date1, Long date2) {

        String fileName = MD5.crypt(date1.toString().concat(date2.toString()));
        String folderPath = System.getenv("HOME") + "/public_html/" + imgFolder + "/";
        String filePath = folderPath + fileName + ".png";
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
        String addLogString = fileName + " " + date1.toString() + " " + date2.toString();
        ArrayList<String> addLog = new ArrayList<>();
        addLog.add(addLogString);
        synchronized (LOCK) {
            FileSystem.appendText(folderPath + "heatmaps.log", addLog);
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
        d = d / 1000;
        d = TimeUnit.DAYS.toSeconds(TimeUnit.SECONDS.toDays(d) - 1);

        for (long t = d; t < d + TimeUnit.HOURS.toSeconds(24); t = t + TimeUnit.HOURS.toSeconds(1)) {
            long hour1 = TimeUnit.SECONDS.toHours(t);
            Long date1 = TimeUnit.HOURS.toSeconds(hour1);
            Long date2 = TimeUnit.HOURS.toSeconds((hour1 + 1) % 24);

            String fileName = MD5.crypt(date1.toString().concat(date2.toString()));
            String folderPath = System.getenv("HOME") + "/public_html/" + imgFolder + "/";
            String filePath = folderPath + fileName + ".png";

            Heatmap img = generateHeatmap(t, t + 3540);
            BufferedImage image = img.toBufferedImage();

            String addLogString = fileName + " " + date1.toString() + " " + date2.toString();
            ArrayList<String> addLog = new ArrayList<>();
            addLog.add(addLogString);

            synchronized (LOCK) {
                FileSystem.appendText(folderPath + "heatmaps.log", addLog);
                FileSystem.saveImage(filePath, image);
                PROCESSING.remove(fileName);
                LOCK.notifyAll();
            }
        }
        System.out.println("Finished HEATMAP Hourly");
    }

    private static void generateVideo(Long d) {
        int i = 0;
        d = (d / 1000) - 86400;
        String fileName = MD5.crypt(d.toString());
        String filePath = System.getenv("HOME") + "/public_html/" + vidFolder + "/" + fileName;

        for (long t = d; t < d + 86400 /*40 * 600*/; t = t + 600) {
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

    private static void Smooth(Double[][] res, Integer w, Integer h, Integer x, Integer y) {
        // 13x13 matrix
        long[][] matriz = {
            {1, 2, 3, 4, 6, 7, 7, 7, 6, 4, 3, 2, 1},
            {2, 3, 6, 8, 11, 13, 13, 13, 11, 8, 6, 3, 2},
            {3, 6, 9, 13, 18, 21, 22, 21, 18, 13, 9, 6, 3},
            {4, 8, 13, 20, 26, 30, 32, 30, 26, 20, 13, 8, 4},
            {6, 11, 18, 26, 34, 40, 42, 40, 34, 26, 18, 11, 6},
            {7, 13, 21, 30, 40, 47, 50, 47, 40, 30, 21, 13, 7},
            {7, 13, 22, 32, 42, 50, 53, 50, 42, 32, 22, 13, 7},
            {7, 13, 21, 30, 40, 47, 50, 47, 40, 30, 21, 13, 7},
            {6, 11, 18, 26, 34, 40, 42, 40, 34, 26, 18, 11, 6},
            {4, 8, 13, 20, 26, 30, 32, 30, 26, 20, 13, 8, 4},
            {3, 6, 9, 13, 18, 21, 22, 21, 18, 13, 9, 6, 3},
            {2, 3, 6, 8, 11, 13, 13, 13, 11, 8, 6, 3, 2},
            {1, 2, 3, 4, 6, 7, 7, 7, 6, 4, 3, 2, 1}
        };
        //double total = 73; //73;

        for (int t = 0; t < 13; t++) {
            for (int l = 0; l < 13; l++) {
                Integer newx = x + t - 6;
                Integer newy = y + l - 6;
                if (newx < 0) {
                    newx = 0;
                }
                if (newx > w - 1) {
                    newx = w - 1;
                }
                if (newy < 0) {
                    newy = 0;
                }
                if (newy > h - 1) {
                    newy = h - 1;
                }
                res[newx][newy] += matriz[t][l];
            }
        }

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
                runDaily();
            }
//        }, 24 - TimeUnit.HOURS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS) + 2, 24, TimeUnit.HOURS);
        }, 5, TimeUnit.HOURS.toSeconds(24), TimeUnit.SECONDS);
    }

    /**
     * Gets number of locations in DB between a given interval, with a precision
     * defined by the step
     *
     * @param nmr
     * @param init initial date of the interval
     * @param fin final date of the interval
     * @param step precision of the interval
     * @return
     */
    public static ArrayList<String> getNumberOfLocationsByInterval(ArrayList<Long> nmr, Long init, Long fin, Long step) {

        Long aux = ((fin - init) / step);

        final ConcurrentHashMap<Integer, AtomicInteger> num = new ConcurrentHashMap<>();
        ArrayList<String> ret = new ArrayList<>();

        for (int i = 0; i < aux; i++) {
            num.put(i, new AtomicInteger(0));
//            System.out.print("|");
        }
//        System.out.println();

        nmr.forEach(loc -> {
            Long tmp = (loc - init) / step;
            num.get(tmp.intValue()).incrementAndGet();
//            System.out.print(".");
        });
//        System.out.println();

        num.entrySet().stream().forEachOrdered(entry -> {
//            System.out.print(entry.getKey() + "|" + entry.getValue() + " ");
            ret.add(Integer.toString(entry.getValue().get()));
        });
//        System.out.println();
        return ret;
    }

    private static void generateLog(Long d, Long step) {
        final Long day = d - TimeUnit.DAYS.toSeconds(1);

        final ArrayList<Pair<Point2D.Double, Long>> tudo = new Locations().getFullLocation(day, day + TimeUnit.DAYS.toSeconds(1));
        final ConcurrentHashMap<String, ArrayList<Long>> data = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, ArrayList<String>> newdata = new ConcurrentHashMap<>();
        final ArrayList<String> finaldata = new ArrayList<>();

//        System.out.println("Entry count: " + tudo.size());
        tudo.forEach(location -> {
            String building = getsBuilding(location.getK().getX(), location.getK().getY());
            data.computeIfAbsent(building, k -> new ArrayList<>())
                    .add(location.getV());
        });

//        System.out.println("Data size: " + data.size());
        data.entrySet().forEach(entry -> {
//            System.out.println(entry.getKey() + ": ");
            newdata.put(entry.getKey(), getNumberOfLocationsByInterval(entry.getValue(), day, day + TimeUnit.DAYS.toSeconds(1), step));
        });

//        System.out.println("NewData size: " + newdata.size());
        newdata.entrySet().forEach(entry -> {
            finaldata.add(entry.getKey());
            String tmp = "";
            tmp = entry.getValue().stream().map((value) -> value + ",").reduce(tmp, String::concat).replaceAll(",$", "");
            finaldata.add(tmp);
        });

//        System.out.println("FinalData size: " + finaldata.size());
//        System.out.println("Finished " + TimeUnit.SECONDS.toMinutes(step) + ".log:");
//        finaldata.stream().forEachOrdered(text -> {
//            System.out.println("\t" + text);
//        });
        String graphPath = System.getenv("HOME") + "/public_html/" + graphFolder + "/" + day + "/";

        FileSystem.saveText(graphPath + TimeUnit.SECONDS.toMinutes(step) + ".log", finaldata);
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
        Boolean check = new Users().vfLogin(name, email, Integer.valueOf(num));
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
    private static String getsBuilding(double latitude, double longitude) {

        if ((41.177965 < latitude) && (latitude < 41.178443) && (longitude > -8.594829) && (longitude < -8.594320)) {
            return "mecanica";
        } else if ((41.177968 < latitude) && (latitude < 41.178463) && (longitude > -8.595530) && (longitude < -8.594838)) {
            return "eletro";
        } else if ((41.177285 < latitude) && (latitude < 41.177627) && (longitude > -8.594891) && (longitude < -8.594468)) {
            return "biblioteca";
        } else if ((41.177162 < latitude) && (latitude < 41.177965) && (longitude > -8.597400) && (longitude < -8.595041)) {
            return "bloco";
        } else if ((41.177847 < latitude) && (latitude < 41.178405) && (longitude > -8.598041) && (longitude < -8.597439)) {
            return "info";
        } else {
            return "other";
        }

    }

    public static void addLocation(double latitude, double longitude) {
        //new Locations().setLocation(latitude, longitude, "0", System.currentTimeMillis());
    }

    public static void runDaily() {
//        long currentDay = TimeUnit.DAYS.toSeconds(TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()));
        long currentDay = TimeUnit.DAYS.toSeconds(TimeUnit.MILLISECONDS.toDays(1483492050000L));

        System.out.println("Current Day: " + currentDay);

        generateLog(currentDay, TimeUnit.MINUTES.toSeconds(15));
        generateLog(currentDay, TimeUnit.MINUTES.toSeconds(30));
        generateLog(currentDay, TimeUnit.MINUTES.toSeconds(60));

        generateVideo(currentDay);
        hourlyHeatmap(currentDay);
        System.out.println("Done: " + currentDay);
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
