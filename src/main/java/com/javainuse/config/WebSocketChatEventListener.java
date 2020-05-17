package com.javainuse.config;

import com.javainuse.controller.WebSocketChatController;
import com.javainuse.entity.NodeRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.javainuse.domain.WebSocketChatMessage;

import java.util.concurrent.TimeUnit;

@Component
public class WebSocketChatEventListener {


    @Value("${minimumNodes}")
    int minimumNodes;
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        System.out.println("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        NodeRoleType nodeRoleType = NodeRoleType.UNDEFINED_NODE;

        if (username != null) {
            nodeRoleType = WebSocketChatController.roleTypeHashMap.get(Integer.parseInt(username));

            WebSocketChatController.roleTypeHashMap.remove(Integer.parseInt(username));
            WebSocketChatMessage chatMessage = new WebSocketChatMessage();
            chatMessage.setType("Leave");
            chatMessage.setSender(username);

            messagingTemplate.convertAndSend("/topic/distributedComputing", chatMessage);

            try {
                TimeUnit.MILLISECONDS.sleep(100);
                if (WebSocketChatController.roleTypeHashMap.size() > minimumNodes) {
                    if(nodeRoleType == NodeRoleType.MASTER_NODE ||nodeRoleType == NodeRoleType.ACCEPTOR_NODE|| nodeRoleType == NodeRoleType.LEARNER_NODE){
                        WebSocketChatMessage electionRequest = new WebSocketChatMessage();
                        electionRequest.setType("Start_Election_Request");
                        messagingTemplate.convertAndSend("/topic/distributedComputing", electionRequest);
                    }

                }else if(nodeRoleType == NodeRoleType.MASTER_NODE && WebSocketChatController.roleTypeHashMap.size() < minimumNodes){
                    System.out.println("cancel the process until fulfill the minimum nodes");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }


    }
}
