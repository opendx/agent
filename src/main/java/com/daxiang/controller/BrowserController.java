package com.daxiang.controller;

import com.daxiang.model.Response;
import com.daxiang.service.BrowserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jiangyitao.
 */
@RestController
@RequestMapping("/browser")
public class BrowserController {

    @Autowired
    private BrowserService browserService;

    @GetMapping("/list")
    public Response list() {
        return browserService.getAll();
    }

    @GetMapping("/{id}")
    public Response findById(@PathVariable String id) {
        return browserService.findById(id);
    }
}
