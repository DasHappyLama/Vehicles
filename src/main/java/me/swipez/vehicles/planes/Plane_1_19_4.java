package me.swipez.vehicles.planes;

import me.swipez.vehicles.VehicleType;
import me.swipez.vehicles.vehicles.Vehicle_1_19_4;
import me.swipez.vehicles.VehiclesPlugin;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Plane_1_19_4 extends Vehicle_1_19_4 implements Plane {

    private double gravityUpdateRate = 1;
    private double raiseRate = 0.15;
    private boolean drivable = false;
    private List<UUID> blades = new ArrayList<>();
    private UUID bladeOrigin = null;
    private boolean hasBlades = true;
    private boolean rotateX = false;

    public Plane_1_19_4(UUID seat, List<UUID> armorStands, Vector forwards, Location origin, String name, List<UUID> extraSeats, VehicleType vehicleType, UUID owner, UUID vehicleId, List<UUID> blades, UUID bladeOrigin, boolean rotateX) {
        super(seat, armorStands, forwards, origin, name, extraSeats, vehicleType, owner, vehicleId);
        this.blades = blades;
        this.bladeOrigin = bladeOrigin;
        if (blades.isEmpty()){
            hasBlades = false;
        }
        this.raiseRate = vehicleType.raiseRate;
        this.rotateX = rotateX;
        armorStands.addAll(blades);
        armorStands.add(bladeOrigin);
        blades.add(bladeOrigin);
        if (VehiclesPlugin.delayedVehicles.containsValue(this)){
            return;
        }
        for (UUID uuid : blades){
            getRelativeLocations().put(uuid, Objects.requireNonNull(Bukkit.getEntity(uuid)).getLocation().clone().subtract(origin));
            PersistentDataContainer persistentDataContainer =  Objects.requireNonNull(Bukkit.getEntity(uuid)).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, getUniqueId().toString());
            getOriginalOffsets().put(uuid, Objects.requireNonNull(Bukkit.getEntity(uuid)).getLocation().clone().getDirection());
        }

        getRelativeLocations().put(bladeOrigin, Objects.requireNonNull(Bukkit.getEntity(bladeOrigin)).getLocation().clone().subtract(origin));
        PersistentDataContainer persistentDataContainer =  Objects.requireNonNull(Bukkit.getEntity(bladeOrigin)).getPersistentDataContainer();
        persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, getUniqueId().toString());
        getOriginalOffsets().put(bladeOrigin, Objects.requireNonNull(Bukkit.getEntity(bladeOrigin)).getLocation().clone().getDirection());
        fixBlades(0.1);
    }

    @Override
    public void runDelayedActions(){
        for (UUID uuid : blades){
            if (Bukkit.getEntity(uuid) == null){
                continue;
            }
            getRelativeLocations().put(uuid, Objects.requireNonNull(Bukkit.getEntity(uuid)).getLocation().clone().subtract(getOrigin()));
            PersistentDataContainer persistentDataContainer =  Objects.requireNonNull(Bukkit.getEntity(uuid)).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, getUniqueId().toString());
            getOriginalOffsets().put(uuid, Objects.requireNonNull(Bukkit.getEntity(uuid)).getLocation().clone().getDirection());
        }
        if (Bukkit.getEntity(bladeOrigin) != null){
            getRelativeLocations().put(bladeOrigin, Objects.requireNonNull(Bukkit.getEntity(bladeOrigin)).getLocation().clone().subtract(getOrigin()));
            PersistentDataContainer bladeContainer =  Objects.requireNonNull(Bukkit.getEntity(bladeOrigin)).getPersistentDataContainer();
            bladeContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, getUniqueId().toString());
            getOriginalOffsets().put(bladeOrigin, Objects.requireNonNull(Bukkit.getEntity(bladeOrigin)).getLocation().clone().getDirection());
        }

        for (UUID uuid : getOtherSeats()){
            if (Bukkit.getEntity(uuid) == null){
                continue;
            }
            getRelativeLocations().put(uuid, Objects.requireNonNull(Bukkit.getEntity(uuid)).getLocation().clone().subtract(getOrigin()));
            PersistentDataContainer persistentDataContainer =  Objects.requireNonNull(Bukkit.getEntity(uuid)).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, getUniqueId().toString());
            getOriginalOffsets().put(uuid, Objects.requireNonNull(Bukkit.getEntity(uuid)).getLocation().clone().getDirection());
        }
        for (UUID uuid : getArmorStands()){
            if (Bukkit.getEntity(uuid) == null){
                continue;
            }
            getRelativeLocations().put(uuid, Objects.requireNonNull(Bukkit.getEntity(uuid)).getLocation().clone().subtract(getOrigin()));
            PersistentDataContainer persistentDataContainer =  Objects.requireNonNull(Bukkit.getEntity(uuid)).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, getUniqueId().toString());
            getOriginalOffsets().put(uuid, Objects.requireNonNull(Bukkit.getEntity(uuid)).getLocation().clone().getDirection());
        }
        if (Bukkit.getEntity(getSeatUUID()) != null){
            getRelativeLocations().put(getSeatUUID(), Objects.requireNonNull(Bukkit.getEntity(getSeatUUID())).getLocation().clone().subtract(getOrigin()));
            getOriginalOffsets().put(getSeatUUID(), Objects.requireNonNull(Bukkit.getEntity(getSeatUUID())).getLocation().clone().getDirection());
        }
        for (int i = 0; i < 360; i++){
            rotate(0.1);
        }
        if (getColor() != null){
            dye(getColor());
        }
    }

    public void fixBlades(double angle){
        for (int i = 0; i < 360; i++){
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            for (UUID uuid : getRelativeLocations().keySet()){
                if (!blades.contains(uuid)){
                    continue;
                }
                Entity entity = Bukkit.getEntity(uuid);
                Location originalRelative = getRelativeLocations().get(uuid).clone().add(getOrigin());
                Location relativeToOrigin = originalRelative.clone().subtract(getOrigin());
                double x = relativeToOrigin.getX();
                double y = relativeToOrigin.getY();
                double z = relativeToOrigin.getZ();
                double newX = x * cos - z * sin;
                double newZ = x * sin + z * cos;
                Location destination = getOrigin().clone().add(newX, y, newZ);
                destination.setDirection(getRelativeLocations().get(uuid).clone().getDirection().rotateAroundY(-(angle)));
                CraftEntity craftEntity = (CraftEntity) entity;
                assert craftEntity != null;
                craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
                getRelativeLocations().put(uuid, entity.getLocation().clone().subtract(getOrigin()));
            }
        }
    }

    public void rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        Vector desiredForwards = getForwards().clone().rotateAroundY(-angle);
        setForwards(desiredForwards.clone());

        for (UUID uuid : getRelativeLocations().keySet()){
            Entity entity = Bukkit.getEntity(uuid);
            Location originalRelative = getRelativeLocations().get(uuid).clone().add(getOrigin());
            Location relativeToOrigin = originalRelative.clone().subtract(getOrigin());
            double x = relativeToOrigin.getX();
            double y = relativeToOrigin.getY();
            double z = relativeToOrigin.getZ();
            double newX = x * cos - z * sin;
            double newZ = x * sin + z * cos;
            Location destination = getOrigin().clone().add(newX, y, newZ);
            destination.setDirection(getRelativeLocations().get(uuid).clone().getDirection().rotateAroundY(-(angle)));
            CraftEntity craftEntity = (CraftEntity) entity;
            if (craftEntity == null){
                if (!VehiclesPlugin.delayedVehicles.containsValue(this)){
                    VehiclesPlugin.delayedVehicles.put(getOrigin(), this);
                }
                continue;
            }
            craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
            getRelativeLocations().put(uuid, entity.getLocation().clone().subtract(getOrigin()));
        }
    }

    public void rotateBlades(double angle){
        if (!hasBlades){
            return;
        }
        if (VehiclesPlugin.delayedVehicles.containsValue(this)){
            return;
        }
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        Entity bladeOriginEntity = Bukkit.getEntity(bladeOrigin);
        for (UUID uuid : blades){
            Entity entity = Bukkit.getEntity(uuid);
            if (entity == null){
                return;
            }
            assert bladeOriginEntity != null;
            Location relativeToOrigin = entity.getLocation().clone().subtract(bladeOriginEntity.getLocation().clone());
            Location destination;
            if (!rotateX){
                double x = relativeToOrigin.getX();
                double y = relativeToOrigin.getY();
                double z = relativeToOrigin.getZ();
                double newX = x * cos - z * sin;
                double newZ = x * sin + z * cos;
                destination = bladeOriginEntity.getLocation().clone().add(newX, y, newZ);
                destination.setDirection(getRelativeLocations().get(uuid).clone().getDirection().rotateAroundY(-(angle)));
            }
            else {
                destination = bladeOriginEntity.getLocation().clone().add(relativeToOrigin.clone().toVector().clone().rotateAroundAxis(getForwards(), -angle));
            }
            CraftEntity craftEntity = (CraftEntity) entity;
            craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
            getRelativeLocations().put(uuid, entity.getLocation().clone().subtract(getOrigin()));
        }
    }

    public void update() {
        setPreviousPosition(getOrigin().clone());
        Entity seat = getSeat();
        boolean moved = false;
        boolean turned = false;
        boolean flew = false;
        if (VehiclesPlugin.delayedVehicles.containsValue(this)) {
            return;
        }
        if (seat == null) {
            // Unloaded entity
            return;
        }
        if (blades == null) {
            return;
        }
        if (!seat.getPassengers().isEmpty()) {
            Entity passenger = seat.getPassengers().get(0);
            double dot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).normalize());
            double turnDot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).rotateAroundY(90).normalize());
            double forwardsLeftTurnDot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).rotateAroundY(45).normalize());
            double forwardsRightTurnDot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).rotateAroundY(-45).normalize());

            if (forwardsRightTurnDot > 0.9) {
                move(false, true);
                rotate(getTurningSpeed() * getSpeedMultiplier());
                setSpeedMultiplier(getSpeedMultiplier() * 0.999F);
                moved = true;
                turned = true;
            } else if (forwardsLeftTurnDot > 0.9) {
                move(false, true);
                rotate(-getTurningSpeed() * getSpeedMultiplier());
                setSpeedMultiplier(getSpeedMultiplier() * 0.999F);
                moved = true;
                turned = true;
            } else if (forwardsLeftTurnDot < -0.9) {
                move(true, true);
                rotate(-getTurningSpeed() * getSpeedMultiplier());
                setSpeedMultiplier(getSpeedMultiplier() * 0.999F);
                moved = true;
                turned = true;
            } else if (forwardsRightTurnDot < -0.9) {
                move(true, true);
                rotate(getTurningSpeed() * getSpeedMultiplier());
                setSpeedMultiplier(getSpeedMultiplier() * 0.999F);
                moved = true;
                turned = true;
            } else if (turnDot > 0.8) {
                rotate(-getTurningSpeed() * getSpeedMultiplier());
                turned = true;
            } else if (turnDot < -0.8) {
                rotate(getTurningSpeed() * getSpeedMultiplier());
                turned = true;
            } else if (dot > 0.9 && dot < 1) {
                move(false, true);
                moved = true;
            } else if (dot < -0.9 && dot > -1) {
                if (getSpeedMultiplier() <= 0) {
                    move(true, true);
                    moved = true;
                }
            }

            if (drivable) {
                if (passenger.getLocation().getPitch() < -20) {
                    double yAddition = raiseRate * getSpeedMultiplier();
                    if (getOrigin().clone().add(0, yAddition, 0).getBlock().getType().isAir()) {
                        for (UUID uuid : getRelativeLocations().keySet()) {
                            Entity entity = Bukkit.getEntity(uuid);
                            Location destination = entity.getLocation().clone().add(0, yAddition, 0);
                            CraftEntity craftEntity = (CraftEntity) entity;
                            craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
                        }
                        getOrigin().add(0, yAddition, 0);
                    } else {
                        setSpeedMultiplier(getSpeedMultiplier() * 0.8F);
                    }
                }
                else if (passenger.getLocation().getPitch() < 20 && passenger.getLocation().getPitch() > -20) {
                    if (getSpeedMultiplier() > 0.8 && getOrigin().clone().subtract(0, getGroundDistance(), 0).getBlock().getType().isAir()){
                        flew = true;
                    }
                }
                else if (passenger.getLocation().getPitch() > 20) {
                    // nothing yet, just go down
                }
            }
        } else {
            drivable = false;
        }
        rotateBlades(0.4 * getSpeedMultiplier());

        if (!moved) {
            if (getSpeedMultiplier() >= 0) {
                move(false, false);
                setSpeedMultiplier(getSpeedMultiplier() - (float) getFrictionRate());
                if (getSpeedMultiplier() < 0.001) {
                    setSpeedMultiplier(0);
                }
            } else {
                move(true, false);
                setSpeedMultiplier(getSpeedMultiplier() + (float) getFrictionRate());
                if (getSpeedMultiplier() > -0.001) {
                    setSpeedMultiplier(0);
                }
            }
        }
        if (getSpeedMultiplier() > 1) {
            setSpeedMultiplier(getSpeedMultiplier() * 0.9F);
        }
        if (getSpeedMultiplier() < -0.5) {
            setSpeedMultiplier(getSpeedMultiplier() * 0.9F);
        }
        if (!flew){
            if (!getOrigin().clone().subtract(0, getGroundDistance(), 0).getBlock().getType().isSolid()) {
                int times = 0;
                double checks = gravityUpdateRate - (gravityUpdateRate * getSpeedMultiplier());
                Location testLoc = getOrigin().clone().subtract(0, getGroundDistance(), 0);
                while (!testLoc.clone().getBlock().getType().isSolid()) {
                    testLoc.add(0, -getGroundDistance(), 0);
                    times++;
                    if (times > checks) {
                        break;
                    }
                }
                for (UUID uuid : getRelativeLocations().keySet()) {
                    Entity entity = Bukkit.getEntity(uuid);
                    assert entity != null;
                    Location destination = entity.getLocation().clone().subtract(0, getGroundDistance() * times, 0);
                    CraftEntity craftEntity = (CraftEntity) entity;
                    craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
                }
                getOrigin().subtract(0, getGroundDistance() * times, 0);
            }
        }
        setCurrentLocation(getOrigin().clone());
        Objects.requireNonNull(getOrigin().getWorld()).spawnParticle(Particle.REDSTONE, getOrigin(), 0, new Particle.DustOptions(Color.fromBGR(100, 100, 100), (float) (2 * getSpeedMultiplier())));
        getOrigin().getWorld().spawnParticle(Particle.REDSTONE, getOrigin().clone().add(getForwards().clone().normalize().multiply(0.25)), 0, new Particle.DustOptions(Color.fromBGR(0, 0, 0), (float) (1 * getSpeedMultiplier())));
    }

    @Override
    public boolean isDriveable() {
        return drivable;
    }

    @Override
    public void setDriveable(boolean drivable) {
        this.drivable = drivable;
    }
}
