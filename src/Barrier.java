//Barrier.java
//objects on the map that can't be interacted with
//and act as obstacles to the player and enemies' movement
package com.company;
import java.awt.*;
//Barrier - class for static walls
public class Barrier {
    private final Rectangle hitbox; //barrier as a rectangle

    public Barrier(Rectangle hitbox){ //constructor
        this.hitbox=hitbox;
    } //constructor

    public Rectangle getHitbox() { //returns the barrier's rectangle
        return hitbox;
    } //accessor

    public boolean checkHit(Bullet b){ //takes a bullet and returns whether it's inside the barrier rectangle
        return hitbox.contains(b.getPoint());
    } //checks if bullet collides with wall
}
