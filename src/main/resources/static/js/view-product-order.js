// This script handles viewing specific product order details

document.addEventListener("DOMContentLoaded", function () {
  // Check if we have an order ID in the URL parameters
  const urlParams = new URLSearchParams(window.location.search);
  const orderId = urlParams.get("orderId") || "15"; // Default to order 15 if not specified

  // Get container for order details
  const orderDetailsContainer = document.getElementById("orderDetails");
  if (!orderDetailsContainer) {
    console.error("Order details container not found");
    return;
  }

  // Show loading state
  orderDetailsContainer.innerHTML =
    '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading order details...</div>';

  // Fetch specific order data
  fetchOrderDetails(orderId);

  /**
   * Fetch order details from the API
   * @param {string|number} orderId - The ID of the order to fetch
   */
  async function fetchOrderDetails(orderId) {
    try {
      const response = await fetch(`/api/vendor/orders/${orderId}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include", // Include cookies for authentication
      });

      if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
          throw new Error(
            "Authentication required. Please log in to view this order."
          );
        }
        throw new Error(
          `Failed to fetch order details: Error ${response.status}`
        );
      }

      const orderData = await response.json();
      renderOrderDetails(orderData);
    } catch (error) {
      console.error("Error fetching order details:", error);
      orderDetailsContainer.innerHTML = `
        <div class="error-message">
          <i class="fas fa-exclamation-triangle"></i>
          <p>${
            error.message ||
            "Failed to load order details. Please try again later."
          }</p>
          <button id="retryButton" class="btn">Retry</button>
        </div>
      `;

      // Add retry functionality
      const retryButton = document.getElementById("retryButton");
      if (retryButton) {
        retryButton.addEventListener("click", () => fetchOrderDetails(orderId));
      }
    }
  }

  /**
   * Render order details in the UI
   * @param {Object} order - The order data
   */
  function renderOrderDetails(order) {
    if (!order) {
      orderDetailsContainer.innerHTML =
        '<div class="empty-state">No order data found.</div>';
      return;
    }

    // Format order date
    const orderDate = order.orderDate
      ? new Date(order.orderDate).toLocaleDateString("en-US", {
          year: "numeric",
          month: "long",
          day: "numeric",
        })
      : "N/A";

    // Determine status class for styling
    const statusClass = order.status
      ? order.status.toLowerCase().replace(/\s/g, "-")
      : "unknown";

    // Create HTML for order items
    const orderItemsHtml =
      order.orderItems && order.orderItems.length > 0
        ? order.orderItems
            .map(
              (item) => `
        <div class="order-item">
          <div class="order-item-header">
            <h4>${item.productName || "Unknown Product"}</h4>
            <span class="item-category">${item.productCategory || "N/A"}</span>
          </div>
          <div class="order-item-details">
            <p><strong>Quantity:</strong> ${item.quantity || 0}</p>
            <p><strong>Price:</strong> $${
              item.priceEach ? item.priceEach.toFixed(2) : "N/A"
            }</p>
            <p><strong>Subtotal:</strong> $${
              item.quantity && item.priceEach
                ? (item.quantity * item.priceEach).toFixed(2)
                : "N/A"
            }</p>
            <button class="view-product-btn" data-product-id="${
              item.productId
            }">View Product Details</button>
          </div>
        </div>
      `
            )
            .join("")
        : '<p class="no-items">No items in this order.</p>';

    // Build the complete HTML for order details
    orderDetailsContainer.innerHTML = `
      <div class="order-details-card">
        <div class="order-header">
          <div class="order-title">
            <h2>Order #${order.orderId || "N/A"}</h2>
            <span class="order-status ${statusClass}">${
      order.status || "Unknown"
    }</span>
          </div>
          <div class="order-total">
            <span>Total:</span>
            <strong>$${
              order.totalAmount ? order.totalAmount.toFixed(2) : "0.00"
            }</strong>
          </div>
        </div>
        
        <div class="order-metadata">
          <p><i class="fas fa-calendar"></i> Ordered on: <strong>${orderDate}</strong></p>
          <p><i class="fas fa-store"></i> Store ID: <strong>${
            order.storeId || "N/A"
          }</strong></p>
        </div>
        
        <div class="order-items-container">
          <h3>Order Items</h3>
          <div class="order-items-list">
            ${orderItemsHtml}
          </div>
        </div>
        
        <div class="order-actions">
          <button id="backButton" class="btn back-btn"><i class="fas fa-arrow-left"></i> Back</button>
        </div>
      </div>
    `;

    // Add event listener for view product buttons
    document.querySelectorAll(".view-product-btn").forEach((button) => {
      button.addEventListener("click", function () {
        const productId = this.getAttribute("data-product-id");
        if (productId) {
          window.location.href = `/products/${productId}`;
        }
      });
    });

    // Add event listener for back button
    const backButton = document.getElementById("backButton");
    if (backButton) {
      backButton.addEventListener("click", function () {
        // Check if we came from a specific page (using referrer)
        if (
          document.referrer &&
          document.referrer.includes(window.location.host)
        ) {
          window.history.back();
        } else {
          // Default go to vendor orders page
          window.location.href = "/vendor-orders";
        }
      });
    }
  }
});
