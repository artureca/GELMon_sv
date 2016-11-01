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

@SuppressWarnings("CallToPrintStackTrace")

/**
 *
 * @author Artur Antunes
 */
public class UDP_sv <T extends Protocol> implements Daemon{
    private final Integer port;
    private Boolean listening ;
    private DatagramSocket serverSocket;
    private final Class<T> clazz;
    private final String label;
    private Integer bufferSize;

    public UDP_sv(Class<T> clazz, Integer port) {
        this.port = port;
        this.clazz = clazz;
        this.label = null;
        this.bufferSize = 10240;
    }
    
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

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    
    
}
