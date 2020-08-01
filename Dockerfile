FROM openjdk:11.0.8-jre
MAINTAINER Chen, Hong-Wun from SELab in Department of CSIE, NTU
RUN apt update
RUN apt -y install curl
ADD build/libs/ /opt/server/
WORKDIR /opt/server
EXPOSE 8080
CMD ["java", "-jar", "server-1.0.jar"]