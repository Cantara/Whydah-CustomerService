package net.whydah.crmservice.verification;

import com.google.inject.Singleton;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.Map;

@Singleton
public class ActiveVerificationCache {

    private static final Logger log = LoggerFactory.getLogger(ActiveVerificationCache.class);

    Map<String, String> userpinmap;
    Map<String, String> emailTokenMap;
    private String gridPrefix;
    private String hazelcastConfigFilename;

    public ActiveVerificationCache(String hazelcastConfigFilename, String gridPrefix){
        this.gridPrefix = gridPrefix;
        this.hazelcastConfigFilename = hazelcastConfigFilename;
        if (userpinmap == null || emailTokenMap == null) {
            init();
        }
    }

    private void init() {
        log.info("Loading hazelcast configuration from :" + hazelcastConfigFilename);
        Config hazelcastConfig = new Config();
        if (hazelcastConfigFilename != null && hazelcastConfigFilename.length() > 10) {
            try {
                hazelcastConfig = new XmlConfigBuilder(hazelcastConfigFilename).build();
                log.info("Loading hazelcast configuration from :" + hazelcastConfigFilename);
            } catch (FileNotFoundException notFound) {
                log.error("Error - not able to load hazelcast.xml configuration.  Using embedded as fallback");
            }
        }
        hazelcastConfig.setProperty("hazelcast.logging.type", "slf4j");
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastConfig);
        userpinmap = hazelcastInstance.getMap(gridPrefix+"CRM_userpinmap");
        log.info("Connecting to map {}",gridPrefix+"CRM_userpinmap");

        emailTokenMap = hazelcastInstance.getMap(gridPrefix+"CRM_emailtokenmap");
        log.info("Connecting to map {}", gridPrefix + "CRM_emailtokenmap");
    }


    public String addToken(String email, String token) {
        return emailTokenMap.put(email.toLowerCase(), token);
    }

    public boolean tokenExists(String email) {
        return emailTokenMap.containsKey(email.toLowerCase());
    }

    public String useToken(String email) {
        return emailTokenMap.remove(email.toLowerCase());
    }

    public String addPin(String phoneNo, String generatedPin) {
        return userpinmap.put(phoneNo, generatedPin);
    }

    public boolean pinExists(String phoneNo) {
        return userpinmap.containsKey(phoneNo);
    }

    public String usePin(String phoneNo) {
        return userpinmap.remove(phoneNo);
    }
}