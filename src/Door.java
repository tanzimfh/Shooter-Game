//Door.java
//doors on the map that can be barricaded, trapped, and broken
//plays sounds for each action and can be interacted with by player and enemies
package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
//Door class - allows players to use doorways to bolster their defenses
public class Door {
    private final Rectangle hitbox; //space door occupies
    private final Ellipse2D actionbox, blastRadius; //area player/enemy must be in to interact, area trap does damage to when
    private boolean trapped=false, enemyActing=false; //doors start with no trap and no enemy acting
    private boolean barricaded, animating; //whether door is barricaded, enemy is currently interacting, trap explosion is being animated
    private boolean finishsound; //indicates whether to let the sound playing finish or interrupt it
    private boolean trapping, barricading; //whether currently being trapped or barricaded
    private int hp=250; //current health of door, starts at 250 hp
    private double progress=0; //current progress between 0 and 1 of interaction
    private final double bspeed=Siege.spf/2.5, tspeed=Siege.spf/0.6; //barricade and teardown speeds, relative to fps so time duration stays same
    private final double setSpeed=Siege.spf/1.7, removeSpeed=Siege.spf/0.6; //trap interaction speeds
    private final SoundEffect barricadeSound=new SoundEffect("Assets/Sounds/barricade.wav"), teardownSound=new SoundEffect("Assets/Sounds/teardown.wav"), destroySound=new SoundEffect("Assets/Sounds/destroy.wav");
    private final SoundEffect detachTrapSound=new SoundEffect("Assets/sounds/shortSwitch.wav"), attachTrapSound=new SoundEffect("Assets/sounds/setUpTrap.wav");
    //sounds for interactions
    private Timer actTimer = new Timer(); //timer used for breaching
    private Timer aniTimer = new Timer(); //for animating explosion
    private final Image[] explodeSprites= new Image[14]; //trap exploding sprites
    private int spritePos = 0, secs=0; //current sprite index, counting seconds for breach

    public Door (Rectangle hitbox, boolean barricaded){ //constructor, takes the hitbox and whether it starts barricaded
        this.barricaded = barricaded;
        this.hitbox=hitbox;
        actionbox=new Ellipse2D.Double(hitbox.getCenterX()-14,hitbox.getCenterY()-14,28,28); //interaction area
        blastRadius=new Ellipse2D.Double(hitbox.getCenterX()-40,hitbox.getCenterY()-40,80,80); //trap blast area

        try {
            for (int i = 0; i < 13; i++) {
                //loading trap explosion sprites
                explodeSprites[i] = ImageIO.read(new File("Assets/sprites/explode/" + (i + 1) + ".png")).getScaledInstance(90,90,Image.SCALE_SMOOTH);
            }
        } catch(IOException ignored){}
    }

    public void animate(){ //animates trap explosion
        TimerTask aniTask = new TimerTask() {
            public void run() { //going through sprites
                if(spritePos < 13) spritePos++;
                else{ //stopping animation at end
                    if(animating) aniTimer.cancel();
                    animating = false; //resetting variables
                    spritePos = 0;
                }
            }
        };
        if(!animating){ //if starting animation
            aniTimer = new Timer();
            aniTimer.scheduleAtFixedRate(aniTask,0,100); //changing sprites at 10 Hz
            animating = true;
        }
    }


    public void barricade(){ //player barricading/tearing down barricade
        if(!enemyActing){ //can't barricade if enemies interacting
            finishsound=false; //sound is interrupted if player stops acting
            barricading = true;
            if(progress==0) { //if starting action
                if (barricaded) teardownSound.play(); //teardown sound if door is barricaded
                else barricadeSound.play(); //barricade sound if not
            }
            progress += barricaded?tspeed:bspeed; //progressing at tearing speed if barricaded, barricade speed if not
            if (progress>=1){ //done interacting
                barricaded=!barricaded; //flip barricade state
                hp=250; //reset hp, doesn't matter if just tore down
                progress=0; //reset progress
                finishsound=true; //finish out the rest of the sound
                barricading = false;
            }
        }
    }

    public void trap(){ //player attaching trap to door
        if(!enemyActing) { //very similar to barricade()
            trapping = true;
            if(progress==0&&!trapped) attachTrapSound.play(); //if starting trapping
            progress += trapped ? removeSpeed : setSpeed;
            if (progress >= 1) {
                trapped = !trapped;
                progress = 0;
                if(!trapped) detachTrapSound.play(); //detach trap sound
                else attachTrapSound.stop();
                trapping = false;
            }
        }
    }

    public void detonate(){ //trap detonation
        trapped = false; //trap goes away
        animate(); //explosion animation
    }

    public void destroy(Enemy e){ //enemy destroying barricade
        enemyActing = true;
        TimerTask task = new TimerTask(){
            public void run(){
                if(secs < 2) secs++; //teardown takes 2 seconds
                else{ //after 2 seconds
                    secs = 0; //reset variables
                    actTimer.cancel();
                    destroySound.play();
                    barricaded = false;
                    e.stopAct();
                    enemyActing = false;
                }
            }
        };
        actTimer = new Timer();
        actTimer.scheduleAtFixedRate(task,0,1000); //starting timer triggering every second
    }

    public void cancelEnemyAct(){ //stops enemy acting
        actTimer.cancel(); //reset variables
        secs = 0;
        enemyActing = false;
    }

    public void stopAct(){ //stopping player acting
        progress=0; //reset acting variables
        trapping = false;
        attachTrapSound.stop();
        barricading = false;
        if(!finishsound) { //interrupt sounds
            teardownSound.stop();
            barricadeSound.stop();
        }
    }

    public void hit(int dmg){ //door taking damage, takes damage as parameter
        hp-=dmg;
        if(hp<=0){ //if door gets broken
            destroySound.play();
            barricaded=false; //no longer barricaded
        }
    }

    //get methods
    public int getCenterX(){ return (int) blastRadius.getCenterX(); }
    public int getCenterY(){ return (int) blastRadius.getCenterY(); }
    public boolean isActing(){ return enemyActing; }
    public boolean isTrapping(){ return trapping; }
    public boolean isBarricading(){ return barricading; }
    public boolean isAnimating(){ return animating; }
    public double getProgress(){ return progress; }
    public Rectangle getHitbox() { return hitbox; }
    public boolean isBarricaded() { return barricaded; }
    public boolean isTrapped(){ return trapped; }
    public Ellipse2D getRadius(){ return blastRadius; }
    public Image getSprite(){ return explodeSprites[spritePos]; } //returns current sprite of explosion

    public boolean inRange(Player player){ //takes the player and returns whether they can interact with the door
        return actionbox.contains(player.getFrontX(),player.getFrontY()); //true if front of player in interact radius
    }

    public boolean checkHit(Bullet b){ //takes a bullet and returns whether it's inside the door
        if(barricaded&&hitbox.contains(b.getPoint())){ //doesn't hit if not barricaded
            hit(b.getDmg()); //take damage from bullet
            return true;
        }
        return false;
    }
}

