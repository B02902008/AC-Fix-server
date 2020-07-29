FROM openjdk:11.0.8-jre
MAINTAINER Chen, Hong-Wun from SELab in Department of CSIE, NTU
RUN apt update
RUN apt -y install curl
RUN mkdir /opt/server
ADD build/libs/ac-fix-server-1.0.jar /opt/server
WORKDIR /opt/server
EXPOSE 5566
CMD ["java", "-jar", "ac-fix-server-1.0.jar"]