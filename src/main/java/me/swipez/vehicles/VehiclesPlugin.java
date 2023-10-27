package me.swipez.vehicles;

import me.swipez.vehicles.commands.ArmorStandMakeCommand;
import me.swipez.vehicles.commands.CreationModeCommand;
import me.swipez.vehicles.commands.VehicleCommand;
import me.swipez.vehicles.config.ConfigGenerator;
import me.swipez.vehicles.gui.GeneralListeners;
import me.swipez.vehicles.items.ItemRegistry;
import me.swipez.vehicles.settings.PluginSettings;
import me.swipez.vehicles.vehicles.Vehicle;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings({"ResultOfMethodCallIgnored", "CallToPrintStackTrace", "SameParameterValue"})
public final class VehiclesPlugin extends JavaPlugin {

    private static JavaPlugin plugin;

    public static ConfigGenerator storage;
    public static ConfigGenerator persistentStorage;
    public static ConfigGenerator mainConfig;
    public static HashMap<String, UUID> nameMappings = new HashMap<>();
    public static HashMap<UUID, Vehicle> vehicles = new HashMap<>();
    public static HashMap<UUID, List<Vehicle>> vehiclesOwnedByPlayers = new HashMap<>();
    public static HashMap<Location, Vehicle> delayedVehicles = new HashMap<>();
    public static List<UUID> allSeats = new ArrayList<>();

    // THIS ENABLES CREATION FUNCTIONS FOR MAKING CARS, FOR OTHERS THAT ARE CREATING CARS, GOOD LUCK FIGURING OUT HOW TO USE IT MUAHAHAHA
    public static boolean creatorModeActive = true;

    public static PluginSettings settings;
    public static WorldGuardManager worldGuardManager = null;

    public static int MINECRAFT_VERSION;

    @Override
    public void onEnable() {
        // Plugin startup logic
        File mainFolder = new File(getDataFolder().getPath());
        if (!mainFolder.exists()){
            mainFolder.mkdir();
        }

        MINECRAFT_VERSION = Utils.getServerVersion();

        if (MINECRAFT_VERSION != 19 && MINECRAFT_VERSION != 20) {
            Bukkit.getLogger().severe("Unsupported minecraft version.");
            return;
        }

        attemptToUpdateCarFile();
        try {
            worldGuardManager = new WorldGuardManager();
        }
        catch (NoClassDefFoundError e){
            Bukkit.getLogger().info("WorldGuard not found, disabling WorldGuard support.");
        }
        // Export stored cars
        storage = new ConfigGenerator(getDataFolder(), "stored_cars");
        persistentStorage = new ConfigGenerator(getDataFolder(), "persistence_file");
        mainConfig = new ConfigGenerator(getDataFolder(), "config");
        mainConfig.addDefaultToConfig("owner-only-drivers", false);
        mainConfig.addDefaultToConfig("planes", true);
        mainConfig.buildConfig();


        plugin = this;
        Objects.requireNonNull(getCommand("vehicle")).setExecutor(new VehicleCommand());
        Objects.requireNonNull(getCommand("vehicle")).setTabCompleter(new VehicleCommand.VehicleCommandCompleter());
        Objects.requireNonNull(getCommand("creationmode")).setExecutor(new CreationModeCommand());
        Objects.requireNonNull(getCommand("armorstandify")).setExecutor(new ArmorStandMakeCommand());
        getServer().getPluginManager().registerEvents(new GeneralListeners(), this);
        repeatTask();
        ItemRegistry.registerRecipes();

        loadFromFile();

        settings = new PluginSettings();
    }

