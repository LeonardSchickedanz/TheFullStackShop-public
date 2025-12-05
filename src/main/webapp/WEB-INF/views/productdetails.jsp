<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${product.name} - TheFullStackShop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
</head>
<body class="d-flex flex-column min-vh-100 bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">TheFullStackShop</a>
        <div class="collapse navbar-collapse">
            <ul class="navbar-nav me-auto">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/catalog">Back to
                    Catalog</a></li>
            </ul>
            <div class="d-flex gap-2 align-items-center">
                <c:choose>
                    <c:when test="${not empty sessionScope.user}">
                        <span class="navbar-text text-white me-2">Hi, ${sessionScope.user.firstName}</span>
                        <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline-light btn-sm">Profile</a>
                        <a href="${pageContext.request.contextPath}/logout" class="btn btn-danger btn-sm">Logout</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-light">Login</a>
                    </c:otherwise>
                </c:choose>
                <a href="${pageContext.request.contextPath}/shoppingcart" class="btn btn-warning">
                    <i class="bi bi-cart-fill"></i> Cart
                    <span id="cartBadge" class="badge bg-dark text-white ms-1 rounded-pill">
                        ${cartCount != null ? cartCount : 0}
                    </span>
                </a>
            </div>
        </div>
    </div>
</nav>

<div class="container py-5">
    <div class="row gx-5 align-items-center">
        <div class="col-md-6 mb-5 mb-md-0">
            <div class="card shadow-sm border-0 p-3">
                <img class="card-img-top rounded"
                     src="${empty product.imgUrl ? 'https://placehold.co/600x400?text=Product+Image' : pageContext.request.contextPath}${product.imgUrl}"
                     alt="${product.name}"/>
            </div>
        </div>

        <div class="col-md-6">
            <h1 class="display-5 fw-bolder">${product.name}</h1>
            <div class="fs-5 mb-3"><span class="fw-bold text-primary">$ ${product.price}</span></div>
            <p class="lead mb-4">${product.description}</p>

            <div class="d-flex mb-4">
                <div class="me-4">
                    <strong>Brand:</strong> ${product.brand.name}<br>
                    <strong>Category:</strong> ${product.category.name}
                </div>
                <div>
                    <strong>Availability:</strong>
                    <span id="stockDisplay" data-total-stock="${product.quantity}">
                        <c:choose>
                            <c:when test="${product.quantity > 0}">
                                <span class="text-success fw-bold"><i class="bi bi-check-circle"></i> Checking...</span>
                            </c:when>
                            <c:otherwise>
                                <span class="text-danger fw-bold"><i class="bi bi-x-circle"></i> Out of Stock</span>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>

            <form id="addToCartForm">
                <div class="d-flex align-items-center mb-2">
                    <input type="hidden" name="productId" value="${product.productId}">

                    <input class="form-control text-center me-3"
                           id="inputQuantity"
                           name="quantity"
                           type="number"
                           value="1"
                           min="1"
                           max="${product.quantity}"
                           style="max-width: 5rem"
                    ${product.quantity <= 0 ? 'disabled' : ''} />

                    <button class="btn btn-outline-dark flex-shrink-0" type="submit"
                            id="submitButton" ${product.quantity <= 0 ? 'disabled' : ''}>
                        <i class="bi-cart-fill me-1"></i>
                        ${product.quantity <= 0 ? 'Sold Out' : 'Add to cart'}
                    </button>
                </div>
                <div id="feedbackMessage" class="alert alert-danger d-none" role="alert"></div>
            </form>

        </div>
    </div>
</div>

<footer class="py-4 bg-dark mt-auto">
    <div class="container"><p class="m-0 text-center text-white">Copyright &copy; The Full Stack Shop 2025</p></div>
