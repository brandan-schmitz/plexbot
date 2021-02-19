/*
 * Plexbot
 * Provides all the API functions necessary for the Plexbot to function.
 *
 * The version of the OpenAPI document: 1.0.0
 * Contact: brandan.schmitz@celestialdata.net
 *
 * NOTE: This class is auto generated by OpenAPI Generator
 * (https://openapi-generator.tech). https://openapi-generator.tech Do not edit
 * the class manually.
 */

package net.celestialdata.plexbot.client;

@javax.annotation.
        Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2021-02-18T23:30:04.504837-06:00[America/Chicago]")
public class Pair {
    private String name = "";
    private String value = "";

    public Pair(String name, String value) {
        setName(name);
        setValue(value);
    }

    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        if (!isValidString(name)) {
            return;
        }

        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    private void setValue(String value) {
        if (!isValidString(value)) {
            return;
        }

        this.value = value;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidString(String arg) {
        if (arg == null) {
            return false;
        }

        if (arg.trim().isEmpty()) {
            return false;
        }

        return true;
    }
}
