package ru.gtncraft.worldprotect;

import com.google.common.collect.ImmutableList;
import org.mongodb.Document;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Entity extends Document {

    public Entity(final Map map) {
        putAll(map);
    }

    public List<Entity> asList(final String key) {
        if (containsKey(key)) {
            return ((List<Document>) get(key)).stream().map(Entity::new).collect(Collectors.toList());
        }
        return ImmutableList.of();
    }

    public List<String> asListString(final String key) {
        if (containsKey(key)) {
            return ((List<String>) get(key));
        }
        return ImmutableList.of();
    }

    public Entity asEntity(final String key) {
        if (containsKey(key)) {
            return new Entity((Document) get(key));
        }
        return null;
    }

    public Stream<Entity> stream(final String key) {
        return asList(key).stream();
    }
}
