package com.company.common;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.channels.WritableByteChannel;
/**
 * The Get Words class is used to generate words for the game
 */
public class GetWords {
//    public static String word() throws IOException {
//        Path path = Paths.get("./words.txt");
//        String st;
//        List<String> strArr = new ArrayList<>();
//
//        BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"));
//
//        while ((st = reader.readLine()) != null)
//            strArr.add(st);
//
//        Random random = new Random();
//        String select_word = strArr.get(random.nextInt(strArr.size()));
//        System.out.println(select_word);
//
//        return select_word;
//
//    }
    public static void main(String[] args) throws IOException {
        word();
    }

    public static String word() throws IOException {

        String res="";
//        List<String> strArr = new ArrayList<>();
        String[] strArr;
        FileInputStream inf = new FileInputStream("./words.txt");
        String select_word ="";
        try (FileChannel inChannel = inf.getChannel()) {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            WritableByteChannel out = Channels.newChannel(bos);

            ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
            byte[] bytes;



            int c = 0;
            while ((c = inChannel.read(buffer)) != -1) {
                buffer.flip();
                out.write(buffer);

                buffer.clear();
            }
            res = new String(bos.toByteArray());
            strArr = res.split("\n");
//            System.out.println(strArr.length);
            Random random = new Random();
            select_word = strArr[random.nextInt(strArr.length-1)];
            System.out.println(select_word);
//            System.out.println(res);
//            return res;
        }catch (IOException e) {
            e.printStackTrace();
        }



//
        return select_word;

    }





}
