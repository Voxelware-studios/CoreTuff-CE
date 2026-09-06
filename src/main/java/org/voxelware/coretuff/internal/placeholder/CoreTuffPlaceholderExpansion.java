package org.voxelware.coretuff.internal.placeholder;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CoreTuffPlaceholderExpansion {

    private final PlaceholderRegistrarImpl registrar;

    public CoreTuffPlaceholderExpansion(PlaceholderRegistrarImpl registrar) {
        this.registrar = registrar;
    }

    public void register() {
        try {
            Class<?> expansionClass = Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion");
            Object expansion = java.lang.reflect.Proxy.newProxyInstance(
                    expansionClass.getClassLoader(),
                    new Class[]{expansionClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("onPlaceholderRequest")) {
                            Player player = args.length > 0 ? (Player) args[0] : null;
                            String params = args.length > 1 ? (String) args[1] : null;
                            return registrar.resolveAll(params, player);
                        }
                        if (method.getName().equals("getIdentifier")) return "coretuff";
                        if (method.getName().equals("getAuthor")) return "Voxelware Studios";
                        if (method.getName().equals("getVersion")) return "1.0";
                        if (method.getName().equals("persist")) return true;
                        if (method.getName().equals("canRegister")) return true;
                        if (method.getName().equals("register")) {
                            ((me.clip.placeholderapi.expansion.PlaceholderExpansion) proxy).register();
                            return true;
                        }
                        return method.getDefaultValue();
                    });
        } catch (Exception e) {
            // PlaceholderAPI not available
        }
    }
}
