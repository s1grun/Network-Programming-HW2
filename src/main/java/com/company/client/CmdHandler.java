package com.company.client;

import com.company.common.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * The CmdHandler handles the input from the client input and sends it as a message to the server.
 */
public class CmdHandler implements Runnable {
    Socket socket;
    Client client;

    private Scanner console = new Scanner(System.in);
    boolean status = true;

    CmdHandler(Socket socket, Client client){
        this.socket = socket;
        this.client = client;


    }

    @Override
    public void run() {
        DataOutputStream output = null;
        try {
            output=new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (status) {
            try {
                String cmd= console.nextLine();
                String cmd_type = cmd.split(" ")[0];

                switch (cmd_type) {
                    case "disconnect":

                        Message.sendMsg(output,new Message("disconnect", "", client.getToken()));
                        status = false;
                        socket.close();
                        break;
                    case "try":
                        String cmd_text = cmd.split(" ")[1];
                        Message.sendMsg(output,new Message("try", cmd_text, client.getToken()));
                        break;
                    case "login":
                        String ctext = cmd.split(" ")[1];
                        Message.sendMsg(output,new Message("login", ctext));
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
