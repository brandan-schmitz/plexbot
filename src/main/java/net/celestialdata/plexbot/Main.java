package net.celestialdata.plexbot;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@QuarkusMain
public class Main {

    public static void main(String... args) {
        // Set the default version and date variables
        String version = "0.0.0";
        String date = "00/00/0000";

        // Load the application information
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("pom.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            version = properties.getProperty("quarkus.application.version");
            date = properties.getProperty("version-date");
        } catch (IOException ignored) {
        }


        // Display the application banner
        System.out.format("\n" +
                        "           _____  _      ________   ______   ____ _______                \n" +
                        "          |  __ \\| |    |  ____\\ \\ / /  _ \\ / __ \\__   __|          \n" +
                        "          | |__) | |    | |__   \\ V /| |_) | |  | | | |                 \n" +
                        "          |  ___/| |    |  __|   > < |  _ <| |  | | | |                  \n" +
                        "          | |    | |____| |____ / . \\| |_) | |__| | | |                 \n" +
                        "          |_|    |______|______/_/ \\_\\____/ \\____/  |_|               \n" +
                        "                                                                         \n" +
                        "                   Version %s - %s                            \n" +
                        "                                                                         \n" +
                        "  This software is distributed under the GNU GENERAL PUBLIC v3 license   \n" +
                        "      and is available for anyone to use and modify as long as the       \n" +
                        "           proper attributes are given in the modified code.             \n" +
                        "                                                                         \n\n",
                version, date);

        // Run the main application
        Quarkus.run(Plexbot.class, args);
    }
}