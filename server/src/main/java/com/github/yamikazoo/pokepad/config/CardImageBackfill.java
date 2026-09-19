package com.github.yamikazoo.pokepad.config;

import com.github.yamikazoo.pokepad.models.Card;
import com.github.yamikazoo.pokepad.repositories.CardRepository;
import com.github.yamikazoo.pokepad.services.CardImageCacheService;
import com.github.yamikazoo.pokepad.services.S3StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
public class CardImageBackfill implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CardImageBackfill.class);

    private final CardImageCacheService cardImageCacheService;
    private final CardRepository cardRepository;
    private final S3StorageService s3StorageService;

    public CardImageBackfill(
            CardImageCacheService cardImageCacheService,
            CardRepository cardRepository,
            S3StorageService s3StorageService) {
        this.cardImageCacheService = cardImageCacheService;
        this.cardRepository = cardRepository;
        this.s3StorageService = s3StorageService;
    }

    @Override
    public void run(String... args) {
        if (!cardImageCacheService.isEnabled()) {
            log.warn("S3 catalog cache skipped: AWS credentials or bucket not configured");
            return;
        }

        String bucketHost = s3StorageService.getBucket() + ".s3.";
        List<Card> cards = cardRepository.findAll().stream()
                .filter(card -> card.getImageUrl() != null && !card.getImageUrl().isBlank())
                .filter(card -> !card.getImageUrl().contains(bucketHost))
                .toList();

        if (cards.isEmpty()) {
            log.info("All catalog images already point at configured S3 bucket");
            return;
        }

        log.info("Caching {} catalog images in S3 bucket {}", cards.size(), s3StorageService.getBucket());
        cardImageCacheService.cacheCards(cards);
    }
}
