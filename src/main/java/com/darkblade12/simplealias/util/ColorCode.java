package com.darkblade12.simplealias.util;

import java.util.Collection;
import java.util.Random;

import org.bukkit.ChatColor;

import com.darkblade12.simplealias.nameable.Nameable;

public enum ColorCode {
	BLACK('0') {
		@Override
		public ColorCode getComplementary() {
			return WHITE;
		}
	},
	DARK_BLUE('1') {
		@Override
		public ColorCode getComplementary() {
			return BLUE;
		}
	},
	DARK_GREEN('2') {
		@Override
		public ColorCode getComplementary() {
			return GREEN;
		}
	},
	DARK_AQUA('3') {
		@Override
		public ColorCode getComplementary() {
			return AQUA;
		}
	},
	DARK_RED('4') {
		@Override
		public ColorCode getComplementary() {
			return RED;
		}
	},
	DARK_PURPLE('5') {
		@Override
		public ColorCode getComplementary() {
			return LIGHT_PURPLE;
		}
	},
	GOLD('6') {
		@Override
		public ColorCode getComplementary() {
			return YELLOW;
		}
	},
	GRAY('7') {
		@Override
		public ColorCode getComplementary() {
			return DARK_GRAY;
		}
	},
	DARK_GRAY('8') {
		@Override
		public ColorCode getComplementary() {
			return GRAY;
		}
	},
	BLUE('9') {
		@Override
		public ColorCode getComplementary() {
			return DARK_BLUE;
		}
	},
	GREEN('a') {
		@Override
		public ColorCode getComplementary() {
			return DARK_GREEN;
		}
	},
	AQUA('b') {
		@Override
		public ColorCode getComplementary() {
			return DARK_AQUA;
		}
	},
	RED('c') {
		@Override
		public ColorCode getComplementary() {
			return DARK_RED;
		}
	},
	LIGHT_PURPLE('d') {
		@Override
		public ColorCode getComplementary() {
			return DARK_PURPLE;
		}
	},
	YELLOW('e') {
		@Override
		public ColorCode getComplementary() {
			return GOLD;
		}
	},
	WHITE('f') {
		@Override
		public ColorCode getComplementary() {
			return BLACK;
		}
	};

	private static Random RANDOM = new Random();
	private char colorChar;

	private ColorCode(char colorChar) {
		this.colorChar = colorChar;
	}

	public char getColorChar() {
		return this.colorChar;
	}

	public abstract ColorCode getComplementary();

	@Override
	public String toString() {
		return new String(new char[] { '§', colorChar });
	}

	public static ColorCode random(boolean includeBlackAndWhite) {
		ColorCode[] values = values();
		return values[(includeBlackAndWhite ? 0 : 1) + RANDOM.nextInt(values.length + (includeBlackAndWhite ? 0 : -2))];
	}

	public static ColorCode random() {
		return random(false);
	}

	public static String[] translateAlternateColorCodes(char colorChar, String[] target) {
		String[] translated = new String[target.length];
		for (int i = 0; i < target.length; i++)
			translated[i] = ChatColor.translateAlternateColorCodes(colorChar, target[i]);
		return translated;
	}

	public static String convertToString(Collection<? extends Nameable> c, char bulletCharacter) {
		StringBuilder s = new StringBuilder();
		for (Nameable n : c) {
			ColorCode code = random();
			s.append("\n§r " + code + bulletCharacter + " " + code.getComplementary() + "§o" + n.getName());
		}
		return s.toString();
	}
}