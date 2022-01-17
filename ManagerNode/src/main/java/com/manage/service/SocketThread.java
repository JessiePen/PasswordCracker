package com.manage.service;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.Callable;

public class SocketThread implements Callable<String> {
    String ip;
    Socket socket;
    String dataSend;
    boolean socketEstablishError = false;
    boolean socketCrashError = false;

    public SocketThread(String ip, int portNum, String dataSend) {
        this.ip = ip;
        this.dataSend = dataSend;
        try {
            this.socket = new Socket(ip, portNum);
        } catch (Exception e) {
            System.out.println("Socket establish time out");
            e.printStackTrace();
            socketEstablishError = true;
        }
    }

    public void sendData() {
        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.println(this.dataSend);
            printWriter.flush();
        } catch (IOException e) {
            System.out.println("Socket Crash, sending data error");
            e.printStackTrace();
            socketCrashError = true;
        }

    }

    public String receiveData() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String reply;
            reply = in.readLine();
            long startTime = System.currentTimeMillis();
            while (reply == null) {
                reply = in.readLine();
                long endTime = System.currentTimeMillis();
                if((endTime - startTime)/1000 > 15) {
                    System.out.println("Socket Crash, receiving data error");
                    reply = ServiceConfig.SOCKET_ERROR_MESSAGE2;
                    socketCrashError = true;
                }
            }
            in.close();
            return reply;
        } catch (IOException e) {
            System.out.println("Socket Crash, receiving data error");
            socketCrashError = true;
        }
        return "";
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String call() {
        if(socket != null) {
            sendData();
            System.out.println("IP: " + this.ip + " Message sent successfully...");

            String subtaskRes = receiveData();
            String subtaskStr = subtaskRes.split(",")[0];
            if(subtaskStr.length() != 5 && !subtaskStr.equals(ServiceConfig.NOT_FOUND_MESSAGE)) {
                subtaskRes = this.ip;
            }
            System.out.println("IP: " + this.ip + " Received message " + subtaskRes);
            if (subtaskRes.equals(ServiceConfig.SOCKET_ERROR_MESSAGE2)) subtaskRes = this.ip;
            closeSocket();
            System.out.println("IP: " + this.ip + " Socket closed successfully...");
            return subtaskRes;
        }
        return "";
    }

    public String getIp() {
        return ip;
    }

}
