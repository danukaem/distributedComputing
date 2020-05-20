package com.javainuse.entity;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MasterOutMessageToProposer {

    String type;
    int lineNumber;
    int number;
    int iterationNumber;
    List<NodeRange> rangeWithNodeId;

}
