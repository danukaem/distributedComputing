package com.javainuse.controller;

import com.javainuse.domain.MasterNodeTasks;
import com.javainuse.domain.NumberRangeWithNodes;
import com.javainuse.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.javainuse.domain.WebSocketChatMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
public class WebSocketChatController {
    @Value("${minimumNodes}")
    int minimumNodes;


    public static HashMap<Integer, NodeRoleType> roleTypeHashMap = new HashMap<>();
    public static HashMap<Integer, Boolean> numberListWithResult = new HashMap<>();
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
            if (roleTypeHashMap.size() > minimumNodes && !masterNodeAssigned) {
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
                int masterNodeId = new MasterNodeTasks().findMasterNodeId();
                MasterNodeTasks masterNodeTasks = new MasterNodeTasks();
                List<Integer> acceptorNodeIds = masterNodeTasks.findAcceptorNodeIds();
                int learnerNodeId = masterNodeTasks.findLearnerNodeIds();
                List<Integer> proposerNodeIds = masterNodeTasks.findProposerNodeIds();
                WebSocketChatMessage.masterNodeId = masterNodeId;

                WebSocketChatMessage electionMasterNode = new WebSocketChatMessage();
                electionMasterNode.setType("Master_Node_BroadCast");
                electionMasterNode.setMasterNodeIdJson(masterNodeId);
                electionMasterNode.setAcceptorNodeIdsJson(acceptorNodeIds);
                electionMasterNode.setLearnerNodeIdJson(learnerNodeId);
                electionMasterNode.setProposerNodeIdsJson(proposerNodeIds);
                electionMasterNode.setContent(NodeRoleType.MASTER_NODE.toString());
                messagingTemplate.convertAndSend("/topic/distributedComputing", electionMasterNode);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat.masterNodeBroadCast")
    public void masterNodeBroadCast(@Payload WebSocketChatMessage webSocketChatMessage,
                                    SimpMessageHeaderAccessor headerAccessor) {
        roleTypeHashMap.put(webSocketChatMessage.getMasterNodeIdJson(), webSocketChatMessage.getNodeRole());
    }

    @MessageMapping("/chat.acceptorNodeBroadCast")
    public void acceptorNodeBroadCast(@Payload WebSocketChatMessage webSocketChatMessage,
                                      SimpMessageHeaderAccessor headerAccessor) {
        webSocketChatMessage.getAcceptorNodeIdsJson().forEach(acceptorId -> {
            roleTypeHashMap.put(acceptorId, webSocketChatMessage.getNodeRole());

        });
    }

    @MessageMapping("/chat.learnerNodeBroadCast")
    public void learnerNodeBroadCast(@Payload WebSocketChatMessage webSocketChatMessage,
                                     SimpMessageHeaderAccessor headerAccessor) {
        roleTypeHashMap.put(webSocketChatMessage.getLearnerNodeIdJson(), webSocketChatMessage.getNodeRole());
    }

    @MessageMapping("/chat.proposerNodeBroadCast")
    public void proposerNodeBroadCast(@Payload WebSocketChatMessage webSocketChatMessage,
                                      SimpMessageHeaderAccessor headerAccessor) {
        webSocketChatMessage.getProposerNodeIdsJson().forEach(proposerId -> {
            roleTypeHashMap.put(proposerId, webSocketChatMessage.getNodeRole());
            System.out.println("nodeid : " + proposerId + " and node Role : " + webSocketChatMessage.getNodeRole());

        });
    }

    public boolean checkMasterNodeAssigned() {
        roleTypeHashMap.forEach((k, v) -> {
            if (v.equals(NodeRoleType.MASTER_NODE)) {
                hasMaster = true;
            }
        });
        return hasMaster;
    }


    @MessageMapping("/chat.startFindingPrimeNumberCommandToMaster")
    public void startMasterNodeWork(@Payload AcceptorInMessageFromProposer acceptorInMessageFromProposer,
                                    SimpMessageHeaderAccessor headerAccessor) {
        new MasterNodeTasks(acceptorInMessageFromProposer, numberListWithResult, messagingTemplate).executeMasterNodeWork();
    }


    @MessageMapping("/chat.numberDataToAcceptor")
    @SendTo("/topic/distributedComputing")
    public AcceptorInMessageFromProposer numberDataToAcceptor(@Payload AcceptorInMessageFromProposer acceptorInMessageFromProposer,
                                                              SimpMessageHeaderAccessor headerAccessor) {
        return acceptorInMessageFromProposer;

    }


    @MessageMapping("/chat.numberDataToLearner")
    @SendTo("/topic/distributedComputing")
    public AcceptorOutMessageToLearner numberDataToLearner(@Payload AcceptorOutMessageToLearner acceptorInMessageFromProposer,
                                                           SimpMessageHeaderAccessor headerAccessor) {
        return acceptorInMessageFromProposer;

    }

    @MessageMapping("/chat.finalResultToLearner")
    @SendTo("/topic/distributedComputing")
    public AcceptorInMessageFromProposer finalResultToLearner(@Payload AcceptorInMessageFromProposer acceptorInMessageFromProposer,
                                                              SimpMessageHeaderAccessor headerAccessor) {
        acceptorInMessageFromProposer.setMasterNodeId(new MasterNodeTasks().findMasterNodeId());
        return acceptorInMessageFromProposer;

    }


    @MessageMapping("/chat.testAnyFunction")
    public void testAnyFunction(@Payload WebSocketChatMessage webSocketChatMessage,
                                SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("all nodes======> " + roleTypeHashMap);
    }

}
