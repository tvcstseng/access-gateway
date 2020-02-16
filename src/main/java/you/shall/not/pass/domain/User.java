package you.shall.not.pass.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Builder
public class User {
    @Id
    String id;
    String userName;
    char[] level1Password;
    char[] level2Password;
}
