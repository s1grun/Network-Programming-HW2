package com.company.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Get Words class is used to generate words for the game
 */
public class GetWords {
    public static String word() throws IOException {
        Path path = Paths.get("./words.txt");
        String st;
        List<String> strArr = new ArrayList<>();

        BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"));

        while ((st = reader.readLine()) != null)
            strArr.add(st);

        Random random = new Random();
        String select_word = strArr.get(random.nextInt(strArr.size()));
        System.out.println(select_word);

        return select_word;

    }
}
