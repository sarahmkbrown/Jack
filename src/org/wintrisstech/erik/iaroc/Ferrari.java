package org.wintrisstech.erik.iaroc;

import android.os.SystemClock;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wintrisstech.irobot.ioio.IRobotCreateAdapter;
import org.wintrisstech.irobot.ioio.IRobotCreateInterface;
import org.wintrisstech.irobot.ioio.IRobotCreateScript;
import org.wintrisstech.sensors.UltraSonicSensors;

/**
 * A Ferrari is an implementation of the IRobotCreateInterface.
 *
 * @author Erik
 */
public class Ferrari extends IRobotCreateAdapter implements Runnable
{

    private static final String TAG = "Ferrari";
    private final UltraSonicSensors ultraSonicSensors;
    private final Dashboard dashboard;
    private static final int RED_BUOY_CODE = 248;
    private static final int GREEN_BUOY_CODE = 244;
    private static final int FORCE_FIELD_CODE = 242;
    private static final int BOTH_BUOY_CODE = 252;
    private static final int RED_BUOY_FORCE_FIELD_CODE = 250;
    private static final int GREEN_BUOY_FORCE_FIELD_CODE = 246;
    private static final int BOTH_BUOY_FORCE_FIELD_CODE = 254;
    /*
     * The maze can be thought of as a grid of quadratic cells, separated by
     * zero-width walls. The cell width includes half a pipe diameter on each
     * side, i.e the cell edges pass through the center of surrounding pipes.
     * <p> Row numbers increase northward, and column numbers increase eastward.
     * <p> Positions and direction use a reference system that has its origin at
     * the west-most, south-most corner of the maze. The x-axis is oriented
     * eastward; the y-axis is oriented northward. The unit is 1 mm. <p> What
     * the Ferrari knows about the maze is:
     */
    private final static int NUM_ROWS = 12;
    private final static int NUM_COLUMNS = 4;
    private final static int CELL_WIDTH = 712;
    /*
     * State variables:
     */
    private int speed = 300; // The normal speed of the Ferrari when going straight
    // The row and column number of the current cell. 
    private int row;
    private int column;
    private boolean running = true;
    private final static int SECOND = 1000; // number of millis in a second
    private int howFarWeHaveGone;
    private boolean weHaveBeenBumped = false;
    private int[] c = {60, 200};
    private int[] e = {64, 200};
    private int[] g = {67, 200};

    /**
     * Constructs a Ferrari, an amazing machine!
     *
     * @param ioio the IOIO instance that the Ferrari can use to communicate
     * with other peripherals such as sensors
     * @param create an implementation of an iRobot
     * @param dashboard the Dashboard instance that is connected to the Ferrari
     * @throws ConnectionLostException
     */
    public Ferrari(IOIO ioio, IRobotCreateInterface create, Dashboard dashboard) throws ConnectionLostException
    {
        super(create);
        ultraSonicSensors = new UltraSonicSensors(ioio);
        this.dashboard = dashboard;
    }

