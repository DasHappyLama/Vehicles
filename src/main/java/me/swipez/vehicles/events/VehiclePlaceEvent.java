package me.swipez.vehicles.events;

import me.swipez.vehicles.vehicles.Vehicle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class VehiclePlaceEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;
    private Player player;
    private Vehicle vehicle;
    private Location location;

    public VehiclePlaceEvent(Player player, Vehicle vehicle, Location location) {
        this.player = player;
        this.vehicle = vehicle;
        this.location = location;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Location getLocation() {
        return location;
    }
}
