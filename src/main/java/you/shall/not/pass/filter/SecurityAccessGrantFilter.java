package you.shall.not.pass.filter;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

import you.shall.not.pass.dto.CsrfViolation;
import you.shall.not.pass.exception.CsrfViolationException;
import you.shall.not.pass.service.CsrfCookieService;
import you.shall.not.pass.domain.grant.AccessGrant;
import you.shall.not.pass.domain.session.Session;
import you.shall.not.pass.dto.AccessViolation;
import you.shall.not.pass.exception.AccessGrantException;
import you.shall.not.pass.filter.staticresource.StaticResourceValidator;
import you.shall.not.pass.service.CookieService;
import you.shall.not.pass.service.SessionService;

@Component
@Order(1)
public class SecurityAccessGrantFilter implements Filter {

    public static final String SESSION_COOKIE = "GRANT";
    public static final String YOU_SHALL_NOT_PASS_FILTER_OVER_ME_AGAIN = "you.shall.not.pass.filter";

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAccessGrantFilter.class);

    @Autowired
    private Gson gson;

    @Autowired
    private CookieService cookieService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private List<StaticResourceValidator> resourcesValidators;

    @Autowired
    private CsrfCookieService csrfCookieService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (request.getAttribute(YOU_SHALL_NOT_PASS_FILTER_OVER_ME_AGAIN) == null) {
                shallNotPassLogic((HttpServletRequest) request);
            }
            request.setAttribute(YOU_SHALL_NOT_PASS_FILTER_OVER_ME_AGAIN, true);
            chain.doFilter(request, response);
        } catch (AccessGrantException age) {
            LOG.info("Access grant violation exception", age);
            processAccessGrantError((HttpServletResponse) response, age);
        } catch (CsrfViolationException cve) {
            LOG.info("Csrf violation exception", cve);
            processCsrfViolation((HttpServletResponse) response, cve);
        }
    }

    private void processCsrfViolation(HttpServletResponse response, CsrfViolationException e) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        CsrfViolation violation = CsrfViolation.builder()
                .message(e.getMessage())
                .build();

        writeResponse(response, gson.toJson(violation));
    }

    private void writeResponse(HttpServletResponse response, String message) throws IOException {
        try {
            PrintWriter out = response.getWriter();
            LOG.info("response message {}", message);
            out.print(message);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processAccessGrantError(HttpServletResponse response, AccessGrantException accessViolation) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());

        AccessViolation violation = AccessViolation.builder()
                .userMessage(accessViolation.getMessage())
                .requiredGrant(accessViolation.getRequired())
                .build();

        writeResponse(response, gson.toJson(violation));
    }

    private void shallNotPassLogic(HttpServletRequest request) {
        final String cookieValue = cookieService.getCookieValue( request, SESSION_COOKIE);
        final Optional<Session> sessionByToken = sessionService.findSessionByToken(cookieValue);
        final String requestedUri = request.getRequestURI();
        LOG.info("Incoming request {} with token {}", requestedUri, cookieValue);
        final AccessGrant grant = sessionByToken.map(Session::getGrant).orElse(null);
        LOG.info("user grant level {}", grant);
        final Optional<StaticResourceValidator> resourceValidator = getValidator(requestedUri);
        resourceValidator.ifPresent(validator -> {
            LOG.info("resource validator enforced {}", validator.requires());
            if (sessionService.isExpiredSession(sessionByToken)
                    || validator.requires().levelIsHigher(grant)) {
                throw new AccessGrantException(validator.requires(), "invalid access grant");
            }
            csrfCookieService.validateCsrfCookie(request);
        });
    }

    private Optional<StaticResourceValidator> getValidator(String requestedUri) {
       return resourcesValidators.stream().filter(staticResourceValidator
               -> staticResourceValidator.isApplicable(requestedUri)).findFirst();
    }

}
