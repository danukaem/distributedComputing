package com.javainuse.domain;

import com.javainuse.entity.NodeRoleType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WebSocketChatMessage {
    private String type;
    private String content;
    private String sender;
    private NodeRoleType nodeRole;
    public static int nodeCount = 0;
    private int nodeCountJson = 0;
    public static int masterNodeId = 0;
    private int masterNodeIdJson = 0;
    public static List<Integer> proposerNodeIds = new ArrayList<>();
    private List<Integer> proposerNodeIdsJson = new ArrayList<>();
    public static List<Integer> acceptorNodeIds = new ArrayList<>();
    private List<Integer> acceptorNodeIdsJson = new ArrayList<>();
    public static int learnerNodeId;
    private int learnerNodeIdJson;


}
