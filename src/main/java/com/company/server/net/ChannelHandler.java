package com.company.server.net;

import com.company.common.Message;
import com.company.common.Serialize;
import com.company.server.model.GameHandler;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.Key;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;

/**
 * The ChannelHandler class handles the game for each client
 */

public class ChannelHandler implements Runnable {
    private Server server;
    public SocketChannel channel;
    private int score;
    GameHandler game = null;
//    Key key;
    SelectionKey skey;
    boolean status = true;
    private ByteBuffer clientMessage = ByteBuffer.allocateDirect(1024);
    private Queue<Message> readQueue = new ArrayDeque<>();
    private Queue<byte[]> sendQueue =new ArrayDeque<byte[]>();
    private Queue<Message> blockTaskQueue = new ArrayDeque<>();

    public ChannelHandler(Server server, SocketChannel channel){
        this.server = server;
        this.channel = channel;
//        this.skey = skey;
    }

    public void readMsg() throws IOException, InterruptedException {
        clientMessage.clear();
        int numOfReadBytes = channel.read(clientMessage);
        if (numOfReadBytes == -1)
            throw new IOException("Client has closed connection.");

        clientMessage.flip();
        byte[] bytes = new byte[clientMessage.remaining()];
        clientMessage.get(bytes);

//        synchronized(readQueue){
        readQueue.add((Message) Serialize.toObject(bytes));
//        }


//        ForkJoinPool.commonPool().execute(this);
//        new Thread(this).start();

//        System.out.println("read message over");
//        ForkJoinPool.commonPool().execute(this);
//        new Thread(this).start();
        while (readQueue.size()>0){
//            System.out.println(readQueue);
            Message msg = readQueue.poll();
            String type = msg.getType();
            String body = msg.getBody();
            System.out.println("From "+skey+" "+msg.getType()+' '+msg.getBody());

            switch (type){
                case "try":
//                    System.out.println(game);
//                    System.out.println(game.equals(null));
                    if(game==null){
                        Message start_msg = new Message("err","use 'start' command to start the game!" );
                        writeSendQueue(start_msg);
                        System.out.println("send err");
                        break;
                    }
                    String str = body.toLowerCase();
                    Message res =  game.guess(str);
                    writeSendQueue(res);
                    if(res.getType().equals("finish")){
                        synchronized (blockTaskQueue){
                            blockTaskQueue.add(res);
                            ForkJoinPool.commonPool().execute(this);
                        }
//                        this.score = Integer.valueOf(res.getBody());
//                        game = new GameHandler(score);
//                        Message start_msg = new Message("update",game.getUnderline()+","+Integer.toString(game.getCounter())+","+Integer.toString(game.getScore()) );
//                        writeSendQueue(start_msg);
                    }

                    break;
                case "start":
//                    game = new GameHandler(score);
//                    Message start_msg = new Message("update",game.getUnderline()+","+Integer.toString(game.getCounter())+","+Integer.toString(game.getScore()));
//                    writeSendQueue(start_msg);
                    synchronized (blockTaskQueue){
                        blockTaskQueue.add(msg);
                        ForkJoinPool.commonPool().execute(this);
                    }

                    break;
                case "disconnect":
                    disconnect();
                    break;
                default:
                    System.out.println("no match type "+type+" "+ msg.getBody());
                    break;
            }


        }


    }
    @Override
    public void run() {
        synchronized (blockTaskQueue){
            while (blockTaskQueue.size()>0){
                Message msg = blockTaskQueue.poll();
                String type = msg.getType();
//            String body = msg.getBody();

                if (type.equals("start")){
//                    try {
//                        Thread.currentThread().sleep(10000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    game = new GameHandler(score);
                    Message start_msg = new Message("update",game.getUnderline()+","+Integer.toString(game.getCounter())+","+Integer.toString(game.getScore()));
                    try {
                        writeSendQueue(start_msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if(type.equals("finish")){
                    this.score = Integer.valueOf(msg.getBody());
                    game = new GameHandler(score);
                    Message start_msg = new Message("update",game.getUnderline()+","+Integer.toString(game.getCounter())+","+Integer.toString(game.getScore()) );
                    try {
                        writeSendQueue(start_msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
//    @Override
//    public void run() {
//
//        while (readQueue.size()>0){
////            System.out.println(readQueue);
//                Message msg = readQueue.poll();
//                String type = msg.getType();
//                String body = msg.getBody();
//                System.out.println("From "+skey+" "+msg.getType()+' '+msg.getBody());
//
//                switch (type){
//                    case "try":
//                        String str = body.toLowerCase();
//                        Message res = null;
//                        try {
//                            res = game.guess(str);
//                            writeSendQueue(res);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                        if(res.getType().equals("finish")){
//                            this.score = Integer.valueOf(res.getBody());
//                            game = new GameHandler(score);
//                            Message start_msg = new Message("update",game.getUnderline()+","+Integer.toString(game.getCounter())+","+Integer.toString(game.getScore()) );
//                            try {
//                                writeSendQueue(start_msg);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        break;
//                    case "start":
////                        System.out.println("run start");
//                        game = new GameHandler(score);
//                        Message start_msg = new Message("update",game.getUnderline()+","+Integer.toString(game.getCounter())+","+Integer.toString(game.getScore()));
//                        try {
//                            writeSendQueue(start_msg);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    case "disconnect":
//                        disconnect();
//                        break;
//                    default:
//                        System.out.println("no match type "+type+" "+ msg.getBody());
//                        break;
//                }
//
//
//
//
////                try {
////                    writeSendQueue(msg);
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
//            }
////        }
//
//    }

    public void writeSendQueue(Message msg) throws IOException {
        Serialize smsg = new Serialize(msg);
        byte[] bytes = smsg.getOut();

        synchronized (sendQueue) {
            sendQueue.add(bytes);
        }

//        System.out.println("write into queue, size:"+sendQueue.size());
//        System.out.println(key.isWritable());
//        server.addKey(key);
//        server.wakeupSelector(key);
        server.timeToSend = true;
        server.wakeupSelector(skey);

    }

    public void sendMessage() throws IOException {
//        System.out.println("send msg to client");
        synchronized (sendQueue) {
            while (sendQueue.size() > 0) {
                ByteBuffer message = ByteBuffer.wrap(sendQueue.poll());
                channel.write(message);
            }
        }
    }

    public void disconnect(){
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }







    /**
     * The run method implements method of interface Java.Lang.Runnable.
     * So when a new thread is created this method is executed.
     */
//    @Override
//    public void run(){
//
//    }
////    public void run(){
//        DataInputStream input= null;
//        DataOutputStream output=null;
//        try {
//            input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
//            output=new DataOutputStream(clientSocket.getOutputStream());
//
//
//            /**
//             * While the client is connected the status is true and the client can communicate with the server
//             */
//
//            while (status){
//                int datalen = input.readInt();
////                System.out.println(datalen);
//                byte[] data = new byte[datalen];
//                input.readFully(data);
//                Message msg =(Message) Serialize.toObject(data);
//
//
//                String type = msg.getType();
//                String body = msg.getBody();
//
//                switch (type){
//                    case "login":
//                        String username = body.split(",")[0];
//                        String pw = body.split(",")[1];
//
//                        /**
//                         * To be able to start a game the user must first login successfully
//                         */
//
//                        if (username.equals("john")&& pw.equals("123")){
//
//                            key= Keys.secretKeyFor(SignatureAlgorithm.HS256);
//                            Date date= new Date();
//                            long d = date.getTime();
//                            date = new Date(d+30*60*1000);
//                            /**
//                             * Here we are constructing the JWT token for authentication
//                             */
//                            String jws = Jwts.builder().setSubject("john").setExpiration(date).signWith(key).compact();
//                            Message login_msg = new Message("token","login successful!", jws);
//                            Message.sendMsg(output,login_msg);
//
//                            game = new GameHandler(score);
//                            Message start_msg = new Message("update",game.getUnderline()+","+Integer.toString(game.getCounter())+","+Integer.toString(game.getScore()));
//                            Message.sendMsg(output,start_msg);
//
//                        }else{
//                            Message start_msg = new Message("login","user is not authenticated" );
//                            Message.sendMsg(output,start_msg);
//                        }
//
//                        break;
//                    case "try":
//                        String token = msg.getJwt();
//
//                        //System.out.println(token);
//                        if (token == null){
//                            Message start_msg = new Message("login","user is not authenticated" );
//                            Message.sendMsg(output,start_msg);
//                            break;
//                        }
//
//                        try{
//                            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
//                            if( Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject().equals("john")){
//                                String str = body.toLowerCase();
//                                Message res =  game.guess(str);
//                                Message.sendMsg(output,res);
//                                if(res.getType().equals("finish")){
//                                    this.score = Integer.valueOf(res.getBody());
//                                    game = new GameHandler(score);
//                                    Message start_msg = new Message("update",game.getUnderline()+","+Integer.toString(game.getCounter())+","+Integer.toString(game.getScore()) );
//                                    Message.sendMsg(output,start_msg);
//                                }
//                            }else{
//                                Message start_msg = new Message("login","user is not authenticated" );
//                                Message.sendMsg(output,start_msg);
//                            }
//                        }catch (JwtException e){
//                            Message start_msg = new Message("login","user is not authenticated" );
//                            Message.sendMsg(output,start_msg);
//                        }
//
//
//                        break;
//                    case "disconnect":
//                        Message disconnect = new Message("disconnect","disconnected successfully" );
//                        Message.sendMsg(output,disconnect);
//                        close(input,output);
//                        break;
//                }
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }

    /**
     * If we get the disconnect message type we close the connection and kill the current thread.
     */
//    public void close(DataInputStream input, DataOutputStream output) throws IOException {
//        status = false;
//        input.close();
//        output.close();
//        System.out.println(Thread.currentThread()+" client quit");
//        clientSocket.close();
//        Thread.currentThread().stop();
//
//    }
}
