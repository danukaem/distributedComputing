package com.javainuse.domain;

import com.javainuse.entity.NodeRoleType;

import java.util.ArrayList;
import java.util.List;

public class WebSocketChatMessage {
    private String type;
    private String content;
    private String sender;
    public NodeRoleType nodeRole;
    public static int nodeCount = 0;
    public static int masterNodeId = 0;
    public static List<Integer> proposerNodeIds = new ArrayList<>();
    public static List<Integer> acceptorNodeIds = new ArrayList<>();
    public static int learnerNodeId;


//    enum NodeRoleType {
//        MASTER_NODE,
//        PROPOSER_NODE,
//        ACCEPTOR_NODE,
//        LEARNER_NODE,
//        UNDEFINED_NODE
//    }

    public NodeRoleType getNodeRole() {
        return nodeRole;
    }

    public void setNodeRole(NodeRoleType nodeRole) {
        this.nodeRole = nodeRole;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public static int getNodeCount() {
        return nodeCount;
    }

    public static void setNodeCount(int nodeCount) {
        WebSocketChatMessage.nodeCount = nodeCount;
    }

    public static int getMasterNodeId() {
        return masterNodeId;
    }

    public static void setMasterNodeId(int masterNodeId) {
        WebSocketChatMessage.masterNodeId = masterNodeId;
    }

    public static List<Integer> getProposerNodeIds() {
        return proposerNodeIds;
    }

    public static void setProposerNodeIds(List<Integer> proposerNodeIds) {
        WebSocketChatMessage.proposerNodeIds = proposerNodeIds;
    }

    public static List<Integer> getAcceptorNodeIds() {
        return acceptorNodeIds;
    }

    public static void setAcceptorNodeIds(List<Integer> acceptorNodeIds) {
        WebSocketChatMessage.acceptorNodeIds = acceptorNodeIds;
    }

    public static int getLearnerNodeId() {
        return learnerNodeId;
    }

    public static void setLearnerNodeId(int learnerNodeId) {
        WebSocketChatMessage.learnerNodeId = learnerNodeId;
    }

}
