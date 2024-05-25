const btn = document.querySelector('#btn');
btn.addEventListener('click', function() {
    fetch('book.json')
        .then(res => {
            if (res.ok) {
                return res.json();
            }
            throw new Error('unable to access the specified resource');
        })
        .then(data => console.log(data.title))
        .catch(e => window.alert(e.message));
}, false);
