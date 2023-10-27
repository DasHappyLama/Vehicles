package me.swipez.vehicles.commands;

import me.swipez.vehicles.ArmorStandCreation;
import me.swipez.vehicles.VehiclesPlugin;
import me.swipez.vehicles.items.ItemRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class CreationModeCommand implements CommandExecutor {

    public static HashMap<UUID, ArmorStandCreation> creationHashMap = new HashMap<>();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!VehiclesPlugin.creatorModeActive){
            return true;
        }
        if (sender instanceof Player player){
            if (args.length == 0){
                player.getInventory().addItem(ItemRegistry.CREATE_STAND);
                player.getInventory().addItem(ItemRegistry.EQUIPMENT_GUI);
                player.getInventory().addItem(ItemRegistry.ROTATE_CONTROL);
                player.getInventory().addItem(ItemRegistry.MOVE_CONTROL);
                if (!creationHashMap.containsKey(player.getUniqueId())){
                    creationHashMap.put(player.getUniqueId(), new ArmorStandCreation(player.getLocation().getBlock().getLocation()));
                }
            }
            else {
                if (args[0].equals("load")){
                    ArmorStandCreation.loadCreation(args[1], player.getLocation().clone(), player);
                }
                if (args[0].equals("save")){
                    String name =  args[1];
                    creationHashMap.get(player.getUniqueId()).save(name);
                    player.sendMessage(ChatColor.GREEN+"Saved structure to "+name);
                }
                if (args[0].equals("clear")){
                    creationHashMap.get(player.getUniqueId()).clear();
                }
                if (args[0].equals("undo")){
                    creationHashMap.get(player.getUniqueId()).undo();
                }
                if (args[0].equals("move")){
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    creationHashMap.get(player.getUniqueId()).move(x, y, z);
                }
                if (args[0].equals("moveseat")){
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    creationHashMap.get(player.getUniqueId()).moveSeat(x, y, z);
                }
                if (args[0].equals("clone")){
                    creationHashMap.get(player.getUniqueId()).cloneStand();
                }
                if (args[0].equals("cycle")){
                    if (args.length == 1){
                        creationHashMap.get(player.getUniqueId()).cycle();
                    }
                    else {
                        creationHashMap.get(player.getUniqueId()).cycle(args[1]);
                    }
                }
                if (args[0].equals("moveall")){
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    creationHashMap.get(player.getUniqueId()).moveAll(x, y, z);
                }
                if (args[0].equals("selectall")){
                    creationHashMap.get(player.getUniqueId()).selectAll(args[1]);
                }
                if (args[0].equals("deselectall")){
                    creationHashMap.get(player.getUniqueId()).deselectAll(args[1]);
                }
                if (args[0].equals("seat")){
                    ArmorStandCreation armorStandCreation = creationHashMap.get(player.getUniqueId());
                    for (ArmorStand armorStand : armorStandCreation.getSelectedStands()){
                        if (armorStandCreation.seats.contains(armorStand.getUniqueId())){
                            armorStandCreation.seats.remove(armorStand.getUniqueId());
                            player.sendMessage(ChatColor.GREEN+"Undid a seat");
                        }
                        else {
                            armorStandCreation.seats.add(armorStand.getUniqueId());
                            player.sendMessage(ChatColor.GREEN+"Seat added");
                        }
                    }
                }
                if (args[0].equals("blade")){
                    ArmorStandCreation armorStandCreation = creationHashMap.get(player.getUniqueId());
                    armorStandCreation.makeBlade();
                }
                if (args[0].equalsIgnoreCase("bladeorigin")){
                    ArmorStandCreation armorStandCreation = creationHashMap.get(player.getUniqueId());
                    armorStandCreation.makeBladeOrigin();
                }
                if (args[0].equalsIgnoreCase("showid")){
                    creationHashMap.get(player.getUniqueId()).displayIds(args[1]);
                }
                if (args[0].equalsIgnoreCase("deselect")){
                    creationHashMap.get(player.getUniqueId()).deselect(Integer.parseInt(args[1]));
                }
                if (args[0].equalsIgnoreCase("select")){
                    creationHashMap.get(player.getUniqueId()).select(Integer.parseInt(args[1]));
                }
            }
        }
        return true;
    }
}
