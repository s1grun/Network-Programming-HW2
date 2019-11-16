package com.company.client.view;

import com.company.client.net.Connection;
import com.company.common.Message;

import java.util.Scanner;

/**
 * Created by weng on 2019/11/14.
 */
public class Client {
    private Scanner console = new Scanner(System.in);
    private boolean status = true;
    private Connection connection;

    public static void main(String[] args)
    {

        Client client= new Client();
        client.startClient();


    }





    public void startClient(){
        connection = new Connection();
        connection.startClient();
        while (status) {
            try {
                String cmd= console.nextLine();
                String cmd_type = cmd.split(" ")[0];

                switch (cmd_type) {
                    case "disconnect":

//                        Message.sendMsg(output,new Message("disconnect", "", client.getToken()));
//                        status = false;
//                        socket.close();

                        break;
                    case "try":
                        String cmd_text = cmd.split(" ")[1];
//                        Message.sendMsg(output,new Message("try", cmd_text, client.getToken()));
                        connection.sendMsg(cmd_text);
                        break;
                    case "login":
//                        String ctext = cmd.split(" ")[1];
//                        Message.sendMsg(output,new Message("login", ctext));
                        break;
                    default:
                        System.out.println("Unknown command");
                        break;

                }
            } catch (Exception e) {
                System.out.println("client read cmd failed");
            }
        }
    }
}
