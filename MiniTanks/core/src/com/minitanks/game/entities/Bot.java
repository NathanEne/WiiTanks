package com.minitanks.game.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestNotMeRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.minitanks.game.states.PlayState;
import com.minitanks.world.MapGenerator;

public class Bot extends Tank {

    private int AIType;
    private Tank player;
    private boolean isActive = true;
    private boolean pendingTarget = false;
    private Vector3 moveDirection = new Vector3();
    private Vector3 gotoLoc;
    private ClosestNotMeRayResultCallback rays;


    public Bot(Turret t, TankBase tb, PlayState plst, Vector3 startingPos, boolean isAI, int aiType, Tank player){
        super(t, tb, plst, startingPos, true);
        this.AIType = aiType;
        this.player = player;
        rays = new ClosestNotMeRayResultCallback(getTankBase().getBody());
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Each tank will only display one type of behaviour
    public void playBehaviour(){
        if (player.isDead() || !isActive)
            return;

        switch (AIType){
            case 1:
                behaviour1();
                break;
            case 2:
                behaviour2();
                break;
        }
    }


    public void behaviour1(){
        /*
            A static tank that only rotates its turret and its shooting is modelled through an exponential distribution.
           (Increased probability of shooting as time without shooting continues).
         */


        super.move(new Vector3(0, 0, 0), new Vector3(player.getTankBase().getModelInstance().transform.getTranslation(new Vector3())));
        if (MapGenerator.randomNumber(0, 15) < 0.05f && rayTest2Tank()){
            super.Shoot();
        }
    }



    public void behaviour2(){
        /*
        Choose random points on the board and move to there.
        Will shoot at player randomly, increase probability of shooting as time continues.
        One reaches its desired point, choose a new point within the map.
         */
        Vector3 thisPos = super.getTankBase().getModelInstance().transform.getTranslation(new Vector3());


        if (!pendingTarget){
            // Pick a random location
            do{
                gotoLoc = new Vector3(MapGenerator.randomNumber(-5000, 5000) + thisPos.x, 0, MapGenerator.randomNumber(-10000, 10000) + thisPos.z);

            } while (Vector2.dst2(gotoLoc.x, gotoLoc.z, thisPos.x, thisPos.z) < 10000 && !rayTest2pos(gotoLoc));
            pendingTarget = true;
        }
        else {
            // Traverse to point
            moveDirection = new Vector3(gotoLoc).sub(new Vector3(thisPos));
            moveDirection = moveDirection.nor();
            super.move(moveDirection, new Vector3(player.getTankBase().getModelInstance().transform.getTranslation(new Vector3())));
            if (Vector2.dst2(gotoLoc.x, gotoLoc.z, thisPos.x, thisPos.z) < 100){
                pendingTarget = false;
            }
        }


        // Shoot at random
        if (MapGenerator.randomNumber(0, 15) < 0.05f && rayTest2Tank()){
            super.Shoot();
        }
    }


    /**
     *
     * @return true if there is a clear path to the tank
     */
    private boolean rayTest2Tank(){
        Vector3 rayFrom = this.getTankBase().getModelInstance().transform.getTranslation(new Vector3());
        Vector3 rayTo = new Vector3(this.player.getTankBase().getModelInstance().transform.getTranslation(new Vector3())).sub(rayFrom);

        rays.setCollisionObject(null);
        rays.setClosestHitFraction(1f);
        rays.setRayFromWorld(rayFrom);
        rays.setRayToWorld(rayTo);

        btCollisionWorld collisionWorld = super.getPlayState().getCollisionWorld();
        collisionWorld.rayTest(rayFrom, new Vector3(rayFrom).add(rayTo), rays);
        if (rays.hasHit()){
            Entity e = getPlayState().getEntities().get(rays.getCollisionObject().getUserValue());

            return e.id != 5 && !e.isAI();
        }
        return false;
    }

    /**
     *
     * @param position the spot to check, (AI wants to travel here)
     * @return true if there is a clear path to the specified position
     */
    private boolean rayTest2pos(Vector3 position){
        Vector3 rayFrom = this.getTankBase().getModelInstance().transform.getTranslation(new Vector3());
        Vector3 rayTo = new Vector3(position).sub(rayFrom);

        rays.setCollisionObject(null);
        rays.setClosestHitFraction(1f);
        rays.setRayFromWorld(rayFrom);
        rays.setRayToWorld(rayTo);

        btCollisionWorld collisionWorld = super.getPlayState().getCollisionWorld();
        collisionWorld.rayTest(rayFrom, new Vector3(rayFrom).add(rayTo), rays);
        if (rays.hasHit()){
            Entity e = getPlayState().getEntities().get(rays.getCollisionObject().getUserValue());

            return e.id != 5;
        }
        return true;
    }

}