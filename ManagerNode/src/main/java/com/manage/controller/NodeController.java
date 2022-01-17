package com.manage.controller;

import com.manage.dao.NodesCenter;
import com.manage.dao.WorkNode;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

// code + msg
@RestController
public class NodeController {
    private final NodesCenter nodesCenter;

    public NodeController(NodesCenter nodesCenter) {
        this.nodesCenter = nodesCenter;
    }

    public List<WorkNode> getWorkNodeList() {
        List<WorkNode> workNodeList = nodesCenter.traverse();
        return workNodeList;
    }

    private boolean checkFormat(WorkNode body) {
        /**
         * IPV4 address validation regex pattern from:
         * https://stackoverflow.com/questions/5284147/validating-ipv4-addresses-with-regexp
         * Port number validation regex pattern from:
         * https://web.archive.org/web/20151029043426/http://utilitymill.com/utility/Regex_For_Range
         */
        Pattern ip = Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        Pattern port = Pattern.compile("^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$");
        return !ip.matcher(body.getIP()).find() || !port.matcher(body.getPort()).find();
    }

    @GetMapping("/admin/all")
    @ResponseBody
    public NodeResponse getAllWorkNodes() {
        // ajax
//        return "{
//          code： 0,
//          Nodes: [{ip: xxxx, port:xxxx}, {...}]]
//        }";
        //
        return new NodeResponse(0, "ok", getWorkNodeList());
    }

    @PostMapping("admin/node")
    @ResponseBody
    public NodeResponse addWorkNode(WorkNode body) {
        // check input format
        if (checkFormat(body)) {
            String invalidFormat = "input data is invalid for ipv4 address or port number";
            return new NodeResponse(2, invalidFormat, getWorkNodeList());
        }
        int code = 0;
        String msg = "ok";
        try {
            nodesCenter.add(body);
        } catch (InterruptedException e) {
            e.printStackTrace();
            code = 1;   // the thread is interrupted
            msg = "current thread is interrupted";
        }
        return new NodeResponse(code, msg, getWorkNodeList());
    }

    @DeleteMapping("/admin/node")
    @ResponseBody
    public NodeResponse deleteWorkNode(WorkNode body) {
        // check input format
        if (checkFormat(body)) {
            String invalidFormat = "input data is invalid for ipv4 address or port number";
            return new NodeResponse(2, invalidFormat, getWorkNodeList());
        }
        int code = 0;
        String msg = "ok";
        if (!nodesCenter.remove(body)) {
            code = 3; // failed to delete the node
            msg = "input node was not found in the queue";
        }
        return new NodeResponse(code, msg, getWorkNodeList());
    }

    class NodeResponse {
        private int code;
        private String msg;
        private List<WorkNode> data;

        public NodeResponse(int code, String msg, List<WorkNode> nodesList) {
            this.code = code;
            this.msg = msg;
            this.data = nodesList;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {return msg;}

        public List<WorkNode> getData() {
            return data;
        }
    }
}
