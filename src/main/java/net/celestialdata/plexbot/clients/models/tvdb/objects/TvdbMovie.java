package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbMovie {
    public List<TvdbAlias> aliases = null;
    public long id;
    public String image;
    public String name;
    public List<String> nameTranslations = null;
    public List<String> overviewTranslations = null;
    public double score;
    public String slug;
    public TvdbStatus status;

    public String getImage() {
        if (StringUtils.isBlank(this.image)) {
            return ConfigProvider.getConfig().getValue("BotSettings.noPosterImageUrl", String.class);
        } else return this.image;
    }
}