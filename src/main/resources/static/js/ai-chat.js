// AI Chat Logic

document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('ai-input');
    const sendBtn = document.getElementById('send-ai-btn');
    const messagesDiv = document.getElementById('chat-messages');

    if (!input || !sendBtn) return;

    const sendMessage = async () => {
        const text = input.value.trim();
        if (!text) return;

        addMessage(text, 'user');
        input.value = '';

        const loadingId = addMessage('Thinking...', 'ai', true);

        try {
            const response = await axios.post('/api/ai/chat', text, {
                headers: { 'Content-Type': 'text/plain' }
            });
            removeMessage(loadingId);
            addMessage(response.data, 'ai');
        } catch (error) {
            removeMessage(loadingId);
            addMessage('Error connecting to AI.', 'ai');
        }
    };

    sendBtn.addEventListener('click', sendMessage);
    input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    function addMessage(text, type, isLoading = false) {
        const id = Date.now();
        const div = document.createElement('div');
        div.className = `message ${type}`;
        div.id = `msg-${id}`;
        div.innerHTML = `<div class="message-content">${isLoading ? '<i class="fa-solid fa-spinner fa-spin"></i> ' : ''}${text}</div>`;
        messagesDiv.appendChild(div);
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
        return id;
    }

    function removeMessage(id) {
        const el = document.getElementById(`msg-${id}`);
        if (el) el.remove();
    }
});
