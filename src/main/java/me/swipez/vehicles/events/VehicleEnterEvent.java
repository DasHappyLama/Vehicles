package me.swipez.vehicles.events;

import me.swipez.vehicles.vehicles.Vehicle;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class VehicleEnterEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;
    private Player player;
    private Vehicle vehicle;
    private boolean driverSeat;

    public VehicleEnterEvent(Player player, Vehicle vehicle, boolean driverSeat) {
        this.player = player;
        this.vehicle = vehicle;
        this.driverSeat = driverSeat;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
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

    public boolean isDriverSeat() {
        return driverSeat;
    }
}
