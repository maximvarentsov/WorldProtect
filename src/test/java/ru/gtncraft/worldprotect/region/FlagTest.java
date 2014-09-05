package ru.gtncraft.worldprotect.region;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;

public class FlagTest {
    @Test
    public void testToArray() {
        Collection<String> values = new ArrayList<>();
        for (Flag flag : Flag.values()) {
            values.add(flag.name());
        }
        assertArrayEquals(values.toArray(new String[0]), Flag.toArray().toArray(new String[0]));
    }
}
