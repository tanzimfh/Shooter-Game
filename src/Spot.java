package com.company;
//Spot class - used for enemy pathfinding - determines the enemy's next move
class Spot {
    //2 possible paths for the spot to point to - used for different enemies, if enemy can breach walls, breachable path taken, and vice versa.
    private Spot next, nextBreach; //next spot in the path
    private int direction, directionBreach; //direction for enemy to take
    private int x, y; //coordinates
    private boolean visited, visitedBreach; //whether the spot has been visited by the enemy or not (so no infinite pathfinding)
    private final boolean blocked, breachable; //if the spot is completely blocked or can be breached into

    public Spot(int x, int y, boolean blocked, boolean breachable){ //constructor
        this.breachable = breachable;
        this.blocked = blocked;
        if(!blocked) { //only makes spot valid if not blocked
            this.x = x;
            this.y = y;
            visited = false;
        }
    }
    public boolean noBreachAvailable() {
        return !visited && !blocked && !breachable; //checks path for non-breaching enemies
    }
    public boolean breachAvailable(){
        return !visitedBreach&&!blocked; //checks path for breaching enemies
    }
    public boolean isBlocked(){ return blocked; } //checks if spot is valid

    //setters and accessors
    public void setDirection(int dir){ //setting direction for non breach enemies
        direction=dir;
        visited=true; //makes spot visited so its no longer valid
    }
    public void setBreachDirection(int dir){ //direction for breachers
        directionBreach=dir;
        visitedBreach=true;
    }

    public void setNext(Spot n){ next=n; } //setting the next spot the current spot points to
    public void setNextBreach(Spot n){ nextBreach=n; }

    public Spot getNext(){ return next; }
    public Spot getNextBreach(){ return nextBreach; }
    public int getDirection(){ return direction; }
    public int getDirectionBreach(){ return directionBreach; }
    public int getX(){ return x; }
    public int getY(){ return y; }
}