'use strict';

var welcomeForm = document.querySelector('#welcomeForm');
var dialogueForm = document.querySelector('#dialogueForm');

var stompClient = null;
var nodeId = null;
var nodeCount = 0;
var nodeRole = "UNDEFINED_NODE";
var masterNodeId;
var acceptorNodeIds = [];
var proposerNodeIds = [];
var learnerNodeId;

function connect(event) {

    document.querySelector('#welcome-page').classList.add('hidden');
    document.querySelector('#dialogue-page').classList.remove('hidden');

    var socket = new SockJS('/websocketApp');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, connectionSuccess);

    event.preventDefault();
}

function connectionSuccess() {
    stompClient.subscribe('/topic/distributedComputing', onMessageReceived);
    nodeId = new Date().getMilliseconds() + Math.floor((Math.random() * 10) + 1);

    var nId = document.createTextNode(nodeId);
    document.querySelector('#nodeId').appendChild(nId);
    stompClient.send("/app/chat.newUser", {}, JSON.stringify(
        {
            sender: nodeId,
            type: 'newUser',
            nodeRole: "UNDEFINED_NODE"
        }
    ))

}

function disconnect() {
    window.location.reload();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageElement = document.createElement('li');

    switch (message.type) {
        case 'newUser': {
            newNodeAddToAllNodes(message);
            break;
        }
        case 'Leave'    : {
            nodeRemoveFromAllNodes(message);
            break;

        }
        case 'newNodeAddToAllNodes': {
            addNodeToHashMap(message);
            break;

        }
        case 'Start_Election_Request': {
            startElectionRequest();
            break;

        }
        case 'Master_Node_BroadCast': {
            setTimeout(masterNodeBroadCast(message), 500);
            setTimeout(acceptorNodeBroadCast(message), 500);
            setTimeout(learnerNodeBroadCast(message), 500);
            setTimeout(proposerNodeBroadCast(message), 500);
            if (nodeId === message.masterNodeIdJson) {
                var tempMessage = {
                    nodeRole: "MASTER_NODE",
                    iterationNumber: 0
                }
                startFindingPrimeNumberCommandToMaster(tempMessage);
            }
            break;
        }
        case 'number_And_Details_From_Master': {
            console.log("message**********************************************************")
            console.log(message)
            console.log("message**********************************************************")
            IfThisProposerNodeCheckNumberIsPrime(message);
            break;

        }
        case 'Acceptor_Receive_Data_From_Proposer': {
            IfThisAcceptorNodeSendToLearner(message);
            break;

        }
        case 'Acceptor_Result': {
            IfThisLearnerNodeSendToMaster(message);
            break;

        }
        case 'Learner_To_Master_Continue_Iteration': {
            console.log("00000000000000000000000000000000000000000000000000000000first saved nodeRole : ", nodeId)
            console.log("00000000000000000000000000000000000000000000000000000000first saved nodeRole : ", message.masterNodeId)

            if (message.masterNodeId === nodeId) {
                continueFindingPrimeNumberCommandToMaster(message);
            }
            break;

        }
        case 'Publish_Final_result_On_Master': {
            if (nodeId === message.masterNodeId) {
                console.log("resultssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss", message)
                publishResultsOfNumbers(message);
            }
            break;

        }


    }

    // var textElement = document.createElement('p');
    // var messageText = document.createTextNode(message.content);
    // textElement.appendChild(messageText);
    //
    // messageElement.appendChild(textElement);
    //
    // document.querySelector('#messageList').appendChild(messageElement);
    // document.querySelector('#messageList').scrollTop = document
    //     .querySelector('#messageList').scrollHeight;

}

