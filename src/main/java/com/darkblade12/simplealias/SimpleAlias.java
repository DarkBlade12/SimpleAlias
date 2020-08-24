package com.darkblade12.simplealias;

import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasException;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.alias.AliasConverter;
import com.darkblade12.simplealias.command.AliasCommandHandler;
import com.darkblade12.simplealias.cooldown.CooldownManager;
import com.darkblade12.simplealias.plugin.PluginBase;
import com.darkblade12.simplealias.plugin.command.CommandRegistrationException;
import com.darkblade12.simplealias.plugin.hook.FactionsHook;
import com.darkblade12.simplealias.plugin.hook.VaultHook;
import com.darkblade12.simplealias.plugin.reader.ConfigurationReader;
import com.darkblade12.simplealias.plugin.settings.InvalidValueException;
import com.darkblade12.simplealias.settings.Settings;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class SimpleAlias extends PluginBase {
    public static final String PREFIX = "§8§l[§a§oSimple§7§oAlias§8§l]§r";
    private final Settings settings;
    private final VaultHook vaultHook;
    private final FactionsHook factionsHook;
    private final CooldownManager cooldownManager;
    private final AliasManager aliasManager;
    private final AliasCommandHandler commandHandler;
    private final AliasConverter aliasConverter;
    private ConfigurationReader<SimpleAlias> templateReader;
    private boolean templateValid;

    public SimpleAlias() {
        super(47360, 8539);
        settings = new Settings(this);
        vaultHook = new VaultHook();
        factionsHook = new FactionsHook();
        cooldownManager = new CooldownManager(this);
        aliasManager = new AliasManager(this);
        commandHandler = new AliasCommandHandler(this);
        aliasConverter = new AliasConverter(this);
    }

    @Override
    public boolean enable() {
        try {
            settings.load();
        } catch (InvalidValueException e) {
            logException("Failed to load settings from the configuration file: {0}", e);
            return false;
        }

        convertAliases();
        initializeTemplate();

        if (vaultHook.enable()) {
            Map<String, Boolean> services = new HashMap<>();
            services.put("permission", vaultHook.isPermissionEnabled());
            services.put("economy", vaultHook.isEconomyEnabled());
            logInfo("Vault detected and hooked. ({0})",
                    services.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.joining(" and ")));
        }
        if (factionsHook.enable()) {
            logInfo("Factions detected and hooked.");
        }

        try {
            cooldownManager.enable();
            aliasManager.enable();
        } catch (Exception e) {
            logException("Failed to enable managers: {0}", e);
            return false;
        }

        try {
            commandHandler.enable();
        } catch (CommandRegistrationException e) {
            logException("Failed register commands: {0}", e);
            return false;
        }

        return true;
    }

    @Override
    public void disable() {
        cooldownManager.disable();
        aliasManager.disable();
    }

    @Override
    public boolean reload() {
        try {
            settings.reload();
        } catch (InvalidValueException e) {
            logException("Failed to load settings from the configuration file: {0}", e);
            return false;
        }

        initializeTemplate();

        vaultHook.enable();
        factionsHook.enable();

        convertAliases();

        try {
            cooldownManager.reload();
            aliasManager.reload();
        } catch (Exception e) {
            logException("Failed to reload managers: {0}", e);
            return false;
        }

        return true;
    }

    private void initializeTemplate() {
        templateReader = new ConfigurationReader<>(this, settings.getTemplatePath(), new File(getDataFolder(), "template.yml"));
        templateValid = false;

        if (!templateReader.getOutputFile().exists() && !templateReader.saveResourceFile()) {
            logWarning("Failed to save the default template file.");
        } else {
            try {
                Alias.validateTemplate(this);
                templateValid = true;
            } catch (AliasException | InvalidValueException e) {
                logException("Failed to read the template file: {0}", e);
            }
        }

        if (!templateValid) {
            logWarning("The template file will be copied from resources instead of {0}.", templateReader.getOutputFile().getAbsolutePath());
        }
    }

    private void convertAliases() {
        if (!settings.isConverterEnabled()) {
            return;
        }

        try {
            aliasConverter.convertFiles();
        } catch (Exception e) {
            logException("Failed to convert aliases: {0}", e);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return settings.isDebugEnabled();
    }

    @Override
    public Locale getCurrentLocale() {
        return Locale.ENGLISH;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    public Settings getSettings() {
        return settings;
    }

    public AliasManager getAliasManager() {
        return aliasManager;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public FactionsHook getFactionsHook() {
        return factionsHook;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ConfigurationReader<SimpleAlias> getTemplateReader() {
        return templateReader;
    }

    public boolean isTemplateValid() {
        return templateValid;
    }
}
