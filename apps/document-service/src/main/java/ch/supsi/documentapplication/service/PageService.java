package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.model.SpacePage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageService extends ch.supsi.documentapplication.service.Service implements IPageService {

    private final IDatabaseDocumentStorage databaseDocumentStorage;

    @Autowired
    public PageService(IDatabaseDocumentStorage databaseDocumentStorage, IDatabaseSpaceStorage databaseSpaceStorage) {
        super(databaseSpaceStorage);
        this.databaseDocumentStorage = databaseDocumentStorage;
    }

    @Override
    public List<SpacePage> getPagesBySpace(String owner, Long spaceId) {
        var space = getSpaceOrThrow(owner, spaceId);
        return databaseDocumentStorage.getPagesBySpaceId(space.getId());
    }
}
