(function ($, undefined) {
	$.namespace('Earthserver.Client.Utilities');
	
	var callWSdefaults = {
		showLoader: false
	};
	
	Earthserver.Client.Utilities.callWS = function (url, type, options) {
		options = $.extend({}, callWSdefaults, options || {});

		var requestData = type.toLowerCase() === 'get' ? options.requestData : JSON.stringify(options.requestData);
		var contentType = options.hasOwnProperty("contentType") ? options.contentType : "application/json; charset=utf-8";
		var dataType = options.hasOwnProperty("dataType") ? options.dataType : "json";

		$.ajax({
			url: url,
			type: type,
			contentType: contentType,
			dataType: dataType,
			data: requestData,
			success: function (data) {
				//if (options.showLoader) $.hideLoader();
				
				if (options.hasOwnProperty("onSuccess")) {
					var payload = data;
					options.onSuccess(payload);
				}
			},
			error: function (data) {
				//if (options.showLoader) $.hideLoader();
				
				if (options.hasOwnProperty('onError')) {
					options.onError(data);
				}
			}
		});
		//if (options.showLoader) $.showLoader();
	};
	
	Earthserver.Client.Utilities.createColumnDefinition = function (name, caption, isHidden, isKey) {
		var columnDef = {};
		
		columnDef.Name = name;
		columnDef.Caption = caption;
		columnDef.ShortCaption = caption;
		columnDef.ColumnCSSClass = "";
		columnDef.IsHidden = isHidden;
		columnDef.IsSortable = false;
		columnDef.IsHeaderCheckable = false;
		columnDef.ShowTooltips = true;
		columnDef.Width = 0;
		columnDef.IsKey = isKey;
		
		return columnDef;
	};
	
	Earthserver.Client.Utilities.toGridPP = function () {
		var result = {};
		
		var columnDefs = [];
		
		{
			var columnDef = Earthserver.Client.Utilities.createColumnDefinition("ID", "ID", true, true);
			columnDefs.push(columnDef);
		}
		{
			var columnDef = Earthserver.Client.Utilities.createColumnDefinition("Endpoint", "Endpoint", false, false);
			columnDefs.push(columnDef);
		}
		{
			var columnDef = Earthserver.Client.Utilities.createColumnDefinition("EndpointAlias", "Endpoint Alias", false, false);
			columnDefs.push(columnDef);
		}
		{
			var columnDef = Earthserver.Client.Utilities.createColumnDefinition("SchedulePeriod", "Schedule Period", false, false);
			columnDefs.push(columnDef);
		}
		{
			var columnDef = Earthserver.Client.Utilities.createColumnDefinition("StartTime", "Start Time", false, false);
			columnDefs.push(columnDef);
		}
		{
			var columnDef = Earthserver.Client.Utilities.createColumnDefinition("EndTime", "End Time", false, false);
			columnDefs.push(columnDef);
		}
		{
			var columnDef = Earthserver.Client.Utilities.createColumnDefinition("Status", "Status", false, false);
			columnDefs.push(columnDef);
		}
		{
			var columnDef = Earthserver.Client.Utilities.createColumnDefinition("TotalElements", "Total Elements", false, false);
			columnDefs.push(columnDef);
		}
		{
			var columnDef = Earthserver.Client.Utilities.createColumnDefinition("NewElements", "New Elements", false, false);
			columnDefs.push(columnDef);
		}
		{
			var columnDef = Earthserver.Client.Utilities.createColumnDefinition("UpdatedElements", "Updated Elements", false, false);
			columnDefs.push(columnDef);
		}
		// {
		// 	var columnDef = Earthserver.Client.Utilities.createColumnDefinition("Previous Harvest Cycles", "Previous Harvest Cycles", false, false);
		// 	columnDefs.push(columnDef);
		// }
				
		result.columnDefinitions = columnDefs;
		result.gridName = 'Harvests';
		result.visibleRows = 20;
		return result;
	};
	
	Earthserver.Client.Utilities.getRetrievalFunction = function (dataCallback) {
		/// <signature>
		/// <summary>Gets a function that will be used to retrieve data.</summary>
		/// <param name="dataCallback(requestData, onSuccess, onError)" type="Function">The actual data retrieval function to call.</param>
		/// <param name="options" type="Object">An _optional_ object containing additional options (e.g. @requestEnhancer,
		/// @customCallback@) that can be used to further customize the retrieval function.</param>
		/// <returns type="Function">The function that can be supplied as a data retrieval callback to a LiveGrid.</returns>
		/// </signature>

		//Build the data retrieval callback.
		var gridRetrievalCallback = function (gridPostData, gridCallback) {
			//Format the grid request appropriately and set the request field allowing for custom overrides by the requestEnhancer.
			var gridRequest = Earthserver.Client.Utilities.toServerRequest(gridPostData);
			var requestData = { request: gridRequest };
			//Prepare the onSuccess callback and retrieve data.
			var dataRefreshCallback = function (data) {
				var gridData = Earthserver.Client.Utilities.toClientResponse(data);
				console.log(data);
				gridCallback(gridData);
			};
			dataCallback(requestData, dataRefreshCallback);
		};
		return gridRetrievalCallback;
	};
	
	Earthserver.Client.Utilities.getDataFunction = function (url) {
		var dataFunction = function (requestData, onSuccess, onError) {
			Earthserver.Client.Utilities.callWS(url, 'GET', {
			    dataType: "json",
				requestData: requestData,
				onSuccess: onSuccess,
				onError: onError,
			});
		};
		return dataFunction;
	};
	
	Earthserver.Client.Utilities.toServerRequest = function (clientRequest) {
		/// <summary> Converts a client dataTables request, to an object that the waits to receive</summary>
		var result = JSON.parse(JSON.stringify(clientRequest));	// clone object
		if (result.hasOwnProperty('start')) { result.Offset = result.start; delete result.start; }
		if (result.hasOwnProperty('length')) { result.Size = result.length; delete result.length; }
		if (result.hasOwnProperty('columns')) { delete result.columns; }
		if (result.hasOwnProperty('search')) { delete result.search; }
		return result;
	};
	
	Earthserver.Client.Utilities.toClientResponse = function (serverResponse) {
		/// <summary> Converts a server response to an object that dataTables wait to receive</summary>
		var result = JSON.parse(JSON.stringify(serverResponse)); // clone object
		console.log(serverResponse);
		$.each(result.Rows, function(index, row) {
		    row.Data.StartTime = row.Data.StartTime != "" ? new Date(row.Data.StartTime) : "";
            row.Data.EndTime = row.Data.EndTime != "" ? new Date(row.Data.EndTime) : "";
			row.Data.PreviousHarvests = row.Data.PreviousHarvests != "" ? row.Data.PreviousHarvests : "There is no previous harvest";
		});
		//Extract extra information from context object.
		var context = result["Context"];
		if (context) {
			if (context.hasOwnProperty('Total')) { result.recordsTotal = context.Total; delete context.Total; }
			if (context.hasOwnProperty('Filtered')) { result.recordsFiltered = context.Filtered; delete context.Filtered; }
		}
		if (result.hasOwnProperty('Rows')) { result.rows = result.Rows; delete result.Rows; }
		return result;
	};
	
})(jQuery);