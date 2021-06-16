package net.celestialdata.plexbot.dataobjects;

import io.quarkus.arc.config.ConfigProperties;

import java.util.List;

@ConfigProperties(prefix = "FolderSettings.blacklistedCharacters")
public class BlacklistedCharacters {
    public List<String> remove;
    public List<Replacements> replace;

    public static class Replacements {
        public String original;
        public String replacement;
    }
}