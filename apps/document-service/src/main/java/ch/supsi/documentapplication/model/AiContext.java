package ch.supsi.documentapplication.model;

import java.util.List;

public record AiContext(String provider, String defaultModel, List<String> models) {}
