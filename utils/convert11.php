#! /usr/bin/env php
<?php
foreach (glob("*.json") as $file) {
    $json = json_decode(file_get_contents(__DIR__ . DIRECTORY_SEPARATOR . $file), true);

    $json['flags'] = fixFlags($json['flags']);

    foreach ($json['regions'] as &$region) {
        $region['flags'] = fixFlags($region['flags']);
    }

    $output = json_encode($json, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
    file_put_contents($file . ".converted", $output);
}

function fixFlags(array $flags) {
    $result = [];
    foreach ($flags as $flag) {
        if ($flag == 'fireSpread' || $flag == 'fallingBlocks') {
            continue;
        }
        if ($flag == 'leavesDecay') {
            $flag = 'grow';
        }
        if ($flag == 'fade') {
            $flag = "spread";
        }
        if ($flag == 'explode') {
            $flag = "entityBlockExplode";
        }
        $result[] = $flag;
    }
    return array_values(array_unique($result));
}
