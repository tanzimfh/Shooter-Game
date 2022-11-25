package com.company;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
//Gun class - used to create and utilise guns available to the player
public class Gun {
    private final String name; //gun name
    private String type; //type --> semiauto or full auto
    private final Image pic; //display picture for loadout selection
    private final int use, maxammo, damage, maxmag, v; //use (primary or sidearm), maximum ammo that can be carried for the gun, damage, maximum magazine size, bullet speed
    private int mag, ammo; //current magazine ammo, current total ammo
    private double reloadProg=0; //reloading progress
    private final double dev, rof, longSpeed, shortSpeed; //bullet deviation, rate of fire, long reload speed (when rechambering new round after unloading magazine), short reload speed
    private boolean reloading=false; //true when reloading progress takes place
    private final SoundEffect shortSound, longSound; //soundeffects
    private final LinkedList<SoundEffect> shotSounds=new LinkedList<>(); //gunshot sounds
    private final String[]stats; //gun stats

    public Gun(String [] stats){ //constructor
        this.stats=stats; //keeps full stats list to clone gun later
        use = Integer.parseInt(stats[0]);
        name = stats[1];
        type = stats[2];
        damage = Integer.parseInt(stats[3]);
        maxmag=Integer.parseInt(stats[4]);
        mag=maxmag+1;
        ammo=maxammo=maxmag*5; //5 full magazines of ammo
        v=(int) (Integer.parseInt(stats[5])*Siege.spf*300); //speed of bullet in pixels/frame, relative to fps
        rof = Double.parseDouble(stats[6])/(60*Siege.fps);
        dev = Double.parseDouble(stats[7]);
        pic = new ImageIcon(stats[8]).getImage();
        for(int i=0;i<50;i++) shotSounds.add(new SoundEffect("Assets/Sounds/"+name+"Shot.wav")); //each gun has unique sound, name used to get right file
        shortSpeed=Siege.spf/Double.parseDouble(stats[9]);
        shortSound=new SoundEffect("Assets/Sounds/"+name+"ShortReload.wav");
        longSpeed=Siege.spf/Double.parseDouble(stats[10]);
        longSound=new SoundEffect("Assets/Sounds/"+name+"LongReload.wav");
    }

    public void resetAmmo(){ //reset ammunition after game over
        mag=maxmag+1; //full magazine (mag size + 1 in chamber)
        ammo=maxammo; //reserve ammo filled
        reloadProg = 0; //resets progress
    }

    @Override
    public Gun clone(){ return new Gun(stats); }  //clone gun for enemy use

    public void resupply(){
        ammo = maxammo;
    } //resupply (used when player uses resupply box), reserve ammo filled

    public void shoot(){ //shoot gun
        shotSounds.getFirst().play(); //plays sound
        shotSounds.addLast(shotSounds.removeFirst());
        mag--; //removes one bullet from magazine
    }

    public void reload(){ //reload weapon
        if(ammo>0) {
            boolean empty = mag==0; //checks if magazine is completely empty to see if long reload needed
            if (!reloading) {
                reloading = true;
                if (empty) longSound.play(); //plays long reload sound if long reload needed
                else shortSound.play();
            }
            if (empty) reloadProg += longSpeed; //advancing reload progress
            else reloadProg += shortSpeed;
            if (reloadProg >= 1){ //reload finished
                int adding=Math.min(maxmag-mag+(empty?0:1), ammo); //adds full magazine to gun (+1 in chamber if not empty reload)
                mag+=adding;
                ammo-=adding; //removes ammo used to reload gun from reserve
                reloadProg = 0; //reset progress
                reloading = false;
            }
        }
    }

    public void stopReload(){ //cancel reload
        longSound.stop(); //stop sound effects
        shortSound.stop();
        reloading=false;
        reloadProg=0; //reset progress
    }

    public void switchType(){ //only for use on select-fire weapons, can fire in semi or auto mode
        if(type.equals("semi")&&use==1){ //can only set primary weapons to automatic fire
            type="auto";
        }
        else if(type.equals("auto")){
            type="semi";
        }
    }

    //accessors
    public int getUse(){ return use; }
    public int getMaxAmmo(){ return maxammo; }
    public int getDmg(){ return damage; }
    public int getMag(){ return mag; }
    public int getAmmo(){ return ammo; }
    public double getRate(){ return rof; }
    public double getDev(){
        if(use==1&&type.equals("semi")) return dev/2; //less recoil for semi auto fire
        return dev; //regular deviation
    }
    public int getV(){ return v; }
    public double getProg(){ return reloadProg; }
    public boolean isReloading(){ return reloading; }
    public boolean canReload(){ return mag<=maxmag; }
    public boolean isSemi(){ return type.equals("semi"); }
    public boolean hasBullets(){ return mag>0; }
    public String getType(){ return type; }
    public Image getPic(){ return pic; }
}
