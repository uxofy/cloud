package com.example.cloud;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class CloudConfig {
    public static boolean enabled = true;
    public static int delay = 2; // 0..20 тиков (1 тик = 50 мс)

    private CloudConfig() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("cloud.properties");
    }

    public static void load() {
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            Properties p = new Properties();
            try (var in = Files.newInputStream(f)) { p.load(in); }
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
            delay = Math.max(0, Math.min(20, Integer.parseInt(p.getProperty("delay", "2"))));
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            Properties p = new Properties();
            p.setProperty("enabled", String.valueOf(enabled));
            p.setProperty("delay", String.valueOf(delay));
            try (var out = Files.newOutputStream(file())) { p.store(out, "Cloud"); }
        } catch (Exception ignored) {
        }
    }
}
