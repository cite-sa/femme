var FemmeClient = (function () {
	
	// var serverUrl = "http://localhost";
	var serverUrl = "http://earthserver-devel.vhosts.cite.gr/"

	var femmePort = "";
	var femmeGeoPort = "";
	// var femmePort = ":8080/";
	// var femmeGeoPort = ":8083/";

	var femmeUrl = serverUrl + femmePort + "femme/";
	var femmeGeoUrl = serverUrl + femmeGeoPort + "femme-geo/";

	var getDataElementById = function(dataElementId, successCallback, errorCallback) {
		Earthserver.Client.Utilities.callWS(femmeUrl + "dataElements/" + dataElementId, 'GET', {
			dataType: "json",
			onSuccess: function (response) {
				if (successCallback) successCallback(response.entity.body);
			},
			onError: function () {
				if (errorCallback) errorCallback();
			}
		});
	};
	
	var getAllCoverages = function(successCallback, errorCallback) {
		Earthserver.Client.Utilities.callWS(femmeGeoUrl + "coverages", 'GET', {
			dataType: "json",
			onSuccess: function (coverages) {
				if (successCallback) successCallback(coverages);
			},
			onError: function () {
				if (errorCallback) errorCallback();
			}
		});
	};

	var getCoveragesByIds = function(ids, successCallback, errorCallback) {
		var idParameters = "?id=" + ids.join("&id=");
		Earthserver.Client.Utilities.callWS(femmeGeoUrl + "coverages/list" + idParameters, 'GET', {
			dataType: "json",
			onSuccess: function (coverages) {
				if (successCallback) successCallback(coverages);
			},
			onError: function () {
				if (errorCallback) errorCallback();
			}
		});
	};

	var getCoveragesByServer = function(serverId, successCallback, errorCallback) {
		Earthserver.Client.Utilities.callWS(femmeGeoUrl + "servers/" + serverId + "/coverages", 'GET', {
			dataType: "json",
			onSuccess: function (coverages) {
				if (successCallback) successCallback(coverages);
			},
			onError: function () {
				if (errorCallback) errorCallback();
			}
		});
	};

	var getCoveragesIntersectingOrWithinBBox = function(bbox, successCallback, errorCallback) {
		Earthserver.Client.Utilities.callWS(femmeGeoUrl + "coverages/intersects", 'POST', {
			dataType: "json",
			requestData: bbox,
			onSuccess: function (coverages) {
				if (successCallback) successCallback(coverages);
			},
			onError: function () {
				if (errorCallback) errorCallback();
			}
		});
	};

	var getServersByCrs = function(crs, successCallback, errorCallback) {
		Earthserver.Client.Utilities.callWS(femmeGeoUrl + "servers?crs=" + crs, 'GET', {
			dataType: "json",
			contentType: undefined,
			onSuccess: function (servers) {
				if (successCallback) successCallback(servers);
			},
			onError: function () {
				if (errorCallback) errorCallback();
			}
		});
	};

	return {
		getDataElementById: getDataElementById,
		getAllCoverages: getAllCoverages,
		getCoveragesByIds: getCoveragesByIds,
		getCoveragesByServer: getCoveragesByServer,
		getCoveragesIntersectingOrWithinBBox: getCoveragesIntersectingOrWithinBBox,
		getServersByCrs: getServersByCrs
	};
  })();