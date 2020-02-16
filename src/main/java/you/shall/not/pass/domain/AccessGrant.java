package you.shall.not.pass.domain;

import java.util.Arrays;
import java.util.Optional;

public enum AccessGrant {
    Level1(1),
    Level2(2);

    private int grant;

    AccessGrant(int grant) {
        this.grant = grant;
    }

    public static Optional<AccessGrant> find(String lvl) {
        return Arrays.stream(AccessGrant.values()).filter(gateKeeperGrant ->
                gateKeeperGrant.grant == Integer.valueOf(lvl)).findFirst();
    }

    public boolean levelIsHigher(AccessGrant sessionAccessGrant) {
        return sessionAccessGrant == null
                || this.grant > sessionAccessGrant.grant;
    }
}
