package ch.supsi.documentapplication.model;

public record AiUsage(int tokensIn, int tokensOut, String provider, String model) {}
