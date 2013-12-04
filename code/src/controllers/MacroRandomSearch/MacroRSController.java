package controllers.MacroRandomSearch;

import framework.core.Controller;
import framework.core.Game;
import framework.graph.Graph;
import java.awt.*;

/**
 * PTSP-Competition
 * Sample controller based on macro actions and random search.
 * Created by Diego Perez, University of Essex.
 * Date: 17/10/12
 */
public class MacroRSController extends Controller {

    /**
     * Graph to do pathfining.
     */
    public static Graph m_graph;

    /**
     * TSP solver.
     */
    public static TSPBranchBound m_tspGraph;

    /**
     * Best route (order of waypoints) found to follow.
     */
    public int[] m_bestRoute;

    /**
     *   Current action in the macro action being execut
     */
    private int m_currentMacroAction;

    /**
     * Random Search engine to find the optimal macro-action to execute.
     */
    private RandomSearch m_rs;

    /**
     * Flag that indicates if the RS engine must be restarted (a new action has been decided).
     */
    boolean m_resetRS;

    /**
     *  Last macro action to be executed.
     */
    private int m_lastMacroAction;

    /**
     * Constructor of the controller
     * @param a_game Copy of the initial game state.
     * @param a_timeDue Time to reply to this call.
     */
    public MacroRSController(Game a_game, long a_timeDue)
    {
        m_resetRS = true;
        m_graph = new Graph(a_game);
        m_tspGraph = new TSPBranchBound(a_game, m_graph);
        m_rs = new RandomSearch();
        m_currentMacroAction = 10;
        m_lastMacroAction = 0;
        m_tspGraph.solve();
        m_bestRoute = m_tspGraph.getBestPath();
    }

    /**
     * Returns an action to execute in the game.
     * @param a_game A copy of the current game
     * @param a_timeDue The time the next move is due
     * @return
     */
    @Override
    public int getAction(Game a_game, long a_timeDue)
    {
        int cycle = a_game.getTotalTime();
        int nextMacroAction;

        if(cycle == 0)
        {
            //First cycle of a match is special, we need to execute any action to start looking for the next one.
            m_lastMacroAction = 0;
            nextMacroAction = m_lastMacroAction;
            m_resetRS = true;
            m_currentMacroAction = RandomSearch.MACRO_ACTION_LENGTH-1;
        }else
        {
            //advance the game until the last action of the macro action
            prepareGameCopy(a_game);
            if(m_currentMacroAction > 0)
            {
                if(m_resetRS)
                {
                    //search needs to be restarted.
                    m_rs.init();
                }
                //keep searching, but it is not time to retrieve the best action found
                m_rs.run(a_game, a_timeDue);
                //we keep executing the same action decided in the past.
                nextMacroAction = m_lastMacroAction;
                m_currentMacroAction--;
                m_resetRS = false;
            }else if(m_currentMacroAction == 0)
            {
                nextMacroAction = m_lastMacroAction; //default value
                //keep searching and retrieve the action suggested by the random search engine.
                int suggestedAction = m_rs.run(a_game, a_timeDue);
                //now it's time to execute this action. Also, in next cycle, we need to reset the search
                m_resetRS = true;
                if(suggestedAction != -1)
                    m_lastMacroAction = suggestedAction;

                if(m_lastMacroAction != -1)
                    m_currentMacroAction = RandomSearch.MACRO_ACTION_LENGTH-1;

            }else{
                throw new RuntimeException("This should not be happening: " + m_currentMacroAction);
            }
        }

        return nextMacroAction;
    }

    /**
     * Updates the game state using the macro-action that is being executed. It rolls the game up to the point in the
     * future where the current macro-action is finished.
     * @param a_game  State of the game.
     */
    public void prepareGameCopy(Game a_game)
    {
        //If there is a macro action being executed now.
        if(m_lastMacroAction != -1)
        {
            //Find out how long have we executed this macro-action
            int first = RandomSearch.MACRO_ACTION_LENGTH - m_currentMacroAction - 1;
            for(int i = first; i < RandomSearch.MACRO_ACTION_LENGTH; ++i)
            {
                //make the moves to advance the game state.
                a_game.tick(m_lastMacroAction);
            }
        }
    }

    /**
     * We are boring and we don't paint anything here.
     * @param a_gr Graphics device to paint.
     */
    public void paint(Graphics2D a_gr) {}

}
