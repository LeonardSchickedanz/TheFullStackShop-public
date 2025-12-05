package yorku.thefullstackshop.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.implementations.ShoppingCartServiceImpl;
import yorku.thefullstackshop.services.implementations.UserServiceImpl;
import yorku.thefullstackshop.services.interfaces.ShoppingCartService;
import yorku.thefullstackshop.services.interfaces.UserService;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "LoginController", value = "/login")
public class LoginController extends HttpServlet {

    private final UserService userService = new UserServiceImpl();
    private final ShoppingCartService cartService = new ShoppingCartServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/catalog");
            return;
        }

        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/login.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String redirectDest = request.getParameter("redirect");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Please enter both email and password.");
            request.setAttribute("email", email);
            request.setAttribute("redirect", redirectDest);
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        Optional<User> userOptional = userService.login(email, password);

        if (userOptional.isPresent()) {
            HttpSession session = request.getSession();
            User user = userOptional.get();
            session.setAttribute("user", user);

            cartService.mergeCarts(user.getUserId(), session.getId());

            if ("checkout".equals(redirectDest)) {
                response.sendRedirect(request.getContextPath() + "/checkout");
            } else {
                response.sendRedirect(request.getContextPath() + "/catalog");
            }

        } else {
            request.setAttribute("error", "Invalid email or password.");
            request.setAttribute("email", email);
            request.setAttribute("redirect", redirectDest);
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}