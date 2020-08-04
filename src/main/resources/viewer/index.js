/*Offical release of the pdfjs worker*/
pdfjsLib.GlobalWorkerOptions.workerSrc = './node_modules/pdfjs-dist/build/pdf.worker.js';
document.getElementById('file').onchange = function (event) {
  var file = event.target.files[0];
  var fileReader = new FileReader();
  fileReader.onload = function () {
    var typedarray = new Uint8Array(this.result);
    console.log(typedarray);
    const loadingTask = pdfjsLib.getDocument(typedarray);
    loadingTask.promise.then(pdf => {
      // The document is loaded here...
      //This below is just for demonstration purposes showing that it works with the moderen api
      pdf.getPage(1).then(function (page) {
        console.log('Page loaded');

        var scale = 1.5;
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
          // Returns a promise, on resolving it will return text contents of the page
          return page.getTextContent();
        }).then(function (textContent) {
          // Assign CSS to the text-layer element
          var textLayer = document.getElementById('text-layer');
          textLayer.style.left = canvas.offsetLeft
          textLayer.style.top = canvas.offsetTop
          textLayer.style.height = canvas.height
          textLayer.style.width = canvas.width

          // Pass the data to the method for rendering of text over the pdf canvas.
          pdfjsLib.renderTextLayer({
            textContent: textContent,
            container: textLayer,
            viewport: viewport,
            textDivs: []
          });
        });

      });
      //end of example code
    });

  }
  fileReader.readAsArrayBuffer(file);
}
