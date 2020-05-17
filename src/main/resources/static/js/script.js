'use strict';

var welcomeForm = document.querySelector('#welcomeForm');
var dialogueForm = document.querySelector('#dialogueForm');

var stompClient = null;
var nodeId = null;
var nodeCount = 0;
var nodeRole = "UNDEFINED_NODE";
var masterNodeId = '';

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
            console.log('*************************************************************')
            console.log('master node broadcast', message)
            console.log('#################################################################')
            masterNodeBroadCast(message);
            setTimeout(acceptorNodeBroadCast(message),1000)
            setTimeout(learnerNodeBroadCast(message),1000)
            setTimeout(proposerNodeBroadCast(message),1000)
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
    if (stompClient) {
        var chatMessage = {
            masterNodeIdJson: message.masterNodeIdJson,
            nodeRole: 'MASTER_NODE'
        };

        stompClient.send("/app/chat.masterNodeBroadCast", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();
}

function acceptorNodeBroadCast(message) {
    if (stompClient) {
        var chatMessage = {
            acceptorNodeIdsJson: message.acceptorNodeIdsJson,
            nodeRole: 'ACCEPTOR_NODE'
        };

        stompClient.send("/app/chat.acceptorNodeBroadCast", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();
}

function learnerNodeBroadCast(message) {
    if (stompClient) {
        var chatMessage = {
            learnerNodeIdJson: message.learnerNodeIdJson,
            nodeRole: 'LEARNER_NODE'
        };

        stompClient.send("/app/chat.learnerNodeBroadCast", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();
}

function proposerNodeBroadCast(message) {
    if (stompClient) {
        var chatMessage = {
            proposerNodeIdsJson: message.proposerNodeIdsJson,
            nodeRole: 'PROPOSER_NODE'
        };

        stompClient.send("/app/chat.proposerNodeBroadCast", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();
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

    if (stompClient) {
        var chatMessage = {};

        stompClient.send("/app/chat.testAnyFunction", {}, JSON
            .stringify(chatMessage));
    }
    event.preventDefault();
}