    /**
     * Main method that gets the Ferrari running.
     *
     */
    public void run()
    {
        try
        {
            song(1, c);
            song(2, e);
            song(3, g);
        } catch (ConnectionLostException ex)
        {
        }
        dashboard.log("Running ...");
        goForward(100, 100);
        while (true)
        {
            try
            {
                readSensors(SENSORS_GROUP_ID6);

                if (isBumpRight() && isBumpLeft())//forward
                {
                    playSong(1);
                    weHaveBeenBumped = true;
                    hitStraight();
                } else
                {
                    if (isBumpRight())
                    {
                        playSong(2);
                        weHaveBeenBumped = true;
                        hitRight();
                    }
                    if (isBumpLeft())
                    {
                        playSong(3);
                        weHaveBeenBumped = true;
                        hitLeft();
                    }
                }
            } catch (ConnectionLostException ex)
            {
            }

//                int previousValue = -1;
//                int currentValue = getInfraredByte();
//                if (previousValue != currentValue)
//                {
//                    previousValue = currentValue;
//                    dashboard.log("" + currentValue);
//                }
//                if (getInfraredByte() == RED_BUOY_CODE)
//                {
//                    driveDirect(60, 50);
//                    dashboard.log("red buoy" + currentValue);
//                }
//                if (getInfraredByte() == 255) //doesnt see
//                {
//                    driveDirect(-100, 100); // || getInfraredByte() != 255);                                problem here with spinning WHILE reading sensors
//                    dashboard.log("reserved" + currentValue);
//                    SystemClock.sleep(5000);
//                    driveDirect(100, 100);
//                }
//                if (getInfraredByte() == GREEN_BUOY_CODE)
//                {
//                    driveDirect(50, 60);
//                    dashboard.log("green buoy" + currentValue);
//                }
//                if (getInfraredByte() == BOTH_BUOY_CODE)
//                {
//                    driveDirect(50, 50);
//                    dashboard.log("red and green buoy" + currentValue);
//                }
//                if (getInfraredByte() == RED_BUOY_FORCE_FIELD_CODE)
//                {
//                    driveDirect(70, 60);
//                    dashboard.log("red buoy and force field" + currentValue);
//                }
//                if (getInfraredByte() == GREEN_BUOY_FORCE_FIELD_CODE)
//                {
//                    driveDirect(60, 70);
//                    dashboard.log("green buoy and force field" + currentValue);
//                }
//                if (getInfraredByte() == BOTH_BUOY_FORCE_FIELD_CODE)
//                {
//                    driveDirect(70, 70);
//                    dashboard.log("both buoy and force field" + currentValue);
//                }

        }
//        dashboard.log("Run completed.");
//        dashboard.log("Shutting down ...");
//        shutDown();
//        setRunning(false);
    }

    /**
     * To run this test, place the Ferrari in a cell surrounded by 4 walls. <p>
     * Note: The sensors draw power from the Create's battery. Make sure it is
     * charged.
     */
    private void testUltraSonicSensors()
    {
        dashboard.log("Starting ultrasonic test.");
        long endTime = System.currentTimeMillis() + 20 * SECOND;
        while (System.currentTimeMillis() < endTime)
        {
            try
            {
                ultraSonicSensors.readUltrasonicSensors();
            } catch (ConnectionLostException ex)
            {
                //TODO
            } catch (InterruptedException ex)
            {
                //TODO
            }
            SystemClock.sleep(500);
        }
        dashboard.log("Ultrasonic test ended.");
    }

    /**
     * Tests the rotation of the Ferrari.
     */
    private void testRotation()
    {
        dashboard.log("Testing rotation");
        try
        {
            turnAndGo(10, 0);
            SystemClock.sleep(500);
            turnAndGo(80, 0);
            SystemClock.sleep(80);
            turnAndGo(-90, 0);
            SystemClock.sleep(80);
            turnAndGo(180, 0);
            SystemClock.sleep(80);
            turnAndGo(-90, 0);
            SystemClock.sleep(80);
            turnAndGo(-180, 0);
            SystemClock.sleep(80);
            turnAndGo(180, 0);
            SystemClock.sleep(80);
        } catch (ConnectionLostException ex)
        {
        } catch (InterruptedException ex)
        {
        }

    }

    private void testStrobe()
    {
        dashboard.log("Starting strobe test.");
        long endTime = System.currentTimeMillis() + 2000 * SECOND;
        while (System.currentTimeMillis() < endTime)
        {
            try
            {
                ultraSonicSensors.testStrobe();
            } catch (ConnectionLostException ex)
            {
                //TODO
            }
            SystemClock.sleep(500);
        }
        dashboard.log("Strobe test ended.");
    }

