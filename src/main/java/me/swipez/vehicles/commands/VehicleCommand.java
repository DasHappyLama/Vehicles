package me.swipez.vehicles.commands;

import me.swipez.vehicles.ArmorStandCreation;
import me.swipez.vehicles.vehicles.Vehicle;
import me.swipez.vehicles.VehicleType;
import me.swipez.vehicles.VehiclesPlugin;
import me.swipez.vehicles.items.ItemRegistry;
import me.swipez.vehicles.recipe.VehicleBox;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VehicleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player){
            if (!player.hasPermission("vehicles.admin")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                return true;
            }
            if (args.length == 2){
                if (args[0].equals("load")){
                    String name = args[1];
                    VehicleType vehicleType;
                    try {
                        vehicleType = VehicleType.valueOf(name.toUpperCase());
                    } catch (IllegalArgumentException e){
                        player.sendMessage(ChatColor.RED+"Vehicle not found, resorting to config names");
                        ArmorStandCreation.load(name, player.getLocation().clone(), null, player.getUniqueId());
                        return true;
                    }
                    ArmorStandCreation.load(vehicleType.carName, player.getLocation().clone(), vehicleType, player.getUniqueId());
                }
                if (args[0].equalsIgnoreCase("removerange")){
                    double distance;
                    try {
                        distance = Double.parseDouble(args[1]);
                    } catch (NumberFormatException e){
                        player.sendMessage(ChatColor.RED+"Invalid number, usage: /vehicle removerange <distance>");
                        return true;
                    }
                    List<Vehicle> vehiclesToRemove = new ArrayList<>();
                    for (Vehicle vehicle : VehiclesPlugin.vehicles.values()){
                        if (vehicle.getOrigin().distance(player.getLocation()) < distance){
                            vehiclesToRemove.add(vehicle);
                        }
                    }
                    for (Vehicle vehicle : vehiclesToRemove){
                        vehicle.remove(true);
                    }
                    player.sendMessage(ChatColor.GREEN+"Removed "+vehiclesToRemove.size()+" vehicles");
                }
                if (args[0].equalsIgnoreCase("list")){
                    String name = args[1];
                    if (VehiclesPlugin.nameMappings.containsKey(name.toLowerCase())){
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(VehiclesPlugin.nameMappings.get(name));
                        player.sendMessage(ChatColor.YELLOW+"-------------------");
                        player.sendMessage(ChatColor.YELLOW+"Vehicles owned by: "+offlinePlayer.getName());
                        TextComponent textComponent = new TextComponent(ChatColor.GRAY+"Vehicles: ");
                        player.spigot().sendMessage(textComponent);
                        for (Vehicle vehicle : VehiclesPlugin.vehiclesOwnedByPlayers.get(VehiclesPlugin.nameMappings.get(name.toLowerCase()))){
                            TextComponent nameComponent = new TextComponent(ChatColor.GOLD+"- "+vehicle.getEnumName());
                            TextComponent clickableComponent = new TextComponent(ChatColor.BLUE+" [Click to Teleport]");
                            clickableComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vehicle teleport "+vehicle.getUniqueId()));
                            nameComponent.addExtra(clickableComponent);
                            player.spigot().sendMessage(nameComponent);
                        }
                        player.sendMessage(ChatColor.YELLOW+"-------------------");
                    }
                }
                if (args[0].equalsIgnoreCase("give")){
                    VehicleType vehicleType;
                    try {
                        vehicleType = VehicleType.valueOf(args[1].toUpperCase());
                    } catch (IllegalArgumentException e){
                        player.sendMessage(ChatColor.RED+"Invalid vehicle.");
                        return true;
                    }
                    player.getInventory().addItem(VehicleBox.vehicleBoxes.get(vehicleType).getItemStack());
                    return true;
                }
                if (args[0].equalsIgnoreCase("teleport")){
                    Vehicle vehicle = VehiclesPlugin.vehicles.get(UUID.fromString(args[1]));
                    if (vehicle != null){
                        player.teleport(vehicle.getOrigin());
                    }
                }
            }
            else if (args.length == 1){
                if (args[0].equals("give")){
                    player.getInventory().addItem(ItemRegistry.MOTORCYCLE_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.MONSTER_TRUCK_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.NORMAL_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.VAN_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.HOTROD_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.FAMILY_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.FORMULA_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.PRIVATE_JET_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.SPY_COPTER_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.BIPLANE_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.JET_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.HELICOPTER_BOX.getItemStack());
                    player.getInventory().addItem(ItemRegistry.TOASTER_BOX.getItemStack());
                }
                if (args[0].equals("clear")){
                    List<Vehicle> allVehicles = new ArrayList<>();
                    for (UUID uuid : VehiclesPlugin.vehicles.keySet()){
                        Vehicle vehicle = VehiclesPlugin.vehicles.get(uuid);
                        allVehicles.add(vehicle);
                    }
                    for (Vehicle vehicle : allVehicles){
                        vehicle.remove(true);
                    }
                    player.sendMessage(ChatColor.GREEN+"All vehicles removed");
                }
            }
        }
        return true;
    }

    public static class VehicleCommandCompleter implements TabCompleter {
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
            if (args.length == 1){
                Completion completion = new Completion();
                completion.add("give");
                completion.add("clear");
                completion.add("list");
                completion.add("removerange");
                return completion.getAvailable(args[0]);
            }
            if (args.length == 2){
                if (args[0].equalsIgnoreCase("give")){
                    Completion completion = new Completion();
                    for (VehicleType vehicleType : VehicleType.values()){
                        completion.add(vehicleType.name());
                    }
                    return completion.getAvailable(args[1]);
                }
                else if (args[0].equalsIgnoreCase("list")){
                    Completion completion = new Completion();
                    for (String name : VehiclesPlugin.nameMappings.keySet()){
                        completion.add(name);
                    }
                    return completion.getAvailable(args[1]);
                }
                else {
                    return new ArrayList<>();
                }
            }
            return new ArrayList<>();
        }
    }
}
