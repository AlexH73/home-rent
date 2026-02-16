document.addEventListener('DOMContentLoaded', function() {

    setTimeout(() => {
        const info = document.querySelector('.info');
        if (info && !document.querySelector('.download-link')) {
            const downloadLink = document.createElement('a');
            downloadLink.href = '/v3/api-docs';
            downloadLink.className = 'download-link';
            downloadLink.textContent = '📄 Скачать OpenAPI JSON';
            downloadLink.style.display = 'inline-block';
            downloadLink.style.marginTop = '10px';
            downloadLink.style.padding = '8px 16px';
            downloadLink.style.backgroundColor = '#48bb78';
            downloadLink.style.color = 'white';
            downloadLink.style.borderRadius = '4px';
            downloadLink.style.textDecoration = 'none';
            downloadLink.target = '_blank';
            info.appendChild(downloadLink);
        }
    }, 500);
});