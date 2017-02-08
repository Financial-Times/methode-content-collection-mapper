# Methode Story Package Mapper

Methode Story Package Mapper (MSPM) is responsible for listening for new story packages (also known as editors choice components) publishes, then transforming it to a structure amenable to processing by UP and putting this structure back on the queue.

A story package is a manual component created in Methode (also known as editors choice). It can be attached to an article, to provide useful information about other stories on the same topic.

A story package can have linked items, that may be stories added in Methode. Items are assumed to be stored in the UP stack by other processes and references to story package items may not resolve.

The application is dependent on the following microservices of the UPP stack:

* [Kafka Proxy](https://github.com/Financial-Times/kafka-proxy), which is directly dependent on Zookeeper (and also on Kafka);

## Running Locally
To compile, run tests and build jar

    mvn clean install

To run locally, run:

    java -Djava.net.preferIPv4Stack=true -jar target/methode-story-package-mapper-0.0.1.jar server methode-story-package-mapper.yaml
    
Health check endpoints are available at `http://localhost:16081/healthcheck` and `http://localhost:16080/__health`.    

## Expected behaviour 

A transformation from a Methode story package to a UPP story package. The story package publish event is triggered when an article containing the story package component will be published. 

The story package mapper reads events from the NativeCmsPublicationEvents topic and sends the transformed UPP story package to the CmsPublicationEvents topic. 
An example of expected message body is provided below.

```json 
    {
	"payload": {
		"uuid": "a403a332-de48-11e6-86ac-f253db7791c6",
		"items": [{
			"uuid": "d4986a58-de3b-11e6-86ac-f253db7791c6"
		},
		{
			"uuid": "d9b4c4c6-dcc6-11e6-86ac-f253db7791c6"
		},
		{
			"uuid": "d8509dc8-d7ec-11e6-944b-e7eb37a6aa8e"
		},
		{
			"uuid": "404040aa-ce97-11e6-864f-20dcb35cede2"
		},
		{ 			"
			uuid": "834a2bc2-bd67-11e6-8b45-b8b81dd5d080"
		}],
		"publishReference": "tdi23377744",
		"lastModified": 1485876801687
	},
	"contentUri": "http://methode-story-package-mapper.svc.ft.com/story-package/a403a332-de48-11e6-86ac-f253db7791c6",
	"lastModified": "2017-01-31T15:33:21.687Z",
	"uuid": "a403a332-de48-11e6-86ac-f253db7791c6"
}
```

## Build and Deployment

### DockerHub

https://hub.docker.com/r/coco/methode-story-package-mapper/

## Observations

If you get IllegalArgumentException: Host name may not be null, you may fix it by using -Djava.net.preferIPv4Stack=true

