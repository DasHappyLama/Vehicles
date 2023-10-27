package me.swipez.vehicles;

import org.bukkit.Bukkit;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.regex.Pattern;

public class Utils {

    public static String convertToString(Vector vector){
        return vector.getX() + ";" + vector.getY() + ";" + vector.getZ();
    }

    public static String convertToString(EulerAngle eulerAngle){
        return eulerAngle.getX() + ";" + eulerAngle.getY() + ";" + eulerAngle.getZ();
    }

    public static Vector convertToVector(String string){
        String[] split = string.split(";");
        return new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
    }

    public static int getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String completeVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
        String version = completeVersion.split(Pattern.quote("_"))[1];

        return Integer.parseInt(version);
    }
}
