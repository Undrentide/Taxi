package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Review extends Entity {
    private final Order order;
    private final Integer rating;
    private final String comment;

    public Review(UUID id, Order order, Integer rating, String comment) {
        super(id);
        this.order = order;
        this.rating = rating;
        this.comment = comment;
    }
}