// --- Guard: redirect to login if not authenticated as ADMIN ---
window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        window.location.reload();
    }
});

const token = localStorage.getItem('token');
const role = localStorage.getItem('role');

if (!token || role !== 'ADMIN') {
    window.location.href = 'login.html';
}

document.getElementById('adminName').textContent = localStorage.getItem('name');

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        localStorage.clear();
        window.location.href = 'index.html';
    }
}

// --- Shared helper: attach the JWT token to every request ---
async function apiRequest(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    };
    if (body) options.body = JSON.stringify(body);

    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
    const data = await response.json().catch(() => null);

    if (!response.ok) {
        throw new Error(data?.message || 'Request failed');
    }
    return data;
}

// --- Load customers into table + dropdown ---
async function loadCustomers() {
    try {
        const customers = await apiRequest('/api/admin/customers');

        const tableBody = document.getElementById('customersTableBody');
        const dropdown = document.getElementById('depositCustomerId');

        tableBody.innerHTML = '';
        dropdown.innerHTML = '<option value="">Select customer</option>';
        document.getElementById('purchaseCustomerId').innerHTML = '<option value="">Select customer</option>';

        customers.forEach(c => {
            tableBody.innerHTML += `<tr><td>${c.id}</td><td>${c.name}</td><td>${c.email}</td><td><button onclick="viewPurchases(${c.id}, '${c.name}')" style="width:auto; margin:0; padding:6px 12px; font-size:12px;">View Purchases</button></td></tr>`;
            dropdown.innerHTML += `<option value="${c.id}">${c.name} (${c.email})</option>`;
            document.getElementById('purchaseCustomerId').innerHTML += `<option value="${c.id}">${c.name}</option>`;
        });
    } catch (err) {
        console.error(err);
    }
}

async function viewPurchases(customerId, customerName) {
    try {
        const purchases = await apiRequest(`/api/admin/customers/${customerId}/purchases`);
        document.getElementById('purchaseModalTitle').textContent = `${customerName}'s Purchases`;

        const body = document.getElementById('purchaseModalBody');
        body.innerHTML = purchases.length === 0
            ? '<tr><td colspan="3">No purchases yet</td></tr>'
            : purchases.map(p => `<tr><td>${p.productName}</td><td>Rs. ${p.priceAtPurchase}</td><td>${new Date(p.purchasedAt).toLocaleString()}</td></tr>`).join('');

        document.getElementById('purchaseModal').style.display = 'flex';
    } catch (err) {
        alert(err.message);
    }
}

function closePurchaseModal() {
    document.getElementById('purchaseModal').style.display = 'none';
}

// --- Load products into grid ---
async function loadProducts() {
    try {
        const products = await apiRequest('/api/admin/products');
        const grid = document.getElementById('productsGrid');
        grid.innerHTML = '';
        document.getElementById('purchaseProductId').innerHTML = '<option value="">Select product</option>';


        products.forEach(p => {
            grid.innerHTML += `
                <div class="product-card">
                    <h3>${p.name}</h3>
                    <div class="price">Rs. ${p.price}</div>
                </div>`;
            document.getElementById('purchaseProductId').innerHTML += `<option value="${p.id}">${p.name} - Rs. ${p.price}</option>`;
        });
    } catch (err) {
        console.error(err);
    }
}

// --- Create Customer form ---
document.getElementById('createCustomerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const msg = document.getElementById('createCustomerMsg');
    msg.textContent = '';

    const name = document.getElementById('custName').value;
    const email = document.getElementById('custEmail').value;
    const password = document.getElementById('custPassword').value;

    try {
        await apiRequest('/api/admin/customers', 'POST', { name, email, password });
        msg.textContent = 'Customer created successfully';
        msg.className = 'msg success';
        e.target.reset();
        loadCustomers();
    } catch (err) {
        msg.textContent = err.message;
        msg.className = 'msg error';
    }
});

let pendingAdminPurchase = null;

document.getElementById('purchaseForForm').addEventListener('submit', (e) => {
    e.preventDefault();

    const customerSelect = document.getElementById('purchaseCustomerId');
    const productSelect = document.getElementById('purchaseProductId');
    const customerId = customerSelect.value;
    const productId = productSelect.value;

    const msg = document.getElementById('purchaseForMsg');
    msg.textContent = '';

    if (!customerId || !productId) {
        msg.textContent = 'Please select both customer and product';
        msg.className = 'msg error';
        return;
    }

    const customerName = customerSelect.options[customerSelect.selectedIndex].text;
    const productName = productSelect.options[productSelect.selectedIndex].text;

    pendingAdminPurchase = { customerId, productId };
    document.getElementById('confirmPurchaseText').textContent = `Buy ${productName} for ${customerName}?`;
    document.getElementById('confirmPurchaseModal').style.display = 'flex';
});

function closeConfirmPurchaseModal() {
    pendingAdminPurchase = null;
    document.getElementById('confirmPurchaseModal').style.display = 'none';
}

async function proceedWithPurchase() {
    const { customerId, productId } = pendingAdminPurchase;
    closeConfirmPurchaseModal();

    const msg = document.getElementById('purchaseForMsg');
    msg.textContent = '';

    try {
        await apiRequest(`/api/admin/customers/${customerId}/purchase/${productId}`, 'POST');
        msg.textContent = 'Purchase recorded successfully';
        msg.className = 'msg success';
        document.getElementById('purchaseForForm').reset();
    } catch (err) {
        msg.textContent = err.message;
        msg.className = 'msg error';
    }
}

// --- Deposit form ---
document.getElementById('depositForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const msg = document.getElementById('depositMsg');
    msg.textContent = '';

    const customerId = document.getElementById('depositCustomerId').value;
    const amount = document.getElementById('depositAmount').value;

    if (!customerId) {
        msg.textContent = 'Please select a customer';
        msg.className = 'msg error';
        return;
    }

    try {
        await apiRequest(`/api/admin/customers/${customerId}/deposit`, 'POST', { amount: parseFloat(amount) });
        msg.textContent = 'Deposit successful';
        msg.className = 'msg success';
        e.target.reset();
    } catch (err) {
        msg.textContent = err.message;
        msg.className = 'msg error';
    }
});


// --- Add Product form ---
document.getElementById('productForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const msg = document.getElementById('productMsg');
    msg.textContent = '';

    const name = document.getElementById('prodName').value;
    const price = document.getElementById('prodPrice').value;

    try {
        await apiRequest('/api/admin/products', 'POST', { name, price: parseFloat(price) });
        msg.textContent = 'Product added successfully';
        msg.className = 'msg success';
        e.target.reset();
        loadProducts();
    } catch (err) {
        msg.textContent = err.message;
        msg.className = 'msg error';
    }
});

// --- Initial load ---
loadCustomers();
loadProducts();