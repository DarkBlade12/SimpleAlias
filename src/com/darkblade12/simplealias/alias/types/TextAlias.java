package com.darkblade12.simplealias.alias.types;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.Type;
import com.darkblade12.simplealias.hook.FactionsHook;
import com.darkblade12.simplealias.hook.VaultHook;

public class TextAlias extends Alias {
	private List<String> lines;

	public TextAlias(SimpleAlias plugin, String name, List<String> lines, String description, boolean permissionEnabled, String permission, boolean permittedGroupsEnabled, Set<String> permittedGroups) {
		super(plugin, name, Type.TEXT, description, permissionEnabled, permission, permittedGroupsEnabled, permittedGroups);
		this.lines = lines;
	}

	public TextAlias(SimpleAlias plugin, String name, List<String> lines) {
		super(plugin, name, Type.TEXT, null, true, DEFAULT_PERMISSION.replace("<name>", name), false, null);
		this.lines = lines;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		super.execute(sender, args);
		sender.sendMessage(prepareText(sender));
	}

	private String prepareText(CommandSender s) {
		String t = StringUtils.join(lines, "\n");
		if (s instanceof Player) {
			Player p = (Player) s;
			t = t.replace("<world_name>", p.getWorld().getName()).replace("<sender_group>", VaultHook.getGroup(p)).replace("<balance>", VaultHook.getBalance(p) + "")
					.replace("<faction>", FactionsHook.getFaction(p));
		}
		return t.replace("<sender_name>", s.getName()).replaceAll(ARGUMENT_PATTERN, "");
	}

	public void setLines(List<String> lines) {
		this.lines = lines;
		setConfigValue("Execution_Settings.Lines", StringUtils.join(lines, "#").replace('§', '&'));
	}

	public void addLines(String... lines) {
		for (String line : lines)
			this.lines.add(line);
	}

	public List<String> getLines() {
		return this.lines;
	}
}