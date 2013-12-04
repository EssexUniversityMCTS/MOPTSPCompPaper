package framework.core;

import framework.utils.Vector2d;

import java.awt.*;

/**
 * This class represents the FuelTank object, that can be collected by the ship during the game.
 * PTSP-Competition
 * Created by Diego Perez, University of Essex.
 * Date: 19/12/11
 */
public class FuelTank extends GameObject
{
    /**
     * Indicates if this fuel tank has been collected in the game.
     */
    protected boolean collected;


    /**
     * Radius of the fuel tank.
     */
    public static int RADIUS = 4;

    /**
     * Color for the fuel tank
     */
    public static Color fuelTankColor = new Color(34,177,76);

    /**
     * Private constructor, used by getCopy();
     */
    private FuelTank()
    {
    }

    /**
     * Constructor of the FuelTank.
     * @param game Reference to the game.
     * @param s Position of the fuel tank in the map
     */
    public FuelTank(Game game, Vector2d s)
    {
        m_game = game;
        this.s = s;
        this.collected = false;
        this.radius = RADIUS;
    }

    /**
     * Function to be called every cycle.
     */
    public void update() {
        //Nothing to do here.
    }

    /**
     * Resets the fuel tank.
     */
    public void reset() {
        //Not in use, nothing to do here.
    }

    /**
     *  Draws the fuel tank.
     *  @param g Graphics object.
     */
    public void draw(Graphics2D g)
    {
        if(!collected)
        {
            g.setColor(fuelTankColor);

            int drawRadius = Ship.SHIP_RADIUS * radius;
            g.fillOval((int) (s.x - drawRadius*0.5),(int) (s.y - drawRadius*0.5),drawRadius,drawRadius+2);

            g.setColor(Color.black);
            g.drawOval((int) (s.x - drawRadius*0.5),(int) (s.y - drawRadius*0.5),drawRadius,drawRadius+2);
            g.drawLine((int) (s.x - drawRadius*0.5), (int) s.y, (int) (s.x + drawRadius*0.5), (int) s.y) ;

            g.setColor(Color.yellow);
            g.fillOval((int) (s.x - radius),(int) (s.y - radius),radius,radius);
        }
    }

    //

    /**
     * Check if this fuel tank is collected, given the position of the ship.
     * @param a_pos Position of the ship, or to be checked.
     * @param a_radius Radius of the ship (or distance from the point provided).
     * @return true if the fuel tank is collected.
     */
    public boolean checkCollected(Vector2d a_pos, int a_radius)
    {
        double xd = s.x - a_pos.x;
        double yd = s.y - a_pos.y;
        double d = Math.sqrt(xd*xd+yd*yd);

        return d<(a_radius+this.radius);
    }

    /**
     * Sets if the fuel tank is collected.
     * @param coll if the fuel tank is collected.
     */
    public void setCollected(boolean coll)
    {
        if(!collected)
        {
            collected = coll;
            m_game.fuelTankCollected();
        }
    }

    /**
     * Indicates if this fuel tank is already collected or not.
     * @return if this fuel tank has been already collected.
     */
    public boolean isCollected() {return this.collected;}


    /**
     *  Gets a copy of the fuel tank.
     *
     * @param a_game Reference to the game object.
     * @return A copy of the fuel tank.
     */
    public FuelTank getCopy(Game a_game)
    {
        FuelTank copied = new FuelTank();

        copied.s = this.s.copy();
        copied.v = this.v.copy();
        copied.ps = this.ps.copy();
        copied.d = this.d.copy();
        copied.m_game = a_game;
        copied.radius = this.radius;
        copied.collected = this.collected;

        return copied;
    }

    /**
     * Check if two fuel tank are the same
     * @param a_other the other fuel tank to check with this.
     * @return true if both fuel tanks are the same.
     */
    @Override
    public boolean equals(Object a_other)
    {
        FuelTank ft = (FuelTank) a_other;
        if(this.s.x == ft.s.x && this.s.y == ft.s.y)
            return true;
        return false;
    }

    /**
     * Hash code for this fuel tank.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return 100000*(100+(int)this.s.y) + (10000+(int)this.s.x);
    }

}
