package com.company;

import java.awt.*;
import java.awt.geom.Ellipse2D;
//Hostage object - game objective to protect
public class Hostage {
    private final int x,y, maxhp, downedhp=40; //coordinates, maximum hp for alive and downed states
    private int hp; //current hp
    private boolean downed = false; //2 stages of hostage damage -> downed(but not out) and alive (downed is like a weakened alive stage, a second chance for the player)
    private final double revspeed=Siege.spf/2; //revive speed (for player)
    private double revprogress = 0; //revive progress
    private final Shape hitbox;
    private final Ellipse2D actionbox; //range in which player can revive hostage
    private final Ellipse2D range; //range in which enemies can start shooting at hostage

    public Hostage(int x, int y, int hp){ //constructor
        this.x = x;
        this.y = y;
        this.hp=maxhp=hp;
        hitbox=new Ellipse2D.Double(x-5,y-5,10,10);
        actionbox=new Ellipse2D.Double(x-15,y-15,30,30);
        range = new Ellipse2D.Double(x-120,y-120,240,260);
    }
    public void revive(){ //reviving hostage if downed
        revprogress += revspeed; //progressing revive process
        if (revprogress>=1){ //revive complete
            downed = false; //removed from down but not out stage
            hp=maxhp; //full hp
            revprogress = 0; //resets progress for next revive
        }
    }

    public void stopRev(){
        revprogress=0;
    } //cancel revive

    private void hit(int d){
        hp-=d;
    } //damage hostage

    public boolean isDead(){
        return hp<=0;
    }

    public void checkDown(){ //checks if hostage is down
        if(hp<=0){ //if no hp
            if (!downed){ //if he is still alive, hostage put into downed stage
                hp=downedhp; //gets a fraction of previous health - like a second life
                downed=true;
            }
            else downed = false; //if downed previously and enemies finish it off, hostage is dead
        }
    }

    public boolean checkHit(Bullet b){ //checks if bullet hit hostage
        if (hitbox.contains(b.getPoint())){
            hit(b.getDmg()); //if bullet in hitbox hostage damaged
            checkDown(); //checks if downed or dead after each shot lands
            return true; //returns true if hit
        }
        return false;
    }

    public boolean inRange(Player player){
        return actionbox.contains(player.getFrontX(),player.getFrontY()); //checks if player is in range to revive
    }

    //accessors
    public double getProgress(){ return revprogress; }
    public boolean isDowned(){ return downed; }
    public Shape getHitbox(){ return hitbox; }
    public Shape getRange(){ return range; }

    public int getX(){ return x-5; }
    public int getY(){ return y-5; }
    public int getCenterX(){ return x; }
    public int getCenterY(){ return y; }
    public Point getPoint(){ return new Point(x,y); }
}
