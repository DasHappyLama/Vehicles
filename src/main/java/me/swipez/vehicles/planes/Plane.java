package me.swipez.vehicles.planes;

import me.swipez.vehicles.vehicles.Vehicle;

public interface Plane extends Vehicle {

    void runDelayedActions();

    void fixBlades(double angle);

    void rotate(double angle);

    void rotateBlades(double angle);

    void update();

    boolean isDriveable();

    void setDriveable(boolean driveable);
}
