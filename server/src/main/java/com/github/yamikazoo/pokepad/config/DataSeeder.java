package com.github.yamikazoo.pokepad.config;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.github.yamikazoo.pokepad.models.Card;
import com.github.yamikazoo.pokepad.repositories.CardRepository;
import com.github.yamikazoo.pokepad.services.CardImageCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds catalog cards from a local classpath JSON file when the database is empty.
 * Does not call external card APIs.
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String SEED_RESOURCE = "data/seed-cards.json";

    private final CardRepository cardRepository;
    private final ObjectMapper objectMapper;
    private final CardImageCacheService cardImageCacheService;

    public DataSeeder(
            CardRepository cardRepository,
            ObjectMapper objectMapper,
            CardImageCacheService cardImageCacheService) {
        this.cardRepository = cardRepository;
        this.objectMapper = objectMapper;
        this.cardImageCacheService = cardImageCacheService;
    }

    @Override
    public void run(String... args) {
        if (cardRepository.count() > 0) {
            return;
        }

        try {
            List<Card> cards = loadSeedCards();
            if (cards.isEmpty()) {
                log.warn("No seed cards found in {}", SEED_RESOURCE);
                return;
            }
            cardRepository.saveAll(cards);
            log.info("Seeded {} cards from local {}", cards.size(), SEED_RESOURCE);
            cardImageCacheService.cacheCards(cards);
        } catch (Exception e) {
            log.error("Failed to seed cards from local JSON", e);
        }
    }

    private List<Card> loadSeedCards() throws Exception {
        ClassPathResource resource = new ClassPathResource(SEED_RESOURCE);
        if (!resource.exists()) {
            return List.of();
        }

        List<Card> cards = new ArrayList<>();
        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(inputStream);
            if (!root.isArray()) {
                throw new IllegalStateException(SEED_RESOURCE + " must be a JSON array");
            }
            for (JsonNode node : root) {
                Card card = new Card();
                card.setName(textOrNull(node, "name"));
                card.setSetId(textOrNull(node, "setId"));
                card.setCardNumber(textOrNull(node, "cardNumber"));
                card.setImageUrl(textOrNull(node, "imageUrl"));
                card.setRarity(textOrDefault(node, "rarity", "Unknown"));
                card.setArtist(textOrDefault(node, "artist", "Unknown"));
                card.setLanguage(textOrNull(node, "language"));
                cards.add(card);
            }
        }
        return cards;
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }

    private static String textOrDefault(JsonNode node, String field, String defaultValue) {
        String text = textOrNull(node, field);
        return text == null ? defaultValue : text;
    }
}
