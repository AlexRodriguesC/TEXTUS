package ch.supsi.textusbackend.model;

import java.util.List;

public record BookIndexEntry(String title, int pageNumber, List<BookIndexEntry> children) {}
