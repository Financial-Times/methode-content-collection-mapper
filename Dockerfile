FROM up-registry.ft.com/coco/dropwizardbase

COPY . /

RUN apk --update add git \
 && HASH=$(git log -1 --pretty=format:%H) \
 && BUILD_NUMBER=$(cat ../buildnum.txt) \
 && BUILD_URL=$(cat ../buildurl.txt) \
 && mvn install -Dbuild.git.revision=$HASH -Dbuild.git.revision=$HASH -Dbuild.number=$BUILD_NUMBER -Dbuild.url=$BUILD_URL -Djava.net.preferIPv4Stack=true \
 && rm -f target/methode-story-package-mapper-*sources.jar \
 && mv target/methode-story-package-mapper-*.jar /methode-story-package-mapper.jar \
 && mv methode-story-package-mapper.yaml /config.yaml \
 && apk del git \
 && rm -rf /var/cache/apk/* \
 && rm -rf /root/.m2/*

EXPOSE 8080 8081

CMD exec java $JAVA_OPTS \
     -Ddw.server.applicationConnectors[0].port=8080 \
     -Ddw.server.adminConnectors[0].port=8081 \
     -Ddw.consumer.messageConsumer.queueProxyHost=http://$VULCAN_HOST \
     -Ddw.producer.messageProducer.proxyHostAndPort=$VULCAN_HOST \
     -Ddw.logging.appenders[0].logFormat="%-5p [%d{ISO8601, GMT}] %c: %X{transaction_id} %replace(%m%n[%thread]%xEx){'\n', '|'}%nopex%n" \
     -jar methode-story-package-mapper.jar server config.yaml