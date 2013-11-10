package com.darkblade12.simplealias.alias;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.types.SingleAlias;
import com.darkblade12.simplealias.cooldown.Cooldown;
import com.darkblade12.simplealias.nameable.NameableList;
import com.darkblade12.simplealias.util.StringUtil;

public class AliasHandler implements Listener, Iterable<Alias> {
	private static File ALIAS_DIRECTORY = new File("plugins/SimpleAlias/aliases/");
	private static String YAML_FILE_NAME_PATTERN = ".*\\.yml";
	private SimpleAlias plugin;
	private Set<String> executedAlias;
	private NameableList<Alias> aliases;

	public AliasHandler(SimpleAlias plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		executedAlias = new HashSet<String>();
		registerAliases();
	}

	public void registerAliases() {
		aliases = new NameableList<Alias>();
		if (ALIAS_DIRECTORY.exists())
			for (File f : ALIAS_DIRECTORY.listFiles()) {
				String n = f.getName();
				if (n.matches(YAML_FILE_NAME_PATTERN)) {
					String name = n.replace(".yml", "");
					try {
						aliases.add(Alias.fromFile(plugin, name));
					} catch (Exception e) {
						plugin.l.info("Failed to load alias '" + name + "'. Reason: " + e.getMessage());
					}
				}
			}
		int amount = aliases.size();
		plugin.l.info(amount + " alias" + (amount == 1 ? "" : "es") + " loaded.");
	}

	public void register(Alias a) {
		aliases.add(a);
		a.saveToFile();
	}

	public void unregister(Alias a) {
		aliases.remove(a);
		a.deleteFile(plugin);
	}

	public boolean exists(String name) {
		return get(name) != null;
	}

	public Alias get(String name) {
		return aliases.get(name);
	}

	public SingleAlias getBlocker(String command) {
		for (Alias a : this)
			if (a.getType() == Type.SINGLE) {
				SingleAlias s = (SingleAlias) a;
				if (s.getDisableCommand() && command.equalsIgnoreCase(s.getCommand()))
					return s;
			}
		return null;
	}

	@Override
	public Iterator<Alias> iterator() {
		return aliases.iterator();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		String n = p.getName();
		if (executedAlias.contains(n)) {
			executedAlias.remove(n);
			return;
		}
		String[] s = StringUtil.stripFirstSlash(event.getMessage()).split(" ");
		SingleAlias b = getBlocker(s[0]);
		if (b != null) {
			event.setCancelled(true);
			p.sendMessage(b.getDisableMessage());
		} else {
			Alias a = get(s[0]);
			if (a != null) {
				event.setCancelled(true);
				if (!a.hasPermission(p)) {
					p.sendMessage("§cYou don't have permission for this alias!");
				} else {
					if (a instanceof CommandAlias) {
						CommandAlias c = (CommandAlias) a;
						if (c.isCooldownEnabled() && !p.hasPermission(SimpleAlias.COOLDOWN_BYPASS_PERMISSION) && !p.hasPermission(SimpleAlias.MASTER_PERMISSION)) {
							String name = a.getName();
							Cooldown co = plugin.cooldownHandler.get(p, name);
							if (co != null)
								if (co.isExpired()) {
									plugin.cooldownHandler.remove(p, name);
								} else {
									long expiredTime = co.getExpiredTime();
									p.sendMessage(expiredTime >= 0 ? "§cYou have to wait " + StringUtil.parse(co.getWaitingTime()) + " until you can use this alias again!"
											: "§cYou can never use this alias again!");
									return;
								}
							plugin.cooldownHandler.add(p, c.createCooldown());
						}
					}
					if (a instanceof SingleAlias && ((SingleAlias) a).getExecutor() == Executor.SENDER)
						executedAlias.add(n);
					a.execute(p, (String[]) Arrays.copyOfRange(s, 1, s.length));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onServerCommand(ServerCommandEvent event) {
		CommandSender sender = event.getSender();
		String[] s = StringUtil.stripFirstSlash(event.getCommand()).split(" ");
		Alias a = get(s[0]);
		if (a != null) {
			event.setCommand("");
			if (!(a instanceof CommandAlias) || a instanceof CommandAlias && ((CommandAlias) a).isExecutableAsConsole())
				a.execute(sender, (String[]) Arrays.copyOfRange(s, 1, s.length));
		}
	}
}