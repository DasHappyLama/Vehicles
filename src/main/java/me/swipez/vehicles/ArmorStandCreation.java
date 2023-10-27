package me.swipez.vehicles;

import me.swipez.vehicles.commands.CreationModeCommand;
import me.swipez.vehicles.planes.Plane;
import me.swipez.vehicles.planes.Plane_1_19_4;
import me.swipez.vehicles.planes.Plane_1_20_2;
import me.swipez.vehicles.vehicles.Vehicle;
import me.swipez.vehicles.vehicles.Vehicle_1_19_4;
import me.swipez.vehicles.vehicles.Vehicle_1_20_2;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.*;

public class ArmorStandCreation {
    private List<UUID> selectedStands = new ArrayList<>();
    private Location origin;
    private Location seat;
    private Vector forwards = new Vector(1, 0, 0);
    private int lastInt = 0;
    private int indexCycle = 0;
    public HashMap<Integer, UUID> allArmorStands = new HashMap<>();
    public List<UUID> seats = new ArrayList<>();
    public List<UUID> blades = new ArrayList<>();
    public UUID bladeOrigin = null;

    CreationSettings creationSettings = new CreationSettings();

    public ArmorStandCreation(Location origin) {
        this.origin = origin;
        this.seat = origin.clone().add(0, 0.5, 0);
    }

    public CreationSettings getSettings() {
        return creationSettings;
    }

    public void moveAll(double x, double y, double z) {
        for (UUID uuid : allArmorStands.values()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
            armorStand.teleport(armorStand.getLocation().clone().add(x, y, z));
        }
    }

    public void makeBlade() {
        if (selectedStands.size() == 0) {
            return;
        }
        for (UUID uuid : selectedStands) {
            if (uuid.equals(bladeOrigin)) {
                continue;
            }
            blades.add(uuid);
        }
    }

    public void makeBladeOrigin() {
        bladeOrigin = selectedStands.get(0);
    }

    public void moveSeat(double x, double y, double z) {
        seat.add(new Vector(x, y, z));
    }

