package org.open.oauth2.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("testing")
public class TestingEndPoint {

    public static class Output {
        public String one = "one";
        public Date date = new Date();
    }

    @GetMapping(path="/getMapping")
    public Output getMapping() {
       return new Output();
    }

    @GetMapping(path="/redirect")
    public String redirect(HttpServletResponse res) throws IOException {
        res.sendRedirect("http://localhost:8081/testing/something");
        return "test";
    }

    @GetMapping(path="/something")
    public String something() {
        return "Something is here";
    }
}


