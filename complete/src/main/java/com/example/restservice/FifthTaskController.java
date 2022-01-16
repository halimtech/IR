package com.example.restservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.example.restservice.FifthTask.*;

@RestController
public class FifthTaskController {

    static String path = System.getProperty("user.dir");

    @GetMapping("/search")
    public ArrayList<ArrayList<String>> greeting(@RequestParam(value = "x", defaultValue = "2"
            ) int x, @RequestParam(value="q",required = true) String q) throws Exception {
        button1continue(path+"\\b", path+"\\a");
        return clusterFront(q,x);
    }

    @GetMapping("/button")
    public String button(@RequestParam(value="btn", required = true) int btn, @RequestParam(value = "s1") String s1, @RequestParam(value = "s2") String s2) throws IOException {
        if(btn == 1){
            button1continue(path+"\\"+s1,path+"\\"+s2);
            return "Success";
        }
        else{
            return "Failed";
        }
    }

    @GetMapping("/cluster")
    public ArrayList<String> clust(@RequestParam(value = "n", defaultValue = "1") int n) throws IOException {
        return listCluster(n);
    }
}