package net.celestialdata.plexbot.dataobjects;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "FolderSettings.blacklistedCharacters")
public interface BlacklistedCharacters {
    List<String> remove();
    List<Replacements> replace();

    @SuppressWarnings("unused")
    interface Replacements {
        String original();
        String replacement();
    }
}