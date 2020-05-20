package com.javainuse.entity;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AcceptorInMessageFromProposer {
    int number;
    int startValue;
    int endValue;
    List<Integer> factorList;
    int proposerNodeId;
    int masterNodeId;
    int acceptorNodeId;
    int learnerNodeId;
    int iterationNumber;
    NodeRoleType nodeRole;
    String type;
    boolean learnerFinalResult;




}
