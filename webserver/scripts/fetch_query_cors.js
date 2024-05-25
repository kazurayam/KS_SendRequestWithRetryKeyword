const result = document.querySelector("#result");
document. querySelector("#myform").addEventListener('click', function(_e) {
  const params=new URLSearchParams();
  params.set('name', document.querySelector("#name").value);
  console.log(`params: ${params.toString()}`);
  fetch(`https://wings.msn.to/tmp/it/fetch_query.php?${params.toString()}`)
    .then(res => res.text())
    .then(text => result.textContent = text);
}, false);
