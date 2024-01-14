package cn.xuexu.crasher;

import cn.xuexu.crasher.utils.Utils;
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
        Utils.unregisterInstance();
        getLogger().info("Disabled " + getDescription().getFullName());
    }
}
