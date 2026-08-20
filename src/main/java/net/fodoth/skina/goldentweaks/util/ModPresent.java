package net.fodoth.skina.goldentweaks.util;

public class ModPresent {
    public static boolean checkIfPresent(String mainClass) {
        String resource = mainClass.replace('.', '/') + ".class";
        return Thread.currentThread().getContextClassLoader().getResource(resource) != null;
    }
}
