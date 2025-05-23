// Vendor Refunds JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const refundsTableBody = document.getElementById('refundsTableBody');
    const loadingIndicator = document.getElementById('loadingIndicator');
    const noRefundsMessage = document.getElementById('noRefundsMessage');
    
    // Modal elements
    const refundDetailModal = document.getElementById('refundDetailModal');
    const refundDetailContent = document.getElementById('refundDetailContent');
    
    // Button elements
    const approveRefundBtn = document.getElementById('approveRefundBtn');
    const rejectRefundBtn = document.getElementById('rejectRefundBtn');
    const closeRefundDetail = document.getElementById('closeRefundDetail');
    
    // Toast notification
    const toast = document.getElementById('toastNotification');
    const toastMessage = document.getElementById('toastMessage');
    const toastIcon = document.getElementById('toastIcon');
    
    // Modal close elements
    const modalCloseButtons = document.querySelectorAll('.modal-close');
    
    // State management
    let currentRefundId = null;
    
    // Mobile menu toggle
    const menuToggle = document.getElementById('menuToggle');
    const navbarLinks = document.getElementById('navbarLinks');
    
    menuToggle.addEventListener('click', function() {
        navbarLinks.classList.toggle('active');
    });
    
  const userMenu = document.getElementById("userMenu");
  const userMenuButton = userMenu
    ? userMenu.querySelector(".user-menu-button")
    : null;

  if (userMenuButton) {
    // Toggle the dropdown when clicking the button (using class approach)
    userMenuButton.addEventListener("click", function (event) {
      event.preventDefault();
      event.stopPropagation(); // Prevent click from propagating to document
      userMenu.classList.toggle("open");
      console.log(
        "User menu toggled, open status:",
        userMenu.classList.contains("open")
      );
    });
    }
    // Initialize the page
    fetchRefunds();
    
    // Modal close button event listeners
    closeRefundDetail.addEventListener('click', function() {
        closeModal(refundDetailModal);
    });
    
    // Close modals when clicking the X button
    modalCloseButtons.forEach(function(btn) {
        btn.addEventListener('click', function() {
            const modal = btn.closest('.modal');
            closeModal(modal);
        });
    });
    
    // Approve and reject buttons
    approveRefundBtn.addEventListener('click', function() {
        updateRefundStatus('APPROVED');
    });
    
    rejectRefundBtn.addEventListener('click', function() {
        updateRefundStatus('REJECTED');
    });
    
    /**
     * Fetch refunds from the API
     */
    function fetchRefunds() {
        refundsTableBody.innerHTML = '';
        loadingIndicator.style.display = 'block';
        noRefundsMessage.style.display = 'none';
        
        // Get current user ID (vendor)
        getCurrentVendorId().then(vendorId => {
            if (!vendorId) {
                loadingIndicator.style.display = 'none';
                noRefundsMessage.style.display = 'block';
                console.error('Could not determine vendor ID');
                return;
            }
            
            fetch(`/api/refunds/vendor/${vendorId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch refunds');
                    }
                    return response.json();
                })
                .then(refunds => {
                    loadingIndicator.style.display = 'none';
                    
                    if (refunds && refunds.length > 0) {
                        displayRefunds(refunds);
                    } else {
                        noRefundsMessage.style.display = 'block';
                    }
                })
                .catch(error => {
                    console.error('Error fetching refunds:', error);
                    loadingIndicator.style.display = 'none';
                    noRefundsMessage.style.display = 'block';
                    showToast('Error loading refunds. Please try again.', false);
                });
        });
    }
    
    /**
     * Get the current vendor's ID
     * @returns {Promise<number>} The vendor's ID
     */
    function getCurrentVendorId() {
        return fetch('/api/user/current')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch current user');
                }
                return response.json();
            })
            .then(user => {
                if (!user || !user.id) {
                    throw new Error('No user ID found');
                }
                return user.id;
            })
            .catch(error => {
                console.error('Error getting current user:', error);
                return null;
            });
    }
    
    /**
     * Display refunds in the table
     * @param {Array} refunds - Array of refund objects
     */
    function displayRefunds(refunds) {
        refundsTableBody.innerHTML = '';
        
        refunds.forEach(refund => {
            const tr = document.createElement('tr');
            const statusClass = getStatusClass(refund.status);
            // Build action buttons: view always, approve/reject for requested
            let actionsHtml = `
                <button class="action-btn view" title="View Details" onClick="viewRefundDetails(${refund.refundId})">
                    <i class="fas fa-eye"></i>
                </button>`;
            if (String(refund.status).toUpperCase() === 'REQUESTED') {
                actionsHtml += `
                <button class="action-btn approve" title="Approve" onClick="updateRefundStatusRow(${refund.refundId}, 'APPROVED')">
                    <i class="fas fa-check"></i>
                </button>
                <button class="action-btn reject" title="Reject" onClick="updateRefundStatusRow(${refund.refundId}, 'REJECTED')">
                    <i class="fas fa-times"></i>
                </button>`;
            }
            tr.innerHTML = `
                <td>${refund.refundId}</td>
                <td>${refund.orderId}</td>
                <td>${escapeHtml(refund.customerUsername)}</td>
                <td>$${refund.amount.toFixed(2)}</td>
                <td>${escapeHtml(truncateText(refund.reason, 30))}</td>
                <td><span class="status-badge ${statusClass}">${refund.status}</span></td>
                <td>${formatDate(refund.refundDate)}</td>
                <td class="actions">${actionsHtml}</td>
            `;
            refundsTableBody.appendChild(tr);
        });
    }
    
    /**
     * View refund details
     * @param {number} refundId - The ID of the refund to view
     */
    window.viewRefundDetails = function(refundId) {
        currentRefundId = refundId;
        
        fetch(`/api/refunds/${refundId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch refund details');
                }
                return response.json();
            })
            .then(refund => {
                const statusClass = getStatusClass(refund.status);
                
                refundDetailContent.innerHTML = `
                    <div class="refund-detail-grid">
                        <div class="detail-row">
                            <div class="detail-label">Refund ID:</div>
                            <div class="detail-value">${refund.refundId}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Order ID:</div>
                            <div class="detail-value">${refund.orderId}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Customer:</div>
                            <div class="detail-value">${escapeHtml(refund.customerUsername)}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Amount:</div>
                            <div class="detail-value">$${refund.amount.toFixed(2)}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Status:</div>
                            <div class="detail-value">
                                <span class="status-badge ${statusClass}">${refund.status}</span>
                            </div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Request Date:</div>
                            <div class="detail-value">${formatDate(refund.refundDate)}</div>
                        </div>
                        ${refund.processedDate ? `
                        <div class="detail-row">
                            <div class="detail-label">Processed Date:</div>
                            <div class="detail-value">${formatDate(refund.processedDate)}</div>
                        </div>` : ''}
                    </div>
                    
                    <div class="refund-reason">
                        <h4>Reason for Refund:</h4>
                        <p>${escapeHtml(refund.reason)}</p>
                    </div>
                    
                    ${refund.notes ? `
                    <div class="refund-notes">
                        <h4>Additional Notes:</h4>
                        <p>${escapeHtml(refund.notes)}</p>
                    </div>` : ''}
                `;
                
                // Show/hide action buttons based on status
                if (refund.status === 'PENDING') {
                    approveRefundBtn.style.display = 'inline-block';
                    rejectRefundBtn.style.display = 'inline-block';
                } else {
                    approveRefundBtn.style.display = 'none';
                    rejectRefundBtn.style.display = 'none';
                }
                
                openModal(refundDetailModal);
            })
            .catch(error => {
                console.error('Error fetching refund details:', error);
                showToast('Error loading refund details. Please try again.', false);
            });
    };
    
    /**
     * Update the status of a refund
     * @param {string} status - The new status ('APPROVED' or 'REJECTED')
     */    function updateRefundStatus(status) {
        if (!currentRefundId) return;
        
        // Convert from UI status format to backend format
        const backendStatus = status === 'APPROVED' ? 'approved' : 'rejected';
        
        const data = { 
            status: backendStatus
        };
        
        fetch(`/api/refunds/${currentRefundId}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to ${status.toLowerCase()} refund`);
            }
            return response.json();
        })
        .then(() => {
            closeModal(refundDetailModal);
            fetchRefunds();
            showToast(`Refund ${status.toLowerCase()} successfully!`, true);
        })
        .catch(error => {
            console.error(`Error ${status.toLowerCase()}ing refund:`, error);
            showToast(`Error ${status.toLowerCase()}ing refund. Please try again.`, false);
        });
    }
    
    /**
     * Helper to update refund status directly from row buttons
     */
    window.updateRefundStatusRow = function(refundId, status) {
        currentRefundId = refundId;
        updateRefundStatus(status);
    };
    
    /**
     * Get CSS class for refund status
     * @param {string} status - The refund status
     * @returns {string} The CSS class for the status
     */    function getStatusClass(status) {
        // Convert status to uppercase for consistent comparison
        const upperStatus = String(status).toUpperCase();
        
        switch (upperStatus) {
            case 'REQUESTED':
            case 'PENDING':
                return 'status-pending';
            case 'APPROVED':
                return 'status-approved';
            case 'REJECTED':
                return 'status-rejected';
            case 'PROCESSED':
                return 'status-processed';
            default:
                return '';
        }
    }
    
    /**
     * Format date string
     * @param {string} dateString - ISO date string
     * @returns {string} Formatted date string
     */
    function formatDate(dateString) {
        if (!dateString) return 'N/A';
        
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
    
    /**
     * Truncate text to a specific length
     * @param {string} text - The text to truncate
     * @param {number} length - Maximum length before truncation
     * @returns {string} Truncated text
     */
    function truncateText(text, length) {
        if (!text) return '';
        if (text.length <= length) return text;
        return text.substring(0, length) + '...';
    }
    
    /**
     * Open a modal
     * @param {HTMLElement} modal - The modal element to open
     */
    function openModal(modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
    
    /**
     * Close a modal
     * @param {HTMLElement} modal - The modal element to close
     */
    function closeModal(modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
    
    /**
     * Show a toast notification
     * @param {string} message - The message to display
     * @param {boolean} isSuccess - Whether this is a success message
     */
    function showToast(message, isSuccess = true) {
        toastMessage.textContent = message;
        
        if (isSuccess) {
            toast.className = 'toast toast-success';
            toastIcon.innerHTML = '<i class="fas fa-check-circle"></i>';
        } else {
            toast.className = 'toast toast-error';
            toastIcon.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
        }
        
        toast.style.display = 'flex';
        
        setTimeout(() => {
            toast.style.display = 'none';
        }, 3000);
    }
    
    /**
     * Escape HTML to prevent XSS
     * @param {string} str - String to escape
     * @returns {string} Escaped string
     */
    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }
});