    /**
     * Turns in place and then goes forward.
     *
     * @param angle the angle in degrees that the Ferrari shall turn. Negative
     * values makes clockwise turns.
     * @param distance the distance in mm that the Ferrari shall run forward.
     * Must be positive.
     */
    private void turnAndGo(int angle, int distance)
            throws ConnectionLostException, InterruptedException
    {
        IRobotCreateScript script = new IRobotCreateScript();
        /*
         * The Create overshoots by approx. 3 degrees depending on the floor
         * surface. Note: This is speed sensitive.
         */
        // TODO: Further tweaks to make the Ferrari make more precise turns.  
        if (angle < 0)
        {
            angle = Math.min(0, angle + 3);
        }
        if (angle > 0)
        {
            angle = Math.max(0, angle - 3);
        }
        if (angle != 0)
        {
            script.turnInPlace(100, angle < 0); // Do not change speed!
            script.waitAngle(angle);
        }
        if (distance > 0)
        {
            script.driveStraight(speed);
            script.waitDistance(distance);
        }
        if (angle != 0 || distance > 0)
        {
            script.stop();
            playScript(script.getBytes(), false);
            // delay return from this method until script has finished executing
        }
    }

    /**
     * Closes down all the connections of the Ferrari, including the connection
     * to the iRobot Create and the connections to all the sensors.
     */
    public void shutDown()
    {
        closeConnection(); // close the connection to the Create
        ultraSonicSensors.closeConnection();
    }

    //// Methods made public for the purpose of the Dashboard ////
    /**
     * Gets the left distance to the wall using the left ultrasonic sensor
     *
     * @return the left distance
     */
    public int getLeftDistance()
    {
        return ultraSonicSensors.getLeftDistance();
    }

    /**
     * Gets the front distance to the wall using the front ultrasonic sensor
     *
     * @return the front distance
     */
    public int getFrontDistance()
    {
        return ultraSonicSensors.getFrontDistance();
    }

    /**
     * Gets the right distance to the wall using the right ultrasonic sensor
     *
     * @return the right distance
     */
    public int getRightDistance()
    {
        return ultraSonicSensors.getRightDistance();
    }

    /**
     * Checks if the Ferrari is running
     *
     * @return true if the Ferrari is running
     */
    public synchronized boolean isRunning()
    {
        return running;
    }

    private synchronized void setRunning(boolean b)
    {
        running = false;
    }

    /**
     * *************************************************************************
     * Jack Super API
     * *************************************************************************
     */
    private void goForward(int leftWheelSpeed, int rightWheelSpeed)
    {
        try
        {
            driveDirect(leftWheelSpeed, rightWheelSpeed);
        } catch (ConnectionLostException ex)
        {
        }
    }

    private void goBackward(int leftWheelSpeed, int rightWheelSpeed)
    {
        try
        {
            driveDirect(leftWheelSpeed, rightWheelSpeed);
        } catch (ConnectionLostException ex)
        {
        }
    }

    private void goForward(int leftWheelSpeed,int rightWheelSpeed,int howFarWeWantToGo)
    {
        howFarWeHaveGone = howFarWeHaveGone + getDistance();
        dashboard.log("how far we have gone " + howFarWeHaveGone);
        if (howFarWeHaveGone >= howFarWeWantToGo)
        {
            howFarWeHaveGone = 0;
            weHaveBeenBumped = false;
            stop();
        }
    }

    private void goBackward(int leftWheelSpeed,int rightWheelSpeed,int howFarWeWantToGo)
    {
        howFarWeHaveGone = howFarWeHaveGone + getDistance();
        dashboard.log("how far we have gone " + howFarWeHaveGone);
        if (howFarWeHaveGone >= howFarWeWantToGo)
        {
            howFarWeHaveGone = 0;
            weHaveBeenBumped = false;
            goForward(leftWheelSpeed,rightWheelSpeed);
        }
    }

    private void stop()
    {
        try
        {
            driveDirect(0, 0);
        } catch (ConnectionLostException ex)
        {
        }
    }

    private void hitRight()
    {
        if(weHaveBeenBumped)
        {
            goBackward(-50, -100, 1000);// left wheel, right wheel
        }
    }

    private void hitLeft()
    {
        if (weHaveBeenBumped)
        {
            goBackward(-100, -50, 1000);
        }
    }

    private void hitStraight()
    {
            goBackward(-100, -100);
            SystemClock.sleep(1000);
            goForward(100, 100);
    }
}
