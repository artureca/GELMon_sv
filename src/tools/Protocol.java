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
import java.sql.Timestamp;

/**
 * Abstract class used as a template for comunications protocols.
 * Any server/client implementations must expect to receive generic classes that
 * extends this class.
 * Every connection protocol that are to be used with server implemented as
 * previously stated must extend this class.
 * 
 * @author Artur Antunes
 */
abstract public class Protocol extends Thread{
    protected final PrintWriter out;
    protected final BufferedReader in;

    /**
     * Simple constructor.
     * 
     * @param out the uplink channel (PrintWriter)
     * @param in the downlink channel (BufferedReader)
     * @author Artur Antunes
     */
    public Protocol(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }
    
    /**
     * Used in a connection based system. Overrides the run method from Thread.
     * It reads from the downlink, calls the decode method and sends the
     * response upstream. It's kept looping until it can't read from the
     * downlink anymore, or if the decode melhod returns null. Either way the
     * connection with the client is closed.
     * 
     * Both the uplink and the downlink String are sent to the standard output.
     * 
     * @author Artur Antunes
     */
    @Override
    public void run() {
        try {
            String inputLine, outputLine;
            while ((inputLine = this.in.readLine()) != null) {
                System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Received: " + inputLine + " | From: " + this.toString());
                outputLine = this.decode(inputLine);
                if (outputLine == null) 
                    break;
                if (" ".equals(outputLine))
                    continue;
                synchronized(this.out){
                    this.out.println(outputLine);
                }
                System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Sent: " + outputLine + " | To: " + this.toString());
            }
            this.kill();
            this.out.close();
            this.in.close();

        } catch (IOException e) {
            // Ignore exception!!!
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * Used in a datagram based system. Calls the decode method and sends both
     * the uplink and the downlink String to the standard output.
     * 
     * @param received the downlink String
     * @return the uplink String
     * @author Artur Antunes
     */
    public String logDecode(String received){
        System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Received: " + received + " | From: " + this.toString());
        String sent = this.decode(received);
        System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Sent: " + sent + " | To: " + this.toString());
        return sent;
    }
    
    /**
     *  Processes the downlink request and sends a response to uplink.
     *  It has to be implemented by sub-classes.
     * 
     * @author Artur Antunes
     * @param received the downlink String
     * @return the uplink String
     */
    abstract public String decode(String received);
    
    /**
     *  Processes the downlink request and sends a response to uplink.
     *  It has to be implemented by sub-classes.
     * 
     * @author Artur Antunes
     */
    abstract public void kill();
    
    protected static void sendTo(String data, PrintWriter out){
        synchronized(out){
            out.println(data);
        }
        System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Sent: " + data);
    }
    
}
