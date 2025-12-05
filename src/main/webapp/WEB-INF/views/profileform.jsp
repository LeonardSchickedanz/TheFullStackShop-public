<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Edit Profile</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="p-4 bg-light">

<div class="container" style="max-width: 900px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Edit: ${customer.firstName} ${customer.lastName}</h2>

        <a href="${pageContext.request.contextPath}/${backUrl}" class="btn btn-outline-secondary">Back</a>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <c:if test="${param.error eq 'true' and empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            An unexpected error occurred while updating the profile.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <c:if test="${param.updated eq 'true'}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            Profile updated successfully!
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/${formAction}" method="POST">
        <input type="hidden" name="userId" value="${customer.userId}">

        <div class="card mb-4 shadow-sm">
            <div class="card-header bg-primary text-white">Personal Information</div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">First Name</label>
                        <input type="text" name="firstName" class="form-control" value="${customer.firstName}" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Last Name</label>
                        <input type="text" name="lastName" class="form-control" value="${customer.lastName}" required>
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label">Email</label>
                    <input type="email" name="email" class="form-control" value="${customer.email}" required>
                </div>
            </div>
        </div>

        <div class="card mb-4 shadow-sm">
            <div class="card-header bg-primary text-white">Shipping Address</div>
            <div class="card-body">
                <input type="hidden" name="addressId"
                       value="${customerAddress != null ? customerAddress.addressId : ''}">

                <div class="row">
                    <div class="col-md-8 mb-3">
                        <label class="form-label">Street</label>
                        <input type="text" name="street" class="form-control"
                               value="${customerAddress != null ? customerAddress.street : ''}">
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label">Number</label>
                        <input type="text" name="streetNumber" class="form-control"
                               value="${customerAddress != null ? customerAddress.streetNumber : ''}">
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-4 mb-3">
                        <label class="form-label">Postal Code</label>
                        <input type="text" name="postalCode" class="form-control"
                               value="${customerAddress != null ? customerAddress.postalCode : ''}">
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label">City</label>
                        <input type="text" name="city" class="form-control"
                               value="${customerAddress != null ? customerAddress.city : ''}">
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label">Province</label>
                        <input type="text" name="province" class="form-control"
                               value="${customerAddress != null ? customerAddress.province : ''}">
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label">Country</label>
                    <input type="text" name="country" class="form-control"
                           value="${customerAddress != null ? customerAddress.country : 'Canada'}">
                </div>
            </div>
        </div>

        <div class="card mb-4 shadow-sm">
            <div class="card-header bg-primary text-white">Payment Details</div>
            <div class="card-body">
                <input type="hidden" name="paymentId"
                       value="${customerPayment != null ? customerPayment.paymentInfoId : ''}">

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Card Number</label>
                        <input type="text" name="cardNumber" class="form-control" placeholder="16 digits"
                               value="${customerPayment != null ? customerPayment.cardNumber : ''}">
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">Expiry Date</label>
                        <input type="text" name="expiryDate" class="form-control" placeholder="MM/YY"
                               value="${customerPayment != null ? customerPayment.expiryDate : ''}">
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">CVC</label>
                        <input type="text" name="cvc" class="form-control" placeholder="123"
                               value="${customerPayment != null ? customerPayment.cvc : ''}">
                    </div>
                </div>
            </div>
        </div>

        <button type="submit" class="btn btn-success btn-lg">Save Changes</button>
    </form>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>