package com.darkblade12.simplealias.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

import com.darkblade12.simplealias.alias.Alias;

public abstract class StringUtil {
	private final static Random RANDOM = new Random();
	private final static Pattern ILLEGAL_CHARACTERS = Pattern.compile("[\\/:*?\"<>|#]");
	private final static String[] COLOR_CODE_MODIFIERS = new String[] { /* "0", */"1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e"/* , "f" */};
	private final static Map<String, String> EQUAL_COLOR_CODES = new HashMap<String, String>();
	public final static String CHECK = "§a\u2714";
	public final static String MISSING = "§c\u2718";

	static {
		EQUAL_COLOR_CODES.put("§1", "§9");
		EQUAL_COLOR_CODES.put("§2", "§a");
		EQUAL_COLOR_CODES.put("§3", "§b");
		EQUAL_COLOR_CODES.put("§4", "§c");
		EQUAL_COLOR_CODES.put("§5", "§d");
		EQUAL_COLOR_CODES.put("§6", "§e");
		EQUAL_COLOR_CODES.put("§7", "§8");
	}

	public static String stripFirstSlash(String s) {
		return s.startsWith("/") ? s.substring(1) : s;
	}

	public static boolean containsIllegalCharacters(String s) {
		return ILLEGAL_CHARACTERS.matcher(s).find();
	}

	public static String randomColorCode() {
		return "§" + COLOR_CODE_MODIFIERS[RANDOM.nextInt(COLOR_CODE_MODIFIERS.length)];
	}

	public static String equalColorCode(String c) {
		for (Entry<String, String> e : EQUAL_COLOR_CODES.entrySet()) {
			String k = e.getKey();
			String v = e.getValue();
			if (k.equals(c))
				return v;
			else if (v.equals(c))
				return k;
		}
		throw new IllegalArgumentException("Invalid color code");
	}

	public static Set<String> asSet(String s, String seperator) {
		Set<String> set = new HashSet<String>();
		if (s != null) {
			String[] sp = s.split(seperator);
			for (String p : sp)
				set.add(p);
		}
		return set;
	}

	public static List<String> asList(String s, String seperator) {
		List<String> list = new ArrayList<String>();
		if (s != null) {
			String[] sp = s.split(seperator);
			for (String p : sp)
				list.add(p);
		}
		return list;
	}

	public static String toString(Iterable<Alias> i) {
		StringBuilder b = new StringBuilder();
		for (Alias a : i) {
			String c = randomColorCode();
			b.append("\n§r " + c + "\u25A9 " + equalColorCode(c) + "§o" + a.getName() + " §f\u276F §7" + a.getType().getName());
		}
		return b.length() > 0 ? b.toString() : "\n§r §c\u25A9 §4§lNone";
	}

	public static String parse(long time) {
		Bukkit.broadcastMessage(time + "");
		if (time < 0)
			throw new IllegalArgumentException("Time value can't be lower than 0");
		long[] units = new long[] { time / (7 * 24 * 60 * 60 * 1000), time / (24 * 60 * 60 * 1000) % 7, time / (60 * 60 * 1000) % 24, time / (60 * 1000) % 60, time / 1000 % 60 };
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < units.length; i++) {
			long unit = units[i];
			if (unit > 0) {
				b.append((b.length() > 0 ? i == 4 ? " and " : ", " : "") + unit + " ");
				if (i == 0)
					b.append("week");
				else if (i == 1)
					b.append("day");
				else if (i == 2)
					b.append("hour");
				else if (i == 3)
					b.append("minute");
				else if (i == 4)
					b.append("second");
				b.append(unit > 1 ? "s" : "");
			}
		}
		return b.length() > 0 ? b.toString() : "a short moment";
	}
}