package com.github.yamikazoo.pokepad.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToMany;
import java.util.List;
import lombok.Data;
import lombok.ToString;

@Entity
@Data // Lombok annotation that automatically creates getters and setters removing the need to manually code them
@Table(name = "cards")
public class Card {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String setId;
    private String cardNumber; // e.g., "27/109"

    @Column(length = 500) // account for long image urls
    private String imageUrl;

    private String language; // "EN" or "JP"

    private String artist;
    private String rarity;

    // creates a many to many foreign key relationship with Collection
    @ToString.Exclude // avoid stack overflow from circular referencing of collections and cards
    @ManyToMany(mappedBy = "cards")
    private List<Collection> collections;
}