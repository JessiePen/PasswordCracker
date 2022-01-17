package com.manage.dao;

import java.util.Objects;

public class WorkNode {
    private final String ip;
    private final String port;
    private boolean available;

    public WorkNode(String ip, String port) {
        this.ip = ip;
        this.port = port;
        this.available = true;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkNode workNode = (WorkNode) o;
        return ip.equals(workNode.ip) && port.equals(workNode.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port, available);
    }

    public String getIP() {
        return ip;
    }

    public String getPort() {
        return port;
    }
}
