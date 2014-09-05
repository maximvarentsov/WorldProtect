package ru.gtncraft.worldprotect.region;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class RegionCudeTest {

    @Test
    public void test() {
        int x1 = 0;
        int y1 = 254;
        int z1 = 0;
        int x2 = 254;
        int y2 = 0;
        int z2 = 254;
        RegionCube cudeRegion = new RegionCube(x1, y1, z1, x2, y2, z2);
        cudeRegion.setName("TestRegion");
        cudeRegion.addFlag(Flag.build);
        cudeRegion.addFlag(Flag.piston);
        cudeRegion.addFlag(Flag.pvp);
        cudeRegion.removeFlag(Flag.piston);
        cudeRegion.addOwner(UUID.fromString("b8089590-34db-11e4-8c21-0800200c9a66"));
        cudeRegion.addMember(UUID.fromString("bc13bd90-34db-11e4-8c21-0800200c9a66"));
        cudeRegion.addMember(UUID.fromString("c7a18110-34db-11e4-8c21-0800200c9a66"));
        cudeRegion.removeMember(UUID.fromString("c7a18110-34db-11e4-8c21-0800200c9a66"));

        String json = new Gson().toJson(cudeRegion);

        String result = "{\"lowerX\":0,\"lowerY\":0,\"lowerZ\":0,\"upperX\":254,\"upperY\":254,\"upperZ\":254,\"name\":\"testregion\",\"owners\":[\"b8089590-34db-11e4-8c21-0800200c9a66\"],\"members\":[\"bc13bd90-34db-11e4-8c21-0800200c9a66\"],\"flags\":[\"build\",\"pvp\"]}\n";

        RegionCube cubeRegion2 = new Gson().fromJson(result, RegionCube.class);
        String json2 = new Gson().toJson(cubeRegion2);

        assertEquals(json, json2);
    }

}
