﻿Kostra projektu je k dispozici ke stažení na https://gitlab.kiv.zcu.cz/UPG/Mapa_UPG_2021.git. 

Aplikace zobrazuje barevnou mapu s vrstevnicemi. 
Aplikace se spouští v příkazovém řádku, před spuštěním aplikace je nutno nejdříve spustit Bulid.cmd poté Run.cmd s argumentem pgm souboru např.: data\bin_data_plzen.pgm, nebo D:\UPG\bin_data_plzen.pgm.
Při prvotním spuštění se vrstevnice zobrazí po tolika metrech, aby se aplikace načetla rychle a nečekalo se až 20 minut na načtení. 
Po kolika metrech se budou vrstevnice načítat se dá nastavit v menuBaru -> Nastavení -> Vrstevnice, kde na uživatele vyskočí okno s volbou.
Mapa reaguje na kliknutí: 
levé kliknutí -> zobrazí se nadmořská výška bodu a zvýrazní se nejbližší vrstevnice. 
Pravé kliknutí 1x – vykreslí se 1.bod, 2x – vykreslí se 2.bod a body se spojí přímkou, zároveň vyskočí graf s převýšením. 
Klávesa R ruší efekty způsobené myší. 
Export se najde v levém horním rohu. Aplikace dokáže exportovat do PNG, ASCII, SVG a Tisknout. Vyexportovaná data se ukládají do složky exports.
Grafy nalezneme vedle menu pro export.
