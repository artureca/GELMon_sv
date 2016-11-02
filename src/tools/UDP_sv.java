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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.codec.binary.Hex;
import java.net.*;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a deamon UDP server. This server implements the Daemon
 * interface present in the tools package, it's made to work with any class that
 * extends the class Protocol also present in the tools package.
 * 
 * 
 * @author Artur Antunes
 * @param <T> Comunication protocol that extends the Protocol abtract class
 */
@SuppressWarnings("CallToPrintStackTrace")
public class UDP_sv <T extends Protocol> implements Daemon{
    private final Integer port;
    private Boolean listening ;
    private DatagramSocket serverSocket;
    private final Class<T> clazz;
    private final String label;
    private Integer bufferSize;

    /**
     * A Simple constructor.
     * 
     * @author Artur Antunes
     * @param clazz The extended Protocol class
     * @param port The port for the server to listen
     */
    public UDP_sv(Class<T> clazz, Integer port) {
        this.port = port;
        this.clazz = clazz;
        this.label = null;
        this.bufferSize = 10240;
    }
    
    /**
     * A Simple constructor with a description.\
     * 
     * @author Artur Antunes
     * @param clazz The extended Protocol class
     * @param port The port for the server to listen
     * @param label A breif description of the server.
     */
    public UDP_sv(Class<T> clazz, Integer port, String label) {
        this.port = port;
        this.clazz = clazz;
        this.label = label;
        this.bufferSize = 10240;
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
        
        serverSocket = new DatagramSocket(this.port);

        new Thread(){
            @Override
            public void run(){
                addClients();
            }
        }.start();
        
        if (label != null)
            System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Started: " + this.label);
    }
 
    @Override
    public void stop() {
        
        listening = false;
        
        serverSocket.close();
        
        if (label != null)
            System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Stopped: " + this.label);
    }

    private void addClients(){
        while(listening){
            DatagramPacket data;
            byte[] rawData;
            rawData = new byte[this.bufferSize];
            data = new DatagramPacket(rawData,this.bufferSize);
            System.out.println(Hex.encodeHexString(rawData));
            try {
                this.serverSocket.receive(data);
            } catch (IOException ex) {
                Logger.getLogger(UDP_sv.class.getName()).log(Level.SEVERE, null, ex);
            }
            new Thread(){
                @Override
                public void run(){
                    System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | New Client: " + data.getAddress().getHostAddress()+ ":" + data.getPort());
                    processClient(data);
                }
            }.start();
        }
    }
    
    
    private void processClient(DatagramPacket data){
        try {
            T proto = newClient(null, null);
            String response = proto.logDecode(new String(data.getData()));
            if (response == null)
                return;
            this.serverSocket.send(new DatagramPacket(response.getBytes(),response.length(),data.getAddress(),data.getPort()));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException ex) {
            Logger.getLogger(UDP_sv.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the vurrents's buffer size.
     * 
     * @return The current buffer's size
     */
    public Integer getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets the buffer's size.
     * 
     * @param bufferSize The new buffer's size.
     */
    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    
    
}
