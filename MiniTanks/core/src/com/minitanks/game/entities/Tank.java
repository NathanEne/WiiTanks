package com.minitanks.game.entities;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.minitanks.game.states.PlayState;

public class Tank extends Entity {
    private boolean isAI;
    private float movementSpeed = 30f;
    private float bulletSpeed = 51f;
    private int bulletsInPlay = 0;
    private int maxNumOfBullets = 5;
    private PlayState playState;
    private boolean canShoot = true;
    private boolean isDead = false;
    public void setNumberOfKills(int numberOfKills) {
        this.numberOfKills = numberOfKills;
    }

    // Essentially, the games score: how many AI tanks the player has killed
    private int numberOfKills = 0;

    // Number of frames past since last shot
    private int timeSinceLastShot = 0;

    // Number of frames allowed between successive shots
    private int shootThreshold = 20;

    // The vector to add to the tank base position for the turret position
    private Vector3 turretOffset = new Vector3(7,240,10);

    private TankBase tankBase;

    private Turret turret;

    /*
    Getters
     */

    public int getNumberOfKills() {return numberOfKills;    }

    public TankBase getTankBase() {
        return tankBase;
    }

    public Turret getTurret() {
        return turret;
    }

    public PlayState getPlayState() {
        return playState;
    }

    public boolean isDead() {
        return isDead;
    }

    /**
     *
     * @param t
     * @param tb
     * @param plst
     * @param startingPos
     * @param isAI
     * Constructor for tank:
     * Initializes:
     * it's turret and base
     * PlayState
     * Starting Pos (0,0,0) for user tank
     * If AI tank is controlled by AI algorithm
     *
     */

    public Tank(Turret t, TankBase tb, PlayState plst, Vector3 startingPos, boolean isAI) {
        this.turret = t;
        this.tankBase = tb;
        this.playState = plst;
        this.isAI = isAI;
        if (isAI)
            super.setAI(true);
        // Set the position
        getTankBase().getModelInstance().transform.set(startingPos, new Quaternion());
        getTurret().getModelInstance().transform.set(startingPos.add(turretOffset), new Quaternion());
        getTurret().getModelInstance().transform.scl(0.5f);
    }

    public void increaseBulletTime(){
        if (this.timeSinceLastShot > this.shootThreshold)
            this.canShoot = true;
        else
            this.timeSinceLastShot++;
    }


    /**
     * Apply the movement for the tank
     *
     * @param dirVector A normalized direction for the tank to travel on this frame.
     */
    public void move(Vector3 dirVector, Vector3 mouseInput) {



        if (turret == null || tankBase == null)
            return;


        // Make the turret face mouse position
        turret.rotateToMouse(mouseInput);


        if (!dirVector.isZero()){

            getTankBase().setDirection(dirVector);

            // Get current position
            Vector3 currentPos = getTankBase().getModelInstance().transform.getTranslation(new Vector3());

            // Rotate the turret
            getTankBase().getModelInstance().transform.setToRotation(dirVector, Vector3.X.scl(-1));

            // Bug: Must rotate by 90 degrees if going diagonal.
            if (dirVector.x != 0 && dirVector.z != 0){
                getTankBase().getModelInstance().transform.rotateRad(Vector3.Y, (float)Math.PI/2);
            }

            // Set back to the current position
            getTankBase().getModelInstance().transform.set(currentPos, getTankBase().getModelInstance().transform.getRotation(new Quaternion()));
        }

        // Actually translate the model
        getTankBase().getModelInstance().transform.trn(dirVector.nor().scl(movementSpeed));



        // Move the turret along with it
        Vector3 tankPos = getTankBase().getModelInstance().transform.getTranslation(new Vector3());
        getTurret().getModelInstance().transform.set(tankPos.add(turretOffset), getTurret().getModelInstance().transform.getRotation(new Quaternion()));
    }



    /**
     * @ Param: Mouse position
     * Instantiate a bullet in the tanks barrel and add the respective force on bullet
     */
    public void Shoot(){
        // Ensure that you can shoot
        if (!canShoot || bulletsInPlay >= maxNumOfBullets)
            return;

        //this.bulletsInPlay++;
        this.timeSinceLastShot = 0;
        this.canShoot = false;

        // Instantiate a bullet at tip of turret
        Vector3 turretPos = getTurret().getModelInstance().transform.getTranslation(new Vector3());
        Vector3 bulletStart = turretPos.add(new Vector3(getTurret().getCurrDirection()).scl(630));
        Bullets newBullet = new Bullets(playState.assets.createBulletModel(0,0,0), getTurret().getCurrDirection(), bulletSpeed,playState);
        newBullet.getModelInstance().transform.set(bulletStart.add(0,-200,0), getTurret().getModelInstance().transform.getRotation(new Quaternion()));

        playState.addEntityToCollisionAndMap(newBullet,true);
    }
}
