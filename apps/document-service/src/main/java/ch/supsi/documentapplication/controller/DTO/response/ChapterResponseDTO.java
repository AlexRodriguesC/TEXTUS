package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.Chapter;
import ch.supsi.documentapplication.model.learn.Node;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public record ChapterResponseDTO(@NotNull Integer index, @NotNull String title, @NotNull String description, @NotNull List<NodeResponseDTO> nodes) {

    public static ChapterResponseDTO from(Chapter chapter) {
        var nodes = chapter.getNodes();
        var nodeDTOs = new ArrayList<NodeResponseDTO>();
        for (Node node : nodes) {
            NodeResponseDTO dto = new NodeResponseDTO(node.getId(), node.getType(), node.getTopics());
            nodeDTOs.add(dto);
        }

        return new ChapterResponseDTO(chapter.getIndex(), chapter.getTitle(), chapter.getDescription(), nodeDTOs);
    }
}
