package cn.xuexu.crasher;

import cn.xuexu.crasher.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Crasher extends JavaPlugin {
    @Override
    public void onLoad() {
        new Utils(this);
        getLogger().info("Loaded " + getDescription().getFullName());
    }

    @Override
    public void onEnable() {
        Utils.setupPlugin();
        getLogger().info("Enabled " + getDescription().getFullName());
    }

    @Override
    public void onDisable() {
        Utils.crashSet.stream().parallel()
                .forEach(uuid -> {
                    Utils.removeCrashSet(uuid);
                    if (Bukkit.getPlayer(uuid) != null) {
                        Utils.removePacketListener(Bukkit.getPlayer(uuid));
                    }
                });
        Utils.unregisterInstance();
        getLogger().info("Disabled " + getDescription().getFullName());
    }
}
