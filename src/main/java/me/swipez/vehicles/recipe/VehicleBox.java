package me.swipez.vehicles.recipe;

import me.swipez.vehicles.VehicleType;
import me.swipez.vehicles.VehiclesPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VehicleBox implements Listener {
    public final VehicleType vehicle;
    public final String color;
    private final ChatColor chatColor;
    public static HashMap<VehicleType, VehicleBox> vehicleBoxes = new HashMap<>();

    public VehicleBox(VehicleType vehicle, String color, ChatColor chatColor) {
        this.vehicle = vehicle;
        this.color = color;
        this.chatColor = chatColor;
        if (!vehicleBoxes.containsKey(vehicle)) {
            vehicleBoxes.put(vehicle, this);
        }
    }

    public ItemStack getItemStack(){
        ItemStack itemStack = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(ChatColor.GOLD+"Vehicle Box: "+ChatColor.GREEN+ChatColor.BOLD+vehicle.carName.toUpperCase().replace("_", " "));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD+"Color: "+chatColor+ChatColor.BOLD+color.toUpperCase().replace("_", " "));
        itemMeta.setLore(lore);
        itemMeta.addEnchant(Enchantment.CHANNELING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleBox"), PersistentDataType.INTEGER, 1);
        persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicle"), PersistentDataType.STRING, vehicle.name());
        persistentDataContainer.set(new NamespacedKey(VehiclesPlugin.getPlugin(), "color"), PersistentDataType.STRING, color);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public VehicleBox getAlternateColor(String color, ChatColor chatColor){
        return new VehicleBox(vehicle, color, chatColor);
    }

    public static VehicleBox getBox(ItemStack itemStack, ChatColor chatColor){
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null){
            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
            if (persistentDataContainer.has(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleBox"), PersistentDataType.INTEGER)){
                int vehicleBox = persistentDataContainer.get(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicleBox"), PersistentDataType.INTEGER);
                if (vehicleBox == 1){
                    String vehicle = persistentDataContainer.get(new NamespacedKey(VehiclesPlugin.getPlugin(), "vehicle"), PersistentDataType.STRING);
                    String color = persistentDataContainer.get(new NamespacedKey(VehiclesPlugin.getPlugin(), "color"), PersistentDataType.STRING);
                    return new VehicleBox(VehicleType.valueOf(vehicle), color, chatColor);
                }
            }
        }
        return null;
    }
}
