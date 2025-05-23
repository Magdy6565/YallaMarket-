// Admin User Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const usersTableBody = document.getElementById('usersTableBody');
    const loadingIndicator = document.getElementById('usersLoadingIndicator');
    const noUsersMessage = document.getElementById('noUsersMessage');
    const roleFilter = document.getElementById('roleFilter');
    const statusFilter = document.getElementById('statusFilter');
    const filterBtn = document.getElementById('filterBtn');
    const prevPageBtn = document.getElementById('prevPage');
    const nextPageBtn = document.getElementById('nextPage');
    const pageInfoSpan = document.getElementById('pageInfo');
    
    // Modal elements
    const addUserModal = document.getElementById('addUserModal');
    const editUserModal = document.getElementById('editUserModal');
    const assignUserModal = document.getElementById('assignUserModal');
    const deleteUserModal = document.getElementById('deleteUserModal');
    const restoreUserModal = document.getElementById('restoreUserModal');
    
    // Button elements
    const addUserBtn = document.getElementById('addUserBtn');
    const cancelAddUserBtn = document.getElementById('cancelAddUser');
    const cancelEditUserBtn = document.getElementById('cancelEditUser');
    const cancelAssignBtn = document.getElementById('cancelAssign');
    const cancelDeleteBtn = document.getElementById('cancelDeleteUser');
    const cancelRestoreBtn = document.getElementById('cancelRestoreUser');
    const confirmDeleteBtn = document.getElementById('confirmDeleteUser');
    const confirmRestoreBtn = document.getElementById('confirmRestoreUser');
    
    // Form elements
    const addUserForm = document.getElementById('addUserForm');
    const editUserForm = document.getElementById('editUserForm');
    const assignUserForm = document.getElementById('assignUserForm');
    
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
    let currentUserId = null;
    let currentFilters = {
        role: 'all',
        status: 'all'
    };
    
    // Initialize the page
    fetchUsers();
    
    // Event listeners for filter form
    filterBtn.addEventListener('click', function() {
        currentFilters.role = roleFilter.value;
        currentFilters.status = statusFilter.value;
        currentPage = 1; // Reset to first page when filtering
        fetchUsers();
    });
    
    // Pagination event listeners
    prevPageBtn.addEventListener('click', function() {
        if (currentPage > 1) {
            currentPage--;
            fetchUsers();
        }
    });
    
    nextPageBtn.addEventListener('click', function() {
        if (currentPage < totalPages) {
            currentPage++;
            fetchUsers();
        }
    });
    
    // Modal button event listeners
    addUserBtn.addEventListener('click', function() {
        openModal(addUserModal);
    });
    
    cancelAddUserBtn.addEventListener('click', function() {
        closeModal(addUserModal);
        addUserForm.reset();
    });
    
    cancelEditUserBtn.addEventListener('click', function() {
        closeModal(editUserModal);
    });
    
    cancelAssignBtn.addEventListener('click', function() {
        closeModal(assignUserModal);
    });
    
    cancelDeleteBtn.addEventListener('click', function() {
        closeModal(deleteUserModal);
    });
    
    cancelRestoreBtn.addEventListener('click', function() {
        closeModal(restoreUserModal);
    });
    
    // Close modals when clicking the X button
    modalCloseButtons.forEach(function(btn) {
        btn.addEventListener('click', function() {
            const modal = btn.closest('.modal');
            closeModal(modal);
        });
    });
    
    // Form submission handlers
    addUserForm.addEventListener('submit', function(e) {
        e.preventDefault();
        createUser();
    });
    
    editUserForm.addEventListener('submit', function(e) {
        e.preventDefault();
        updateUser();
    });
    
    assignUserForm.addEventListener('submit', function(e) {
        e.preventDefault();
        assignUser();
    });
    
    confirmDeleteBtn.addEventListener('click', function() {
        deleteUser();
    });
    
    confirmRestoreBtn.addEventListener('click', function() {
        restoreUser();
    });
      /**
     * Fetch users from the API based on current filters and pagination
     */
    function fetchUsers() {
        usersTableBody.innerHTML = '';
        loadingIndicator.style.display = 'block';
        noUsersMessage.style.display = 'none';
        
        let url = `/api/user-management/users?page=${currentPage - 1}&size=${pageSize}`;
        
        if (currentFilters.role !== 'all') {
            url += `&role=${currentFilters.role}`;
        }
        
        if (currentFilters.status !== 'all') {
            url += `&status=${currentFilters.status}`;
        }
        
        fetch(url)
            .then(response => response.json())
            .then(data => {
                loadingIndicator.style.display = 'none';
                
                if (data.content && data.content.length > 0) {
                    displayUsers(data.content);
                    updatePagination(data);
                } else {
                    noUsersMessage.style.display = 'block';
                    updatePagination({ totalPages: 0, number: 0, totalElements: 0 });
                }
            })
            .catch(error => {
                console.error('Error fetching users:', error);
                loadingIndicator.style.display = 'none';
                showToast('Error loading users. Please try again.', false);
            });
    }
    
    /**
     * Display users in the table
     * @param {Array} users - Array of user objects
     */
    function displayUsers(users) {
        usersTableBody.innerHTML = '';
        
        users.forEach(user => {
            const tr = document.createElement('tr');
            
            const isDeleted = user.isDeleted === true;
            const isActive = user.enabled === true && !isDeleted;
            
            tr.innerHTML = `
                <td>${user.id}</td>
                <td>${user.username}</td>
                <td>${user.email}</td>
                <td>${getRoleBadge(user.role)}</td>
                <td>${getStatusBadge(isActive, isDeleted)}</td>
                <td>${user.storeVendorName || '-'}</td>
                <td class="actions">
                    <button class="action-btn edit" title="Edit User" ${isDeleted ? 'disabled' : ''} onClick="editUserDetails(${user.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="action-btn assign" title="Assign to Store/Vendor" ${isDeleted ? 'disabled' : ''} onClick="openAssignModal(${user.id}, ${user.role}, '${user.username}')">
                        <i class="fas fa-link"></i>
                    </button>
                    ${isDeleted ?
                        `<button class="action-btn restore" title="Restore User" onClick="openRestoreModal(${user.id}, '${user.username}')">
                            <i class="fas fa-trash-restore"></i>
                        </button>` :
                        `<button class="action-btn delete" title="Delete User" onClick="openDeleteModal(${user.id}, '${user.username}')">
                            <i class="fas fa-trash-alt"></i>
                        </button>`
                    }
                </td>
            `;
            
            usersTableBody.appendChild(tr);
        });
    }
      /**
     * Update pagination controls based on API response
     * @param {Object} pageData - Page information from API
     */
    function updatePagination(pageData) {
        totalPages = pageData.totalPages || 0;
        currentPage = (pageData.number || 0) + 1;
        const totalElements = pageData.totalElements || 0;
        
        pageInfoSpan.textContent = `Page ${currentPage} of ${totalPages || 1} (${totalElements} total users)`;
        
        prevPageBtn.disabled = currentPage <= 1;
        nextPageBtn.disabled = currentPage >= totalPages;
    }
    
    /**
     * Create a new user
     */
    function createUser() {
        const formData = new FormData(addUserForm);
        const userData = {
            username: formData.get('username'),
            email: formData.get('email'),
            password: formData.get('password'),
            role: parseInt(formData.get('role')),
            contactInfo: formData.get('contactInfo'),
            address: formData.get('address'),
            enabled: true
        };
        
        fetch('/api/user-management/users', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(userData)
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Failed to create user');
        })
        .then(data => {
            closeModal(addUserModal);
            addUserForm.reset();
            fetchUsers();
            showToast('User created successfully!', true);
        })
        .catch(error => {
            console.error('Error creating user:', error);
            showToast('Error creating user. Please try again.', false);
        });
    }
    
    /**
     * Update an existing user
     */
    function updateUser() {
        const formData = new FormData(editUserForm);
        const userId = formData.get('id');
        const userData = {
            username: formData.get('username'),
            email: formData.get('email'),
            role: parseInt(formData.get('role')),
            contactInfo: formData.get('contactInfo'),
            address: formData.get('address'),
            enabled: formData.get('status') === 'active'
        };
        
        fetch(`/api/user-management/users/${userId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(userData)
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Failed to update user');
        })
        .then(data => {
            closeModal(editUserModal);
            fetchUsers();
            showToast('User updated successfully!', true);
        })
        .catch(error => {
            console.error('Error updating user:', error);
            showToast('Error updating user. Please try again.', false);
        });
    }
    
    /**
     * Assign a user to a store/vendor
     */
    function assignUser() {
        const formData = new FormData(assignUserForm);
        const userId = formData.get('id');
        const entityId = formData.get('entityId');
        
        const assignmentData = {
            userId: parseInt(userId),
            targetId: parseInt(entityId)
        };
        
        fetch('/api/user-management/users/assign', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(assignmentData)
        })
        .then(response => {
            if (response.ok) {
                closeModal(assignUserModal);
                fetchUsers();
                showToast('User assigned successfully!', true);
            } else {
                throw new Error('Failed to assign user');
            }
        })
        .catch(error => {
            console.error('Error assigning user:', error);
            showToast('Error assigning user. Please try again.', false);
        });
    }
    
    /**
     * Delete (soft-delete) a user
     */
    function deleteUser() {
        if (!currentUserId) return;
        
        fetch(`/api/user-management/users/${currentUserId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                closeModal(deleteUserModal);
                fetchUsers();
                showToast('User deleted successfully!', true);
            } else {
                throw new Error('Failed to delete user');
            }
        })
        .catch(error => {
            console.error('Error deleting user:', error);
            showToast('Error deleting user. Please try again.', false);
        });
    }
    
    /**
     * Restore a soft-deleted user
     */
    function restoreUser() {
        if (!currentUserId) return;
        
        fetch(`/api/user-management/users/${currentUserId}/restore`, {
            method: 'POST'
        })
        .then(response => {
            if (response.ok) {
                closeModal(restoreUserModal);
                fetchUsers();
                showToast('User restored successfully!', true);
            } else {
                throw new Error('Failed to restore user');
            }
        })
        .catch(error => {
            console.error('Error restoring user:', error);
            showToast('Error restoring user. Please try again.', false);
        });
    }
    
    /**
     * Get HTML for role badge
     * @param {number} roleId - Role ID value
     * @returns {string} HTML for role badge
     */
    function getRoleBadge(roleId) {
        let roleName = '';
        let badgeClass = '';
        
        switch (roleId) {
            case 0:
                roleName = 'Admin';
                badgeClass = 'admin';
                break;
            case 1:
                roleName = 'Vendor';
                badgeClass = 'vendor';
                break;
            case 2:
                roleName = 'Retail Store';
                badgeClass = 'retail';
                break;
            case 3:
                roleName = 'Customer';
                badgeClass = 'customer';
                break;
            default:
                roleName = 'Unknown';
                badgeClass = '';
        }
        
        return `<span class="role-badge ${badgeClass}">${roleName}</span>`;
    }
    
    /**
     * Get HTML for status badge
     * @param {boolean} isActive - Whether the user is active
     * @param {boolean} isDeleted - Whether the user is deleted
     * @returns {string} HTML for status badge
     */
    function getStatusBadge(isActive, isDeleted) {
        if (isDeleted) {
            return '<span class="status-indicator status-deleted"><i class="fas fa-ban"></i> Deleted</span>';
        } else if (isActive) {
            return '<span class="status-indicator status-active"><i class="fas fa-check-circle"></i> Active</span>';
        } else {
            return '<span class="status-indicator status-inactive"><i class="fas fa-exclamation-circle"></i> Inactive</span>';
        }
    }
    
    /**
     * Open a modal
     * @param {HTMLElement} modal - Modal element to open
     */
    function openModal(modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
    
    /**
     * Close a modal
     * @param {HTMLElement} modal - Modal element to close
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
    
    // Make these functions available globally
    window.editUserDetails = function(userId) {
        fetch(`/api/user-management/users/${userId}`)
            .then(response => response.json())
            .then(user => {
                document.getElementById('editUserId').value = user.id;
                document.getElementById('editUsername').value = user.username;
                document.getElementById('editEmail').value = user.email;
                document.getElementById('editRole').value = user.role;
                document.getElementById('editContactInfo').value = user.contactInfo || '';
                document.getElementById('editAddress').value = user.address || '';
                document.getElementById('editStatus').value = user.enabled ? 'active' : 'inactive';
                
                openModal(editUserModal);
            })
            .catch(error => {
                console.error('Error fetching user details:', error);
                showToast('Error loading user details. Please try again.', false);
            });
    };
    
    window.openAssignModal = function(userId, roleId, username) {
        currentUserId = userId;
        const entityDropdown = document.getElementById('assignEntityId');
        const assignUserLabel = document.getElementById('assignUserLabel');
        
        // Clear previous options
        entityDropdown.innerHTML = '';
        
        // Set the label based on user role
        if (roleId === 2) { // Retail Store
            assignUserLabel.textContent = `Assign ${username} to Vendor`;
            fetchVendorsForDropdown(entityDropdown);
        } else if (roleId === 1) { // Vendor
            assignUserLabel.textContent = `Assign ${username} to Store`;
            fetchRetailStoresForDropdown(entityDropdown);
        } else {
            showToast('This user role cannot be assigned to a store/vendor.', false);
            return;
        }
        
        document.getElementById('assignUserId').value = userId;
        openModal(assignUserModal);
    };
    
    window.openDeleteModal = function(userId, username) {
        currentUserId = userId;
        document.getElementById('deleteUserName').textContent = username;
        openModal(deleteUserModal);
    };
    
    window.openRestoreModal = function(userId, username) {
        currentUserId = userId;
        document.getElementById('restoreUserName').textContent = username;
        openModal(restoreUserModal);
    };
    
    /**
     * Fetch vendors for dropdown
     * @param {HTMLElement} dropdown - The select element to populate
     */
    function fetchVendorsForDropdown(dropdown) {
        fetch('/api/vendors')
            .then(response => response.json())
            .then(vendors => {
                if (vendors.length === 0) {
                    const option = document.createElement('option');
                    option.textContent = 'No vendors available';
                    option.disabled = true;
                    dropdown.appendChild(option);
                } else {
                    vendors.forEach(vendor => {
                        const option = document.createElement('option');
                        option.value = vendor.id;
                        option.textContent = vendor.username;
                        dropdown.appendChild(option);
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching vendors:', error);
                const option = document.createElement('option');
                option.textContent = 'Error loading vendors';
                option.disabled = true;
                dropdown.appendChild(option);
            });
    }
    
    /**
     * Fetch retail stores for dropdown
     * @param {HTMLElement} dropdown - The select element to populate
     */
    function fetchRetailStoresForDropdown(dropdown) {
        fetch('/api/retail-stores')
            .then(response => response.json())
            .then(stores => {
                if (stores.length === 0) {
                    const option = document.createElement('option');
                    option.textContent = 'No retail stores available';
                    option.disabled = true;
                    dropdown.appendChild(option);
                } else {
                    stores.forEach(store => {
                        const option = document.createElement('option');
                        option.value = store.id;
                        option.textContent = store.username;
                        dropdown.appendChild(option);
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching retail stores:', error);
                const option = document.createElement('option');
                option.textContent = 'Error loading stores';
                option.disabled = true;
                dropdown.appendChild(option);
            });
    }
});
