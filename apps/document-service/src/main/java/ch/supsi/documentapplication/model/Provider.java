package ch.supsi.documentapplication.model;

public record Provider(String providerId, String externalId, Pricing pricing, String stability) {}
