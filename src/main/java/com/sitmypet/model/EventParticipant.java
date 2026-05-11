package com.sitmypet.model;

public class EventParticipant {
    private int eventId;
    private int userId;

    public EventParticipant() {}

    public EventParticipant(int eventId, int userId) {
        this.eventId = eventId;
        this.userId = userId;
    }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "Utilisateur ID: " + userId + " (inscrit à l'Event ID: " + eventId + ")";
    }
}
