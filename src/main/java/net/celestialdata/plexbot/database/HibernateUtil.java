package net.celestialdata.plexbot.database;

import net.celestialdata.plexbot.BotConfig;
import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.database.models.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author imssbora
 */
public class HibernateUtil {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

                Map<String, Object> settings = new HashMap<>();
                settings.put(Environment.DRIVER, "org.mariadb.jdbc.Driver");
                settings.put(Environment.URL, "jdbc:mariadb://" + BotConfig.getInstance().dbConnectionAddress() + ":" + BotConfig.getInstance().dbPort() + "/" + BotConfig.getInstance().dbName());
                settings.put(Environment.USER, BotConfig.getInstance().dbUsername());
                settings.put(Environment.PASS, BotConfig.getInstance().dbPassword());
                settings.put(Environment.HBM2DDL_AUTO, "update");
                settings.put(Environment.SHOW_SQL, false);

                // HikariCP settings
                settings.put("hibernate.hikari.connectionTimeout", "10000");
                settings.put("hibernate.hikari.minimumIdle", "10");
                settings.put("hibernate.hikari.maximumPoolSize", "20");
                settings.put("hibernate.hikari.idleTimeout", "300000");
                settings.put("hibernate.hikari.maxLifetime", "300000");
                settings.put("hibernate.hikari.poolName", "Database Connection");

                registryBuilder.applySettings(settings);
                registry = registryBuilder.build();

                MetadataSources sources = new MetadataSources(registry);
                sources.addAnnotatedClass(Movie.class);
                sources.addAnnotatedClass(UpgradeItem.class);
                sources.addAnnotatedClass(User.class);
                sources.addAnnotatedClass(WaitlistItem.class);
                sources.addAnnotatedClass(Show.class);
                sources.addAnnotatedClass(Season.class);
                sources.addAnnotatedClass(Episode.class);
                sources.addAnnotatedClass(EpisodeSubtitle.class);
                sources.addAnnotatedClass(MovieSubtitle.class);
                Metadata metadata = sources.getMetadataBuilder().build();

                sessionFactory = metadata.getSessionFactoryBuilder().applyInterceptor(new CustomInterceptor()).build();
            } catch (Exception e) {
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
                new MessageBuilder()
                        .append("An error has occurred while performing the following task:", MessageDecoration.BOLD)
                        .appendCode("", "Database Connection Pool")
                        .appendCode("java", ExceptionUtils.getMessage(e))
                        .appendCode("java", ExceptionUtils.getStackTrace(e))
                        .send(Main.getBotApi().getUserById(BotConfig.getInstance().adminUserId()).join());
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}