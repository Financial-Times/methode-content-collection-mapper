FROM openjdk:8u212-jdk-alpine3.9

COPY . /methode-content-collection-mapper

ARG SONATYPE_USER
ARG SONATYPE_PASSWORD
ARG GIT_TAG

ENV MAVEN_HOME=/root/.m2
ENV TAG=$GIT_TAG

RUN apk --update add git maven curl \
 && mkdir $MAVEN_HOME \
 && curl -v -o $MAVEN_HOME/settings.xml "https://raw.githubusercontent.com/Financial-Times/nexus-settings/master/public-settings.xml" \
 && cd methode-content-collection-mapper \
 && HASH=$(git log -1 --pretty=format:%H) \
 && TAG=$(git tag --sort=committerdate | tail -1) \
 && VERSION=${TAG:-untagged} \
 && mvn versions:set -DnewVersion=$VERSION \
 && mvn install -Dbuild.git.revision=$HASH -Djava.net.preferIPv4Stack=true \
 && rm -f target/methode-content-collection-mapper-*sources.jar \
 && mv target/methode-content-collection-mapper-*.jar /methode-content-collection-mapper.jar \
 && mv methode-content-collection-mapper.yaml /config.yaml \
 && apk del git maven \
 && rm -rf /var/cache/apk/* \
 && rm -rf $MAVEN_HOME/*

EXPOSE 8080 8081

CMD exec java $JAVA_OPTS \
     -Ddw.server.applicationConnectors[0].port=8080 \
     -Ddw.server.adminConnectors[0].port=8081 \
     -Ddw.consumer.messageConsumer.queueProxyHost=http://$KAFKA_PROXY_URL \
     -Ddw.producer.messageProducer.proxyHostAndPort=$KAFKA_PROXY_URL \
     -Ddw.documentStoreApi.endpointConfiguration.primaryNodes=$DOCUMENT_STORE_API_URL \
     -Ddw.logging.appenders[0].logFormat="%-5p [%d{ISO8601, GMT}] %c: %X{transaction_id} %replace(%m%n[%thread]%xEx){'\n', '|'}%nopex%n" \
     -jar methode-content-collection-mapper.jar server config.yaml
