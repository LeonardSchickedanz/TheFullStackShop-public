<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin - Sales History</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="p-4">
<div class="d-flex justify-content-between align-items-center mb-4">
    <h2>Sales History</h2>
    <a href="${pageContext.request.contextPath}/catalog" class="btn btn-outline-secondary">Back to Catalog</a>
</div>

<div class="mb-3">
    <a href="${pageContext.request.contextPath}/admin/inventory" class="btn btn-outline-primary">Inventory</a>
    <a href="${pageContext.request.contextPath}/admin/customers" class="btn btn-outline-primary">Customers</a>
</div>

<form action="" method="GET" class="row g-3 mb-4 p-3 bg-light rounded border">
    <div class="col-md-3">
        <label class="form-label small text-muted">Customer Email</label>
        <input type="text" name="email" class="form-control" placeholder="Search email..." value="${param.email}">
    </div>
    <div class="col-md-3">
        <label class="form-label small text-muted">Product Name</label>
        <input type="text" name="product" class="form-control" placeholder="Search product..." value="${param.product}">
    </div>
    <div class="col-md-2">
        <label class="form-label small text-muted">From Date</label>
        <input type="date" name="dateFrom" class="form-control" value="${param.dateFrom}">
    </div>
    <div class="col-md-2">
        <label class="form-label small text-muted">To Date</label>
        <input type="date" name="dateTo" class="form-control" value="${param.dateTo}">
    </div>
    <div class="col-md-2 d-flex align-items-end">
        <button type="submit" class="btn btn-primary w-100">Filter Results</button>
    </div>
</form>

<div class="table-responsive">
    <table class="table table-striped table-hover">
        <thead class="table-dark">
        <tr>
            <th>Order ID</th>
            <th>Date & Time</th>
            <th>Customer</th>
            <th>Total Amount</th>
            <th>Action</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${orders}" var="o">
            <tr>
                <td>#${o.orderId}</td>
                <td>${o.transactionTime}</td>
                <td>${o.user.email}</td>
                <td class="fw-bold">$ ${o.totalPrice}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/order?id=${o.orderId}"
                       class="btn btn-sm btn-info text-white">
                        Details
                    </a>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty orders}">
            <tr>
                <td colspan="5" class="text-center py-3 text-muted">No orders found matching your criteria.</td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>
</body>
</html>