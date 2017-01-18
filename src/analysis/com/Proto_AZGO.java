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
package analysis.com;

import analysis.bl.Logic;
import tools.*;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The protocol used to comunicate with the AZGO mobile client.
 *
 * @author Artur Antunes
 * @author Rubens Figueiredo
 */
public class Proto_AZGO extends Protocol {

    private static final ConcurrentHashMap<String, Pair<String, PrintWriter>> USERS = new ConcurrentHashMap<>();

    private String currentUser = null;

    private static final Thread REQUESTHANDLER = new Thread() {
        @Override
        public void run() {
            requestHandler();
        }
    };

    /**
     * Simple Constructor. Just calls the superclass' constructor.
     *
     * @param out the uplink channel (PrintWriter)
     * @param in the downlink channel (BufferedReader)
     */
    public Proto_AZGO(PrintWriter out, BufferedReader in) {
        super(out, in);
    }

    @Override
    public String decode(String received) {
        if (!REQUESTHANDLER.isAlive()) {
            REQUESTHANDLER.start();
        }
        String[] tokens = received.split("\\$");
        //System.out.println("Received from AZGO: " + tokens[0]);
        switch (tokens[0]) {
            case "Login":
                return handlerLogin(tokens); //Login$email$session_id
            case "Logout":
                return handlerLogout();
            case "Meet":
                return handlerMeet(tokens);
            case "MeetRequest":
                return handlerMeetRequest(tokens);
            case "Coordinates":
                return handlerCoordinates(tokens);
            case "Friends":
                return handlerFriends(tokens);
            case "Debug":
                return handlerDebug();
            default:
                return received.concat("_OK");
        }
    }

    private String handlerLogin(String[] tokens) {
        if (tokens.length != 4) {
            return " ";
        }

        String email = tokens[2];
        String name = tokens[1];
        String num = tokens[3];

        this.currentUser = Logic.loginUser(name, email, num);

        USERS.put(this.currentUser, new Pair<>(email, this.out));

        return "Login$" + this.currentUser;
    }

    private String handlerLogout() {
        if (this.currentUser == null) {
            System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Logout error: null user");
            return null;
        }
        synchronized (USERS) {
            Pair<String, PrintWriter> deadSession = USERS.get(this.currentUser);
            if (deadSession == null) {
                System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Logout error: no user " + this.currentUser);
                return null;
            }
            Logic.logoutUser(this.currentUser, deadSession.getK());
            USERS.remove(this.currentUser);
        }
        return null;
    }

    private String handlerDebug() {
        Logic.printLOGGEDIN();
        System.out.println("USERS: ");
        synchronized (USERS) {
            USERS.entrySet().stream().forEach((pair) -> {
                System.out.println("\t" + pair.getKey() + " | " + pair.getValue().toString());
            });
        }
        return " ";
    }

    private String handlerMeet(String[] tokens) {
        if (tokens.length != 3 || this.currentUser == null) {
            return " ";
        }

        Pair<String, PrintWriter> rout = USERS.get(this.currentUser);
        if (rout == null) {
            return " ";
        }

        if (rout.getK().equals(tokens[1])) {

        }
        Logic.requetsMeet(rout.getK(), tokens[1], tokens[2]);
        return " ";
    }

    private String handlerMeetRequest(String[] tokens) {
        if (tokens.length != 4 || this.currentUser == null) {
            return " ";
        }
        Pair<String, PrintWriter> rout = USERS.get(this.currentUser);
        if (rout == null) {
            return " ";
        }
        Logic.requetsMeet(tokens[1], rout.getK(), tokens[2], tokens[3]);
        return " ";
    }

    private String handlerCoordinates(String[] tokens) {

        String user = tokens[1];
        Double lat = Double.parseDouble(tokens[2]);
        Double longi = Double.parseDouble(tokens[3]);

        Logic.addLocation(lat, longi);

        return "Coordinates".concat("$").concat("OK");
    }

    private String handlerFriends(String[] tokens) {

        ArrayList<String> lAmigos = new ArrayList<>();

        for (int i = 1; i < tokens.length; i++) {
            lAmigos.add(tokens[i]);
        }

        String envio = Logic.getFriendsInf(lAmigos);

        return envio;
    }

    private static void requestHandler() {

        System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Starting Logic Request Handler");
        for (;;) {
            String request;
            synchronized (Logic.REQUESTS) {
                while ((request = Logic.REQUESTS.poll()) == null) {
                    try {
                        Logic.REQUESTS.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Proto_AZGO.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Processing Request: " + request);

            String splitRequest[] = request.split("#");
            switch (splitRequest[0]) {
                case "KillMe":
                    requestKillMe(splitRequest);
                    break;
                case "MeetRequest":
                    requestMeetRequest(splitRequest);
                    break;
                case "Meet":
                    requestMeet(splitRequest);
                    break;
            }
        }
    }

    private static void requestMeetRequest(String[] tokens) {
        Pair<String, PrintWriter> rout = USERS.get(tokens[1]);

        if (rout == null) {
            System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Meet error: " + tokens[2] + " is null.");
            return;
        }

        String resp = "MeetRequest$" + tokens[2];
        for (int i = 3; i < tokens.length; i++) {
            resp = resp + "$" + tokens[i];
        }

        sendTo(resp, rout.getV());
    }

    private static void requestKillMe(String[] tokens) {
        String uid = tokens[1];
        Pair<String, PrintWriter> rout = USERS.get(uid);

        if (rout == null) {
            return;
        }

        sendTo("KillMe$" + tokens[1], rout.getV());

        rout.getV().flush();
        rout.getV().close();
        USERS.remove(uid);
    }

    private static void requestMeet(String[] tokens) {
        Pair<String, PrintWriter> rout = USERS.get(tokens[1]);

        if (rout == null) {
            return;
        }

        String resp = "Meet$" + tokens[2];
        for (int i = 3; i < tokens.length; i++) {
            resp = resp + "$" + tokens[i];
        }

        sendTo(resp, rout.getV());
    }

    @Override
    public void kill() {
        handlerLogout();
    }
}
