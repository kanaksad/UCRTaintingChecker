package foo.bar;

import edu.ucr.cs.riple.taint.ucrtainting.qual.*;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;

class Foo {

  String path;

  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    response.setContentType("text/html;charset=UTF-8");

    // :: error: (assignment)
    @RUntainted String param = request.getHeader("BenchmarkTest00175");
    // :: error: (assignment)
    @RUntainted File file = new File(path);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setContentType("text/html;charset=UTF-8");
    javax.servlet.http.Cookie userCookie =
        new javax.servlet.http.Cookie("BenchmarkTest00093", "ls");
    userCookie.setMaxAge(60 * 3); // Store cookie for 3 minutes
    userCookie.setSecure(true);
    // :: error: argument
    userCookie.setPath(request.getRequestURI());
    // :: error: argument
    userCookie.setDomain(new java.net.URL(request.getRequestURL().toString()).getHost());
    response.addCookie(userCookie);
    javax.servlet.RequestDispatcher rd =
        request.getRequestDispatcher("/cmdi-00/BenchmarkTest00093.html");
    rd.include(request, response);
  }
}