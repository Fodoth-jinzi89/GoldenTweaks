package net.fodoth.skina.goldentweaks.util;

public class ModPresent {
    public static boolean checkIfPresent(String mainClass) {
        try {
            Class.forName(mainClass, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
