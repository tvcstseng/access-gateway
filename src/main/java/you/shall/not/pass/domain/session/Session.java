package you.shall.not.pass.domain.session;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import you.shall.not.pass.domain.grant.AccessGrant;

import java.util.Date;

@Builder
@Data
public class Session {
    @Id
    String sessionId;
    AccessGrant grant;
    String userId;
    String token;
    Date date;
}
