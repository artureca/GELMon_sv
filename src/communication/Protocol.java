/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import java.io.*;

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
                outputLine = this.decode(inputLine);
                if (outputLine == null) break;
                synchronized(this.out){
                    this.out.println(outputLine);
                }
            }
            this.out.close();
            this.in.close();

        } catch (IOException e) {
            //IGNORE EXCEPTION ???
        }
    }
    
    abstract String decode(String received);
}
