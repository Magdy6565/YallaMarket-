//document.addEventListener("DOMContentLoaded", function () {
//  // Get orderId from URL path (e.g., /vendor-orders/6)
//  const pathParts = window.location.pathname.split("/");
//  const orderId = pathParts[pathParts.length - 1]; // Gets the last part of the URL
//
//  const orderDetailsDiv = document.getElementById("orderDetails");
//
//  // Define a mapping for status strings to select option values
//  const statusValueMap = {
//    "pending": "1",
//    "processing": "2",
//    "shipped": "3",
//    "delivered": "4",
//    "cancelled": "5",
//    "approved": "2" // Assuming "approved" maps to "Processing"
//  };
//
//  // Function to fetch and display order details
//  function fetchOrderDetails() {
//    orderDetailsDiv.innerHTML = '<div class="loading-message">Loading order details...</div>'; // Show loading
//
//    fetch(`/api/vendor/orders/${orderId}`)
//      .then((res) => {
//        if (!res.ok) {
//          // If response is not OK, throw an error to be caught by the .catch block
//          return res.text().then(text => { throw new Error(text || `HTTP error! status: ${res.status}`); });
//        }
//        return res.json();
//      })
//      .then((order) => {
//        // Format total amount to 2 decimal places if available
//        const formattedTotal = order.totalAmount !== null ? order.totalAmount.toFixed(2) : "N/A";
//        const orderDate = order.orderDate || "N/A"; // Use orderDate from the provided JSON
//
//        let html = `
//          <div class="order-detail-container">
//            <div class="order-detail-header">
//                <div class="order-detail-title">
//                    <h2>Order #${order.orderId}</h2>
//                    <span class="order-detail-status" id="orderStatus">${order.status || "N/A"}</span>
//                </div>
//                <div class="order-detail-total">Total: ${formattedTotal}</div>
//            </div>
//
//            <div class="order-detail-info">
//                <div class="order-items-section">
//                    <h3>Order Items</h3>
//                    <ul class="order-items-list">
//                        ${
//                          // Check if orderItems exists and has length, then map
//                          order.orderItems && order.orderItems.length > 0
//                            ? order.orderItems
//                                .map(
//                                  (item) => `
//                                  <li>
//                                    <strong>${item.productName || "N/A"}</strong>
//                                    (Category: ${item.productCategory || "N/A"},
//                                    Quantity: ${item.quantity || 0})
//                                  </li>
//                                `
//                                )
//                                .join("")
//                            : "<li>No items in this order.</li>"
//                        }
//                    </ul>
//                </div>
//
//                <div class="status-update-section">
//                    <div class="meta-item">
//                        <i class="fas fa-calendar-alt"></i>
//                        <span class="meta-label">Order Date:</span>
//                        <span>${orderDate}</span>
//                    </div>
//                    <div class="form-group">
//                        <label for="statusSelect">Change Status:</label>
//                        <select id="statusSelect">
//                            <option value="1">Pending</option>
//                            <option value="2">Processing</option>
//                            <option value="3">Shipped</option>
//                            <option value="4">Delivered</option>
//                            <option value="5">Cancelled</option>
//                        </select>
//                    </div>
//                    <div class="status-update-actions">
//                        <button id="changeStatusBtn" class="btn save-btn">Update Status</button>
//                        <span class="success-message" id="statusSuccess"></span>
//                        <span class="error-message" id="statusError"></span>
//                    </div>
//                </div>
//            </div>
//          </div>
//        `;
//        orderDetailsDiv.innerHTML = html;
//
//        // Set the selected value of the status dropdown based on current order status
//        const currentStatusValue = statusValueMap[order.status.toLowerCase()] || "1";
//        const statusSelect = document.getElementById("statusSelect");
//        if (statusSelect) {
//            statusSelect.value = currentStatusValue;
//        }
//
//        // Attach event listener to the update status button
//        const changeStatusBtn = document.getElementById("changeStatusBtn");
//        if (changeStatusBtn) {
//            changeStatusBtn.onclick = function () {
//                const newStatusText = document.getElementById("statusSelect").options[document.getElementById("statusSelect").selectedIndex].text;
//
//                document.getElementById("statusSuccess").textContent = "";
//                document.getElementById("statusError").textContent = "";
//
//                fetch(`/api/vendor/orders/${orderId}/status`, {
//                    method: "PUT",
//                    headers: { "Content-Type": "application/json" },
//                    body: JSON.stringify({ status: newStatusText.toLowerCase() }) // Send the status text as lowercase
//                })
//                .then((res) => {
//                    if (res.ok) {
//                        document.getElementById("orderStatus").textContent = newStatusText;
//                        document.getElementById("statusSuccess").textContent = "Status updated successfully!";
//                        // Optionally re-fetch details to ensure all data is fresh, or just update UI
//                        // setTimeout(fetchOrderDetails, 1500); // Re-fetch after a delay
//                    } else {
//                        return res.text().then((text) => {
//                            let errorMessage = "Failed to update status.";
//                            try {
//                                const errorData = JSON.parse(text);
//                                errorMessage = errorData.message || errorData.error || text;
//                            } catch (e) {
//                                errorMessage = text;
//                            }
//                            throw new Error(errorMessage);
//                        });
//                    }
//                })
//                .catch((err) => {
//                    console.error("Error updating status:", err);
//                    document.getElementById("statusError").textContent = err.message || "Failed to update status.";
//                });
//            };
//        }
//      })
//      .catch((error) => {
//        console.error("Error loading order details:", error);
//        orderDetailsDiv.innerHTML = `<div class="error-message">Failed to load order details: ${error.message || error}.</div>`;
//      });
//  }
//
//  // Initial fetch when the page loads
//  if (orderId && !isNaN(orderId)) { // Ensure orderId is a valid number
//    fetchOrderDetails();
//  } else {
//    orderDetailsDiv.innerHTML = '<div class="error-message">Invalid order ID provided.</div>';
//  }
//});
