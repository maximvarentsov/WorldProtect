package ru.gtncraft.worldprotect.flags;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;

public class PreventTest {
    @Test
    public void testToArray() {
        Collection<String> values = new ArrayList<>();
        for (Prevent flag : Prevent.values()) {
            values.add(flag.name());
        }
        assertArrayEquals(values.toArray(new String[0]), Prevent.toArray().toArray(new String[0]));
    }
}
