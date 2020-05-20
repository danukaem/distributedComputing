package com.javainuse;

import com.javainuse.controller.WebSocketChatController;
import com.javainuse.domain.NumberRangeWithNodes;
import com.javainuse.domain.WebSocketChatMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class WebsocketChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketChatApplication.class, args);

//        List<Integer> integers = new WebSocketChatController().readNumberInLineFromFile();
//        System.out.println("*********************************************");
//        System.out.println(integers);
//        System.out.println("*********************************************");
    }


}
