<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Profile - TheFullStackShop</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
</head>
<body class="d-flex flex-column min-vh-100 bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">TheFullStackShop</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarContent">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/catalog">Home</a></li>
            </ul>

            <div class="d-flex gap-2">
                <span class="navbar-text text-white me-2">
                    Welcome, ${sessionScope.user.firstName}
                </span>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-danger btn-sm">Logout</a>
                <a href="${pageContext.request.contextPath}/shoppingcart" class="btn btn-warning btn-sm">
                    <i class="bi bi-cart-fill"></i> Cart
                </a>
            </div>
        </div>
    </div>
</nav>

<div class="container">
    <div class="row">

        <div class="col-md-4 mb-4">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white">
                    <h5 class="mb-0"><i class="bi bi-person-circle"></i> Personal Info</h5>
                </div>
                <div class="card-body text-center">
                    <img src="https://ui-avatars.com/api/?name=${sessionScope.user.firstName}+${sessionScope.user.lastName}&background=0D6EFD&color=fff&size=128"
                         class="rounded-circle mb-3" alt="Profile">

                    <h4>${sessionScope.user.firstName} ${sessionScope.user.lastName}</h4>
                    <p class="text-muted mb-4">${sessionScope.user.email}</p>

                    <hr>

                    <div class="text-start mb-4">
                        <h6 class="text-primary"><i class="bi bi-geo-alt-fill"></i> Shipping Address</h6>

                        <c:choose>
                            <c:when test="${not empty latestAddress}">
                                <p class="mb-0 fw-bold">
                                        ${latestAddress.street} ${latestAddress.streetNumber}
                                </p>
                                <p class="text-muted">
                                        ${latestAddress.postalCode} ${latestAddress.city}
                                </p>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted small">No address saved yet.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="text-start">
                        <h6 class="text-danger"><i class="bi bi-credit-card-fill"></i> Payment Info</h6>

                        <c:choose>
                            <c:when test="${not empty customerPayment}">
                                <p class="mb-0 fw-bold">
                                    Card: ${customerPayment.cardNumber}
                                </p>
                                <p class="mb-0 text-muted small">
                                    Expires: ${customerPayment.expiryDate}
                                </p>
                                <p class="mb-0 text-muted small">
                                    CVC/CVS: ${customerPayment.cvc}
                                </p>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted small">No payment details saved yet.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="d-grid gap-2 mt-4">
                        <a href="${pageContext.request.contextPath}/profile/edit" class="btn btn-outline-primary">
                            <i class="bi bi-pencil-square"></i> Edit Profile
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-8">
            <div class="card shadow-sm">
                <div class="card-header bg-dark text-white">
                    <h5 class="mb-0"><i class="bi bi-clock-history"></i> Order History</h5>
                </div>
                <div class="card-body p-0">

                    <c:choose>
                        <c:when test="${empty orders}">
                            <div class="p-5 text-center text-muted">
                                <i class="bi bi-bag-x display-4"></i>
                                <p class="mt-3">You haven't placed any orders yet.</p>
                                <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary">Start
                                    Shopping</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-hover table-striped mb-0">
                                    <thead class="table-light">
                                    <tr>
                                        <th>Order ID</th>
                                        <th>Date</th>
                                        <th>Total Amount</th>
                                        <th>Action</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${orders}" var="order">
                                        <tr>
                                            <td>#${order.orderId}</td>
                                            <td>${order.transactionTime}</td>
                                            <td class="fw-bold text-success">$ ${order.totalPrice}</td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/order?id=${order.orderId}&from=profile"
                                                   class="btn btn-sm btn-outline-dark">
                                                    Details
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>

                </div>
            </div>
        </div>
    </div>
</div>

<footer class="py-4 bg-dark mt-auto">
    <div class="container">
        <p class="m-0 text-center text-white">Copyright &copy; The Full Stack Shop 2025</p>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>