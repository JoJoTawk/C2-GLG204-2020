package com.example.gotexample.controller;

import com.example.gotexample.Modules.GOTHeroes;
import com.example.gotexample.service.GOTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path="/GOT")
public class GotController {

    @Value("${got.app.var}")
    private String someProperty;

    @Autowired
    private GOTService gotService;

    @RequestMapping(path="/heroes", method = RequestMethod.GET)
    public @ResponseBody List<GOTHeroes> heroes() {

        System.out.println("This is a new list to check if class is being accessed or not");
        List<GOTHeroes> listOfHeroes = gotService.getAllHeroes();
        return listOfHeroes;
    }

    @RequestMapping(path="/hero/{id}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody GOTHeroes hero(@PathVariable int id, @RequestParam String params) {
        System.out.println("Hero Id [" + id + "] , params [" + params + "]");
        System.out.println("Custom property [" + someProperty + "]");
        return  new GOTHeroes();
    }

}
