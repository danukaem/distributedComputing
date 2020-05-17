package com.javainuse.controller;

import com.javainuse.entity.NodeRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.javainuse.domain.WebSocketChatMessage;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
public class WebSocketChatController {
    @Value("${minimumNodes}")
    int minimumNodes;

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

                int masterNodeId = findMasterNodeId();
                List<Integer> acceptorNodeIds= findAcceptorNodeIds();
                int learnerNodeId=findLearnerNodeIds();
                List<Integer> proposerNodeIds= findProposerNodeIds();
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
        System.out.println("nodeid : "+ webSocketChatMessage.getMasterNodeIdJson()+" and node Role : "+webSocketChatMessage.getNodeRole());

        roleTypeHashMap.put(webSocketChatMessage.getMasterNodeIdJson(), webSocketChatMessage.getNodeRole());
    }
    @MessageMapping("/chat.acceptorNodeBroadCast")
    public void acceptorNodeBroadCast(@Payload WebSocketChatMessage webSocketChatMessage,
                                     SimpMessageHeaderAccessor headerAccessor) {

        webSocketChatMessage.getAcceptorNodeIdsJson().forEach(acceptorId->{
            roleTypeHashMap.put(acceptorId, webSocketChatMessage.getNodeRole());
            System.out.println("nodeid : "+acceptorId+" and node Role : "+webSocketChatMessage.getNodeRole());

        });

    }
    @MessageMapping("/chat.learnerNodeBroadCast")
    public void learnerNodeBroadCast(@Payload WebSocketChatMessage webSocketChatMessage,
                                     SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("nodeid : "+webSocketChatMessage.getLearnerNodeIdJson()+" and node Role : "+webSocketChatMessage.getNodeRole());

            roleTypeHashMap.put(webSocketChatMessage.getLearnerNodeIdJson(), webSocketChatMessage.getNodeRole());


    }
    @MessageMapping("/chat.proposerNodeBroadCast")
    public void proposerNodeBroadCast(@Payload WebSocketChatMessage webSocketChatMessage,
                                      SimpMessageHeaderAccessor headerAccessor) {

        webSocketChatMessage.getProposerNodeIdsJson().forEach(proposerId->{
            roleTypeHashMap.put(proposerId, webSocketChatMessage.getNodeRole());
            System.out.println("nodeid : "+proposerId+" and node Role : "+webSocketChatMessage.getNodeRole());

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

    public int findMasterNodeId() {
        ArrayList<Integer> nodeIdList = new ArrayList<Integer>();
        roleTypeHashMap.forEach((k, v) -> {
            nodeIdList.add(k);
        });
        return Collections.max(nodeIdList);
    }

    public List<Integer> findAcceptorNodeIds() {
        ArrayList<Integer> nodeIdList = new ArrayList<Integer>();
        ArrayList<Integer> acceptorList = new ArrayList<Integer>();
        roleTypeHashMap.forEach((k, v) -> {
            nodeIdList.add(k);
        });
        Collections.sort(nodeIdList);
        Collections.reverse(nodeIdList);
        acceptorList.add(nodeIdList.get(1));
        acceptorList.add(nodeIdList.get(2));

        return acceptorList;
    }

    public int findLearnerNodeIds() {
        ArrayList<Integer> nodeIdList = new ArrayList<Integer>();
        ArrayList<Integer> proposerList = new ArrayList<Integer>();
        roleTypeHashMap.forEach((k, v) -> {
            nodeIdList.add(k);
        });
        Collections.sort(nodeIdList);
        Collections.reverse(nodeIdList);
        return nodeIdList.get(3);
    }

    public List<Integer> findProposerNodeIds() {
        ArrayList<Integer> nodeIdList = new ArrayList<Integer>();
        ArrayList<Integer> proposerList = new ArrayList<Integer>();
        roleTypeHashMap.forEach((k, v) -> {
            nodeIdList.add(k);
        });
        Collections.sort(nodeIdList);
        Collections.reverse(nodeIdList);
        for (int i = 4; i < nodeIdList.size(); i++) {
            proposerList.add(nodeIdList.get(i));
        }
        return proposerList;
    }


    //
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
    @MessageMapping("/chat.testAnyFunction")
    public void testAnyFunction(@Payload WebSocketChatMessage webSocketChatMessage,
                                SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("all nodes======> " + roleTypeHashMap);
    }

}
