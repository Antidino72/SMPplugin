package fr.antidino.smp.core;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermissionsManager {
    private static final Logger log = LoggerFactory.getLogger(PermissionsManager.class);

    public static Permission RegisterPermission(FileConfiguration config){
        Permission perm_default = new Permission("smp.permssion.null");
        String name = config.getString("permission.name");
        String desc = config.getString("permission.description");
        String defaultStr = config.getString("permission.default");
        if (name == null) return perm_default;
        PermissionDefault permissionDefault = PermissionsManager.parseDefault(defaultStr);
        Permission permission = new Permission(name, desc, permissionDefault);

        if (Bukkit.getPluginManager().getPermission(name) == null){
            Bukkit.getPluginManager().addPermission(permission);
            return permission;
        }

        return perm_default;
    }
    public static PermissionDefault parseDefault(String def) {
        if (def == null) return PermissionDefault.OP;
        switch (def.toLowerCase()) {
            case "true":
            case "everyone":
                return PermissionDefault.TRUE;
            case "false":
            case "nobody":
                return PermissionDefault.FALSE;
            case "notop":
                return PermissionDefault.NOT_OP;
            case "op":
            default:
                return PermissionDefault.OP;
        }
    }
}
