# AC-Fix Server

###### Author: Chen, Hong-Wun
###### Organization: SELab in Department of CSIE, NTU

---

This project is a submodule of AC-Fix.

This project set up a Spring server for AC-Fix.

## System Requirement

Minimum Requirement
- JDK 8
- Docker 1.13 (Docker Engine API 1.25)

## Build Project

Build command: ```./gradlew build```

## Run server

Build command: ```./gradlew bootRun```

The generated ```./data``` directory will be the persistence database

## Build Docker Image

Build command ```./gradlew buildDockerImage -Pdocker```

This will build a Docker image with tag ```ac-fix/ac-fix-server:1.0```

Run the image with command like:

```docker volume create ACFixServerData```

```docker run -d -p 5566:5566 -v ACFixServerData:/opt/data -v /var/run/docker.sock:/var/run/docker.sock [IMAGE ID]```