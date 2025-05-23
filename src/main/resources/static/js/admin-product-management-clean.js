// Admin Product Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const productsTableBody = document.getElementById('productsTableBody');
    const loadingIndicator = document.getElementById('productsLoadingIndicator');
    const noProductsMessage = document.getElementById('noProductsMessage');
    const categoryFilter = document.getElementById('categoryFilter');
    const productIdFilter = document.getElementById('productIdFilter');
    const filterBtn = document.getElementById('filterBtn');
    const clearFilterBtn = document.getElementById('clearFilterBtn');
    const prevPageBtn = document.getElementById('prevPage');
    const nextPageBtn = document.getElementById('nextPage');
    const pageInfoSpan = document.getElementById('pageInfo');
    
    // Modal elements
    const editProductModal = document.getElementById('editProductModal');
    const productDetailsModal = document.getElementById('productDetailsModal');
    
    // Button elements
    const cancelEditProductBtn = document.getElementById('cancelEditProduct');
    const closeProductDetailsBtn = document.getElementById('closeProductDetails');
    
    // Form elements
    const editProductForm = document.getElementById('editProductForm');
    
    // Toast notification
    const toast = document.getElementById('toastNotification');
    const toastMessage = document.getElementById('toastMessage');
    const toastIcon = document.getElementById('toastIcon');
    
    // Modal close elements
    const modalCloseButtons = document.querySelectorAll('.modal-close');
    
    // State management
    let currentPage = 1;
    let totalPages = 1;
    let pageSize = 10;
    window.currentProductId = null;
    let currentFilters = {
        category: '',
        productId: ''
    };
    
    // Initialize the page
    initializeDropdowns();
    fetchProducts();

    // Event listeners for filter form
    filterBtn.addEventListener('click', function() {
        currentFilters.category = categoryFilter.value;
        currentFilters.productId = productIdFilter.value;
        currentPage = 1; // Reset to first page when filtering
        fetchProducts();
    });

    clearFilterBtn.addEventListener('click', function() {
        categoryFilter.value = '';
        productIdFilter.value = '';
        currentFilters = {
            category: '',
            productId: ''
        };
        currentPage = 1;
        fetchProducts();
    });
    
    // Pagination event listeners
    prevPageBtn.addEventListener('click', function() {
        if (currentPage > 1) {
            currentPage--;
            fetchProducts();
        }
    });
    
    nextPageBtn.addEventListener('click', function() {
        if (currentPage < totalPages) {
            currentPage++;
            fetchProducts();
        }
    });
    
    // Modal button event listeners
    cancelEditProductBtn.addEventListener('click', function() {
        closeModal(editProductModal);
    });
    
    closeProductDetailsBtn.addEventListener('click', function() {
        closeModal(productDetailsModal);
    });
    
    // Close modals when clicking the X button
    modalCloseButtons.forEach(function(btn) {
        btn.addEventListener('click', function() {
            const modal = btn.closest('.modal');
            closeModal(modal);
        });
    });
    
    // Form submission handlers
    editProductForm.addEventListener('submit', function(e) {
        e.preventDefault();
        updateProduct();
    });
    
    /**
     * Initialize category dropdowns
     */
    function initializeDropdowns() {
        // Fetch categories for dropdown
        fetch('/api/admin/products/categories')
            .then(response => response.json())
            .then(categories => {
                populateCategoryDropdowns(categories);
            })
            .catch(error => {
                console.error('Error fetching categories:', error);
            });
    }
    
    /**
     * Populate category dropdown options
     */
    function populateCategoryDropdowns(categories) {
        const categoryOptions = categories.map(category => 
            `<option value="${category}">${category}</option>`
        ).join('');
        
        // For filter
        categoryFilter.innerHTML = '<option value="">All Categories</option>' + categoryOptions;
        
        // For edit form
        const editCategoryDropdown = document.getElementById('editCategory');
        if (editCategoryDropdown) {
            editCategoryDropdown.innerHTML = '<option value="">Select Category</option>' + categoryOptions;
        }
    }

    /**
     * Fetch products from the API based on current filters and pagination
     */
    function fetchProducts() {
        productsTableBody.innerHTML = '';
        loadingIndicator.style.display = 'block';
        noProductsMessage.style.display = 'none';
        
        let url = `/api/admin/products?page=${currentPage - 1}&size=${pageSize}`;
        
        if (currentFilters.category) {
            url += `&category=${encodeURIComponent(currentFilters.category)}`;
        }
        
        if (currentFilters.productId) {
            url += `&productId=${currentFilters.productId}`;
        }
        
        fetch(url)
            .then(response => response.json())
            .then(data => {
                loadingIndicator.style.display = 'none';
                
                if (data.content && data.content.length > 0) {
                    displayProducts(data.content);
                    updatePagination(data);
                } else {
                    noProductsMessage.style.display = 'block';
                    updatePagination({ totalPages: 0, number: 0, totalElements: 0 });
                }
            })
            .catch(error => {
                console.error('Error fetching products:', error);
                loadingIndicator.style.display = 'none';
                showToast('Error loading products. Please try again.', false);
            });
    }
    
    /**
     * Display products in the table
     */
    function displayProducts(products) {
        productsTableBody.innerHTML = '';
        
        products.forEach(product => {
            const tr = document.createElement('tr');
            
            const isDeleted = product.isDeleted === true;
            const isInStock = product.quantity > 0 && !isDeleted;
            
            tr.innerHTML = `
                <td>${product.productId}</td>
                <td>${escapeHtml(product.name)}</td>
                <td>${escapeHtml(product.category)}</td>
                <td>$${product.price.toFixed(2)}</td>
                <td>${product.quantity}</td>
                <td>${getStatusBadge(isInStock, isDeleted)}</td>
                <td class="actions">
                    <button class="action-btn view" title="View Product Details" onClick="viewProductDetails(${product.productId})">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="action-btn edit" title="Edit Product" ${isDeleted ? 'disabled' : ''} onClick="editProductDetails(${product.productId})">
                        <i class="fas fa-edit"></i>
                    </button>
                    ${isDeleted ?
                        `<button class="action-btn restore" title="Restore Product" onClick="restoreProduct(${product.productId}, '${escapeHtml(product.name)}')">
                            <i class="fas fa-trash-restore"></i>
                        </button>` : ''
                    }
                </td>
            `;
            
            productsTableBody.appendChild(tr);
        });
    }
    
    /**
     * Update pagination controls
     */
    function updatePagination(pageData) {
        totalPages = pageData.totalPages || 0;
        currentPage = (pageData.number || 0) + 1;
        const totalElements = pageData.totalElements || 0;
        
        pageInfoSpan.textContent = `Page ${currentPage} of ${totalPages || 1} (${totalElements} total products)`;
        
        prevPageBtn.disabled = currentPage <= 1;
        nextPageBtn.disabled = currentPage >= totalPages;
    }
    
    /**
     * Update a product
     */
    function updateProduct() {
        const formData = new FormData(editProductForm);
        const productId = formData.get('id');
        
        const productData = {
            name: formData.get('name'),
            description: formData.get('description'),
            category: formData.get('category'),
            price: parseFloat(formData.get('price')),
            quantity: parseInt(formData.get('quantity')),
            imageUrl: formData.get('imageUrl')
        };
        
        fetch(`/api/admin/products/${productId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(productData)
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Failed to update product');
        })
        .then(data => {
            closeModal(editProductModal);
            fetchProducts();
            showToast('Product updated successfully!', true);
        })
        .catch(error => {
            console.error('Error updating product:', error);
            showToast('Error updating product. Please try again.', false);
        });
    }
    
    /**
     * Show toast notification
     */
    function showToast(message, success) {
        toastMessage.textContent = message;
        
        if (success) {
            toast.className = 'toast toast-success';
            toastIcon.innerHTML = '<i class="fas fa-check-circle"></i>';
        } else {
            toast.className = 'toast toast-error';
            toastIcon.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
        }
        
        toast.style.display = 'flex';
        
        setTimeout(() => {
            toast.style.display = 'none';
        }, 5000);
    }
    
    /**
     * Open a modal
     */
    function openModal(modal) {
        modal.style.display = 'block';
        document.body.classList.add('modal-open');
    }
    
    /**
     * Close a modal
     */
    function closeModal(modal) {
        modal.style.display = 'none';
        document.body.classList.remove('modal-open');
    }
    
    /**
     * Get status badge HTML
     */
    function getStatusBadge(isInStock, isDeleted) {
        if (isDeleted) {
            return '<span class="status-badge deleted">Deleted</span>';
        } else if (isInStock) {
            return '<span class="status-badge active">In Stock</span>';
        } else {
            return '<span class="status-badge inactive">Out of Stock</span>';
        }
    }
    
    /**
     * Escape HTML to prevent XSS
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

    // Make these functions available globally
    window.viewProductDetails = function(productId) {
        fetch(`/api/admin/products/${productId}`)
            .then(response => response.json())
            .then(product => {
                const isDeleted = product.isDeleted === true;
                const isInStock = product.quantity > 0 && !isDeleted;
                
                const detailsContainer = document.getElementById('productDetailsContent');
                
                detailsContainer.innerHTML = `
                    <h3>${escapeHtml(product.name)}</h3>
                    
                    <div class="product-details-grid">
                        <div class="detail-row">
                            <div class="detail-label">ID:</div>
                            <div class="detail-value">${product.productId}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Category:</div>
                            <div class="detail-value">${escapeHtml(product.category)}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Price:</div>
                            <div class="detail-value">$${product.price.toFixed(2)}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Quantity:</div>
                            <div class="detail-value">${product.quantity}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Status:</div>
                            <div class="detail-value status">
                                ${isDeleted ? 
                                    '<span class="status-badge deleted">Deleted</span>' : 
                                    (isInStock ? 
                                        '<span class="status-badge active">In Stock</span>' : 
                                        '<span class="status-badge inactive">Out of Stock</span>'
                                    )
                                }
                            </div>
                        </div>
                    </div>
                    
                    <div class="product-description">
                        <h4>Description:</h4>
                        <p>${escapeHtml(product.description) || 'No description available'}</p>
                    </div>
                    
                    ${product.imageUrl ? 
                        `<div class="product-image">
                            <img src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}">
                        </div>` : 
                        ''
                    }
                `;
                
                openModal(productDetailsModal);
            })
            .catch(error => {
                console.error('Error fetching product details:', error);
                showToast('Error loading product details. Please try again.', false);
            });
    };
    
    window.editProductDetails = function(productId) {
        fetch(`/api/admin/products/${productId}`)
            .then(response => response.json())
            .then(product => {
                document.getElementById('editProductId').value = product.productId;
                document.getElementById('editName').value = product.name;
                document.getElementById('editDescription').value = product.description || '';
                document.getElementById('editCategory').value = product.category;
                document.getElementById('editPrice').value = product.price;
                document.getElementById('editQuantity').value = product.quantity;
                document.getElementById('editImageUrl').value = product.imageUrl || '';
                
                openModal(editProductModal);
            })
            .catch(error => {
                console.error('Error fetching product details:', error);
                showToast('Error loading product details for editing. Please try again.', false);
            });
    };
    
    window.restoreProduct = function(productId, productName) {
        if (!confirm(`Are you sure you want to restore product "${productName}"?`)) {
            return;
        }
        
        fetch(`/api/admin/products/${productId}/restore`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                fetchProducts();
                showToast('Product restored successfully!', true);
            } else {
                throw new Error('Failed to restore product');
            }
        })
        .catch(error => {
            console.error('Error restoring product:', error);
            showToast('Error restoring product. Please try again.', false);
        });
    };
});

// Listen for the custom event
document.addEventListener('refreshProducts', function() {
    // Find and call the internal fetchProducts function
    const filterBtn = document.getElementById('filterBtn');
    if (filterBtn) {
        filterBtn.click();
    }
});
