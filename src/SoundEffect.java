package com.company;

import javax.sound.sampled.*;
import java.io.File;
//SoundEffect class - for in-game sounds
public class SoundEffect{
    private Clip c; //clip
    public SoundEffect(String filename){
        setClip(filename);
    } //constructor
    public void setClip(String filename){
        try{ //sets the current clip to be played
            File f = new File(filename);
            c = AudioSystem.getClip();
            c.open(AudioSystem.getAudioInputStream(f));
        } catch(Exception e){ System.out.println("error"); }
    }
    public void play(){ //plays the clip
        c.setFramePosition(0); //starts from beginning
        c.start();
    }
    public void stop(){
        c.stop();
    }
}