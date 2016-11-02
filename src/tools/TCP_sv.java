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

package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a deamon TCP server. This server implements the Daemon
 * interface present in the tools package, it's made to work with any class that
 * extends the class Protocol also present in the tools package.
 * 
 * 
 * @author Artur Antunes
 * @param <T> Comunication protocol that extends the Protocol abtract class
 */
@SuppressWarnings("CallToPrintStackTrace")
public class TCP_sv <T extends Protocol> implements Daemon{
    private final ConcurrentHashMap<Socket, T> connected= new ConcurrentHashMap<>();    
    private final Integer port;
    private Boolean listening ;
    private ServerSocket serverSocket;
    private final Class<T> clazz;
    private final String label;

    /**
     * A Simple constructor.
     * 
     * @author Artur Antunes
     * @param clazz The extended Protocol class
     * @param port The port for the server to listen
     */
    public TCP_sv(Class<T> clazz, Integer port) {
        this.port = port;
        this.clazz = clazz;
        this.label = null;
    }
    
    /**
     * A Simple constructor with a description.\
     * 
     * @author Artur Antunes
     * @param clazz The extended Protocol class
     * @param port The port for the server to listen
     * @param label A breif description of the server.
     */
    public TCP_sv(Class<T> clazz, Integer port, String label) {
        this.port = port;
        this.clazz = clazz;
        this.label = label;
    }

    private T newClient(PrintWriter out, BufferedReader in) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Class[] cArg = new Class[2];
        cArg[0] = PrintWriter.class;
        cArg[1] = BufferedReader.class;
        return clazz.getConstructor(cArg).newInstance(out,in);
    }
        
    @Override
    public void start() throws IOException{
       
        listening = true;
        
        serverSocket = new ServerSocket(this.port);

        new Thread(){
            @Override
            public void run(){
                addClients();
            }
        }.start();
        
        new Thread(){
            @Override
            public void run(){
                killClients();
            }
        }.start();
        
        if (label != null)
            System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Started: " + this.label);
    }
 
    @Override
    @SuppressWarnings("empty-statement")
    public void stop() {
        
        listening = false;
        
        while(!connected.isEmpty());
        
        try {
            serverSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        if (label != null)
            System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Stopped: " + this.label);
    }

    private void addClients(){
        while(listening){
            Socket sock;
            T proto;
            PrintWriter out;
            BufferedReader in;
            
            try {
                sock = serverSocket.accept();
                out = new PrintWriter(sock.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            
            } catch (IOException ex) {
                System.err.println("Could not connect new client!");
                ex.printStackTrace();
                continue;
            }

            try {
                proto = newClient(out, in);
                proto.start();
                connected.put(sock, proto);
                System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | + Client: "+sock.toString()+" | "+proto.toString());

            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(TCP_sv.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void killClients(){
        while(listening || !connected.isEmpty()){
            
            connected.keySet().stream().forEach((sock) -> {
                T proto = connected.get(sock);
                if (!proto.isAlive()) {
                    System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | - Client: "+sock.toString()+" | "+proto.toString());
                    try {
                        sock.close();
                    } catch (IOException ex) {
                        System.err.println("Could not unconnect client!!");
                        ex.printStackTrace();
                    }
                    connected.remove(sock);
                }
            });
        }
    }
}
