package com.company;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import java.util.Timer;
//Player class - allows user to perform player actions to win the game
public class Player{
    private int hp, score = 0, kills = 0;
    private double muzzleCooldown =0; //cooldown before muzzle flash is shown again
    private final double muzzleSpeed=Siege.spf*200/3; //speed at which muzzle cooldown recovers recovers
    private final int maxhp; //maximum hp that can be healed up to
    private final double normalv=Siege.spf*60, sprintv=Siege.spf*100; //walk speed, sprint speed
    private double x, y, ang, v=normalv, fx, fy; //coordinates, facing angle, speed, front of player (x,y)
    private double switchprog=0; //switching gun progress
    private final double switchSpeed=Siege.spf*2; //switching gun speed
    private Shape hitbox;
    private Gun primary, secondary, equipped; //player weapons, main, sidearm, and currently equipped gun
    private boolean acting=false, switching=false, animating = false, moving = false; //player states
    private int reinforcements=3, traps = 5; //wall reinforcements and traps to be set on doorways
    private int spritePos = 0; //player sprite position
    private final SoundEffect switchSound=new SoundEffect("Assets/sounds/shortSwitch.wav"); //switching gun sound
    private final BufferedImage [] moveSprites = new BufferedImage[8]; //lists of sprites
    private final BufferedImage [] shootSprites = new BufferedImage[8];
    private final BufferedImage [] actSprites = new BufferedImage[8];
    private final double[]moveDev={0,.13,.32,.14,0,-.07,0,0}; //gun deviations for each sprite so that bullet comes out of the muzzle at every sprite (makes shooting on the move more erratic)
    private final int[]gunOffX={23,24,22,24,23,23,22,22}; //gun offset for each sprite (centerx - tip of muzzle x, center y - tip of muzzle y)
    private final int[]gunOffY={10,7,3,7,11,10,8,8};
    private Timer aniTimer = new Timer(); //animation timer

    public Player(int x, int y, int maxhp) { //constructor
        this.x=x;
        this.y=y;
        updateHitbox(); //creating hitbox
        this.maxhp=maxhp; //maxhp set
        hp=maxhp; //default hp is max
        try { //loading sprite images
            final BufferedImage moveSprite=ImageIO.read(new File("Assets/sprites/movesprite.png"));
            final BufferedImage shootSprite=ImageIO.read(new File("Assets/sprites/shootsprite.png"));
            final BufferedImage actSprite=ImageIO.read(new File("Assets/sprites/actsprite.png"));
            for (int i = 0; i < 8; i++) {
                moveSprites[i] = moveSprite.getSubimage(0, 35*i, 75, 35); //cutting sprites out from sheet
                shootSprites[i] = shootSprite.getSubimage(0, 35*i, 75, 35);
            }
            for (int i = 0; i < 8; i++) {
                actSprites[i] = actSprite.getSubimage(0, 35*i, 75, 35);
            }
        } catch(IOException ignored){}
    }

    public void useReinforcement(){ reinforcements-=1; } //takes away a reinforcement when player reinforces a wall

    public void sprint(){ //change speeds when sprinting
        v = sprintv;
        equipped.stopReload(); //cancels reload when sprinting
    }
    public void normalSpeed(){
        v = normalv;
    } //return speed to normal

    public void regen(int addHP){
        hp= Math.min(hp+addHP,maxhp); //regenerate health each round - cannot overheal over maxhp
    }

    public void face(double mx, double my){ //face - turns player to mouse position
        double dx=mx-x, dy=my-y; //distance between mx,my and x,y
        ang=Math.atan(dy/dx)+(dx<0?Math.PI:0); //changes angle
        updateFront(); //updates front of player
    }

    public void animate(int delay){ //animate player
        TimerTask aniTask = new TimerTask() {
            public void run() {
                spritePos=(spritePos+1)%8;
            }
        }; //advancing sprite position
        if(!animating){ //start animation
            aniTimer = new Timer(); //start timer, schedule animation task
            aniTimer.scheduleAtFixedRate(aniTask,0,delay);
        }
        animating = true;
    }
    public void stopAnimation(){ //cancel animation
        if(animating) aniTimer.cancel(); //cancel timer
        animating = false;
        spritePos = 0;  //reset sprite position
    }

    public void setLoadout(Loadout l){ //set loadout for player
        equipped=primary = l.getP(); //default equipped weapon is the primary gun, takes secondary and primary weapons from parameter loadout object
        secondary = l.getS();
    }

    public void switchGuns(){  //switching weapons and delaying
        equipped.stopReload(); //cancel any reload in progress
        if(!switching){ //start switching
            switching=true;
            switchSound.play();
        }
        switchprog+=switchSpeed; //advance switch progress
        if(switchprog >= 1){ //finish switch
            equipped=equipped==primary?secondary:primary; //changes equipped weapon
            switchprog = 0; //reset
            switching=false;
        }
    }

    public boolean isDead(){
        return hp<=0;
    } //if player dead, game over

    public void walk(double ang) { //move player with angle
        x+=v*Math.cos(ang); //adding x and y values
        y-=v*Math.sin(ang);
        updateHitbox(); //updates hitbox and front of player
        updateFront();
    }

