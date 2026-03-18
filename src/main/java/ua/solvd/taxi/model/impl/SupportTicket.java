package ua.solvd.taxi.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.model.Entity;

@Getter
@RequiredArgsConstructor
public class SupportTicket extends Entity {
    private final User user;
    private final String subject;
    private final String message;
    private final boolean isResolved;
}