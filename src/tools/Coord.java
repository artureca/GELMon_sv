/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.awt.geom.Point2D;
import java.util.Objects;

/**
 *
 * @author Artur Antunes
 */
public class Coord extends Point2D{

    private Integer X;
    private Integer Y;

    public void setX(Integer X) {
        this.X = X;
    }

    public void setY(Integer Y) {
        this.Y = Y;
    }
    
    @Override
    public double getX() {
        return this.X;
    }

    @Override
    public double getY() {
        return this.Y;
    }

    @Override
    public void setLocation(double d, double d1) {
        this.X = (int) Math.round(d);
        this.Y = (int) Math.round(d1);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.X);
        hash = 89 * hash + Objects.hashCode(this.Y);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Coord other = (Coord) obj;
        if (!Objects.equals(this.X, other.X)) {
            return false;
        }
        return Objects.equals(this.Y, other.Y);
    }
    
    
}
