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

/**
 * The Server class handles the logic of creating a server socket and connect to the client socket.
 */

public class Server {

    Selector selector;
    boolean timeToSend = false;
    private final Queue<SelectionKey> sendKey = new ArrayDeque<>();
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
//            if (!sendKey.isEmpty()) {
//
////                sendKey.poll().interestOps(SelectionKey.OP_WRITE);
//
////                System.out.println(sendKey.poll().interestOps(SelectionKey.OP_WRITE));
//            }


                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel channel = serverSocketChannel.accept();

                        channel.configureBlocking(false);
                        ChannelHandler handler = new ChannelHandler(this, channel, key);
                        channel.register(selector, SelectionKey.OP_WRITE, handler);
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
//                        System.out.println("readable");
                        ChannelHandler handler = (ChannelHandler) key.attachment();
                        try{
                            handler.readMsg();
                            key.interestOps(SelectionKey.OP_WRITE);
                            selector.wakeup();
                        }catch (Exception e) {
//                            System.err.println("loss connection");
                        }

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
                        ChannelHandler handler = (ChannelHandler) key.attachment();
//                    Thread.currentThread().sleep(10000);
                        handler.sendMessage();
                        key.interestOps(SelectionKey.OP_READ);

                    }
                }

            }
        } catch (Exception e) {
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






}