    public void saveToFile(){
        getLogger().info("Saving to file");
        persistentStorage.getConfig().set("vh", null);
        int vehicleCount = vehicles.keySet().size();
        int currentLoop = 0;
        persistentStorage.getConfig().set("vehicleCount", vehicleCount);

        for (UUID uuid : vehicles.keySet()){
            Vehicle vehicle = vehicles.get(uuid);
            String builder = Objects.requireNonNull(vehicle.getOrigin().getWorld()).getName() +
                    ";" +
                    Utils.convertToString(vehicle.getOrigin().clone().toVector()) +
                    ";" +
                    vehicle.getEnumName() +
                    ";" +
                    (vehicle.getColor() == null ? "null" : vehicle.getColor()) +
                    ";" +
                    vehicle.getOwner();
            persistentStorage.getConfig().set("vh."+currentLoop, builder);
            currentLoop++;
        }

        try {
            persistentStorage.saveConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        getLogger().info("Saved "+vehicleCount+" vehicles");
    }

    public void loadFromFile(){
        getLogger().info("Loading from persistence file");
        int vehicleCount = persistentStorage.getConfig().getInt("vehicleCount");
        for (int i = 0; i < vehicleCount; i++){
            String vehicleString = persistentStorage.getConfig().getString("vh."+i);
            assert vehicleString != null;
            String[] vehicleSplit = vehicleString.split(Pattern.quote(";"));
            World world = getServer().getWorld(vehicleSplit[0]);
            Location location = new Location(world, Double.parseDouble(vehicleSplit[1]), Double.parseDouble(vehicleSplit[2]), Double.parseDouble(vehicleSplit[3]));
            String name = vehicleSplit[4];
            String color = vehicleSplit[5];
            UUID owner = UUID.fromString(vehicleSplit[6]);
            VehicleType vehicle = VehicleType.valueOf(name);
            Vehicle spawned = ArmorStandCreation.load(vehicle.carName, location, vehicle, owner);
            if (!color.equals("null")){
                spawned.setColor(color);
                spawned.dye(color);
            }
        }
        getLogger().info("Loaded "+vehicleCount+" vehicles");
    }

    private void attemptToUpdateCarFile(){
        File destination = new File(getDataFolder().getPath() + File.separator + "stored_cars.yml");
        getLogger().info("Attempting to export stored cars...");
        if (!destination.exists()){
            getLogger().info("No stored cars found, exporting...");
            exportResourceToFile("stored_cars.yml", destination);
        }
        else {
            getLogger().info("Stored cars found, comparing versions.");
            File otherTemp = new File(getDataFolder().getPath() + File.separator + "stored_cars_temp.yml");
            getLogger().info("Exporting stored cars to temporary file...");
            // Export temporary file
            exportResourceToFile("stored_cars.yml", otherTemp);

            // Make config
            YamlConfiguration tempConfig = YamlConfiguration.loadConfiguration(otherTemp);
            YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(destination);

            // Compare values
            double currentValue = tempConfig.getDouble("version");
            double newValue = currentConfig.getDouble("version");

            getLogger().info("Current version: " + newValue);
            getLogger().info("One Stored in Jars version: " + currentValue);

            // If the version inside the jar is higher than the one that is stored, copy the temporary file to the stored file
            if (currentValue > newValue){
                getLogger().info("New version found, copying to stored file...");
                exportResourceToFile("stored_cars.yml", destination);
            }
            otherTemp.delete();

        }
    }

    private void repeatTask(){
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()){
                    for (Location location : delayedVehicles.keySet()){
                        if (location.distance(player.getLocation()) < 50){
                            Vehicle vehicle = delayedVehicles.get(location);
                            vehicle.runDelayedActions();
                            delayedVehicles.remove(location);
                        }
                    }
                }
                for (World world : getServer().getWorlds()){
                    world.getEntities().forEach(entity -> {
                        if (entity instanceof ArmorStand){
                            PersistentDataContainer persistentDataContainer = entity.getPersistentDataContainer();
                            if (persistentDataContainer.has(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING)){
                                UUID vehicleId = UUID.fromString(Objects.requireNonNull(persistentDataContainer.get(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleId"), PersistentDataType.STRING)));
                                if (!vehicles.containsKey(vehicleId)){
                                    entity.remove();
                                }
                            }
                        }
                    });
                }
                for (UUID uuid : vehicles.keySet()){
                    vehicles.get(uuid).update();
                }
                for (UUID uuid : CreationModeCommand.creationHashMap.keySet()){
                    ArmorStandCreation armorStandCreation = CreationModeCommand.creationHashMap.get(uuid);
                    armorStandCreation.update();
                }
            }
        }.runTaskTimer(this, 1, 1);
    }

    private void exportResourceToFile(String resourcePath, File outFile){
        try {
            InputStream inputStream = getResource(resourcePath);
            assert inputStream != null;
            Files.copy(inputStream, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void onDisable() {
        saveToFile();
        for (UUID uuid : vehicles.keySet()){
            vehicles.get(uuid).remove(false);
        }
    }
}
