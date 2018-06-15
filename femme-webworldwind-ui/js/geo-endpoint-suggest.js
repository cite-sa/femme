var GeoEndpointSuggest = (function () {
	
	var initialize = function() {
		var self = this;
		this.asgEndpoint = $('.asgEndpoint');
	
		this.asgEndpoint.CiteAutoSuggest({
			currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
			suggestionMode: $.ui.CiteAutoSuggest.SuggestionMode.Callback,
			uiMode: $.ui.CiteAutoSuggest.UIMode.TextBox,
			selectionNameProperty: 'serverName',
			selectionValueProperty: 'id',
			allowOnlySuggestionSelections: false,
			allowMultiSelect: true,
			highlightMatches: true,
			watermarkText: 'Available WCS Servers',
			retrieveSuggestionsCallback: function (text, filters, limit, callback) {
				FemmeClient.getServersByCrs("EPSG:4326",
					(servers) => {
						callback(servers);
						numberOfWCSServerEndpoints = servers.length;
						return numberOfWCSServerEndpoints;
					}, () => {
						numberOfWCSServerEndpoints = 0;
						alert("No endpoints available");
						return numberOfWCSServerEndpoints;
					}
				);
			},
			maximumSelections: function (numberOfWCSServerEndpoints) {
				return numberOfWCSServerEndpoints.value;
			},
			minimumSelections: 0,
			autoInitialize: true,
		});
	
		this.asgEndpoint.bind('selectionchanged', function (e) {
			// clearCoverages();
			CoverageTagsInput.destroy();
			
			var serverIds = self.asgEndpoint.CiteAutoSuggest('getSelectedValues');
	
			var coveragesRetrieved = {};
			serverIds.forEach(serverId => coveragesRetrieved[serverId] = false);
	
			var allCoverages = [];
			serverIds.forEach(serverId => {
				FemmeClient.getCoveragesByServer(serverId, (coverages) => {
					coveragesRetrieved[serverId] = true;
					allCoverages = allCoverages.concat(coverages);
		
					if (allCoveragesRetieved(coveragesRetrieved)) {
						CoverageTagsInput.destroy();
						CoverageTagsInput.initialize(allCoverages);
					}
				}, () => {
					console.log("Error on coverages retrieval for server [" + serverId + "]");
				})
			});
	
		});
	};

	var allCoveragesRetieved = function(coveragesRetrieved) {
		var allRetrieved = true;
		Object.values(coveragesRetrieved).forEach(coveragesRetrieved => allRetrieved = allRetrieved && coveragesRetrieved);
		return allRetrieved;
	};

	return {
		initialize: initialize
	};

})();