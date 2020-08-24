package com.darkblade12.simplealias.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MessageUtils {
    private static final Map<ChatColor, ChatColor> SIMILAR_COLORS = new HashMap<>();
    private static final ChatColor[] COLORS;
    private static final Random RANDOM = new Random();
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§([0-9a-fk-orx])", Pattern.CASE_INSENSITIVE);

    static {
        SIMILAR_COLORS.put(ChatColor.DARK_BLUE, ChatColor.BLUE);
        SIMILAR_COLORS.put(ChatColor.DARK_GREEN, ChatColor.GREEN);
        SIMILAR_COLORS.put(ChatColor.DARK_AQUA, ChatColor.AQUA);
        SIMILAR_COLORS.put(ChatColor.DARK_RED, ChatColor.RED);
        SIMILAR_COLORS.put(ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE);
        SIMILAR_COLORS.put(ChatColor.DARK_GRAY, ChatColor.GRAY);
        SIMILAR_COLORS.put(ChatColor.GOLD, ChatColor.YELLOW);

        COLORS = Arrays.stream(ChatColor.values()).filter(c -> c.isColor() && c != ChatColor.BLACK && c != ChatColor.WHITE)
                       .toArray(ChatColor[]::new);
    }

    private MessageUtils() {
    }

    public static ChatColor randomColor() {
        return COLORS[RANDOM.nextInt(COLORS.length)];
    }

    public static ChatColor similarColor(ChatColor color) {
        for (Entry<ChatColor, ChatColor> entry : SIMILAR_COLORS.entrySet()) {
            ChatColor key = entry.getKey();
            ChatColor value = entry.getValue();
            if (key == color) {
                return value;
            } else if (value == color) {
                return key;
            }
        }

        return color;
    }

    public static String formatName(Enum<?> enumObj, boolean capitalize, String delimiter) {
        String[] split = enumObj.name().toLowerCase().split("_");
        return Arrays.stream(split).map(s -> capitalize ? StringUtils.capitalize(s) : s).collect(Collectors.joining(delimiter));
    }

    public static String formatName(Enum<?> enumObj, boolean capitalize) {
        return formatName(enumObj, capitalize, " ");
    }

    public static String formatName(Enum<?> enumObj) {
        return formatName(enumObj, true);
    }

    public static boolean isBlank(String message) {
        return message == null || message.isEmpty() || message.matches("\\s+");
    }

    public static String translateMessage(String message, char colorChar) {
        return isBlank(message) ? message : ChatColor.translateAlternateColorCodes(colorChar, StringEscapeUtils.unescapeJava(message));
    }

    public static String translateMessage(String message) {
        return translateMessage(message, '&');
    }

    public static String reverseTranslateMessage(String message, char colorChar) {
        return isBlank(message) ? message : StringEscapeUtils.escapeJava(COLOR_CODE_PATTERN.matcher(message).replaceAll(colorChar + "$1"));
    }

    public static String reverseTranslateMessage(String message) {
        return reverseTranslateMessage(message, '&');
    }

    public static String[] translateArguments(String[] args, char colorChar) {
        return Arrays.stream(args).map(s -> ChatColor.translateAlternateColorCodes(colorChar, s)).toArray(String[]::new);
    }

    public static String[] translateArguments(String[] args) {
        return translateArguments(args, '&');
    }

    public static String formatDuration(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration cannot be lower than 0.");
        }

        TimeUnit[] units = { TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS };
        StringBuilder builder = new StringBuilder();

        for (TimeUnit unit : units) {
            long duration = unit.convert(millis, TimeUnit.MILLISECONDS);
            if (duration <= 0) {
                continue;
            }
            millis -= TimeUnit.MILLISECONDS.convert(duration, unit);

            if (builder.length() > 0) {
                builder.append(", ");
            }

            String name = unit.name().toLowerCase();
            if (duration == 1) {
                name = name.substring(0, name.length() - 1);
            }
            builder.append(duration).append(" ").append(name);
        }

        int index = builder.lastIndexOf(", ");
        if (index != -1) {
            builder.replace(index, index + 2, " and ");
        }

        return builder.length() > 0 ? builder.toString() : "<1 second";
    }
}
