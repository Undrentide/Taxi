package ua.solvd.taxi.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.model.Entity;

@Getter
@RequiredArgsConstructor
public class Review extends Entity {
    private final Order order;
    private final int rating;
    private final String comment;
}