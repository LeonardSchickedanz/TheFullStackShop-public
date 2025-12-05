package yorku.thefullstackshop.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import yorku.thefullstackshop.models.User;
import yorku.thefullstackshop.services.implementations.UserServiceImpl;
import yorku.thefullstackshop.services.interfaces.UserService;
import yorku.thefullstackshop.utils.ValidationUtil;

import java.io.IOException;

@WebServlet(name = "RegisterController", value = "/register")
public class RegisterController extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/register.jsp");
        rd.forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (ValidationUtil.isEmpty(firstName) || ValidationUtil.isEmpty(lastName) || ValidationUtil.isEmpty(email) || ValidationUtil.isEmpty(password) || ValidationUtil.isEmpty(confirmPassword)) {

            forwardWithError(request, response, "All fields are required.", firstName, lastName, email);
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            forwardWithError(request, response, "Please enter a valid email address.", firstName, lastName, email);
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            forwardWithError(request, response, "Password must be at least 6 characters and include one special character.", firstName, lastName, email);
            return;
        }

        if (!password.equals(confirmPassword)) {
            forwardWithError(request, response, "Passwords do not match.", firstName, lastName, email);
            return;
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);

        try {
            userService.registerUser(user);
            response.sendRedirect(request.getContextPath() + "/login?registered=success");

        } catch (RuntimeException e) {
            String errorMessage = "Registration failed due to a server error. Please try again.";

            if (e.getMessage() != null && e.getMessage().contains("Email is already taken")) {
                errorMessage = "Email is already taken.";
            }

            forwardWithError(request, response, errorMessage, firstName, lastName, email);
        }
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String error, String firstName, String lastName, String email) throws ServletException, IOException {

        request.setAttribute("error", error);
        request.setAttribute("firstName", firstName);
        request.setAttribute("lastName", lastName);
        request.setAttribute("email", email);

        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }
}