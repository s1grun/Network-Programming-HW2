package com.company.server.net;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * The Server class handles the logic of creating a server socket and connect to the client socket.
 */

public class Server {
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
        Selector selector = Selector.open();
        ServerSocketChannel schannel = ServerSocketChannel.open();
        ServerSocket socket = schannel.socket();
        schannel.configureBlocking(false);
        socket.bind(new InetSocketAddress(3001));
        schannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("server start at 3001");
        while (true) {
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
//                    ChannelHandler handler = new ChannelHandler(this, channel);
                    channel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));

                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    channel.read(buffer);
//                    key.interestOps(SelectionKey.OP_WRITE);
                    String outcome = new String(buffer.array()).trim();
                    System.out.println("Message was received from c:" + outcome);
//                    buffer.clear();
                    key.interestOps(SelectionKey.OP_WRITE);

                } else if (key.isWritable()) {
                    Thread.currentThread().sleep(10000);
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    buffer.flip();
                    channel.write(buffer);
                    if(buffer.hasRemaining()) {
                        buffer.compact();
                    } else {
                        buffer.clear();
                        key.interestOps(SelectionKey.OP_READ);
                    }
                    buffer.clear();
                }
            }
            //Socket clientSocket = serverSocket.accept();
            //System.out.println("client join:"+ clientSocket);
//            clientSocket.setSoLinger(true, LINGER_TIME);

            /**
             * When a new client is accepted we create a new thread which is then handled by the ChannelHandler
             */
            //Thread handler = new Thread(new ChannelHandler(clientSocket));
           // handler.setPriority(Thread.MAX_PRIORITY);
           // handler.start();
        }
    }









}



