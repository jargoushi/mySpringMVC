package com.rwb.controller;

import com.rwb.annotation.Autowired;
import com.rwb.annotation.Controller;
import com.rwb.annotation.RequestMapping;
import com.rwb.annotation.RequestParam;
import com.rwb.service.MyService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@RequestMapping("/rwb")
public class MyController {

    @Autowired("myService")
    private MyService myService;

    @RequestMapping("/query")
    public void showIndex(HttpServletRequest request, HttpServletResponse response,
                          @RequestParam("name") String name, @RequestParam("age") String age) {
        String result = myService.query(name, Integer.parseInt(age));
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(result);
        } catch (IOException e) {
            System.out.println(e);
        }
    }


}
