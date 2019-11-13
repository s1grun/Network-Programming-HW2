package com.company.client;

import com.company.client.ClientView;
import com.company.client.CmdHandler;
import com.company.common.Message;
import com.company.common.Serialize;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * The Client class handles the messages received from the Server.
 * The Client has two threads, one for handling receiving messages from the server and another thread
 * for handling input from the user and sends it to the server.
 * The Client will not be stuck while waiting for a reply from the server, it can execute other commands in the mean while.
 *
 */

public class Client {
    public String token = null;
    public static void main(String[] args)
    {

        Client client = new Client();
        client.startClient(client);

    }

    public String getToken(){
        return token;
    }

    /**
     * Start a new client socket to receive messages
     * @param client
     */
    public void startClient(Client client){
        try(Socket clientSocket =new Socket("127.0.0.1",3000)){

            System.out.println("connect to 3000");
            System.out.println("Welcome to Hangman, to start playing login first. Example: login john,123");

            Thread cmdhandler = new Thread(new CmdHandler(clientSocket,client));
            cmdhandler.setPriority(Thread.MAX_PRIORITY);
            cmdhandler.start();


            /**
             * While the client is connected we handle the logic for the client and update the client view
             */
            DataInputStream inStream = new DataInputStream(clientSocket.getInputStream());
            while(true){
                int datalen = inStream.readInt();
                byte[] data = new byte[datalen];
                inStream.readFully(data);
                Message msg =(Message) Serialize.toObject(data);


                String type = msg.getType();
                String body = msg.getBody();

                switch (type){
                    case "update":
                        new ClientView(msg);
                        break;
                    case "finish":
                        new ClientView(msg);
                        break;
                    case "token":
                        token = msg.getJwt();

                        break;
                    case "login":
                        System.out.println("server: "+" "+body);
                        System.out.println("please login");
                        break;

                    default:
                        System.out.println("server: "+" "+body);

                }
            }


        } catch (Exception e){
            System.err.println("Exception :"+e);
        }
    }

}



