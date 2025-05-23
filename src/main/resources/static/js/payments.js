// Payments page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const paymentsTableBody = document.getElementById('paymentsTableBody');
    const loadingIndicator = document.getElementById('paymentsLoadingIndicator');
    const noPaymentsMessage = document.getElementById('noPaymentsMessage');
    const paymentIdFilter = document.getElementById('paymentIdFilter');
    const orderIdFilter = document.getElementById('orderIdFilter');
    const userIdFilter = document.getElementById('userIdFilter');
    const minAmountFilter = document.getElementById('minAmountFilter');
    const maxAmountFilter = document.getElementById('maxAmountFilter');
    const statusFilter = document.getElementById('statusFilter');
    const filterBtn = document.getElementById('filterBtn');
    const clearFilterBtn = document.getElementById('clearFilterBtn');
    const prevPageBtn = document.getElementById('prevPage');
    const nextPageBtn = document.getElementById('nextPage');
    const pageInfoSpan = document.getElementById('pageInfo');
    
    // Modal elements
    const paymentDetailsModal = document.getElementById('paymentDetailsModal');
    const paymentDetailsContent = document.getElementById('paymentDetailsContent');
    const modalCloseButtons = document.querySelectorAll('.modal-close');
    
    // Toast notification
    const toast = document.getElementById('toastNotification');
    const toastMessage = document.getElementById('toastMessage');
    const toastIcon = document.getElementById('toastIcon');
    
    // State management
    let currentPage = 0;
    let totalPages = 1;
    let pageSize = 10;
    let currentFilters = {
        paymentId: null,
        orderId: null,
        userId: null,
        minAmount: null,
        maxAmount: null,
        status: ''
    };
    
    // Initialize the page
    fetchPayments();
    
    // Event listeners for filter form
    filterBtn.addEventListener('click', function() {
        currentFilters.paymentId = paymentIdFilter.value ? parseInt(paymentIdFilter.value) : null;
        currentFilters.orderId = orderIdFilter.value ? parseInt(orderIdFilter.value) : null;
        currentFilters.userId = userIdFilter.value ? parseInt(userIdFilter.value) : null;
        currentFilters.minAmount = minAmountFilter.value ? parseFloat(minAmountFilter.value) : null;
        currentFilters.maxAmount = maxAmountFilter.value ? parseFloat(maxAmountFilter.value) : null;
        currentFilters.status = statusFilter.value;
        
        currentPage = 0; // Reset to first page when filtering
        fetchPayments();
    });
    
    clearFilterBtn.addEventListener('click', function() {
        // Clear all filter inputs
        paymentIdFilter.value = '';
        orderIdFilter.value = '';
        userIdFilter.value = '';
        minAmountFilter.value = '';
        maxAmountFilter.value = '';
        statusFilter.value = '';
        
        // Reset filters in state
        currentFilters = {
            paymentId: null,
            orderId: null,
            userId: null,
            minAmount: null,
            maxAmount: null,
            status: ''
        };
        
        // Reset to first page and fetch all payments
        currentPage = 0;
        fetchPayments();
    });
    
    // Pagination event listeners
    prevPageBtn.addEventListener('click', function() {
        if (currentPage > 0) {
            currentPage--;
            fetchPayments();
        }
    });
    
    nextPageBtn.addEventListener('click', function() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            fetchPayments();
        }
    });
    
    // Close modals when clicking the X button
    modalCloseButtons.forEach(function(btn) {
        btn.addEventListener('click', function() {
            const modal = btn.closest('.modal');
            closeModal(modal);
        });
    });
    
    /**
     * Fetch payments from the API based on current filters and pagination
     */    function fetchPayments() {
        paymentsTableBody.innerHTML = '';
        loadingIndicator.style.display = 'block';
        noPaymentsMessage.style.display = 'none';
        
        let url = `/api/payments?page=${currentPage}&size=${pageSize}`;
        
        // Add filters to URL if they exist
        if (currentFilters.paymentId) {
            url += `&paymentId=${currentFilters.paymentId}`;
        }
        
        if (currentFilters.orderId) {
            url += `&orderId=${currentFilters.orderId}`;
        }
        
        if (currentFilters.userId) {
            url += `&userId=${currentFilters.userId}`;
        }
        
        if (currentFilters.minAmount) {
            url += `&minAmount=${currentFilters.minAmount}`;
        }
        
        if (currentFilters.maxAmount) {
            url += `&maxAmount=${currentFilters.maxAmount}`;
        }
        
        if (currentFilters.status) {
            url += `&status=${currentFilters.status}`;
        }
        
        console.log('Fetching payments with URL:', url); // Debug log
        
        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`API returned ${response.status}: ${response.statusText}`);
                }
                return response.json();
            })
            .then(data => {
                loadingIndicator.style.display = 'none';
                console.log('API response:', data); // Debug log
                
                if (data.content && data.content.length > 0) {
                    displayPayments(data.content);
                    updatePagination(data);
                } else {
                    noPaymentsMessage.style.display = 'block';
                    updatePagination({ totalPages: 0, number: 0, totalElements: 0 });
                }
            })
            .catch(error => {
                console.error('Error fetching payments:', error);
                loadingIndicator.style.display = 'none';
                showToast('Error loading payments. Please try again.', false);
            });
    }
    
    /**
     * Display payments in the table
     * @param {Array} payments - Array of payment objects
     */
    function displayPayments(payments) {
        paymentsTableBody.innerHTML = '';
        
        payments.forEach(payment => {
            const tr = document.createElement('tr');
            
            // Format payment date with null check
            let formattedDate = 'N/A';
            if (payment.paymentDate) {
                const paymentDate = new Date(payment.paymentDate);
                formattedDate = paymentDate.toLocaleString();
            }
            
            // Get status for class and display safely
            const statusLower = payment.status ? payment.status.toLowerCase() : 'unknown';
            const statusDisplay = payment.status || 'UNKNOWN';
            
            tr.innerHTML = `
                <td>${payment.paymentId || 'N/A'}</td>
                <td>${payment.orderId || 'N/A'}</td>
                <td>${payment.userId || 'N/A'}</td>
                <td>${formattedDate}</td>
                <td>$${parseFloat(payment.amount || 0).toFixed(2)}</td>
                <td>${payment.paymentMethod || 'N/A'}</td>
                <td><span class="payment-status status-${statusLower}">${statusDisplay}</span></td>
                <td>${payment.transactionId || 'N/A'}</td>
                <td class="actions">
                    <button class="action-btn view" title="View Details" onClick="viewPaymentDetails(${payment.paymentId})">
                        <i class="fas fa-eye"></i>
                    </button>
                </td>
            `;
            
            paymentsTableBody.appendChild(tr);
        });
    }
    
        /**
     * Update pagination controls based on API response
     * @param {Object} pageData - Page information from API
     */
    function updatePagination(pageData) {
        totalPages = pageData.totalPages || 0;
        currentPage = pageData.number || 0;
        const totalElements = pageData.totalElements || 0;
        
        pageInfoSpan.textContent = `Page ${currentPage + 1} of ${totalPages || 1} (${totalElements} total payments)`;
        
        prevPageBtn.disabled = currentPage <= 0;
        nextPageBtn.disabled = currentPage >= totalPages - 1;
    }
    
    /**
     * View details of a payment
     * @param {Number} paymentId - ID of the payment to view
     */
    window.viewPaymentDetails = function(paymentId) {
        fetch(`/api/payments/${paymentId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Failed to load payment details: ${response.status}`);
                }
                return response.json();
            })
            .then(payment => {
                showPaymentDetailsModal(payment);
            })
            .catch(error => {
                console.error('Error fetching payment details:', error);
                showToast('Error loading payment details', false);
            });
    };

    /**
     * Display payment details in a modal
     * @param {Object} payment - Payment object
     */
    function showPaymentDetailsModal(payment) {
        // Format payment date with null check
        let formattedDate = 'N/A';
        if (payment.paymentDate) {
            const paymentDate = new Date(payment.paymentDate);
            formattedDate = paymentDate.toLocaleString();
        }
        
        // Get status for class and display safely
        const statusLower = payment.status ? payment.status.toLowerCase() : 'unknown';
        const statusDisplay = payment.status || 'UNKNOWN';
        
        paymentDetailsContent.innerHTML = `
            <dl class="payment-details-list">
                <dt>Payment ID:</dt>
                <dd>${payment.paymentId || 'N/A'}</dd>
                
                <dt>Order ID:</dt>
                <dd>${payment.orderId || 'N/A'}</dd>
                
                <dt>User ID:</dt>
                <dd>${payment.userId || 'N/A'}</dd>
                
                <dt>Date & Time:</dt>
                <dd>${formattedDate}</dd>
                
                <dt>Amount:</dt>
                <dd>$${parseFloat(payment.amount || 0).toFixed(2)}</dd>
                
                <dt>Payment Method:</dt>
                <dd>${payment.paymentMethod || 'N/A'}</dd>
                
                <dt>Status:</dt>
                <dd><span class="payment-status status-${statusLower}">${statusDisplay}</span></dd>
                
                <dt>Transaction ID:</dt>
                <dd>${payment.transactionId || 'N/A'}</dd>
                
                <dt>Reference Number:</dt>
                <dd>${payment.referenceNo || 'N/A'}</dd>

                <dt>Invoices:</dt>
                <dd>${payment.invoiceCount || 0}</dd>
            </dl>
            
            <div class="modal-actions">
                <button class="btn btn-secondary" onclick="closePaymentDetailsModal()">Close</button>
                ${payment.status === 'PENDING' ? 
                    '<button class="btn btn-primary" onclick="viewRelatedOrder(' + payment.orderId + ')">View Order</button>' : ''}
            </div>
        `;
        
        showModal(paymentDetailsModal);
    }
    
    /**
     * Show a modal
     * @param {HTMLElement} modal - The modal to show
     */
    function showModal(modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
        
        // Add event listener to close when clicking outside modal content
        window.addEventListener('click', function modalOutsideClick(e) {
            if (e.target === modal) {
                closeModal(modal);
                window.removeEventListener('click', modalOutsideClick);
            }
        });
    }
    
    /**
     * Close a modal
     * @param {HTMLElement} modal - The modal to close
     */
    function closeModal(modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
    
    /**
     * Show a toast notification
     * @param {string} message - Message to display
     * @param {boolean} isSuccess - Whether this is a success message
     */
    function showToast(message, isSuccess = true) {
        toastMessage.textContent = message;
        
        if (isSuccess) {
            toastIcon.className = 'toast-icon success';
            toastIcon.innerHTML = '<i class="fas fa-check-circle"></i>';
        } else {
            toastIcon.className = 'toast-icon error';
            toastIcon.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
        }
        
        toast.classList.add('visible');
        
        // Hide after 3 seconds
        setTimeout(() => {
            toast.classList.remove('visible');
        }, 3000);
    }
    
    // Close payment details modal function
    window.closePaymentDetailsModal = function() {
        closeModal(paymentDetailsModal);
    };
    
    // View related order function (placeholder, implement as needed)
    window.viewRelatedOrder = function(orderId) {
        // Redirect to order details page or show order in a modal
        window.location.href = `/orders/${orderId}`;
    };
});
