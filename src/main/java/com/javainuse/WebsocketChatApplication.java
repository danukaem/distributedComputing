package com.javainuse;

import com.javainuse.domain.WebSocketChatMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class WebsocketChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketChatApplication.class, args);


    }


}
