package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.model.SpacePage;
import java.util.List;

public interface IPageService {

    List<SpacePage> getPagesBySpace(String owner, Long spaceId);
}
