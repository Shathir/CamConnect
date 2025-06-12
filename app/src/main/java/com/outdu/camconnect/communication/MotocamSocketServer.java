package com.outdu.camconnect.communication;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by sr on 11/9/19.
 */
public class MotocamSocketServer {


    private ServerSocket motocam_ss;

    public void init() throws IOException {
        System.out.println("init");
        motocam_ss = new ServerSocket(MotocamAPIHelperWrapper.MOTOCAM_SERVER_SOCKET_PORT);
    }

    public void startServer() {
        if(motocam_ss==null) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket s;
                while (true) {
                    try {
                        s=motocam_ss.accept();
                        System.out.println("accept client");
                        DataInputStream dis=new DataInputStream(s.getInputStream());
                        byte[] messageByte = new byte[1000];
                        int size = dis.read(messageByte);
                        String  str=new String(messageByte, 0, size);
                        System.out.println("message= "+str);
//                        DataOutputStream dos=new DataOutputStream(s.getOutputStream());
//                        dos.writeChars("OK");
                        dis.close();
//                        dos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }

                }
            }
        }).start();
    }

    public void destroy() throws IOException {
        if(motocam_ss!=null)
            motocam_ss.close();
        System.out.println("destroy");
    }
}
