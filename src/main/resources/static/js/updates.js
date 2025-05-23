// Updates for supermarket-basket.js
// Update the updateItemQuantity function to allow removing items when quantity reaches zero

function updateItemQuantity(productId, change, absoluteValue = null) {
  // Find item in cart - use string comparison for consistent ID handling
  const itemIndex = cartItems.findIndex(
    (item) => String(item.id) === String(productId)
  );

  if (itemIndex === -1) return;

  // Update quantity
  if (absoluteValue !== null) {
    cartItems[itemIndex].quantity = absoluteValue;
  } else {
    cartItems[itemIndex].quantity += change;
  }

  // If quantity is 0 or less, remove the item
  if (cartItems[itemIndex].quantity <= 0) {
    removeCartItem(productId); // Call existing remove function
    return; // Exit to prevent further processing for this item
  }

  // Save updated cart to localStorage
  localStorage.setItem("cartItems", JSON.stringify(cartItems));

  // Re-render cart
  renderCart();
}
