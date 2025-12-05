<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin - Inventory</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="p-4">
<h2>Inventory Management</h2>
<a href="${pageContext.request.contextPath}/admin/sales" class="btn btn-outline-primary mb-3">Sales</a>
<a href="${pageContext.request.contextPath}/admin/customers" class="btn btn-outline-primary mb-3">Customers</a>

<table class="table table-bordered">
    <thead>
    <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Current Stock</th>
        <th>Action</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${products}" var="p">
        <tr>
            <td>${p.productId}</td>
            <td>${p.name}</td>
            <td>
                <form action="${pageContext.request.contextPath}/admin/inventory" method="POST" class="d-flex gap-2">
                    <input type="hidden" name="productId" value="${p.productId}">
                    <input type="number" name="quantity" value="${p.quantity}" class="form-control"
                           style="width: 100px;">
                    <button type="submit" class="btn btn-sm btn-success">Update</button>
                </form>
            </td>
            <td>
                   <span class="badge ${p.quantity > 0 ? 'bg-success' : 'bg-danger'}">
                           ${p.quantity > 0 ? 'In Stock' : 'Out of Stock'}
                   </span>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>