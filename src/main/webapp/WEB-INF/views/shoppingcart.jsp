<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Shopping Cart - TheFullStackShop</title>

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
                <c:if test="${not empty sessionScope.user}">
                    <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline-light">Profile</a>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-danger">Logout</a>
                </c:if>
                <c:if test="${empty sessionScope.user}">
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-light">Login</a>
                </c:if>
            </div>
        </div>
    </div>
</nav>

<div class="container mb-5">
    <h2 class="mb-4">Shopping Cart</h2>

    <c:if test="${not empty sessionScope.inventoryError}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <c:out value="${sessionScope.inventoryError}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <c:remove var="inventoryError" scope="session"/>
    </c:if>

    <c:if test="${not empty sessionScope.inventoryWarning}">
        <div class="alert alert-warning alert-dismissible fade show" role="alert">
            <strong>Inventory Warning:</strong> One or more items in your cart had inventory issues:
            <br>
            <c:out value="${sessionScope.inventoryWarning}" escapeXml="false"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <c:remove var="inventoryWarning" scope="session"/>
    </c:if>


    <c:set var="grandTotal" value="0"/>

    <c:choose>
        <c:when test="${empty cart or empty cart.cartItems}">
            <div class="text-center py-5">
                <i class="bi bi-cart-x display-1 text-muted"></i>
                <h3 class="mt-3">Your cart is empty</h3>
                <p class="text-muted">Looks like you haven't added anything to your cart yet.</p>
                <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary mt-3">Start Shopping</a>
            </div>
        </c:when>

        <c:otherwise>
            <div class="row">

                <div class="col-lg-8">
                    <div class="card shadow-sm border-0">
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                    <tr>
                                        <th scope="col" class="ps-4">Product</th>
                                        <th scope="col">Price</th>
                                        <th scope="col">Quantity</th>
                                        <th scope="col" class="text-end pe-4">Total</th>
                                        <th scope="col"></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${cart.cartItems}" var="item">
                                        <c:set var="lineTotal" value="${item.product.price * item.quantity}"/>
                                        <c:set var="grandTotal" value="${grandTotal + lineTotal}"/>

                                        <tr>
                                            <td class="ps-4">
                                                <div class="d-flex align-items-center">
                                                    <img src="${empty item.product.imgUrl ? 'https://placehold.co/100x100?text=Product' : pageContext.request.contextPath}${item.product.imgUrl}"
                                                         class="rounded me-3"
                                                         style="width: 60px; height: 60px; object-fit: contain; border: 1px solid #eee;">
                                                    <div>
                                                        <h6 class="mb-0">
                                                            <a href="${pageContext.request.contextPath}/product?id=${item.product.productId}"
                                                               class="text-decoration-none text-dark">
                                                                    ${item.product.name}
                                                            </a>
                                                        </h6>
                                                        <small class="text-muted">${item.product.brand.name}</small>
                                                    </div>
                                                </div>
                                            </td>

                                            <td>$ ${item.product.price}</td>

                                            <td style="width: 120px;">
                                                <form action="${pageContext.request.contextPath}/shoppingcart/update"
                                                      method="post" class="d-flex">
                                                    <input type="hidden" name="productId"
                                                           value="${item.product.productId}">
                                                    <input type="number" name="quantity" value="${item.quantity}"
                                                           min="1"
                                                           class="form-control form-control-sm text-center me-2">
                                                    <button type="submit" class="btn btn-sm btn-outline-secondary"
                                                            title="Update Quantity">
                                                        <i class="bi bi-arrow-clockwise"></i>
                                                    </button>
                                                </form>
                                            </td>

                                            <td class="text-end pe-4 fw-bold">
                                                $ <fmt:formatNumber value="${lineTotal}" type="number"
                                                                    minFractionDigits="2" maxFractionDigits="2"/>
                                            </td>

                                            <td class="text-end">
                                                <form action="${pageContext.request.contextPath}/shoppingcart/remove"
                                                      method="post">
                                                    <input type="hidden" name="productId"
                                                           value="${item.product.productId}">
                                                    <button type="submit" class="btn btn-sm btn-outline-danger"
                                                            title="Remove Item">
                                                        <i class="bi bi-trash"></i>
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <div class="mt-3">
                        <a href="${pageContext.request.contextPath}/catalog" class="btn btn-outline-dark">
                            <i class="bi bi-arrow-left"></i> Continue Shopping
                        </a>
                    </div>
                </div>

                <div class="col-lg-4 mt-4 mt-lg-0">
                    <div class="card shadow-sm border-0">
                        <div class="card-header bg-white border-bottom-0 pt-4 pb-0">
                            <h5 class="mb-0">Order Summary</h5>
                        </div>
                        <div class="card-body">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-muted">Subtotal</span>
                                <span class="fw-bold">$ <fmt:formatNumber value="${grandTotal}" type="number"
                                                                          minFractionDigits="2"
                                                                          maxFractionDigits="2"/></span>
                            </div>
                            <div class="d-flex justify-content-between mb-4">
                                <span class="text-muted">Shipping</span>
                                <span class="text-success">Free</span>
                            </div>
                            <hr>
                            <div class="d-flex justify-content-between mb-4">
                                <span class="h5">Total</span>
                                <span class="h5 text-primary">$ <fmt:formatNumber value="${grandTotal}" type="number"
                                                                                  minFractionDigits="2"
                                                                                  maxFractionDigits="2"/></span>
                            </div>

                            <a href="${pageContext.request.contextPath}/checkout" class="btn btn-dark w-100 py-2">
                                Proceed to Checkout
                            </a>
                        </div>
                    </div>
                </div>

            </div>
        </c:otherwise>
    </c:choose>
</div>

<footer class="py-4 bg-dark mt-auto">
    <div class="container">
        <p class="m-0 text-center text-white">Copyright &copy; The Full Stack Shop 2025</p>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>