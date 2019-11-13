package com.company.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Words class is used to generate words for the game
 */
public class Words {
    public static String getWord() throws IOException {
        File file = new File("./words.txt");

        BufferedReader br = new BufferedReader(new FileReader(file));

        List<String> strArr = new ArrayList<>();
        String st;
        while ((st = br.readLine()) != null)
            strArr.add(st);

        Random random = new Random();
        String select_word = strArr.get(random.nextInt(strArr.size()));
        System.out.println(select_word);

        return select_word;
    }
}
