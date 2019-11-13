package com.company.client;

import com.company.common.Message;

/**
 * The ClientView class handles the logic of updating the client view.
 */
public class ClientView {

    public ClientView(Message msg){
        String type = msg.getType();
        String body = msg.getBody();

        switch (type){
            case "update":
                String[] body_arr = body.split(",");
                String word = body_arr[0];
                String attempts_left = body_arr[1];
                String score = body_arr[2];
                System.out.println("word:"+ word +" attempts_left:"+attempts_left+" score:"+score);
                System.out.println("To guess a character or the word use the 'try' command and a character/word. Example: try a");
                break;
            case "finish":
                System.out.println("game finish, score: "+ body + " new game start!");
        }

    }


}
