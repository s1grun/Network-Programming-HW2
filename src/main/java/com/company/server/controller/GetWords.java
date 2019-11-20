package com.company.server.controller;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Random;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
/**
 * The Get Words class is used to generate words for the game.
 * We implemented it using the non-blocking file handling in java.
 */
public class GetWords {
    public static void main(String[] args) throws IOException {
        word();
    }

    public static String word() throws IOException {

        String res="";
        String[] strArr;
        FileInputStream readFile = new FileInputStream("./words.txt");
        String select_word ="";
        try (FileChannel fileChannel = readFile.getChannel()) {

            ByteArrayOutputStream byteArr= new ByteArrayOutputStream();
            WritableByteChannel out = Channels.newChannel(byteArr);

            ByteBuffer buffer = ByteBuffer.allocateDirect(8192);



            while (( fileChannel.read(buffer)) != -1) {
                buffer.flip();
                out.write(buffer);

                buffer.clear();
            }
            res = new String(byteArr.toByteArray());
            strArr = res.split("\n");

            Random random = new Random();
            select_word = strArr[random.nextInt(strArr.length-1)];
            System.out.println(select_word);

        }catch (IOException e) {
            e.printStackTrace();
        }

        return select_word;

    }





}
