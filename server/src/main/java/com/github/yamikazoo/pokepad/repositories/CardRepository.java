package com.github.yamikazoo.pokepad.repositories;

import com.github.yamikazoo.pokepad.models.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository     // Data Access Object (DAO) for Card entity
public interface CardRepository extends JpaRepository<Card, Long> {
    // extending the JpaRepository provides CRUD methods like save(), findById(), findAll(), delete(), etc.
    List<Card> findByImageUrlStartingWith(String prefix);
}