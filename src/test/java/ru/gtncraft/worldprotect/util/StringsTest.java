package ru.gtncraft.worldprotect.util;

import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.assertArrayEquals;

public class StringsTest extends TestCase {

    @Test
    public void testPartial() {
        assertArrayEquals(
            Arrays.asList("foo", "foobar", "foobaz").toArray(new String[0]),
            Strings.partial("foo", ImmutableList.of("foo", "bar", "baz", "foobar", "foobaz")).toArray(new String[0])
        );
    }
}
