(function ($, undefined) {
	$.extend({
		namespace: function (str, root) {
			var parts = str.split('.');
			if (!root)
				root = window;
			var parent = root, pl, i;
			pl = parts.length;
			for (i = 0; i < pl; i++) {
				//create a property if it doesnt exist
				//ptritakis: changed to use undefined property from function closure. We could always do string comparison but at least it should be with ===.
				//if (typeof parent[parts[i]] == 'undefined') {
				if (parent[parts[i]] === undefined) {
					parent[parts[i]] = {};
				}
				parent = parent[parts[i]];
			}
			return parent;
		}
	});
})(jQuery);
