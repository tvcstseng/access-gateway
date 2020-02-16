package you.shall.not.pass.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import you.shall.not.pass.dto.Access;
import you.shall.not.pass.service.SessionService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Controller
public class GateController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private Gson gson;

    @GetMapping({"/access"})
    public ResponseEntity<String> access(HttpServletRequest request, HttpServletResponse response) {
        Access.AccessBuilder builder = Access.builder();
        Optional<Cookie> sessionCookie = sessionService.grantSessionCookie();
        sessionCookie.ifPresent(cookie -> {
            response.addCookie(cookie);
            builder.authenticated(true);
        });
        return ResponseEntity.ok(gson.toJson(builder.build()));
    }

    @GetMapping({"/home"})
    public String hello() {
        return "angular-app";
    }

}
