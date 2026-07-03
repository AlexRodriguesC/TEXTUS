package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.model.RawModel;
import java.util.List;

public interface LLMRestClient {

    List<RawModel> getModels();
}
