package tech.rayline.core.plugin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public final class Formatter {
    private final YAMLConfigurationFile formatsFile;
    private String loadedPrefix;

    public Formatter(YAMLConfigurationFile formatsFile) {
        this.formatsFile = formatsFile;
        loadedPrefix = formatsFile.getConfig().contains("prefix") ? ChatColor.translateAlternateColorCodes('&', formatsFile.getConfig().getString("prefix")) : null;
    }

    public FormatBuilder begin(String path) {
        // Throw a proper error
        FileConfiguration config = formatsFile.getConfig();
        if (config.contains(path)) {
            return new FormatBuilder(config.getString(path));
        } else {
            // TODO probably a better exception for this
            throw new RuntimeException("No such string at path: " + path + " in formats.yml");
        }
    }

    public boolean has(String path) {
        return formatsFile.getConfig().contains(path);
    }

    public FormatBuilder withValue(String value) {
        return new FormatBuilder(value);
    }

    public final class FormatBuilder {
        private final String formatString;
        private final Map<String, String> modifiers = new HashMap<String, String>();
        private boolean prefix = true, coloredInputs = true;

        private FormatBuilder(String formatString) {
            this.formatString = formatString;
        }

        public FormatBuilder withModifier(String key, Object value) {
            modifiers.put(key, value.toString());
            return this;
        }

        public FormatBuilder with(String key, Object value) {
            modifiers.put(key, value.toString());
            return this;
        }

        public FormatBuilder withPrefix(boolean p) {
            prefix = p;
            return this;
        }

        public FormatBuilder withColoredInputs(boolean c) {
            coloredInputs = c;
            return this;
        }

        public String get() {
            if (formatString == null) return "Issue finding format from the formats.yml file!";
            String s = ChatColor.translateAlternateColorCodes('&', formatString);
            for (Map.Entry<String, String> stringStringEntry : modifiers.entrySet()) {
                String value = stringStringEntry.getValue();
                if (coloredInputs) value = ChatColor.translateAlternateColorCodes('&', value);
                s = s.replaceAll(String.format("\\{\\{%s\\}\\}", stringStringEntry.getKey()), value);
            }
            if (prefix && loadedPrefix != null) return loadedPrefix + s;
            return s;
        }

        @Override
        public String toString() {
            return get();
        }
    }
}