    public void deselect(int id) {
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            if (selectedStands.contains(entry.getValue())) {
                if (entry.getKey() == id) {
                    selectedStands.remove(entry.getValue());
                    return;
                }
            }
        }
    }

    public void select(int id) {
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            if (entry.getKey() == id) {
                selectedStands.add(entry.getValue());
                return;
            }
        }
    }

    public void clearVisibleIds() {
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(entry.getValue());
            armorStand.setCustomName(entry.getKey() + "#");
            armorStand.setCustomNameVisible(false);
        }
    }

    public void displayIds(String block) {
        clearVisibleIds();
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(entry.getValue());
            if (armorStand.getEquipment().getHelmet().getType().toString().toLowerCase().contains(block.toLowerCase())) {
                armorStand.setCustomName(entry.getKey() + "#");
                armorStand.setCustomNameVisible(true);
            }
        }
    }

    public void addAll(List<UUID> uuids) {
        for (UUID uuid : uuids) {
            allArmorStands.put(lastInt, uuid);
            lastInt++;
        }
        selectedStands.clear();
        selectedStands.add(uuids.get(0));
    }

    public void cycle() {
        selectedStands.clear();
        selectedStands.add(allArmorStands.get(indexCycle));
        indexCycle++;
        if (indexCycle == allArmorStands.size()) {
            indexCycle = 0;
        }
    }

    public void cycle(String block) {
        List<Integer> availableIntegers = new ArrayList<>();
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(entry.getValue());
            if (armorStand.getEquipment().getHelmet().getType().toString().toLowerCase().contains(block.toLowerCase())) {
                availableIntegers.add(entry.getKey());
            }
        }
        availableIntegers.sort(Comparator.naturalOrder());
        for (int i = 0; i < availableIntegers.size(); i++) {
            int index = availableIntegers.get(i);
            if (indexCycle == index) {
                if (availableIntegers.size() > i + 1) {
                    index = availableIntegers.get(i + 1);
                } else {
                    indexCycle = 0;
                    index = availableIntegers.get(0);
                }
            }
            Bukkit.broadcastMessage("Comparing indexCycle: " + indexCycle + " to index: " + index);
            if (indexCycle < index) {
                Bukkit.broadcastMessage("IndexCycle is less than index");
                selectedStands.clear();
                selectedStands.add(allArmorStands.get(index));
                indexCycle = index;
                break;
            }
        }
        if (indexCycle >= availableIntegers.get(availableIntegers.size() - 1)) {
            indexCycle = -1;
        }
    }

    public void makeArmorStand() {
        ArmorStand armorStand = (ArmorStand) origin.getWorld().spawnEntity(origin.clone(), EntityType.ARMOR_STAND);
        armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.setRightArmPose(new EulerAngle(0, 0, 0));
        armorStand.setLeftArmPose(new EulerAngle(0, 0, 0));
        armorStand.teleport(armorStand.getLocation().clone().setDirection(forwards));
        selectedStands.clear();
        selectedStands.add(armorStand.getUniqueId());
        allArmorStands.put(lastInt, armorStand.getUniqueId());
        lastInt++;
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setBasePlate(false);
        armorStand.setArms(true);
    }

    public void cloneStand() {
        for (ArmorStand selected : getSelectedStands()) {
            ArmorStand clone = (ArmorStand) selected.getWorld().spawnEntity(selected.getLocation(), EntityType.ARMOR_STAND);
            clone.getEquipment().setHelmet(selected.getEquipment().getHelmet());
            clone.getEquipment().setItemInMainHand(selected.getEquipment().getItemInMainHand());
            clone.getEquipment().setItemInOffHand(selected.getEquipment().getItemInOffHand());
            clone.setLeftArmPose(selected.getLeftArmPose());
            clone.setRightArmPose(selected.getRightArmPose());

            clone.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);

            clone.setGravity(false);
            clone.setVisible(false);
            clone.setBasePlate(false);
            clone.setArms(true);

            selectedStands.add(clone.getUniqueId());
            allArmorStands.put(lastInt, clone.getUniqueId());
            lastInt++;
        }
    }

    public void selectAll(String block) {
        selectedStands.clear();
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(entry.getValue());
            if (armorStand.getEquipment().getHelmet().getType().toString().toLowerCase().contains(block.toLowerCase())) {
                selectedStands.add(entry.getValue());
            }
        }

    }

    public void deselectAll(String block) {
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(entry.getValue());
            if (armorStand.getEquipment().getHelmet().getType().toString().toLowerCase().contains(block.toLowerCase())) {
                selectedStands.remove(entry.getValue());
            }
        }
    }

    public void undo() {
        allArmorStands.remove(lastInt - 1);
        seats.remove(selectedStands.get(selectedStands.size() - 1));
        Bukkit.getEntity(selectedStands.get(selectedStands.size() - 1)).remove();
        selectedStands.remove(selectedStands.size() - 1);
        lastInt--;
        selectedStands.add(allArmorStands.get(lastInt - 1));
    }

    public void clear() {
        lastInt = 0;
        for (UUID uuid : allArmorStands.values()) {
            if (Bukkit.getEntity(uuid) != null) {
                Bukkit.getEntity(uuid).remove();
            }
        }
        allArmorStands.clear();
        selectedStands.clear();
    }

    public void move(double x, double y, double z) {
        for (ArmorStand armorStand : getSelectedStands()) {
            armorStand.teleport(armorStand.getLocation().clone().add(x, y, z));
        }
    }

    public void rotateTotal(double x, double y, double z) {
        for (ArmorStand armorStand : getSelectedStands()) {
            Location location = armorStand.getLocation().clone();
            Location desiredLocation = location.setDirection(location.getDirection().clone().rotateAroundY(x));
            desiredLocation.setDirection(desiredLocation.getDirection().clone().setX(Math.round(desiredLocation.getDirection().clone().getX())));
            desiredLocation.setDirection(desiredLocation.getDirection().clone().setY(Math.round(desiredLocation.getDirection().clone().getY())));
            desiredLocation.setDirection(desiredLocation.getDirection().clone().setZ(Math.round(desiredLocation.getDirection().clone().getZ())));
            armorStand.teleport(desiredLocation);
        }
    }

    public void rotateArm(boolean left, double x, double y, double z) {
        for (ArmorStand armorStand : getSelectedStands()) {
            if (left) {
                armorStand.setLeftArmPose(armorStand.getLeftArmPose().add(x, y, z));
                armorStand.setLeftArmPose(armorStand.getLeftArmPose().setX(Math.round(armorStand.getLeftArmPose().getX())));
                armorStand.setLeftArmPose(armorStand.getLeftArmPose().setY(Math.round(armorStand.getLeftArmPose().getY())));
                armorStand.setLeftArmPose(armorStand.getLeftArmPose().setZ(Math.round(armorStand.getLeftArmPose().getZ())));
            } else {
                armorStand.setRightArmPose(armorStand.getRightArmPose().add(x, y, z));
                armorStand.setRightArmPose(armorStand.getRightArmPose().setX(Math.round(armorStand.getRightArmPose().getX())));
                armorStand.setRightArmPose(armorStand.getRightArmPose().setY(Math.round(armorStand.getRightArmPose().getY())));
                armorStand.setRightArmPose(armorStand.getRightArmPose().setZ(Math.round(armorStand.getRightArmPose().getZ())));
            }
        }
    }

    public void update() {
        if (getSelectedStands() == null) {
            return;
        }
        for (ArmorStand armorStand : getSelectedStands()) {
            armorStand.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1, true, false, false));
        }
        seat.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, seat.clone(), 0);
        for (UUID uuid : seats) {
            ArmorStand seat = (ArmorStand) Bukkit.getEntity(uuid);
            seat.getWorld().spawnParticle(Particle.FLAME, seat.getLocation().clone().add(0, 1.8, 0), 0);
            seat.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10, 1, true, false, false));
        }
        Vector direction = forwards.clone().normalize().multiply(5);
        Location start = origin.clone();
        int parts = 10;
        double percent = direction.length() / parts / direction.length();
        for (int i = 0; i < parts; i++) {
            start.add(direction.clone().multiply(percent));
            start.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, start.clone(), 0);
        }
    }

    public void save(String name) {
        if (getSelectedStands() == null) {
            return;
        }
        VehiclesPlugin.storage.getConfig().set(name, null);
        VehiclesPlugin.storage.getConfig().set(name + ".count", lastInt);
        VehiclesPlugin.storage.getConfig().set(name + ".seat", Utils.convertToString(seat.clone().subtract(origin).subtract(0, 1, 0).toVector()));
        VehiclesPlugin.storage.getConfig().set(name + ".extraseatcount", seats.size());
        for (int i = 0; i < seats.size(); i++) {
            UUID uuid = seats.get(i);
            VehiclesPlugin.storage.getConfig().set(name + ".exs." + i, Utils.convertToString(Bukkit.getEntity(uuid).getLocation().clone().subtract(origin).toVector()));
        }
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            if (seats.contains(entry.getValue())) {
                VehiclesPlugin.storage.getConfig().set(name + ".am." + entry.getKey(), "seat");
                continue;
            }
            String extension = "";
            if (blades.contains(entry.getValue())) {
                extension = "blade";
            }
            if (bladeOrigin.equals(entry.getValue())) {
                extension = "bladeOrigin";
            }
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(entry.getValue());
            StringBuilder stringBuilder = new StringBuilder(Utils.convertToString(armorStand.getLocation().clone().subtract(origin.clone()).toVector()));
            stringBuilder.append(";");
            stringBuilder.append(Utils.convertToString(armorStand.getLocation().clone().getDirection().clone()));
            stringBuilder.append(";");
            stringBuilder.append(Utils.convertToString(armorStand.getLeftArmPose()));
            stringBuilder.append(";");
            stringBuilder.append(Utils.convertToString(armorStand.getRightArmPose()));
            stringBuilder.append(";");
            stringBuilder.append(armorStand.getEquipment().getHelmet() != null ? armorStand.getEquipment().getHelmet().getType().name() : "null");
            stringBuilder.append(";");
            stringBuilder.append(armorStand.getEquipment().getItemInMainHand().getType().name());
            stringBuilder.append(";");
            stringBuilder.append(armorStand.getEquipment().getItemInOffHand().getType().name());
            stringBuilder.append(";");
            stringBuilder.append(extension);
            VehiclesPlugin.storage.getConfig().set(name + ".am." + entry.getKey(), stringBuilder.toString());
        }
        try {
            VehiclesPlugin.storage.saveConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadCreation(String name, Location origin, Player owner) {
        List<UUID> seats = loadSeats(name, origin, null);
        List<UUID> armorStands = loadArmorStands(name, origin, null);
        List<UUID> blades = loadBlades(name, origin, null);
        UUID bladeOrigin = loadBladeOrigin(name, origin, null);
        Vector seatOffset = Utils.convertToVector(VehiclesPlugin.storage.getConfig().getString(name + ".seat"));
        ArmorStandCreation armorStandCreation = new ArmorStandCreation(origin);
        armorStandCreation.addAll(armorStands);
        armorStandCreation.addAll(seats);
        armorStandCreation.addAll(blades);
        armorStandCreation.addAll(new ArrayList<>(Collections.singletonList(bladeOrigin)));
        armorStandCreation.seat = origin.clone().add(seatOffset);
        armorStandCreation.blades.addAll(blades);
        armorStandCreation.bladeOrigin = bladeOrigin;
        armorStandCreation.seats.addAll(seats);
        CreationModeCommand.creationHashMap.put(owner.getUniqueId(), armorStandCreation);
    }

    public static List<UUID> loadSeats(String name, Location origin, UUID uuid) {
        int seatCount = 0;
        try {
            seatCount = VehiclesPlugin.storage.getConfig().getInt(name + ".extraseatcount");
        } catch (Exception e) {
            // ignore
        }
        List<UUID> extraSeats = new ArrayList<>();
        for (int i = 0; i < seatCount; i++) {
            String exs = VehiclesPlugin.storage.getConfig().getString(name + ".exs." + i);
            String[] exsSplit = exs.split(";");
            Vector exsVector = new Vector(Double.parseDouble(exsSplit[0]), Double.parseDouble(exsSplit[1]), Double.parseDouble(exsSplit[2]));
            ArmorStand extraSeat = (ArmorStand) origin.getWorld().spawnEntity(origin.clone().add(exsVector), EntityType.ARMOR_STAND);
            extraSeat.setGravity(false);
            extraSeat.setVisible(false);
            extraSeat.setBasePlate(false);
            extraSeat.setArms(true);
            extraSeat.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeats.add(extraSeat.getUniqueId());

            if (uuid != null) {
                PersistentDataContainer persistentDataContainer = extraSeat.getPersistentDataContainer();
                persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, uuid.toString());
            }
        }

        return extraSeats;
    }

    public static UUID loadBladeOrigin(String name, Location origin, UUID uuid) {
        UUID armorStandId = null;
        int count = VehiclesPlugin.storage.getConfig().getInt(name + ".count");
        for (int i = 0; i < count; i++) {
            String key = VehiclesPlugin.storage.getConfig().getString(name + ".am." + i);
            if (!key.endsWith("bladeOrigin")) {
                continue;
            }
            String[] split = key.split(";");
            Vector vector = new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
            Vector direction = new Vector(Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]));
            EulerAngle leftArm = new EulerAngle(Double.parseDouble(split[6]), Double.parseDouble(split[7]), Double.parseDouble(split[8]));
            EulerAngle rightArm = new EulerAngle(Double.parseDouble(split[9]), Double.parseDouble(split[10]), Double.parseDouble(split[11]));
            ArmorStand armorStand = (ArmorStand) origin.getWorld().spawnEntity(origin.clone().add(vector), EntityType.ARMOR_STAND);
            Location location = armorStand.getLocation().clone();
            location.setDirection(direction);
            armorStand.teleport(location);
            armorStand.setLeftArmPose(leftArm);
            armorStand.setRightArmPose(rightArm);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setBasePlate(false);
            armorStand.setArms(true);
            armorStand.setCustomName("Part");
            armorStand.setCustomNameVisible(false);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(split[12])));
            armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.valueOf(split[13])));
            armorStand.getEquipment().setItemInOffHand(new ItemStack(Material.valueOf(split[14])));
            armorStandId = armorStand.getUniqueId();
            if (uuid != null) {
                PersistentDataContainer persistentDataContainer = armorStand.getPersistentDataContainer();
                persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, uuid.toString());
            }
        }
        return armorStandId;
    }

    public static List<UUID> loadBlades(String name, Location origin, UUID uuid) {
        List<UUID> armorStands = new ArrayList<>();
        int count = VehiclesPlugin.storage.getConfig().getInt(name + ".count");
        for (int i = 0; i < count; i++) {
            String key = VehiclesPlugin.storage.getConfig().getString(name + ".am." + i);
            if (!key.endsWith("blade")) {
                continue;
            }
            String[] split = key.split(";");
            Vector vector = new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
            Vector direction = new Vector(Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]));
            EulerAngle leftArm = new EulerAngle(Double.parseDouble(split[6]), Double.parseDouble(split[7]), Double.parseDouble(split[8]));
            EulerAngle rightArm = new EulerAngle(Double.parseDouble(split[9]), Double.parseDouble(split[10]), Double.parseDouble(split[11]));
            ArmorStand armorStand = (ArmorStand) origin.getWorld().spawnEntity(origin.clone().add(vector), EntityType.ARMOR_STAND);
            Location location = armorStand.getLocation().clone();
            location.setDirection(direction);
            armorStand.teleport(location);
            armorStand.setLeftArmPose(leftArm);
            armorStand.setRightArmPose(rightArm);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setBasePlate(false);
            armorStand.setArms(true);
            armorStand.setCustomName("Part");
            armorStand.setCustomNameVisible(false);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(split[12])));
            armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.valueOf(split[13])));
            armorStand.getEquipment().setItemInOffHand(new ItemStack(Material.valueOf(split[14])));
            armorStands.add(armorStand.getUniqueId());

            if (uuid != null) {
                PersistentDataContainer persistentDataContainer = armorStand.getPersistentDataContainer();
                persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, uuid.toString());
            }
        }
        return armorStands;
    }

    public static List<UUID> loadArmorStands(String name, Location origin, UUID uuid) {
        List<UUID> armorStands = new ArrayList<>();
        int count = VehiclesPlugin.storage.getConfig().getInt(name + ".count");
        for (int i = 0; i < count; i++) {
            String key = VehiclesPlugin.storage.getConfig().getString(name + ".am." + i);
            if (key.equals("seat")) {
                continue;
            }
            if (key.endsWith("blade") || key.endsWith("bladeOrigin")) {
                continue;
            }
            String[] split = key.split(";");
            Vector vector = new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
            Vector direction = new Vector(Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]));
            EulerAngle leftArm = new EulerAngle(Double.parseDouble(split[6]), Double.parseDouble(split[7]), Double.parseDouble(split[8]));
            EulerAngle rightArm = new EulerAngle(Double.parseDouble(split[9]), Double.parseDouble(split[10]), Double.parseDouble(split[11]));
            ArmorStand armorStand = (ArmorStand) origin.getWorld().spawnEntity(origin.clone().add(vector), EntityType.ARMOR_STAND);
            Location location = armorStand.getLocation().clone();
            location.setDirection(direction);
            armorStand.teleport(location);
            armorStand.setLeftArmPose(leftArm);
            armorStand.setRightArmPose(rightArm);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setBasePlate(false);
            armorStand.setArms(true);
            armorStand.setCustomName("Part");
            armorStand.setCustomNameVisible(false);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(split[12])));
            armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.valueOf(split[13])));
            armorStand.getEquipment().setItemInOffHand(new ItemStack(Material.valueOf(split[14])));
            armorStands.add(armorStand.getUniqueId());

            if (uuid != null) {
                PersistentDataContainer persistentDataContainer = armorStand.getPersistentDataContainer();
                persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, uuid.toString());
            }
        }
        return armorStands;
    }

    public static me.swipez.vehicles.vehicles.Vehicle load(String name, Location origin, VehicleType vehicle, UUID owner) {

        UUID uuid = UUID.randomUUID();
        String seat = VehiclesPlugin.storage.getConfig().getString(name + ".seat");
        String[] seatSplit = seat.split(";");
        Vector seatVector = new Vector(Double.parseDouble(seatSplit[0]), Double.parseDouble(seatSplit[1]), Double.parseDouble(seatSplit[2]));

        List<UUID> armorStands = loadArmorStands(name, origin, uuid);
        List<UUID> extraSeats = loadSeats(name, origin, uuid);
        List<UUID> blades = loadBlades(name, origin, uuid);
        UUID bladeOrigin = loadBladeOrigin(name, origin, uuid);

        ArmorStand realSeat = origin.getWorld().spawn(origin.clone().add(seatVector.clone().subtract(new Vector(0, 0.7, 0))), ArmorStand.class, (pig1 -> {
            pig1.setGravity(false);
            pig1.setVisible(false);
            pig1.setBasePlate(false);
            pig1.setArms(true);
            pig1.getEquipment().setHelmet(new ItemStack(Material.AIR));
            pig1.setCustomName("Seat");
            pig1.setCustomNameVisible(false);
            pig1.setRemoveWhenFarAway(false);
            pig1.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            pig1.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            pig1.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            pig1.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            pig1.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            pig1.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            pig1.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            pig1.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            pig1.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            pig1.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            pig1.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            pig1.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            PersistentDataContainer persistentDataContainer = pig1.getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING, uuid.toString());
        }));
        if (bladeOrigin != null) {
            return buildPlane(realSeat.getUniqueId(), armorStands, new Vector(1, 0, 0), origin.clone(), name, extraSeats, vehicle, owner, uuid, blades, bladeOrigin, vehicle.rotateX);
        }
        return buildVehicle(realSeat.getUniqueId(), armorStands, new Vector(1, 0, 0), origin.clone(), name, extraSeats, vehicle, owner, uuid);
    }

    private static Plane buildPlane(UUID seat, List<UUID> armorStands, Vector forwards, Location origin, String name, List<UUID> extraSeats, VehicleType vehicleType, UUID owner, UUID vehicleId, List<UUID> blades, UUID bladeOrigin, boolean rotateX) {
        if (VehiclesPlugin.MINECRAFT_VERSION == 19) {
            return new Plane_1_19_4(seat, armorStands, forwards, origin, name, extraSeats, vehicleType, owner, vehicleId, blades, bladeOrigin, rotateX);
        } else {
            return new Plane_1_20_2(seat, armorStands, forwards, origin, name, extraSeats, vehicleType, owner, vehicleId, blades, bladeOrigin, rotateX);
        }
    }

    private static Vehicle buildVehicle(UUID seat, List<UUID> armorStands, Vector forwards, Location origin, String name, List<UUID> extraSeats, VehicleType vehicleType, UUID owner, UUID vehicleId) {
        if (VehiclesPlugin.MINECRAFT_VERSION == 19) {
            return new Vehicle_1_19_4(seat, armorStands, forwards, origin, name, extraSeats, vehicleType, owner, vehicleId);
        } else {
            return new Vehicle_1_20_2(seat, armorStands, forwards, origin, name, extraSeats, vehicleType, owner, vehicleId);
        }
    }

    public List<ArmorStand> getSelectedStands() {
        if (selectedStands.isEmpty()) {
            return null;
        }
        // Convert list of uuids to list of armorstands
        List<ArmorStand> armorStands = new ArrayList<>();
        for (UUID uuid : selectedStands) {
            armorStands.add((ArmorStand) Bukkit.getEntity(uuid));
        }
        return armorStands;
    }
}
