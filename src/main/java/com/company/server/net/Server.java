package com.company.server.net;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The Server class handles the logic of creating a server socket and connect to the client socket.
 */

public class Server {

    Selector selector;
    boolean timeToSend = false;
    private LinkedBlockingQueue<SelectionKey> sendKey = new LinkedBlockingQueue<>();
    public static void main(String[] args){
        try{


            Server s = new Server();
            s.server();


        }catch(IOException e){

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void server() throws IOException, InterruptedException {
        //ServerSocket serverSocket =new ServerSocket (3001);
        try{
            selector = Selector.open();
            ServerSocketChannel schannel = ServerSocketChannel.open();
            ServerSocket socket = schannel.socket();
            schannel.configureBlocking(false);
            socket.bind(new InetSocketAddress(3001));
            schannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("server start at 3001");
            while (true) {
            if (timeToSend) {
                SelectionKey _k = sendKey.poll();
                if(_k.isValid()){
                    _k.interestOps(SelectionKey.OP_WRITE);
                }

//                writeOperationForAllActiveClients(sendKey.poll());
//                System.out.println(sendKey.poll().interestOps(SelectionKey.OP_WRITE));
            }


                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {

                    SelectionKey key = keys.next();
//                    System.out.println("kkkkkkk"+key);
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }


                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel channel = serverSocketChannel.accept();

                        channel.configureBlocking(false);
                        ChannelHandler handler = new ChannelHandler(this, channel);
                        SelectionKey clientKey =channel.register(selector, SelectionKey.OP_READ, handler);
                        handler.skey = clientKey;
                        channel.setOption(StandardSocketOptions.SO_LINGER, 5000);

                    } else if (key.isReadable()) {
//                    SocketChannel channel = (SocketChannel) key.channel();
//                    ByteBuffer buffer = (ByteBuffer) key.attachment();
//                    channel.read(buffer);
////                    key.interestOps(SelectionKey.OP_WRITE);
//                    String outcome = new String(buffer.array()).trim();
//                    System.out.println("Message was received from c:" + outcome);
////                    buffer.clear();
//                    key.interestOps(SelectionKey.OP_WRITE);
//                        System.out.println("i'm readinng");
                        ChannelHandler handler = (ChannelHandler) key.attachment();
                        try{
                            handler.readMsg();
//                            System.out.println("end of reading");
//                            while (timeToSend){
//                                System.out.println("time to send");
//                                key.interestOps(SelectionKey.OP_WRITE);
//                                System.out.println("is writeable,"+ key.isWritable());
//                                timeToSend = false;
//                            }

                        }catch (Exception e) {
//                            System.err.println("loss connection");
                        }
//                        System.out.println("end of reading");
                    } else if (key.isWritable()) {
//                    Thread.currentThread().sleep(10000);

//                    SocketChannel channel = (SocketChannel) key.channel();
//                    ByteBuffer buffer = (ByteBuffer) key.attachment();
////                    System.out.println("wirtable "+buffer.toString());
//                    buffer.flip();
//                    channel.write(buffer);
//                    if(buffer.hasRemaining()) {
//
//                        buffer.compact();
//                    } else {
//
//                        buffer.clear();
//                        key.interestOps(SelectionKey.OP_READ);
////                        System.out.println(2);
//                    }
//                    buffer.clear();
//                        System.out.println("i'm writing");
                        ChannelHandler handler = (ChannelHandler) key.attachment();
//                    Thread.currentThread().sleep(10000);
                        handler.sendMessage();
                        key.interestOps(SelectionKey.OP_READ);
                        timeToSend=false;
//                        System.out.println("end of writing");

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Server failure.");
        }

    }





    public void wakeupSelector(SelectionKey skey){
//        selector.selectedKeys().add(key);

//        skey.interestOps(SelectionKey.OP_WRITE);
//        System.out.println(skey.isWritable());
        sendKey.add(skey);
        selector.wakeup();

    }
//    public void addKey(SelectionKey skey){
//        sendKey.add(skey);
//    }

//    private void writeOperationForAllActiveClients(SelectionKey akey) {
//        System.out.println("akey "+akey);
//        for (SelectionKey key : selector.keys()) {
//
//            if (key.channel() instanceof SocketChannel && key.isValid()) {
//                System.out.println(key);
//                key.interestOps(SelectionKey.OP_WRITE);
//            }
//        }
//    }




}



