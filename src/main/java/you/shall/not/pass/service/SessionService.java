package you.shall.not.pass.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import you.shall.not.pass.domain.grant.AccessGrant;
import you.shall.not.pass.domain.session.Session;
import you.shall.not.pass.domain.user.User;
import you.shall.not.pass.repositories.SessionRepository;

import javax.servlet.http.Cookie;
import javax.xml.bind.DatatypeConverter;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

import static you.shall.not.pass.filter.SecurityAccessGrantFilter.SESSION_COOKIE;

@Service
public class SessionService {

    private static final Logger LOG = LoggerFactory.getLogger(SessionService.class);

    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private UserService userService;
    @Value("${session.expiry.seconds}")
    private int sessionExpirySeconds;


    private final SecureRandom secureRandom = new SecureRandom();

    private String generateToken() {
        byte[] buffer = new byte[50];
        this.secureRandom.nextBytes(buffer);
        return DatatypeConverter.printHexBinary(buffer);
    }

    public Optional<Session> findSessionByToken(String token) {
        Example<Session> example = Example.of(Session.builder()
                .token(token).build());
        return sessionRepository.findOne(example);
    }

    public boolean isExpiredSession(Optional<Session> optionalSession) {
        return !optionalSession.isPresent() || optionalSession.filter(session -> LocalDateTime.now()
                .isAfter(asLocalDateTime(session.getDate()))).isPresent();
    }

    private Optional<Session> findLastKnownSession(User user, AccessGrant grant) {
        Example<Session> example = Example.of(Session.builder()
                .userId(user.getId()).grant(grant).build());
        return sessionRepository.findAll(example).stream()
                .sorted(Comparator.comparing(Session::getDate,
                Comparator.nullsLast(Comparator.reverseOrder()))).findFirst();
    }

    public Optional<Cookie> grantSessionCookie() {
        final String username = LogonUserService.getCurrentUser().orElseThrow(()
                -> new RuntimeException("unknown user requesting session!"));
        final AccessGrant grant = LogonUserService.getCurrentAccessLevel().orElseThrow(()
                -> new RuntimeException("Invalid user access grant!"));

        final User user = userService.getUserByName(username);
        Optional<Session> priorSession = findLastKnownSession(user, grant);

        boolean expired = isExpiredSession(priorSession);
        if (!expired) {
            LOG.info("returning old session cookie");
            return createOldSessionCookie(priorSession);
        }

        LOG.info("returning new session cookie");
        return createNewSessionCookie(grant, user);
    }

    private Optional<Cookie> createOldSessionCookie(Optional<Session> priorSession) {
        Session session = priorSession.orElseThrow(()
                -> new RuntimeException("This should never happen you may not pass!"));
        LocalDateTime cookieDate = asLocalDateTime(session.getDate());
        long diff = LocalDateTime.now().until(cookieDate, ChronoUnit.SECONDS);
        return createCookie(session.getToken(), (int) diff);
    }

    private Optional<Cookie> createNewSessionCookie(AccessGrant grant, User user) {
        final String token = generateToken();

        Session session = Session.builder()
                .date(asDate(LocalDateTime.now().plusSeconds(sessionExpirySeconds)))
                .grant(grant)
                .token(token)
                .userId(user.getId())
                .build();

        sessionRepository.save(session);
        return createCookie(token, sessionExpirySeconds);
    }

    private Optional<Cookie> createCookie(String token, int expireInSeconds) {
        Cookie cookie = new Cookie(SESSION_COOKIE, token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(expireInSeconds);
        //cookie.setSecure(true);
        return Optional.of(cookie);
    }

    private static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}
