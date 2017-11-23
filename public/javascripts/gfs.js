function getChunkServers() {
    $("#chunkServerTableBody").html("");
    $.get("/master/getChunkServers", function(data) {
        $.each(data.chunkServers[0], function(index, value) {
            var ip = "/master/chunkServerDead?ip=" + value.ip + "&port=" + value.port;
            $("#chunkServerTableBody").append("" +
                "<tr>" +
                    "<td scope='row'>" + value.ip + ":" + value.port + "</td>" +
                    "<td scope='row'>" + "chunks" + "</td>" +
                    "<td scope='row'>" + value.status + "</td>" +
                    "<td scope='row'><a href='' + class='btn btn-danger startOrStopButton'>Stop</a></td>" +
                "</tr>"
            );
            $('.startOrStopButton').click(function(event) {
                event.preventDefault();
                $.get(ip, function () {
                    getChunkServers();
                });
            });
        });
        setTimeout(getChunkServers, 10000);
    });
}
