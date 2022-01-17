package com.manage.service;

import com.manage.dao.WorkNode;
import com.manage.dao.NodesCenter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.regex.Pattern;

@Service
public class DistributeWork {
    Vector<Future<String>> futures;
    ExecutorService es;
    ArrayList<WorkNode> availableNodes;
    Map<String, WorkNode> ipNodeMap;
    private String subtaskPrefix;
    private String md5Password;
    private int bitNum = 2;
    private String lastTaskPrefix;
//    private int count = 0;

    public DistributeWork() {
        this.futures = new Vector<>();
        this.ipNodeMap = new HashMap<>();
        this.availableNodes = new ArrayList<>();
    }

    public void setBitNum(int bitNum) {
        this.bitNum = bitNum;
    }

    public int getBitNum() {
        return bitNum;
    }

    public void setMap() {
        for(WorkNode workNode: NodesCenter.getWorkNodeQueue()) {
            ipNodeMap.put(workNode.getIP(), workNode);
        }
    }

    public void setSubtaskPrefix(String subtaskPrefix) {
        this.subtaskPrefix = subtaskPrefix;
    }

    public void setMd5Password(String md5Password) {
        this.md5Password = md5Password;
    }

    public String getMd5Password() {
        return md5Password;
    }

    public String distributeWork() {
//        if(count == 0) {
//            setNodesForTest();
//            count++;
//        }
        long startTime = System.currentTimeMillis();
        setAllNodesAvailable();
        setMap();
        while(!subtaskPrefix.equals(ServiceConfig.END_DISTRIBUTE)) {
            setAvailableNodes();
            if(noAvailableNodes()) return ServiceConfig.NO_AVAILABLE_NODES_MESSAGE;
            try {
                this.es = new ThreadPoolExecutor(3, 10, 60,
                        TimeUnit.SECONDS, new ArrayBlockingQueue<>(20));
                String s = distributeWorkOnce();
                if (s.equals(ServiceConfig.NO_AVAILABLE_NODES_MESSAGE)) return s;
                if (!s.equals(ServiceConfig.NOT_FOUND_MESSAGE)) {
                    s = ServiceConfig.FIND_PWD_MESSAGE + s;
                    long time = System.currentTimeMillis() - startTime;
                    // total time found a password.


                    return s;
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            } catch (ExecutionException ee) {
                ee.printStackTrace();
            }
        }
        long time = System.currentTimeMillis() - startTime;
        // total time not found a password.

        return ServiceConfig.NOT_FOUND_MESSAGE;
    }

    public boolean noAvailableNodes() {
        int count = 0;
        while(availableNodes.size() == 0) {
            if(count == 2) return true;
            count++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                System.out.println("Current thread is interrupted.");
            }
            setAvailableNodes();
        }
        return false;
    }

    public void setAvailableNodes() {
        availableNodes.clear();
        for(WorkNode workNode: NodesCenter.getWorkNodeQueue()) {
            if(workNode.isAvailable()) {
                availableNodes.add(workNode);
            }
        }
    }

    public void setAllNodesAvailable() {
        for(WorkNode workNode: NodesCenter.getWorkNodeQueue()) {
            workNode.setAvailable(true);
        }
    }

    public void setNodesForTest() {
        WorkNode workNode1 = new WorkNode("164.67.126.17", "10000");
//        WorkNode workNode2 = new WorkNode("128.197.11.45", "58001");
//        WorkNode workNode3 = new WorkNode("128.197.11.40", "58001");
        NodesCenter.getWorkNodeQueue().add(workNode1);
//        NodesCenter.getWorkNodeQueue().add(workNode2);
//        NodesCenter.getWorkNodeQueue().add(workNode3);
    }

    public String distributeWorkOnce() throws ExecutionException, InterruptedException {
        lastTaskPrefix = subtaskPrefix;
        for(WorkNode workNode: availableNodes) {
            System.out.println(availableNodes.size());
            if(subtaskPrefix.equals(ServiceConfig.END_DISTRIBUTE)) break;
            String message = subtaskPrefix + "," + md5Password;
            String ip = workNode.getIP();
            int port = Integer.parseInt(workNode.getPort());
            System.out.println("IP:" + ip + " Port: " + port + " Establishing socket...");
            SocketThread socketThread = new SocketThread(ip, port, message);
            if(socketThread.socketEstablishError) {
                System.out.println("WorkNode with ip " + socketThread.ip + "crashed");
                workNode.setAvailable(false);
                if(allWorkNodeFalse()) {
                    return ServiceConfig.NO_AVAILABLE_NODES_MESSAGE;
                }
            }
            Future<String> future = es.submit(socketThread);
            futures.add(future);
            modifyPrefix();
        }
        es.shutdown();
        availableNodes.clear();

        for(Future<String> future: futures) {
            String[] strings = future.get().split(",");
            String ret = strings[0];
            if(strings.length == 2) {
                String time = strings[1];
                if(ret.equals(ServiceConfig.NOT_FOUND_MESSAGE)) {
                    // not found time
                } else {
                    // found time
                }
            }
            if(!ret.equals(ServiceConfig.NOT_FOUND_MESSAGE) && !ret.equals("") && !testIp(ret)) {
                futures.clear();
                return ret;
            }
            if(testIp(ret)) {
                subtaskPrefix = lastTaskPrefix;
                ipNodeMap.get(ret).setAvailable(false);
            }
            if(ret.equals("")) subtaskPrefix = lastTaskPrefix;
        }
        futures.clear();
        return ServiceConfig.NOT_FOUND_MESSAGE;
    }

    boolean allWorkNodeFalse() {
        for(WorkNode workNode: NodesCenter.getWorkNodeQueue()) {
            if(workNode.isAvailable()) return false;
        }
        return true;
    }

    public boolean testIp(String str) {
        Pattern ip = Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        return ip.matcher(str).find();
    }

    private void modifyPrefix() {
        if(this.bitNum == 1) {
            modifyPrefix1();
        } else if(this.bitNum == 2) {
            modifyPrefix2();
        }
    }

    private void modifyPrefix2() {
        StringBuilder strBuilder = new StringBuilder(subtaskPrefix);
        char first = subtaskPrefix.charAt(0);
        char second = subtaskPrefix.charAt(1);
        if(second == 'z') {
            strBuilder.setCharAt(1, 'A');
        } else if (second == 'Z') {
            if(first == 'z') {
                strBuilder.setCharAt(0, 'A');
                strBuilder.setCharAt(1, 'a');
            } else if(first == 'Z') {
                subtaskPrefix = ServiceConfig.END_DISTRIBUTE;
                return;
            } else {
                strBuilder.setCharAt(0, (char)(first+1));
                strBuilder.setCharAt(1, 'a');
            }
        } else {
            strBuilder.setCharAt(1, (char) (second+1));
        }
        subtaskPrefix = strBuilder.toString();
    }

    private void modifyPrefix1() {
        StringBuilder stringBuilder = new StringBuilder(subtaskPrefix);
        char c = subtaskPrefix.charAt(0);
        if(c == 'z') {
            stringBuilder.setCharAt(0, 'A');
        } else if(c == 'Z') {
            subtaskPrefix = ServiceConfig.END_DISTRIBUTE;
            return;
        } else {
            stringBuilder.setCharAt(0, (char)(c+1));
        }
        subtaskPrefix = stringBuilder.toString();
    }
}
