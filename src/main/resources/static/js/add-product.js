document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("addProductForm");
  form.addEventListener("submit", function (e) {
    e.preventDefault();
    document.getElementById("successMsg").textContent = "";
    document.getElementById("errorMsg").textContent = "";
    const newProduct = {
      name: document.getElementById("name").value.trim(),
      description: document.getElementById("description").value.trim(),
      price: parseFloat(document.getElementById("price").value),
      quantity: parseInt(document.getElementById("quantity").value, 10),
      category: document.getElementById("category").value.trim(),
    };
    fetch("/api/my-products", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newProduct),
    })
      .then((res) => {
        if (res.ok) {
          document.getElementById("successMsg").textContent =
            "Product added successfully! Redirecting...";
          setTimeout(() => {
            window.location.href = "/products";
          }, 1200);
        } else {
          return res.text().then((text) => {
            throw new Error(text);
          });
        }
      })
      .catch((err) => {
        document.getElementById("errorMsg").textContent =
          err.message || "Failed to add product.";
      });
  });
});
