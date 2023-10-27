package me.swipez.vehicles;

import me.swipez.vehicles.events.VehicleEnterEvent;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Vehicle {

    public UUID id;
    List<UUID> armorStands = new ArrayList<>();
    HashMap<UUID, Location> relativeLocations = new HashMap<>();
    HashMap<UUID, Vector> originalOffsets = new HashMap<>();
    public UUID seat;
    public Location origin;
    Location previousPosition;
    Location currentLocation;
    Vector forwards;
    String name;
    double speed = 0.4;
    float speedMultiplier = 0;
    double gainRate = 0.01;
    double groundDistance = 0.1;
    double turningSpeed = 0.15;
    double stepHeight = 1;
    double frictionRate = 0.02;
    double gravityUpdateRate = 5;

    boolean crashed = false;
    boolean checkedForTires = false;
    double turnMultiplier = 0;

    public String color = null;
    public String enumName = "";
    UUID owner;
    private VehicleType vehicleType = null;

    List<UUID> otherSeats = new ArrayList<>();
    List<UUID> probablyTires = new ArrayList<>();

    public Vehicle(UUID seat, List<UUID> armorStands, Vector forwards, Location origin, String name, List<UUID> extraSeats, VehicleType vehicleType, UUID owner, UUID vehicleId) {
        this.id = vehicleId;
        this.otherSeats = extraSeats;
        this.seat = seat;
        this.name = name;
        this.armorStands = armorStands;
        this.forwards = forwards;
        this.origin = origin.clone();
        if (vehicleType != null){
            this.enumName = vehicleType.name();
            this.speed = vehicleType.speed;
            this.name = vehicleType.carName;
            this.gainRate = vehicleType.gainRate;
            this.turningSpeed = vehicleType.turnRate;
            this.stepHeight = vehicleType.stepHeight;
            this.vehicleType = vehicleType;
        }
        this.owner = owner;

        VehiclesPlugin.vehiclesOwnedByPlayers.putIfAbsent(owner, new ArrayList<>());
        List<Vehicle> vehiclesList = VehiclesPlugin.vehiclesOwnedByPlayers.get(owner);
        if (vehiclesList == null){
            vehiclesList = new ArrayList<>();
        }
        vehiclesList.add(this);
        VehiclesPlugin.vehiclesOwnedByPlayers.put(owner, vehiclesList);

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
        try {
            if (!VehiclesPlugin.nameMappings.containsKey(offlinePlayer.getName().toLowerCase())){
                System.out.printf("Adding %s to name mappings%n", offlinePlayer.getName().toLowerCase());
                VehiclesPlugin.nameMappings.put(offlinePlayer.getName(), owner);
            }
        } catch (NullPointerException ignored){
            // ignore
        }

        VehiclesPlugin.allSeats.add(seat);
        VehiclesPlugin.vehicles.put(id, this);


        for (UUID uuid : armorStands){
            if (Bukkit.getEntity(uuid) == null){
                // World unloaded bug
                VehiclesPlugin.delayedVehicles.put(origin, this);
                VehiclesPlugin.getPlugin().getLogger().info("World unloaded bug, delaying vehicle");
                VehiclesPlugin.getPlugin().getLogger().info(VehiclesPlugin.delayedVehicles.size()+ " vehicles delayed");
                return;
            }
        }

        for (UUID uuid : extraSeats){
            relativeLocations.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(origin));
            PersistentDataContainer persistentDataContainer =  Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, id.toString());
            originalOffsets.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }
        for (UUID uuid : armorStands){
            relativeLocations.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(origin));
            PersistentDataContainer persistentDataContainer =  Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, id.toString());
            originalOffsets.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }
        relativeLocations.put(seat, Bukkit.getEntity(seat).getLocation().clone().subtract(origin));
        originalOffsets.put(seat, Bukkit.getEntity(seat).getLocation().clone().getDirection());

        // Fixes initial armor stand rotation (bug)
        for (int i = 0; i < 360; i++){
            rotate(0.1);
        }
    }

    public VehicleType getVehicleType(){
        return vehicleType;
    }

    // ONLY FOR USE WITH DELAYED VEHICLES (Ones that want to spawn in locations that are not loaded)
    public void runDelayedActions(){
        for (UUID uuid : otherSeats){
            relativeLocations.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(origin));
            PersistentDataContainer persistentDataContainer =  Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, id.toString());
            originalOffsets.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }
        for (UUID uuid : armorStands){
            relativeLocations.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(origin));
            PersistentDataContainer persistentDataContainer =  Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, id.toString());
            originalOffsets.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }
        relativeLocations.put(seat, Bukkit.getEntity(seat).getLocation().clone().subtract(origin));
        originalOffsets.put(seat, Bukkit.getEntity(seat).getLocation().clone().getDirection());
        for (int i = 0; i < 360; i++){
            rotate(0.1);
        }
        if (color != null){
            dye(color);
        }
    }

    public boolean isOwnedBy(Player player){
        if (player.getUniqueId().equals(owner)){
            return true;
        }
        if (player.hasPermission("vehicles.admin")){
            return true;
        }
        return false;
    }

    public void dye(String color){
        this.color = color;
        if (!checkedForTires){
            for (UUID uuid : relativeLocations.keySet()){
                Entity entity = Bukkit.getEntity(uuid);
                if (entity != null){
                    if (!isSeat(uuid)){
                        ArmorStand armorStand = (ArmorStand) entity;
                        if (armorStand.getEquipment().getHelmet() != null){
                            if (armorStand.getEquipment().getHelmet().getType().equals(Material.BLACK_CONCRETE)){
                                probablyTires.add(uuid);
                            }
                        }
                    }
                }
            }
            checkedForTires = true;
        }
        HashMap<Material, Integer> materialCount = new HashMap<>();
        for (UUID uuid : relativeLocations.keySet()){
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null){
                if (!isSeat(uuid) && !probablyTires.contains(uuid)){
                    ArmorStand armorStand = (ArmorStand) entity;
                    if (armorStand.getEquipment().getHelmet() != null){
                        materialCount.putIfAbsent(armorStand.getEquipment().getHelmet().getType(), 0);
                        materialCount.put(armorStand.getEquipment().getHelmet().getType(), materialCount.get(armorStand.getEquipment().getHelmet().getType()) + 1);
                    }
                }
            }
        }
        Material majorityConcrete = null;
        int highestConcrete = -1;
        Material majorityCarpet = null;
        int highestCarpet = -1;
        for (Material material : materialCount.keySet()){
            if (materialCount.get(material) > highestConcrete && material.toString().toLowerCase().contains("concrete") && !material.toString().toLowerCase().contains("powder")){
                majorityConcrete = material;
                highestConcrete = materialCount.get(material);
            }
            if (materialCount.get(material) > highestCarpet && material.toString().toLowerCase().contains("carpet")){
                majorityCarpet = material;
                highestCarpet = materialCount.get(material);
            }
        }

        for (UUID uuid : relativeLocations.keySet()){
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null){
                if (!isSeat(uuid) && !probablyTires.contains(uuid)){
                    ArmorStand armorStand = (ArmorStand) entity;
                    if (armorStand.getEquipment().getHelmet() != null){
                        Material material = armorStand.getEquipment().getHelmet().getType();
                        if (material.equals(majorityConcrete)){
                            armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(color.toUpperCase() + "_CONCRETE")));
                        }
                        else if (material.equals(majorityCarpet)){
                            armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(color.toUpperCase() + "_CARPET")));
                        }
                        if (majorityConcrete != null){
                            String carpetCheck = majorityConcrete.toString().toLowerCase().replace("_concrete", "_carpet");
                            if (material.equals(Material.valueOf(carpetCheck.toUpperCase()))){
                                armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(color.toUpperCase() + "_CARPET")));
                            }
                        }
                    }
                }
            }
        }
    }

    public void remove(boolean clearId){
        for (UUID uuid : relativeLocations.keySet()){
            if (Bukkit.getEntity(uuid) != null){
                Bukkit.getEntity(uuid).remove();
            }
        }
        if (clearId){
            List<Vehicle> vehicles = VehiclesPlugin.vehiclesOwnedByPlayers.get(owner);
            vehicles.remove(this);
            VehiclesPlugin.vehiclesOwnedByPlayers.put(owner, vehicles);
            VehiclesPlugin.vehicles.remove(id);
            VehiclesPlugin.allSeats.remove(seat);
        }
    }

    public void attemptSit(Player player){
        if (VehiclesPlugin.settings.ownerOnlyRider && !isOwnedBy(player)){
            if (otherSeats.isEmpty()){
                return;
            }
            for (UUID uuid : otherSeats){
                if (Bukkit.getEntity(uuid).getPassengers().isEmpty()){
                    VehicleEnterEvent vehicleEnterEvent = new VehicleEnterEvent(player, this, false);
                    Bukkit.getPluginManager().callEvent(vehicleEnterEvent);
                    if (!vehicleEnterEvent.isCancelled()){
                        Bukkit.getEntity(uuid).addPassenger(player);
                        return;
                    }
                }
            }
        }
        else if (VehiclesPlugin.settings.ownerOnlyRider && isOwnedBy(player)){
            if (getSeat().getPassengers().isEmpty()){
                VehicleEnterEvent vehicleEnterEvent = new VehicleEnterEvent(player, this, true);
                Bukkit.getPluginManager().callEvent(vehicleEnterEvent);
                if (!vehicleEnterEvent.isCancelled()){
                    getSeat().addPassenger(player);
                }
            }
        }
        else {
            if (getSeat().getPassengers().isEmpty()){
                VehicleEnterEvent vehicleEnterEvent = new VehicleEnterEvent(player, this, true);
                Bukkit.getPluginManager().callEvent(vehicleEnterEvent);
                if (!vehicleEnterEvent.isCancelled()){
                    getSeat().addPassenger(player);
                }
            }
            else {
                if (otherSeats.isEmpty()){
                    return;
                }
                for (UUID uuid : otherSeats){
                    if (Bukkit.getEntity(uuid).getPassengers().isEmpty()){
                        VehicleEnterEvent vehicleEnterEvent = new VehicleEnterEvent(player, this, false);
                        Bukkit.getPluginManager().callEvent(vehicleEnterEvent);
                        if (!vehicleEnterEvent.isCancelled()){
                            Bukkit.getEntity(uuid).addPassenger(player);
                            return;
                        }
                    }
                }
            }
        }
    }

    public Entity getSeat(){
        return Bukkit.getEntity(seat);
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

    private boolean isSeat(UUID uuid){
        if (otherSeats.contains(uuid)){
            return true;
        }
        if (uuid.equals(seat)){
            return true;
        }
        return false;
    }

    public void move(boolean negative, boolean modify){
        double desiredSpeed = speed;
        desiredSpeed *= speedMultiplier;
        Location floorCheck = origin.clone().add(forwards.clone().multiply(desiredSpeed));
        Location aboveCheck = floorCheck.clone().add(0, stepHeight, 0);
        if (floorCheck.getBlock().getType().isSolid()) {
            if (aboveCheck.getBlock().getType().isSolid()) {
                if (!crashed){
                    if (speedMultiplier > 0.9){
                        crashed = true;
                        origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                        origin.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, origin, 3);
                    }
                }
                speedMultiplier = 0;
                return;
            }
            else {
                origin = aboveCheck;
                for (UUID uuid : armorStands){
                    ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
                    armorStand.teleport(armorStand.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed)).add(0, stepHeight, 0));
                }
                for (UUID uuid : otherSeats){
                    ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
                    Location destination = armorStand.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed)).add(0, stepHeight, 0);
                    CraftEntity craftEntity = (CraftEntity) armorStand;
                    craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
                }
                Entity seat = Bukkit.getEntity(this.seat);
                Location seatDestination = seat.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed)).add(0, stepHeight, 0);
                seatDestination.setDirection(forwards);
                if (!seat.getPassengers().isEmpty()){
                    CraftEntity craftEntity = (CraftEntity) seat;
                    craftEntity.getHandle().b(seatDestination.getX(), seatDestination.getY(), seatDestination.getZ(), seatDestination.getYaw(), seatDestination.getPitch());
                }
                else {
                    seat.teleport(seatDestination);
                }
                if (modify){
                    speedMultiplier += gainRate;
                }
                return;
            }
        }
        for (UUID uuid : armorStands){
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
            if (armorStand != null){
                armorStand.teleport(armorStand.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed)));
            }

        }
        for (UUID uuid : otherSeats){
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
            if (armorStand != null){
                Location destination = armorStand.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed));
                CraftEntity craftEntity = (CraftEntity) armorStand;
                craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
            }
        }
        Entity seat = Bukkit.getEntity(this.seat);
        if (seat == null){
            return;
        }
        Location seatDestination = seat.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed));
        seatDestination.setDirection(forwards);
        if (!seat.getPassengers().isEmpty()){
            CraftEntity craftEntity = (CraftEntity) seat;
            craftEntity.getHandle().b(seatDestination.getX(), seatDestination.getY(), seatDestination.getZ(), seatDestination.getYaw(), seatDestination.getPitch());
        }
        else {
            seat.teleport(seatDestination);
        }
        origin.add(forwards.clone().normalize().multiply(desiredSpeed));
        if (modify){
            if (negative){
                speedMultiplier -= gainRate;
            }
            else {
                speedMultiplier += gainRate;
            }
        }
        crashed = false;
    }

    public void update(){
        previousPosition = origin.clone();
        Entity seat = getSeat();
        boolean moved = false;
        boolean turned = false;
        if (seat == null){
            // Unloaded entity
            return;
        }
        if (!seat.getPassengers().isEmpty()){
            Entity passenger = seat.getPassengers().get(0);
            double dot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).normalize());
            double turnDot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).rotateAroundY(90).normalize());
            double forwardsLeftTurnDot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).rotateAroundY(45).normalize());
            double forwardsRightTurnDot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).rotateAroundY(-45).normalize());

            if (forwardsRightTurnDot > 0.9){
                move(false, true);
                rotate(turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999;
                moved = true;
                turned = true;
            }
            else if (forwardsLeftTurnDot > 0.9){
                move(false, true);
                rotate(-turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999;
                moved = true;
                turned = true;
            }
            else if (forwardsLeftTurnDot < -0.9){
                move(true, true);
                rotate(-turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999;
                moved = true;
                turned = true;
            }
            else if (forwardsRightTurnDot < -0.9){
                move(true, true);
                rotate(turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999;
                moved = true;
                turned = true;
            }
            else if (turnDot > 0.8){
                rotate(-turningSpeed * speedMultiplier);
                turned = true;
            }
            else if (turnDot < -0.8){
                rotate(turningSpeed * speedMultiplier);
                turned = true;
            }
            else if (dot > 0.9 && dot < 1){
                move(false, true);
                moved = true;
            }
            else if (dot < -0.9 && dot > -1){
                if (speedMultiplier <= 0){
                    move(true, true);
                    moved = true;
                }
            }

        }
        if (!moved){
            if (speedMultiplier >= 0) {
                move(false, false);
                speedMultiplier -= frictionRate;
                if (speedMultiplier < 0.001) {
                    speedMultiplier = 0;
                }
            }
            else {
                move(true, false);
                speedMultiplier += frictionRate;
                if (speedMultiplier > -0.001) {
                    speedMultiplier = 0;
                }
            }
        }
        if (speedMultiplier > 1){
            speedMultiplier *= 0.9;
        }
        if (speedMultiplier < -0.5){
            speedMultiplier *= 0.9;
        }
        if (!origin.clone().subtract(0, groundDistance, 0).getBlock().getType().isSolid()){
            int times = 0;
            Location testLoc = origin.clone().subtract(0, groundDistance, 0);
            while (!testLoc.clone().getBlock().getType().isSolid()){
                testLoc.add(0, -groundDistance, 0);
                times++;
                if (times > gravityUpdateRate){
                    break;
                }
            }
            for (UUID uuid : relativeLocations.keySet()){
                Entity entity = Bukkit.getEntity(uuid);
                Location destination = entity.getLocation().clone().subtract(0, groundDistance*times, 0);
                CraftEntity craftEntity = (CraftEntity) entity;
                craftEntity.getHandle().b(destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());
            }
            origin.subtract(0, groundDistance * times, 0);
        }
        currentLocation = origin.clone();
        origin.getWorld().spawnParticle(Particle.REDSTONE, origin, 0, new Particle.DustOptions(Color.fromBGR(100, 100, 100), (float) (2 * speedMultiplier)));
        origin.getWorld().spawnParticle(Particle.REDSTONE, origin.clone().add(forwards.clone().normalize().multiply(0.25)), 0, new Particle.DustOptions(Color.fromBGR(0, 0, 0), (float) (1 * speedMultiplier)));
    }
}
