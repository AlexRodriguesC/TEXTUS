package ch.supsi.documentapplication.controller.DTO.mapper;

import ch.supsi.documentapplication.controller.DTO.response.ModelResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.ModelsResponseDTO;
import ch.supsi.documentapplication.model.Provider;
import ch.supsi.documentapplication.model.RawModel;

import java.util.List;

public class ModelMapper {

    public ModelsResponseDTO toDTO(List<RawModel> rawModels) {
        List<ModelResponseDTO> models = rawModels.stream()
                .filter(this::isTextOnly)
                .filter(m -> m.contextLength() > 0)
                .filter(m -> m.stability() == null || !m.stability().equals("unstable"))
                .map(this::toModelResponseDTO)
                .toList();
        return new ModelsResponseDTO(models);
    }

    private boolean isTextOnly(RawModel model) {
        var arch = model.architecture();
        return arch != null
                && arch.inputModalities() != null
                && arch.inputModalities().contains("text")
                && arch.outputModalities() != null
                && arch.outputModalities().contains("text");
    }

    private ModelResponseDTO toModelResponseDTO(RawModel model) {
        var provider = model.providers() == null || model.providers().isEmpty()
                ? null
                : model.providers().getFirst();

        return new ModelResponseDTO(
                model.id(),
                model.name(),
                model.description(),
                model.pricing().prompt(),
                model.pricing().completion(),
                model.contextLength(),
                resolveStability(model, provider),
                model.free()
        );
    }

    private String resolveStability(RawModel model, Provider provider) {
        if (model.stability() != null) {
            return model.stability().toLowerCase();
        }
        if (provider != null && provider.stability() != null) {
            return provider.stability();
        }
        return "unknown";
    }
}
