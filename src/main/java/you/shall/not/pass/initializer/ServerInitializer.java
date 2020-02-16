package you.shall.not.pass.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import you.shall.not.pass.domain.user.User;
import you.shall.not.pass.repositories.UserRepository;

import java.util.Optional;

@Component
public class ServerInitializer implements ApplicationRunner {

    private final UserRepository resp;
    private final PasswordEncoder passwordEncoder;

    private static final Logger LOG = LoggerFactory.getLogger(ServerInitializer.class);

    @Autowired
    public ServerInitializer(UserRepository resp, PasswordEncoder passwordEncoder) {
        this.resp = resp;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        try {
            User.UserBuilder builder = User.builder();

            builder.userName("test");
            builder.level1Password(passwordEncoder.encode("1234").toCharArray());
            builder.level2Password(passwordEncoder.encode("test").toCharArray());

            Example<User> example = Example.of(User.builder().userName("test").build());
            Optional<User> OptionalUser = resp.findOne(example);

            OptionalUser.ifPresent(user -> {
                builder.id(user.getId());
            });

            User saved = resp.save(builder.build());
            LOG.info("Setting sysadmin [" + saved.getId() + "] adding test user...");
        } catch (Exception ex) {
            LOG.info("Error running system init", ex);
            throw ex;
        }
    }
}