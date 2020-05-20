package com.javainuse.entity;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AcceptorOutMessageToLearner {

    int number;
    int startValue;
    int endValue;
    List<Integer> factorList;
    int proposerNodeId;
    int acceptorNodeId;
    int learnerNodeId;
    int iterationNumber;
    NodeRoleType nodeRole;
    String type;
    boolean acceptorFinalResult;
}
