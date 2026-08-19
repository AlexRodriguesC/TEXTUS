package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.model.RawModel;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InferenceService {

    private final LLMRestClient llmRestClient;

    @Autowired
    public InferenceService(LLMRestClient llmRestClient) {
        this.llmRestClient = llmRestClient;
    }

    public List<RawModel> getAvailableModels() {
        return llmRestClient.getModels();
    }
}
