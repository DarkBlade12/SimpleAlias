package com.darkblade12.simplealias.plugin;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MessageManager extends Manager<PluginBase> {
    private static final String BUNDLE_NAME = "messages";
    private static final MessageFormat MISSING_FORMAT = new MessageFormat("§cThe message §6{0} §cis not available!");
    private final Map<String, MessageFormat> messageCache;
    private ResourceBundle bundle;

    public MessageManager(PluginBase plugin) {
        super(plugin);
        messageCache = new HashMap<>();
    }

    @Override
    protected void onEnable() {
        Locale locale = plugin.getCurrentLocale();
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        plugin.logInfo("Language {0} loaded.", locale.toLanguageTag());
    }

    @Override
    protected void onDisable() {
        messageCache.clear();
    }

    public String formatMessage(String key, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                args[i] = "";
            }
        }

        MessageFormat format = messageCache.get(key);
        if (format == null) {
            try {
                String message = StringEscapeUtils.unescapeJava(bundle.getString(key));
                format = new MessageFormat(ChatColor.translateAlternateColorCodes('&', message));
                messageCache.put(key, format);
            } catch (MissingResourceException e) {
                return MISSING_FORMAT.format(new Object[] { key });
            }
        }

        return format.format(args);
    }
}
