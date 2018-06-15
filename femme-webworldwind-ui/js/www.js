(function ($, undefined) {
	$.namespace("Earthserver.WebWorldWind");

	var polygonsLayer = undefined;
	var searchLayer = undefined;
	var annotationsLayer = undefined;

	var annotation = {};
	var templatePolygons = [];
	var bigNavRange = 80e5;
	var smallNavRange = 30e5;

	var ctrlDown = false;
	var initialCtrlClickPosition = undefined;
	var lastCtrlClickPosition = undefined;
	var searchPolygon = undefined;

	var highlightedItems = [];

	Earthserver.WebWorldWind.initialize = function() {
		initializeWorldWindow();
		initializeLayers();
		initializeEventHandlers();
	};

	var initializeWorldWindow = function() {
		wwd = new WorldWind.WorldWindow("coverageCanvas");
		wwd.deepPicking = true;
		wwd.navigator.range = 200e5;
		WorldWind.Logger.setLoggingLevel(WorldWind.Logger.LEVEL_WARNING);
		WorldWind.configuration.gpuCacheSize = 300e6;
	};

	var initializeLayers = function() {
		// wwd.addLayer(new WorldWind.BingAerialLayer());
		wwd.addLayer(new WorldWind.BMNGLayer());
		wwd.addLayer(new WorldWind.CoordinatesDisplayLayer(wwd));

		searchLayer = new WorldWind.RenderableLayer("SearchLayer");
		searchLayer.enabled = true;
		searchLayer.pickEnabled = false;
		wwd.addLayer(searchLayer);

		polygonsLayer = new WorldWind.RenderableLayer("CoveragesLayer");
		polygonsLayer.enabled = true;
		polygonsLayer.pickEnabled = true;
		wwd.addLayer(polygonsLayer);
	
		annotationsLayer = new WorldWind.RenderableLayer("AnnotationsLayer");
		annotationsLayer.enabled = true;
		annotationsLayer.pickEnabled = true;
		wwd.addLayer(annotationsLayer);
	};

	Earthserver.WebWorldWind.clear = function() {
		searchLayer.removeAllRenderables();
		polygonsLayer.removeAllRenderables();
		annotationsLayer.removeAllRenderables();
	};

	var initializeEventHandlers = function() {
		$("body")
			.keydown(event => {
				if (event.which == 17) {
					if (! ctrlDown) {
						deleteSearchPolygon();
						ctrlDown = true;
						initialCtrlClickPosition = undefined;
						lastCtrlClickPosition = undefined;
					}
				}
			})
			.keyup(event => {
				if (event.which == 17) {
					ctrlDown = false;
					initialCtrlClickPosition = undefined;
					lastCtrlClickPosition = undefined;

					if (searchPolygon != undefined && searchPolygon.boundaries != undefined && searchPolygon.boundaries.length > 0) {
						var boundaries = searchPolygon.boundaries.map(position => [position.longitude, position.latitude]);
							
						var geoJson = {
							"type":"Polygon",
							"coordinates":[boundaries]
						};
						Femme.clearCoverages();
						getCoveragesIntersectingOrWithinBBox(geoJson);
					}
					// deleteSearchPolygon();
				}
			});

		var clickPickRecognizer = new WorldWind.ClickRecognizer(wwd, handlePick);
		clickPickRecognizer.enabled = true;
		wwd.addEventListener("mouseclick", handlePick);
	
		// var clickHoverRecognizer = new WorldWind.ClickRecognizer(wwd, handleHover);
		// clickHoverRecognizer.enabled = true;
		// wwd.addEventListener("mousemove", handleHover);

		var tapPickRecognizer = new WorldWind.TapRecognizer(wwd, handlePick);
		tapPickRecognizer.enabled = true;

		// var tapHoverRecognizer = new WorldWind.TapRecognizer(wwd, handleHover);
		// tapHoverRecognizer.enabled = true;

		wwd.addEventListener("pointerdown", (event) => {
			if (ctrlDown) {
				event.preventDefault();
				event.stopPropagation();
			}
		});

		var ctrlDragRecognizer = new WorldWind.DragRecognizer(wwd, handleCtrlDrag);
		ctrlDragRecognizer.enabled = true;
		wwd.addEventListener("pointerdown", handleCtrlDrag);
	
		wwd.redraw();
	};

	var handleCtrlDrag = function(recognizer) {
		if (ctrlDown) {
			var x = recognizer.clientX,
				y = recognizer.clientY;

			var pickList = wwd.pick(wwd.canvasCoordinates(x, y));
			if (pickList.objects.length > 0) {
				for (var i = 0; i < pickList.objects.length; i ++) {
					var pickObject = pickList.objects[i];
					
					if (! initialCtrlClickPosition) {
						initialCtrlClickPosition = pickObject.position;
						initialCtrlClickPosition.altitude = 0;
					} else {
						if (! lastCtrlClickPosition) {
							createSearchPolygon();
							lastCtrlClickPosition = pickObject.position;
							lastCtrlClickPosition.altitude = 0;

							buildBoundariesOfSearchPolygon();
							renderSearchPolygon();
						} else {
							lastCtrlClickPosition = pickObject.position;
							lastCtrlClickPosition.altitude = 0;

							buildBoundariesOfSearchPolygon();
							wwd.redraw();
						}
					}
				}	
			}
		}
	};

	var createSearchPolygon = function() {
		var attributes = new WorldWind.ShapeAttributes(null);
		attributes.outlineColor = WorldWind.Color.GREEN;
		attributes.drawInterior = true;
		attributes.interiorColor = new WorldWind.Color(0, 128, 0, 0.1);
		attributes.drawOutline = true;
	
		searchPolygon = new WorldWind.SurfacePolygon([], attributes);
		searchPolygon.enabled = true;
		searchPolygon.displayName = "search";
		searchPolygon.opacity = 0.2;
		searchPolygon.name = "search";
		searchPolygon.pickEnabled = true;
	
		var highlightAttributes = new WorldWind.ShapeAttributes(attributes);
		highlightAttributes.imageScale = 0.6;
		highlightAttributes.drawInterior = true;
		highlightAttributes.outlineColor = WorldWind.Color.RED;
		highlightAttributes.interiorColor = new WorldWind.Color(1, 1, 1, 0);
		searchPolygon.highlightAttributes = highlightAttributes;
		searchPolygon.pathType = WorldWind.RHUMB_LINE;
		searchPolygon.label = "search";
	};

	var buildBoundariesOfSearchPolygon = function() {
		var boundaries = [];

		var ctrlClickPosition1 = new WorldWind.Position(lastCtrlClickPosition.latitude, initialCtrlClickPosition.longitude, 0);
		var ctrlClickPosition2 = new WorldWind.Position(initialCtrlClickPosition.latitude, lastCtrlClickPosition.longitude, 0);

		boundaries.push(initialCtrlClickPosition, ctrlClickPosition1, lastCtrlClickPosition, ctrlClickPosition2, initialCtrlClickPosition);

		searchPolygon.boundaries = boundaries;
	}

	var renderSearchPolygon = function() {
		searchLayer.addRenderable(searchPolygon);
		wwd.redraw();
	};

	var deleteSearchPolygon = function() {
		searchLayer.removeAllRenderables();
		delete searchPolygon;
		searchPolygon = undefined;
	}

	var getCoveragesIntersectingOrWithinBBox = function(bbox) {
		Loader.attach();

		FemmeClient.getCoveragesIntersectingOrWithinBBox(bbox,
			(coverages) => {
				Loader.detach();
				displaySelectedCoverages(coverages);
			}, () => {
				console.log("Error querying for intersecting coverages");
			}
		);
	};

	var displaySelectedCoverages = function(coverages) {
		coverages.forEach(coverage => {
			Femme.createCoverageAccordion(coverage);
			$("#show").prop("disabled", false);
		});
		deleteSearchPolygon();
	};

	// var getServerName = function(coverage) {
	// 	Earthserver.Client.Utilities.callWS(femmeGeoUrl + 'servers/' + coverage.serverId, 'GET', {
	// 		dataType: "json",
	// 		onSuccess: function (server) {
	// 			coverage.serverName = server.serverName;
	// 		},
	// 		onError: function () {
	// 			alert("Error retrieving collection name");
	// 		}
	// 	});
	// };

	// var getDataElement = function(coverage) {
	// 	Earthserver.Client.Utilities.callWS(femmeUrl + 'dataElements/' + coverage.dataElementId, 'GET', {
	// 		dataType: "json",
	// 		onSuccess: function (data) {
	// 			Femme.createCoverageAccordion(coverage);
	// 			$("#show").prop("disabled", false);
	// 		},
	// 		onError: function () {
	// 			alert("Error retrieving intersecting coverages info");
	// 		}
	// 	});
	// };

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

				var pickObject = pickList.objects[p];
				if (pickObject.isTerrain) {
					handleTerrainPick(pickObject);
				// } else if (pickObject.userObject.userProperties == undefined) {
				// 	if (pickObject.userObject.type == "coverage") {
				// 		handleCoveragePick(pickObject);
				// 	}
				} else if (pickObject.userObject.userProperties.type == "coverage") {
					handleCoveragePick(pickObject);
				} else if (pickObject.userObject.userProperties.type == "annotation") {
					return false;
				}
			}
		}
		if (redrawRequired) {
			wwd.redraw();
		}
	};

	var handleTerrainPick = function(pickObject) {
		console.log("You picked Terrain");
	};

	var handleCoveragePick = function(pickObject) {
		var dataElementId = pickObject.userObject.userProperties.dataElementId;
		Femme.fillMetadataModal(dataElementId);
		$("#metadata-modal").modal("show");
	};

	// var handleHover = function(recognizer) {
	// 	var x = recognizer.clientX,
	// 		y = recognizer.clientY;

	// 	var redrawRequired = highlightedItems.length > 0;
	// 	for (var h = 0; h < highlightedItems.length; h++) {
	// 		highlightedItems[h].highlighted = false;
	// 	}

	// 	highlightedItems = [];
	// 	var pickList = wwd.pick(wwd.canvasCoordinates(x, y));
	// 	if (pickList.objects.length == 0) {
	// 		return false;
	// 	}
	// 	if (pickList.objects.length > 0) {
	// 		redrawRequired = true;
	// 	}

	// 	if (pickList.objects.length > 0) {
	// 		for (var p = 0; p < pickList.objects.length; p++) {

	// 			var pickObject = pickList.objects[p];
	// 			if (pickObject.isTerrain == true) {
	// 				if (annotation !== null) {
	// 					deleteAnnotations();
	// 				}
	// 			} else if (pickObject.userObject.userProperties != undefined && pickObject.userObject.userProperties.type == "coverage") {
	// 				// drawAnnotation(pickList.objects[p]);
	// 			} else if (pickObject.userObject.userProperties != undefined && pickObject.userObject.userProperties.type == "annotation") {
	// 				return false;
	// 			}
	// 		}
	// 	}

	// 	if (redrawRequired) {
	// 		wwd.redraw();
	// 	}
	// };

	// var drawAnnotation = function(polygon) {
	// 	var annotationAttributes = new WorldWind.AnnotationAttributes(null);
	// 	annotationAttributes.cornerRadius = 8;
	// 	annotationAttributes.opacity = 0.5;
	// 	annotationAttributes.scale = 1;
	// 	annotationAttributes.width = 200;
	// 	annotationAttributes.height = 100;
	// 	annotationAttributes.textAttributes.color = WorldWind.Color.RED;
	// 	annotationAttributes.insets = new WorldWind.Insets(5, 5, 5, 5);
	// 	annotationAttributes.cornerRadius = 14;
	// 	annotationAttributes.textColor = new WorldWind.Color(1, 0, 0, 1);
	
	// 	if (polygon.boundaries != undefined) {
	// 		var centroid = getCentroid(polygon._boundaries[0].latitude, polygon._boundaries[0].longitude, polygon._boundaries[2].latitude, polygon._boundaries[2].longitude);
	// 		var position = new WorldWind.Position(centroid[0], centroid[1], 0);
	// 	} else {
	// 		var position = new WorldWind.Position(polygon.position.latitude, polygon.position.longitude, 0);
	// 	}
	
	// 	annotation = new WorldWind.Annotation(position, annotationAttributes);
	// 	annotation.displayName = polygon.label;
	
	// 	annotation.label = polygon.label + "\n" + "DELETED";
	// 	annotationsLayer.addRenderable(annotation);
	
	// 	if (centroid === undefined) {
	// 		wwd.goTo(new WorldWind.Location(polygon.position.latitude, polygon.position.longitude, bigNavRange));
	// 	}
	// 	else {
	// 		wwd.goTo(new WorldWind.Location(centroid[0], centroid[1], bigNavRange));
	// 	}
	// 	wwd.redraw();
	// 	return annotation;
	// };

	// var deleteAnnotation = function(annotation) {
	// 	annotationsLayer.removeRenderable(annotation);
	// 	wwd.redraw();
	// };
	
	// var deleteAnnotations = function() {
	// 	annotationsLayer.removeAllRenderables();
	// 	wwd.redraw();
	// };
	
	Earthserver.WebWorldWind.drawSchema = function (coverage) {
		var coverageGeography = coverage.geometry.coordinates[0];
		var centroid = getCentroid(coverageGeography[0][1], coverageGeography[0][0], coverageGeography[2][1], coverageGeography[2][0]);
		var distanceX = findDistance(coverageGeography[0][1], coverageGeography[0][0], coverageGeography[1][1], coverageGeography[1][0]);
		var distanceY = findDistance(coverageGeography[1][1], coverageGeography[1][0], coverageGeography[2][1], coverageGeography[2][0]);
		var area = distanceX * distanceY;
	
		if (area > 0) {
			drawPlacemark(coverage, coverageGeography);
			drawPolygon(coverage, coverageGeography, area, centroid);
		} else if (area == 0) {
			drawPlacemark(coverage, coverageGeography);
		}
	};

	var drawPolygon = function(coverage, coverageGeography, area, centroid) {
		var boundaries = buildBoundaries(coverageGeography);
		var attributes = buildAttributes();
		var highlightAttributes = buildHighlightAttributes(attributes);
	
		var polygon = new WorldWind.SurfacePolygon(boundaries, attributes);
		polygon.enabled = true;
		polygon.displayName = coverage.dataElementId;
		polygon.name = coverage.dataElementId;
		polygon.pathType = WorldWind.RHUMB_LINE;
		polygon.label = coverage.dataElementId;
		polygon.highlightAttributes = highlightAttributes;
		
		polygon.userProperties = {
			type: "coverage",
			id: coverage.id,
			dataElementId: coverage.dataElementId
		};
		polygonsLayer.addRenderable(polygon);
	
		wwd.navigator.range = area > 10 ? bigNavRange : smallNavRange;
	
		wwd.goTo(new WorldWind.Location(centroid[0], centroid[1], wwd.navigator.range));
		wwd.redraw();
	};

	var buildBoundaries = function(coverageGeography) {
		var boundaries = [];
		var altitude = 0;

		boundaries.push(new WorldWind.Position(coverageGeography[0][1], coverageGeography[0][0], altitude));
		boundaries.push(new WorldWind.Position(coverageGeography[1][1], coverageGeography[1][0], altitude));
		boundaries.push(new WorldWind.Position(coverageGeography[2][1], coverageGeography[2][0], altitude));
		boundaries.push(new WorldWind.Position(coverageGeography[3][1], coverageGeography[3][0], altitude));
		boundaries.push(new WorldWind.Position(coverageGeography[4][1], coverageGeography[4][0], altitude));

		return boundaries;
	}

	var buildAttributes = function() {
		var attributes = new WorldWind.ShapeAttributes(null);
		attributes.drawInterior = true;
		attributes.interiorColor = new WorldWind.Color(0, 0, 0, 0.5);
		attributes.drawOutline = true;
		attributes.outlineColor = WorldWind.Color.BLUE;
		attributes.applyLighting = true;

		return attributes;
	}

	var buildHighlightAttributes = function(attributes) {
		var highlightAttributes = new WorldWind.ShapeAttributes(attributes);
		highlightAttributes.imageScale = 0.6;
		highlightAttributes.drawInterior = true;
		highlightAttributes.outlineColor = WorldWind.Color.RED;
		highlightAttributes.interiorColor = new WorldWind.Color(1, 1, 1, 0);

		return highlightAttributes;
	}

	Earthserver.WebWorldWind.deleteSchema = function(name) {
		var renderablesToBeDeleted = [];
		 polygonsLayer.renderables.forEach(renderable => {
			if (renderable.name == name || renderable.name == name + "-placemark") {
				renderablesToBeDeleted.push(renderable);
			}
		});

		renderablesToBeDeleted.forEach(renderable => polygonsLayer.removeRenderable(renderable));
		wwd.redraw();
	};

	Earthserver.WebWorldWind.clear = function() {
		polygonsLayer.removeAllRenderables();
		wwd.redraw();
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
		placemark.name = coverage.dataElementId + "-placemark";
		// placemark.label = coverage.dataElementId + "-placemark";
	
		placemark.altitudeMode = WorldWind.RELATIVE_TO_GLOBE;
		placemarkAttributes = new WorldWind.PlacemarkAttributes(placemarkAttributes);
		placemarkAttributes.imageSource = image;
		placemark.attributes = placemarkAttributes;
	
		highlightAttributes = new WorldWind.PlacemarkAttributes(placemarkAttributes);
		highlightAttributes.imageScale = 0.6;
		placemark.highlightAttributes = highlightAttributes;
	
		placemark.userProperties = {
			type: "coverage",
			id: coverage.id,
			dataElementId: coverage.dataElementId
		};
	
		polygonsLayer.addRenderable(placemark);
	
		wwd.navigator.range = 5e5;
		wwd.goTo(new WorldWind.Location(centroid[0], centroid[1], bigNavRange));
		wwd.redraw();
	};

	var getCentroid = function(x1, y1, x2, y2) {
		return [x, y] = [(x2 + x1) / 2, (y2 + y1) / 2];
	};

	var findDistance = function(point1Lon, point1Lat, point2Lon, point2Lat) {
		return Math.sqrt(Math.pow((point2Lon - point1Lon), 2) + Math.pow((point2Lat - point1Lat), 2));
	};
	
	var canvasInfo = function() {
		var canvas = document.getElementById("coverageCanvas");
		var context = canvas.getContext('webgl');
		var pixels = new Uint8Array(context.drawingBufferWidth * context.drawingBufferHeight * 4);
		context.readPixels(0, 0, context.drawingBufferWidth, context.drawingBufferHeight,
		context.RGBA, context.UNSIGNED_BYTE, pixels);
	};

	// var canvasManipulation = {
    //     drawStuff: function () {
    //         var canvas = document.getElementById("coverageCanvas");
    //         var img = new Image();
    //         img.src = "http://www.cite.gr/sites/default/files/logo_%28480%29.png";
    //         var context = canvas.getContext('2d');
    //         // context = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
    //         context.drawImage(img, 110, 60, 75, 30, 30, 30, 70, 80);
    //         return context;
    //     },

    //     createShaders: function () {
    //         var img, tex, vloc, tloc, vertexBuff, texBuff;

    //         var cvs3d = document.getElementById('coverageCanvas');
    //         var ctx3d = cvs3d.getContext('experimental-webgl');
    //         var uLoc;

    //         var vertexShaderSrc =
    //             "attribute vec2 aVertex;" +
    //             "attribute vec2 aUV;" +
    //             "varying vec2 vTex;" +
    //             "uniform vec2 pos;" +
    //             "void main(void) {" +
    //             "  gl_Position = vec4(aVertex + pos, 0.0, 1.0);" +
    //             "  vTex = aUV;" +
    //             "}";

    //         var fragmentShaderSrc =
    //             "precision highp float;" +
    //             "varying vec2 vTex;" +
    //             "uniform sampler2D sampler0;" +
    //             "void main(void){" +
    //             "  gl_FragColor = texture2D(sampler0, vTex);" +
    //             "}";

    //         var vertShaderObj = ctx3d.createShader(ctx3d.VERTEX_SHADER);
    //         var fragShaderObj = ctx3d.createShader(ctx3d.FRAGMENT_SHADER);
    //         ctx3d.shaderSource(vertShaderObj, vertexShaderSrc);
    //         ctx3d.shaderSource(fragShaderObj, fragmentShaderSrc);
    //         ctx3d.compileShader(vertShaderObj);
    //         ctx3d.compileShader(fragShaderObj);

    //         var progObj = ctx3d.createProgram();
    //         ctx3d.attachShader(progObj, vertShaderObj);
    //         ctx3d.attachShader(progObj, fragShaderObj);

    //         ctx3d.linkProgram(progObj);
    //         ctx3d.useProgram(progObj);

    //         ctx3d.viewport(0, 0, 1024, 768);

    //         vertexBuff = ctx3d.createBuffer();
    //         ctx3d.bindBuffer(ctx3d.ARRAY_BUFFER, vertexBuff);
    //         ctx3d.bufferData(ctx3d.ARRAY_BUFFER, new Float32Array([-1, 1, -1, -1, 1, -1, 1, 1]), ctx3d.STATIC_DRAW);

    //         texBuff = ctx3d.createBuffer();
    //         ctx3d.bindBuffer(ctx3d.ARRAY_BUFFER, texBuff);
    //         ctx3d.bufferData(ctx3d.ARRAY_BUFFER, new Float32Array([0, 1, 0, 0, 1, 0, 1, 1]), ctx3d.STATIC_DRAW);

    //         vloc = ctx3d.getAttribLocation(progObj, "aVertex");
    //         tloc = ctx3d.getAttribLocation(progObj, "aUV");
    //         uLoc = ctx3d.getUniformLocation(progObj, "pos");

    //         img = new Image();
    //         img.src = "http://www.cite.gr/sites/default/files/logo_%28480%29.png";

    //         img.onload = function () {
    //             tex = ctx3d.createTexture();
    //             ctx3d.bindTexture(ctx3d.TEXTURE_2D, tex);
    //             ctx3d.texParameteri(ctx3d.TEXTURE_2D, ctx3d.TEXTURE_MIN_FILTER, ctx3d.NEAREST);
    //             ctx3d.texParameteri(ctx3d.TEXTURE_2D, ctx3d.TEXTURE_MAG_FILTER, ctx3d.NEAREST);
    //             ctx3d.texImage2D(ctx3d.TEXTURE_2D, 0, ctx3d.RGBA, ctx3d.RGBA, ctx3d.UNSIGNED_BYTE, this);

    //             ctx3d.enableVertexAttribArray(vloc);
    //             ctx3d.bindBuffer(ctx3d.ARRAY_BUFFER, vertexBuff);
    //             ctx3d.vertexAttribPointer(vloc, 2, ctx3d.FLOAT, false, 0, 0);

    //             ctx3d.enableVertexAttribArray(tloc);
    //             ctx3d.bindBuffer(ctx3d.ARRAY_BUFFER, texBuff);
    //             ctx3d.bindTexture(ctx3d.TEXTURE_2D, tex);
    //             ctx3d.vertexAttribPointer(tloc, 2, ctx3d.FLOAT, false, 0, 0);

    //             ctx3d.drawArrays(ctx3d.TRIANGLE_FAN, 0, 4);
    //         };
    //     }
    // };

})(jQuery);