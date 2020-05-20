package com.javainuse.domain;

import com.javainuse.entity.NodeRange;

import java.util.ArrayList;
import java.util.List;

public class NumberRangeWithNodes {


    public List<NodeRange> getRangesWithMinMax(int number,int numberOfNodes, List<Integer> proposerNodeIds) {

        List<NodeRange> nodeRanges = new ArrayList<>();
        int rangeWidth = number / numberOfNodes;


        if (number % numberOfNodes == 0) {
            for (int i = 0; i < numberOfNodes; i++) {
                NodeRange nodeRange = new NodeRange();
                nodeRange.setStartValue(rangeWidth * i + 1);
                nodeRange.setEndValue(rangeWidth * (i + 1));
                nodeRange.setNodeId(proposerNodeIds.get(i));
                nodeRanges.add(nodeRange);
            }
        } else {
            for (int i = 0; i < numberOfNodes - 1; i++) {
                NodeRange nodeRange = new NodeRange();
                nodeRange.setStartValue(rangeWidth * i + 1);
                nodeRange.setEndValue(rangeWidth * (i + 1));
                nodeRange.setNodeId(proposerNodeIds.get(i));

                nodeRanges.add(nodeRange);
            }
            NodeRange nodeRange = new NodeRange();
            nodeRange.setStartValue(rangeWidth * (numberOfNodes - 1) + 1);
            nodeRange.setEndValue(number);
            nodeRange.setNodeId(proposerNodeIds.get(numberOfNodes - 1));

            nodeRanges.add(nodeRange);

        }

        nodeRanges.forEach(n -> {
            System.out.println("nodeRanges : " + n);

        });

        return nodeRanges;
    }

}
