package you.shall.not.pass.filter.staticresource;

import you.shall.not.pass.domain.grant.AccessGrant;

public interface StaticResourceValidator {
    boolean isApplicable(String requestUri);
    AccessGrant requires();
    void setList();
}
