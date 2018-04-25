var coordinates = [];
var coverages = {};
var polygon = undefined;
var coveragePickDelegate = {};
var annotation = undefined;
var polygonsDrawn = [];
var polygonsDeleted = [];

var serverUrl = "http://localhost";
// var serverUrl = "http://earthserver-devel.vhosts.cite.gr/"

// var femmePort = "";
var femmePort = ":8080/";
// var femmeGeoPort = "";
var femmeGeoPort = ":8083/";

var femmeUrl = serverUrl + femmePort + "femme/";
var femmeGeoUrl = serverUrl + femmeGeoPort + "femme-geo/";

var numberOfWCSServerEndpoints = undefined;
var coverage = undefined;
var serverCoverages = [];

$(document).ready(function () {
    var self = this;
    createEndpointSuggest();
    createMetadataModal();
    console.log(Earthserver.WebWorldWind);
    Earthserver.WebWorldWind.initialize();
    bindEvents();
    $("#coverage-tags-input").val("");
    $("#coverage-tags-input").attr("placeholder", "Type here to search for Coverages");
});

function bindEvents() {
    $("#clear-coverages-btn").on('click', function (e) {
        clearCoverages();
    });

    $("#get-coverages-btn").on('click', function (e) {
        serverCoverages = [];
        var collectionIds = [];

        if (!$('.asgEndpoint').CiteAutoSuggest('getSelectedValues').length == 0) {
            for (var i = 0; i < $('.asgEndpoint').CiteAutoSuggest('getSelectedValues').length; i++) {
                collectionIds.push($('.asgEndpoint').CiteAutoSuggest('getSelectedValues')[i]);
                var xPath = $("#xpath-input").val();
                var queryCoverageIds = $.map($("#coverage-tags-input").tagsinput('items'), function (value, index) {
                    if (value != undefined) {
                        return value.id;
                    }
                    return undefined;
                });
            }
            getCoverages(collectionIds, queryCoverageIds, xPath, false);
        }
    });

    $("#close-metadata-display").on("click", function () {
        $("#metadata-container").toggleClass("closed");
    });

    $("#metadata-modal").on('show.bs.modal', function (event) {
        if (event.hasOwnProperty("relatedTarget")) {
            var coverageId = $(event.relatedTarget).attr("id");
            fillMetadataModal(coverageId);
        }
    });
}

function getCoverages(endpointId, coverageIds, xPath, includeMetadata) {

    for (var i = 0; i < endpointId.length; i++) {
        var requestUrl = femmeUrl;

        if (coverageIds.length == 0) {
            requestUrl += "collections/" + endpointId[i] + "/dataElements?";
        }
        else {
            requestUrl += "dataElements/list?";
        }

        var optionsUrl = '';
        $.each(coverageIds, function (index, coverageId) {
            if (index != 0) {
                if (optionsUrl != '') optionsUrl += "&";
            }
            optionsUrl += "id=" + coverageId;
        });
        if (xPath && xPath != '') {
            if (optionsUrl != '') optionsUrl += "&";
            optionsUrl += "xpath=" + xPath;
        }
        if (!includeMetadata) {
            if (optionsUrl != '') optionsUrl += "&";
            optionsUrl += 'options={"exclude":["metadata"]}';
        }

        requestUrl += optionsUrl;

        Earthserver.Client.Utilities.callWS(requestUrl, 'GET', {
            dataType: "json",
            contentType: undefined,
            onSuccess: function (data) {
                $.each(data.entity.body.elements, function (index, coverage) {
                    
                    getCoverageGeoJson(coverage.id, function(coverageGeo) {
                        createCoverageAccordion(coverage, coverageGeo, index);
                        coverages[coverage.name] = {};
                        coverages[coverage.name].coverage = coverage;
                        $("#show").prop("disabled", false);
                    });
                });
            },
            onError: function () {
                alert("No coverages available");
            }
        });
        if (coverageIds.length > 1) {
            return
        }
    }
}

function getCoverageGeoJson(dataElementId, callback) {
    var requestUrl = femmeGeoUrl + "coverages?dataElementId=" + dataElementId;
    Earthserver.Client.Utilities.callWS(requestUrl, 'GET', {
        dataType: "json",
        onSuccess: function (coverageGeo) {
            callback(coverageGeo);
        },
        onError: function () {
            alert("No geo information for coverage");
        }
    });
}

