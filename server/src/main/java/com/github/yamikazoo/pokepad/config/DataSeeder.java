package com.github.yamikazoo.pokepad.config;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.github.yamikazoo.pokepad.models.Card;
import com.github.yamikazoo.pokepad.repositories.CardRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CardRepository cardRepository;
    private final ObjectMapper objectMapper; // spring's tool for reading JSON

    // uses the value from application.yaml
    @Value("${pokemon.api.key}")
    private String apiKey;

    public DataSeeder(CardRepository cardRepository, ObjectMapper objectMapper) {
        this.cardRepository = cardRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        // only run if the database is empty
        if (cardRepository.count() == 0) {
            System.out.println("fetching Prismatic Evolutions set from API...");
            try {
                seedPrismaticEvolutions();
                seedStellarCrown();
            } catch (Exception e) {
                System.out.println("error seeding data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // method to fetch and seed Prismatic Evolutions set from the Pokemon TCG API
    private void seedPrismaticEvolutions() throws Exception {
        // build the request (asking for set 'sv8pt5' - Prismatic Evolutions)
        String apiUrl = "https://api.pokemontcg.io/v2/cards?q=set.id:sv8pt5&pageSize=250";
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("X-Api-Key", apiKey)
                .build();

        // send the request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // parse the JSON response
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode dataArray = root.path("data");

        List<Card> cardBatch = new ArrayList<>();

        if (dataArray.isArray()) {
            for (JsonNode node : dataArray) {
                Card card = new Card();
                card.setName(node.path("name").asText());
                card.setSetId(node.path("set").path("id").asText());
                card.setCardNumber(node.path("number").asText() + "/" + node.path("set").path("printedTotal").asText());
                // grab the high res if available, otherwise small
                card.setImageUrl(node.path("images").path("large").asText());
                card.setRarity(node.path("rarity").asText("Unknown"));
                card.setArtist(node.path("artist").asText("Unknown"));
                
                // add to card list
                cardBatch.add(card);
            }
        }

        // save all cards to the database in one go
        cardRepository.saveAll(cardBatch);
        System.out.println("successfully seeded " + cardBatch.size() + " cards from Prismatic Evolutions!");
    }

    // method to fetch and seed Stellar Crown set from the Pokemon TCG API
    private void seedStellarCrown() throws Exception {
        // build the request (asking for set 'sv7' - Stellar Crown)
        String apiUrl = "https://api.pokemontcg.io/v2/cards?q=set.id:sv7&pageSize=250";
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("X-Api-Key", apiKey)
                .build();

        // send the request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // parse the JSON response
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode dataArray = root.path("data");

        List<Card> cardBatch = new ArrayList<>();

        if (dataArray.isArray()) {
            for (JsonNode node : dataArray) {
                Card card = new Card();
                card.setName(node.path("name").asText());
                card.setSetId(node.path("set").path("id").asText());
                card.setCardNumber(node.path("number").asText() + "/" + node.path("set").path("printedTotal").asText());
                // grab the high res if available, otherwise small
                card.setImageUrl(node.path("images").path("large").asText());
                card.setRarity(node.path("rarity").asText("Unknown"));
                card.setArtist(node.path("artist").asText("Unknown"));
                
                // add to card list
                cardBatch.add(card);
            }
        }

        // save all cards to the database in one go
        cardRepository.saveAll(cardBatch);
        System.out.println("successfully seeded " + cardBatch.size() + " cards from Stellar Crown!");
    }
}