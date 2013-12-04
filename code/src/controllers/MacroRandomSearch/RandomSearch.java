package controllers.MacroRandomSearch;

import framework.core.*;
import framework.graph.Node;
import framework.graph.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 * PTSP-Competition
 * Random Search engine. Creates random paths and determines which one is the best to execute according to an heuristic.
 * It keeps looking during MACRO_ACTION_LENGTH time steps. After that point, the search is reset.
 * Created by Diego Perez, University of Essex.
 * Date: 17/10/12
 */
public class RandomSearch
{
    /**
     * Best individual (route) found in the current search step.
     */
    public int[] m_bestRandomPath;

    /**
     * Best heuristic cost of the best individual
     */
    public double m_bestFitnessFound;

    /**
     * Next generated individual to be evaluated
     */
    public int[] m_currentRandomPath;

    /**
     * Random number generator
     */
    public Random m_rnd;

    /**
     * Next two waypoints in the route to pick up.
     */
    public static int[] m_nextPickups;

    /**
     * Current game state
     */
    public static Game m_currentGameState;

    /**
     * Game state used to roll actions and evaluate  the current path.
     */
    public static Game m_futureGameState;

    /**
     * Cache  for speeding up looks for nodes in the graph.
     */
    public static HashMap<Integer, Node> m_nodeLookup;


    /** NOW, SOME PARAMETERS **/

    /**
     * Number of macro-actions that form the random path.
     */
    public static int NUM_ACTIONS_INDIVIDUAL = 5;

    /**
     * Number of single actions that form a macro action.
     */
    public static int MACRO_ACTION_LENGTH = 8;

    /**
     * Heuristic cost parameter:points per waypoint.
     */
    public static final double SCORE_PER_WAYPOINT = 1000;

    /**
     * Heuristic cost parameter: points per fuel tank.
     */
    public static final double SCORE_PER_FUELTANK = 900;

    /**
     * Heuristic cost parameter: reduction factor over the damage suffered by the ship.
     */
    public static final double DAMAGE_REDUCTION_FACTOR = 0.25;


    /**
     * Constructor of the random search engine.
     */
    public RandomSearch()
    {
        m_rnd = new Random();
        m_nodeLookup = new HashMap<Integer, Node>();
        init();
    }

    /**
     * Initializes the random search engine. This function is also called to reset it.
     */
    public void init()
    {
        //Resetting the random paths found and best fitness.
        m_bestRandomPath = new int[NUM_ACTIONS_INDIVIDUAL];
        m_currentRandomPath = new int[NUM_ACTIONS_INDIVIDUAL];
        m_bestFitnessFound = -1;
    }

    /**
     * Runs the Random Search engine for one cycle.
     * @param a_gameState Game state where the macro-action to be decided must be executed from.
     * @param a_timeDue When this function must end.
     * @return  the action decided to be executed.
     */
    public int run(Game a_gameState, long a_timeDue)
    {
        m_currentGameState = a_gameState;
        updateNextWaypoints(2);
        double remaining = (a_timeDue-System.currentTimeMillis());

        //check that we don't overspend
        while(remaining > 10)
        {
            //create and evaluate a new random path.
            double randomPathFitness = createRandomPath(a_timeDue);

            //keep the best one.
            if(randomPathFitness > m_bestFitnessFound)
            {
                m_bestFitnessFound = randomPathFitness;
                System.arraycopy(m_currentRandomPath,0, m_bestRandomPath,0,NUM_ACTIONS_INDIVIDUAL);
            }
            //update remaining time.
            remaining = (a_timeDue-System.currentTimeMillis());
        }

        //take the best one so far, the best macroaction is the first one of the path.
        return m_bestRandomPath[0];
    }

    /**
     * Creates a new random path in m_currentRandomPath, watching for the limit time.
     * @param a_timeDue limit time to create the path.
     * @return score of the new path.
     */
    public double createRandomPath(long a_timeDue)
    {
        m_futureGameState = m_currentGameState.getCopy();
        boolean end = (a_timeDue-System.currentTimeMillis())<10;

        //Create and evaluate the path
        for(int i = 0; !end && i < m_currentRandomPath.length; ++i)
        {
            //Next macro action:
            m_currentRandomPath[i] = m_rnd.nextInt(Controller.NUM_ACTIONS);

            //Rollout macro-action in the game
            for(int j =0; !end && j < RandomSearch.MACRO_ACTION_LENGTH; ++j)
            {
                m_futureGameState.tick(m_currentRandomPath[i]);
                end = (a_timeDue-System.currentTimeMillis())<10;
            }
        }


        //At the end of the random path, return evaluation of the reached state.
        return RandomSearch.scoreGame();
    }

