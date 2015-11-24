cp -r ./5/src/* ./
javac -cp "./commons-codec-1.9.jar:./commons-logging-1.2.jar:./fluent-hc-4.5.1.jar:./httpclient-4.5.1.jar:./httpclient-cache-4.5.1.jar:./httpclient-win-4.5.1.jar:./httpcore-4.4.3.jar:./httpmime-4.5.1.jar:./jna-4.1.0.jar:./jna-platform-4.1.0.jar:./jsonsimple.jar:." *.java
timeout 30s java -cp "./commons-codec-1.9.jar:./commons-logging-1.2.jar:./fluent-hc-4.5.1.jar:./httpclient-4.5.1.jar:./httpclient-cache-4.5.1.jar:./httpclient-win-4.5.1.jar:./httpcore-4.4.3.jar:./httpmime-4.5.1.jar:./jna-4.1.0.jar:./jna-platform-4.1.0.jar:./jsonsimple.jar:." Grader

status=$?

if [ $status -eq 124 ]; then
  curl -H "Content-Type: application/json" -X POST -d "{\"reponame\": \"$CIRCLE_PROJECT_REPONAME\", \"errorMessage\": \"Build timed out. You are probably using an algorithm with a very bad time complexity. We usually allocate at least 10 times the expected time for a code with an acceptable algorithm to run.\"}" http://jarvis.xyz/webhook/curl
fi

if [ $status -eq 1 ]; then
  curl -H "Content-Type: application/json" -X POST -d "{\"reponame\": \"$CIRCLE_PROJECT_REPONAME\", \"errorMessage\": \"Build failed to compile. Do you have all classes and methods present? You should have dummy methods (returning null or dummy primitives) for methods you have yet to implement.\"}" http://jarvis.xyz/webhook/curl
fi

if [ $status -eq -1 ]; then
  curl -H "Content-Type: application/json" -X POST -d "{\"reponame\": \"$CIRCLE_PROJECT_REPONAME\", \"errorMessage\": \"You exceeded the memory limit (4GB). The results of this build are likely invalid. You are probably caught in an infinite loop somewhere.\"}" http://jarvis.xyz/webhook/curl
fi

# echo "HW deadline over"

exit
