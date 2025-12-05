<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin - Customers</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="p-4">
<h2>Customer Management</h2>
<div class="d-flex justify-content-between align-items-center mb-3">
    <a href="${pageContext.request.contextPath}/admin/sales" class="btn btn-outline-primary">Back to Sales</a>

    <form action="${pageContext.request.contextPath}/admin/customers" method="GET" class="d-flex gap-2">
        <input type="text" name="searchEmail" class="form-control" placeholder="Search by Email..."
               value="${param.searchEmail}">
        <button type="submit" class="btn btn-primary">Search</button>
        <c:if test="${not empty param.searchEmail}">
            <a href="${pageContext.request.contextPath}/admin/customers" class="btn btn-secondary">Reset</a>
        </c:if>
    </form>
</div>

<table class="table table-hover">
    <thead>
    <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Email</th>
        <th>Action</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${users}" var="u">
        <tr>
            <td>${u.userId}</td>
            <td>${u.firstName} ${u.lastName}</td>
            <td>${u.email}</td>
            <td>
                <a href="customer/edit?id=${u.userId}" class="btn btn-sm btn-warning">Edit</a>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty users}">
        <tr>
            <td colspan="4" class="text-center text-muted">No customers found.</td>
        </tr>
    </c:if>
    </tbody>
</table>
</body>
</html>