package me.swipez.vehicles.gui;

import me.swipez.vehicles.ArmorStandCreation;
import me.swipez.vehicles.VehiclesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EquipmentGUI implements Listener {

    private final Inventory displayInventory;
    private ArmorStandCreation armorStandCreation;
    private UUID owner;

    public EquipmentGUI(ArmorStandCreation creation, UUID owner) {
        this.owner = owner;
        this.displayInventory = Bukkit.createInventory(null, 9, ChatColor.RED + "ArmorStand Equipment");
        this.armorStandCreation = creation;
        VehiclesPlugin.getPlugin().getServer().getPluginManager().registerEvents(this, VehiclesPlugin.getPlugin());
    }

    public void display(Player player){
        displayInventory.clear();
        ArmorStand selected = armorStandCreation.getSelectedStands().get(0);
        displayInventory.setItem(0, displayOrNull(Objects.requireNonNull(selected.getEquipment()).getHelmet(), "Helmet"));
        displayInventory.setItem(1, displayOrNull(selected.getEquipment().getChestplate(), "Chestplate"));
        displayInventory.setItem(2, displayOrNull(selected.getEquipment().getLeggings(), "Leggings"));
        displayInventory.setItem(3, displayOrNull(selected.getEquipment().getBoots(), "Boots"));
        displayInventory.setItem(4, displayOrNull(selected.getEquipment().getItemInMainHand(), "Mainhand"));
        displayInventory.setItem(5, displayOrNull(selected.getEquipment().getItemInOffHand(), "Offhand"));

        for (int i = 0; i < displayInventory.getSize(); i++){
            if (displayInventory.getItem(i) == null){
                displayInventory.setItem(i, generateDummyItem());
            }
        }
        player.openInventory(displayInventory);
    }

    @EventHandler
    public void onPlayerClicksInventory(InventoryClickEvent event){
        if (event.getClickedInventory() == null){
            return;
        }
        if (!event.getWhoClicked().getUniqueId().equals(owner)){
            return;
        }
        if (event.getView().getTitle().equals(ChatColor.RED + "ArmorStand Equipment")){
            if (event.getClickedInventory().contains(generateDummyItem())){
                Player player = (Player) event.getWhoClicked();
                if (event.getClick().isLeftClick()){
                    if (event.getCursor() != null){
                        if (attemptSet(event.getSlot(), event.getCursor())){
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                            player.closeInventory();
                            display(player);
                        }
                        else {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        }
                    }
                }
                else if (event.getClick().isRightClick()){
                    if (attemptClear(event.getSlot())){
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        player.closeInventory();
                        display(player);
                    }
                    else {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    public boolean attemptClear(int slot){
        ArmorStand selected = armorStandCreation.getSelectedStands().get(0);
        if (selected == null){
            return false;
        }
        if (slot == 0){
            Objects.requireNonNull(selected.getEquipment()).setHelmet(null);
            return true;
        }
        else if (slot == 1){
            Objects.requireNonNull(selected.getEquipment()).setChestplate(null);
            return true;
        }
        else if (slot == 2){
            Objects.requireNonNull(selected.getEquipment()).setLeggings(null);
            return true;
        }
        else if (slot == 3){
            Objects.requireNonNull(selected.getEquipment()).setBoots(null);
            return true;
        }
        else if (slot == 4){
            Objects.requireNonNull(selected.getEquipment()).setItemInMainHand(null);
            return true;
        }
        else if (slot == 5){
            Objects.requireNonNull(selected.getEquipment()).setItemInOffHand(null);
            return true;
        }
        return false;
    }

    public boolean attemptSet(int slot, ItemStack itemStack){
        ArmorStand selected = armorStandCreation.getSelectedStands().get(0);
        if (selected == null){
            return false;
        }
        if (slot == 0){
            Objects.requireNonNull(selected.getEquipment()).setHelmet(itemStack.clone());
            return true;
        }
        if (slot == 1){
            Objects.requireNonNull(selected.getEquipment()).setChestplate(itemStack.clone());
            return true;
        }
        if (slot == 2){
            Objects.requireNonNull(selected.getEquipment()).setLeggings(itemStack.clone());
            return true;
        }
        if (slot == 3){
            Objects.requireNonNull(selected.getEquipment()).setBoots(itemStack.clone());
            return true;
        }
        if (slot == 4){
            Objects.requireNonNull(selected.getEquipment()).setItemInMainHand(itemStack.clone());
            return true;
        }
        if (slot == 5){
            Objects.requireNonNull(selected.getEquipment()).setItemInOffHand(itemStack.clone());
            return true;
        }
        return false;
    }

    private static ItemStack generateDummyItem(){
        ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName("");

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    private ItemStack displayOrNull(ItemStack stack, String title) {
        ItemStack itemStack;
        if (stack != null && stack.getItemMeta() != null) {
            itemStack = new ItemStack(stack.getType());
            ItemMeta meta = itemStack.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.GOLD + title);
            List<String> lore = new LinkedList<>();
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Right click to clear me!");
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        else {
            itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta meta = itemStack.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.RED+title);
            List<String> lore = new LinkedList<>();
            lore.add(ChatColor.GRAY.toString()+ChatColor.ITALIC+"Left click on this slot with an item!");
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
}
