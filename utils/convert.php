#! /usr/bin/env php
<?php
$ts = time();
foreach (glob("*.json") as $file) {
    $json = json_decode(file_get_contents(__DIR__ . DIRECTORY_SEPARATOR . $file), true);

    $worldFlags = [];
    $regions = [];

    foreach ($json['world']['flags'] as $key => $value) {
        if ($value == true) {
            $worldFlags[] = $key;
        }
    }
    $total = count($json['regions']);
    $i = 1;
    foreach ($json['regions'] as $region) {
        echo "convert " . ($i++) . " of " . $total . PHP_EOL;
        $lowerX = $region['p1']['x'];
        $lowerY = $region['p1']['y'];
        $lowerZ = $region['p1']['z'];

        $upperX = $region['p2']['x'];
        $upperY = $region['p2']['y'];
        $upperZ = $region['p2']['z'];

        $name = $region['name'];

        $owners = [];
        foreach ($region['owners'] as $uuid) {
            $owners[] = $uuid;
        }

        $members = [];
        foreach ($region['members'] as $uuid) {
            $members[] = $uuid;
        }

        $flags = [];
        foreach ($region['flags'] as $key => $value) {
            if ($value == true) {
                $flags[] = $key;
            }
        }

        $regions[] = [
            'lowerX' => $lowerX,
            'lowerY' => $lowerY,
            'lowerZ' => $lowerZ,
            'upperX' => $upperX,
            'upperY' => $upperY,
            'upperZ' => $upperZ,
            'createdAt' => $ts++,
            'name' => $name,
            'owners' => $owners,
            'members' => $members,
            'flags' => $flags
        ];
        echo "convert region (" . ($i++) . " of " . $total  . ")" . PHP_EOL;
    }
    $output = json_encode(['flags' => $worldFlags, 'regions' => $regions], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
    file_put_contents($file . ".converted", $output);
}