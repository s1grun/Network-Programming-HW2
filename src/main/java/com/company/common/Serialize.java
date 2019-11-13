package com.company.common;

import java.io.*;


/**
 * The Serialize class handles the logic of taking a Message(object) and convert it to a byte array.
 */
public class Serialize {



    private byte[] out;

    public String getOutString() {
        return outString;
    }

    private String outString;
    public ObjectOutputStream objectOutputStream;

    private int length;

    /**
     * Here we convert the message to a byte array and get the length of the array
     * @param msg
     * @throws IOException
     */
    public Serialize(Message msg) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream() ;
        this.objectOutputStream = new ObjectOutputStream(byteOutputStream) ;
        this.objectOutputStream.writeObject(msg);



        objectOutputStream.flush();
        objectOutputStream.close();

        this.length = byteOutputStream.toByteArray().length;
        this.out = byteOutputStream.toByteArray();
        this.outString = byteOutputStream.toString() ;
//        System.out.printf("len:  "+ length);
    }

    public int getLength() {
        return length;
    }
    public byte[] getOut() {
        return out;
    }

//    public static boolean checkLength(String msg, int len) throws IOException {
//        Serialize serialized = new Serialize(msg);
//        if(serialized.getLength()==len){
//            return true;
//        }else {
//            return false;
//        }
//
//    }

    /**
     * Here we convert the byte array into an object so it is readable
     * @param bytes
     * @return
     */
    public static Object toObject (byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
            ObjectInputStream ois = new ObjectInputStream (bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return obj;
    }
}
