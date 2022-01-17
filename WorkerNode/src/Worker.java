import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Worker {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(58001);

        while(true) {
            try (
                    Socket socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
            ) {
                String input;
                while ((input = in.readLine()) != null) {
                    long recvTime = System.currentTimeMillis();
                    String[] task = input.split(",");
                    int prefixSize = task[0].length();
                    System.out.println("Received task range from " + task[0] + (prefixSize == 1? "aaaa ":"aaa ") + " to "
                            + task[0] + (prefixSize == 1? "ZZZZ ":"ZZZ "));
                    String res = crack(task[0], task[1],recvTime);
//                    out.write(res);
                    out.println(res);
                    if (!res.substring(0,3).equals("101")) {
                        System.out.println("Password found");
                    }else{
                        System.out.println("No matching password in this range");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String crack(String start, String hash,long recvTime){

        StringBuilder sb = new StringBuilder();
        for(int i='a';i<='z';i++){
            sb.append((char) i);
        }
        for(int i='A';i<='Z';i++){
            sb.append((char) i);
        }
        String allChars = sb.toString();

        StringBuilder password;
        password = new StringBuilder(start);
        int loopTimes = 5 - password.length();

        if(loopTimes == 3) {
            if (loop3Times(hash, allChars, password)){
                long time = System.currentTimeMillis()-recvTime;
                password.append(",");
                password.append(time);
                return password.toString();
            }
        } else if(loopTimes == 4) {
            for (int j = 0; j < 52; j++) {
                password.append(allChars.charAt(j));
                if (loop3Times(hash, allChars, password)){
                    long time = System.currentTimeMillis()-recvTime;
                    password.append(",");
                    password.append(time);
                    return password.toString();
                }
                password.deleteCharAt(password.length() - 1);
            }
        }
        long time = System.currentTimeMillis()-recvTime;
        return "101 Not Found,"+time;

    }

    private static boolean loop3Times(String hash, String allChars, StringBuilder password) {
        for (int k = 0; k < 52; k++) {
            password.append(allChars.charAt(k));
            for (int l = 0; l < 52; l++) {
                password.append(allChars.charAt(l));
                for(int h  = 0; h < 52; h++) {
                    password.append(allChars.charAt(h));
                    if (toMD5(password.toString()).equals(hash)) {
                        return true;
                    }
                    password.deleteCharAt(password.length() - 1);
                }
                password.deleteCharAt(password.length() - 1);
            }
            password.deleteCharAt(password.length() - 1);
        }
        return false;
    }

    public static String toMD5(String plainText){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte[] digest = md.digest();
            String md5 = DatatypeConverter.printHexBinary(digest).toLowerCase();
            return md5;
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
