package ru.gtncraft.worldprotect.region;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CuboidTest {

    Location l1, l2;
    World w;

    public CuboidTest() {

        w = mock(World.class);
        when(w.getName()).thenReturn("world");

        l1 = mock(Location.class);
        when(l1.getWorld()).thenReturn(w);

        l2 = mock(Location.class);
        when(l2.getWorld()).thenReturn(w);
    }

    @Test
    public void testGetVolume() {
        when(l1.getBlockX()).thenReturn(0);
        when(l1.getBlockY()).thenReturn(0);
        when(l1.getBlockZ()).thenReturn(0);

        when(l2.getBlockX()).thenReturn(15);
        when(l2.getBlockY()).thenReturn(15);
        when(l2.getBlockZ()).thenReturn(15);


        Cuboid cuboid = new Cuboid(l1, l2);

        assertEquals(4096, cuboid.getVolume());
    }

    @Test
    public void testGetChunksCoords() {
        when(l1.getBlockX()).thenReturn(7999);
        when(l1.getBlockY()).thenReturn(0);
        when(l1.getBlockZ()).thenReturn(6933);

        when(l2.getBlockX()).thenReturn(8000);
        when(l2.getBlockY()).thenReturn(70);
        when(l2.getBlockZ()).thenReturn(7000);

        Cuboid cuboid = new Cuboid(l1, l2);
        Multimap actual = ArrayListMultimap.create();
        actual.put(500, 433);
        actual.put(500, 434);
        actual.put(500, 435);
        actual.put(500, 436);
        actual.put(500, 437);
        actual.put(499, 433);
        actual.put(499, 434);
        actual.put(499, 435);
        actual.put(499, 436);
        actual.put(499, 437);

        assertTrue(actual.asMap().equals(cuboid.getChunksCoords().asMap()));
    }
}
