package me.swipez.vehicles;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Plane extends Vehicle{

    double gravityUpdateRate = 1;
    double raiseRate = 0.15;
    public boolean drivable = false;
    List<UUID> blades = new ArrayList<>();
    UUID bladeOrigin = null;
    boolean hasBlades = true;
    boolean rotateX = false;

    public Plane(UUID seat, List<UUID> armorStands, Vector forwards, Location origin, String name, List<UUID> extraSeats, VehicleType vehicleType, UUID owner, UUID vehicleId, List<UUID> blades, UUID bladeOrigin, boolean rotateX) {
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
            relativeLocations.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(origin));
            PersistentDataContainer persistentDataContainer =  Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, id.toString());
            originalOffsets.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }

        relativeLocations.put(bladeOrigin, Bukkit.getEntity(bladeOrigin).getLocation().clone().subtract(origin));
        PersistentDataContainer persistentDataContainer =  Bukkit.getEntity(bladeOrigin).getPersistentDataContainer();
        persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, id.toString());
        originalOffsets.put(bladeOrigin, Bukkit.getEntity(bladeOrigin).getLocation().clone().getDirection());
        fixBlades(0.1);
    }

    @Override
    public void runDelayedActions(){
        for (UUID uuid : blades){
            if (Bukkit.getEntity(uuid) == null){
                continue;
            }
            relativeLocations.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(origin));
            PersistentDataContainer persistentDataContainer =  Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, id.toString());
            originalOffsets.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }
        if (Bukkit.getEntity(bladeOrigin) != null){
            relativeLocations.put(bladeOrigin, Bukkit.getEntity(bladeOrigin).getLocation().clone().subtract(origin));
            PersistentDataContainer bladeContainer =  Bukkit.getEntity(bladeOrigin).getPersistentDataContainer();
            bladeContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, id.toString());
            originalOffsets.put(bladeOrigin, Bukkit.getEntity(bladeOrigin).getLocation().clone().getDirection());
        }

        for (UUID uuid : otherSeats){
            if (Bukkit.getEntity(uuid) == null){
                continue;
            }
            relativeLocations.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(origin));
            PersistentDataContainer persistentDataContainer =  Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, id.toString());
            originalOffsets.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }
        for (UUID uuid : armorStands){
            if (Bukkit.getEntity(uuid) == null){
                continue;
            }
            relativeLocations.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(origin));
            PersistentDataContainer persistentDataContainer =  Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, id.toString());
            originalOffsets.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }
        if (Bukkit.getEntity(seat) != null){
            relativeLocations.put(seat, Bukkit.getEntity(seat).getLocation().clone().subtract(origin));
            originalOffsets.put(seat, Bukkit.getEntity(seat).getLocation().clone().getDirection());
        }
        for (int i = 0; i < 360; i++){
            rotate(0.1);
        }
        if (color != null){
            dye(color);
        }
    }

    public void fixBlades(double angle){
        for (int i = 0; i < 360; i++){
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            for (UUID uuid : relativeLocations.keySet()){
                if (!blades.contains(uuid)){
                    continue;
                }
                Entity entity = Bukkit.getEntity(uuid);
                Location originalRelative = relativeLocations.get(uuid).clone().add(origin);
                Location relativeToOrigin = originalRelative.clone().subtract(origin);
                double x = relativeToOrigin.getX();
                double y = relativeToOrigin.getY();
                double z = relativeToOrigin.getZ();
                double newX = x * cos - z * sin;
                double newZ = x * sin + z * cos;
                Location destination = origin.clone().add(newX, y, newZ);
                destination.setDirection(relativeLocations.get(uuid).clone().getDirection().rotateAroundY(-(angle)));
                CraftEntity craftEntity = (CraftEntity) entity;
                craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
                relativeLocations.put(uuid, entity.getLocation().clone().subtract(origin));
            }
        }
    }

    public void rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        Vector desiredForwards = forwards.clone().rotateAroundY(-angle);
        forwards = desiredForwards.clone();

        for (UUID uuid : relativeLocations.keySet()){
            Entity entity = Bukkit.getEntity(uuid);
            Location originalRelative = relativeLocations.get(uuid).clone().add(origin);
            Location relativeToOrigin = originalRelative.clone().subtract(origin);
            double x = relativeToOrigin.getX();
            double y = relativeToOrigin.getY();
            double z = relativeToOrigin.getZ();
            double newX = x * cos - z * sin;
            double newZ = x * sin + z * cos;
            Location destination = origin.clone().add(newX, y, newZ);
            destination.setDirection(relativeLocations.get(uuid).clone().getDirection().rotateAroundY(-(angle)));
            CraftEntity craftEntity = (CraftEntity) entity;
            if (craftEntity == null){
                if (!VehiclesPlugin.delayedVehicles.containsValue(this)){
                    VehiclesPlugin.delayedVehicles.put(origin, this);
                }
                continue;
            }
            craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
            relativeLocations.put(uuid, entity.getLocation().clone().subtract(origin));
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
            Location relativeToOrigin = entity.getLocation().clone().subtract(bladeOriginEntity.getLocation().clone());
            Location destination = null;
            if (!rotateX){
                double x = relativeToOrigin.getX();
                double y = relativeToOrigin.getY();
                double z = relativeToOrigin.getZ();
                double newX = x * cos - z * sin;
                double newZ = x * sin + z * cos;
                destination = bladeOriginEntity.getLocation().clone().add(newX, y, newZ);
                destination.setDirection(relativeLocations.get(uuid).clone().getDirection().rotateAroundY(-(angle)));
            }
            else {
                destination = bladeOriginEntity.getLocation().clone().add(relativeToOrigin.clone().toVector().clone().rotateAroundAxis(forwards, -angle));
            }
            CraftEntity craftEntity = (CraftEntity) entity;
            craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
            relativeLocations.put(uuid, entity.getLocation().clone().subtract(origin));
        }
    }

    public void update() {
        previousPosition = origin.clone();
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
                rotate(turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999;
                moved = true;
                turned = true;
            } else if (forwardsLeftTurnDot > 0.9) {
                move(false, true);
                rotate(-turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999;
                moved = true;
                turned = true;
            } else if (forwardsLeftTurnDot < -0.9) {
                move(true, true);
                rotate(-turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999;
                moved = true;
                turned = true;
            } else if (forwardsRightTurnDot < -0.9) {
                move(true, true);
                rotate(turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999;
                moved = true;
                turned = true;
            } else if (turnDot > 0.8) {
                rotate(-turningSpeed * speedMultiplier);
                turned = true;
            } else if (turnDot < -0.8) {
                rotate(turningSpeed * speedMultiplier);
                turned = true;
            } else if (dot > 0.9 && dot < 1) {
                move(false, true);
                moved = true;
            } else if (dot < -0.9 && dot > -1) {
                if (speedMultiplier <= 0) {
                    move(true, true);
                    moved = true;
                }
            }

            if (drivable) {
                if (passenger.getLocation().getPitch() < -20) {
                    double yAddition = raiseRate * speedMultiplier;
                    if (origin.clone().add(0, yAddition, 0).getBlock().getType().isAir()) {
                        for (UUID uuid : relativeLocations.keySet()) {
                            Entity entity = Bukkit.getEntity(uuid);
                            Location destination = entity.getLocation().clone().add(0, yAddition, 0);
                            CraftEntity craftEntity = (CraftEntity) entity;
                            craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
                        }
                        origin.add(0, yAddition, 0);
                    } else {
                        speedMultiplier *= 0.8;
                    }
                }
                else if (passenger.getLocation().getPitch() < 20 && passenger.getLocation().getPitch() > -20) {
                    if (speedMultiplier > 0.8 && origin.clone().subtract(0, groundDistance, 0).getBlock().getType().isAir()){
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
        rotateBlades(0.4 * speedMultiplier);

        if (!moved) {
            if (speedMultiplier >= 0) {
                move(false, false);
                speedMultiplier -= frictionRate;
                if (speedMultiplier < 0.001) {
                    speedMultiplier = 0;
                }
            } else {
                move(true, false);
                speedMultiplier += frictionRate;
                if (speedMultiplier > -0.001) {
                    speedMultiplier = 0;
                }
            }
        }
        if (speedMultiplier > 1) {
            speedMultiplier *= 0.9;
        }
        if (speedMultiplier < -0.5) {
            speedMultiplier *= 0.9;
        }
        if (!flew){
            if (!origin.clone().subtract(0, groundDistance, 0).getBlock().getType().isSolid()) {
                int times = 0;
                double checks = gravityUpdateRate - (gravityUpdateRate * speedMultiplier);
                Location testLoc = origin.clone().subtract(0, groundDistance, 0);
                while (!testLoc.clone().getBlock().getType().isSolid()) {
                    testLoc.add(0, -groundDistance, 0);
                    times++;
                    if (times > checks) {
                        break;
                    }
                }
                for (UUID uuid : relativeLocations.keySet()) {
                    Entity entity = Bukkit.getEntity(uuid);
                    Location destination = entity.getLocation().clone().subtract(0, groundDistance * times, 0);
                    CraftEntity craftEntity = (CraftEntity) entity;
                    craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
                }
                origin.subtract(0, groundDistance * times, 0);
            }
        }
        currentLocation = origin.clone();
        origin.getWorld().spawnParticle(Particle.REDSTONE, origin, 0, new Particle.DustOptions(Color.fromBGR(100, 100, 100), (float) (2 * speedMultiplier)));
        origin.getWorld().spawnParticle(Particle.REDSTONE, origin.clone().add(forwards.clone().normalize().multiply(0.25)), 0, new Particle.DustOptions(Color.fromBGR(0, 0, 0), (float) (1 * speedMultiplier)));
    }
}
