package framework.core;

/**
 * This class contains some important constants for the game.
 * PTSP-Competition
 * Created by Diego Perez, University of Essex.
 * Date: 19/12/11
 */
public abstract class PTSPConstants
{
    /**
     * Delay between time steps, used for replays and human plays.
     * It is set to 16ms, what implies near 62fps (1000/16) = 62.5
     */
    public final static int DELAY = 16;

    /**
     * Time constant
     */
    public final static double T = 1.0;

    /**
     * This is the number of steps allowed until reaching the next waypoint.
     */
    private final static int STEPS_PER_WAYPOINT = 1000;

    /**
     * The velocity of the ship will be multiplied by this amount when colliding with a wall.
     */
    public final static double COLLISION_SPEED_RED = 0.25;

    /**
     * The velocity of the ship will be multiplied by this amount when colliding with a DAMAGE wall.
     */
    public final static double COLLISION_DAMAGE_SPEED_RED = 0.1;

    /**
     * The velocity of the ship will be multiplied by this amount when colliding with an ELASTIC wall.
     */
    public final static double COLLISION_ELASTIC_SPEED_RED = 0.9;

    /**
     * Time for the controller to be initialized.
     */
    private final static int INIT_TIME_MS = 100;        //To be multiplied by num. waypoints.

    /**
     * Time for the controller to provide an action every step.
     */
    public final static int ACTION_TIME_MS = 40;

    /**
     * If the controller spends more than TIME_ACTION_DISQ to reply with an action,
     * it gets disqualified from this game (getting 0 wp and getStepsPerWaypoints() time steps.)
     */
    public final static int TIME_ACTION_DISQ = ACTION_TIME_MS * 3;
    
    /**
     * Interval wait. Used to check for controller replies in some execution modes.
     */
    public static final int INTERVAL_WAIT=1;

    /**
     * Initial (and maximum) fuel for the ship.
     */
    public static final int INITIAL_FUEL = 5000;

    /**
     * Amount of fuel gained when a fuel tank is collected
     */
    public static final int FUEL_TANK_BOOST = 250;

    /**
     * Fuel reward for visiting a waypoint.
     */
    public static final int FUEL_WAYPOINT_REWARD = 50;

    /**
     * Time the ship is set to invulnerable after a collision.
     */
    public static final int INVULNERABLE = 50;

    /**
     * Maximum damage the ship can hold.
     */
    public static final int MAX_DAMAGE = 5000;

    /**
     * Collision type: no collision
     */
    public static final int NO_COLLISION_TYPE = 0;

    /**
     * Collision type: normal collision
     */
    public static final int NORMAL_COLLISION_TYPE = 1;

    /**
     * Collision type: extra damaging collision
     */
    public static final int DAMAGE_COLLISION_TYPE = 2;

    /**
     * Collision type: elastic collision
     */
    public static final int ELASTIC_COLLISION_TYPE = 3;


    /**
     * Damage suffered by the ship when colliding with a normal collision
     */
    public static final int DAMAGE_NORMAL_COLLISION = 10;

    /**
     * Damage suffered by the ship when colliding with a damage collision
     */
    public static final int DAMAGE_DAMAGE_COLLISION = 30;

    /**
     * Damage suffered by the ship when being on a lava surface.
     */
    public static final int DAMAGE_LAVA = 1;



    /**
     * Returns the number of time steps until reaching the next waypoint
     * @param a_numWaypoints Number of waypoints of the map.
     */
    public static int getStepsPerWaypoints(int a_numWaypoints)
    {
        if(a_numWaypoints == 30)
            return 700;
        else if(a_numWaypoints == 40)
            return 550;
        else if(a_numWaypoints == 50)
            return 400;

        else return 800;
    }

    /**
     * Returns the number of time steps for initialization
     * @param a_numWaypoints Number of waypoints of the map.
     */
    public static int getStepsInit(int a_numWaypoints)
    {
        return INIT_TIME_MS * a_numWaypoints;
    }
}
