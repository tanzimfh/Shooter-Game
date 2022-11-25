//GamePanel.java
//uses all other classes and contains most of the game's functions
package com.company;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.Timer;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    private GamePanel.State state = GamePanel.State.MENU; //GamePanel class - create new game object
    public final int RIGHT=0, DOWN=1, LEFT=2, UP=3; //directions
    private final boolean []keys=new boolean[KeyEvent.KEY_LAST+1]; //keys pressed
    private final Siege mainFrame; //JFrame panel will be in
    private Font siegeFont, siegeFont2, niceFont, niceFont2; //on screen fonts
    //background images
    private final Image bg1 = new ImageIcon("Assets/bg1.jpg").getImage().getScaledInstance(1280,720, Image.SCALE_SMOOTH);
    private final Image bg2 = new ImageIcon("Assets/bg2.png").getImage().getScaledInstance(1280,720, Image.SCALE_SMOOTH);
    private final Image bg3 = new ImageIcon("Assets/loadout.jpg").getImage().getScaledInstance(1280,720, Image.SCALE_SMOOTH);
    private final Image bg4 = new ImageIcon("Assets/pauseB.png").getImage();
    private final Image bg5 = new ImageIcon("Assets/errorB.png").getImage();
    private final Image bg6 = new ImageIcon("Assets/gameOver.png").getImage();
    private final Image bg7 = new ImageIcon("Assets/gameStats.jpg").getImage().getScaledInstance(1280,720, Image.SCALE_SMOOTH);
    private final Image bg8 = new ImageIcon("Assets/gameOver2.png").getImage();
    private final Image bg9 = new ImageIcon("Assets/help.png").getImage().getScaledInstance(1280,720, Image.SCALE_SMOOTH);
    private final Image victoryBg = new ImageIcon("Assets/victory.png").getImage();
    private final Image credBack = new ImageIcon("Assets/credits.jpg").getImage().getScaledInstance(1280,720, Image.SCALE_SMOOTH);
    private final Image playB = new ImageIcon("Assets/buttons/startB.png").getImage();
    private final Image creditB = new ImageIcon("Assets/buttons/creditB.png").getImage();
    private final Image quitB = new ImageIcon("Assets/buttons/quitB.png").getImage();
    private final Image helpB = new ImageIcon("Assets/buttons/helpB.png").getImage();
    private final Image prepB = new ImageIcon("Assets/buttons/prepB.png").getImage();
    private final Image backB = new ImageIcon("Assets/buttons/exit.png").getImage();
    private final Image startB = new ImageIcon("Assets/buttons/startGame.png").getImage();
    private final Image pauseB1 = new ImageIcon("Assets/buttons/pauseB1.png").getImage();
    private final Image pauseB2 = new ImageIcon("Assets/buttons/pauseB2.png").getImage();
    private final Image retryB = new ImageIcon("Assets/buttons/retry.png").getImage();
    private final Image replayB = new ImageIcon("Assets/buttons/replay.png").getImage();
    private final Image gameStatsB = new ImageIcon("Assets/buttons/gameStats.png").getImage();
    private final Image backToMenu = new ImageIcon("Assets/buttons/backtomenu.png").getImage();
    private final Image map = new ImageIcon("Assets/map.png").getImage();
    //icon images
    private final Image crosshair = new ImageIcon("Assets/crosshair.png").getImage().getScaledInstance(25,25,Image.SCALE_SMOOTH);
    private final Image infinity = new ImageIcon("Assets/infinity.png").getImage().getScaledInstance(70,35,Image.SCALE_SMOOTH);
    private final Image reinforceIcon = new ImageIcon("Assets/reinforce.png").getImage().getScaledInstance(50,50,Image.SCALE_SMOOTH);
    private final Image semiIcon = new ImageIcon("Assets/semi.png").getImage().getScaledInstance(30,30,Image.SCALE_SMOOTH);
    private final Image autoIcon = new ImageIcon("Assets/auto.png").getImage().getScaledInstance(30,30,Image.SCALE_SMOOTH);
    private final Image trapIcon = new ImageIcon("Assets/trapIcon.jpg").getImage().getScaledInstance(50,50,Image.SCALE_SMOOTH);
    private final Image ammoboxPic = new ImageIcon("Assets/objects/ammobox.png").getImage().getScaledInstance(25,16,Image.SCALE_SMOOTH);
    private final BufferedImage deadSprite = ImageIO.read(new File("Assets/sprites/enemyDead50.png")); //BufferedImage for rotation
    private final Image hostagePic = new ImageIcon("Assets/hostage.png").getImage().getScaledInstance(30,30,Image.SCALE_SMOOTH);
    private final Image hostageDownedPic = new ImageIcon("Assets/hostageDowned.png").getImage().getScaledInstance(30,30,Image.SCALE_SMOOTH);
    private final Image[]difficultyBs = {new ImageIcon("Assets/buttons/easy.png").getImage(),new ImageIcon("Assets/buttons/medium.png").getImage(),new ImageIcon("Assets/buttons/hard.png").getImage()};
    private final Rectangle[]difficultyRects = {new Rectangle(1000,150,150,36), new Rectangle(1000,200,150,36), new Rectangle(1000,250,150,36)};

    private Image currentBack; //current background
    private Image secondaryBack;

    private final Music currentTheme = new Music(); //background music

    private final Rectangle[] boundaryRects=genBounds(); //boundaries of building
    private ArrayList<deadSprite> deadSprites = new ArrayList<>(); //enemy dead bodies

    private final Barrier[]barriers=genBarriers("Assets/hardwalls.txt"); //unbreachable walls and other obstacles
    private final Wall[]walls=genSoftWalls("Assets/softwalls.txt"); //breachable walls
    private Door[] doors=genDoors();
    private Spot[][] grid = genGrid(); //grid on the map, used to find routes from enemies to the hostage
    private final Ellipse2D[] spawnAreas = {new Ellipse2D.Double(0,250,250,250), new Ellipse2D.Double(1200,250,250,250), new Ellipse2D.Double(1050,700,200,200)}; //enemy spawn areas
    private final Spot objSpot; //square in grid the hostage is in
    private ArrayList<Bullet> activeBullets=new ArrayList<>();
    private final ArrayList<Gun> enemyguns =new ArrayList<>(); //guns accessible by enemies
    private ArrayList<Enemy>enemies = new ArrayList<>();
    private Player player1;
    private Hostage obj; //objective, hostage
    private final Resupply ammobox; //resupply box
    private boolean canrev, prepPhase = true, ticking = false, prepTicking = false, canResupply;
    //canrev - player can revive hostage, prepPhase - in preparation phase, ticking - seconds going down, canResupply - player can use resupply box
    private Wall actablewall; //wall in range
    private Door actabledoor; //door in range
    private final Loadout l = new Loadout(); //player loadout
    private double actionbar=0, actcooldown =0, buttonCooldown = 0; //action progress between 0 and 1, cooldown between actions, cooldown between check for buttons
    private final double coolspeed=Siege.spf; //cooldown speeds relative to fps
    private double bulletcooldown=0, rof, reloadbar = 0, switchbar=0; //cooldown between shots, rate of fire of player's gun, reload progress, gun switch progress
    private boolean click=false;
    private int prepSeconds, deathSeconds = 10, wave = 1, waveCooldown; //seconds left in prep phase, seconds left before death, enemy wave number, cooldown between waves

    private final Cursor hidden = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB),new Point(0,0),"hidden cursor"); //blank cursor

    private Timer timer, deathTimer; //match and death timers

    //button rects
    private ArrayList <stateButton> activeButtons; //active buttons
    private final Rectangle backButton = new Rectangle(1100,30,143,47);
    private int offx, offy, mx, my; //offset between world and window coordinates, mouse location

    //announcer and explosion sound clips
    private final SoundEffect firstRound = new SoundEffect("Assets/sounds/announcer1.wav");
    private final SoundEffect playerDead = new SoundEffect("Assets/sounds/announcer2.wav");
    private final SoundEffect fortifyRoom = new SoundEffect("Assets/sounds/announcer3.wav");
    private final SoundEffect enemyZone = new SoundEffect("Assets/sounds/announcer4.wav");
    private final SoundEffect hostageRevived = new SoundEffect("Assets/sounds/announcer5.wav");
    private final SoundEffect enemiesDead = new SoundEffect("Assets/sounds/announcer7.wav");
    private final SoundEffect newWave = new SoundEffect("Assets/sounds/announcer8.wav");
    private final SoundEffect hostageDead = new SoundEffect("Assets/sounds/announcer9.wav");
    private final SoundEffect hostageDown1 = new SoundEffect("Assets/sounds/announcer6.wav");
    private final SoundEffect hostageDown2 = new SoundEffect("Assets/sounds/announcer10.wav");
    private final SoundEffect explode = new SoundEffect("Assets/sounds/explosion.wav");


    public GamePanel(Siege m) throws IOException {
        mainFrame = m;
        setSize(814,600);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        try { //creating fonts
            siegeFont = Font.createFont(Font.TRUETYPE_FONT, new File("Assets/fonts/Rainbow.ttf")).deriveFont(50f); //size 18
            siegeFont2 = Font.createFont(Font.TRUETYPE_FONT, new File("Assets/fonts/Rainbow.ttf")).deriveFont(33f);
            niceFont = Font.createFont(Font.TRUETYPE_FONT, new File("Assets/fonts/Roboto-Regular.ttf")).deriveFont(16f); //size 16
            niceFont2 = Font.createFont(Font.TRUETYPE_FONT, new File("Assets/fonts/VioletSans-Regular.ttf")).deriveFont(21f);
        } catch (FontFormatException | IOException e) { //catches if no text file present, doesn't crash
            e.printStackTrace();
        }
        player1 = new Player(606,300,300-50*Siege.difficulty);
        obj = new Hostage(485,352,300-50*Siege.difficulty);
        objSpot=grid[obj.getX()/10][obj.getY()/10]; //grid hostage occupies
        ammobox = new Resupply(592,266);
        timer = new Timer();
    }

    public enum State{ //current game state
        MENU, PREP, GAME, GAME_PAUSED, VICTORY, GAMEOVER_PLAYER, GAMEOVER_HOSTAGE, GAME_STATS, CREDITS, SELECTION, HELP, QUIT, ERROR
    }

    public Spot[][] genGrid(){ //creating squares on grid
        Spot[][] grid=new Spot[148][96]; //map is 1480x960, each square (Spot) is 10x10
        for(int x=0;x<148;x++){
            for(int y=0;y<96;y++){
                Rectangle square=new Rectangle(x*10+5,y*10+5,10,10); //area square takes
                Spot tmp = new Spot(x,y, blocked(square), breachable(square)); //spot on grid
                grid[x][y] = tmp;
                if(tmp.isBlocked()){ //if square is inaccessible by enemy
                    for(int i = -1; i < 2; i++){
                        for(int j = -1; j < 2; j++){ //make surrounding squares blocked to stop enemies from hugging walls
                            grid[x+i][y+j] = new Spot(tmp.getX()+(i*10), tmp.getY()+(i*10),true, false);
                        }
                    }
                }
            }
        }
        return grid;
    }

    public boolean breachable(Rectangle square){ //takes a rectangle and returns whether it intersects a breachable wall
        for(Wall w:walls)if (square.intersects(w.getHitbox())&&w.isIntact()&&!w.isReinforced()&&!w.isReinforcing()) return true;
        return false;
    }

    public boolean blocked(Rectangle square){ //takes a rectangle and returns whether it intersects an uninteractable object
        for(Barrier b: barriers) if(square.intersects(b.getHitbox())) return true;
        for(Wall w:walls) if (square.intersects(w.getHitbox()) && (w.isIntact()||w.isReinforced()||w.isReinforcing())) return true;
        return false;
    }

    public void genPaths(){ //uses breadth first search to give every spot on the grid a route to the hostage
        LinkedList<Spot> queue = new LinkedList<>(getNeighbors(objSpot,true)); //queue one - creates paths that can pass through breachable surfaces
        LinkedList<Spot> queue2 = new LinkedList<>(getNeighbors(objSpot,false)); //queue two - paths for normal enemies
        while(queue.size()!=0){ //continue through the queue until all paths completed
            queue.addAll(getNeighbors(queue.getFirst(),true)); //gets the neighbors of the first spot
            queue.removeFirst(); //removes it once its neighbors have been added
        }
        while(queue2.size() != 0){ //does same thing but for non breachable paths
            queue2.addAll(getNeighbors(queue2.getFirst(),false));
            queue2.removeFirst();
        }
    }

    public ArrayList<Spot> getNeighbors(Spot current, boolean breachable){ //generates neighboring spots around a current spot to see which ones are traversable
        ArrayList<Spot>neighbors=new ArrayList<>();
        int x=current.getX();
        int y=current.getY();
        Spot right=null,up=null,left=null,down=null;
        //checks the grid for all 4 possible spots (enemy can only traverse vertically and horizontally)
        if(x<147) right=grid[x+1][y];
        if(y>0) up=grid[x][y-1];
        if(x>0) left=grid[x-1][y];
        if(y<95) down=grid[x][y+1];

        //each spot has 2 paths it can point to, one that traverses through breachable obstacles and one that doesnt
        if(right!=null){
            if(breachable){ //checks if generating a path through breachable obstacles or not
                if(right.breachAvailable()){ //if generating breachable path, available spot is determined by different criteria (in spot class)
                    right.setBreachDirection(LEFT); //sets opposite direction because this program works its way back from the objective
                    right.setNextBreach(current); //sets the breach direction for the new spot and sets its next spot to current so path can be retraced
                    neighbors.add(right);
                }
            }
            else{ //non breachable
                if(right.noBreachAvailable()){
                    right.setDirection(LEFT); //sets the non breach direction for the spot
                    right.setNext(current); //sets next spot
                    neighbors.add(right);
                }
            }
        }
        //same thing for all other directions
        if(up!=null){
            if(breachable){
                if(up.breachAvailable()) {
                    up.setBreachDirection(DOWN);
                    up.setNextBreach(current);
                    neighbors.add(up);
                }
            }
            else{
                if(up.noBreachAvailable()){
                    up.setDirection(DOWN);
                    up.setNext(current);
                    neighbors.add(up);
                }
            }
        }
        if(left!=null){
            if(breachable){
                if(left.breachAvailable()) {
                    left.setBreachDirection(RIGHT);
                    left.setNextBreach(current);
                    neighbors.add(left);
                }
            }
            else{
                if(left.noBreachAvailable()){
                    left.setDirection(RIGHT);
                    left.setNext(current);
                    neighbors.add(left);
                }
            }
        }
        if(down!=null){
            if(breachable){
                if(down.breachAvailable()) {
                    down.setBreachDirection(UP);
                    down.setNextBreach(current);
                    neighbors.add(down);
                }
            }
            else{
                if(down.noBreachAvailable()){
                    down.setDirection(UP);
                    down.setNext(current);
                    neighbors.add(down);
                }
            }
        }
        return neighbors; //returns list of all neighbors
    }

    public Wall[] genSoftWalls (String filename) throws IOException{ //returns all softwalls in primitive array
        Scanner inFile = new Scanner(new BufferedReader(new FileReader(filename))); //load file
        int n=Integer.parseInt(inFile.nextLine()); //number of softwalls
        Wall[]walls=new Wall[n];
        for(int i=0;i<n;i++){
            String [] statList = inFile.nextLine().split(" "); //creating wall with line from text file
            walls[i]=new Wall(new Rectangle(Integer.parseInt(statList[0]), Integer.parseInt(statList[1]), Integer.parseInt(statList[2]), Integer.parseInt(statList[3])),i);
        }
        inFile.close();
        return walls;
    }

    public Barrier[] genBarriers(String filename) throws IOException{ //same as genSoftWalls()
        Scanner inFile = new Scanner(new BufferedReader(new FileReader(filename)));
        int n=Integer.parseInt(inFile.nextLine());
        Barrier[]walls=new Barrier[n];
        for(int i=0;i<n;i++){
            String [] statList = inFile.nextLine().split(" ");
            walls[i]=new Barrier(new Rectangle(Integer.parseInt(statList[0]), Integer.parseInt(statList[1]), Integer.parseInt(statList[2]), Integer.parseInt(statList[3])));
        }
        inFile.close();
        return walls;
    }

    public Door[] genDoors(){ //returns doors in []
        Door[]doors=new Door[7]; //creaing doors with their hitbox and whether they're barricaded
        doors[0]= new Door (new Rectangle(520, 575, 31, 3),  false); //inside doors start unbarricaded
        doors[1]= new Door (new Rectangle(739, 371, 61, 3),  false);
        doors[2]= new Door (new Rectangle(698, 625, 3,  30), false);
        doors[3]= new Door (new Rectangle(827, 630, 3,  30), false);
        doors[4]= new Door (new Rectangle(1032,556, 3,  47), true);
        doors[5]= new Door (new Rectangle(433, 684, 3,  48), true);
        doors[6]= new Door (new Rectangle(725, 854, 77, 3),  true);
        return doors;
    }

    public Rectangle[]genBounds(){ //returns rectangles that cover the inside of the building
        Rectangle[]bounds=new Rectangle[3];
        bounds[0]= new Rectangle(488,235,546,621);
        bounds[1]= new Rectangle(437,235,53,204);
        bounds[2]= new Rectangle(434,677,57,62);
        return bounds;
    }

    public void genEnemies(){ //adds enemies to ArrayList
        if (wave == 1){ //breaching capability and number of enemies depends on wave number
            for(int i=0;i<1;i++) enemies.add(randEnemy(true));
            for(int i=0;i<3;i++) enemies.add(randEnemy(false));
        }
        else if(wave == 2){
            for(int i=0;i<2;i++) enemies.add(randEnemy(true));
            for(int i=0;i<3;i++) enemies.add(randEnemy(false));
        }
        else if(wave == 3){
            for(int i=0;i<3;i++) enemies.add(randEnemy(true));
            for(int i=0;i<3;i++) enemies.add(randEnemy(false));
        }
        else if(wave == 4){
            for(int i=0;i<3;i++) enemies.add(randEnemy(true));
            for(int i=0;i<4;i++) enemies.add(randEnemy(false));
        }
    }

    public Enemy randEnemy(boolean special){ //returns a random enemy, takes whether it can breach walls
        int randNum = randint(0, 2); //random spawn area
        Ellipse2D randSpawn = spawnAreas[randNum];

        int x = randint((int)randSpawn.getX(),(int)(randSpawn.getX()+randSpawn.getWidth()))/10; //random spot in the area
        int y = randint((int)randSpawn.getY(),(int)(randSpawn.getY()+randSpawn.getHeight()))/10;

        double tmpAng; //angle enemy will be facing
        if(randNum == 0) tmpAng = 0; //is left spawm, face right
        else tmpAng = Math.PI; //vice versa

        Enemy e=new Enemy(x*10+5,y*10+5,tmpAng, 100, enemyguns.get(randint(0,enemyguns.size()-1)).clone(), special); //create enemy with random gun
        Spot spot=grid[x][y];
        e.setPath(updatePaths(spot,e)); //set enemy's path based on location
        return e;
    }

    public LinkedList<Integer> updatePaths(Spot s, Enemy e){ //takes an enemy and its spot and updates its path
        LinkedList<Integer>path=new LinkedList<>();
        while(s!=objSpot){ //while hostage isn't reached
            int dir; //direction to go
            if(e.isBreacher()){ //if enemy can breach
                dir=s.getDirectionBreach(); //follow direction for breacher
                s = s.getNextBreach();
            }
            else{
                dir = s.getDirection(); //if not breacher
                s = s.getNext(); //follow regular directions
            }
            for(int i=0;i<10;i++){
                path.addLast(dir); //10 of the same direction because enemy moves one pixel at a time
            }
        }
        return path;
    }

    public void loadStats(String fileName) throws IOException { //loads guns
        Scanner inFile = new Scanner(new BufferedReader(new FileReader(fileName))); //load text file
        int n=Integer.parseInt(inFile.nextLine().substring(0,1)); //number of guns
        for(int i=0;i<n;i++){
            String [] statList = inFile.nextLine().split(" ");
            Gun newGun = new Gun(statList); //create gun with stats
            if(newGun.getUse() == 1){ //if primary gun
                l.getpGuns().add(newGun); //add to both player and enemy arsenals
                enemyguns.add(newGun);
            }
            else l.getsGuns().add(newGun); //only player gets secondary guns
        }
        inFile.close();
    }

    public void loadButtons(){ //loads all the buttons in the menu
        activeButtons = new ArrayList<>();
        if(state == State.MENU){ //different buttons depending on state, each has location, size and image
            activeButtons.add(new stateButton(new Rectangle(535, 190, 212, 96), State.SELECTION, playB));
            activeButtons.add(new stateButton(new Rectangle(535, 320, 212, 96), State.CREDITS, creditB));
            activeButtons.add(new stateButton(new Rectangle(535, 450, 212, 96), State.QUIT, quitB));
        }
        else if(state == State.SELECTION){
            activeButtons.add(new stateButton(new Rectangle(535, 230, 212, 96), State.HELP, helpB));
            activeButtons.add(new stateButton(new Rectangle(535, 360, 212, 96), State.PREP, prepB));
        }
        else if(state == State.PREP) activeButtons.add(new stateButton(new Rectangle(950, 600, 294, 53), State.GAME, startB));
        else if(state == State.GAME_PAUSED){
            activeButtons.add(new stateButton(new Rectangle(475, 390, 110, 45), State.SELECTION, pauseB1));
            activeButtons.add(new stateButton(new Rectangle(675, 390, 110, 45), State.GAME, pauseB2));
        }
        else if(state == State.ERROR) activeButtons.add(new stateButton(new Rectangle(580, 340, 110, 45), State.PREP, pauseB2));
        else if(state == State.GAMEOVER_PLAYER || state == State.GAMEOVER_HOSTAGE){
            activeButtons.add(new stateButton(new Rectangle(465, 385, 110, 50), State.PREP, retryB));
            activeButtons.add(new stateButton(new Rectangle(660, 385, 167, 50), State.GAME_STATS, gameStatsB));
        }
        else if(state == State.VICTORY){
            activeButtons.add(new stateButton(new Rectangle(465, 385, 110, 50), State.PREP, replayB));
            activeButtons.add(new stateButton(new Rectangle(660, 385, 167, 50), State.GAME_STATS, gameStatsB));
        }
        else if(state == State.GAME_STATS) activeButtons.add(new stateButton(new Rectangle(930, 610, 300, 53), State.SELECTION, backToMenu));
    }

    public void setBg(){ //update background depending on state
        if(state == State.MENU) currentBack = bg1;
        else if(state == State.CREDITS)  currentBack = credBack;
        else if(state == State.SELECTION) {
            currentBack = bg2;
            l.cancel(); //cancel loadout if in selection menu
        }
        else if(state == State.PREP) currentBack = bg3;
        else if(state == State.GAME) currentBack = map;
        else if(state == State.GAME_STATS) currentBack = bg7;
        else if(state == State.HELP) currentBack = bg9;
    }

    public void setSecondBg(){ //update secondary background
        if(state == State.GAME_PAUSED) secondaryBack = bg4;
        else if(state == State.ERROR) secondaryBack = bg5;
        else if(state == State.GAMEOVER_HOSTAGE) secondaryBack = bg6;
        else if(state == State.GAMEOVER_PLAYER) secondaryBack = bg8;
        else if(state == State.VICTORY) secondaryBack = victoryBg;
    }

    public void checkQuit(){ //closes window if quit
        if(state == State.QUIT) System.exit(0);
    }

    public void reset() { //resets relevant variables to restart level
        for(Wall w : walls) w.reset(); //reset walls rather than loading again
        doors = genDoors();
        activeBullets = new ArrayList<>();
        prepSeconds = 45-5*Siege.difficulty;
        prepPhase = true;
        prepTicking = false;
        player1 = new Player(606,300,300-50*Siege.difficulty);
        obj = new Hostage(485,352,300-50*Siege.difficulty);
        enemies = new ArrayList<>();
        deadSprites = new ArrayList<>();
        genEnemies();
        l.cancel();
        wave = 1;
        for(Gun g : l.getpGuns()) g.resetAmmo();
        for(Gun g : l.getsGuns()) g.resetAmmo();
    }

    public void updateCursor(){ //updates cursor based on state
        if(state == State.GAME) mainFrame.setCursor(hidden); //no cursor in game
        else mainFrame.setCursor(Cursor.getDefaultCursor()); //normal cursor elsewhere
    }

    public void playMusic(){ //plays background music
        if(state == State.GAME || state == State.GAME_PAUSED){
            currentTheme.stop(); //no sound in game
        }
        else{ //specific sounds elsewhere
            if(state == State.PREP) currentTheme.playSong("Assets/music/theme2.wav");
            else if(state == State.MENU || state == State.SELECTION || state == State.CREDITS || state == State.HELP) currentTheme.playSong("Assets/music/theme1.wav");
            else if(state == State.GAMEOVER_PLAYER || state == State.GAMEOVER_HOSTAGE) currentTheme.playSong("Assets/music/defeat.wav");
            else if(state == State.VICTORY) currentTheme.playSong("Assets/music/victory.wav");
        }
    }

    public void enemyCollide(){ //checks for enemies sharing personal space
        for(Enemy e1:enemies) {
            for (Enemy e2 : enemies) {
                if (!e2.equals(e1)) { //if not same enemy
                    if (e1.getMovebox().intersects(e2.getHitbox().getBounds()) && e1.isMoving()) { //if sharing space and first enemy is moving
                        (e2.getPathSize()<e1.getPathSize()?e1:e2).stopMove(); //stop the enemy that's farther from hostage
                        //sorry this hurts to look at
                    }
                }
            }
        }
    }

    public void gameplay(){ //called every frame, runs the game
        setBg();
        setSecondBg();
        loadButtons();
        checkQuit();
        updateOffset(player1.getX(),player1.getY()); //update offset based on player location
        updateCursor();
        playMusic();

        buttonCooldown = Math.max(0, buttonCooldown - coolspeed); //lower button cooldown
        if(state == State.GAME) { //if game running
            grid = genGrid(); //update grid/path
            genPaths(); //required because map can be changed throughout the round

            if(!player1.isActing()) player1.face(mx-offx,my-offy); //player faces crosshair unless interacting
            player1.checkShoot(); //lowers shot cooldown

            if(enemies.size() == 0) nextWave(); //next wave if all enemies dead

            move(player1); //checks for movement keys
            checkAnimations();

            if(!prepPhase) { //enemies can move and shoot if not in prep phase
                enemyShoot();
                enemyMove();
                enemyCollide();
            }

            checkEnemyAct(); //check if enemies should be acting
            actcooldown = Math.max(0, actcooldown-coolspeed); //lower action cooldown
            updateActable(); //update interactable object variables
            if (keys[KeyEvent.VK_E]) checkPosActions("E"); //perform actions depending on key
            else if (keys[KeyEvent.VK_T]) checkPosActions("T");
            else stopActions();

            rof = player1.getEquipped().getRate(); //gun's rate of fire
            bulletcooldown = Math.max(0,bulletcooldown-rof); //lower shot cooldown
            playerShoot();

            for (Bullet b:activeBullets) {
                for (int i=0;i<b.getV();i++){ //# of pixels/frame
                    b.move(); //move one pixel
                    if(bulletHit(b)) {
                        b.deactivate(); //deactivate bullet if it hits something
                        break;
                    }
                }
            }
            activeBullets.removeIf(b -> !b.isActive()); //remove inactive bullets from list

            Gun gun=player1.getEquipped();
            if (player1.canReload()&&(keys[KeyEvent.VK_R]||gun.isReloading())){ //call reload() if already reloading or player presses R
                gun.reload();
            }
            reloadbar=gun.getProg(); //reload progress

            if ((player1.canSwitch()&&player1.getEquipped().getUse()==1?keys[KeyEvent.VK_2]:keys[KeyEvent.VK_1])||player1.isSwitching()){
                //switching guns, press 2 to switch to secondary, 1 to switch to primary
                player1.switchGuns(); //calls switchGuns() if already switching to make progress on switch
            }
            switchbar=player1.getSwitchProg();
            if (actcooldown==0&&player1.canSwitch()&&keys[KeyEvent.VK_V]){ //switching fire mode
                player1.getEquipped().switchType();
                actcooldown=1;
            }

            checkEnemySight(); //check if player has line of sight to player/hostage

            if(obj.isDead()) { //game over if hostage/player dies
                hostageDown1.stop();
                hostageDown2.stop();
                hostageDead.play();
                state = State.GAMEOVER_HOSTAGE;
            }
            else if(player1.isDead()){
                playerDead.play();
                state = State.GAMEOVER_PLAYER;
            }
            if(!prepTicking && prepPhase){ //start prep phase timer
                prepTicking = true;
                prepSeconds = 45-5*Siege.difficulty;
                setGameTimer();
            }
            else if(prepSeconds == 0 && prepTicking){ //end of prep phase
                firstRound.play();
                timer.cancel();
                prepPhase = false;
                prepTicking = false;
            }

            checkPlayerInBounds(); //check if player is inside building

            for(Enemy e : enemies){
                if(e.isDead()){ //add enemy dead body to map
                    deadSprites.add(new deadSprite(e.getX(), e.getY(), e.getAng()));
                    if(e.isActing()) e.cancelActions();
                }
            }
            enemies.removeIf(Enemy::isDead); //remove dead enemies from list
        }
        if(state != State.GAME && state != State.GAME_PAUSED && state != State.GAMEOVER_HOSTAGE && state != State.GAMEOVER_PLAYER){
            offx = 0; //reset offset in not in game
            offy = 0;
        }
        repaint();
    }

    public void checkPlayerInBounds(){ //starts death timers if player is outside building
        if(player1.collide(boundaryRects[0])||player1.collide(boundaryRects[1])||player1.collide(boundaryRects[2])){ //if player is inside building
            if(deathTimer!=null) deathTimer.cancel(); //cancel death timers
            ticking = false;
            deathSeconds = 10;
        }
        else {
            if(!ticking){ //start death countdown if outside
                deathCountdown();
                ticking = true;
            }
            if(deathSeconds <= 0){
                playerDead.play(); //player dies after 10 seconds outside
                state = State.GAMEOVER_PLAYER;
            }
        }
    }

    public void checkAnimations(){ //updates animating states of player and enemies
        if(player1.isMoving()) player1.animate(100); //player animations
        else if(player1.isActing()) player1.animate(70);
        else if(player1.isAnimating()) player1.stopAnimation();

        for(Enemy e : enemies){ //enemy animations
            if(e.isMoving()) e.animate(100);
            else if(e.isActing()) e.animate(70);
            else if (e.isAnimating()) e.stopAnimation();
        }
    }

    public void nextWave(){ //called at the end of waves
        if(wave == 4){ //end of 4th wave
            enemiesDead.play();
            state = State.VICTORY;
        }
        else{
            waveCooldown = 2; //2 seconds between waves
            TimerTask task = new TimerTask() {
                public void run() {
                    waveCooldown--;
                }
            };
            Timer tmp = new Timer();
            tmp.scheduleAtFixedRate(task,0,1000);
            if(waveCooldown <= 0){ //next wave, reset some variables
                tmp.cancel();
                deadSprites = new ArrayList<>();
                player1.regen(90-10*Siege.difficulty); //regen health, amount depends on difficulty
                wave++;
                newWave.play(); //announcing new wave
                genEnemies();
                prepPhase = true;
            }
        }
    }

    public void checkEnemyAct(){ //checks if enemy should be acting
        for(Enemy e: enemies){
            for(Door d : doors){
                if(d.getHitbox().intersects(e.getHitbox().getBounds()) && d.isBarricaded()){ //if enemy hits barricaded door
                    if(!e.isActing()){ //if not already acting
                        if(d.isActing()||d.isBarricading()||d.isTrapping()) { //if door is being interacted with
                            e.stopMove(); //stop moving
                        }
                        else {
                            e.act();
                            e.removeBarricade(d); //tear down barricade
                        }
                    }
                }
                if(d.getHitbox().contains(e.getBackX(),e.getBackY())&&d.isTrapped()) detonateTrap(d); //detonate trap if enemy hits it
            }
            for(Wall w : walls){
                if(w.getHitbox().contains(e.getFrontX(),e.getBackY())||w.getHitbox().intersects(e.getHitbox().getBounds())){ //if hits breachable wall
                    if(!e.isActing()&&w.isIntact()&&!w.isReinforced()&&!w.isReinforcing()&&e.isBreacher()) { //if enemy can breach wall
                        e.act(); //breach wall
                        e.breachWall(player1, w);
                    }
                }
            }
        }
    }

    public void detonateTrap(Door d){ //detonate trap on given door
        d.detonate();
        explode.play(); //explosion sound
        for(Enemy e : enemies){
            if(d.getRadius().contains(e.getCenterX(),e.getCenterY())){ //if enemy in blast radius
                double dist = Math.hypot(d.getCenterX()-e.getCenterX(), d.getCenterY()-e.getCenterY()); //distance between enemy and door
                e.hit(Math.max((int)(200-5*dist),0)); //deal damage to enemy based on distance
                if(e.isDead()){
                    player1.addScore(110);
                    player1.addKill();
                }
            }
        }
        double dist = Math.hypot(d.getCenterX()-player1.getCenterX(), d.getCenterY()-player1.getCenterY());
        player1.hit(Math.max((int)(200-5*dist),0)); //player takes damage too
    }

    public void playerShoot(){ //tries to shoot gun
        if (click&&player1.canShoot()) {
            if(player1.getEquipped().hasBullets()){ //if gun isn't empty
                if(bulletcooldown==0&&!gunCollide()) { //if not in shot cooldown and gun not intersecting an object
                    player1.shoot();
                    activeBullets.add(new Bullet(player1)); //create bullet
                    bulletcooldown = 1; //start cooldown
                    if (player1.getEquipped().isSemi()) click = false; //player must click again to shoot if gun is semi-auto
                }
            }
            else player1.getEquipped().reload(); //reload if attempting to shoot with 0 bullets in mag
        }
    }

    public void enemyShoot(){ //makes enemies shoot if they see hostage/player
        for(Enemy enemy: enemies){
            enemy.cooldown();
            if((enemy.seesPlayer()||enemy.seesHostage())&&enemy.canShoot()){ //if player/hostage in sight and can shoot
                if(enemy.getGun().hasBullets()){
                    activeBullets.add(new Bullet(enemy));
                    enemy.shoot();
                }
                else enemy.getGun().reload(); //reload if mag is empty
            }
        }
    }

    public void checkEnemySight(){ //updates whether enemies have line of sight to player/hostage
        outerloop: //label used to move onto next enemy
        for(Enemy enemy:enemies){
            Line2D line=new Line2D.Double(enemy.getPoint(),player1.getPoint()); //line from enemy to player
            for(Barrier b: barriers){
                if(line.intersects(b.getHitbox())){
                    enemy.loseEyes(player1); //lose eyes if any object blocks line of sight
                    continue outerloop; //next enemy
                }
            }
            for (Wall w:walls){
                if(line.intersects(w.getHitbox())&&(w.isIntact()||w.isReinforced())){
                    enemy.loseEyes(player1);
                    continue outerloop;
                }
            }
            for (Door d:doors){
                if(line.intersects(d.getHitbox())&&d.isBarricaded()){
                    enemy.loseEyes(player1);
                    continue outerloop;
                }
            }
            enemy.gainEyes(player1); //sees player if nothing blocking
        }

        outerloop: //almost identical to ^
        for(Enemy enemy:enemies){
            if(enemy.seesPlayer()) continue; //enemies prioritize shooting player, eyes on hostage is irrelevant when they see player
            Line2D line=new Line2D.Double(enemy.getPoint(),obj.getPoint());
            for(Barrier b: barriers){
                if(line.intersects(b.getHitbox())){
                    enemy.loseEyes(obj);
                    continue outerloop;
                }
            }
            for (Wall w:walls){
                if(line.intersects(w.getHitbox())&&(w.isIntact()||w.isReinforced())){
                    enemy.loseEyes(obj);
                    continue outerloop;
                }
            }
            for (Door d:doors){
                if(line.intersects(d.getHitbox())&&d.isBarricaded()){
                    enemy.loseEyes(obj);
                    continue outerloop;
                }
            }
            if(!obj.getRange().contains(enemy.getX(),enemy.getY())){
                enemy.loseEyes(obj);
                continue;
            }
            enemy.gainEyes(obj);
        }
    }

    public boolean gunCollide(){ //returns whether player's gun is inside an object and thus can't shoot
        Line2D gunLine=player1.getGunLine(); //line from player's center to tip of gun
        for(Barrier b: barriers) if(b.getHitbox().intersectsLine(gunLine)) return true; //blocking if any object intersects with line
        for(Wall w:walls) if((w.isReinforced()||w.isIntact())&&w.getHitbox().intersectsLine(gunLine)) return true;
        for(Door d:doors) if(d.isBarricaded()&&d.getHitbox().intersectsLine(gunLine)) return true;
        return false;
    }

    public boolean bulletHit (Bullet b){ //returns whether given bullet is inside an object
        if (!new Rectangle(0,0,1480,960).contains(b.getPoint())) return true; //if outside map
        if (obj.checkHit(b)){
            if(obj.isDowned()){ //if hostage is downed
                if(b.isFriendly()){ //if bullet was shot by player
                    player1.addScore(-50); //-ve pts for dowing hostage
                    hostageDown1.play(); //friendly fire announcement
                }
                else hostageDown2.play(); //enemy fire announcement
            }
            else if(obj.isDead()&&b.isFriendly()) player1.addScore(-100); //-ve 100 if player kills hostage
            return true;
        }
        for (Enemy e:enemies){
            if(b.isFriendly()&&!prepPhase&&e.checkHit(b)){ //if player bullet hits enemy after prep phase
                if(e.isDead()){ //kill credit
                    player1.addKill();
                    player1.addScore(100);
                }
                return true;
            }
        }
        if (!b.isFriendly()) if (player1.checkHit(b)) return true; //if enemy bullet hits player
        for (Barrier barrier: barriers) if (barrier.checkHit(b)) return true;
        for (Wall w: walls) if (w.checkHit(b)) return true;
        for (Door d:doors) if (d.checkHit(b)) return true;
        return false; //not hit anything
    }

    public void deathCountdown(){ //starts timer when player goes outside
        enemyZone.play(); //announcement
        deathTimer=new Timer(); //10 second timer
        TimerTask task = new TimerTask() {
            public void run() {
                deathSeconds--; //deathSeconds starts at 10
            }
        };
        deathTimer.scheduleAtFixedRate(task, 1000, 1000); //trigger every second
    }

    public void setGameTimer() { //starts game timer, used in prep phase
        if(wave == 1) fortifyRoom.play(); //starting announcement
        timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                prepSeconds--;
            }
        };
        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    public void stopActions(){ //player stops acting
        actionbar = 0;
        player1.stopAct();
        obj.stopRev();
        for (Door door : doors) door.stopAct();
        for (Wall wall : walls) wall.stopReinforce();
        ammobox.stopResupply();
    }

    public void checkPosActions(String key){ //start player actions based on key pressed
        //note: player can't have more than one interactable object in range
        if(key.equals("E")) {
            if (canrev) { //revive hostage
                player1.act();
                obj.revive();
                actionbar = obj.getProgress();
                if (actionbar == 0) { //if revive done
                    player1.stopAct();
                    actcooldown = 1;
                    hostageRevived.play();
                    player1.addScore(50);
                }
            } else if (canResupply) { //replenishing ammo
                player1.act();
                ammobox.startResupply();
                actionbar = ammobox.getProg();
                if (actionbar == 0) {
                    player1.stopAct();
                    actcooldown = 1;
                    player1.resupply();
                }
            } else if (actabledoor != null&&!actabledoor.isTrapping()) {
                player1.act();
                actabledoor.barricade();
                actionbar = actabledoor.getProgress();
                if (actionbar == 0) {
                    actcooldown = 1;
                    player1.stopAct();
                    if (actabledoor.isBarricaded()) player1.addScore(5);
                }
            } else if (actablewall != null) { //reinforce wall
                player1.act();
                actablewall.reinforce();
                actionbar = actablewall.getProgress();
                if (actionbar == 0) {
                    player1.stopAct();
                    player1.useReinforcement();
                    actcooldown = 1;
                    player1.addScore(20);
                }
            }
        }

        if(actabledoor!=null&&key.equals("T")&&player1.getTraps()>0&&!actabledoor.isBarricading()){ //trapping door
            player1.act();
            actabledoor.trap();
            actionbar = actabledoor.getProgress();
            if(actionbar == 0){ //if done
                player1.stopAct();
                if(actabledoor.isTrapped()) player1.useTrap(); //use up trap
                else player1.addTrap();
                actcooldown = 1;
                if(actabledoor.isTrapped()) player1.addScore(10);
            }
        }
    }

    public void updateActable(){ //updates interactable objects in range of player
        if (actcooldown==0) {
            canrev = obj.isDowned() && obj.inRange(player1); //can revive hostage
            canResupply = ammobox.hasSupply() && ammobox.getActionbox().contains(player1.getFrontX(),player1.getFrontY()) && player1.canResupply();
            if (canrev||canResupply) actabledoor = null; //can't act on door if reviving or resupplying
            else {
                actabledoor = findActableDoor();
                actablewall = actabledoor==null?findActableWall():null; //find wall if no door in range
            }
        }
        else{ //can't act if in cooldown
            canrev=false;
            actabledoor=null;
            actablewall=null;
            canResupply=false;
        }
    }

    public Door findActableDoor() { //returns actable door in range of player
        for(Door door : doors) if(door.inRange(player1)&&!player1.collide(door.getHitbox())) return door; //if player is in range and not inside door
        return null;
    }

    public Wall findActableWall(){ //returns actable wall in range
        if(player1.canReinforce()) for(Wall w:walls) if(w.isActable()&&w.inRange(player1)&&!player1.collide(w.getHitbox())) return w;
        return null;
    }

    public void enemyMove(){ //moves enemies
        for(Enemy e : enemies){
            e.setPath(updatePaths(grid[e.getX()/10][e.getY()/10],e)); //update path
            e.move();
        }
    }

    public void move (Player player){ //attempts to move player
        if (!player.isActing()) { //can't move if acting
            if (keys[KeyEvent.VK_SHIFT]) player.sprint(); //hold shift to sprint
            else player.normalSpeed();
            double ang=0; //angle to move
            int count=0; //# of keys pressed
            if (keys[KeyEvent.VK_W]){
                ang+=Math.PI/2; //up
                count++;
            }
            if (keys[KeyEvent.VK_S]){
                ang+=3*Math.PI/2; //down
                count++;
            }
            if (keys[KeyEvent.VK_D]){
                count++; //right
                if (keys[KeyEvent.VK_S]) ang += 2*Math.PI; //if pressing down add 2pi to average properly
            }
            if (keys[KeyEvent.VK_A]){
                ang+=Math.PI; //left
                count++;
            }
            if(!(keys[KeyEvent.VK_W]&&keys[KeyEvent.VK_S])&&!(keys[KeyEvent.VK_D]&&keys[KeyEvent.VK_A])&&count>0){
                //move if pressing 1 or 2 keys and not opposing motion keys
                player1.startMove();
                ang/=count; //average angle
                if(!tryMove(ang)) if(!tryMove(ang-Math.PI/4)) tryMove(ang+Math.PI/4); //if can't move to desired angle, attempt moving 45 degrees to either side
            }
            else player1.stopMove();
        }
    }

    public boolean tryMove(double ang){ //tries to move player towards given angle and returns whether move was possible
        player1.walk(ang); //move
        if(player1.collide(obj.getHitbox().getBounds())){ //if intersects with hostage
            player1.walk(ang+Math.PI); //move back
            player1.stopMove();
            return false;
        }
        for(Barrier b : barriers){ //repeat for all other objects
            if(player1.collide(b.getHitbox())){
                player1.walk(ang+Math.PI);
                player1.stopMove();
                return false;
            }
        }
        for(Wall wall: walls){
            if(wall.isIntact()|| wall.isReinforced()){
                if(player1.collide(wall.getHitbox())){
                    player1.walk(ang+Math.PI);
                    player1.stopMove();
                    return false;
                }
            }
        }
        for(Door door:doors){
            if(door.isBarricaded()){
                if(player1.collide(door.getHitbox())){
                    player1.stopMove();
                    player1.walk(ang+Math.PI);
                    return false;
                }
            }
        }
        if(prepPhase){ //can't go outside in prep phase
            if(!player1.collide(boundaryRects[0]) && !player1.collide(boundaryRects[1]) && !player1.collide(boundaryRects[2]) && !ticking){
                player1.stopMove();
                player1.walk(ang+Math.PI);
                return false;
            }
        }
        return true;
    }

    public void updateOffset(int x, int y){ //takes player's locations and updates screen-game offset
        if(x<640) offx=0; //no horizontal offset if player is on left side of screen
        else offx=Math.max(640-x, -205); //offset keeps player in the middle until right side of map hits right side of screen
        if(y<360) offy=0; //same logic for vertical offset
        else offy=Math.max(360-y, -244);
    }

    //methods needed to run window
    public void addNotify() {
        super.addNotify();
        requestFocus();
        mainFrame.start();
    }

    public void mouseClicked(MouseEvent e) {
        for(stateButton i : activeButtons){ //checking mouse hovering over buttons
            if(i.rect.contains(e.getX(),e.getY()) && buttonCooldown == 0){
                if(state == State.PREP){ //if button leads to prep phase
                    if(checkLoadout(l)){ //if proper loadout has been selected
                        player1.setLoadout(l);
                        state = State.GAME; //start game
                    }
                    else state = State.ERROR;
                }
                else state = i.state;

                if(i.state==State.SELECTION||i.state==State.PREP){ //if going into selection or prep phase
                    reset(); //reset variables
                }
                buttonCooldown = 0.3;
            }
        }
        if(backButton.contains(e.getX(),e.getY()) && buttonCooldown == 0){ //back button
            if(state==State.SELECTION||state==State.CREDITS) state = State.MENU;
            else if(state == State.PREP || state == State.HELP){
                l.cancel();
                state = State.SELECTION;
            }
            buttonCooldown = 0.3;
        }
    }

    public boolean checkLoadout(Loadout l){ //returns whether given loadout has a primary and secondary
        return l.getP()!=null&&l.getS()!=null;
    }

    public void mousePressed(MouseEvent e) { click=true; }

    public void mouseReleased(MouseEvent e) {
        click=false;
        if(state == State.PREP){
            for(Loadout.Button i : l.getPBs()){ //selecting primary guns
                if(i.getRect().contains(e.getX(),e.getY())){
                    l.setPrimary(i.getGun());
                }
            }
            for(Loadout.Button i : l.getSBs()){ //secondary
                if(i.getRect().contains(e.getX(),e.getY())){
                    l.setSecondary(i.getGun());
                }
            }
            for(int i = 0; i < difficultyRects.length; i++){ //selecting difficulty
                if(difficultyRects[i].contains(e.getX(),e.getY())){
                    mainFrame.setDifficulty(i+2);
                }
            }
        }
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    //gamePlay method - calls all the methods needed to run the game
    //getting keyboard input
    public void keyTyped(KeyEvent e) {
        if(state == State.GAME&&keys[KeyEvent.VK_ESCAPE]) state = State.GAME_PAUSED; //esc to pause game
    }

    public void keyPressed(KeyEvent e) { //update keys pushed down
        keys[e.getKeyCode()] = true;
    }
    public void keyReleased(KeyEvent e) { //keys released
        keys[e.getKeyCode()] = false;
    }
    public void mouseMoved(MouseEvent evt){
        mx = evt.getX();
        my = evt.getY();
    }
    public void mouseDragged(MouseEvent evt){
        mx = evt.getX();
        my = evt.getY();
    }

    public void paintComponent(Graphics g){//paint method - draws images needed for the game
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));

        //backgrounds
        if(state != State.GAME_PAUSED && state != State.ERROR && state != State.GAMEOVER_HOSTAGE && state != State.GAMEOVER_PLAYER && state != State.VICTORY) g.drawImage(currentBack,offx,offy,null);
        else{
            if(state == State.GAME_PAUSED) g.drawImage(secondaryBack, 450, 220, null);
            else if(state == State.GAMEOVER_HOSTAGE || state == State.GAMEOVER_PLAYER || state == State.VICTORY) g.drawImage(secondaryBack, 430, 220, null);
            else g.drawImage(secondaryBack, 450, 265,null);
        }

        //back button
        if(state == State.SELECTION || state == State.HELP || state == State.PREP || state == State.CREDITS) {
            g.drawImage(backB, 1100, 30, null);
            if (backButton.contains(mx, my)) g2d.draw(backButton);
        }

        if(state == State.GAME) {
            g2d.setFont(niceFont2);
            g2d.setColor(Color.ORANGE); //orange bullets
            for(Bullet b:activeBullets) g2d.fillOval(b.getX()+offx,b.getY()+offy,3,3);

            g.drawImage(obj.isDowned()?hostageDownedPic:hostagePic,obj.getCenterX()-10+offx,obj.getCenterY()-15+offy,null); //hostage image

            for(deadSprite d : deadSprites){ //dead bodies
                AffineTransform bodyRotate = new AffineTransform();
                bodyRotate.rotate(d.ang,37,16); //rotate image to match orientation enemy died in
                AffineTransformOp bRotOp = new AffineTransformOp(bodyRotate, AffineTransformOp.TYPE_BILINEAR);
                g2d.drawImage(deadSprite,bRotOp,d.x-28+offx,d.y-11+offy);
            }

            for (Wall w : walls) {
                if (w.isIntact()) g.drawImage(w.getIntactPic(),offx,offy,null); //separate images for intact/reinforced/explosion
                if (w.isReinforced()) g.drawImage(w.getReinforcePic(),offx,offy,null);
                if (w.isAnimating()) g.drawImage(w.getSprite(),(int) w.getHitbox().getX()-20 + offx,(int) w.getHitbox().getY()-20 + offy,null);
            }

            g2d.setColor(new Color(155,94,66)); //wood color barricade
            for (Door d : doors) {
                if (d.isBarricaded()) {
                    Rectangle hitbox = d.getHitbox();
                    g2d.drawRect((int) hitbox.getX() + offx, (int) hitbox.getY() + offy, (int) hitbox.getWidth(), (int) hitbox.getHeight()); //barricade
                }
                if(d.isAnimating()){
                    g.drawImage(d.getSprite(),(int) d.getHitbox().getX()-10 + offx,(int) d.getHitbox().getY()-15 + offy,null); //trap explosion sprite
                }
            }

            g.drawImage(ammoboxPic,(ammobox.getX()-5)+offx,(ammobox.getY()-5)+offy,null); //resupply box

            AffineTransform rot = new AffineTransform(); //player
            rot.rotate(player1.getAng(),37,16); //rotate to match angle facing
            AffineTransformOp rotOp = new AffineTransformOp(rot, AffineTransformOp.TYPE_BILINEAR);
            g2d.drawImage(player1.getSprite(), rotOp,player1.getX()-28+offx, player1.getY()-11+offy); //sprite frame
            if(player1.hasMuzzle()) g2d.drawImage(player1.getShootSprite(), rotOp,player1.getX()-28+offx, player1.getY()-11+offy); //muzzle flash

            for(Enemy e : enemies){ //same as ^
                AffineTransform enemyRot = new AffineTransform();
                enemyRot.rotate(e.getAng(),37,16);
                AffineTransformOp eRotOp = new AffineTransformOp(enemyRot, AffineTransformOp.TYPE_BILINEAR);
                g2d.drawImage(e.getCurrentSprite(),eRotOp,e.getX()-28+offx, e.getY()-11+offy);
                if(e.hasMuzzle()) g2d.drawImage(e.getShootSprite(), eRotOp,e.getX()-28+offx, e.getY()-11+offy);
            }
            g2d.setColor(Color.WHITE);
            if (actcooldown == 0) { //action bar and context text clues
                if (actionbar > 0) {
                    g2d.drawRect(170, 40, 190, 15); //action bar outline
                    g2d.fillRect(170, 40, (int) (190 * actionbar), 15); //actual bar
                }
                if (canrev) g2d.drawString("Hold E to revive hostage", 60, 610);
                else if(canResupply) g2d.drawString("Hold E to resupply, " + ammobox.getSupply() + " supply left.", 60, 610);
                else if (actabledoor != null){
                    if (actabledoor.isBarricaded()) g2d.drawString("Hold E to remove barricade", 60, 610);
                    else g2d.drawString("Hold E to barricade", 60, 610);
                    if(!actabledoor.isTrapped()) g2d.drawString("Hold T to set trap", 60, 580);
                    else g2d.drawString("Hold T to remove trap", 60, 580);
                }
                else if (actablewall != null) g2d.drawString("Hold E to reinforce", 60, 610);
            }

            if(reloadbar > 0){ //reload bar on top of player
                int x=player1.getCenterX()-30+offx;
                int y=player1.getY()-20+offy;
                g2d.drawRect(x, y, 60, 10);
                g2d.fillRect(x, y, (int) (60*reloadbar), 10);
            }

            if(switchbar>0) g2d.fillRect(970, 15, (int) (player1.getEquipped().getPic().getWidth(null) * switchbar), 5); //gun switch progress bar
            g.drawImage(player1.getEquipped().getPic(), 970, 30, null); //equipped gun image
            g.drawImage(semiIcon, 1150, 130, null); //semi auto image
            if(player1.getEquipped().getType().equals("auto")) g.drawImage(autoIcon, 1150, 130, null); //full auto image
            g2d.setFont(siegeFont2);
            g2d.drawString(player1.getEquipped().getMag()+"/"+player1.getEquipped().getAmmo(),1150,120); //ammo count
            g.drawImage(crosshair,mx-crosshair.getWidth(null)/2,my-crosshair.getHeight(null)/2,null); //crosshair

            g2d.setFont(siegeFont);
            if(prepPhase){ //prep phase timer
                if(prepSeconds %60 >= 10) g2d.drawString(prepSeconds /60 + ":" + prepSeconds %60, 650 ,50);
                else g2d.drawString(prepSeconds /60 + ":0" + prepSeconds %60, 650 ,50); //add 0 before seconds if <10
            }
            else g.drawImage(infinity, 650,50,null); //no timer outside of prep phase

            if(ticking){
                g2d.setFont(niceFont2);
                g2d.setColor(Color.RED); //death timer in red
                if(deathSeconds%60 >= 10) g2d.drawString("You will die in: " + "0:" + deathSeconds%60, 60 ,610);
                else g2d.drawString( "You will die in: " + "0:0" + deathSeconds%60, 60 ,610);
            }

            g.drawImage(reinforceIcon, 50,630,null); //reinforcement icon
            g2d.setFont(siegeFont2);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(player1.getReinforcements()), 105, 665); //reinforcement count

            //health bar
            g2d.setStroke(new BasicStroke(5));
            g.drawRect(148, 642, 203,26);
            g2d.setColor(Color.BLACK);
            g.fillRect(150, 643, 200,25);
            double hpFraction=(double) player1.getHP()/player1.getMaxHP(); //player hp as a fraction of max hp
            g2d.setColor(new Color((int)((1-hpFraction)*255),(int) (hpFraction*255), 0)); //bar color changes according to hp
            g.fillRect(150,643,(int) (hpFraction*200),25);

            g.drawImage(trapIcon, 1096, 630, null);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Integer.toString(player1.getTraps()), 1160, 665);
            g2d.setColor(Color.WHITE);
        }

        else if(state == State.GAMEOVER_PLAYER){
            g2d.setColor(Color.BLACK);
            g.fillRect(150, 643, 200,25);
        }

        else if(state == State.PREP){
            l.select(g);
            g.setColor(Color.WHITE);
            for(Loadout.Button i : l.getPBs()) {
                if (i.getRect().contains(mx, my)) {
                    if(i.getGun() != l.getP()){
                        g2d.draw(i.getRect());
                    }
                }
            }
            for(Loadout.Button i : l.getSBs()) {
                if (i.getRect().contains(mx, my)) {
                    if (i.getGun() != l.getS()) {
                        g2d.draw(i.getRect());
                    }
                }
            }
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.WHITE);

            for(int i = 0; i < difficultyBs.length; i++){
                g.drawImage(difficultyBs[i],1000,150+50*i,null);
                if(Siege.difficulty == i+2 || difficultyRects[i].contains(mx,my)){
                    g2d.drawRect(1000,150+50*i,150,36);
                }
            }
        }

        //endgame stats
        else if(state == State.GAME_STATS){
            g2d.setFont(siegeFont);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Kills: " + player1.getKills(), 450,200);
            g2d.drawString("Score: " + player1.getScore(), 450,270);
            g2d.drawString("Waves Survived: " + wave, 450,340);
        }

        if(activeButtons != null) { //buttons
            for (stateButton i : activeButtons) {
                g.drawImage(i.bPic, (int) i.rect.getX(), (int) i.rect.getY(), null);
                if (i.rect.contains(mx, my)) g2d.draw(i.rect); //drawing outline when mouse hovers over button
            }
        }

        //in-game stats
        if(state == State.GAME || state == State.GAME_PAUSED){
            g2d.setFont(siegeFont2);
            g2d.drawString("Score: " + player1.getScore(), 40,40);
            g2d.drawString("Kills: " + player1.getKills(), 40,70);
            g2d.drawString("Wave " + wave, 40,100);
        }
    }

    static class Music{ //loads and plays music
        Clip c; //current clip
        String filename; //audio file
        public void setClip(String filename){ //takes audio file name and sets it as the clip
            this.filename = filename;
            try{
                File f = new File(filename); //load audio file
                AudioInputStream sound = AudioSystem.getAudioInputStream(f);
                c = AudioSystem.getClip();
                c.open(sound);
            } catch(Exception e){ System.out.println("error"); }
        }
        public void play(){ //plays clip
            c.setFramePosition(0);
            c.start();
        }
        public void loop(){ c.loop(c.LOOP_CONTINUOUSLY); } //puts clip on loop
        public void stop(){ //stops clip
            c.stop();
            c.close();
        }
        public void playSong(String f){ //play new clip
            if (!f.equals(filename)) { //if not same clip
                if(c != null) stop(); //stop current clip
                setClip(f); //set new clip as current clip
                play(); //play new clip
                loop(); //on loop
            }
        }
    }

    public static int randint (int low, int high){ //returns random int between 2 ints inclusive
        return (int) (Math.random()*(high-low+1)+low);
    }

    static class stateButton{ //buttons used to switch between states
        private final Rectangle rect; //area button occupies
        private final GamePanel.State state; //state button leads to
        private final Image bPic; //background picture of button
        public stateButton(Rectangle rect, GamePanel.State state, Image bPic){ //constructor, takes all 3 variables
            this.rect = rect;
            this.state = state;
            this.bPic = bPic;
        }
        //nested class therefore don't need get methods
    }

    static class deadSprite{ //stores location and orientation of dead body
        private final int x, y; //location
        private final double ang; //angle facing
        public deadSprite(int x, int y, double ang){ //takes all 3 variables
            this.x = x;
            this.y = y;
            this.ang = ang;
        }
    }
}