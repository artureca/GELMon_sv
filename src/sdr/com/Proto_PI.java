/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdr.com;

import java.io.BufferedReader;
import java.io.PrintWriter;
import tools.*;

/**
 *
 * @author artureca
 */
public class Proto_PI extends Protocol {

    public Proto_PI(PrintWriter out, BufferedReader in) {
        super(out, in);
    }

    @Override
    public String decode(String received) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
