package com.company;

import java.awt.geom.Ellipse2D;
//Resupply class - allows players to resupply ammo and supplies in between rounds or during rounds if they like to live life dangerously
public class Resupply {
    private int x,y,supply; //coordinates and amount of resupplies left
    private final Ellipse2D actionbox; //range in which player can resupply
    private final double resupplySpeed=Siege.spf/5; //speed of resupply
    private double resupplyProgress = 0; //progress

    public Resupply(int x,int y){ //constructor
        this.x = x;
        this.y = y;
        supply = 5; //only 5 resupplies allowed, player out of luck if all run out
        actionbox=new Ellipse2D.Double(x-10,y-10,30,30); //forming range
    }

    public void startResupply(){ //begin resupply progress
        resupplyProgress += resupplySpeed; //advance progress
        if(resupplyProgress >= 1){ //resupply finished
            resupplyProgress = 0; //resets progress
            supply--; //removes one supply
        }
    }

    public void stopResupply(){
        resupplyProgress = 0;
    } //cancel resupply

    //accessors
    public int getX(){ return x; }
    public int getY(){ return y; }
    public int getSupply(){ return supply; }
    public boolean hasSupply(){ return supply > 0; }
    public Ellipse2D getActionbox(){ return actionbox; }
    public double getProg(){ return resupplyProgress; }
}
