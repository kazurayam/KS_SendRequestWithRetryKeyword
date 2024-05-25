const result = document.querySelector('#result');
document.querySelector('#btn').addEventListener('click', function() {
    const params = new URLSearchParams();
    params.set('url', document.querySelector('#url').value);
    result.textContent = '通信中...';
    fetch(`bookmark?${params.toString()}`)
        .then(res => res.json())
        .then(data => {
            const ul = document.createElement('ul');
            for (const bm of data.bookmarks) {
                const li = document.createElement('li');
                const anchor = document.createElement('a');
                anchor.href = `https://b.hatena.ne.jp/${bm.user}`;
                anchor.textContent = `${bm.user} ${bm.comment}`;
                li.append(anchor);
                ul.append(li);
            }
            result.replaceChild(ul, result.firstChild)
        })
        .catch(ex => console.log(ex));
}, false);
