package you.shall.not.pass.service;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Component
public class CookieService {

    public String getCookieValue(HttpServletRequest req, String cookieName) {
        return Arrays.stream(req.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    public Cookie createCookie(String name, String token, int expireInSeconds) {
        Cookie cookie = new Cookie(name, token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(expireInSeconds);
        return cookie;
    }

}
