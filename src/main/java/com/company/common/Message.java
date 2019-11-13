package com.company.common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

/**
 * The Message class handles the data structure of the messages being passed from the both client and server
 */
public class Message implements Serializable {
    private String type;
    private String body;



    private String jwt = null;

    public Message(String type, String body){
        this.type = type;
        this.body = body;
    }
    public Message(String type, String body, String token){
        this.type = type;
        this.body = body;
        this.jwt = token;
    }

    public String getBody(){
        return body;
    }
    public String getJwt() {
        return jwt;
    }


    public String getType() {
        return type;
    }



    public static void sendMsg(DataOutputStream output, Message msg) throws IOException {

        Serialize serialized = new Serialize(msg);
        output.writeInt(serialized.getLength());
        output.write(serialized.getOut());
        output.flush();
    }
}