function createEndpointSuggest() {
    var self = this;
    var getServerEndpointsUrl = femmeGeoUrl + "servers?crs=EPSG:4326";
    this.asgEndpoint = $('.asgEndpoint');

    this.asgEndpoint.CiteAutoSuggest({
        currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
        suggestionMode: $.ui.CiteAutoSuggest.SuggestionMode.Callback,
        uiMode: $.ui.CiteAutoSuggest.UIMode.TextBox,
        selectionNameProperty: 'serverName',
        selectionValueProperty: 'collectionId',
        allowOnlySuggestionSelections: false,
        allowMultiSelect: true,
        highlightMatches: true,
        watermarkText: 'Available WCS Servers',
        retrieveSuggestionsCallback: function (text, filters, limit, callback) {
            Earthserver.Client.Utilities.callWS(getServerEndpointsUrl, 'GET', {
                dataType: "json",
                contentType: undefined,
                onSuccess: function (data) {
                    callback(data);
                    numberOfWCSServerEndpoints = data.length;
                    return numberOfWCSServerEndpoints;
                },
                onError: function () {
                    numberOfWCSServerEndpoints = 0;
                    alert("No endpoints available");
                    return numberOfWCSServerEndpoints;
                }
            });
        },
        maximumSelections: function (numberOfWCSServerEndpoints) {
            return numberOfWCSServerEndpoints.value;
        },
        minimumSelections: 0,
        autoInitialize: true,
    });

    this.asgEndpoint.bind('selectionchanged', function (e) {
        clearCoverages();
        var endpointId = [];

        if (!self.asgEndpoint.CiteAutoSuggest('getSelectedValues').length == 0) {
            for (var i = 0; i < self.asgEndpoint.CiteAutoSuggest('getSelectedValues').length; i++) {
                endpointId.push(self.asgEndpoint.CiteAutoSuggest('getSelectedValues')[i]);
            }
        }
        if (endpointId == '') return;

        for (var i = 0; i < endpointId.length; i++) {
            var requestUrl = femmeUrl + 'collections/' + endpointId[i] + '/dataElements?options={"include": ["name"]}';
            Earthserver.Client.Utilities.callWS(requestUrl, 'GET', {
                dataType: "json",
                contentType: undefined,
                onSuccess: function (data) {
                    $.each(data.entity.body.elements, function (index, coverage) {
                        serverCoverages.push(coverage);
                    });
                    initCoverageTagsInput(serverCoverages);
                },
                onError: function () {
                    alert("No endpoints available");
                }
            });
        }
        serverCoverages = [];
    });
}

function createCoverageAccordion(coverage, geo) {
    $("#metadata-panel-group").append(
        '<div id="' + coverage.name + '-panel" class="panel panel-default">' +
        '<div id="' + coverage.name + '-heading" class="panel-heading" role="tab">' +
        '<div class="panel-title">' +
        '<a data-toggle="collapse" data-target="#' + coverage.name + '-panel-body" role="button" aria-expanded="true" aria-controls="' + coverage.name + '">' +
        '<div>' + coverage.name + '</div>' +
        '</a>' +
        '</div>' +
        '</div>' +
        '<div id=' + coverage.name + '-panel-body class="panel-collapse collapse" role="tabpanel" aria-labelledby="' + coverage.name + '-panel-body">' +
        '<div class="panel-body">' +
        '<p><span class="panel-data-label">Coverage Id: </span>' + coverage.name + '</p>' +
        '<p><span class="panel-data-label">Endpoint: </span><a href=' + coverage.endpoint + ' target="_blank">' + coverage.endpoint + '</a></p>' +
        '<button id="' + coverage.name + '" type="button" class="btn btn-info metadataModalBtn" data-toggle="modal" data-target="#metadata-modal">Show Metadata</button>' +
        '</div>' +
        '</div>' +
        '</div>');

    if (geo.hasOwnProperty("geometry") && geo.geometry != null && canBeDrawed(geo.geometry)) {

        $("#" + coverage.name + "-panel").find(".panel-title").append(
            '<span type="button" class="btn btn-default show-on-www" id=' + coverage.name + '-show-on-www data-id=' + coverage.id + '>' +
            '<span class="glyphicon glyphicon-globe"></span>' +
            '</span>');

        $("#" + coverage.name + "-show-on-www").click(function (event) {
            var button = $(this);
            button.toggleClass("active");

            drawOrDelete(button, geo);
            // var requestUrl = femmeUrl + 'dataElements/' + $(button).attr("data-id") + '?options={"exclude":["metadata"]}';
            // Earthserver.Client.Utilities.callWS(requestUrl, 'GET', {
            //     dataType: "json",
            //     contentType: undefined,
            //     onSuccess: function (data) {
            //         drawOrDelete(button, data);
            //     },
            //     onError: function () {
            //         alert("No endpoints available");
            //     }
            // });
        });
    }
}

function createMetadataModal() {
    $('body').append(
        '<div id="metadata-modal" class="modal fade" role="dialog">' +
        '<div class="modal-dialog modal-lg">' +
        '<div class="modal-content">' +
        '<div class="modal-header">' +
        '<button type="button" class="close" data-dismiss="modal">&times;</button>' +
        '<h4 class="modal-title"></h4>' +
        '</div>' +
        '<div class="modal-body">' +
        '<pre>' +
        '<code>' +
        '</code>' +
        '</pre>' +
        '</div>' +
        '<div class="modal-footer"></div>' +
        '</div>' +
        '</div>' +
        '</div>');
}

