//Enemy.java
//used to create enemies that can move, shoot,
//and interact with objects on the map
package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

//Enemy class - used to create enemy objects that will try and either kill the player or the hostage
public class Enemy {
    private double ang, lastAng, bulletcooldown=0; //angle facing, last angle tried to face, cooldown until next bullet can be fired
    private int x,y,hp; //location, health
    private double muzzleduration = 0; //duration left for muzzle flash to appear on enemy's gun
    private boolean movecooldown=false; //whether enemy can currently move
    private final double muzzlespeed=Siege.spf*200/3; //speed at which muzzle flash disappears
    private boolean dead=false, seesPlayer =false, seesHostage=false;
    private final boolean breacher; //whether enemy can breach walls
    private final Gun gun; //enemies have one gun
    private Shape hitbox;
    private Ellipse2D movementCircle; //enemy's personal space that other enemies are kept away from
    private LinkedList<Integer>path=null; //path that enemy will follow to hostage, each node has a direction
    private Door actingDoor = null; //door/wall enemy is currently acting on
    private Wall actingWall = null;
    private boolean animating = false; //whether currently animating sprites
    private boolean moving = false, acting=false, turnComplete = false;
    //whether enemy is moving, acting on door/wall, has completed turn
    private final BufferedImage[] moveSprites = new BufferedImage[8], shootSprites = new BufferedImage[8], actSprites = new BufferedImage[8]; //sprites for movement, muzzle flash, and interaction
    private Timer aniTimer = new Timer(); //timer for animating sprites
    private int spritePos = 0; //current sprite index

    public Enemy (int x, int y, double ang, int hp, Gun gun, boolean breacher){ //constructor, takes location, direction, health, gun, and whether the enemy can breach walls
        this.x = x;
        this.y = y;
        this.lastAng=this.ang=ang;
        this.gun=gun;
        this.hp=hp;
        this.breacher = breacher;
        updateHitbox();
        updateMovebox();
        try { //loading sprite sheets
            BufferedImage moveSprite=ImageIO.read(new File("Assets/sprites/enemyMove.png"));
            BufferedImage shootSprite=ImageIO.read(new File("Assets/sprites/shootsprite.png"));
            BufferedImage actSprite=ImageIO.read(new File("Assets/sprites/enemyAct.png"));
            for (int i = 0; i < 8; i++) { //cutting sprite sheet into 8 frames for sprites
                moveSprites[i] = moveSprite.getSubimage(0, 35*i, 75, 35);
                shootSprites[i] = shootSprite.getSubimage(0, 35*i, 75, 35);
                actSprites[i] = actSprite.getSubimage(0, 35*i, 75, 35);
            }
        } catch(IOException ignored){}

        TimerTask moveTask = new TimerTask() { //task for resetting movement cooldown
            public void run() {
                movecooldown=false;
            }
        };
        new Timer().scheduleAtFixedRate(moveTask,0,20);
    }

    public void updateHitbox(){
        hitbox=new Ellipse2D.Double(x-12,y-12,24,24); //hitbox is a 12px radius around center
    }
    public void updateMovebox(){
        movementCircle=new Ellipse2D.Double(x-50,y-50,100,100); //50px radius personal space
    }

    public void hit(int dmg){ //taking damage
        hp-=dmg;
        if(hp<=0) dead=true;
    }

    public void animate(int delay){ //animates sprites
        TimerTask aniTask = new TimerTask() { //changes sprite index to animate
            public void run() {
                spritePos=(spritePos+1)%8; //next sprite index, loops
            }
        };
        if(!animating){ //starting animation
            aniTimer = new Timer();
            aniTimer.scheduleAtFixedRate(aniTask,0,delay);
        }
        animating = true;
    }

    public void stopAnimation(){ //pauses animation
        if(animating) aniTimer.cancel(); //cancel sprite index change
        animating = false; //reset variables
        spritePos = 0;
    }

    public void face(Point spot){ //makes enemy turn to face a given point
        double dx=spot.getX()-x, dy=spot.getY()-y; //delta x and y
        double targetAng; //angle trying to face
        if (dx==0){ //rare case delta x is exactly 0
            if(dy>0) targetAng=Math.PI/2; //facing down
            else targetAng=3*Math.PI/2; //up
        }
        else targetAng=Math.atan(dy/dx)+(dx<0?Math.PI:0); //if dx!=0, can use arctan to find angle
        double angDiff = targetAng-ang; //delta angle
        if(targetAng==ang){
            turnComplete=true;
            return;
        }
        turnComplete = false;
        if(angDiff>Math.PI) angDiff-=Math.PI*2; //make angle as short as possible
        if(angDiff<-Math.PI) angDiff+=Math.PI*2;

        double changespeed=(angDiff>0?1:-1)*Siege.spf*Siege.difficulty;
        //amount to turn per frame, relative to fps and difficulty
        ang+=changespeed;
        if(angDiff<=changespeed&&angDiff>=-changespeed){
            ang+=angDiff; //finish turning when close enough
            turnComplete = true;
        }
    }

