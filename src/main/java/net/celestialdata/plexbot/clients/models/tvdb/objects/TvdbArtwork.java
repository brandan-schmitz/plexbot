package net.celestialdata.plexbot.clients.models.tvdb.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.eclipse.microprofile.config.ConfigProvider;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvdbArtwork {
    public int id;
    public String image;
    public String language;
    public float score;
    public String thumbnail;
    public long type;

    public String getImage() {
        var noPosterImageUrl = ConfigProvider.getConfig().getValue("BotSettings.noPosterImageUrl", String.class);
        if (this.image.isBlank() || this.image == null) {
            return noPosterImageUrl;
        } else return this.image;
    }
}