    public void addKill(){ kills++; }
    public void addScore(int tmp){ score += tmp; }
    public void useTrap(){ traps -= 1; }
    public void addTrap(){ traps+=1; }

    public void resupply(){ //resupply player after interacting with resupply box
        if(reinforcements < 3) reinforcements += 1; //adds reinforcements, resupplies all ammo reserves
        primary.resupply();
        secondary.resupply();
    }

    public boolean canResupply(){ //checks if player can resupply (has used up some resources)
        return primary.getAmmo()!=primary.getMaxAmmo() || secondary.getAmmo()!=secondary.getMaxAmmo() || reinforcements<3;
    }

    public boolean checkLoadout(){
        return primary != null && secondary != null; //checks if loadout is valid to start game (sidearm and primary guns have to be chosen)
    }

    //updating hitbox and front of player
    public void updateHitbox(){
        hitbox=new Ellipse2D.Double(x-10,y-10,23,23);
    }
    public void updateFront(){
        fx = x + 20*Math.cos(ang);
        fy = y + 20*Math.sin(ang);
    }

    public void hit(int damage){ hp-=damage;} //damage player

    public void stopMove(){ moving = false; }
    public void startMove(){ moving = true; }
    public boolean collide(Rectangle rect){ //checks if player is colliding with any obstructions
        return hitbox.intersects(rect);
    }
    public boolean checkHit(Bullet b){ //checks if player has been hit by a bullet
        if (hitbox.contains(b.getPoint())){
            hit(b.getDmg()); //damages player
            return true;
        }
        return false;
    }
    public void act(){ //start acting
        acting=true;
        equipped.stopReload(); //cancel reload
        switching=false;
    }
    public void checkShoot(){ //checks when to display muzzle flash
        muzzleCooldown=Math.max(muzzleCooldown-muzzleSpeed,0);
    }
    public void stopAct(){
        acting=false;
    }
    public boolean canShoot(){ //checks if player can shoot
        return v==normalv&&!acting&&!equipped.isReloading()&&!switching; //not sprinting, acting, or switching
    }
    public void shoot(){ //shoot bullet
        muzzleCooldown =1; //muzzle cooldown activated so muzzle flash disappears after certain time
        equipped.shoot(); //shoot gun
        equipped.stopReload(); //if it was reloading, cancel
        switching=false;
    }
    public boolean canReload(){
        return v==normalv&&!acting&&!switching&&equipped.canReload(); //checks if player is not sprinting switching or has used some of his ammo
        //if yes, can reload
    }
    public boolean canSwitch(){ //checks if player can switch guns
        return !acting&&!switching;
    }

    //accessors
    public Point getPoint(){ return new Point((int)x,(int)y); }
    public double getAng(){ return ang; }
    public double getSwitchProg(){ return switchprog; }
    public int getX(){ return (int) x-5; }
    public int getY(){ return (int) y-5; }
    public int getCenterX(){ return (int) x; }
    public int getCenterY(){ return (int) y; }
    public int getFrontX(){ return (int) fx; }
    public int getFrontY(){ return (int) fy; }
    public int getReinforcements(){ return reinforcements; }
    public int getHP(){ return hp; }
    public int getMaxHP(){ return maxhp; }
    public int getScore() { return score; }
    public int getKills() { return kills; }
    public int getTraps(){ return traps; }
    public Gun getEquipped(){ return equipped; }
    public boolean hasMuzzle(){ return muzzleCooldown>0; }
    public boolean isSwitching(){ return switching; }
    public boolean isAnimating(){ return animating; }
    public boolean isMoving(){ return moving; }
    public boolean canReinforce(){ return reinforcements>0; }
    public boolean isActing(){ return acting; }
    public double getBulletAng(){ return ang+moveDev[spritePos]; } //gets what angle the bullet will come out at depending on the sprite
    public BufferedImage getSprite(){
        if(animating){
            if(acting) return actSprites[spritePos]; //returns sprites as animation progresses (acting)
            return moveSprites[spritePos]; //when moving return move sprites

        }
        return moveSprites[0]; //returns idle sprite
    }
    public BufferedImage getShootSprite(){ //when shooting, muzzle flash sprites returned
        if(animating) return shootSprites[spritePos];
        return shootSprites[0];
    }
    public Point getMuzzle(){ //getting muzzle position for where bullet will travel out of
        double offx=gunOffX[spritePos], offy=gunOffY[spritePos]; //gets the offset coordinates of the muzzle from the center of the player model
        double gunAng=Math.atan(offy/offx)+ang; //gets angle of gun
        double l= Math.hypot(offx,offy); //gets length of gun from center
        return new Point((int) (l*Math.cos(gunAng)+x) ,(int) (l*Math.sin(gunAng)+y)); //returns the point where the bullet will come out of
    }

    public Line2D getGunLine(){ //gunline used to see if player gun muzzle is colliding with any obstacles
        Point gunOff=getMuzzle();
        return new Line2D.Double(x,y,gunOff.getX(),gunOff.getY());
    }
}
