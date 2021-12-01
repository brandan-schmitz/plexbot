package net.celestialdata.plexbot.utilities;

import io.smallrye.config.ConfigSourceInterceptor;
import io.smallrye.config.ConfigSourceInterceptorContext;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.Priorities;

import javax.annotation.Priority;

@Priority(Priorities.LIBRARY + 1000)
public class ConfigInterceptor implements ConfigSourceInterceptor {

    @Override
    public ConfigValue getValue(final ConfigSourceInterceptorContext context, final String name) {
        ConfigValue configValue = context.proceed(name);
        switch (name) {
            case "quarkus.banner.enabled":
                configValue = configValue.withValue("false");
                break;
            case "quarkus.log.level":
                var logLevel = context.proceed("BotSettings.logLevel");
                configValue = logLevel.withName(name);
                break;
            case "quarkus.hibernate-orm.database.generation":
                var generationStrategy = context.proceed("DatabaseSettings.generationStrategy");
                configValue = generationStrategy.withName(name);
                break;
            case "quarkus.datasource.jdbc.url":
                var connectionAddress = context.proceed("DatabaseSettings.address");
                var connectionPort = context.proceed("DatabaseSettings.port");
                var databaseName = context.proceed("DatabaseSettings.name");

                configValue = connectionAddress.withValue("jdbc:mariadb://" +
                        connectionAddress.getValue() + ":" +
                        connectionPort.getValue() + "/" +
                        databaseName.getValue()
                ).withName(name);
                break;
            case "quarkus.datasource.username":
                var username = context.proceed("DatabaseSettings.username");
                configValue = username.withName(name);
                break;
            case "quarkus.datasource.password":
                var password = context.proceed("DatabaseSettings.password");
                configValue = password.withName(name);
                break;
            case "quarkus.datasource.metrics.enabled":
            case "quarkus.datasource.jdbc.enable-metrics":
                var metrics = context.proceed("DatabaseSettings.collectMetrics");
                configValue = metrics.withName(name);
                break;
            case "quarkus.datasource.jdbc.extended-leak-report":
                var extendedReport = context.proceed("DatabaseSettings.collectMetrics");
                configValue = extendedReport.withName(name)
                        .withValue("true");
                break;
            case "quarkus.datasource.jdbc.max-size":
                configValue = configValue.withValue("40");
                break;
            case "quarkus.datasource.jdbc.acquisition-timeout":
                configValue = configValue.withValue("10");
                break;
            case "quarkus.datasource.jdbc.leak-detection-interval":
                var leakDetectionInterval = context.proceed("DatabaseSettings.collectMetrics");
                configValue = leakDetectionInterval.withName(name)
                        .withValue("1M");
                break;
        }

        return configValue;
    }
}