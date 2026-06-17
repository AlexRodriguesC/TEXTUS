package ch.supsi.textusbackend.model;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public record Entity(String name, String type) {

    public Entity validate() {
        var allowedValues =
                Arrays.stream(EntityType.values()).map(EntityType::name).toList();
        if (allowedValues.contains(type.toUpperCase())) {
            return new Entity(name, EntityType.CONCEPT.name());
        }
        return this;
    }

    @Override
    @NotNull
    public String toString() {
        return "Entity{" + "name='" + name + '\'' + ", type='" + type + '\'' + '}';
    }
}
