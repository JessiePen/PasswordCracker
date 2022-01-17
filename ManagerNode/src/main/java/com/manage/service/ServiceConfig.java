package com.manage.service;

public class ServiceConfig {
    public static final String NOT_FOUND_MESSAGE = "101 Not Found";
    public static final String NO_AVAILABLE_NODES_MESSAGE = "102 No Available Nodes";
    public static final String SOCKET_ERROR_MESSAGE1 = "103 Socket Establishment Error";
    public static final String SOCKET_ERROR_MESSAGE2 = "104 Socket Crash ERROR. ";
    public static final String FIND_PWD_MESSAGE = "0 OK ";
    public static final String END_DISTRIBUTE = "End";


    public static String setInitialPredix(int bitNum) {
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < bitNum; i++) {
            ret.append('a');
        }
        return ret.toString();
    }

    public static String[] parse(String res) {
        String code = "", message = "", data = "";
        int i, j;
        for(i = 0; i < res.length(); i++) {
            if(res.charAt(i) == ' ') {
                code = res.substring(0, i);
                break;
            }
        }
        if(code.equals("0")) {
            for (j = res.length() - 1; j >= 0; j--) {
                if (res.charAt(j) == ' ') {
                    data = res.substring(j+1);
                    message = res.substring(i+1, j);
                    break;
                }
            }
        } else {
            message = res.substring(i+1);
        }

        return new String[]{code, message, data};
    }
}
