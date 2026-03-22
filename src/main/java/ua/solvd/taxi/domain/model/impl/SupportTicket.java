package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

@Getter
@RequiredArgsConstructor
public class SupportTicket extends Entity {
    private final User user;
    private final String subject;
    private final String message;
    private final boolean isResolved;
}