package com.javainuse.domain;

import com.javainuse.controller.WebSocketChatController;
import com.javainuse.entity.*;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class MasterNodeTasks {

    public static HashMap<Integer, NodeRoleType> roleTypeHashMap = WebSocketChatController.roleTypeHashMap;

    AcceptorInMessageFromProposer acceptorInMessageFromProposer;
    public HashMap<Integer, Boolean> numberListWithResult;

    private SimpMessageSendingOperations messagingTemplate;

    public MasterNodeTasks() {
    }

    public MasterNodeTasks(AcceptorInMessageFromProposer acceptorInMessageFromProposer, HashMap<Integer, Boolean> numberListWithResult, SimpMessageSendingOperations messagingTemplate) {
        this.acceptorInMessageFromProposer = acceptorInMessageFromProposer;
        this.numberListWithResult = numberListWithResult;
        this.messagingTemplate = messagingTemplate;
    }


    public MasterNodeTasks(AcceptorInMessageFromProposer acceptorInMessageFromProposer) {
        this.acceptorInMessageFromProposer = acceptorInMessageFromProposer;
    }

    public MasterNodeTasks(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public MasterNodeTasks(HashMap<Integer, NodeRoleType> roleTypeHashMap, AcceptorInMessageFromProposer acceptorInMessageFromProposer, HashMap<Integer, Boolean> numberListWithResult) {
        this.roleTypeHashMap = roleTypeHashMap;
        this.acceptorInMessageFromProposer = acceptorInMessageFromProposer;
        this.numberListWithResult = numberListWithResult;
    }

    public int findMasterNodeId() {
        ArrayList<Integer> nodeIdList = new ArrayList<Integer>();
        roleTypeHashMap.forEach((k, v) -> {
            nodeIdList.add(k);
        });
        return Collections.max(nodeIdList);
    }

    public List<Integer> readNumberInLineFromFile() {
        List<Integer> numberListInFile = new ArrayList<>();
        try {
            File file = new File("numberList.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                numberListInFile.add(Integer.parseInt(scanner.nextLine()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return numberListInFile;
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

    public void executeMasterNodeWork() {
        MasterOutMessageToProposer masterOutMessageToProposer = new MasterOutMessageToProposer();
        masterOutMessageToProposer.setType("number_And_Details_From_Master");
        masterOutMessageToProposer.setIterationNumber(acceptorInMessageFromProposer.getIterationNumber());
        List<Integer> numbersFromFile = readNumberInLineFromFile();
        List<Integer> proposerNodeIds = findProposerNodeIds();
        NumberRangeWithNodes numberRangeWithNodes = new NumberRangeWithNodes();
        numberListWithResult.put(acceptorInMessageFromProposer.getNumber(), acceptorInMessageFromProposer.isLearnerFinalResult());
        if (acceptorInMessageFromProposer.getIterationNumber() < numbersFromFile.size()) {
            masterOutMessageToProposer.setLineNumber(acceptorInMessageFromProposer.getIterationNumber() + 1);
            masterOutMessageToProposer.setNumber(numbersFromFile.get(acceptorInMessageFromProposer.getIterationNumber()));
            List<NodeRange> rangesWithMinMax = numberRangeWithNodes.getRangesWithMinMax(numbersFromFile.get(acceptorInMessageFromProposer.getIterationNumber()), proposerNodeIds.size(), proposerNodeIds);
            masterOutMessageToProposer.setRangeWithNodeId(rangesWithMinMax);
        } else {
            numberListWithResult.put(acceptorInMessageFromProposer.getNumber(), acceptorInMessageFromProposer.isLearnerFinalResult());
            numberListWithResult.remove(0);
            List<FinalResult> finalResults = new ArrayList<>();
            numberListWithResult.forEach((k, v) -> {
                finalResults.add(new FinalResult(k, v));
            });
            FinalResultsOfNumbers finalResultsOfNumbers = new FinalResultsOfNumbers();
            finalResultsOfNumbers.setMasterNodeId(findMasterNodeId());
            finalResultsOfNumbers.setType("Publish_Final_result_On_Master");
            finalResultsOfNumbers.setResultListOfNumbers(finalResults);
            System.out.println(" finish finding all numbers");
            numberListWithResult.forEach((k, v) -> {
                if (v) {
                    System.out.println(k + " is a prime number");

                } else {
                    System.out.println(k + " is not a prime number");
                }
            });
            messagingTemplate.convertAndSend("/topic/distributedComputing", finalResultsOfNumbers);
        }
        messagingTemplate.convertAndSend("/topic/distributedComputing", masterOutMessageToProposer);

    }


}
