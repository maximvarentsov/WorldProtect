package ru.gtncraft.worldprotect;

import org.mongodb.Document;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class Entity extends Document {

    public Entity(final Map<String, Object> map) {
        putAll(map);
    }

    @SuppressWarnings("unchecked")
    public <E> Collection<E> asCollection(final String key) {
        return (Collection<E>) get(key);
    }

    @SuppressWarnings("unchecked")
    public <E> Stream<E> stream(final String key) {
        return (Stream<E>) asCollection(key).stream();
    }

    public Entity asEntity(final String key) {
        if (containsKey(key)) {
            return new Entity((Document) get(key));
        }
        return null;
    }
}
