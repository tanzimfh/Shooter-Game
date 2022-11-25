package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
//Wall class - allows destructible wall objects to be created, giving enemies new opportunities for entry and players opportunities to bolster their defenses
public class Wall {
    private final Rectangle hitbox;
    private final Ellipse2D actionbox; //player acting range
    private boolean reinforced, intact, reinforcing, enemyActing, animating; //reinforced - enemies cannot get through, intact - wall is still standing and can provide cover,
    //reinforcing - player currently reinforcing wall, enemyActing - enemy trying to breach wall, animating - enemy successfully detonated breach charge so explosion animation plays.
    private double progress; //reinforcing progress
    private final double rspeed=Siege.spf/4.5; //reinforcing speed
    private final SoundEffect rsound=new SoundEffect("Assets/Sounds/reinforce.wav"), dsound=new SoundEffect("Assets/Sounds/destroy.wav"), explode = new SoundEffect("Assets/sounds/explosion.wav");
    //rsound - reinforcing sound, dsound - destruction sound, explode - breach charge going off sound
    private int hp=1000, enemyProg = 0; //wall hp, enemy progress with breaching wall
    private Timer actTimer = new Timer(); //enemy progress timer
    private final Image[] explodeSprites= new Image[14]; //explode sprites
    private Image currentSprite;
    private Timer aniTimer = new Timer(); //animation timer
    private int spritePos = 0, spriteCount = 0; //current sprite position, amount of sprites total
    private final Image intactPic, reinforcePic; //pictures for walls

    public Wall(Rectangle hitbox, int index){ //constructor
        intactPic= new ImageIcon("Assets/softwalls/intact"+index+".png").getImage(); //generating images for reinforced and non reinforced walls with index
        reinforcePic= new ImageIcon("Assets/softwalls/reinforce"+index+".png").getImage();
        reinforced = false; //default not reinforced
        intact = true;
        this.hitbox=hitbox;
        actionbox=new Ellipse2D.Double(hitbox.getCenterX()-15,hitbox.getCenterY()-15,30,30);
        try {
            for (int i = 0; i < 13; i++) { //getting explode sprites
                explodeSprites[i] = ImageIO.read(new File("Assets/sprites/explode/" + (i + 1) + ".png")).getScaledInstance(90,90,Image.SCALE_SMOOTH);
            }
        } catch(IOException ignored){}
    }

    public void animate(){ //animate wall explosion
        spriteCount = 13; //13 explosion sprites
        TimerTask aniTask = new TimerTask() {
            public void run() {
                if(spritePos < spriteCount) spritePos++; //advances sprites as long as there are still sprites to animate
                else{
                    stopAnimation(); //once end of sprites reached, animation stops
                }
                currentSprite = explodeSprites[spritePos]; //current sprite is drawn on screen
            }
        };
        if(!animating){ //start animation
            aniTimer = new Timer(); //new timer
            aniTimer.scheduleAtFixedRate(aniTask,0,100); //schedule animation task
        }
        animating = true;
    }

    public void stopAnimation(){ //cancel animation
        if(animating) aniTimer.cancel(); //stop timer
        animating = false;
        spritePos = 0; //reset sprite position
    }

    public void breach(Player player, Enemy e){ //enemy breaching wall
        enemyActing = true; //enemy currently acting, so player can't act on the wall
        TimerTask task = new TimerTask(){
            public void run(){
                if(enemyProg < 5) enemyProg++; //5 seconds before wall blows up, advances enemy progress
                else{
                    enemyProg = 0; //reset enemyProg
                    actTimer.cancel();
                    intact = false; //destroys wall
                    e.stopAct(); //stops enemy actions
                    enemyActing = false;
                    animate(); //starts explosion animations
                    explode.play(); //plays explosion sound
                    damage(player); //checks if blast would damage player
                }
            }
        };
        actTimer = new Timer(); //starting countdown to detonation
        actTimer.scheduleAtFixedRate(task,0,1000);
    }

    public void damage(Player p){ //damage player via breaching charge
        double dx= p.getCenterX()-hitbox.getX(); //x distance
        double dy= p.getCenterY()-hitbox.getY(); //y distance
        double dist = Math.hypot(dx,dy); //distance from blast center
        p.hit(Math.max((int) (300-3*dist),0)); //damages player (if player further away, wont do damage)
    }

    public void cancelEnemyAct(){ //stop enemy act (if player kills them while acting)
        actTimer.cancel(); //stops countdown
        enemyProg = 0;//resets progress
        enemyActing = false;
    }

    public void reinforce(){ //reinforce wall (player action)
        reinforcing = true; //currently reinforcing, enemies cannot act on it
        if(progress==0) rsound.play(); //play reinforce sound
        progress+=rspeed; //advance progress
        if(progress>=1){ //finished reinforcing
            reinforced=true;
            progress=0; //reset progress
            reinforcing = false;
        }
    }

    public void hit(int d){ //bullets damaging wall
        hp-=d; //removes health
        if(hp<=0){ //destroys wall if <0
            dsound.play();
            intact=false;
        }
    }

    public void reset(){ //reset wall to default
        reinforced = false;
        intact = true;
        if(enemyActing) cancelEnemyAct();
        if(animating) stopAnimation();
    }

    public void stopReinforce(){ //cancel player reinforce
        rsound.stop(); //stop sound effect, reset progress
        progress = 0;
    }

    //accessors
    public Rectangle getHitbox() { return hitbox; }
    public double getProgress(){ return progress; }
    public boolean isIntact(){ return intact; }
    public boolean isActable(){ return !reinforced&&!enemyActing; }
    public boolean isReinforced(){ return reinforced; }
    public Image getSprite(){ return currentSprite; }
    public boolean isReinforcing(){ return reinforcing; }
    public boolean isAnimating(){ return animating; }
    public Image getIntactPic(){ return intactPic; }
    public Image getReinforcePic(){ return reinforcePic; }

    public boolean checkHit(Bullet b){ //check if bullet hit wall
        if ((intact||reinforced)&&hitbox.contains(b.getPoint())){ //bullet in wall
            if (intact&&!reinforced) hit(b.getDmg()); //damages wall, wall comes down after sustaining too much damage (reinforced makes it bulletproof)
            return true;
        }
        return false;
    }
    public boolean inRange(Player player){
        return actionbox.contains(player.getFrontX(),player.getFrontY());
    } //player in acting range (can reinforce)
}
