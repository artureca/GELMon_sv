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

import java.io.IOException;

/**
 * The Daemon interface to be used by the main launcher.
 * @author Artur Antunes
 */
public interface Daemon {

    /**
     * Starts the daemon.
     * @throws IOException
     * @author Artur Antunes
     */
    public void start() throws IOException;

    /**
     * Stops the daemon.
     * @author Artur Antunes
     */
    public void stop();
}