function newNodeAddToAllNodes(message) {
    if (stompClient) {
        var chatMessage = {
            sender: nodeId,
            nodeRole: nodeRole,
            type: 'newNodeAddToAllNodes'
        };

        stompClient.send("/app/chat.newNodeAddToAllNodes", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();

}

function addNodeToHashMap(message) {
    if (stompClient) {
        var chatMessage = {
            sender: message.sender,
            nodeRole: message.nodeRole,
        };

        stompClient.send("/app/chat.addNodeToHashMap", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();


}

function nodeRemoveFromAllNodes(message) {
    if (stompClient) {
        var chatMessage = {
            sender: message.sender
        };

        stompClient.send("/app/chat.nodeRemoveFromAllNodes", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();
}

function startElectionRequest() {
    if (stompClient) {
        var chatMessage = {
            sender: nodeId
        };

        stompClient.send("/app/chat.startElectionRequest", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();
}

function masterNodeBroadCast(message) {
    if (nodeId === message.masterNodeIdJson) {
        nodeRole = 'MASTER_NODE';
        var nRole = document.createTextNode(nodeRole);
        document.querySelector('#nodeRole').appendChild(nRole);
    }
    if (stompClient) {
        var chatMessage = {
            masterNodeIdJson: message.masterNodeIdJson,
            nodeRole: 'MASTER_NODE'
        };
        masterNodeId = message.masterNodeIdJson;
        stompClient.send("/app/chat.masterNodeBroadCast", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();
}

function acceptorNodeBroadCast(message) {

    if (nodeId === message.acceptorNodeIdsJson[0] || nodeId === message.acceptorNodeIdsJson[1]) {
        nodeRole = 'ACCEPTOR_NODE';
        var nRole = document.createTextNode(nodeRole);
        document.querySelector('#nodeRole').appendChild(nRole);
    }

    if (stompClient) {
        var chatMessage = {
            acceptorNodeIdsJson: message.acceptorNodeIdsJson,
            nodeRole: 'ACCEPTOR_NODE'
        };
        acceptorNodeIds.push(message.acceptorNodeIdsJson[0]);
        acceptorNodeIds.push(message.acceptorNodeIdsJson[1]);
        stompClient.send("/app/chat.acceptorNodeBroadCast", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();
}

function learnerNodeBroadCast(message) {
    if (nodeId === message.learnerNodeIdJson) {
        nodeRole = 'LEARNER_NODE';
        var nRole = document.createTextNode(nodeRole);
        document.querySelector('#nodeRole').appendChild(nRole);
    }
    if (stompClient) {
        var chatMessage = {
            learnerNodeIdJson: message.learnerNodeIdJson,
            nodeRole: 'LEARNER_NODE'
        };
        learnerNodeId = message.learnerNodeIdJson;
        stompClient.send("/app/chat.learnerNodeBroadCast", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();
}

function proposerNodeBroadCast(message) {
    for (var n = 0; n < message.proposerNodeIdsJson.length; n++) {
        if (nodeId === message.proposerNodeIdsJson[n]) {
            nodeRole = 'PROPOSER_NODE';
            var nRole = document.createTextNode(nodeRole);
            document.querySelector('#nodeRole').appendChild(nRole);
        }
    }

    if (stompClient) {
        var chatMessage = {
            proposerNodeIdsJson: message.proposerNodeIdsJson,
            nodeRole: 'PROPOSER_NODE'
        };

        for (var i = 0; i < message.proposerNodeIdsJson.length; i++) {
            proposerNodeIds.push(message.proposerNodeIdsJson[i]);
        }

        stompClient.send("/app/chat.proposerNodeBroadCast", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();
}

function startFindingPrimeNumberCommandToMaster(message) {
    console.log("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx : 1 :", message)

    if (stompClient) {
        var chatMessage = {
            nodeRole: message.nodeRole,
            iterationNumber: message.iterationNumber
        };
        stompClient.send("/app/chat.startFindingPrimeNumberCommandToMaster", {}, JSON
            .stringify(chatMessage));
    }
    console.log("########################################################################## : 1 :", chatMessage)

    event.preventDefault();
}

function continueFindingPrimeNumberCommandToMaster(message) {
    console.log("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx : 1new :", message)

    if (stompClient) {
        var chatMessage = {
            nodeRole: message.nodeRole,
            iterationNumber: message.iterationNumber,
            number: message.number,
            learnerFinalResult: message.learnerFinalResult
        };
        stompClient.send("/app/chat.startFindingPrimeNumberCommandToMaster", {}, JSON
            .stringify(chatMessage));
    }
    console.log("########################################################################## : 1 a:", chatMessage)

    event.preventDefault();

}

function IfThisProposerNodeCheckNumberIsPrime(message) {
    var rangeWithNodeId = message.rangeWithNodeId;
    for (var i = 0; i < rangeWithNodeId.length; i++) {
        if (nodeId === rangeWithNodeId[i].nodeId) {
            divideNumberByValueBitweenRange(message.number, rangeWithNodeId[i].startValue, rangeWithNodeId[i].endValue, message.iterationNumber);
            console.log("########################################################################## : 2 : ", message)
        }
    }

}

function divideNumberByValueBitweenRange(number, startValue, endValue, iterationNumber) {
    var factorList = [];
    var proposerNodeId = nodeId;
    var acceptorNodeId = acceptorNodeIds[Math.floor(Math.random() * Math.floor(2))];
    console.log("#########acceptorNodeId###########acceptorNodeId###################################################### :", acceptorNodeId)

    var nodeRole = nodeRole;
    for (var i = startValue; i <= endValue; i++) {
        if (number % i === 0) {
            factorList.push(i);
            console.log("########################################################################## : 3 : ", factorList)

        }
    }
    if (stompClient) {
        var acceptorInMessageFromProposer = {
            number: number,
            startValue: startValue,
            endValue: endValue,
            factorList: factorList,
            proposerNodeId: proposerNodeId,
            acceptorNodeId: acceptorNodeId,
            iterationNumber: iterationNumber,
            learnerNodeId: learnerNodeId,
            nodeRole: nodeRole,
            type: "Acceptor_Receive_Data_From_Proposer"
        };
        stompClient.send("/app/chat.numberDataToAcceptor", {}, JSON
            .stringify(acceptorInMessageFromProposer));
    }
    event.preventDefault();

    var messageElement = document.createElement('li');
    var textElement = document.createElement('p');
    var rangeDetailsElement = document.createTextNode(number + ' is divided by values between ' + startValue + " to " + endValue);
    textElement.appendChild(rangeDetailsElement);
    messageElement.appendChild(textElement);
    document.querySelector('#messageList').appendChild(messageElement);


}

function IfThisAcceptorNodeSendToLearner(message) {
    if (nodeId === message.acceptorNodeId) {
        console.log("########################################################################## : 4 ", message)

        var number = message.number;
        var startValue = message.startValue;
        var endValue = message.endValue;
        var factorList = message.factorList;
        var proposerNodeId = message.proposerNodeId;
        var acceptorNodeId = message.acceptorNodeId;
        var learnerNodeId = message.learnerNodeId;
        var iterationNumber = message.iterationNumber;
        var nodeRole = message.nodeRole;
        var type = "Acceptor_Result";

        var acceptorFinalResult = true;
        for (var i = 0; i < factorList.length; i++) {
            if (factorList[i] !== 1 && factorList[i] !== number) {
                acceptorFinalResult = false;
                console.log("########################################################################## : 5 ", acceptorFinalResult)

            }
        }

        if (stompClient) {
            var acceptorOutMessageToLearner = {
                number: number,
                startValue: startValue,
                endValue: endValue,
                factorList: factorList,
                proposerNodeId: proposerNodeId,
                acceptorNodeId: acceptorNodeId,
                learnerNodeId: learnerNodeId,
                iterationNumber: iterationNumber,
                nodeRole: nodeRole,
                type: type,
                acceptorFinalResult: acceptorFinalResult
            };
            console.log("########################################################################## : 5a ", acceptorOutMessageToLearner)

            stompClient.send("/app/chat.numberDataToLearner", {}, JSON
                .stringify(acceptorOutMessageToLearner));
        }
        event.preventDefault();

        var messageElement = document.createElement('li');
        var textElement = document.createElement('p');
        var proposerResultElement = document.createTextNode(' acceptor received details from proposer node: ' + proposerNodeId +"\n");
        var acceptorResultElement = document.createTextNode(number +' has factors between '+ startValue +' to '+ endValue + ' : '+ acceptorFinalResult  );
        textElement.appendChild(proposerResultElement);
        textElement.appendChild(acceptorResultElement);
        messageElement.appendChild(textElement);
        document.querySelector('#messageList').appendChild(messageElement);



    }

}

var acceptorFinalResults = [];
var learnerFinalResult = true;
var factorListAll = [];

function IfThisLearnerNodeSendToMaster(message) {

    if (nodeId === message.learnerNodeId) {
        console.log("########################################################################## : 6 ", message)

        acceptorFinalResults.push({
            proposerNodeId: message.proposerNodeId,
            acceptorFinalResult: message.acceptorFinalResult
        });
        for (var f = 0; f < message.factorList.length; f++) {
            factorListAll.push(message.factorList[f]);
            console.log("########################################################################## : 7 ", factorListAll)
        }

        if (acceptorFinalResults.length === proposerNodeIds.length) {
            for (var i = 0; i < acceptorFinalResults.length; i++) {
                if (!acceptorFinalResults[i].acceptorFinalResult) {
                    learnerFinalResult = false;
                    console.log("########################################################################## : 8 ", learnerFinalResult)

                }
            }

            if (stompClient) {
                var chatMessage = {
                    nodeRole: 'MASTER_NODE',
                    number: message.number,
                    iterationNumber: parseInt(message.iterationNumber) + 1,
                    factorList: factorListAll,
                    type: "Learner_To_Master_Continue_Iteration",
                    learnerFinalResult: learnerFinalResult
                };
                console.log("########################################################################## : 8a ", chatMessage)

                stompClient.send("/app/chat.finalResultToLearner", {}, JSON
                    .stringify(chatMessage));
                event.preventDefault();

                var messageElement = document.createElement('li');
                var textElement = document.createElement('p');
                var proposerResultElement = document.createTextNode('final result is taken by learner ::: '+message.number +" is prime number :"+  learnerFinalResult );
                textElement.appendChild(proposerResultElement);
                messageElement.appendChild(textElement);
                document.querySelector('#messageList').appendChild(messageElement);

                acceptorFinalResults = [];
                learnerFinalResult = true;
                factorListAll = [];
            }
        }
    }

}

function publishResultsOfNumbers(message) {
    console.log("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy ", message)
    var messageElement = document.createElement('li');
    for (var i = 0; i < message.resultListOfNumbers.length; i++) {
        var textElement = document.createElement('p');
        var messageText = document.createTextNode(message.resultListOfNumbers[i].number + ' is  a prime number : ' + message.resultListOfNumbers[i].prime);
        textElement.appendChild(messageText);
        messageElement.appendChild(textElement);
    }

    document.querySelector('#messageList').appendChild(messageElement);
    document.querySelector('#messageList').scrollTop = document
        .querySelector('#messageList').scrollHeight;
}


////////////////////////////////////sample
// function httpGet(x) {
//     //////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//     // var chatMessage = {
//     //     sender: x.sender,
//     //     content: x.content,
//     //     type: 'CHAT'
//     // };
//     // stompClient.send("/app/chat.sendMessage", {}, JSON
//     //     .stringify(chatMessage));
//
//
//     //////////////////////////////////////////////////////////////////////////////////////////////////////////
//     var userName = "guest";
//     var password = "guest";
//     var token = userName + ":" + password;
//     var hash = token;
//     // var hash = btoa(token);
//     var authr = "Basic " + hash;
//
//     var xmlHttp = new XMLHttpRequest();
//
//     xmlHttp.open("GET", "http://localhost:15672/api/consumers", true, "guest", "guest"); // false for synchronous request
//     // xmlHttp.setRequestHeader("Content-type", "application/json");
//     // xmlHttp.withCredentials = true;
//     // xmlHttp.setRequestHeader("Access-Control-Allow-Credentials","true")
//
//     // xmlHttp.setRequestHeader("Authorization", authr);
//     xmlHttp.responseType = 'json'
//
//     xmlHttp.send(null);
//
//     console.log(xmlHttp)
//
//     return xmlHttp.responseText;
//     // //////////////////////////////////////////////////////////////////////////////////////////////////////////
//     // var xmlHttp = new XMLHttpRequest();
//     // xmlHttp.open("GET", "http://localhost:8081/hello",true); // false for synchronous request
//     // xmlHttp.setRequestHeader("Content-type", "application/json");
//     // xmlHttp.withCredentials=true;
//     // xmlHttp.setRequestHeader("Access-Control-Allow-Credentials","true")
//     // xmlHttp.responseType= 'json'
//     // xmlHttp.send(null);
//     //
//     // console.log( xmlHttp);
//     // console.log( xmlHttp.responseText);
//     //
//     // // return xmlHttp.responseText;
//     //////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//
//     //////////////////////////////////////////////////////////////////////////////////////////////////////////
//     // var xmlHttp = new XMLHttpRequest();
//     // xmlHttp.open("GET", "http://localhost:8081/hello", true); // false for synchronous request
//     //
//     // xmlHttp.responseType = "json";
//     // xmlHttp.send(null);
//     //
//     // console.log(xmlHttp);
//     //
//     // if (xmlHttp.status === 200) {
//     //     if (xmlHttp.readyState === 4) {
//     //         console.log(xmlHttp.response);
//     //         alert(xmlHttp.response)
//     //     }
//     // }
//     //
//     // // console.log( xmlHttp.response);
//
//     // return xmlHttp.responseText;
//
//
// }
// function loadDoc() {
//     var xhttp = new XMLHttpRequest();
//     xhttp.onreadystatechange = function () {
//         if (this.readyState === 4 && this.status === 200) {
//             // console.log( this)
//             console.log(this.responseText)
//         }
//     };
//     xhttp.open("GET", "http://localhost:8081/hello", true);
//     xhttp.send();
// }
// function loadDoc1() {
//     var xhttp = new XMLHttpRequest();
//     xhttp.onreadystatechange = function () {
//         console.log(this.responseXML)
//
//         if (this.readyState === 4 && this.status === 200) {
//             console.log(this.responseText)
//         }
//     };
//     xhttp.open("GET", "http://localhost:15672/api/consumers", true, 'guest', 'guest');
//     xhttp.send();
// }
function testAnyFunction() {
// console.log("document.getElementById(\"#myfile\").value()" ,document.getElementById("myfile").value)
// console.log("document.getElementById(\"#myfile\").value()" ,document.getElementById("myfile").size)
    alert('nodeRole : ' + nodeRole + ' and nodeId :' + nodeId)
    // if (stompClient) {
    //     var chatMessage = {};
    //
    //     stompClient.send("/app/chat.testAnyFunction", {}, JSON
    //         .stringify(chatMessage));
    // }
    // event.preventDefault();
}