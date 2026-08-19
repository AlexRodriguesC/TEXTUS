package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Architecture(
        @JsonProperty("input_modalities") List<String> inputModalities,
        @JsonProperty("output_modalities") List<String> outputModalities,
        String tokenizer) {}
