//package com.outdu.camconnect.communication;
//
//import android.util.Log;
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.net.Socket;
//
///**
// * Created by sr on 19/8/19.
// */
//public class MotocamSocketClient {
//
//    private static final String TAG = "MotocamSocketClient";
//    private Socket motocamClientSocket;
//    private DataOutputStream out;
//    private DataInputStream in;
//
//    private static final int sotimeout = 10000;
//
//    public boolean checkDevice(String ipAddress) {
//        Log.i(TAG, "checkDevice");
//        Socket s = new Socket();
//        try {
//            s.connect(new InetSocketAddress(ipAddress,
//                    MotocamAPIHelperWrapper.MOTOCAM_CLIENT_SOCKET_PORT), sotimeout);
//            s.close();
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public void init() throws IOException {
//        Log.i(TAG, "init "+MotocamAPIHelperWrapper.DEVICE_IP_ADDRESS);
//        motocamClientSocket = new Socket();
//        motocamClientSocket.connect(new InetSocketAddress(MotocamAPIHelperWrapper.DEVICE_IP_ADDRESS,
//                MotocamAPIHelperWrapper.MOTOCAM_CLIENT_SOCKET_PORT), sotimeout);
//        out = new DataOutputStream(motocamClientSocket.getOutputStream());
//        in = new DataInputStream(motocamClientSocket.getInputStream());
//        Log.i(TAG, "init done");
//    }
//
//    private byte[] convert(int[] intarray) {
//        byte[] bytearray = new byte[intarray.length];
//        for(int i=0;i<intarray.length;i++) {
//            bytearray[i]= (byte) intarray[i];
//        }
//        return bytearray;
//    }
//
//    private void convert(byte[] bytearray, int[] intarray) {
//        for(int i=0;i<bytearray.length;i++) {
//            intarray[i]= bytearray[i] & 0xFF;
//        }
//    }
//
//    private static byte calcCRC(byte[] reqbytearray) {
//        int sum=0;
//        for(int i=0;i<reqbytearray.length-1;i++) {
//            sum+=reqbytearray[i];
//        }
//        int crc_byte = sum ^ 255;
//        byte crc = (byte) (crc_byte + 1);
//        return crc;
//    }
//
//    private static boolean validateCRC(byte[] resbytearray, int length) {
//        int sum=0;
//        for(int i=0;i<length;i++) {
//            sum+=resbytearray[i];
//        }
//        byte sum_byte = (byte) sum;
//        return (sum_byte == 0);
//    }
//
//    public int sendCmd(int[] req, int[] res) throws Exception {
//        byte reqbytes_bytearray[] = convert(req);
//        reqbytes_bytearray[req.length-1] = calcCRC(reqbytes_bytearray);
//        out.write(reqbytes_bytearray, 0, reqbytes_bytearray.length);
//        byte[] bytearray=new byte[res.length];
//        int ret = in.read(bytearray);
//        if(ret <= 0) {
//            throw new Exception("Invalid cmd response");
//        }
//        if(!validateCRC(bytearray, ret)) {
//            throw new Exception("Invalid CRC in response");
//        }
//        convert(bytearray, res);
//        return ret;
//    }
//
//    public void destroy() throws IOException {
//        out.close();
//        in.close();
//        motocamClientSocket.close();
//    }
//
//}