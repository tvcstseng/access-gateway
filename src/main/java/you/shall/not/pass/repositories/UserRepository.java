package you.shall.not.pass.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import you.shall.not.pass.domain.user.User;

public  interface UserRepository extends MongoRepository<User, String> {
}
