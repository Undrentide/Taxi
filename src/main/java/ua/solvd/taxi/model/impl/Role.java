package ua.solvd.taxi.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.model.Entity;

@Getter
@RequiredArgsConstructor
public class Role extends Entity {
    private final String name;
}