package ch.supsi.textusbackend.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;

public record BookIndex(Long id, Integer page, List<BookIndexEntry> children) {

    public BookIndex normalise(PDDocument pdfDocument) {
        if (children.isEmpty()) {
            return this;
        }

        int pageCount = pdfDocument.getNumberOfPages();

        List<int[]> absPages = new ArrayList<>();
        int previousRelativePage = -1;

        for (BookIndexEntry entry : children) {
            int relativePage = entry.fromPage();

            if (previousRelativePage != -1 && relativePage < previousRelativePage) {
                break;
            }

            absPages.add(new int[] {Math.min(relativePage, pageCount)});
            previousRelativePage = relativePage;
        }

        List<BookIndexEntry> normalised = new ArrayList<>();
        var offset = page;

        for (int i = 0; i < absPages.size(); i++) {
            int fromPage = absPages.get(i)[0] + offset;
            int toPage = ((i + 1 < absPages.size()) ? absPages.get(i + 1)[0] - 1 : pageCount) + offset;

            normalised.add(new BookIndexEntry(
                    children.get(i).title(), i, Math.min(fromPage, toPage), Math.max(toPage, fromPage)));
        }

        return new BookIndex(id, page, normalised);
    }
}
