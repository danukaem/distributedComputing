package com.javainuse.entity;

import lombok.*;

import java.util.HashMap;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FinalResultsOfNumbers {

    HashMap<Integer,Boolean> resultListOfNumbers;
    String type;
    int masterNodeId;



}
