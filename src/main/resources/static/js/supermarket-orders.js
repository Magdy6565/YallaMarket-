// supermarket-orders.js - JavaScript for the supermarket orders page

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // --- DOM Element Selections ---
    const ordersContainer = document.getElementById('ordersContainer');
    const emptyState = document.getElementById('emptyState');
    const orderStatusFilter = document.getElementById('orderStatusFilter');
    const filterOrdersButton = document.getElementById('filterOrdersButton');
    const orderDetailModal = document.getElementById('orderDetailModal');
    const closeModal = document.querySelector('.close-modal');
    const orderModalBody = document.getElementById('orderModalBody');
    const cartBadge = document.getElementById('cartBadge');
    
    // Load cart items from localStorage and update badge
    const cartItems = JSON.parse(localStorage.getItem('cartItems')) || [];
    updateCartBadge();

    function updateCartBadge() {
        const totalItems = cartItems.reduce((total, item) => total + item.quantity, 0);
        cartBadge.textContent = totalItems;
        cartBadge.style.display = totalItems > 0 ? 'flex' : 'none';
    }

    // Mobile menu toggle functionality
    const menuToggle = document.getElementById('menuToggle');
    const navbarLinks = document.getElementById('navbarLinks');

    if (menuToggle && navbarLinks) {
        menuToggle.addEventListener('click', function() {
            navbarLinks.classList.toggle('active');
        });
    }

    // User menu dropdown functionality
    const userMenu = document.getElementById('userMenu');
    const userMenuButton = userMenu ? userMenu.querySelector('.user-menu-button') : null;

    if (userMenuButton) {
        userMenuButton.addEventListener('click', function() {
            userMenu.classList.toggle('open');
        });

        // Close the dropdown when clicking outside
        document.addEventListener('click', function(event) {
            if (!userMenu.contains(event.target)) {
                userMenu.classList.remove('open');
            }
        });
    }

    // --- Core Functions ---

    /**
     * Fetch orders from the API with optional status filter
     */
    async function fetchOrders(status = '') {
        // Show loading spinner
        ordersContainer.innerHTML = '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> Loading orders...</div>';
        
        try {
            // Construct the URL with optional status filter
            let url = '/api/store/orders';
            if (status) {
                url += `?status=${status}`;
            }
            
            const response = await fetch(url);
            
            if (!response.ok) {
                throw new Error('Failed to fetch orders');
            }
            
            const orders = await response.json();
            renderOrders(orders);
        } catch (error) {
            console.error('Error fetching orders:', error);
            ordersContainer.innerHTML = `
                <div class="error">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Failed to load orders. Please try again later.</p>
                </div>
            `;
        }
    }

    /**
     * Render orders in the container
     */
    function renderOrders(orders) {
        if (!orders || orders.length === 0) {
            ordersContainer.innerHTML = '';
            emptyState.style.display = 'flex';
            return;
        }
        
        emptyState.style.display = 'none';
        ordersContainer.innerHTML = '';

        // Create orders list
        const ordersList = document.createElement('div');
        ordersList.className = 'orders-list';
        
        orders.forEach(order => {
            const orderDate = new Date(order.orderDate || Date.now()).toLocaleDateString();
            
            // Determine status class for styling
            const statusMap = {
                'PENDING': 'pending',
                'APPROVED': 'approved',
                'SHIPPED': 'shipped',
                'DELIVERED': 'delivered',
                'CANCELLED': 'cancelled',
                'DENIED': 'cancelled'
            };
            
            const statusClass = statusMap[order.status] || 'pending';
            
            const orderCard = document.createElement('div');
            orderCard.className = 'order-card';
            orderCard.innerHTML = `
                <div class="order-header">
                    <div class="order-id">Order #${order.id}</div>
                    <div class="order-status ${statusClass}">${order.status}</div>
                </div>
                <div class="order-info">
                    <div class="order-date">Ordered on: ${orderDate}</div>
                    <div class="order-total">Total: $${parseFloat(order.total).toFixed(2)}</div>
                    <div class="order-items-count">${order.itemCount || 'Multiple'} items</div>
                </div>
                <div class="order-actions">
                    <button class="details-btn" data-order-id="${order.id}">
                        <i class="fas fa-info-circle"></i> View Details
                    </button>
                </div>
            `;
            
            ordersList.appendChild(orderCard);
        });
        
        ordersContainer.appendChild(ordersList);
        
        // Add event listeners to details buttons
        document.querySelectorAll('.details-btn').forEach(button => {
            button.addEventListener('click', function() {
                const orderId = this.getAttribute('data-order-id');
                openOrderDetailModal(orderId);
            });
        });
    }

    /**
     * Open order detail modal with order info
     */
    async function openOrderDetailModal(orderId) {
        try {
            // Show loading in modal
            orderModalBody.innerHTML = '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> Loading order details...</div>';
            
            // Open modal
            orderDetailModal.style.display = 'block';
            
            // Fetch order details
            const response = await fetch(`/api/store/orders/${orderId}`);
            
            if (!response.ok) {
                throw new Error('Failed to fetch order details');
            }
            
            const order = await response.json();
            
            // Format date
            const orderDate = new Date(order.orderDate || Date.now()).toLocaleDateString();
            
            // Status class for styling
            const statusMap = {
                'PENDING': 'pending',
                'APPROVED': 'approved',
                'SHIPPED': 'shipped',
                'DELIVERED': 'delivered',
                'CANCELLED': 'cancelled',
                'DENIED': 'cancelled'
            };
            
            const statusClass = statusMap[order.status] || 'pending';
            
            // Create content for modal
            let itemsHtml = '';
            if (order.items && order.items.length > 0) {
                itemsHtml = order.items.map(item => `
                    <div class="order-item">
                        <div class="item-image">
                            <img src="${item.imageUrl || 'https://via.placeholder.com/60'}" alt="${item.productName}">
                        </div>
                        <div class="item-details">
                            <h4>${item.productName}</h4>
                            <div class="item-vendor">${item.vendorName || 'Unknown Vendor'}</div>
                            <div class="item-price-qty">
                                <span class="item-price">$${parseFloat(item.price).toFixed(2)}</span>
                                <span class="item-qty">x${item.quantity}</span>
                            </div>
                        </div>
                        <div class="item-total">
                            $${parseFloat(item.price * item.quantity).toFixed(2)}
                        </div>
                    </div>
                `).join('');
            } else {
                itemsHtml = '<p>No items found for this order.</p>';
            }
            
            // Render order details in modal
            orderModalBody.innerHTML = `
                <div class="order-detail">
                    <div class="order-detail-header">
                        <h2>Order #${order.id}</h2>
                        <div class="order-status ${statusClass}">${order.status}</div>
                    </div>
                    
                    <div class="order-detail-info">
                        <div class="order-info-group">
                            <div class="info-label">Order Date:</div>
                            <div class="info-value">${orderDate}</div>
                        </div>
                        <div class="order-info-group">
                            <div class="info-label">Total Amount:</div>
                            <div class="info-value">$${parseFloat(order.total).toFixed(2)}</div>
                        </div>
                        <div class="order-info-group">
                            <div class="info-label">Shipping Address:</div>
                            <div class="info-value">${order.shippingAddress || 'Not available'}</div>
                        </div>
                        <div class="order-info-group">
                            <div class="info-label">Payment Method:</div>
                            <div class="info-value">${order.paymentMethod || 'Not specified'}</div>
                        </div>
                    </div>
                    
                    <div class="order-items-section">
                        <h3>Order Items</h3>
                        <div class="order-items">
                            ${itemsHtml}
                        </div>
                    </div>
                    
                    <div class="order-detail-summary">
                        <div class="summary-row">
                            <div class="summary-label">Subtotal:</div>
                            <div class="summary-value">$${parseFloat(order.subtotal || order.total).toFixed(2)}</div>
                        </div>
                        <div class="summary-row">
                            <div class="summary-label">Shipping:</div>
                            <div class="summary-value">$${parseFloat(order.shippingCost || 0).toFixed(2)}</div>
                        </div>
                        <div class="summary-row">
                            <div class="summary-label">Tax:</div>
                            <div class="summary-value">$${parseFloat(order.tax || 0).toFixed(2)}</div>
                        </div>
                        <div class="summary-row total">
                            <div class="summary-label">Total:</div>
                            <div class="summary-value">$${parseFloat(order.total).toFixed(2)}</div>
                        </div>
                    </div>
                    
                    ${order.status === 'PENDING' ? `
                        <div class="order-detail-actions">
                            <button class="cancel-order-btn" data-order-id="${order.id}">
                                <i class="fas fa-times-circle"></i> Cancel Order
                            </button>
                        </div>
                    ` : ''}
                </div>
            `;
            
            // Add event listener for cancel button if exists
            const cancelBtn = orderModalBody.querySelector('.cancel-order-btn');
            if (cancelBtn) {
                cancelBtn.addEventListener('click', function() {
                    cancelOrder(order.id);
                });
            }
            
        } catch (error) {
            console.error('Error loading order details:', error);
            orderModalBody.innerHTML = `
                <div class="error">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Failed to load order details. Please try again later.</p>
                </div>
            `;
        }
    }

    /**
     * Cancel an order
     */
    async function cancelOrder(orderId) {
        if (!confirm('Are you sure you want to cancel this order?')) {
            return;
        }
        
        try {
            const response = await fetch(`/api/store/orders/${orderId}/cancel`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to cancel order');
            }
            
            // Close modal and refresh orders list
            orderDetailModal.style.display = 'none';
            fetchOrders(orderStatusFilter.value);
            
            // Show success message
            alert('Order cancelled successfully');
        } catch (error) {
            console.error('Error cancelling order:', error);
            alert('Failed to cancel order. Please try again later.');
        }
    }

    // --- Event Listeners ---
    
    // Filter button click
    if (filterOrdersButton) {
        filterOrdersButton.addEventListener('click', function() {
            const status = orderStatusFilter.value;
            fetchOrders(status);
        });
    }
    
    // Modal close button
    if (closeModal) {
        closeModal.addEventListener('click', function() {
            orderDetailModal.style.display = 'none';
        });
    }
    
    // Close modal when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target === orderDetailModal) {
            orderDetailModal.style.display = 'none';
        }
    });

    // --- Initial Setup ---
    fetchOrders();
});
