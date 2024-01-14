package cn.xuexu.crasher.listener;

import cn.xuexu.crasher.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public final class PlayerListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuit(final PlayerQuitEvent event) {
        Utils.frozenSet.remove(event.getPlayer().getUniqueId());
        Utils.packetReviveQueues.remove(event.getPlayer().getUniqueId());
        Utils.packetQueues.remove(event.getPlayer().getUniqueId());
        Utils.removeCrashSet(event.getPlayer().getUniqueId());
        Utils.removePacketListener(event.getPlayer());
        if (Utils.instance.getConfig().getBoolean("Settings.Frozen.QuitCommandBoolean") && Utils.frozenSet.contains(event.getPlayer().getUniqueId())) {
            for (final String cmd : Utils.instance.getConfig().getStringList("Settings.Frozen.QuitCommandList")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%player%", event.getPlayer().getName()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void inventoryClose(final InventoryCloseEvent event) {
        if (Utils.frozenSet.contains(event.getPlayer().getUniqueId()) && event.getPlayer().getOpenInventory() != null && event.getPlayer().getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.FrozenGUITitle"))) && event.getPlayer().getOpenInventory().getType().equals(InventoryType.CHEST)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getPlayer().openInventory(Utils.frozenInventory((Player) event.getPlayer()));
                }
            }.runTaskAsynchronously(Utils.instance);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void inventoryClick(final InventoryClickEvent event) {
        try {
            if (Utils.frozenSet.contains(event.getWhoClicked().getUniqueId()) && event.getInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.FrozenGUITitle"))) && event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
                event.setCancelled(true);
            }
        } catch (NullPointerException ignored) {
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerMove(final PlayerMoveEvent event) {
        if (!Utils.frozenSet.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        if (event.getPlayer().getOpenInventory() != null && event.getPlayer().getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.FrozenGUITitle"))) && event.getPlayer().getOpenInventory().getType().equals(InventoryType.CHEST)) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                event.getPlayer().openInventory(Utils.frozenInventory(event.getPlayer()));
            }
        }.runTaskAsynchronously(Utils.instance);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void entityDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getEntity();
        if (!Utils.frozenSet.contains(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        if (player.getOpenInventory() != null && player.getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.FrozenGUITitle"))) && player.getOpenInventory().getType().equals(InventoryType.CHEST)) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(Utils.frozenInventory(player));
            }
        }.runTaskAsynchronously(Utils.instance);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerTeleport(final PlayerTeleportEvent event) {
        if (!Utils.frozenSet.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        if (event.getPlayer().getOpenInventory() != null && event.getPlayer().getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', Utils.instance.getConfig().getString("Messages.FrozenGUITitle"))) && event.getPlayer().getOpenInventory().getType().equals(InventoryType.CHEST)) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                event.getPlayer().openInventory(Utils.frozenInventory(event.getPlayer()));
            }
        }.runTaskAsynchronously(Utils.instance);
    }
}