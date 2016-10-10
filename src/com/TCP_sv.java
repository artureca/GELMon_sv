/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

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
public class TCP_sv <T extends Protocol> {
    private final ConcurrentHashMap<Socket, T> connected= new ConcurrentHashMap<>();    
    private final Integer PORT = 1531;
    private Boolean listening ;
    private ServerSocket serverSocket;
    Class<T> clazz;

    public TCP_sv(Class<T> clazz) {
        this.clazz = clazz;
    }

    private T newClient(PrintWriter out, BufferedReader in) throws InstantiationException, IllegalAccessException {
        return clazz.newInstance();
    }
        
    public void start() {
       
        listening = true;
        
        try {
            
            serverSocket = new ServerSocket(PORT);
            
        } catch (IOException ex) {
            System.err.println("Could not listen on port: "+PORT.toString()+"!");
            ex.printStackTrace();
            System.out.println("Server shuting down!");
            return;
        }

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
    }
 
    public void stop() {

        try {
            serverSocket.close();
        } catch (IOException ex) {
            System.err.println("Could not close port!");
            ex.printStackTrace();
            System.err.println("Server shuting down!");
        }
        
        System.err.println("Scrabble - SERVER FINISH");
    }

    private void addClients(){
        while(listening){
            Socket sock;
            T proto = null;
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
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(TCP_sv.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (proto != null){
                proto.start();
            connected.put(sock, proto);
            System.out.println("+ Client: "+sock.toString()+" | "+proto.toString());
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
