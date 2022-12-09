package io.nermdev.krpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KrpclientApplication {

    static final String TOPIC = "rx.csx.store.000.test";
    static final String BOOTSTRAP_SERVERS = "kafka.store.svc.cluster.local:9072";
    static final String CLIENT_ID = "android-generator";
    

    public static void main(String[] args) {
        SpringApplication.run(KrpclientApplication.class, args);
    }

}
