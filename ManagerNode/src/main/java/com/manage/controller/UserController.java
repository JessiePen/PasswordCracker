package com.manage.controller;

import com.manage.dao.NodesCenter;
import com.manage.service.DistributeWork;
import com.manage.service.ServiceConfig;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class UserController {
    private final NodesCenter nodesCenter;
    private int bitNum;

    public UserController(NodesCenter nodesCenter) {
        this.nodesCenter = nodesCenter;
        bitNum = 2;
    }

    @PostMapping("/user/crack")
    @ResponseBody
    public UserResponse crackPassword(RequestBody body) {
        DistributeWork distributeWork = new DistributeWork();
        String md5Password = body.getPasswordMd5();
        distributeWork.setBitNum(bitNum);
        distributeWork.setMd5Password(md5Password);
        distributeWork.setSubtaskPrefix(ServiceConfig.setInitialPredix(distributeWork.getBitNum()));
        System.out.println("md5Pwd: " + distributeWork.getMd5Password());
        String res = distributeWork.distributeWork();
        String[] parseStr = ServiceConfig.parse(res);
        UserResponse responseBody = new UserResponse(parseStr[0], parseStr[1], parseStr[2]);
        return responseBody;
    }

    @PostMapping("/set")
    @ResponseBody
    public void setPrefixBit(@RequestParam("bitNum")int bitNum) {
        this.bitNum = bitNum;
        System.out.println("Set bit number " + bitNum + " successfully");
    }

    class UserResponse {
        private String code;
        private String msg;
        private String data;

        public UserResponse(String code, String msg, String data) {
            this.code = code;
            this.msg = msg;
            this.data = data;
        }

        public String getCode() {
            return code;
        }

        public String getData() {
            return data;
        }

        public String getMsg() {
            return msg;
        }
    }
}
