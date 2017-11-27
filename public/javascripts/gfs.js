function getChunkServers() {
    $("#chunkServerTableBody").html("");
    $.get("/master/getChunkServers", function(data) {
        $.each(data.chunkServers[0], function(index, value) {
            if (index == 0) $("#chunkServerTableBody").html("");
            var ip = "/master/chunkServerDead?ip=" + value.ip + "&port=" + value.port;
            $("#chunkServerTableBody").append("" +
                "<tr>" +
                    "<td scope='row'>" + value.ip + ":" + value.port + "</td>" +
                    "<td scope='row'>" + "chunks" + "</td>" +
                    "<td scope='row'>" + value.status + "</td>" +
                    "<td scope='row'><a href='' id=" + value.port + " class='btn btn-danger'>Stop</a></td>" +
                "</tr>"
            );
            $('#' + value.port).click(function(event) {
                event.preventDefault();
                $.get(ip, function () {
                    getChunkServers();
                });
            });
        });
    });
}

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
