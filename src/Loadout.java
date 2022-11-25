package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
//Loadout class - creates a graphics interface for players to choose what weapons they carry into the fight
public class Loadout{
    private Gun primary; //primary weapon
    private Gun secondary; //sidearm

    ArrayList <Gun> pGuns = new ArrayList<>(); //list of primary guns and sidearms
    ArrayList <Gun> sGuns = new ArrayList<>();
    ArrayList <Button> primaryBs = new ArrayList<>(); //list of buttons for sidearms and primaries
    ArrayList <Button> secondaryBs = new ArrayList<>();
    Image check = new ImageIcon("Assets/checkmark.png").getImage().getScaledInstance(25,25,Image.SCALE_SMOOTH);

    public void select(Graphics g){ //displays arsenal on screen
        g.drawImage(new ImageIcon("Assets/label1.jpg").getImage(), 425, 140,null); //labels for secondaries and primaries
        g.drawImage(new ImageIcon("Assets/label2.jpg").getImage(), 715, 140,null);
        loadButtons(); //loads buttons
        for(int i = 0; i < 4; i++){ //goes through each list of guns and draws their display picture
            g.drawImage(pGuns.get(i).getPic(), 420, 200 + 110*i, null);
            g.drawImage(sGuns.get(i).getPic(), 755, 190 + 100*i, null);
        }
        //places a checkmark next to selected primary and secondary weapons
        for(Button i : primaryBs){
            if(i.getGun() == primary){
                g.drawImage(check, (int) i.getRect().getX() + 210, (int) i.getRect().getY() + 60, null);
            }
        }
        for(Button i : secondaryBs){
            if(i.getGun() == secondary){
                g.drawImage(check, (int) i.getRect().getX() + 150, (int) i.getRect().getY() + 45, null);
            }
        }
    }

    public void loadButtons(){ //loading in buttons with guns
        for(int i = 0; i < 4; i++){
            primaryBs.add(new Button(new Rectangle(400, 190 + 112*i, 250, 100),pGuns.get(i))); //creates buttons for primary and sidearm weapons
            secondaryBs.add(new Button(new Rectangle(715, 190 + 100*i, 190, 80),sGuns.get(i)));
        }
    }
    //accessors
    public Gun getP(){ return primary; }
    public Gun getS(){ return secondary; }
    public ArrayList <Gun> getpGuns() { return pGuns; }
    public ArrayList <Gun> getsGuns() { return sGuns; }
    public ArrayList <Button> getPBs() { return primaryBs; }
    public ArrayList <Button> getSBs() { return secondaryBs; }
    //setters
    public void setPrimary(Gun p){ primary = p; }
    public void setSecondary(Gun s){ secondary = s; }

    public void cancel(){ //cancel loadout selection, resets all previously chosen weapons
        primary = null;
        secondary = null;
    }

    class Button{ //loadout button - button used to select weapons
        private Rectangle rect;
        private Gun gun;
        public Button(Rectangle rect, Gun gun){ //takes in the rectangle (button) and the gun the button will access
            this.rect = rect;
            this.gun = gun;
        }
        //accessors
        public Rectangle getRect(){ return rect; }
        public Gun getGun(){ return gun; }
    }
}
