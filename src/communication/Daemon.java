/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import java.io.IOException;

/**
 *
 * @author artureca
 */
public interface Daemon {
    public void start() throws IOException;
    public void stop();
}
