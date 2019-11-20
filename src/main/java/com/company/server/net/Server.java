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

    /**
     * In the server function we create the selector object and
     * ServerSocketChannel instance, which listens to the new connection request from the client.
     * We configure the channel to a non blocking state. Then we bound the channel to a port number so
     * the client socket can communicate to it.
     * Register a channel with the accept operation.
     * @throws IOException
     * @throws InterruptedException
     */
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

                    /**
                     * When we establish a client connection, we create a channel and attach the handler
                     * with the channel. The key is used for communication, to decide if the operation for the channel.
                     */
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel channel = serverSocketChannel.accept();

                        channel.configureBlocking(false);
                        ChannelHandler handler = new ChannelHandler(this, channel);
                        SelectionKey clientKey =channel.register(selector, SelectionKey.OP_READ, handler);
                        handler.skey = clientKey;
                        //channel.setOption(StandardSocketOptions.SO_LINGER, 5000);

                    } else if (key.isReadable()) {
                        ChannelHandler handler = (ChannelHandler) key.attachment();
                        try{
                            handler.readMsg();
//                            }

                        }catch (Exception e) {
//                            System.err.println("loss connection");
                        }
                    } else if (key.isWritable()) {
                        ChannelHandler handler = (ChannelHandler) key.attachment();
                        handler.sendMessage();
                        key.interestOps(SelectionKey.OP_READ);
                        timeToSend=false;

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Server failure.");
        }

    }


    /**
     * The wakeupSelector function - For read operation we need to wake up the selector
     * @param skey
     */

    public void wakeupSelector(SelectionKey skey){
        sendKey.add(skey);
        selector.wakeup();

    }


}



