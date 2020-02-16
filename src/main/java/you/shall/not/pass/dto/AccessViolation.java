package you.shall.not.pass.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import you.shall.not.pass.domain.grant.AccessGrant;

@Getter
@Setter
@Builder
public class AccessViolation {
    private AccessGrant requiredGrant;
    private String userMessage;
}
