package com.github.yamikazoo.pokepad.services;

import com.github.yamikazoo.pokepad.models.Card;
import com.github.yamikazoo.pokepad.repositories.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class CardImageCacheService {

    private static final Logger log = LoggerFactory.getLogger(CardImageCacheService.class);
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final long THROTTLE_MS = 75;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final CardRepository cardRepository;
    private final S3StorageService s3StorageService;
    private final HttpClient httpClient;
    private final boolean enabled;
    private final String accessKeyId;
    private final String catalogPrefix;

    public CardImageCacheService(
            CardRepository cardRepository,
            S3StorageService s3StorageService,
            @Value("${aws.s3.enabled:true}") boolean enabled,
            @Value("${AWS_ACCESS_KEY_ID:}") String accessKeyId,
            @Value("${aws.s3.catalog-prefix:catalog}") String catalogPrefix) {
        this.cardRepository = cardRepository;
        this.s3StorageService = s3StorageService;
        this.enabled = enabled;
        this.accessKeyId = accessKeyId;
        this.catalogPrefix = catalogPrefix;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public boolean isEnabled() {
        return enabled && accessKeyId != null && !accessKeyId.isBlank();
    }

    public void cacheCards(List<Card> cards) {
        if (!isEnabled()) {
            log.warn("S3 catalog cache skipped: set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY in server/.env");
            return;
        }
        if (cards == null || cards.isEmpty()) {
            return;
        }

        int cached = 0;
        int skipped = 0;
        int failed = 0;
        for (Card card : cards) {
            try {
                if (cacheCard(card)) {
                    cached++;
                    Thread.sleep(THROTTLE_MS);
                } else {
                    skipped++;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("S3 catalog cache interrupted after {} uploads", cached);
                return;
            } catch (Exception e) {
                failed++;
                log.error("Failed to cache image for card {} ({})", card.getId(), card.getName(), e);
            }
        }
        log.info("S3 catalog cache finished: {} uploaded, {} skipped, {} failed", cached, skipped, failed);
    }

    public boolean cacheCard(Card card) throws Exception {
        if (card == null || alreadyCached(card.getImageUrl())) {
            return false;
        }

        String sourceUrl = card.getImageUrl();
        if (sourceUrl == null || sourceUrl.isBlank()) {
            return false;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sourceUrl))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "PokePad/1.0 (catalog-image-cache)")
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Failed to download " + sourceUrl + " (HTTP " + response.statusCode() + ")");
        }

        byte[] body = response.body();
        if (body == null || body.length == 0) {
            throw new IllegalStateException("Downloaded empty image from " + sourceUrl);
        }
        if (body.length > MAX_IMAGE_BYTES) {
            throw new IllegalStateException("Image exceeds " + MAX_IMAGE_BYTES + " bytes: " + sourceUrl);
        }

        String contentType = response.headers()
                .firstValue("Content-Type")
                .map(value -> value.split(";", 2)[0].trim().toLowerCase(Locale.ROOT))
                .orElse(guessContentType(sourceUrl));
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            contentType = guessContentType(sourceUrl);
        }

        String key = buildObjectKey(card, sourceUrl, contentType);
        s3StorageService.uploadCatalogImage(key, body, contentType);
        card.setImageUrl(s3StorageService.publicUrl(key));
        cardRepository.save(card);
        return true;
    }

    private boolean alreadyCached(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return true;
        }
        String bucketHost = s3StorageService.getBucket() + ".s3.";
        return imageUrl.contains(bucketHost);
    }

    private String buildObjectKey(Card card, String sourceUrl, String contentType) {
        String extension = extensionFor(sourceUrl, contentType);
        String setId = sanitizeSegment(card.getSetId(), "unknown-set");
        String cardNumber = sanitizeSegment(card.getCardNumber(), String.valueOf(card.getId()));
        return catalogPrefix + "/" + setId + "/" + cardNumber + "." + extension;
    }

    private String sanitizeSegment(String value, String fallback) {
        String source = (value == null || value.isBlank()) ? fallback : value;
        String sanitized = source.replace('/', '-').replaceAll("[^a-zA-Z0-9._-]", "-");
        if (sanitized.contains("..") || sanitized.isBlank()) {
            throw new IllegalArgumentException("Invalid S3 key segment: " + value);
        }
        return sanitized;
    }

    private String guessContentType(String sourceUrl) {
        String path = URI.create(sourceUrl).getPath().toLowerCase(Locale.ROOT);
        if (path.endsWith(".png")) {
            return "image/png";
        }
        if (path.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private String extensionFor(String sourceUrl, String contentType) {
        if (contentType.contains("png") || sourceUrl.toLowerCase(Locale.ROOT).contains(".png")) {
            return "png";
        }
        if (contentType.contains("webp") || sourceUrl.toLowerCase(Locale.ROOT).contains(".webp")) {
            return "webp";
        }
        return "jpg";
    }
}
