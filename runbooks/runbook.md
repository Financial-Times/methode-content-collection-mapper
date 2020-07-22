# UPP - Methode Content Collection Mapper

Methode Content Collection Mapper (MCCM) is responsible for listening to new content collection publishes (i.e. content package or story package) on the kafka NativeCMSPublicationEvents queue, and then transforming them into UPP format and then writing them to kafka CMSPublicationEvents queue.

## Primary URL

<https://upp-prod-delivery-glb.upp.ft.com/__methode-content-collection-mapper/>

## Service Tier

Platinum

## Lifecycle Stage

Production

## Delivered By

content

## Supported By

content

## Known About By

- hristo.georgiev
- robert.marinov
- elina.kaneva
- georgi.ivanov
- tsvetan.dimitrov
- kalin.arsov
- mihail.mihaylov
- boyko.boykov
- donislav.belev
- dimitar.terziev

## Host Platform

AWS

## Architecture

Methode Content Collection Mapper (MCCM) is responsible for listening to new content collection publishes (i.e. content package or story package) on the kafka NativeCMSPublicationEvents queue, and then transforming them into UPP format and then writing them to kafka CMSPublicationEvents queue.

## Contains Personal Data

No

## Contains Sensitive Data

No

## Dependencies

- kafka-proxy
- document-store-api

## Failover Architecture Type

ActiveActive

## Failover Process Type

FullyAutomated

## Failback Process Type

FullyAutomated

## Failover Details

The service is deployed in both Delivery clusters. The failover guide for the cluster is located here:
<https://github.com/Financial-Times/upp-docs/tree/master/failover-guides/delivery-cluster>

## Data Recovery Process Type

NotApplicable

## Data Recovery Details

The service does not store data, so it does not require any data recovery steps.

## Release Process Type

PartiallyAutomated

## Rollback Process Type

Manual

## Release Details

Manual failover is not needed when a new version of the service is deployed to production. But if you want to do one, here are are the instructions for the failover process: <https://github.com/Financial-Times/upp-docs/tree/master/failover-guides/delivery-cluster>

## Key Management Process Type

Manual

## Key Management Details

To access the service clients need to provide basic auth credentials.
To rotate credentials you need to login to a particular cluster and update varnish-auth secrets.

## Monitoring

Service in UPP K8S delivery clusters:

- Delivery-Prod-EU health: <https://upp-prod-delivery-eu.upp.ft.com/__health/__pods-health?service-name=methode-content-collection-mapper>
- Delivery-Prod-US health: <https://upp-prod-delivery-us.upp.ft.com/__health/__pods-health?service-name=methode-content-collection-mapper>

## First Line Troubleshooting

<https://github.com/Financial-Times/upp-docs/tree/master/guides/ops/first-line-troubleshooting>

## Second Line Troubleshooting

Please refer to the GitHub repository README for troubleshooting information.
