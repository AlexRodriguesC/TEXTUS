package ch.supsi.textusbackend.model;

import java.util.List;

public record BookIndexEntry(String title, int position, int fromPage, int toPage, List<BookIndexEntry> children) {

    public BookIndexEntry(String title, int fromPage, List<BookIndexEntry> children) {
        this(title, -1, fromPage, 0, children);
    }
}
