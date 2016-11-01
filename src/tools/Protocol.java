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
 *
 * @author artureca
 */
abstract public class Protocol extends Thread{
    final PrintWriter out;
    final BufferedReader in;

    public Protocol(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }
    
    @Override
    public void run() {
        try {
            String inputLine, outputLine;
            while ((inputLine = this.in.readLine()) != null) {
                System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Received: " + inputLine + " | From: " + this.toString());
                outputLine = this.decode(inputLine);
                if (outputLine == null) break;
                synchronized(this.out){
                    this.out.println(outputLine);
                }
                System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Sent: " + outputLine + " | To: " + this.toString());
            }
            this.out.close();
            this.in.close();

        } catch (IOException e) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public String logDecode(String received){
        System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Received: " + received + " | From: " + this.toString());
        String sent = this.decode(received);
        System.out.println("@ " + new Timestamp(System.currentTimeMillis()).toString() + " | Sent: " + sent + " | To: " + this.toString());
        return sent;
    }
    
    abstract public String decode(String received);
}
