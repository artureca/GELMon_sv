/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("CallToPrintStackTrace")

/**
 *
 * @author artureca
 */
public class TCP_sv {
    private static final ConcurrentHashMap<Socket, Proto_AZGO> connected= new ConcurrentHashMap<>();    
    private static final Integer PORT = 1531;
    private static Boolean listening ;
    private static ServerSocket serverSocket;
    
        
    public static void start() {
       
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
 
    public static void stop() {

        try {
            serverSocket.close();
        } catch (IOException ex) {
            System.err.println("Could not close port!");
            ex.printStackTrace();
            System.err.println("Server shuting down!");
        }
        
        System.err.println("Scrabble - SERVER FINISH");
    }

    private static void addClients(){
        while(listening){
            Socket sock;
            Proto_AZGO proto;
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

            proto = new Proto_AZGO(out, in);
            proto.start();

            connected.put(sock, proto);
            
            System.out.println("+ Client: "+sock.toString()+" | "+proto.toString());
        }
    }
    
    private static void killClients(){
        while(listening || !connected.isEmpty()){
            
            connected.keySet().stream().forEach((sock) -> {
                Proto_AZGO proto = connected.get(sock);
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
