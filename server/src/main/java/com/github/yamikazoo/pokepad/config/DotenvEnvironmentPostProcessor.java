package com.github.yamikazoo.pokepad.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> dotenv = loadEnvFile();
        if (dotenv.isEmpty()) {
            return;
        }
        environment.getPropertySources().addFirst(new MapPropertySource("pokepadDotenv", dotenv));

        copyToSystemProperty(dotenv, "AWS_ACCESS_KEY_ID", "aws.accessKeyId");
        copyToSystemProperty(dotenv, "AWS_SECRET_ACCESS_KEY", "aws.secretAccessKey");
        copyToSystemProperty(dotenv, "AWS_REGION", "aws.region");
        copyToSystemProperty(dotenv, "AWS_ACCESS_KEY_ID", "AWS_ACCESS_KEY_ID");
        copyToSystemProperty(dotenv, "AWS_SECRET_ACCESS_KEY", "AWS_SECRET_ACCESS_KEY");
        copyToSystemProperty(dotenv, "AWS_REGION", "AWS_REGION");
        copyToSystemProperty(dotenv, "AWS_S3_BUCKET", "AWS_S3_BUCKET");
        copyToSystemProperty(dotenv, "AWS_S3_ENABLED", "AWS_S3_ENABLED");
    }

    static Map<String, Object> loadEnvFile() {
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) {
            envFile = Path.of("server", ".env");
        }

        Map<String, Object> values = new LinkedHashMap<>();
        if (!Files.exists(envFile)) {
            return values;
        }

        try {
            for (String line : Files.readAllLines(envFile)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();
                if (!value.isBlank()) {
                    values.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + envFile.toAbsolutePath(), e);
        }
        return values;
    }

    private static void copyToSystemProperty(Map<String, Object> dotenv, String sourceKey, String propertyName) {
        Object value = dotenv.get(sourceKey);
        if (value != null && System.getProperty(propertyName) == null) {
            System.setProperty(propertyName, value.toString());
        }
    }
}
