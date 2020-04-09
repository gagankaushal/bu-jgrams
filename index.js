var http = require("http"),
    exec = require("child_process").exec;

function onRequest(request, response) {
  console.log("Request received.");
  response.writeHead(200, {"Content-Type": "text/plain"});

  //executes my shell script - main.sh when a request is posted to the server
  exec('sh server/build/distributions/server-1.0/bin/server', function (err, stdout, stderr) {
    if (err) handleError();

    //Print stdout/stderr to console
    console.log(stdout);
    console.log(stderr);

    //Simple response to user whenever localhost:8888 is accessed
    response.write(stdout);
    response.end();
  });
}

http.createServer(onRequest).listen(8888, function () {
  console.log("Server has started.");
});