function fillMetadataModal(coverageId) {

    $("#metadata-modal .modal-title").text(coverageId);

    var requestUrl = femmeUrl + 'dataElements/list?id=' + coverages[coverageId].coverage.id;

    Earthserver.Client.Utilities.callWS(requestUrl, 'GET', {
        dataType: "json",
        contentType: undefined,
        onSuccess: function (data) {
            if (data.entity.body.elements.length != 1) return;
            var coverage = data.entity.body.elements[0];
            coverages[coverageId].coverage.metadata = coverage.metadata;
            appendMetadataToModal(coverages[coverageId].coverage.metadata[0].value);
        },
        onError: function () {
            alert("No metadata available for selected coverage");
        }
    });
}

function fillMetadataModalonClick(coverageId) {

    var requestUrl = femmeUrl + 'dataElements/list?id=' + coverageId;

    Earthserver.Client.Utilities.callWS(requestUrl, 'GET', {
        contentType: undefined,
        dataType: "json",
        onSuccess: function (data) {
            if (data.entity.body.dataElements.length != 1) return;
            var coverage = data.entity.body.dataElements[0];
            $("#metadata-modal .modal-title").text(coverage.name);
            appendMetadataToModal(coverage.metadata[0].value);
        },
        onError: function () {
            alert("No metadata available for selected coverage");
        }
    });
}

function appendMetadataToModal(metadata) {
    var beautifulMetadata = vkbeautify.xml(metadata);
    $("#metadata-modal .modal-body pre code").text(beautifulMetadata);
    $('pre code').each(function (i, block) {
        hljs.highlightBlock(block);
    });
}

function drawOrDelete(button, geo) {

    var coverageName = button.attr("id").split("-show-on-www")[0];
    // var coverage = data.entity.body;

    if (button.hasClass("active")) {

        polygon = Earthserver.WebWorldWind.drawSchema(geo);
        polygonsDrawn.push(polygon);

        polygonsDeleted.find(p => p.name = coverageName);
        var index = polygonsDeleted.indexOf(polygon);

        if (index > -1) {
            polygonsDeleted.splice(index, 1);
        }

        // if (polygon != undefined) {
        //     drawSchema(polygon);
        // }

        console.log(polygonsDrawn);
        $.each(polygonsDrawn, function (key, value) {
            if (value[0] !== undefined) {
                console.log(key + " is index of " + value[0].name);
            }
            else {
                console.log(key + " is index of " + value.name);
            }
        });
        // console.log(polygonsDrawn.length);
        // console.log(polygonsDeleted);
        // console.log(polygonsDeleted.name);
        // console.log(polygonsDeleted.length);
    }
    else {

        polygon = polygonsDrawn.find(p => p.name = coverageName);
        var index = polygonsDrawn.indexOf(polygon);

        polygonsDeleted.push(polygon);

        if (index > -1) {
            polygonsDrawn.splice(index, 1);
        }
        if (polygon != undefined) {
            Earthserver.WebWorldWind.deleteSchema(polygon);
        }

        // console.log(polygonsDrawn);
        // $.each( polygonsDrawn, function( key, value ) {
        //     if(value[0] !== undefined){
        //         console.log(key + " is index of " + value[0].name);
        //     }
        //     else {
        //         console.log(key + " is index of " + value.name);
        //     }
        // });
        // console.log(polygonsDeleted.length);
        // console.log(polygonsDeleted);
        // console.log(polygonsDeleted.name);
    }
}

function initCoverageTagsInput(coverages) {
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
}

function destroyCoverageTagsInput() {
    $("#coverage-tags-input").tagsinput('removeAll');
    var tagsInput = $("#coverage-tags-input");
    tagsInput.typeahead("destroy");
    tagsInput.tagsinput("destroy");
    tagsInput.off('itemAdded');
    coverageTags = [];
}

function clearCoverages() {
    $("#metadata-panel-group").empty();
    // polygonsLayer.removeAllRenderables();
    // annotationsLayer.removeAllRenderables();
    Earthserver.WebWorldWind.clear();
    destroyCoverageTagsInput();
    coverageTags = [];
}

function canBeDrawed(geo) {
    var boundingBox = geo.coordinates[0];
    var boundingBoxLat = [];
    var boundingBoxLon = [];

    for (var i = 0; i < boundingBox.length; i++) {
        boundingBoxLat.push(boundingBox[i][1])
        boundingBoxLon.push(boundingBox[i][0]);
    }

    var maxLat = Math.max.apply(Math, boundingBoxLat);
    var maxLon = Math.max.apply(Math, boundingBoxLon);
    var minLat = Math.min.apply(Math, boundingBoxLat);
    var minLon = Math.min.apply(Math, boundingBoxLon);

    var distanceY = Math.abs(maxLat - minLat);
    var distanceX = Math.abs(maxLon - minLon);

    return (distanceY < 180) && (distanceX < 360);
}

