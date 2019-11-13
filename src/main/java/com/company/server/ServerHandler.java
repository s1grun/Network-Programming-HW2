package com.company.server;

import com.company.common.Message;
import com.company.common.Serialize;
import com.company.common.Words;
import com.company.server.GameHandler;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;

/**
 * The ServerHandler class handles the game for each client
 */

public class ServerHandler implements Runnable{
    private Socket clientSocket;
    private int score;
    GameHandler game = null;
    Key key;
    boolean status = true;

    public ServerHandler(Socket socket){
        this.clientSocket = socket;
        this.score = 0;

    }



    /**
     * The run method implements method of interface Java.Lang.Runnable.
     * So when a new thread is created this method is executed.
     */
    @Override
    public void run(){
        DataInputStream input= null;
        DataOutputStream output=null;
        try {
            input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            output=new DataOutputStream(clientSocket.getOutputStream());


            /**
             * While the client is connected the status is true and the client can communicate with the server
             */

            while (status){
                int datalen = input.readInt();
//                System.out.println(datalen);
                byte[] data = new byte[datalen];
                input.readFully(data);
                Message msg =(Message) Serialize.toObject(data);


                String type = msg.getType();
                String body = msg.getBody();

                switch (type){
                    case "login":
                        String username = body.split(",")[0];
                        String pw = body.split(",")[1];

                        /**
                         * To be able to start a game the user must first login successfully
                         */

                        if (username.equals("john")&& pw.equals("123")){

                            key= Keys.secretKeyFor(SignatureAlgorithm.HS256);
                            Date date= new Date();
                            long d = date.getTime();
                            date = new Date(d+30*60*1000);
                            /**
                             * Here we are constructing the JWT token for authentication
                             */
                            String jws = Jwts.builder().setSubject("john").setExpiration(date).signWith(key).compact();
                            Message login_msg = new Message("token","login successful!", jws);
                            Message.sendMsg(output,login_msg);

                            game = new GameHandler(score);
                            Message start_msg = new Message("update",game.getUnderline()+","+Integer.toString(game.getCounter())+","+Integer.toString(game.getScore()));
                            Message.sendMsg(output,start_msg);

                        }else{
                            Message start_msg = new Message("login","user is not authenticated" );
                            Message.sendMsg(output,start_msg);
                        }

                        break;
                    case "try":
                        String token = msg.getJwt();

                        //System.out.println(token);
                        if (token == null){
                            Message start_msg = new Message("login","user is not authenticated" );
                            Message.sendMsg(output,start_msg);
                            break;
                        }

                        try{
                            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                            if( Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject().equals("john")){
                                String str = body.toLowerCase();
                                Message res =  game.guess(str);
                                Message.sendMsg(output,res);
                                if(res.getType().equals("finish")){
                                    this.score = Integer.valueOf(res.getBody());
                                    game = new GameHandler(score);
                                    Message start_msg = new Message("update",game.getUnderline()+","+Integer.toString(game.getCounter())+","+Integer.toString(game.getScore()) );
                                    Message.sendMsg(output,start_msg);
                                }
                            }else{
                                Message start_msg = new Message("login","user is not authenticated" );
                                Message.sendMsg(output,start_msg);
                            }
                        }catch (JwtException e){
                            Message start_msg = new Message("login","user is not authenticated" );
                            Message.sendMsg(output,start_msg);
                        }


                        break;
                    case "disconnect":
                        Message disconnect = new Message("disconnect","disconnected successfully" );
                        Message.sendMsg(output,disconnect);
                        close(input,output);
                        break;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * If we get the disconnect message type we close the connection and kill the current thread.
     */
    public void close(DataInputStream input, DataOutputStream output) throws IOException {
        status = false;
        input.close();
        output.close();
        System.out.println(Thread.currentThread()+" client quit");
        clientSocket.close();
        Thread.currentThread().stop();

    }
}
