package ch.supsi.documentapplication.model;

import ch.supsi.documentapplication.controller.DTO.request.PatchSettingsDTO;
import ch.supsi.documentapplication.exception.InvalidInputException;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class PathSettings {

    private Integer questionPercentage;
    private Integer listPercentage;
    private Integer groupPercentage;
    private Integer pairPercentage;

    public static PathSettings from(PatchSettingsDTO dto) {
        return new PathSettings(
                dto.getQuestionPercentage(),
                dto.getListPercentage(),
                dto.getGroupPercentage(),
                dto.getPairPercentage());
    }

    public void validate() {
        int sum = Stream.of(
                        this.getQuestionPercentage(),
                        this.getListPercentage(),
                        this.getGroupPercentage(),
                        this.getPairPercentage())
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        if (sum != 100) {
            throw InvalidInputException.invalidSettings();
        }
    }
}
