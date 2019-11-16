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
 * The Client class handles the messages received from the Server.
 * The Client has two threads, one for handling receiving messages from the server and another thread
 * for handling input from the user and sends it to the server.
 * The Client will not be stuck while waiting for a reply from the server, it can execute other commands in the mean while.
 *
 */

public class Connection implements Runnable{
    public String token = null;
    private Selector selector;
    private SocketChannel socketChannel;
    private static BufferedReader input = null;
//    public Scanner scan = new Scanner(System.in);
    private ByteBuffer bufferFromServer = ByteBuffer.allocateDirect(1024);
    private Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
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

//                        SocketChannel channel = (SocketChannel) key.channel();
//                        ByteBuffer buffer = ByteBuffer.allocate(1024);
//                        channel.read(buffer);
//                        String outcome = new String(buffer.array()).trim();
//                        System.out.println("Message was received from server:" + outcome);
////                        new ClientView(outcome);
////                        buffer.clear();
//                        key.interestOps(SelectionKey.OP_WRITE);
                        recvMessage(key);

                    } else if (key.isWritable()) {
//                        System.out.println("send message:" );

//                        String msg = scan.nextLine();
//                        if(!msg.equals("")){
//                            sendMsg(msg);
//                        }
//                        System.out.println(msg);
//                        if(!msg.equals("")){
//                            SocketChannel channel = (SocketChannel) key.channel();
//                            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
//                            channel.write(buffer);
//                            key.interestOps(SelectionKey.OP_READ);
//                        }
                        sendToServer(key);


                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //WritableByteChannel wout = Channels.newChannel(System.out);





//       /* try(Socket clientSocket =new Socket("127.0.0.1",3000)){
//
//            System.out.println("connect to 3000");
//            System.out.println("Welcome to Hangman, to start playing login first. Example: login john,123");
//
//            Thread cmdhandler = new Thread(new CmdHandler(clientSocket,client));
//            cmdhandler.setPriority(Thread.MAX_PRIORITY);
//            cmdhandler.start();
//
//
//            *//**
//             * While the client is connected we handle the logic for the client and update the client view
//             *//*
//            DataInputStream inStream = new DataInputStream(clientSocket.getInputStream());
//            while(true){
//                int datalen = inStream.readInt();
//                byte[] data = new byte[datalen];
//                inStream.readFully(data);
//                Message msg =(Message) Serialize.toObject(data);
//
//
//                String type = msg.getType();
//                String body = msg.getBody();
//
//                switch (type){
//                    case "update":
//                        new ClientView(msg);
//                        break;
//                    case "finish":
//                        new ClientView(msg);
//                        break;
//                    case "token":
//                        token = msg.getJwt();
//
//                        break;
//                    case "login":
//                        System.out.println("server: "+" "+body);
//                        System.out.println("please login");
//                        break;
//
//                    default:
//                        System.out.println("server: "+" "+body);
//
//                }
//            }
//
//
//        } catch (Exception e){
//            System.err.println("Exception :"+e);
//        }*/
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



//        String recvdString = extractMessageFromBuffer();
//        msgSplitter.appendRecvdString(recvdString);
//        while (msgSplitter.hasNext()) {
//            String msg = msgSplitter.nextMsg();
//            if (MessageSplitter.typeOf(msg) != MsgType.BROADCAST) {
//                throw new MessageException("Received corrupt message: " + msg);
//            }
//            notifyMsgReceived(MessageSplitter.bodyOf(msg));
//        }
    }

    public void sendMsg(String type, String msg) throws IOException {
//        StringJoiner joiner = new StringJoiner(Constants.MSG_TYPE_DELIMETER);
//        for (String part : parts) {
//            joiner.add(part);
//        }
//        String messageWithLengthHeader = MessageSplitter.prependLengthHeader(joiner.toString());
//        synchronized (messagesToSend) {
//            messagesToSend.add(ByteBuffer.wrap(messageWithLengthHeader.getBytes()));
//        }
//        timeToSend = true;
//        selector.wakeup();
        Serialize smsg = new Serialize(new Message(type, msg));
        byte[] bytes = smsg.getOut();
        synchronized (messagesToSend) {
            messagesToSend.add(ByteBuffer.wrap(bytes));
        }
        timeToSend = true;
        selector.wakeup();
    }

    private void sendToServer(SelectionKey key) throws IOException {
        ByteBuffer msg;
//        System.out.println(messagesToSend);
//        System.out.println("sent message to servers");
        synchronized (messagesToSend) {
            while (messagesToSend.size()>0) {
                msg = messagesToSend.poll();

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
//        System.out.println("connectable");
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
//        try {
//            InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
//        } catch (IOException e) {
//            key.cancel();
//        }
    }


}



