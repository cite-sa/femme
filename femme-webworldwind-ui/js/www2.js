(function ($, undefined) {
	$.namespace("Earthserver.WebWorldWind");

	// var wwd = undefined;
	var polygonsLayer = undefined;
	var annotationsLayer;
	var annotation = {};
	var templatePolygons = [];
	var bigNavRange = 50e5;
	var smallNavRange = 10e5;

	Earthserver.WebWorldWind.initialize = function() {
		wwd = new WorldWind.WorldWindow("coverageCanvas");
		wwd.deepPicking = true;
		wwd.navigator.range = 200e5;
		WorldWind.Logger.setLoggingLevel(WorldWind.Logger.LEVEL_WARNING);
		WorldWind.configuration.gpuCacheSize = 300e6;
	
		// wwd.addLayer(new WorldWind.BingAerialLayer());
		wwd.addLayer(new WorldWind.BMNGLayer());
		wwd.addLayer(new WorldWind.CoordinatesDisplayLayer(wwd));
		// wwd.addLayer(new WorldWind.AtmosphereLayer());
	
		polygonsLayer = new WorldWind.RenderableLayer("CoveragesLayer");
		polygonsLayer.enabled = true;
		polygonsLayer.pickEnabled = true;
		wwd.addLayer(polygonsLayer);
	
		annotationsLayer = new WorldWind.RenderableLayer("AnnotationsLayer");
		annotationsLayer.enabled = true;
		annotationsLayer.pickEnabled = true;
		wwd.addLayer(annotationsLayer);
	
		eventHandler();
	};

	Earthserver.WebWorldWind.clear = function() {
		polygonsLayer.removeAllRenderables();
    	annotationsLayer.removeAllRenderables();
	}

	var highlightedItems = [];

	var eventHandler = function() {
		// var highlightedItems = [];

		var clickPickRecognizer = new WorldWind.ClickRecognizer(wwd, handlePick);
		clickPickRecognizer.enabled = true;
	
		var clickHoverRecognizer = new WorldWind.ClickRecognizer(wwd, handleHover);
		clickHoverRecognizer.enabled = true;
	
		var tapPickRecognizer = new WorldWind.TapRecognizer(wwd, handlePick);
		tapPickRecognizer.enabled = true;

		var tapHoverRecognizer = new WorldWind.TapRecognizer(wwd, handleHover);
		tapHoverRecognizer.enabled = true;
	
		wwd.addEventListener("mouseclick", handlePick);
		wwd.addEventListener("mousemove", handleHover);
	
		wwd.redraw();
	}

	var handlePick = function(recognizer) {
		
		var x = recognizer.clientX,
			y = recognizer.clientY;

		var redrawRequired = highlightedItems.length > 0;

		for (var h = 0; h < highlightedItems.length; h++) {
			highlightedItems[h].highlighted = false;
		}

		highlightedItems = [];

		var pickList = wwd.pick(wwd.canvasCoordinates(x, y));
		if (pickList.objects.length > 0) {
			redrawRequired = true;
		}
		if (pickList.objects.length > 0) {
			for (var p = 0; p < pickList.objects.length; p++) {

				console.log(pickList.objects[p]);
				if (pickList.objects[p].isTerrain == true) {
					console.log("You picked Terrain")
				}
				else if (pickList.objects[p].userObject.pickDelegate === undefined) {
					if (pickList.objects[p].userObject.type == "coverage") {
						var coverageId = pickList.objects[p].userObject.id;
						fillMetadataModalonClick(coverageId);
						$("#metadata-modal").modal("show");
					}
				} else if (pickList.objects[p].userObject.pickDelegate.type == "coverage") {
					var coverageId = pickList.objects[p].userObject.pickDelegate.id;
					fillMetadataModalonClick(coverageId);
					$("#metadata-modal").modal("show");
				} else if (pickList.objects[p].userObject.pickDelegate.type == "annotation") {
					return false;
				}
			}
		}
		if (redrawRequired) {
			wwd.redraw();
		}
	};

	var handleHover = function(recognizer) {
		var x = recognizer.clientX,
			y = recognizer.clientY;

		var redrawRequired = highlightedItems.length > 0;

		for (var h = 0; h < highlightedItems.length; h++) {
			highlightedItems[h].highlighted = false;
		}

		highlightedItems = [];

		var pickList = wwd.pick(wwd.canvasCoordinates(x, y));

		if (pickList.objects.length == 0) {
			return false;
		}

		if (pickList.objects.length > 0) {
			redrawRequired = true;
		}

		if (pickList.objects.length > 0) {
			for (var p = 0; p < pickList.objects.length; p++) {
				if (pickList.objects[p].isTerrain == true) {
					if (annotation !== null) {
						deleteAnnotations();
					}
				} else if (pickList.objects[p].userObject.pickDelegate != undefined && pickList.objects[p].userObject.pickDelegate.type == "coverage") {
					drawAnnotation(pickList.objects[p].userObject);
				} else if (pickList.objects[p].userObject.pickDelegate != undefined && pickList.objects[p].userObject.pickDelegate.type == "annotation") {
					return false;
				}
			}
		}

		if (redrawRequired) {
			wwd.redraw();
		}
	};

	var drawAnnotation = function(polygon) {
		var annotationAttributes = new WorldWind.AnnotationAttributes(null);
		annotationAttributes.cornerRadius = 8;
		annotationAttributes.opacity = 0.5;
		annotationAttributes.scale = 1;
		annotationAttributes.width = 200;
		annotationAttributes.height = 100;
		annotationAttributes.textAttributes.color = WorldWind.Color.RED;
		annotationAttributes.insets = new WorldWind.Insets(5, 5, 5, 5);
		annotationAttributes.cornerRadius = 14;
		annotationAttributes.textColor = new WorldWind.Color(1, 0, 0, 1);
	
		if (polygon._boundaries != undefined) {
			var centroid = getCentroid(polygon._boundaries[0].latitude, polygon._boundaries[0].longitude, polygon._boundaries[2].latitude, polygon._boundaries[2].longitude);
			var position = new WorldWind.Position(centroid[0], centroid[1], 0);
		} else {
			var position = new WorldWind.Position(polygon.position.latitude, polygon.position.longitude, 0);
		}
	
		annotation = new WorldWind.Annotation(position, annotationAttributes);
		annotation.displayName = polygon.label;
	
		// if (polygon._boundaries != undefined){
		// 	annotation.label = "Name" + "\n" + polygon.label + "\n" + /*"Bounding Box (lat,lon) = " + "\n" +
		// 		"[("+ polygon._boundaries[0].latitude + "," + polygon._boundaries[0].longitude + ")" + "\t" +
		// 		"(" + polygon._boundaries[1].latitude + "," + polygon._boundaries[1].longitude + ")" + "\t" +
		// 		"(" + polygon._boundaries[2].latitude + "," + polygon._boundaries[2].longitude + ")" + "\t" +
		// 		"(" + polygon._boundaries[3].latitude + "," + polygon._boundaries[3].longitude + ")]"+ "\n" +
		// 		*/"DELETED";
		// }
		// else {
		// 	annotation.label = "Name" + "\n" + polygon.label + "\n" + /*"(lat,lon) = " + "\n" +
		// 		"[("+ position.latitude + "," + position.longitude + ")" + "\n" +*/
		// 		"DELETED";
		// }
	
		annotation.label = polygon.label + "\n" + "DELETED";
		annotationsLayer.addRenderable(annotation);
	
		if (centroid === undefined) {
			wwd.goTo(new WorldWind.Location(polygon.position.latitude, polygon.position.longitude, bigNavRange));
		}
		else {
			wwd.goTo(new WorldWind.Location(centroid[0], centroid[1], bigNavRange));
		}
		wwd.redraw();
		return annotation;
	};

	var getCentroid = function(x1, y1, x2, y2) {
		return [x, y] = [(x2 + x1) / 2, (y2 + y1) / 2];
	};

	
	Earthserver.WebWorldWind.drawSchema = function (coverage) {
		var schema = undefined;
		console.log(coverage.geometry.coordinates[0]);
		var coverageGeography = coverage.geometry.coordinates[0];
		var centroid = getCentroid(coverageGeography[0][1], coverageGeography[0][0], coverageGeography[2][1], coverageGeography[2][0]);
		var distanceX = findDistance(coverageGeography[0][1], coverageGeography[0][0], coverageGeography[1][1], coverageGeography[1][0]);
		var distanceY = findDistance(coverageGeography[1][1], coverageGeography[1][0], coverageGeography[2][1], coverageGeography[2][0]);
		var area = distanceX * distanceY;
		var height = 0;
	
		if (area > 0) {
			schema = [];
			schema[1] = drawPlacemark(coverage, coverageGeography);
			schema[0] = drawPolygon(coverage, coverageGeography, area, centroid);
		} else if (area == 0) {
			schema = drawPlacemark(coverage, coverageGeography);
		}
		
		if (schema != undefined) {
			return schema;
		}
	};

	var drawPolygon = function(coverage, coverageGeography, area, centroid) {
		var coverageId = coverage.id;
		var coverageName = coverage.name;
		var boundaries = [];
		var height = 0;
	
		boundaries.push(new WorldWind.Position(coverageGeography[0][1], coverageGeography[0][0], height));
		boundaries.push(new WorldWind.Position(coverageGeography[1][1], coverageGeography[1][0], height));
		boundaries.push(new WorldWind.Position(coverageGeography[2][1], coverageGeography[2][0], height));
		boundaries.push(new WorldWind.Position(coverageGeography[3][1], coverageGeography[3][0], height));
		boundaries.push(new WorldWind.Position(coverageGeography[4][1], coverageGeography[4][0], height));
	
		var attributes = new WorldWind.ShapeAttributes(null);
		attributes.outlineColor = WorldWind.Color.BLUE;
		attributes.drawInterior = false;
		attributes.interiorColor = new WorldWind.Color(1, 0, 0, 1);
		attributes.drawOutline = true;
	
		var polygon = new WorldWind.SurfacePolygon(boundaries, attributes);
		polygon.enabled = true;
		polygon.displayName = coverageName;
		polygon.opacity = 0;
		polygon.name = coverageName;
		polygon.pickEnabled = true;
	
		var highlightAttributes = new WorldWind.ShapeAttributes(attributes);
		highlightAttributes.imageScale = 0.6;
		highlightAttributes.drawInterior = true;
		highlightAttributes.outlineColor = WorldWind.Color.RED;
		highlightAttributes.interiorColor = new WorldWind.Color(1, 1, 1, 0);
		polygon.highlightAttributes = highlightAttributes;
		polygon.pathType = WorldWind.RHUMB_LINE;
		polygon.label = coverageName;
	
		polygon.pickDelegate = {
			type: "coverage",
			id: coverageId
		};
		polygonsLayer.addRenderable(polygon);
	
		if (area > 10) {
			wwd.navigator.range = bigNavRange;
		}
		else {
			wwd.navigator.range = smallNavRange;
		}
	
		wwd.goTo(new WorldWind.Location(centroid[0], centroid[1], bigNavRange));
		wwd.redraw();
		return polygon;
	};

	var drawPlacemark = function(coverage, coverageGeography) {
		var image;
		
		if (coverage.serverName.includes("MEEO")) {
			image = "images/Meteorological-Environmental-Earth-Observation.gif";
		} else if (coverage.serverName.includes("PML")) {
			image = "images/pml-logo-small-white.png";
		} else if (coverage.serverName.includes("PS")) {
			image = "images/images.jpg";
		} else if (coverage.serverName.includes("ECMWF")) {
			image = "images/ecmwf.png"
		} else {
			image = "images/castshadow-blue.png";
		}
	
		var coverageId = coverage.id;
		var centroid = getCentroid(coverageGeography[0][1], coverageGeography[0][0], coverageGeography[2][1], coverageGeography[2][0]);
	
		var placemarkAttributes = new WorldWind.PlacemarkAttributes(null);
		placemarkAttributes.depthTest = false;
		placemarkAttributes.imageScale = 0.5;
		placemarkAttributes.imageOffset = new WorldWind.Offset(WorldWind.OFFSET_FRACTION, 0.3, WorldWind.OFFSET_FRACTION, 0.0);
		placemarkAttributes.imageColor = WorldWind.Color.WHITE;
		placemarkAttributes.labelAttributes.offset = new WorldWind.Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 1.0);
		placemarkAttributes.labelAttributes.color = WorldWind.Color.WHITE;
		placemarkAttributes.drawLeaderLine = true;
		placemarkAttributes.leaderLineAttributes.outlineColor = WorldWind.Color.BLUE;
	
		placemark = new WorldWind.Placemark(new WorldWind.Position(centroid[0], centroid[1], 0), true, null);
		placemark.enabled = true;
		placemark.name = coverage.name;
		placemark.label = "Name" + "\n" + coverage.name  /* +"\n" + "Bounding Box (lat,lon) = " + "\n" +
			"[(" + coverageGeography[0][1] + "," + coverageGeography[0][0] + ")" + "\t" +
			"(" + coverageGeography[1][1] + "," + coverageGeography[1][0] + ")" + "\t" +
			"(" + coverageGeography[2][1] + "," + coverageGeography[2][0] + ")" + "\t" +
			"(" + coverageGeography[3][1] + "," + coverageGeography[3][0] + ")]"*/;
	
		placemark.altitudeMode = WorldWind.RELATIVE_TO_GLOBE;
		placemarkAttributes = new WorldWind.PlacemarkAttributes(placemarkAttributes);
		placemarkAttributes.imageSource = image;
		placemark.attributes = placemarkAttributes;
	
		highlightAttributes = new WorldWind.PlacemarkAttributes(placemarkAttributes);
		highlightAttributes.imageScale = 0.6;
		placemark.highlightAttributes = highlightAttributes;
	
		placemark.pickDelegate = {
			type: "coverage",
			id: coverageId
		};
	
		polygonsLayer.addRenderable(placemark);
	
		wwd.navigator.range = 5e5;
		wwd.goTo(new WorldWind.Location(centroid[0], centroid[1], bigNavRange));
		wwd.redraw();
	
		return placemark;
	};
	
	var findDistance = function(point1Lon, point1Lat, point2Lon, point2Lat) {
		return Math.sqrt(Math.pow((point2Lon - point1Lon), 2) + Math.pow((point2Lat - point1Lat), 2));
	};
	
	Earthserver.WebWorldWind.deleteSchema = function(schema) {
		if (schema.length === undefined) {
			polygonsLayer.removeRenderable(schema);
			drawAnnotation(schema);
			wwd.redraw();
		} else if (schema.length > 1) {
			polygonsLayer.removeRenderable(schema[0]);
			polygonsLayer.removeRenderable(schema[1]);
			drawAnnotation(schema[0]);
			wwd.redraw();
		}
	};
	
	var deleteAnnotation = function(annotation) {
		annotationsLayer.removeRenderable(annotation);
		wwd.redraw();
	};
	
	var deleteAnnotations = function() {
		annotationsLayer.removeAllRenderables();
		wwd.redraw();
	};
	
	var canvasInfo = function() {
		var canvas = document.getElementById("coverageCanvas");
		var context = canvas.getContext('webgl');
		var pixels = new Uint8Array(context.drawingBufferWidth * context.drawingBufferHeight * 4);
		context.readPixels(0, 0, context.drawingBufferWidth, context.drawingBufferHeight,
		context.RGBA, context.UNSIGNED_BYTE, pixels);
	};

})(jQuery);