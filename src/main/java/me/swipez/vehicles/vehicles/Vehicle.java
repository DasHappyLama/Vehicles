package me.swipez.vehicles.vehicles;

import me.swipez.vehicles.VehicleType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface Vehicle {

    void setColor(String color);
    UUID getSeatUUID();
    UUID getOwner();
    String getColor();
    String getEnumName();
    Location getOrigin();
    UUID getUniqueId();

    VehicleType getVehicleType();

    void runDelayedActions();

    boolean isOwnedBy(Player player);

    void dye(String color);

    void remove(boolean clearId);

    void attemptSit(Player player);

    Entity getSeat();

    void rotate(double angle);


    void move(boolean negative, boolean modify);

    void update();
}
