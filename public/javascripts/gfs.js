function getChunkServers() {
    $("#chunkServerTableBody").html("");
    $("#fileTableBody").html("");

    $.get("/master/triggerPolling", function () {});
    $.get("/master/getChunkServers", function(data) {
        $.each(data.chunkServers[0], function(index, value) {
            var getChunks = "http://" + value.ip + ":" + value.port + "/chunkServer/poll";
            var stopAddress = "http://" + value.ip + ":" + value.port + "/chunkServer/stop?ip=" + value.ip + "&port=" + value.port;
            if (index == 0) $("#chunkServerTableBody").html("");
            $("#chunkServerTableBody").append("" +
                "<tr>" +
                    "<td scope='row'>" + value.ip + ":" + value.port + "</td>" +
                    "<td scope='row'> <a href='" + getChunks + "' target='_blank'> Display Chunks </a></td>" +
                    "<td scope='row'>" + value.status + "</td>" +
                    "<td scope='row'><button id=" + value.port + " class='btn btn-danger'>Stop</button></td>" +
                "</tr>"
            );
            $('#' + value.port).click(function(event) {
                event.preventDefault();
                $.get(stopAddress, function () {
                    getChunkServers();
                });
            });
        });
    });

    $.get("/master/getFiles", function(data) {
        $("#fileTableBody").append("" +
            "<tr>" +
            "<td scope='row'>" + JSON.stringify(data,  null, 2) + "</td>" +
            "</tr>"
        );
    });
}

// Call the getChunkServers method every 10 seconds and update data.
// "Heartbeat messages"
setInterval(getChunkServers, 10000);

function newChunkServer() {
    $.get("/master/registerNewChunkServer", function(data) {
      // Nothing here
        console.log(data);
    }).done(function() {
        $("#newChunkServerFailedText").hide();
        $("#newChunkServerSuccessText").show();
        getChunkServers();
    }).fail(function() {
        $("#newChunkServerSuccessText").hide();
        $("#newChunkServerFailedText").show();
    });
}
