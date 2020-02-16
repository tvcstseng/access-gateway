package you.shall.not.pass.configuration;

import com.mongodb.MongoClient;
import cz.jirutka.spring.embedmongo.EmbeddedMongoFactoryBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;


@Configuration
public class MongoConfig extends AbstractMongoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private int port;

    @Value("${spring.data.mongodb.database}")
    private String name;


    @Override
    protected String getDatabaseName() {
        try {
            return name;
        } catch (Exception ex) {
            LOG.error("Error loading database properties", ex);
        }
        return null;
    }

    @Bean
    @Override
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }

    @Override
    public MongoClient mongoClient() {
        if (port == 0) {
            throw new RuntimeException("No port provided for mongo db, failed connection to db!");
        }
        if (host == null) {
            throw new RuntimeException("No url provided for mongo db, failed connection to db!");
        }

        return new MongoClient(host, port);
    }

    @Bean
    public void setupMongo()  {
        EmbeddedMongoFactoryBean mongo = new EmbeddedMongoFactoryBean();
        mongo.setBindIp(host);
        mongo.setPort(port);
    }

}
