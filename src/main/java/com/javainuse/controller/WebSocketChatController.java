package com.javainuse.controller;

import com.javainuse.entity.NodeRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.javainuse.domain.WebSocketChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Controller
public class WebSocketChatController {

    public static HashMap<Integer, NodeRoleType> roleTypeHashMap = new HashMap<>();
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    int nodeId;
    boolean hasMaster = false;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/distributedComputing")
    public WebSocketChatMessage sendMessage(@Payload WebSocketChatMessage webSocketChatMessage) {
        return webSocketChatMessage;
    }

    @MessageMapping("/chat.newUser")
    public void addUser(@Payload WebSocketChatMessage webSocketChatMessage,
                        SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", webSocketChatMessage.getSender());

        nodeId = Integer.parseInt(webSocketChatMessage.getSender());

        roleTypeHashMap.put(Integer.parseInt(webSocketChatMessage.getSender()), webSocketChatMessage.getNodeRole());
        messagingTemplate.convertAndSend("/topic/distributedComputing", webSocketChatMessage);

        try {
            TimeUnit.MILLISECONDS.sleep(4000);
            boolean masterNodeAssigned = checkMasterNodeAssigned();

            if (roleTypeHashMap.size() > 3 && !masterNodeAssigned) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                    WebSocketChatMessage electionRequest = new WebSocketChatMessage();
                    electionRequest.setType("Start_Election_Request");
                    messagingTemplate.convertAndSend("/topic/distributedComputing", electionRequest);

                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat.newNodeAddToAllNodes")
    @SendTo("/topic/distributedComputing")
    public WebSocketChatMessage newNodeAddToAllNodes(@Payload WebSocketChatMessage webSocketChatMessage,
                                                     SimpMessageHeaderAccessor headerAccessor) {
        return webSocketChatMessage;
    }


    @MessageMapping("/chat.addNodeToHashMap")
    public void addNodeToHashMap(@Payload WebSocketChatMessage webSocketChatMessage,
                                 SimpMessageHeaderAccessor headerAccessor) {
        roleTypeHashMap.put(Integer.parseInt(webSocketChatMessage.getSender()), webSocketChatMessage.getNodeRole());

    }


    @MessageMapping("/chat.nodeRemoveFromAllNodes")
    public void nodeRemoveFromAllNodes(@Payload WebSocketChatMessage webSocketChatMessage,
                                       SimpMessageHeaderAccessor headerAccessor) {
        roleTypeHashMap.remove(Integer.parseInt(webSocketChatMessage.getSender()));

    }

    @MessageMapping("/chat.startElectionRequest")
    public void startElectionRequest(@Payload WebSocketChatMessage webSocketChatMessage,
                                     SimpMessageHeaderAccessor headerAccessor) {

        try {
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(10) * 1000);
            System.out.println("waiting time startElectionRequest");
            if (!checkMasterNodeAssigned()) {

                int masterNodeId = findMasterNodeId();
                WebSocketChatMessage.masterNodeId = masterNodeId;

                WebSocketChatMessage electionMasterNode = new WebSocketChatMessage();
                electionMasterNode.setType("Master_Node_BroadCast");
                electionMasterNode.setMasterNodeIdJson(masterNodeId);
                electionMasterNode.setContent(NodeRoleType.MASTER_NODE.toString());
                messagingTemplate.convertAndSend("/topic/distributedComputing", electionMasterNode);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @MessageMapping("/chat.masterNodeDataAdding")
    public void masterNodeDataAdding(@Payload WebSocketChatMessage webSocketChatMessage,
                                     SimpMessageHeaderAccessor headerAccessor) {
        roleTypeHashMap.put(webSocketChatMessage.getMasterNodeIdJson(), webSocketChatMessage.getNodeRole());
    }



    public boolean checkMasterNodeAssigned() {
        roleTypeHashMap.forEach((k, v) -> {
            if (v.equals(NodeRoleType.MASTER_NODE)) {
                hasMaster = true;
            }
        });
        return hasMaster;
    }

    public int findMasterNodeId() {
        ArrayList<Integer> nodeIdList = new ArrayList<Integer>();
        roleTypeHashMap.forEach((k, v) -> {
            nodeIdList.add(k);
        });
        return Collections.max(nodeIdList);
    }

    @MessageMapping("/chat.testAnyFunction")
    public void testAnyFunction(@Payload WebSocketChatMessage webSocketChatMessage,
                                SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("all nodes======> "+roleTypeHashMap);
    }

}
