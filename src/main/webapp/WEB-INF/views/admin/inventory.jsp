<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin - Inventory</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="p-4">
<div class="d-flex justify-content-between align-items-center mb-4">
    <h2>Inventory Management</h2>
    <a href="${pageContext.request.contextPath}/catalog" class="btn btn-outline-secondary">Back to Catalog</a>
</div>

<div class="mb-3">
    <a href="${pageContext.request.contextPath}/admin/sales" class="btn btn-outline-primary">Sales History</a>
    <a href="${pageContext.request.contextPath}/admin/customers" class="btn btn-outline-primary">Customers</a>
</div>

<table class="table table-bordered table-hover">
    <thead class="table-light">
    <tr>
        <th>ID</th>
        <th>Product Name</th>
        <th>Price</th>
        <th>Current Stock</th>
        <th>Update Stock</th>
        <th>Status</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${products}" var="p">
        <tr>
            <td>${p.productId}</td>
            <td>${p.name}</td>
            <td>$ ${p.price}</td>
            <td>${p.quantity}</td>
            <td>
                <form action="${pageContext.request.contextPath}/admin/inventory" method="POST" class="d-flex gap-2">
                    <input type="hidden" name="productId" value="${p.productId}">
                    <input type="number" name="quantity" value="${p.quantity}" min="0"
                           class="form-control form-control-sm" style="width: 100px;">
                    <button type="submit" class="btn btn-sm btn-success">Save</button>
                </form>
            </td>
            <td>
                   <span class="badge ${p.quantity > 10 ? 'bg-success' : (p.quantity > 0 ? 'bg-warning' : 'bg-danger')}">
                           ${p.quantity > 10 ? 'In Stock' : (p.quantity > 0 ? 'Low Stock' : 'Out of Stock')}
                   </span>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>