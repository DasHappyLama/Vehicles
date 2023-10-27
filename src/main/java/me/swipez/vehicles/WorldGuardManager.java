package me.swipez.vehicles;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

public class WorldGuardManager {

    private WorldGuard instance;
    private StateFlag CAN_PLACE_VEHICLES;

    public WorldGuardManager(){
        instance = WorldGuard.getInstance();
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("sw-vehicle-place", true);
            registry.register(flag);
            CAN_PLACE_VEHICLES = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("sw-vehicle-place");
            if (existing instanceof StateFlag) {
                CAN_PLACE_VEHICLES = (StateFlag) existing;
            }
        }
    }

    public boolean canPlace(Location location, Player player){
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = instance.getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        boolean canBypass = WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(Objects.requireNonNull(location.getWorld())));
        if (!query.testState(BukkitAdapter.adapt(location), localPlayer, CAN_PLACE_VEHICLES)){
            return canBypass;
        }
        return true;
    }
}
