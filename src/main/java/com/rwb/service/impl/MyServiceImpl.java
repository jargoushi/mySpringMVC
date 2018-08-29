package com.rwb.service.impl;

import com.rwb.annotation.Service;
import com.rwb.service.MyService;

@Service("myService")
public class MyServiceImpl implements MyService {


    public String query(String name, Integer age) {
        return "name :" + name + "=============" + "age:" + age;
    }
}