</footer>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const form = document.getElementById("addToCartForm");
        const quantityInput = document.getElementById("inputQuantity");
        const submitButton = document.getElementById("submitButton");
        const cartBadge = document.getElementById("cartBadge");
        const stockDisplay = document.getElementById("stockDisplay");
        const feedbackMessage = document.getElementById("feedbackMessage");

        const totalStock = parseInt(stockDisplay.getAttribute('data-total-stock')) || 0;
        const productId = form ? form.querySelector('input[name="productId"]').value : 0;

        const addApiPath = "${pageContext.request.contextPath}/api/shoppingcart/add";
        const statusApiPath = "${pageContext.request.contextPath}/api/shoppingcart/product/" + productId + "/status";

        if (productId > 0) {
            fetch(statusApiPath)
                .then(r => r.json())
                .then(data => {
                    updateAvailabilityUI(data.inCart);
                })
                .catch(err => console.error("Could not fetch cart status", err));
        }

        function updateAvailabilityUI(inCart) {
            const availableToBuy = totalStock - inCart;

            quantityInput.max = availableToBuy;
            if (parseInt(quantityInput.value) > availableToBuy) {
                quantityInput.value = availableToBuy > 0 ? 1 : 0;
            }

            if (availableToBuy <= 0) {
                stockDisplay.innerHTML = '<span class="text-danger fw-bold"><i class="bi bi-x-circle"></i> Max limit reached (You have ' + inCart + ')</span>';
                submitButton.disabled = true;
                quantityInput.disabled = true;
                submitButton.innerText = "Max Reached";
                submitButton.classList.remove("btn-outline-dark");
                submitButton.classList.add("btn-secondary");
            } else if (availableToBuy < 5) {
                stockDisplay.innerHTML = '<span class="text-warning fw-bold"><i class="bi bi-exclamation-circle"></i> Low Stock (' + availableToBuy + ' left, ' + inCart + ' in cart)</span>';
            } else {
                stockDisplay.innerHTML = '<span class="text-success fw-bold"><i class="bi bi-check-circle"></i> In Stock (' + availableToBuy + ' available)</span>';
            }
        }

        function showMessage(msg, isError) {
            feedbackMessage.innerText = msg;
            feedbackMessage.classList.remove("d-none", "alert-danger", "alert-success");
            feedbackMessage.classList.add(isError ? "alert-danger" : "alert-success");
        }

        function hideMessage() {
            feedbackMessage.classList.add("d-none");
        }

        if (form) {
            form.addEventListener("submit", function (event) {
                event.preventDefault();
                hideMessage();

                const quantityToAdd = parseInt(quantityInput.value);

                if (submitButton) {
                    const originalContent = submitButton.innerHTML;
                    submitButton.disabled = true;
                    submitButton.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

                    const formData = new URLSearchParams();
                    formData.append('productId', productId);
                    formData.append('quantity', quantityToAdd);

                    fetch(addApiPath, {
                        method: 'POST',
                        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                        body: formData
                    })
                        .then(async response => {
                            if (response.ok) return response.json();
                            const errData = await response.json();
                            throw new Error(errData.error || `Error ${response.status}`);
                        })
                        .then(data => {
                            if (cartBadge && data.count !== undefined) cartBadge.innerText = data.count;

                            fetch(statusApiPath)
                                .then(r => r.json())
                                .then(statusData => updateAvailabilityUI(statusData.inCart));

                            submitButton.classList.remove("btn-outline-dark");
                            submitButton.classList.add("btn-success");
                            submitButton.innerHTML = '<i class="bi-check-circle-fill me-1"></i> Added';

                            setTimeout(() => {
                                submitButton.classList.remove("btn-success");
                                submitButton.classList.add("btn-outline-dark");
                                submitButton.innerHTML = originalContent;
                                if (submitButton.innerText === "Max Reached") submitButton.disabled = true;
                                else submitButton.disabled = false;
                            }, 1000);
                        })
                        .catch(error => {
                            showMessage(error.message, true);
                            submitButton.disabled = false;
                            submitButton.innerHTML = originalContent;
                        });
                }
            });
        }
    });
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>