<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Customer ${customer.userId}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
</head>
<body class="bg-light d-flex flex-column min-vh-100">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/admin/sales">TheFullStackShop Admin</a>
    </div>
</nav>

<div class="container py-5">
    <div class="row">
        <div class="col-12 mb-4">
            <a href="${pageContext.request.contextPath}/${backUrl}" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> Back to Customer Management
            </a>
            <h1 class="mt-3">Edit Customer Profile: ${customer.firstName} ${customer.lastName}</h1>
            <p class="text-muted">ID: #${customer.userId} | Email: ${customer.email}</p>
            <hr>
        </div>

        <div class="col-md-5">
            <div class="card shadow-sm mb-4">

                <div class="card-header bg-primary text-white">
                    <h5 class="mb-0"><i class="bi bi-person-lines-fill"></i> User Data & Address</h5>
                </div>
                <div class="card-body">

                    <form action="${pageContext.request.contextPath}/${formAction}" method="POST">
                        <input type="hidden" name="userId" value="${customer.userId}">
                        <input type="hidden" name="addressId" value="${customerAddress.addressId}">
                        <c:if test="${not empty customerPayment}">
                            <input type="hidden" name="paymentId" value="${customerPayment.paymentInfoId}">
                        </c:if>

                        <h6 class="text-primary mb-3">Personal Information</h6>
                        <div class="mb-3">
                            <label for="firstName" class="form-label">First Name</label>
                            <input type="text" class="form-control" id="firstName" name="firstName"
                                   value="${customer.firstName}" required>
                        </div>
                        <div class="mb-3">
                            <label for="lastName" class="form-label">Last Name</label>
                            <input type="text" class="form-control" id="lastName" name="lastName"
                                   value="${customer.lastName}" required>
                        </div>
                        <div class="mb-4">
                            <label for="email" class="form-label">Email</label>
                            <input type="email" class="form-control" id="email" name="email" value="${customer.email}"
                                   required>
                        </div>

                        <h6 class="text-primary mb-3 mt-4">Address</h6>
                        <div class="row">
                            <div class="col-9 mb-3">
                                <label for="street" class="form-label">Street</label>
                                <input type="text" class="form-control" id="street" name="street"
                                       value="${customerAddress.street}">
                            </div>
                            <div class="col-3 mb-3">
                                <label for="streetNumber" class="form-label">Number</label>
                                <input type="text" class="form-control" id="streetNumber" name="streetNumber"
                                       value="${customerAddress.streetNumber}">
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-3 mb-3">
                                <label for="postalCode" class="form-label">Postal Code</label>
                                <input type="text" class="form-control" id="postalCode" name="postalCode"
                                       value="${customerAddress.postalCode}">
                            </div>
                            <div class="col-5 mb-3">
                                <label for="city" class="form-label">City</label>
                                <input type="text" class="form-control" id="city" name="city"
                                       value="${customerAddress.city}">
                            </div>
                            <div class="col-4 mb-3">
                                <label for="province" class="form-label">Province</label>
                                <input type="text" class="form-control" id="province" name="province"
                                       value="${customerAddress.province}" required>
                            </div>
                        </div>
                        <div class="mb-4">
                            <label for="country" class="form-label">Country</label>
                            <input type="text" class="form-control" id="country" name="country"
                                   value="${customerAddress.country}">
                        </div>

                        <h6 class="text-danger mb-3 mt-4">Payment Information</h6>
                        <div class="mb-3">
                            <label for="cardNumber" class="form-label">Card Number</label>
                            <input type="text" class="form-control" id="cardNumber" name="cardNumber"
                                   value="${customerPayment.cardNumber}">
                        </div>

                        <div class="row">
                            <div class="col-6 mb-3">
                                <label for="expiryDate" class="form-label">Expiry Date (MM/YY)</label>
                                <input type="text" class="form-control" id="expiryDate" name="expiryDate"
                                       value="${customerPayment.expiryDate}" placeholder="MM/YY">
                            </div>
                            <div class="col-6 mb-3">
                                <label for="cvc" class="form-label">CVC/CVS (Security Code)</label>
                                <input type="text" class="form-control" id="cvc" name="cvc"
                                       value="${customerPayment.cvc}" placeholder="123">
                            </div>
                        </div>

                        <button type="submit" class="btn bg-primary mt-4 w-100">

                            <i class="bi bi-save"></i> Save Changes
                        </button>
                    </form>
                </div>
            </div>
        </div>

        <div class="col-md-7">
            <div class="card shadow-sm mb-4">
                <div class="card-header bg-primary text-white">
                    <h5 class="mb-0"><i class="bi bi-clock-history"></i> Order History</h5>
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${not empty customerOrders}">
                            <div class="table-responsive">
                                <table class="table table-striped align-middle">
                                    <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Date</th>
                                        <th class="text-end">Total</th>
                                        <th class="text-center">Action</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${customerOrders}" var="order">
                                        <tr>
                                            <td>#${order.orderId}</td>
                                            <td>
                                                <fmt:parseDate value="${order.transactionTime}"
                                                               pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedDate"
                                                               type="both"/>
                                                <fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy HH:mm"/>
                                            </td>
                                            <td class="text-end fw-bold">
                                                $ <fmt:formatNumber value="${order.totalPrice}" type="number"
                                                                    minFractionDigits="2" maxFractionDigits="2"/>
                                            </td>
                                            <td class="text-center">
                                                <a href="${pageContext.request.contextPath}/admin/order?id=${order.orderId}"
                                                   class="btn btn-sm btn-outline-primary">
                                                    Details
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-light text-center" role="alert">
                                This customer has not placed any orders yet.
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
        <p class="m-0 text-center text-white">Copyright &copy; TheFullStackShop 2025</p>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>