    /**
     * Updates  m_nextPickups, that indicates the next a_howMany waypoints to follow.
     * @param a_howMany number of waypoints to include in the search.
     */
    public static void updateNextWaypoints(int a_howMany)
    {
        m_nextPickups = null;
        try{

            //All my waypoints
            LinkedList<Waypoint> waypoints = m_currentGameState.getWaypoints();

            //Number of waypoints visited.
            int nVisited = m_currentGameState.getWaypointsVisited();
            if(nVisited != waypoints.size())
            {
                //Array with the next waypoints to visit, considering the case where there are less available.
                m_nextPickups = new int[Math.min(a_howMany, waypoints.size() - nVisited)];
                int pLength =  m_nextPickups.length; //number of elements to pick up.
                int bestPath[] = MacroRSController.m_tspGraph.getBestPath();

                //Go through the best path and check for what is collected.
                for(int i = 0, j = 0; j < pLength && i<bestPath.length; ++i)
                {
                    int key = bestPath[i];
                    if(!waypoints.get(key).isCollected())
                    {
                        //The first pLength elements not visited are selected.
                        m_nextPickups[j++] = key;
                    }

                }

            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Provides an heuristic score of the game state m_futureGameState.
     * @return the score.
     */
    public static double scoreGame()
    {

        int timeSpent = 0;
        double score = 0;
        if(m_nextPickups == null)
        {
            //All waypoints visited, reward for finished game.
            timeSpent = 10000 - m_futureGameState.getTotalTime();
            score = 10 * (m_futureGameState.getWaypointsVisited() * SCORE_PER_WAYPOINT + timeSpent);
        }else
        {
            //This is the normal case

            GameObject obj0 = null, obj1 = null;
            boolean obj0Collected = false, obj1Collected = false;

            //Next object supposed to be collected, that might have been collected since we started the random path.
            obj0 = m_futureGameState.getWaypoints().get(m_nextPickups[0]);
            obj0Collected = ((Waypoint)obj0).isCollected();

            //This is the path to the object we ned to collect
            Path pathToFirst = getPathToGameObject(m_futureGameState,obj0, m_nextPickups[0]);

            //Let's give some points for the distance to it
            double distancePoints = 0;
            if(m_nextPickups.length == 1)
            {
                //If it is the last waypoint, we just give scores for it.
                distancePoints = scoreDist(pathToFirst.m_cost);
            }else
            {
                //There are more waypoints after this one. Get that one.
                obj1 = m_futureGameState.getWaypoints().get(m_nextPickups[1]);
                obj1Collected = ((Waypoint)obj1).isCollected();

                //And give points to these distances.
                if(obj0Collected)
                {
                    double dist = m_futureGameState.getShip().s.dist(obj1.s);
                    distancePoints = scoreDist(dist) + SCORE_PER_WAYPOINT*10;
                    
                }else
                    distancePoints = scoreDist(pathToFirst.m_cost);

            }

            //Reward points for collecting waypoints.
            double waypointsPoints = 0;
            if(match(m_futureGameState.getVisitOrder(), MacroRSController.m_tspGraph.getBestPath()))
            {
                if(obj0Collected)
                    waypointsPoints = SCORE_PER_WAYPOINT;

                if(obj1 != null && obj1Collected)
                    waypointsPoints = SCORE_PER_WAYPOINT * 2;
            }

            //And some other points:
            double fuelPoints = m_futureGameState.getFuelTanksCollected() * SCORE_PER_FUELTANK;       //Points per fuel tanks.
            timeSpent = 10000 - m_futureGameState.getTotalTime();                                        //Points for time spent
            double damageTaken = -m_futureGameState.getShip().getDamage()*DAMAGE_REDUCTION_FACTOR; //Points for damage taken.

            //Sum up all the scores
            score = waypointsPoints + distancePoints + timeSpent + fuelPoints + damageTaken;

        }
        return score;
    }

    /**
     *  Given a distance, returns a score based on its distance.
     * @param a_dist  distance
     * @return heuristic score.
     */
    public static double scoreDist(double a_dist)
    {
        double estMaxDistance = 10000;
        double distancePoints = estMaxDistance - a_dist;
        distancePoints = Math.max(distancePoints,0);
        return distancePoints;
    }

    /**
     * Checks if the waypoint order followed so far matches the predifined route.
     * @param a_followedOrder Order followed so far.
     * @param a_pathDesired order of waypoints decided by the TSP solver.
     * @return true if the order followed matches a_pathDesired
     */
    public static boolean match(ArrayList<Integer> a_followedOrder, int[] a_pathDesired)
    {
        int idx = 0;
        for (Integer i : a_followedOrder)
        {
            if(i != a_pathDesired[idx])
                return false;
            idx++;
        }
        return true;
    }

    /**
     * Gets the path from the current location of the ship to the object passed as parameter.
     * @param a_game copy of the current game state.
     * @param a_gObj object ot get the path to.
     * @param a_objKey index of the object to look for.
     * @return the path from the current ship position to  a_gObj.
     */
    private static Path getPathToGameObject(Game a_game, GameObject a_gObj, int a_objKey)
    {
        //The closest node to the ship's location.
        Node shipNode = MacroRSController.m_graph.getClosestNodeTo(a_game.getShip().s.x, a_game.getShip().s.y);

        //The closest node to the target's location (checking the cache).
        Node objectNode = null;
        if(RandomSearch.m_nodeLookup.containsKey(a_objKey))
            objectNode = RandomSearch.m_nodeLookup.get(a_objKey);
        else{
            objectNode = MacroRSController.m_graph.getClosestNodeTo(a_gObj.s.x, a_gObj.s.y);
            RandomSearch.m_nodeLookup.put(a_objKey, objectNode);
        }

        //Get the parh between the nodes.
        return MacroRSController.m_graph.getPath(shipNode.id(), objectNode.id());
    }


}
