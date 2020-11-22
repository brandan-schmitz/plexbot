package net.celestialdata.plexbot.database.builders;

import net.celestialdata.plexbot.database.models.User;

public class UserBuilder {
    private Long id;
    private String discriminatedName;

    public UserBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public UserBuilder withDiscriminatedName(String discriminatedName) {
        this.discriminatedName = discriminatedName;
        return this;
    }

    public User build() {
        return new User(this.id, this.discriminatedName);
    }
}