var canvasManipulation = {

    drawStuff: function () {
        var canvas = document.getElementById("coverageCanvas");
        var img = new Image();
        img.src = "http://www.cite.gr/sites/default/files/logo_%28480%29.png";
        var context = canvas.getContext('2d');
        // context = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
        context.drawImage(img, 110, 60, 75, 30, 30, 30, 70, 80);
        return context;
    },

    createShaders: function () {
        var img, tex, vloc, tloc, vertexBuff, texBuff;

        var cvs3d = document.getElementById('coverageCanvas');
        var ctx3d = cvs3d.getContext('experimental-webgl');
        var uLoc;

        var vertexShaderSrc =
            "attribute vec2 aVertex;" +
            "attribute vec2 aUV;" +
            "varying vec2 vTex;" +
            "uniform vec2 pos;" +
            "void main(void) {" +
            "  gl_Position = vec4(aVertex + pos, 0.0, 1.0);" +
            "  vTex = aUV;" +
            "}";

        var fragmentShaderSrc =
            "precision highp float;" +
            "varying vec2 vTex;" +
            "uniform sampler2D sampler0;" +
            "void main(void){" +
            "  gl_FragColor = texture2D(sampler0, vTex);" +
            "}";

        var vertShaderObj = ctx3d.createShader(ctx3d.VERTEX_SHADER);
        var fragShaderObj = ctx3d.createShader(ctx3d.FRAGMENT_SHADER);
        ctx3d.shaderSource(vertShaderObj, vertexShaderSrc);
        ctx3d.shaderSource(fragShaderObj, fragmentShaderSrc);
        ctx3d.compileShader(vertShaderObj);
        ctx3d.compileShader(fragShaderObj);

        var progObj = ctx3d.createProgram();
        ctx3d.attachShader(progObj, vertShaderObj);
        ctx3d.attachShader(progObj, fragShaderObj);

        ctx3d.linkProgram(progObj);
        ctx3d.useProgram(progObj);

        ctx3d.viewport(0, 0, 1024, 768);

        vertexBuff = ctx3d.createBuffer();
        ctx3d.bindBuffer(ctx3d.ARRAY_BUFFER, vertexBuff);
        ctx3d.bufferData(ctx3d.ARRAY_BUFFER, new Float32Array([-1, 1, -1, -1, 1, -1, 1, 1]), ctx3d.STATIC_DRAW);

        texBuff = ctx3d.createBuffer();
        ctx3d.bindBuffer(ctx3d.ARRAY_BUFFER, texBuff);
        ctx3d.bufferData(ctx3d.ARRAY_BUFFER, new Float32Array([0, 1, 0, 0, 1, 0, 1, 1]), ctx3d.STATIC_DRAW);

        vloc = ctx3d.getAttribLocation(progObj, "aVertex");
        tloc = ctx3d.getAttribLocation(progObj, "aUV");
        uLoc = ctx3d.getUniformLocation(progObj, "pos");

        img = new Image();
        img.src = "http://www.cite.gr/sites/default/files/logo_%28480%29.png";

        img.onload = function () {
            tex = ctx3d.createTexture();
            ctx3d.bindTexture(ctx3d.TEXTURE_2D, tex);
            ctx3d.texParameteri(ctx3d.TEXTURE_2D, ctx3d.TEXTURE_MIN_FILTER, ctx3d.NEAREST);
            ctx3d.texParameteri(ctx3d.TEXTURE_2D, ctx3d.TEXTURE_MAG_FILTER, ctx3d.NEAREST);
            ctx3d.texImage2D(ctx3d.TEXTURE_2D, 0, ctx3d.RGBA, ctx3d.RGBA, ctx3d.UNSIGNED_BYTE, this);

            ctx3d.enableVertexAttribArray(vloc);
            ctx3d.bindBuffer(ctx3d.ARRAY_BUFFER, vertexBuff);
            ctx3d.vertexAttribPointer(vloc, 2, ctx3d.FLOAT, false, 0, 0);

            ctx3d.enableVertexAttribArray(tloc);
            ctx3d.bindBuffer(ctx3d.ARRAY_BUFFER, texBuff);
            ctx3d.bindTexture(ctx3d.TEXTURE_2D, tex);
            ctx3d.vertexAttribPointer(tloc, 2, ctx3d.FLOAT, false, 0, 0);

            ctx3d.drawArrays(ctx3d.TRIANGLE_FAN, 0, 4);
        };
    }
};

