document.addEventListener('DOMContentLoaded', () => {
    loadProfile();

    document.getElementById('profile-form').addEventListener('submit', handleSave);
});

async function loadProfile() {
    // For MVP, assuming Investor ID 1
    // Ideally this comes from auth context
    const investorId = 1;

    try {
        const response = await axios.get(`/api/investors/${investorId}`);
        const data = response.data;

        document.getElementById('investor-id').value = data.id;
        document.getElementById('name').value = data.name;
        document.getElementById('email').value = data.email;
        document.getElementById('goal').value = data.investmentGoal;
        document.getElementById('risk').value = data.riskPreference;
        document.getElementById('profile-pic-url').value = data.profilePicUrl || '';

        document.getElementById('display-name').textContent = data.name;

        if (data.profilePicUrl) {
            const img = document.getElementById('avatar-img');
            const initials = document.getElementById('avatar-initials');
            img.src = data.profilePicUrl;
            img.style.display = 'block';
            initials.style.display = 'none';
        }

    } catch (error) {
        console.error("Error loading profile", error);
        // If not found, maybe show create specific UI or just leave blank
        if (error.response && error.response.status === 404) {
            alert("Profile not found. Please create one.");
        }
    }
}

const fileInput = document.getElementById('avatar-upload');
const file = fileInput.files[0];
let profilePicUrl = document.getElementById('avatar-img').src; // Default to existing

// If file selected, upload it first
if (file) {
    const formData = new FormData();
    formData.append('file', file);

    try {
        const uploadResponse = await axios.post(`/api/investors/${id}/avatar`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        profilePicUrl = uploadResponse.data.profilePicUrl;
        console.log("Uploaded avatar:", profilePicUrl);
    } catch (error) {
        console.error("Avatar upload failed", error);
        alert("Failed to upload profile picture, but will save other details.");
    }
}

const payload = {
    name: document.getElementById('name').value,
    email: document.getElementById('email').value,
    investmentGoal: document.getElementById('goal').value,
    riskPreference: document.getElementById('risk').value,
    profilePicUrl: profilePicUrl // This may be ignored by backend update logic if we don't send it, but safe to send
};

try {
    await axios.put(`/api/investors/${id}`, payload);
    alert("Profile updated successfully!");
    loadProfile();

    // Force refresh avatar in sidebar if needed (simple reload)
    // location.reload(); 
} catch (error) {
    console.error(error);
    alert("Failed to update profile.");
}
}

// Add listener for file input change
document.getElementById('avatar-upload').addEventListener('change', function () {
    const fileName = this.files[0] ? this.files[0].name : "No file chosen";
    document.getElementById('file-name').textContent = fileName;
});
