package com.company.server.net;

import com.company.common.Message;
import com.company.common.Serialize;
import com.company.server.model.GameHandler;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;

/**
 * The ChannelHandler class handles the game for each client.
 * For each client we only one channelHandler, we can start a new game in one handler.
 */

public class ChannelHandler implements Runnable {
    private Server server;
    public SocketChannel channel;
    private int score;
    GameHandler game = null;
    //    Key key;
    SelectionKey skey;
    boolean status = true;
    private ByteBuffer clientMessageBuffer = ByteBuffer.allocateDirect(1024);
    private Queue<Message> readMessageQueue = new ArrayDeque<>();
    private Queue<byte[]> sendMessageQueue = new ArrayDeque<byte[]>();
    private Queue<Message> blockTaskQueue = new ArrayDeque<>();

    public ChannelHandler(Server server, SocketChannel channel) {
        this.server = server;
        this.channel = channel;
//        this.skey = skey;
    }

    public void readMsg() throws IOException, InterruptedException {
        clientMessageBuffer.clear();
        int numOfReadBytes = channel.read(clientMessageBuffer);
        if (numOfReadBytes == -1)
            throw new IOException("Client has closed connection.");

        clientMessageBuffer.flip();
        byte[] bytes = new byte[clientMessageBuffer.remaining()];
        clientMessageBuffer.get(bytes);

//        synchronized(readQueue){
        readMessageQueue.add((Message) Serialize.toObject(bytes));
//        }


//        ForkJoinPool.commonPool().execute(this);
//        new Thread(this).start();

//        System.out.println("read message over");
//        ForkJoinPool.commonPool().execute(this);
//        new Thread(this).start();
        while (readMessageQueue.size() > 0) {
//            System.out.println(readQueue);
            Message msg = readMessageQueue.poll();
            String type = msg.getType();
            String body = msg.getBody();
            System.out.println("From " + skey + " " + msg.getType() + ' ' + msg.getBody());

            switch (type) {
                case "try":
//                    System.out.println(game);
//                    System.out.println(game.equals(null));
                    if (game == null) {
                        Message start_msg = new Message("err", "use 'start' command to start the game!");
                        writeSendQueue(start_msg);
                        System.out.println("send err");
                        break;
                    }
                    String str = body.toLowerCase();
                    Message res = game.guess(str);
                    writeSendQueue(res);
                    if (res.getType().equals("finish")) {
                        synchronized (blockTaskQueue) {
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
                    synchronized (blockTaskQueue) {
                        blockTaskQueue.add(msg);
                        ForkJoinPool.commonPool().execute(this);
                    }

                    break;
                case "disconnect":
                    disconnect();
                    break;
                default:
                    System.out.println("no match type " + type + " " + msg.getBody());
                    break;
            }


        }


    }

    @Override
    public void run() {
        synchronized (blockTaskQueue) {
            while (blockTaskQueue.size() > 0) {
                Message msg = blockTaskQueue.poll();
                String type = msg.getType();
//            String body = msg.getBody();

                if (type.equals("start")) {
//                    try {
//                        Thread.currentThread().sleep(10000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    game = new GameHandler(score);
                    Message start_msg = new Message("update", game.getUnderline() + "," + Integer.toString(game.getCounter()) + "," + Integer.toString(game.getScore()));
                    try {
                        writeSendQueue(start_msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (type.equals("finish")) {
                    this.score = Integer.valueOf(msg.getBody());
                    game = new GameHandler(score);
                    Message start_msg = new Message("update", game.getUnderline() + "," + Integer.toString(game.getCounter()) + "," + Integer.toString(game.getScore()));
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

        synchronized (sendMessageQueue) {
            sendMessageQueue.add(bytes);
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
        synchronized (sendMessageQueue) {
            while (sendMessageQueue.size() > 0) {
                ByteBuffer message = ByteBuffer.wrap(sendMessageQueue.poll());
                channel.write(message);
            }
        }
    }

    public void disconnect() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}