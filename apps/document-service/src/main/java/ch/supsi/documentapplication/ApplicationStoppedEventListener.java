package ch.supsi.documentapplication;

import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class ApplicationStoppedEventListener {

    private final IFileSystemDocumentStorage fileSystemDocumentStorage;

    @Autowired
    public ApplicationStoppedEventListener(IFileSystemDocumentStorage fileSystemDocumentStorage) {
        this.fileSystemDocumentStorage = fileSystemDocumentStorage;
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        log.debug("Handling context closed event...");

        fileSystemDocumentStorage.deleteAll();
    }
}
