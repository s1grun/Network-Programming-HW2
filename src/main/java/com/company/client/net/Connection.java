package com.company.client.net;

import com.company.client.view.ClientView;
import com.company.common.Message;
import com.company.common.Serialize;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Queue;
//import java.util.StringJoiner;

/**
 * The Connection class
 *
 *
 *
 *
 */

public class Connection implements Runnable{
    public String token = null;
    private Selector selector;
    private SocketChannel socketChannel;
    private static BufferedReader input = null;
//    public Scanner scan = new Scanner(System.in);
    private ByteBuffer bufferFromServer = ByteBuffer.allocateDirect(1024);
    private Queue<ByteBuffer> messagesToSendQueue = new ArrayDeque<>();
    private Queue<Message> recvQueue = new ArrayDeque<Message>();
    private boolean timeToSend = false;


//    public static void main(String[] args)
//    {
//
//        Connection client = new Connection();
//        client.startClient(client);
//
//
//    }

    public String getToken(){
        return token;
    }

    /**
     * Start a new client socket to receive message
     */
    @Override
    public void run(){

        try {
            init();

            while (true) {
                if (timeToSend) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }

//                System.out.println(timeToSend);
                selector.select();

                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if(!key.isValid()){
                        continue;
                    }
                    if (key.isConnectable()) {
                        fullyConnected(key);
                        System.out.println("connect");
                    } else if (key.isReadable()) {

                        recvMessage(key);

                    } else if (key.isWritable()) {

                        sendToServer(key);


                    }
                }
            }
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("disconnect");
        }

    }

    private void recvMessage(SelectionKey key) throws IOException {
        System.out.println("recvMessage");
        this.bufferFromServer.clear();
        int numOfReadBytes = socketChannel.read(bufferFromServer);
        if (numOfReadBytes == -1) {
            throw new IOException("no read buffer");
        }

        bufferFromServer.flip();
        byte[] bytes = new byte[bufferFromServer.remaining()];
        bufferFromServer.get(bytes);
//        String msg =  new String(bytes);

        recvQueue.add((Message) Serialize.toObject(bytes));
        while (recvQueue.size()>0){
            Message msg2 = recvQueue.poll();
//            System.out.println("receive: "+msg2);

            new ClientView(msg2);


        }

    }

    public void sendMsg(String type, String msg) throws IOException {
        Serialize smsg = new Serialize(new Message(type, msg));
        byte[] bytes = smsg.getOut();
        synchronized (messagesToSendQueue) {
            messagesToSendQueue.add(ByteBuffer.wrap(bytes));
        }
        timeToSend = true;
        selector.wakeup();
    }

    private void sendToServer(SelectionKey key) throws IOException {
        ByteBuffer msg;
        synchronized (messagesToSendQueue) {
            while (messagesToSendQueue.size()>0) {
                msg = messagesToSendQueue.poll();

                socketChannel.write(msg);
                if (msg.hasRemaining()) {
                    System.out.println("has remaining");
                    return;
                }

            }
            key.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        }
    }

    private void init() throws IOException {
        selector = Selector.open();

        this.socketChannel = SocketChannel.open();
        this.socketChannel.configureBlocking(false);
        this.socketChannel.connect(new InetSocketAddress(InetAddress.getByName("localhost"),3001));
        this.socketChannel.register(selector, SelectionKey.OP_CONNECT);
        //this.connected = true;
    }

    private void fullyConnected(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }

}



