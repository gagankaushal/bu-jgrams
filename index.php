<?php
$output = shell_exec("server/build/distributions/server-1.0/bin/server1");
header('Location: https://bu-jgrams.herokuapp.com/index.ejs?success=true');
echo "<pre>$output</pre>";
?>
