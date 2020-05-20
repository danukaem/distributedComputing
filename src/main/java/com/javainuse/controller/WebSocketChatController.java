package com.javainuse.controller;

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

        System.out.println("################################################################################# : 1no");
    }

    @MessageMapping("/chat.newNodeAddToAllNodes")
    @SendTo("/topic/distributedComputing")
    public WebSocketChatMessage newNodeAddToAllNodes(@Payload WebSocketChatMessage webSocketChatMessage,
                                                     SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("################################################################################# : 2 " + webSocketChatMessage);

        return webSocketChatMessage;
    }


    @MessageMapping("/chat.addNodeToHashMap")
    public void addNodeToHashMap(@Payload WebSocketChatMessage webSocketChatMessage,
                                 SimpMessageHeaderAccessor headerAccessor) {
        roleTypeHashMap.put(Integer.parseInt(webSocketChatMessage.getSender()), webSocketChatMessage.getNodeRole());
        System.out.println("################################################################################# : 3 " + webSocketChatMessage);


    }


    @MessageMapping("/chat.nodeRemoveFromAllNodes")
    public void nodeRemoveFromAllNodes(@Payload WebSocketChatMessage webSocketChatMessage,
                                       SimpMessageHeaderAccessor headerAccessor) {
        roleTypeHashMap.remove(Integer.parseInt(webSocketChatMessage.getSender()));
        System.out.println("################################################################################# : 4 " + webSocketChatMessage);


    }

    @MessageMapping("/chat.startElectionRequest")
    public void startElectionRequest(@Payload WebSocketChatMessage webSocketChatMessage,
                                     SimpMessageHeaderAccessor headerAccessor) {

        try {
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(10) * 1000);
            System.out.println("waiting time startElectionRequest");
            if (!checkMasterNodeAssigned()) {

                int masterNodeId = findMasterNodeId();
                List<Integer> acceptorNodeIds = findAcceptorNodeIds();
                int learnerNodeId = findLearnerNodeIds();
                List<Integer> proposerNodeIds = findProposerNodeIds();
                WebSocketChatMessage.masterNodeId = masterNodeId;

                WebSocketChatMessage electionMasterNode = new WebSocketChatMessage();
                electionMasterNode.setType("Master_Node_BroadCast");
                electionMasterNode.setMasterNodeIdJson(masterNodeId);
                electionMasterNode.setAcceptorNodeIdsJson(acceptorNodeIds);
                electionMasterNode.setLearnerNodeIdJson(learnerNodeId);
                electionMasterNode.setProposerNodeIdsJson(proposerNodeIds);
                electionMasterNode.setContent(NodeRoleType.MASTER_NODE.toString());
                messagingTemplate.convertAndSend("/topic/distributedComputing", electionMasterNode);
                System.out.println("################################################################################# : 5a  " + electionMasterNode);

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("################################################################################# : 5no");

    }

    @MessageMapping("/chat.masterNodeBroadCast")
    public void masterNodeBroadCast(@Payload WebSocketChatMessage webSocketChatMessage,
                                    SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("nodeid : " + webSocketChatMessage.getMasterNodeIdJson() + " and node Role : " + webSocketChatMessage.getNodeRole());

        roleTypeHashMap.put(webSocketChatMessage.getMasterNodeIdJson(), webSocketChatMessage.getNodeRole());
        System.out.println("################################################################################# : 6no");

    }

    @MessageMapping("/chat.acceptorNodeBroadCast")
    public void acceptorNodeBroadCast(@Payload WebSocketChatMessage webSocketChatMessage,
                                      SimpMessageHeaderAccessor headerAccessor) {

        webSocketChatMessage.getAcceptorNodeIdsJson().forEach(acceptorId -> {
            roleTypeHashMap.put(acceptorId, webSocketChatMessage.getNodeRole());
            System.out.println("nodeid : " + acceptorId + " and node Role : " + webSocketChatMessage.getNodeRole());

        });
        System.out.println("################################################################################# : 7no");

    }

    @MessageMapping("/chat.learnerNodeBroadCast")
    public void learnerNodeBroadCast(@Payload WebSocketChatMessage webSocketChatMessage,
                                     SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("nodeid : " + webSocketChatMessage.getLearnerNodeIdJson() + " and node Role : " + webSocketChatMessage.getNodeRole());

        roleTypeHashMap.put(webSocketChatMessage.getLearnerNodeIdJson(), webSocketChatMessage.getNodeRole());

        System.out.println("################################################################################# : 8no");

    }

    @MessageMapping("/chat.proposerNodeBroadCast")
    public void proposerNodeBroadCast(@Payload WebSocketChatMessage webSocketChatMessage,
                                      SimpMessageHeaderAccessor headerAccessor) {

        webSocketChatMessage.getProposerNodeIdsJson().forEach(proposerId -> {
            roleTypeHashMap.put(proposerId, webSocketChatMessage.getNodeRole());
            System.out.println("nodeid : " + proposerId + " and node Role : " + webSocketChatMessage.getNodeRole());

        });
        System.out.println("################################################################################# : 9no");

    }

    public boolean checkMasterNodeAssigned() {
        roleTypeHashMap.forEach((k, v) -> {
            if (v.equals(NodeRoleType.MASTER_NODE)) {
                hasMaster = true;
            }
        });
        System.out.println("################################################################################# : 10 " + hasMaster);

        return hasMaster;
    }

    public int findMasterNodeId() {
        ArrayList<Integer> nodeIdList = new ArrayList<Integer>();
        roleTypeHashMap.forEach((k, v) -> {
            nodeIdList.add(k);
        });
        System.out.println("################################################################################# : 11 no");

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
        System.out.println("################################################################################# : 12 no");

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
        System.out.println("################################################################################# : 13");

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
        System.out.println("################################################################################# : 14  " + proposerList);

        return proposerList;
    }


    public List<Integer> readNumberInLineFromFile() {
        List<Integer> numberListInFile = new ArrayList<>();

//        try {
//            File file = new File("primeNumbersCheck.txt");
//            Scanner scanner = new Scanner(file);
//
//            while (scanner.hasNextLine()) {
//                numberListInFile.add(Integer.parseInt(scanner.nextLine()));
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        numberListInFile.add(5);
        numberListInFile.add(6);
        numberListInFile.add(7);
        numberListInFile.add(8);
        System.out.println("numberListInFile ============================================================= : " + numberListInFile);
        return numberListInFile;
    }


    @MessageMapping("/chat.startFindingPrimeNumberCommandToMaster")
    public void startMasterNodeWork(@Payload AcceptorInMessageFromProposer acceptorInMessageFromProposer,
                                    SimpMessageHeaderAccessor headerAccessor) {

        MasterOutMessageToProposer masterOutMessageToProposer = new MasterOutMessageToProposer();
        masterOutMessageToProposer.setType("number_And_Details_From_Master");
        masterOutMessageToProposer.setIterationNumber(acceptorInMessageFromProposer.getIterationNumber());
        List<Integer> numbersFromFile = readNumberInLineFromFile();
        List<Integer> proposerNodeIds = findProposerNodeIds();
        NumberRangeWithNodes numberRangeWithNodes = new NumberRangeWithNodes();

//        if(acceptorInMessageFromProposer.getNumber()!=0){
            numberListWithResult.put(acceptorInMessageFromProposer.getNumber(),acceptorInMessageFromProposer.isLearnerFinalResult());

//        }

        System.out.println("number result adding to hashmap number: "+acceptorInMessageFromProposer.getNumber());
        System.out.println("number result adding to hashmap result: "+acceptorInMessageFromProposer.isLearnerFinalResult());
        if (acceptorInMessageFromProposer.getIterationNumber() < numbersFromFile.size()) {
            System.out.println("################################################################################# : 18no");

            masterOutMessageToProposer.setLineNumber(acceptorInMessageFromProposer.getIterationNumber() + 1);
            masterOutMessageToProposer.setNumber(numbersFromFile.get(acceptorInMessageFromProposer.getIterationNumber()));
            List<NodeRange> rangesWithMinMax = numberRangeWithNodes.getRangesWithMinMax(numbersFromFile.get(acceptorInMessageFromProposer.getIterationNumber()), proposerNodeIds.size(), proposerNodeIds);
            masterOutMessageToProposer.setRangeWithNodeId(rangesWithMinMax);

//            numberListWithResult.put(acceptorInMessageFromProposer.getNumber(),acceptorInMessageFromProposer.isLearnerFinalResult());
        } else {
//            numberListWithResult.put(acceptorInMessageFromProposer.getNumber(),acceptorInMessageFromProposer.isLearnerFinalResult());
//            FinalResultsOfNumbers finalResultsOfNumbers = new FinalResultsOfNumbers();
//            finalResultsOfNumbers.setMasterNodeId(findMasterNodeId());
//            finalResultsOfNumbers.setType("Publish_Final_result_On_Master");
//            finalResultsOfNumbers.setResultListOfNumbers(numberListWithResult);
            System.out.println(" finish finding all numbers");
            numberListWithResult.forEach((k,v)->{
                if (v) {
                    System.out.println(k + " is a prime number");

                } else {
                    System.out.println(k + " is not a prime number");

                }
            });
//            messagingTemplate.convertAndSend("/topic/distributedComputing", finalResultsOfNumbers);

        }

        System.out.println("################################################################################# : 15 " + masterOutMessageToProposer);

        messagingTemplate.convertAndSend("/topic/distributedComputing", masterOutMessageToProposer);

    }


    @MessageMapping("/chat.numberDataToAcceptor")
    @SendTo("/topic/distributedComputing")
    public AcceptorInMessageFromProposer numberDataToAcceptor(@Payload AcceptorInMessageFromProposer acceptorInMessageFromProposer,
                                                              SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("################################################################################# : 16 " + acceptorInMessageFromProposer);

        return acceptorInMessageFromProposer;

    }


    @MessageMapping("/chat.numberDataToLearner")
    @SendTo("/topic/distributedComputing")
    public AcceptorOutMessageToLearner numberDataToLearner(@Payload AcceptorOutMessageToLearner acceptorInMessageFromProposer,
                                                           SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("################################################################################# : 17 " + acceptorInMessageFromProposer);

        return acceptorInMessageFromProposer;

    }

    @MessageMapping("/chat.finalResultToLearner")
    @SendTo("/topic/distributedComputing")
    public AcceptorInMessageFromProposer finalResultToLearner(@Payload AcceptorInMessageFromProposer acceptorInMessageFromProposer,
                                                              SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("################################################################################# : 19 " + acceptorInMessageFromProposer);
        acceptorInMessageFromProposer.setMasterNodeId(findMasterNodeId());
        return acceptorInMessageFromProposer;

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
