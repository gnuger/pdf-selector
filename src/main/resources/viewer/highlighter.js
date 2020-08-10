var scale = 1.5;

pdfjsLib.GlobalWorkerOptions.workerSrc = './node_modules/pdfjs-dist/build/pdf.worker.js';
document.getElementById('pdf-file').onchange = function (event) {
  var file = event.target.files[0];
  var fileReader = new FileReader();
  fileReader.onload = function () {
    var typedArray = new Uint8Array(this.result);
    console.log(typedArray);
    const loadingTask = pdfjsLib.getDocument(typedArray);
    loadingTask.promise.then(pdf => {

      pdf.getPage(1).then(function (page) {
        console.log('Page loaded');

        var viewport = page.getViewport({
          scale: scale
        });

        var canvas = document.getElementById('pdf-canvas');
        var context = canvas.getContext('2d');
        canvas.height = viewport.height;
        canvas.width = viewport.width;

        // Render PDF page into canvas context
        var renderContext = {
          canvasContext: context,
          viewport: viewport
        };
        var renderTask = page.render(renderContext);

        renderTask.promise.then(function () {
          console.log('Page rendered');
          return page.getTextContent();
        }).then(function (textContent) {
          var textLayer = document.getElementById('text-layer');
          textLayer.style.left = canvas.offsetLeft
          textLayer.style.top = canvas.offsetTop
          textLayer.style.height = canvas.height
          textLayer.style.width = canvas.width

          pdfjsLib.renderTextLayer({
            textContent: textContent,
            container: textLayer,
            viewport: viewport,
            textDivs: []
          });
        });

      });
    });

  }
  fileReader.readAsArrayBuffer(file);
}

document.getElementById('highlight').onsubmit = function(e) {
  console.log("submitted form")

  var formEl = document.forms.highlight

  var formData = new FormData(formEl);

  console.log("retrieved form data")
  const options = {
    method: 'POST',
    body: formData,
  };

  fetch('/select2', options)
  .then(res => res.json())
  .then(response => {
    console.log('Success:', response.text)

    document.getElementById('preview').appendChild(document.createTextNode(response.text))

    var textLayer = document.getElementById('text-layer');
    var annotationLayer = document.getElementById('annotation-layer');
    annotationLayer.style.left = textLayer.style.left
    annotationLayer.style.top = textLayer.style.top
    annotationLayer.style.height = textLayer.style.height
    annotationLayer.style.width = textLayer.style.width

    var position = response.positions[0]
    annotationLayer.appendChild(rectangleElement(position.boundingRect, 'yellow'));
    position.rects.forEach(rect => annotationLayer.appendChild(rectangleElement(rect, 'cyan')))
   })
  .catch(error => console.error('Error:', error));

  return false;
}

function rectangleElement(rect, color) {
  var rectEl = document.createElement("span")
  rectEl.style.left = rect.x1 * scale
  rectEl.style.bottom = rect.y1 * scale
  rectEl.style.height = rect.height * scale
  rectEl.style.width = rect.width * scale
  rectEl.style.background = color

  return rectEl;
}
