package ch.supsi.textusbackend;

import ch.supsi.textusbackend.dataaccess.IFileSystemBookStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationStoppedEventListener {

    private final IFileSystemBookStorage bookStorage;

    @Autowired
    public ApplicationStoppedEventListener(IFileSystemBookStorage bookStorage) {
        this.bookStorage = bookStorage;
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        log.debug("Handling context closed event...");

        bookStorage.deleteAll();
    }
}
