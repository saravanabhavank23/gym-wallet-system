// --- Guard: redirect to login if not authenticated as CUSTOMER ---

window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        window.location.reload();
    }
});

const token = localStorage.getItem('token');
const role = localStorage.getItem('role');

if (!token || role !== 'CUSTOMER') {
    window.location.href = 'login.html';
}

document.getElementById('customerName').textContent = localStorage.getItem('name');


function logout() {
     if (confirm('Are you sure you want to logout?')) {
        localStorage.clear();
        window.location.href = 'login.html';
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

// --- Load wallet balance ---
async function loadWallet() {
    try {
        const wallet = await apiRequest('/api/customer/wallet');
        document.getElementById('walletBalance').textContent = wallet.balance.toFixed(2);
    } catch (err) {
        console.error(err);
    }
}

// --- Load products, each with a Buy button ---
async function loadProducts() {
    try {
        const products = await apiRequest('/api/customer/products');
        const grid = document.getElementById('productsGrid');
        grid.innerHTML = '';

        products.forEach(p => {
            grid.innerHTML += `
                <div class="product-card">
                    <h3>${p.name}</h3>
                    <div class="price">Rs. ${p.price}</div>
                    <button onclick="buyProduct(${p.id}, '${p.name}', ${p.price})">Buy</button>
                </div>`;
        });
    } catch (err) {
        console.error(err);
    }
}

// --- Buy a product ---
let pendingProductId = null;

function buyProduct(productId, productName, price) {
    pendingProductId = productId;
    document.getElementById('modalProductName').textContent = productName;
    document.getElementById('modalProductPrice').textContent = `Rs. ${price} will be deducted from your wallet`;
    document.getElementById('confirmModal').style.display = 'flex';
}

function closeModal() {
    pendingProductId = null;
    document.getElementById('confirmModal').style.display = 'none';
}

async function confirmPurchase() {
    const msg = document.getElementById('purchaseMsg');
    msg.textContent = '';
    const productId = pendingProductId;
    closeModal();

    try {
        await apiRequest(`/api/customer/purchase/${productId}`, 'POST');
        msg.textContent = 'Purchase successful!';
        msg.className = 'msg success';
        loadWallet();
        loadPurchaseHistory();
    } catch (err) {
        msg.textContent = err.message;
        msg.className = 'msg error';
    }
}

// --- Load purchase history ---
async function loadPurchaseHistory() {
    try {
        const purchases = await apiRequest('/api/customer/purchases');
        const tableBody = document.getElementById('purchaseHistoryBody');
        tableBody.innerHTML = '';

        purchases.forEach(p => {
            const date = new Date(p.purchasedAt).toLocaleString();
            tableBody.innerHTML += `<tr><td>${p.productName}</td><td>Rs. ${p.priceAtPurchase}</td><td>${date}</td></tr>`;
        });
    } catch (err) {
        console.error(err);
    }
}

// --- Initial load ---
loadWallet();
loadProducts();
loadPurchaseHistory();