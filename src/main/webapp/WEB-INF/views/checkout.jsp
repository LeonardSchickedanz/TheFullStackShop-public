<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout - TheFullStackShop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .payment-display {
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 15px;
        }

        .payment-display p {
            margin-bottom: 8px;
        }

        .masked-card {
            letter-spacing: 2px;
        }
    </style>
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="text-center mb-5">
        <h2>Checkout</h2>
        <p class="lead">Complete your purchase</p>
    </div>

    <% String error = (String) request.getAttribute("error"); %>
    <% if (error != null) { %>
    <div class="alert alert-danger mb-4"><%= error %>
    </div>
    <% } %>

    <c:if test="${empty sessionScope.user}">
        <div class="row">
            <div class="col-md-6">
                <div class="card shadow-sm mb-4">
                    <div class="card-header bg-secondary text-white">
                        <h4 class="mb-0">Returning Customer</h4>
                    </div>
                    <div class="card-body">
                        <p>Please login to retrieve your saved address.</p>
                        <form action="${pageContext.request.contextPath}/login" method="post">
                            <input type="hidden" name="redirect" value="checkout">

                            <div class="mb-3">
                                <label class="form-label">Email</label>
                                <input type="email" name="email" class="form-control" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Password</label>
                                <input type="password" name="password" class="form-control" required>
                            </div>
                            <button type="submit" class="btn btn-primary w-100">Login & Checkout</button>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="card shadow-sm">
                    <div class="card-header bg-dark text-white">
                        <h4 class="mb-0">New Customer</h4>
                    </div>
                    <div class="card-body">
                        <p>Enter your details to checkout. An account will be created for you.</p>
                        <form action="${pageContext.request.contextPath}/checkout/register" method="post">
                            <div class="row">
                                <div class="col-6 mb-3">
                                    <label class="form-label">First Name</label>
                                    <input type="text" name="firstName" class="form-control" required>
                                </div>
                                <div class="col-6 mb-3">
                                    <label class="form-label">Last Name</label>
                                    <input type="text" name="lastName" class="form-control" required>
                                </div>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Email</label>
                                <input type="email" name="email" class="form-control" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Create Password</label>
                                <input type="password" name="password" class="form-control" required>
                            </div>
                            <button type="submit" class="btn btn-success w-100">Continue to Payment</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty sessionScope.user}">
        <div class="row">
            <div class="col-md-8">
                <form action="${pageContext.request.contextPath}/checkout/process" method="post">

                    <div class="card shadow-sm mb-4">
                        <div class="card-header">
                            <h4 class="mb-0">Shipping Address</h4>
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label">Street</label>
                                <input type="text" name="street" class="form-control" required
                                       value="${latestAddress != null ? latestAddress.street : ''}">
                            </div>
                            <div class="row">
                                <div class="col-md-4 mb-3">
                                    <label class="form-label">Number</label>
                                    <input type="text" name="streetNumber" class="form-control" required
                                           value="${latestAddress != null ? latestAddress.streetNumber : ''}">
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label class="form-label">Postal Code</label>
                                    <input type="text" name="postalCode" class="form-control" required
                                           value="${latestAddress != null ? latestAddress.postalCode : ''}">
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label class="form-label">City</label>
                                    <input type="text" name="city" class="form-control" required
                                           value="${latestAddress != null ? latestAddress.city : ''}">
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Province</label>
                                    <input type="text" name="province" class="form-control" required
                                           value="${latestAddress != null ? latestAddress.province : ''}">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Country</label>
                                    <input type="text" name="country" class="form-control" value="Canada" required
                                           value="${latestAddress != null ? latestAddress.country : 'Canada'}">
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- PAYMENT DETAILS MIT EDIT-BUTTON -->
                    <div class="card shadow-sm mb-4">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <h4 class="mb-0">Payment Details</h4>
                            <button type="button" class="btn btn-sm btn-outline-primary" id="editPaymentBtn"
                                    onclick="togglePaymentEdit()">
                                <span id="editBtnText">Edit</span>
                            </button>
                        </div>
                        <div class="card-body">
                            <!-- ANZEIGE-MODUS (Read-Only) -->
                            <div id="paymentDisplay" class="payment-display">
                                <p><strong>Card Number:</strong> <span
                                        class="masked-card">${latestPayment != null ? latestPayment.cardNumber : ''}</span>
                                </p>
                                <p><strong>Expiry
                                    Date:</strong> ${latestPayment != null ? latestPayment.expiryDate : ''}</p>
                                <p><strong>CVC:</strong> ${latestPayment != null ? latestPayment.cvc : ''}</p>

                            </div>

                            <!-- EDIT-MODUS (Versteckt standardmäßig) -->
                            <div id="paymentEdit" style="display: none;">
                                <div class="alert alert-info">
                                    <small><strong>Note:</strong> These payment details will only be used for this order
                                        and will not be saved. To edit your permanent payment details go to profile.
                                    </small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Card Number</label>
                                    <input type="text" id="cardNumberInput" class="form-control"
                                           placeholder="0000 0000 0000 0000"
                                           value="${latestPayment != null ? latestPayment.cardNumber : ''}">
                                </div>
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">Expiry Date</label>
                                        <input type="text" id="expiryDateInput" class="form-control" placeholder="MM/YY"
                                               value="${latestPayment != null ? latestPayment.expiryDate : ''}">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label">CVC</label>
                                        <input type="text" id="cvcInput" class="form-control" placeholder="123"
                                               value="${latestPayment != null ? latestPayment.cvc : ''}">
                                    </div>
                                </div>
                            </div>

                            <!-- ACTUAL FORM FIELDS (werden dynamisch gefüllt) -->
                            <input type="hidden" name="cardNumber" id="cardNumberField"
                                   value="${latestPayment != null ? latestPayment.cardNumber : ''}">
                            <input type="hidden" name="expiryDate" id="expiryDateField"
                                   value="${latestPayment != null ? latestPayment.expiryDate : ''}">
                            <input type="hidden" name="cvc" id="cvcField"
                                   value="${latestPayment != null ? latestPayment.cvc : ''}">

                            <!-- FLAG: Wurde Payment editiert? -->
                            <input type="hidden" name="paymentWasEdited" id="paymentWasEditedFlag" value="false">
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary btn-lg w-100">Confirm Order</button>
                </form>
            </div>

            <div class="col-md-4">
                <div class="card shadow-sm">
                    <div class="card-header">
                        <h4 class="mb-0">Your Order</h4>
                    </div>
                    <div class="card-body">
                        <ul class="list-group list-group-flush mb-3">
                            <c:forEach items="${cart.cartItems}" var="item">
                                <li class="list-group-item d-flex justify-content-between lh-sm">
                                    <div>
                                        <h6 class="my-0">${item.product.name}</h6>
                                        <small class="text-muted">Qty: ${item.quantity}</small>
                                    </div>
                                    <span class="text-muted">$ ${item.product.price * item.quantity}</span>
                                </li>
                            </c:forEach>
                            <li class="list-group-item d-flex justify-content-between bg-light">
                                <span>Total (CAD)</span>
                                <strong>$ ${grandTotal}</strong>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    let isEditMode = false;

    function togglePaymentEdit() {
        const display = document.getElementById('paymentDisplay');
        const edit = document.getElementById('paymentEdit');
        const btnText = document.getElementById('editBtnText');
        const btn = document.getElementById('editPaymentBtn');
        const editFlag = document.getElementById('paymentWasEditedFlag');

        isEditMode = !isEditMode;

        if (isEditMode) {
            display.style.display = 'none';
            edit.style.display = 'block';
            btnText.textContent = 'Use Saved';
            btn.classList.remove('btn-outline-primary');
            btn.classList.add('btn-outline-secondary');

            editFlag.value = 'true';

            const cardNum = document.getElementById('cardNumberField').value;
            const expiry = document.getElementById('expiryDateField').value;
            const cvcVal = document.getElementById('cvcField').value;

            document.getElementById('cardNumberInput').value = cardNum;
            document.getElementById('expiryDateInput').value = expiry;
            document.getElementById('cvcInput').value = cvcVal;
        } else {
            display.style.display = 'block';
            edit.style.display = 'none';
            btnText.textContent = 'Edit';
            btn.classList.remove('btn-outline-secondary');
            btn.classList.add('btn-outline-primary');

            editFlag.value = 'false';

            const savedCardNum = '${latestPayment != null ? latestPayment.cardNumber : ""}';
            const savedExpiry = '${latestPayment != null ? latestPayment.expiryDate : ""}';
            const savedCvc = '${latestPayment != null ? latestPayment.cvc : ""}';

            document.getElementById('cardNumberField').value = savedCardNum;
            document.getElementById('expiryDateField').value = savedExpiry;
            document.getElementById('cvcField').value = savedCvc;
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        const cardInput = document.getElementById('cardNumberInput');
        const expiryInput = document.getElementById('expiryDateInput');
        const cvcInput = document.getElementById('cvcInput');

        cardInput.addEventListener('input', function () {
            if (isEditMode) {
                document.getElementById('cardNumberField').value = this.value;
            }
        });

        expiryInput.addEventListener('input', function () {
            if (isEditMode) {
                document.getElementById('expiryDateField').value = this.value;
            }
        });

        cvcInput.addEventListener('input', function () {
            if (isEditMode) {
                document.getElementById('cvcField').value = this.value;
            }
        });
    });
</script>
</body>
</html>