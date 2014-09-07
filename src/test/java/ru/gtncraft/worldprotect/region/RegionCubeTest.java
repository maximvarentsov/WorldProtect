package ru.gtncraft.worldprotect.region;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class RegionCubeTest {

    @Test
    public void serializationTest() {
        int x1 = 0;
        int y1 = 254;
        int z1 = 0;
        int x2 = 254;
        int y2 = 0;
        int z2 = 254;
        RegionCube region = new RegionCube(x1, y1, z1, x2, y2, z2);
        region.setName("TestRegion");
        region.addFlag(Flag.build);
        region.addFlag(Flag.piston);
        region.addFlag(Flag.pvp);
        region.removeFlag(Flag.piston);
        region.addOwner(UUID.fromString("b8089590-34db-11e4-8c21-0800200c9a66"));
        region.addMember(UUID.fromString("bc13bd90-34db-11e4-8c21-0800200c9a66"));
        region.addMember(UUID.fromString("c7a18110-34db-11e4-8c21-0800200c9a66"));
        region.removeMember(UUID.fromString("c7a18110-34db-11e4-8c21-0800200c9a66"));

        String json = new Gson().toJson(region);

        String result = "{\"lowerX\":0,\"lowerY\":0,\"lowerZ\":0,\"upperX\":254,\"upperY\":254,\"upperZ\":254,\"name\":\"testregion\",\"owners\":[\"b8089590-34db-11e4-8c21-0800200c9a66\"],\"members\":[\"bc13bd90-34db-11e4-8c21-0800200c9a66\"],\"flags\":[\"build\",\"pvp\"]}\n";

        RegionCube region2 = new Gson().fromJson(result, RegionCube.class);
        String json2 = new Gson().toJson(region2);

        assertEquals(json, json2);
    }

    @Test
    public void overlayTest() {
        //                                   x   y    z     x    y   z
        RegionCube center =  new RegionCube(-19, 16, -136, -11, 13, -129);
        RegionCube inside =  new RegionCube(-13, 14, -131, -17, 13, -134);
        RegionCube outside = new RegionCube(-10, 13, -127, -21, 14, -137);
        RegionCube near =    new RegionCube(-7,  18, -113, -10, 13, -116);

        boolean contains = inside.AABB(center) && outside.AABB(center) && !near.AABB(center);

        assertEquals(contains, true);
    }
}
