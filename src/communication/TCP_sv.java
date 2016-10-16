/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("CallToPrintStackTrace")

/**
 *
 * @author artureca
 */
public class TCP_sv <T extends Protocol> implements Daemon{
    private final ConcurrentHashMap<Socket, T> connected= new ConcurrentHashMap<>();    
    private final Integer port;
    private Boolean listening ;
    private ServerSocket serverSocket;
    private final Class<T> clazz;
    private final String label;

    TCP_sv(Class<T> clazz, Integer port) {
        this.port = port;
        this.clazz = clazz;
        this.label = null;
    }
    
    TCP_sv(Class<T> clazz, Integer port, String label) {
        this.port = port;
        this.clazz = clazz;
        this.label = label;
    }

    private T newClient(PrintWriter out, BufferedReader in) throws InstantiationException, IllegalAccessException {
        return clazz.newInstance();
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
            System.out.println("Started: " + this.label);
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
            System.out.println("Stopped: " + this.label);
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
                System.out.println("+ Client: "+sock.toString()+" | "+proto.toString());

            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(TCP_sv.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void killClients(){
        while(listening || !connected.isEmpty()){
            
            connected.keySet().stream().forEach((sock) -> {
                T proto = connected.get(sock);
                if (!proto.isAlive()) {
                    System.out.println("- Client: "+sock.toString()+" | "+proto.toString());
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
