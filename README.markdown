# Methode Story Package Mapper

Methode Story Package Mapper (MSPM) is responsible for listening for new story packages publishes, then transforming it to a structure amenable to processing by
UP and putting this structure back on the queue.

// TODO add description

The application is dependent on the following microservices of the UPP stack:

* [Kafka Proxy](https://github.com/Financial-Times/kafka-proxy), which is 
directly dependent on Zookeeper (and also on Kafka);
* [Document Store API](/projects/CP/repos/document-store-api/browse), which is dependent on MongoDB;

## Running Locally
To compile, run tests and build jar

    mvn clean install

To run locally, run:

    java -jar target/methode-list-mapper-0.0.1-SNAPSHOT.jar server methode-list-mapper.yaml
    
Health check endpoints are available at `http://localhost:16081/healthcheck` and `http://localhost:16080/__health`.    

## Expected behaviour 

// TODO

## Build and Deployment

### Jenkins Job

// TODO

### Run Book TODO

// TODO

## Observations