    public void face(double targetAng){ //same as face(Point spot) but angle given
        if(targetAng==ang){
            turnComplete=true;
            return;
        }
        turnComplete = false;
        double angDiff = targetAng-ang;
        if(angDiff>Math.PI) angDiff-=Math.PI*2;
        if(angDiff<-Math.PI)angDiff+=Math.PI*2;
        double changespeed=(angDiff>0?5:-5)*Siege.spf;
        //method is called less often than face(Point spot) so change in speed is greater,
        // not multiplied by Siege.difficulty because higher difficulty calls method more often already
        ang+=changespeed;
        if(angDiff<=changespeed&&angDiff>=-changespeed){
            ang+=angDiff;
            turnComplete = true;
        }
    }

    public void move(){ //called every frame, moves enemy
        if(path.size()>0&&!seesPlayer&&!seesHostage&&!acting) { //if path exists, enemy doesn't see player or hostage and isn't interacting
            face(lastAng); //turn towards direction moving
            if(!movecooldown && moving){ //if moving is allowed
                movecooldown=true;
                lastAng = path.removeFirst()*Math.PI/2; //update angle moving
                x+=Math.cos(lastAng); //move 1 pixel
                y+=Math.sin(lastAng);
                updateHitbox();
                updateMovebox();
            }
            moving = true; //moving is allowed if neither shooting nor acting (overriden later if enemies share personal space)
        }
        else moving = false;
    }

    public void gainEyes(Hostage obj){ //called when enemy has line of sight to hostage
        seesHostage=true;
        if(!acting) face(obj.getPoint()); //enemy turns to hostage
    }

    public void gainEyes(Player player){ //line of sight to player
        seesPlayer =true;
        if(!acting) face(player.getPoint());
    }

    public void loseEyes(Player p){
        seesPlayer =false;
    } //called when no line of sight
    public void loseEyes(Hostage h){
        seesHostage=false;
    }

    public BufferedImage getCurrentSprite(){ //returns the current sprite frame
        if(animating){
            if(acting) return actSprites[spritePos]; //return appropriate sprite's current index
            return moveSprites[spritePos];
        }
        return moveSprites[0]; //not moving/acting
    }

    public BufferedImage getShootSprite(){ //returns image with muzzle flash
        if(animating) return shootSprites[spritePos];
        return shootSprites[0];
    }


    public boolean canShoot(){ //returns whether enemy is allowed to shoot
        return bulletcooldown==0 && turnComplete && !acting;
    }

    public void shoot(){ //shoots gun
        muzzleduration = 1; //reset muzzle flash
        gun.shoot();
        bulletcooldown=1; //reset shot cooldown
    }

    public void cooldown(){ //called every frame, lowers shot and muzzle flash cooldowns
        bulletcooldown = Math.max(0,bulletcooldown-gun.getRate()); //decrease by gun's rate of fire until no cooldown
        muzzleduration= Math.max(0,muzzleduration-muzzlespeed);
    }

    public boolean checkHit(Bullet b){ //takes a bullet and checks if it hits enemy
        if (hitbox.contains(b.getPoint())){ //if inside enemy
            hit(b.getDmg()); //take damage
            return true;
        }
        return false;
    }

    public void removeBarricade(Door d){ //makes enemy tear down given door
        actingDoor = d;
        d.destroy(this);
        spritePos = 0; //reset sprite index
    }

    public void breachWall(Player player, Wall w){ //makes enemy breach given wall, passes player onto Wall to check for damage
        actingWall = w;
        w.breach(player, this);
        spritePos = 0;
    }

    public void cancelActions(){ //disengages enemy from Door and Wall when not interacting
        if(actingDoor != null) actingDoor.cancelEnemyAct();
        else if(actingWall != null) actingWall.cancelEnemyAct();
    }

    public Point getMuzzle(){ //returns location of gun's muzzle
        double l= Math.hypot(10,23); //finding distance of line from center of enemy to muzzle, idle sprite has muzzle (10,23) pixels off center
        double gunAng=Math.atan(1.0/2.3)+ang;//finding the line's angle and adding enemy's angle to find actual angle of gun
        return new Point((int) (l*Math.cos(gunAng)+x) ,(int) (l*Math.sin(gunAng)+y)); //returning the tip of that line
    }

    //get/set methods
    public int getX(){ return x-5; }
    public int getY(){ return y-5; }
    public int getCenterX(){ return x; }
    public int getCenterY(){ return y; }
    public int getFrontX(){ return (int) (x + 20*Math.cos(ang)); }
    public int getFrontY(){ return (int) (y + 20*Math.sin(ang)); }
    public int getBackX(){ return (int) (x - 10*Math.cos(ang)); }
    public int getBackY(){ return (int) (y - 10*Math.sin(ang)); }
    public int getPathSize(){ return path.size(); }

    public boolean isAnimating(){ return animating; }
    public boolean isMoving(){ return moving; }
    public boolean isActing(){ return acting; }
    public boolean isBreacher(){ return breacher; }
    public boolean hasMuzzle(){ return muzzleduration>0; }
    public Point getPoint(){ return new Point(x,y);}
    public Gun getGun(){ return gun; }
    public double getAng(){ return ang; }
    public Shape getHitbox(){ return hitbox; }
    public Ellipse2D getMovebox(){ return movementCircle; }
    public boolean isDead(){ return dead; }

    public boolean seesPlayer() { return seesPlayer; }
    public boolean seesHostage() { return seesHostage; }
    public void setPath(LinkedList<Integer>path){ this.path=path; }
    public void stopMove(){ moving = false; }
    public void act(){
        acting = true;
        moving = false;
    }
    public void stopAct(){ acting = false; }
}
