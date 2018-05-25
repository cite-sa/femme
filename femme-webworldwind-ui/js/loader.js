var Loader = (function () {
	
	var loader = $('<div class="loader-container"><div class="loader"></div><div>');
	var attached = false;

	var attach = function(element) {
		// loader.prependTo($(".coverage-container > .col-xs-12"));
		if (! attached) {
			loader.prependTo($("#buttons"));
			attached = true;
		}
	}

	var detach = function() {
		if (attached) {
			loader.remove();
			attached = false;
		}
	}

	return {
		attach: attach,
		detach: detach
	};
  
  })();