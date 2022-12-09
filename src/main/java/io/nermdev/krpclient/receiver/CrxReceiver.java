package io.nermdev.krpclient.receiver;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.receiver.internals.ConsumerFactory;
import reactor.kafka.receiver.internals.DefaultKafkaReceiver;

import java.util.*;

@RestController
@RequestMapping(path = "/v1/api")
public class CrxReceiver {
    private static final String TOPIC_PREFIX = "rx.crx.store.";

    @GetMapping(value = "/init", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getLaneEvents(@RequestParam(name = "laneId") String laneId) {
        System.out.println("INIT received: " + laneId);
        final Map<String, Object> configProperties = receiverConfig(laneId);
        ReceiverOptions<String, String> options = ReceiverOptions.create(configProperties);

        // lazily create consumer on the fly (.subscribe())
        final DefaultKafkaReceiver<String, String> crxReceiver = new DefaultKafkaReceiver<>(
                ConsumerFactory.INSTANCE,
                options.subscription(Collections.singleton(TOPIC_PREFIX+laneId+".test"))
        );
        final Flux<ReceiverRecord<String, String>> crxInboundFlux = crxReceiver.receive();
        return crxInboundFlux
                .map(rr -> {
                    rr.receiverOffset().acknowledge();
                    return rr.value();
                })
                .log();
    }


    private Map<String, Object> receiverConfig(final String laneId) {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka.store.svc.cluster.local:9072",
                ConsumerConfig.GROUP_ID_CONFIG, "rxcrx." + laneId,
                ConsumerConfig.CLIENT_ID_CONFIG, "rxcrx." + laneId + "." + UUID.randomUUID(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                "security.protocol", "SSL",
                SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/tls/keystore.p12",
                SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "mystorepassword",
                SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/tls/truststore.p12",
                SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "mystorepassword"
        );
    }

}
