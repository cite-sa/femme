var CoverageTagsInput = (function () {
	
	var initialize = function(coverages) {
		var tagsInput = $("#coverage-tags-input");
		tagsInput.tagsinput({
			itemValue: function (item) {
				return item.name;
			},
			typeahead: {
				source: coverages,
				afterSelect: function () {
					this.$element[0].value = '';
				}
			},
			freeInput: false
		});
	
		tagsInput.on('beforeItemAdd', function (event) {
			setTimeout(function () {
				$(">input[type=text]", ".bootstrap-tagsinput").val("");
			}, 0);
		});

		$("#coverage-tags-input").val("");
        $("#coverage-tags-input").attr("placeholder", "Search for Coverages");
	};
	
	var destroy = function() {
		$("#coverage-tags-input").tagsinput('removeAll');
		var tagsInput = $("#coverage-tags-input");
		tagsInput.typeahead("destroy");
		tagsInput.tagsinput("destroy");
		tagsInput.off('itemAdded');
		coverageTags = [];
	};

	return {
		initialize: initialize,
		destroy: destroy
	};
  
  })();

