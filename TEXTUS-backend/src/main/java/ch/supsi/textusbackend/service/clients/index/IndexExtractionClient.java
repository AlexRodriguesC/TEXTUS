package ch.supsi.textusbackend.service.clients.index;

import ch.supsi.textusbackend.model.BookIndex;
import java.util.List;
import org.springframework.ai.content.Media;

public interface IndexExtractionClient {

    BookIndex extractBookIndex(List<Media> pages);
}
