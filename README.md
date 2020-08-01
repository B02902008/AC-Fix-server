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

Build command: ```./gradlew clean build```

## Run Server

Build command: ```./gradlew clean bootRun```

The generated ```./data``` directory will be the persistence database.

## Options

- ```acfix.docker.compose```: Setting this property in build command will configure server to be Docker mode
- ```acfix.server.internal.port```: Setting this property in build command will change server running port