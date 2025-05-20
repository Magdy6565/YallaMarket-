document.addEventListener("DOMContentLoaded", function () {
  const ordersList = document.getElementById("ordersList");

  fetch("/api/vendor/orders")
    .then((res) => {
      // Check if the response is OK (status 200-299)
      if (!res.ok) {
        // If not OK, throw an error with the response text
        return res.text().then(text => {
          throw new Error(text || `HTTP error! status: ${res.status}`);
        });
      }
      return res.json();
    })
    .then((orders) => {
      if (!orders.length) {
        ordersList.innerHTML = '<div class="empty">No orders found.</div>';
        return;
      }
      ordersList.innerHTML = orders
        .map(
          (order) => `
                <div class="product-card">
                    <div class="product-info">
                        <h3>Order #${order.orderId}</h3>
                        <p><strong>Status:</strong> ${order.status}</p>
                        <p><strong>Total:</strong> ${
                          order.totalAmount !== null ? order.totalAmount.toFixed(2) : "N/A"
                        }</p>
                        <p><strong>Date:</strong> ${order.orderDate || "N/A"}</p>

                        <h4>Order Items:</h4>
                        <ul class="order-items-list">
                            ${
                              // Map over orderItems to display each product's details
                              order.orderItems && order.orderItems.length > 0
                                ? order.orderItems
                                    .map(
                                      (item) => `
                                    <li>
                                        <strong>${item.productName || "N/A"}</strong>
                                        (Category: ${item.productCategory || "N/A"},
                                        Quantity: ${item.quantity || 0})
                                    </li>
                                  `
                                    )
                                    .join("")
                                : "<li>No items in this order.</li>"
                            }
                        </ul>

                        <a href="/vendor-orders/${
                          order.orderId
                        }" class="details-btn">View Details</a>
                    </div>
                </div>
            `
        )
        .join("");
    })
    .catch((error) => {
      console.error("Error loading orders:", error);
      ordersList.innerHTML =
        `<div class="error-message">Failed to load orders: ${error.message || error}.</div>`;
    });
});
