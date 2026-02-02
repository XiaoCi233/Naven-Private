package tech.blinkfix.modules;

public interface PermissionGatedModule {
    boolean hasPermission();

    default String getPermissionDenyMessage() {
        return "You not Admin or Beta.";
    }
}



