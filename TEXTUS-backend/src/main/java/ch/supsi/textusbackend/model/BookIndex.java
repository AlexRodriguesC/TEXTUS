package ch.supsi.textusbackend.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;

public record BookIndex(List<BookIndexEntry> children) {

    public BookIndex normalise(PDDocument pdfDocument) {
        List<BookIndexEntry> flat = flatten(children);
        if (flat.isEmpty()) {
            return this;
        }

        int pageCount = pdfDocument.getNumberOfPages();

        List<int[]> absPages = new ArrayList<>();
        int previousRelativePage = -1;

        for (BookIndexEntry entry : flat) {
            int relativePage = entry.fromPage();

            if (previousRelativePage != -1 && relativePage < previousRelativePage) {
                break;
            }

            int absolutePage = Math.min(relativePage, pageCount);
            absPages.add(new int[] {absolutePage});

            previousRelativePage = relativePage;
        }

        List<BookIndexEntry> normalised = new ArrayList<>();
        for (int i = 0; i < absPages.size(); i++) {
            int fromPage = absPages.get(i)[0];
            int toPage = (i + 1 < absPages.size()) ? absPages.get(i + 1)[0] - 1 : pageCount;

            normalised.add(new BookIndexEntry(
                    flat.get(i).title(), i, Math.min(fromPage, toPage), Math.max(toPage, fromPage), List.of()));
        }

        return new BookIndex(normalised);
    }

    private static List<BookIndexEntry> flatten(List<BookIndexEntry> entries) {
        List<BookIndexEntry> result = new ArrayList<>();
        for (BookIndexEntry entry : entries) {
            result.add(new BookIndexEntry(entry.title(), entry.fromPage(), List.of()));
            result.addAll(flatten(entry.children()));
        }
        return result;
    }
}
