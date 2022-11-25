//Bullet.java
//each of the bullets fired by both the player and enemies,
//shot from a player or enemy and follows straight line until impact
package com.company;
import java.awt.*;

public class Bullet{
    private double x,y; //coordinates of bullet
    private final int dmg, v; //damage bullet will do to player/enemy, speed of bullet in pixels/frame
    private final double ang; //angle bullet will follow in rad
    private final boolean friendly; //whether or not shot by player
    private boolean active=true; //bullet is inactivated on impact, starts active


    public Bullet(Player player){ //constructor for bullet shot by player
        friendly=true;
        Point muzzle=player.getMuzzle(); //position of the gun's muzzle
        x=muzzle.getX(); //bullet starts at muzzle
        y=muzzle.getY();
        Gun gun=player.getEquipped(); //gun firing the bullet
        dmg=gun.getDmg(); //bullet's damage and speed determined by gun
        v=gun.getV();
        double dev=gun.getDev(); //max bullet deviation in relative integers
        ang=player.getBulletAng()+randdouble(-dev,dev)*0.02;
        //angle of bullet is angle gun is pointed + random deviation to either side (converted to rad)
    }

    public Bullet(Enemy enemy){ //shot by enemy, very similar to player
        friendly=false;
        Point muzzle=enemy.getMuzzle();
        x=muzzle.getX();
        y=muzzle.getY();
        Gun gun=enemy.getGun();
        dmg=gun.getDmg();
        v=gun.getV();
        double dev=gun.getDev()*.5/Siege.difficulty;
        ang=enemy.getAng()+randdouble(-dev,dev);
        // 10x the deviation to compensate for enemy having perfect aim
    }

    public void move(){ //move bullet one pixel, possible because x and y are doubles
        x+=Math.cos(ang); //trig
        y+=Math.sin(ang);
    }

    //get methods
    public int getX(){ return (int) x; } //casting to int for display on screen
    public int getY(){ return (int) y; }
    public int getV() { return v; }
    public Point getPoint(){ return new Point((int)x,(int)y); }
    public void deactivate(){ active=false; }
    public boolean isActive(){ return active; }
    public int getDmg(){ return dmg; }
    public boolean isFriendly() { return friendly; }

    public static double randdouble (double low, double high){ //returns a random double between 2 doubles
        return (Math.random()*(high-low)+low); //random double between 0 and 1 * range + lower double
    }
}