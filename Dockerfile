FROM adoptopenjdk/openjdk11
RUN apt-get update && apt-get install -y iputils-ping
COPY ./target/chatgptAPI-0.0.1-SNAPSHOT.jar .
CMD java -jar chatgptAPI-0.0.1-SNAPSHOT.jar
