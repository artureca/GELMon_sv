/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.*;

/**
 *
 * @author artureca
 */
public class Protocol extends Thread{
    private final PrintWriter out;
    private final BufferedReader in;

    public Protocol(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }
}
