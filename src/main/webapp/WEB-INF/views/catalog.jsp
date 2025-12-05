<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TheFullStackShop - Catalog</title>
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
                <li class="nav-item"><a class="nav-link active" href="">Home</a></li>
            </ul>

            <div class="d-flex gap-2 align-items-center">
                <c:choose>
                    <c:when test="${not empty sessionScope.user}">
                        <span class="navbar-text text-white me-2">
                            Hi, ${sessionScope.user.firstName}
                        </span>

                        <c:if test="${sessionScope.user.role.roleId == 1}">
                            <a href="${pageContext.request.contextPath}/admin/sales" class="btn btn-danger btn-sm">Admin
                                Dashboard</a>
                        </c:if>

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

<div class="container">
    <div class="row">
        <div class="col-lg-3 mb-4">
            <div class="card shadow-sm border-0">
                <div class="card-header bg-white fw-bold">Filters & Search</div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/catalog" method="GET">

                        <div class="mb-3">
                            <label class="form-label small text-muted">Search</label>
                            <input type="text" name="search" class="form-control" placeholder="Keyword..."
                                   value="${selectedSearch}">
                        </div>

                        <div class="mb-3">
                            <label class="form-label small text-muted">Category</label>
                            <select name="category" class="form-select">
                                <option value="">All Categories</option>
                                <c:forEach items="${categories}" var="cat">
                                    <option value="${cat.categoryId}" ${cat.categoryId == selectedCategory ? 'selected' : ''}>
                                            ${cat.name}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label class="form-label small text-muted">Brand</label>
                            <select name="brand" class="form-select">
                                <option value="">All Brands</option>
                                <c:forEach items="${brands}" var="b">
                                    <option value="${b.brandId}" ${b.brandId == selectedBrand ? 'selected' : ''}>
                                            ${b.name}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label class="form-label small text-muted">Sort By</label>
                            <select name="sort" class="form-select">
                                <option value="">Newest First</option>
                                <option value="price_asc" ${selectedSort == 'price_asc' ? 'selected' : ''}>Price: Low to
                                    High
                                </option>
                                <option value="price_desc" ${selectedSort == 'price_desc' ? 'selected' : ''}>Price: High
                                    to Low
                                </option>
                                <option value="name_asc" ${selectedSort == 'name_asc' ? 'selected' : ''}>Name: A to Z
                                </option>
                                <option value="name_desc" ${selectedSort == 'name_desc' ? 'selected' : ''}>Name: Z to
                                    A
                                </option>
                            </select>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary">Apply Filters</button>
                            <a href="${pageContext.request.contextPath}/catalog"
                               class="btn btn-outline-secondary">Reset</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="col-lg-9">
            <div class="row gx-4 gx-lg-5 row-cols-1 row-cols-md-2 row-cols-xl-3 justify-content-center">
                <c:forEach items="${products}" var="product">
                    <div class="col mb-5">
                        <div class="card h-100 shadow-sm border-0">
                            <a href="product?id=${product.productId}" style="text-decoration: none; color: inherit;">
                                <img class="card-img-top"
                                     src="${empty product.imgUrl ? 'https://placehold.co/450x300?text=No+Image' : pageContext.request.contextPath}${product.imgUrl}"
                                     alt="${product.name}"
                                     style="height: 200px; object-fit: contain; padding: 10px;"/>
                            </a>

                            <div class="card-body p-4">
                                <div class="text-center">
                                    <h5 class="fw-bolder text-truncate">${product.name}</h5>
                                    <span class="badge bg-secondary mb-2">${product.brand.name}</span>
                                    <p class="text-muted small text-truncate">${product.description}</p>
                                    <h5 class="text-primary">$ ${product.price}</h5>
                                </div>
                            </div>

                            <div class="card-footer p-4 pt-0 border-top-0 bg-transparent">
                                <div class="text-center d-flex justify-content-center gap-2">
                                    <a href="product?id=${product.productId}"
                                       class="btn btn-outline-dark mt-auto">View</a>

                                    <c:if test="${product.quantity > 0}">
                                        <form class="add-to-cart-form" method="POST"
                                              data-product-id="${product.productId}">

                                            <input type="hidden" name="quantity" value="1">

                                            <button type="submit" class="btn btn-dark mt-auto">
                                                <i class="bi bi-cart-plus"></i>
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${product.quantity <= 0}">
                                        <button disabled class="btn btn-secondary mt-auto">Sold Out</button>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>

                <c:if test="${empty products}">
                    <div class="col-12 text-center py-5">
                        <h3>No products found.</h3>
                        <p class="text-muted">Try adjusting your filters.</p>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</div>

<footer class="py-5 bg-dark mt-auto">
    <div class="container">
        <p class="m-0 text-center text-white">Copyright &copy; The Full Stack Shop 2025</p>
    </div>
</footer>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const forms = document.querySelectorAll(".add-to-cart-form");
        const cartBadge = document.getElementById("cartBadge");

        const apiPath = "${pageContext.request.contextPath}/api/shoppingcart/add";

        forms.forEach(form => {
            form.addEventListener("submit", function (event) {
                event.preventDefault();

                const productId = form.dataset.productId;
                const quantity = form.querySelector('input[name="quantity"]').value;
                const submitButton = form.querySelector("button[type='submit']");

                const originalContent = submitButton.innerHTML;
                submitButton.disabled = true;
                submitButton.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

                const formData = new URLSearchParams();
                formData.append('productId', productId);
                formData.append('quantity', quantity);

                fetch(apiPath, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: formData
                })
                    .then(response => {
                        if (response.ok) {
                            return response.json();
                        }
                        if (response.headers.get('content-type')?.includes('application/json')) {
                            return response.json().then(errorData => {
                                throw new Error(errorData.error || `Server responded with status: ${response.status}`);
                            });
                        }
                        throw new Error(`Server responded with status: ${response.status}`);
                    })
                    .then(data => {
                        if (data && data.count !== undefined) {
                            cartBadge.innerText = data.count;
                        }

                        submitButton.classList.remove("btn-dark");
                        submitButton.classList.add("btn-success");
                        setTimeout(() => {
                            submitButton.classList.remove("btn-success");
                            submitButton.classList.add("btn-dark");
                        }, 1000);

                    })
                    .catch(error => {
                        console.error('Error adding to cart:', error);
                        alert(`Error adding item to cart: ${error.message}`);
                    })
                    .finally(() => {
                        submitButton.disabled = false;
                        submitButton.innerHTML = originalContent;
                    });
            });
        });
    });
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>