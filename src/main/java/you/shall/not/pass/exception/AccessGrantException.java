package you.shall.not.pass.exception;

import lombok.Getter;
import you.shall.not.pass.domain.grant.AccessGrant;


@Getter
public class AccessGrantException extends RuntimeException {

    private final AccessGrant required;

    public AccessGrantException(AccessGrant required, String message) {
        super(message);
        this.required = required;
    }

}
