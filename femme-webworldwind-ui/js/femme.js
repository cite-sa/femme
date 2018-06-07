var serverUrl = "http://localhost";
// var serverUrl = "http://earthserver-devel.vhosts.cite.gr/"

// var femmePort = "";
var femmePort = ":8080/";
// var femmeGeoPort = "";
var femmeGeoPort = ":8083/";

var femmeUrl = serverUrl + femmePort + "femme/";
var femmeGeoUrl = serverUrl + femmeGeoPort + "femme-geo/";

var numberOfWCSServerEndpoints = undefined;
// var coverage = undefined;
// var serverCoverages = [];

var Femme = (function () {

    var initialize = function() {
        GeoEndpointSuggest.initialize();
        createMetadataModal();
        bindEvents();
    };

    var bindEvents = function() {
        bindGetCoveragesEvent();
        bindClearCoveragesEvent();
        bindShowMetadataModalEvent();
        bindCloseMetadataModalEvent();
    };

    var bindGetCoveragesEvent = function() {
        $("#get-coverages-btn").on('click', function (e) {
            var xPath = $("#xpath-input").val();
            var selectedEndpoints = $('.asgEndpoint').CiteAutoSuggest('getSelectedValues');
            var queryCoverageIds = $.map($("#coverage-tags-input").tagsinput('items'), value => value != undefined ? value.id : value);

            getCoverages(selectedEndpoints, queryCoverageIds, xPath, false);
        });
    };

    var bindClearCoveragesEvent = function() {
        $("#clear-coverages-btn").on('click', function (e) {
            clearCoverages();
        });
    };

    var bindShowMetadataModalEvent = function() {
        $("#metadata-modal").on('show.bs.modal', function (event) {
            if (event.hasOwnProperty("relatedTarget")) {
                // var coverageId = $(event.relatedTarget).attr("id");
                var dataElementId = $(event.relatedTarget).data("data-element-id");
                fillMetadataModal(dataElementId);
            }
        });
    };

    var bindCloseMetadataModalEvent = function() {
        $("#close-metadata-display").on("click", function () {
            $("#metadata-container").toggleClass("closed");
        });
    };

    var getCoverages = function(serverIds, coverageIds, xPath, includeMetadata) {
        Loader.attach();
        clearCoverages();

        if (coverageIds == undefined || coverageIds.length == 0) {
            if (serverIds == undefined || serverIds.length == 0) {
                getAndCreateCoverages(undefined, undefined, xPath, includeMetadata);
            } else {
                serverIds.forEach(serverId => {
                    getAndCreateCoverages(serverId, undefined, xPath, includeMetadata);
                });            
            }
        } else {
            getAndCreateCoverages(undefined, coverageIds, xPath, includeMetadata);
        }
    };

    var getAndCreateCoverages = function(serverId, coverageIds) {
        var successCallback = (coverages) => {
            coverages.forEach(coverage => {
                createCoverageAccordion(coverage);
                $("#show").prop("disabled", false);
            });
            Loader.detach();
        };

        var errorCallback = () => {
            Loader.detach();
            console.log("Error retrieving coverages");
        };

        if (coverageIds != undefined && coverageIds.length > 0) {
            FemmeClient.getCoveragesByIds(coverageIds, successCallback, errorCallback);
        } else {
            if (serverId != undefined && serverId != "") {
                FemmeClient.getCoveragesByServer(serverId, successCallback, errorCallback);
            } else {
                FemmeClient.getAllCoverages(successCallback, errorCallback);
            }
        }
    };

    var buildRequestUrl = function(endpointId, coverageIds, xPath, includeMetadata) {
        var requestUrl = femmeUrl;

        requestUrl += endpointId != undefined && endpointId != "" ? "collections/" + endpointId + "/" : "";
        requestUrl += coverageIds != undefined && coverageIds.length > 0 ? "dataElements/list?": "dataElements?";

        var optionsUrl = '';
        if (coverageIds != undefined && coverageIds.length > 0) {
            coverageIds.forEach((coverageId, index) => {
                if (index != 0) {
                    if (optionsUrl != '') optionsUrl += "&";
                }
                optionsUrl += "id=" + coverageId;
            });
        }
        if (xPath && xPath != '') {
            if (optionsUrl != '') optionsUrl += "&";
            optionsUrl += "xpath=" + xPath;
        }
        if (!includeMetadata) {
            if (optionsUrl != '') optionsUrl += "&";
            optionsUrl += 'options={"exclude":["metadata"]}';
        }

        requestUrl += optionsUrl;

        return requestUrl;
    };

    var createCoverageAccordion = function(coverage) {
        var processCoverage = coverage && coverage.hasOwnProperty("geometry") && coverage.geometry != null && canBeDrawed(coverage.geometry);
        var coverageAccordion =
                '<div id="' + coverage.name + '-panel" class="panel panel-default">' +
                    '<div id="' + coverage.name + '-heading" class="panel-heading" role="tab">' +
                        '<div class="panel-title">' +
                            '<a data-toggle="collapse" data-target="#' + coverage.name + '-panel-body" role="button" aria-expanded="true" aria-controls="' + coverage.name + '">' +
                                '<div>' + coverage.name + '</div>' +
                            '</a>';

        if (processCoverage) {
            coverageAccordion +=
                '<span type="button" class="btn btn-default show-on-www" id=' + coverage.dataElementId + '-show-on-www data-id=' + coverage.dataElementId + '>' +
                    '<span class="glyphicon glyphicon-globe"></span>' +
                '</span>';
        }

        coverageAccordion +=
                '</div>' +
            '</div>' +
            '<div id=' + coverage.name + '-panel-body class="panel-collapse collapse" role="tabpanel" aria-labelledby="' + coverage.name + '-panel-body">' +
                '<div class="panel-body">' +
                    '<p><span class="panel-data-label">Coverage Id: </span>' + coverage.name + '</p>' +
                    // '<p><span class="panel-data-label">Endpoint: </span><a href=' + dataElement.endpoint + ' target="_blank">' + dataElement.endpoint + '</a></p>' +
                    '<button id="' + coverage.name + '" data-data-element-id= "' + coverage.dataElementId + '" type="button" class="btn btn-info metadataModalBtn" data-toggle="modal" data-target="#metadata-modal">Show Metadata</button>' +
                '</div>' +
            '</div>' +
        '</div>'

        $("#metadata-panel-group").append(coverageAccordion);

        if (processCoverage) {
            $("#" + coverage.dataElementId + "-show-on-www").click(function(event) {
                var button = $(this);
                button.toggleClass("active");

                drawOrDelete(button, coverage);
            });
        }
    };

    var canBeDrawed = function(geo) {
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
    };

    var drawOrDelete = function(button, geo) {
        var dataElementId = button.data("id");

        if (button.hasClass("active")) {
            Earthserver.WebWorldWind.deleteSchema(dataElementId);
            Earthserver.WebWorldWind.drawSchema(geo);
        } else {
            Earthserver.WebWorldWind.deleteSchema(dataElementId);
        }
    };

    var createMetadataModal = function() {
        $('body').append(
            '<div id="metadata-modal" class="modal fade" role="dialog">' +
                '<div class="modal-dialog modal-lg">' +
                    '<div class="modal-content">' +
                        '<div class="modal-header">' +
                            '<button type="button" class="close" data-dismiss="modal">&times;</button>' +
                            '<h4 class="modal-title"></h4>' +
                        '</div>' +
                        '<div class="modal-body">' +
                            '<pre><code></code></pre>' +
                        '</div>' +
                        '<div class="modal-footer"></div></div>' +
                    '</div>' +
                '</div>');
    };

    var fillMetadataModal = function(dataElementId) {
        FemmeClient.getDataElementById(dataElementId,
            (dataElement) => {
                $("#metadata-modal .modal-title").text(dataElement.name);
                appendMetadataToModal(dataElement.metadata[0].value);
            }, () => {
                alert("No metadata available for coverage " + coverageId);
            }
        );
    };

    var appendMetadataToModal = function(metadata) {
        var beautifulMetadata = vkbeautify.xml(metadata);
        $("#metadata-modal .modal-body pre code").text(beautifulMetadata);
        $('pre code').each(function (i, block) {
            hljs.highlightBlock(block);
        });
    };

    var clearCoverages = function() {
        $("#metadata-panel-group").empty();
        Earthserver.WebWorldWind.clear();
        CoverageTagsInput.destroy();
    };

    return {
        initialize: initialize,
        clearCoverages: clearCoverages,
        createCoverageAccordion: createCoverageAccordion,
        fillMetadataModal: fillMetadataModal
    };
  
})();