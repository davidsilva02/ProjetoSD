var stompClient = null;

function connect() {
    var socket = new SockJS('/stats-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame)
    {
        console.log('Connected:' + frame);
        stompClient.subscribe('/stats/messages', function (message)
        {
            showMessage(JSON.parse(message.body));});
    });
}

function disconnect() {
    if (stompClient !== null) {stompClient.disconnect();}
    console.log("Disconnected");
}

function showMessage(message) {
    //tbody of components
    const c = document.getElementById("components");
    c.innerHTML="";
    for(var component in message["components"]){
        c.innerHTML+=(`<tr>
                    <td>${component}</td>
                    <td>${message["components"][component].ip}</td>
                    <td>${message["components"][component].isAvailable}</td>
                 </tr>`);
    }

    const s = document.getElementById("searches");
    s.innerHTML="";
    for(var search in message["topSearches"]){
        s.innerHTML+=(`<tr>
                    <td>${message["topSearches"][search].numSearches}</td>
                    <td>${message["topSearches"][search].term}</td>
                 </tr>`);
    }

}

connect();