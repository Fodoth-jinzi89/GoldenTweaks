package net.fodoth.skina.goldentweaks.util;

public class GTState {
    private static volatile boolean ready;

    public static void setReady() {
        ready = true;
    }

    public static boolean isReady() {
        return ready;
    }
}
