[![CircleCI](https://circleci.com/gh/Financial-Times/methode-content-collection-mapper.svg?style=svg)](https://circleci.com/gh/Financial-Times/methode-content-collection-mapper) [![Coverage Status](https://coveralls.io/repos/github/Financial-Times/methode-content-collection-mapper/badge.svg?branch=master)](https://coveralls.io/github/Financial-Times/methode-content-collection-mapper?branch=master)

# Methode Content Collection Mapper

Methode Content Collection Mapper (MCCM) is responsible for listening for new content collecion publishes (i.e. content package or story package, the later is also known as editors choice component), and then transforming it to a structure amenable to processing by UP and putting this structure back on the kafka queue.

A content collection is a web container that links to a list of contents that is called items.

A story package is a manual component created in Methode (also known as editors choice). It can be attached to an article, to provide useful information about other stories on the same topic.

A story package can have linked items, that may be stories added in Methode. Items are assumed to be stored in the UP stack by other processes and references to story package items may not resolve.

A content package is a specific type of content collection which aims to include series and special reports publications. It also has a list of items (content) that it contains.

The application is dependent on the following microservices of the UPP stack:

* [Kafka Proxy](https://github.com/Financial-Times/kafka-proxy), which is directly dependent on Zookeeper (and also on Kafka);
* [document-store-api](https://github.com/Financial-Times/document-store-api)

## Running Locally
To compile, run tests and build jar

    mvn clean install

To run locally, run:

    java -Djava.net.preferIPv4Stack=true -jar target/methode-content-collection-mapper-0.0.1.jar server methode-content-collection-mapper.yaml
    
Health check endpoints are available at `http://localhost:16081/healthcheck` and `http://localhost:16080/__health`.    

## Expected behaviour 

A transformation from a Methode story package to a UPP story package. The story package publish event is triggered when an article containing the story package component will be published.

The mapper reads events from the NativeCmsPublicationEvents topic and sends the transformed package to the CmsPublicationEvents topic.

Content in the package that are Content Placeholders and that are pointing to an FT blog, will be searched for in the document-store-api and their original wordpress uuid will be used in the list.

An example of expected mapper body response for a story package is provided below:
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
		"lastModified": "2017-01-31T15:33:21.687Z"
	},
	"contentUri": "http://methode-content-collection-mapper.svc.ft.com/content-collection/story-package/a403a332-de48-11e6-86ac-f253db7791c6",
	"lastModified": "2017-01-31T15:33:21.687Z",
	"uuid": "a403a332-de48-11e6-86ac-f253db7791c6"
}
```

An example of expected mapper body response for a content package is provided below:
```json
    {
	"payload": {
		"uuid": "45163790-eec9-11e6-abbc-ee7d9c5b3b90",
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
		"lastModified": "2017-01-31T15:33:21.687Z"
	},
	"contentUri": "http://methode-content-collection-mapper.svc.ft.com/content-collection/content-package/45163790-eec9-11e6-abbc-ee7d9c5b3b90",
	"lastModified": "2017-01-31T15:33:21.687Z",
	"uuid": "45163790-eec9-11e6-abbc-ee7d9c5b3b90"
}
```
Note: the only difference is in the contentUri, based on which each one is handled differently in UP stack onwards.

## Build and Deployment

### DockerHub

https://hub.docker.com/r/coco/methode-story-package-mapper/

## Observations

If you get IllegalArgumentException: Host name may not be null, you may fix it by using -Djava.net.preferIPv4Stack=true

