# Spring Data Redis Demo 

This project demonstrates how to use Spring Data Redis. 
The web services contained in this project simulate a simple version of throttling bandwidth speed based on usage for an imaginary *unlimited* data plan. 

This data plan has 3 bandwidth service tier speeds FAST, MEDIUM, and SLOW. Each tier has an assigned data usage threshold. As data usage increases for a account holder, bandwidth speed is throttled downwards as the usage amount crosses each threshold. Data usage is reset to zero periodically based on how long each account is consuming data.

## Development stack

This project was developed using Java 8, Spring Boot, Spring Data Redis, Eclipse Oxygen, and Maven. 

## Prerequisites

The integration tests within this project require a Redis instance running on localhost. Testing was done on Windows 10 Professional with Docker installed. The Docker container is started using these commands from a Windows Command prompt:
`docker pull redis`
`docker run --name some-redis –p 6379:6379 -d redis`

When finished, the Docker container is terminated using these commands:
`docker stop some-redis`
`docker rm some-redis`

## Build

Run `mvn clean install` to build the project and run the supplied integration tests. The build artifacts will be stored in the `target/` directory. 


