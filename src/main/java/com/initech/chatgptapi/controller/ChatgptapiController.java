package com.initech.chatgptapi.controller;

import com.initech.chatgptapi.service.ChatgptapiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/chatgpt-ui/v1")
public class ChatgptapiController {

    @Autowired
    private ChatgptapiService service;

    @ResponseBody
    @PostMapping("/send")
    public Map send(@RequestBody Map param) throws Exception{
        return service.sendMessage(param);
    }


}
