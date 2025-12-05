<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - TheFullStackShop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card shadow-lg">
                <div class="card-header bg-dark text-white">
                    <h4 class="mb-0">Sign In</h4>
                </div>
                <div class="card-body">

                    <% String registered = request.getParameter("registered"); %>
                    <% if ("success".equals(registered)) { %>
                    <div class="alert alert-success" role="alert">
                        Registration successful! Please log in.
                    </div>
                    <% } %>

                    <% String error = (String) request.getAttribute("error"); %>
                    <% if (error != null) { %>
                    <div class="alert alert-danger" role="alert">
                        <%= error %>
                    </div>
                    <% } %>

                    <form action="login" method="post">
                        <input type="hidden" name="redirect"
                               value="${requestScope.redirect != null ? requestScope.redirect : param.redirect}">

                        <div class="mb-3">
                            <label for="email" class="form-label">Email Address</label>
                            <%
                                String emailValue = (String) request.getAttribute("email");
                                if (emailValue == null) {
                                    emailValue = request.getParameter("email");
                                }
                                if (emailValue == null) {
                                    emailValue = "";
                                }
                            %>
                            <input type="email" name="email" id="email" class="form-control"
                                   value="<%= emailValue %>" required>
                        </div>

                        <div class="mb-4">
                            <label for="password" class="form-label">Password</label>
                            <input type="password" name="password" id="password" class="form-control" required>
                        </div>

                        <button type="submit" class="btn btn-primary w-100">Sign In</button>
                    </form>
                </div>
                <div class="card-footer text-center">
                    Don't have an account? <a href="register">Create one</a>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>