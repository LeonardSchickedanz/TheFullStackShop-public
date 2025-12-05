<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>
        <c:choose>
            <c:when test="${isConfirmation}">Order Confirmed</c:when>
            <c:otherwise>Order Details</c:otherwise>
        </c:choose>
    </title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
</head>
<body class="bg-light d-flex flex-column min-vh-100">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">TheFullStackShop</a>
    </div>
</nav>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow-sm border-0">

                <c:if test="${isConfirmation}">
                    <div class="card-header bg-success text-white text-center py-4">
                        <i class="bi bi-check-circle-fill display-1"></i>
                        <h2 class="mt-3">Thank You!</h2>
                        <p class="mb-0">Your order has been placed successfully.</p>
                    </div>
                </c:if>

                <c:if test="${not isConfirmation}">
                    <div class="card-header bg-primary text-white">
                        <h4 class="mb-0"><i class="bi bi-box-seam"></i> Order #${order.orderId}</h4>
                    </div>
                </c:if>

                <div class="card-body p-4">

                    <div class="row mb-4">
                        <div class="col-sm-6">
                            <h6 class="text-muted">Order ID:</h6>
                            <p class="fw-bold">#${order.orderId}</p>
                        </div>
                        <div class="col-sm-6 text-sm-end">
                            <h6 class="text-muted">Date:</h6>
                            <p>${order.transactionTime}</p>
                        </div>
                    </div>

                    <hr class="mt-0">

                    <div class="row">
                        <div class="col-md-6">
                            <h5 class="mb-3">Customer Details</h5>
                            <div class="mb-4">
                                <p class="mb-1">
                                    Name: <strong>${order.user.firstName} ${order.user.lastName}</strong>
                                </p>
                                <p class="mb-1">
                                    Email: <a href="mailto:${order.user.email}">${order.user.email}</a>
                                </p>
                            </div>
                        </div>

                        <div class="col-md-6 text-start">
                            <c:if test="${not empty order.address}">
                                <h5 class="mb-3">Shipping Address</h5>
                                <div class="mb-4">
                                    <p class="mb-1">
                                        <strong>${order.address.street} ${order.address.streetNumber}</strong>
                                    </p>
                                    <p class="mb-1">
                                            ${order.address.postalCode} ${order.address.city}
                                    </p>
                                    <p class="mb-1 text-muted">
                                            ${order.address.province}, ${order.address.country}
                                    </p>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <hr>

                    <h5 class="mb-3">Items</h5>
                    <div class="table-responsive">
                        <table class="table align-middle">
                            <thead class="table-light">
                            <tr>
                                <th>Product</th>
                                <th class="text-center">Qty</th>
                                <th class="text-end">Price</th>
                                <th class="text-end">Total</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${order.orderLines}" var="line">
                                <tr>
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <c:if test="${not empty line.product.imgUrl}">
                                                <img src="${pageContext.request.contextPath}${line.product.imgUrl}"
                                                     alt="" style="width: 40px; height: 40px; object-fit: contain;"
                                                     class="me-2 rounded border">
                                            </c:if>
                                            <div>
                                                <span class="fw-bold">${line.product.name}</span><br>
                                                <small class="text-muted">${line.product.brand.name}</small>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="text-center">${line.quantity}</td>
                                    <td class="text-end">$ ${line.unitPrice}</td>
                                    <td class="text-end fw-bold">$ <fmt:formatNumber
                                            value="${line.unitPrice * line.quantity}" type="number"
                                            minFractionDigits="2" maxFractionDigits="2"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                            <tfoot class="table-light">
                            <tr>
                                <td colspan="3" class="text-end fw-bold">Total Amount:</td>
                                <td class="text-end fw-bold text-primary fs-5">$ <fmt:formatNumber
                                        value="${order.totalPrice}" type="number" minFractionDigits="2"
                                        maxFractionDigits="2"/></td>
                            </tr>
                            </tfoot>
                        </table>
                    </div>
                    <div class="d-flex justify-content-between mt-4">
                        <c:choose>
                            <c:when test="${isConfirmation}">
                                <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline-dark">View
                                    Order History</a>
                                <a href="${pageContext.request.contextPath}/catalog" class="btn btn-success">Continue
                                    Shopping</a>
                            </c:when>
                            <c:otherwise>
                                <c:choose>
                                    <c:when test="${not empty returnUrl}">
                                        <a href="${returnUrl}" class="btn btn-outline-secondary">
                                            <i class="bi bi-arrow-left"></i> ${returnLabel}
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/profile"
                                           class="btn btn-outline-secondary">
                                            <i class="bi bi-arrow-left"></i> Back to Profile
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </div>
            </div>
        </div>
    </div>
</div>

<footer class="py-4 bg-dark mt-auto">
    <div class="container">
        <p class="m-0 text-center text-white">Copyright &copy; TheFullStackShop 2025</p>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>