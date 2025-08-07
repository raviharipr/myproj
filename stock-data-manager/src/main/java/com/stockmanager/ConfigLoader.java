package com.stockmanager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfigLoader {

    private static final String CONFIG_FILE = "config.properties";
    private final Properties properties;

    public ConfigLoader() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Sorry, unable to find " + CONFIG_FILE);
                throw new RuntimeException("Configuration file not found");
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error loading configuration file");
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public List<Integer> getIntegerListProperty(String key) {
        String property = getProperty(key);
        if (property == null || property.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] values = property.split(",");
        List<Integer> intValues = new ArrayList<>();
        for (String value : values) {
            intValues.add(Integer.parseInt(value.trim()));
        }
        return intValues;
    }
}
