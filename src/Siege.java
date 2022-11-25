package com.company;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

//Siege.java
//Tanzim Hossain and Christopher Shih
//This program is our recreation of the popular tactical shooter game "Rainbow Six Siege" in top down shooter format. The objective of the game
//is to protect an asset from groups of attackers. Players have an arsenal of weapons at their disposal, as well as various other methods of keeping the attackers out,
//such as reinforcing walls and trapping doorways with bombs. The game requires players to be mindful of resources at their disposal, and gives them time to
//set up and prepare in between rounds.

public class Siege extends JFrame implements ActionListener{
    private final Timer myTimer; //timer to call gameplay functions per frame
    private final GamePanel game;
    public static final int fps=60;
    public static final double spf= 1.0/fps;
    public static int difficulty;

    public Siege() throws IOException {
        super("R6 Siege"); //title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setPreferredSize(new Dimension(1280,720)); //setting frame size
        pack();
        setDifficulty(2);
        myTimer = new Timer(1000/fps, this);
        //creating new jPanel object
        game = new GamePanel(this);
        //calling game functions
        game.loadStats("Assets/guns/stats.txt");
        game.genPaths();
        game.genEnemies();
        add(game); //adding jPanel
        setResizable(false);
        setVisible(true);
    }

    public void setDifficulty(int difficulty){
        Siege.difficulty = difficulty;
    }

    public void start(){
        myTimer.start(); //starting timer
    }
    public void actionPerformed(ActionEvent evt){ game.gameplay(); } //call gameplay functions

    public static void main(String[] arguments) throws IOException {
        new Siege();
    }
}
