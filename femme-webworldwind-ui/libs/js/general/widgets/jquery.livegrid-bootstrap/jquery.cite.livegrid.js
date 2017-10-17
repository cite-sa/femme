/*
 * jQuery CITE LiveGrid
 *
 * Depends on:
 *	jquery.ui.core.js 1.9
 *	jquery.ui.widget.js 1.9
 *  jquery.cite.basecontrol.js 1.0
 */

(function($, undefined) {

	var globalInitialized = false;
	var inlineFormCellEditorChildClassNames = [];
	var inlineFormCellEditorChildClassSelector = null;
	var originalSetRowDataFunc = null;
	var isCtrlPressed = false;

	var constants = {
		INLINE_FORM_CELL_EDIT_LAYOUT_EVENT: 'layoutChanged',
		OBJECT_RIGHT_EDIT: 'e',
		OBJECT_RIGHT_DELETE: 'd'
	};

	var actionIconFontSize = 'inherit';


	////////////////////////////////////////////////////////////////
	//  Inline Editor
	////////////////////////////////////////////////////////////////

	function findDescendantsOrSelf (controls, selector) {
		/// <summary>Expands the specified elements collection to contain all its current items in addition to all their descendants that
		/// match the specified selector.</summary>
		if (!selector) {
			selector = '*';
		}
		var result = $();
		var rootControls = controls.filter(selector);
		var childControls = controls.find(selector);
		result = result.add(rootControls);
		result = result.add(childControls);
		return result;
	}

	function getActionType(control) {
		/// <summary>Use the data-action-type attribute to decide on which event to attach.</summary>
		return control.attr("data-action-type");
	}

	function getActionControls(host) {
		/// <summary>Get all controls that have the data-action-type attirbute set.</summary>
		return $("[data-action-type]", host);
	}

	function getActionEvent(control) {
		/// <summary>Use the data-action-event attribute to decide on which event to attach.</summary>
		return control.attr("data-action-event");
	}

	function getControlEventTargets(control) {
		/// <summary>Get all controls that have the data-action-event attirbute set.</summary>
		return findDescendantsOrSelf(control, "[data-action-event]");
	}

	function attachCallbackToControls(controls, eventNameResolver, callback) {
		/// <summary>Binds the specifed callback to appropriate events for each of the specified controls.</summary>
		var eventHandler = callback ?
			function (event) { callback(event); return false; } :
			function () { return false; }; //If no callback is specified, attach an empty (i.e. false) event handler.
		controls.each(function () {
			var control = $(this);
			var eventName = eventNameResolver(control);
			if (!!eventName) {
				control.off(eventName).on(eventName, eventHandler);
			}
		});
	}

	function attachActionHandler(actionControl, callback) {
		/// <summary>Binds the specifed callback to appropriate events for each of the applicable targets on the specified control.</summary>
		var targetControls = getControlEventTargets(actionControl);
		attachCallbackToControls(targetControls, getActionEvent, callback);
	}

	$.widget("ui.CiteInlineEditor", $.ui.CiteBaseControl, {
		options: {
			item: null,
			saveCallback: null,
			cancelCallback: null,
		},

		doInitialize: function () {
			if (this._super("doInitialize") === false) return false;

			this.actionCallbacks = {};
			return true;
		},

		_setOption: function (key, value) {
			this._super(key, value);
			if (key === "item") {
				this.refresh();
			} else if (key === "saveCallback") {
				this.actionCallbacks.save = value;
				this._refreshHandlers();
			} else if (key === "cancelCallback") {
				this.actionCallbacks.cancel = value;
				this._refreshHandlers();
			}
		},

		_refreshView: function () {

		},

		_refreshHandlers: function () {
			var actionCallbacks = this.actionCallbacks;
			if (actionCallbacks && !$.isEmptyObject(actionCallbacks)) {
				var actionControls = getActionControls(this.element);
				actionControls.each(function () {
					var actionControl = $(this);
					var actionType = getActionType(actionControl);
					attachActionHandler(actionControl, actionCallbacks[actionType]);
				});
			}
		},

		refresh: function () {
			this._refreshView();
			this._refreshHandlers();
		},

		getData: function () {
			return {};
		},

		setData: function (data) {
			this.option('item', data);
		},

		validate: function (callback) {
			callback(true);
		},

		validateWithServerResponse: function (serverResponse, callback) {
			callback(true);
		},

		setCurrentDisplayModeAndApply: function (displayMode) {
			this.setCurrentDisplayMode(displayMode);
			if (this.hasInitialized())
				this.applyViewMode();
		},

		applyViewMode: function () {
			var actionCallbacks = this.actionCallbacks;
			if (actionCallbacks && !$.isEmptyObject(actionCallbacks)) {
				var actionControls = getActionControls(this.element);
				var visible = this.getCurrentDisplayMode() != jQuery.ui.CiteBaseControl.DisplayMode.View;
				actionControls.each(function () {
					var actionControl = $(this).toggle(visible);
				});
			}
		}
	});

	////////////////////////////////////////////////////////////////
	//  Live Grid
	////////////////////////////////////////////////////////////////

	$.widget("ui.CiteLiveGrid", $.ui.CiteBaseControl, {
		version: "1.0.0",
		defaultElement: "<div>",
		options: {
			quickSearchLabel: '',
			quickSearchButtonLabel: 'Search',
			complexFiltersLabel: 'Filters',
			complexFiltersApplyButtonLabel: 'Apply Filters',
			complexFiltersClearButtonLabel: 'Reset All',
			complexFiltersRemoveAllButtonLabel: 'Remove All',
			complexFiltersClearFilterButtonLabel: 'Reset Filter',
			complexFiltersRemoveFilterButtonLabel: 'Remove Filter',
			refreshButtonLabel: 'Refresh',
			newButtonLabel: 'New',
			editButtonLabel: 'Edit',
			viewButtonLabel: 'View',
			deleteButtonLabel: 'Delete',
			reportButtonLabel: 'Generate Report',
			recordCountLabel: 'Showing {0} - {1} of {2}',
			pageCountLabel: 'Page {0} of {1}',
			noRecordsLabel: 'No records to view',
			advancedFiltersLabel: 'Advanced',
			inlineFormEditSaveButtonLabel: '',
			inlineFormEditCancelButtonLabel: '',
			pageSizeAllLabel: 'All',

			gridName: '',
			gridClasses: '',
			width: null,
			height: null,
			maxHeight: 0,
			columnDefinitions: [],
			//updateColumnModelCallback: '',
			retrieveDataCallback: '',
			retrieveDataCompletedCallback: null,
			retrieveDataCallbackFlags: null,
			generateReportCallback: '',
			//rowAdditionCallback: '',
			//beginRowChangeCallback: '',
			//rowChangeCallback: '',
			//rowChangeCancelledCallback: '',
			//beginCellChangeCallback: '',
			//cellChangeCallback: '',
			//showContextMenuCallback: '',
			//contextMenuActionCallback: '',
			enabled: true,
			blockInteractionsInViewMode: false,
			autoSizeToParent: true,
			autoFitColumnsToWidth: false,
			minWidth: 100,
			caption: '',
			visibleRows: 10,

			pagingType: 'numbers',		// Paging Type
			pagingPrevLabel: 'Previous',
			pagingNextLabel: 'Next',
			pagingFirstLabel: 'First',
			pagingLastLabel: 'Last',

			processingLabel: 'Processing...',

			autoLoadInitialDataset: true,
			allowRowSelection: false,
			allowCellSelection: true,
			multipleSelectionMode: 0, 	// None
			multipleSelectionKey: 2, 	// Control
			//prefetchWholeDataset: false,
			availablePageSizes: '10,20,50,100,-1',
			//showContextMenu: false,
			noCommandsMessage: '',
			showCommandsColumn: false,
			showHeaders: true,
			//showRowNumbers: false,
			editIcon: 0, 				// None
			customEditIconCallback: '',
			editIconClass: 'fa-edit',
			viewIconClass: 'fa-eye',
			showEditIconOnToolbar: false,
			showEditIconOnEachRow: true,
			customButtons: [],
			customRowButtons: [],
			//customFiltersButtons: [],
			rowClickAction: 0, 			// Select
			customRowClickCallback: '',
			//customRowCheckCallback: '',
			rowDoubleClickAction: 0, 	// Select
			customRowDoubleClickCallback: '',
			showGenerateReportIconOnToolbar: false,
			showDeleteIconOnToolbar: false,
			showDeleteIconOnEachRow: false,
			deleteIconClass: 'fa-trash',
			customDeleteRowCallback: '',
			addIcon: 0, 					// None
			customAddRowCallback: null,
			showAddIconOnEachGroupHeaderRow: false,
			addIconClass: 'fa-file',
			//showRowCommandsMenuCallback: '',
			//rowCommandCallback: '',
			allowMultiColumnSort: false,
			defaultSortColumns: [],
			//allowDrag: false,
			//dragStartCallback: '',
			//dragHandleWidth: 20,
			//allowDrop: false,
			//dropCallback: '',
			//allowRowReordering: false,
			//rowsReorderedCallback: '',
			refreshIcon: 0, 				// None
			customRefreshIconCallback: '',
			selectedRows: [],
			checkedRows: [],
			filterDisplayMode: 0,
			filteringSections: [],
			quickFilteringControlClientID: '',
			additionalFiltersCallback: '',
			fixedFilters: [],
			showControlColumnsFirst: true,
			//enableRowGrouping: false,
			//rowGroupingColumnName: '',
			//rowCreationCallback: '',
			//columnCheckCallback: '',
			//postRenderCallback: '',
			postInitializeCallback: '',
			//inlineEditFormContainerClientID: '',
			//inlineFormEditCustomButtons: [],
			//disableHorizontalScroll: false,
			//allowDropToOtherGrids: false,
			//dropToOtherGridCallback: '',
			//dropAutoId: false,
			//dropCopy: true,
			//dropByName: true,
			//dropTargetGridClientIDs: '',
			//enableSubGrid: false,
			//subGridRowExpandCollapseCallback: '',
			//sortChangeCallback: '',
			//subGridClientID: '',
			//allowOnlyOneExpandedSubGrid: true,
			//expandAllSubGridsOnLoad: false,
			//columnResizedCallback: ''
			inlineEditor: null,
			inlineEditorGetItemCallback: '',
			inlineEditorSaveItemCallback: ''
		},

		doInitialize: function() {
			if (this._super("doInitialize") === false) return false;

			var self = this;

			// Init UI
			if (!this.element[0].id) this.element[0].id = $.ui.CiteBaseControl.generateControlId();
			this.element[0].style.display = 'none';
			this.element.addClass('LiveGrid');
			this.element.addClass('CGrid');
			if (this.options.disableHorizontalScroll)
				this.element.addClass("nohscroll");

			// Initialize Parameters
			this.gridContainer = $('<div id="' + this.element[0].id + '_container" class="table-responsive">')
			this.element.append(this.gridContainer);
			this.gridTable = $('<table id="' + this.element[0].id + '_table" class="liveGridTable table table-striped ' + this.options.gridClasses + '"></table>');
			this.gridContainer.append(this.gridTable);

			this.loadDataFunction = null;
			this.columnIndices = [];
			this.columnModel = [];
			this.currentEditRow = -1;
			this.isCurrentEditRowNew = false;
			this.currentEditColumn = null;
			this.rowContexts = {};
			this.rowRights = {};
			this.keyColumnNames = [];
			this.selectedCells = [];
			this.isEditable = false;
			this.currentFilters = [];
			this.commandsColumnIndex = -1;
			this.sortInfo = [];
			this.checkedRowInfo = {};
			this.currentExpandedRowId = null;

			var sortingInfo = this.getDefaultSortColumns();
			if (sortingInfo.length > 0) {
				for (var i = 0; i < sortingInfo.length; i++) {
					this.sortInfo.push({ 'colName': sortingInfo[i].ColumnName, 'order': sortingInfo[i].SortOrder });
				}
				shouldUpdateSortIcons = true;
			}

			this.initGrid(undefined, true);

			this.element[0].style.display = 'block';

			return true;
		},

		initGrid: function(dataFunction, preserveSorting) {
			var self = this;

			// Construct the column definitions array
			var colDefs = this.getColumnDefinitions();
			var colNames = [];
			var colTooltips = [];
			var colModel = [];
			this.keyColumnNames = [];
			this.columnIndices = [];
			this.isEditable = false;
			this.commandsColumnIndex = -1;
			this.rowContexts = {};
			this.rowRights = {};
			this.checkedRowInfo = {};
			this.setCheckedRowsKeys([]);
			this.currentEditRow = -1;
			this.isCurrentAddingRow = false;
			this.isCurrentEditRowNew = false;

			// Edit Icon on Each Row
			if (this.getShowEditIconOnEachRow()) {
				var caption = '';
				colNames.push(caption);
				colTooltips.push(caption);
				var col = {
					name: 'row_edit',
					title: caption,
					render: function (data, type, full, meta) {
						var canEdit = true;//(options.rowId in self.rowRights) ? ((constants.OBJECT_RIGHT_EDIT in self.rowRights[options.rowId]) ? self.rowRights[options.rowId][constants.OBJECT_RIGHT_EDIT] : true) : true;
						var editMode = self.getCurrentDisplayMode() == "1" && canEdit;
						var td = $(self.gridTable.DataTable().cell({ row: meta.row, column: meta.col }).node());
						td.css('text-align', 'center');
						return '<span style="font-size: ' + actionIconFontSize + ';" class="liveGridCommandEditIcon" title="' + (editMode ? self.getEditButtonLabel() : self.getViewButtonLabel()) + '"><i class="fa '
							+ (editMode ? self.getEditIconClass() : self.getViewIconClass()) + '"></i></span>';
					},
					width: '10px',
					orderable: false
				};
				colModel.push(col);

				this.columnIndices[col.name] = colModel.length - 1;
			}

			// Delete Icon on Each Row
			if (this.getShowDeleteIconOnEachRow()) {
				var caption = '';
				colNames.push(caption);
				colTooltips.push(caption);
				var col = {
					name: 'row_delete',
					title: caption,
					render: function (data, type, full, meta) {
						var td = $(self.gridTable.DataTable().cell({ row: meta.row, column: meta.col }).node());
						td.css('text-align', 'center');
						return '<span style="font-size: ' + actionIconFontSize + ';" class="liveGridCommandDeleteIcon" title="' + self.getDeleteButtonLabel() + '"><i class="fa ' + self.getDeleteIconClass() + '"></i></span>';
					},
					width: '10px',
					orderable: false
				};
				colModel.push(col);

				this.columnIndices[col.name] = colModel.length - 1;
			}

			// Custom Buttons on Each Row
			var customRowButtons = this.getCustomRowButtons();
			for (var crb = 0; crb < customRowButtons.length; crb++) {
				var col = {
					name: 'row_custom_' + crb,
					title: caption,
					// TODO You have to give the appropriate index
					render: (function(cIndex) {
						if (customRowButtons[cIndex].Type == $.ui.CiteLiveGrid.ButtonType.Icon) {
							return function (data, type, full, meta) {
								var cb = self.getCustomRowButtons()[cIndex];
								var td = $(self.gridTable.DataTable().cell({ row: meta.row, column: meta.col }).node());
								td.css('text-align', 'center');
								return '<span style="font-size: ' + actionIconFontSize + ';" class="' + cb.CssClass + ' liveGridCommandCustom liveGridCommandCustom_' + cIndex + '" title="' + self.getCustomRowButtons()[cIndex].Label + '"><i class="fa ' + cb.IconClass + '"></i></span>';
							};
						}
						else if (customRowButtons[cIndex].Type == $.ui.CiteLiveGrid.ButtonType.Button) {
							return function (data, type, full, meta) {
								var td = $(self.gridTable.DataTable().cell({ row: meta.row, column: meta.col }).node());
								td.css('text-align', 'center');
								return '<button class="liveGridCommandCustom liveGridCommandCustom_' + cIndex + ' ' + self.getCustomRowButtons()[cIndex].CssClass + '">' + self.getCustomRowButtons()[cIndex].Label + '</button>';
							};
						}
					})(crb),
					orderable: false
				};
				colModel.push(col);

				this.columnIndices[col.name] = colModel.length - 1;
			}


			// Init Options and Create Data Table
			for (var i in colDefs) {
				var caption = colDefs[i].ShortCaption;
				if (colDefs[i].IsHeaderCheckable) {
					var checkId = this.element[0].id + '_hdrChk' + colDefs[i].Name;
					caption = '<input type="checkbox" class="liveGridColCheck hdrCheck_' + colDefs[i].Name + '" id="' + checkId + '"/><label for="' + checkId + '" class="liveGridHeaderCheckLabel">' + caption + '</label>';
				}
				colNames.push(caption);
				colTooltips.push(colDefs[i].Caption);
				var col = {
					name: colDefs[i].Name,
					title: caption,
					render: (function(i) {
						return function (data, type, full, meta) {
							if (data === undefined) return '';
							if (colDefs[i].Formatter) return Cite.CloudRoll.BLL.Formatting.FormattingManager.getFormatter(colDefs[i].Formatter).format(data);
							else return data;
						}
					})(i),
					//align
					className: colDefs[i].ColumnCSSClass,
					visible: !colDefs[i].IsHidden,
					orderable: colDefs[i].IsSortable
				};
				if (colDefs[i].ShowTooltips == false)
					col.title = false;
				if (colDefs[i].Width != 0)
					col.width = colDefs[i].Width;
				colModel.push(col);

				this.columnIndices[colDefs[i].Name] = colModel.length - 1;
				if (colDefs[i].IsKey)
					this.keyColumnNames.push(colDefs[i].Name);
			}

			this.loadDataFunction = function (postData, callback) {
				if (self.getRetrieveDataCallback()) {

					$.extend(postData, { 'filters': self.currentFilters, 'PresentationPointName': self.getGridName() });

					//self.invalidateCache = false;
					//If row grouping is enabled, explicitly set the sorting parameters because jqGrid overwrites them using its own format
					if (self.getEnableRowGrouping()) {
						var sortData = self.getSortNameAndOrderStrings();
						$.extend(postData, { 'sidx': sortData.sortname, 'sord': sortData.sortorder });
					}

					var retrieveFlags = self.getRetrieveDataCallbackFlags();
					if (retrieveFlags && !$.isEmptyObject(retrieveFlags)) {
						postData['flags'] = retrieveFlags;
					} else {
						delete postData.flags;
					}

					// overide order with column names
					for (var i = 0; i < postData.order.length; i++) {
						var item = postData.order[i];
						item.column = this._getColumnNameByIndex(item.column);
					}

					eval(self.getRetrieveDataCallback())(postData, function (data) {
						if (data == null) {
							//self.gridTable.jqGrid("clearGridData", true);
							return;
						}
						var json = data;
						var rows = [];
						self.rowContexts = {};
						self.rowRights = {};
						for (var i in json.rows) {
							var celldata = [];
							for (var j in json.rows[i].Data) {
								if (self.columnIndices[j] !== undefined) {
									celldata[self.columnIndices[j]] = json.rows[i].Data[j];
								}
							}

							var rowid = self.generateRowId(json.rows[i].Data);
							rows.push($.extend({ DT_RowId: rowid }, celldata));

							if (json.rows[i].Context)
								self.rowContexts[rowid] = json.rows[i].Context;
							if (json.rows[i].R)
								self.rowRights[rowid] = json.rows[i].R;
						}

						// Here we give the data to DataTables to render the results
						// We give all the parameters as is and we extend them with data of the form: {DT_RowId: xx, 0: xx, 1: xx, 2: xx etc..}
						callback(
							$.extend(json, { data: rows })
						);

						self.bindRowEventHandlers();

						if (self.getRetrieveDataCompletedCallback()) {
							eval(self.getRetrieveDataCompletedCallback())();
						}
					});
				}
			};

			var sortingInfo = this.getDefaultSortColumns();
			var sortColumns = [];
			for (var i = 0; i < sortingInfo.length; i++) {
				sortColumns.push([this.columnIndices[sortingInfo[i].ColumnName], sortingInfo[i].SortOrder]);
			}

			var pageSizeTexts = this.options.availablePageSizes.replace('-1', this.getPageSizeAllLabel());

			var options = {
				autoWidth: false,
				responsive: true,
				searching: false,	// Hide default searching
				pagingType: self.options.pagingType,
				dom: '<"row"<"liveGridFiltersContainer"f>><"row"<"col-sm-9 liveGridToolbar"><"col-sm-3 liveGridPageLength"l>><"row"<"col-sm-12"rt>><"row"<"col-sm-5"i><"col-sm-7"p>>',
				lengthMenu: [ $.map(self.options.availablePageSizes.split(','), function(v, i) { return parseInt(v, 10); }), pageSizeTexts.split(',')],

				serverSide: true,	// Use server side control for rerendering and filters
				processing: true,	// Displays processing indicator
				ajax: function (data, callback, settings) {	// (data sent to server, callback to call for data rendering, various settings)
					self.onDataTableDrawCalled(data, callback, settings);
				},
				deferLoading: self.options.autoLoadInitialDataset ? false : 0,
				initComplete: function (settings, json) {
					if (self.options.postInitializeCallback)
						self.options.postInitializeCallback();
				},

				language: {
					processing: self.options.processingLabel,
					lengthMenu: '_MENU_',
					info: self.options.recordCountLabel.replace('{0}', '_START_').replace('{1}', '_END_').replace('{2}', '_MAX_'),
					infoEmpty: self.options.noRecordsLabel,
					//infoFiltered: '',
					loadingRecords: self.options.processingLabel,
					paginate: {
						first: self.options.pagingFirstLabel,
						last: self.options.pagingLastLabel,
						previous: self.options.pagingPrevLabel,
						next: self.options.pagingNextLabel
					},
					emptyTable: self.options.noRecordsLabel,
					zeroRecords: self.options.noRecordsLabel
				},

				order: sortColumns,	// The sort columns
				columns: colModel
			};

			this.buildCriteriaAndReload(false);

			this.gridTable.DataTable(options);
			
			/*this.filtersContainer = $('.liveGridFiltersContainer', this.element);
			this.filtersContainer.CiteFilters({
				autoInitialize: true,

				quickSearchLabel: self.options.quickSearchLabel,
				complexFiltersLabel: self.options.complexFiltersLabel,
				complexFiltersApplyButtonLabel: self.options.complexFiltersApplyButtonLabel,
				complexFiltersClearButtonLabel: self.options.complexFiltersClearButtonLabel,
				complexFiltersRemoveAllButtonLabel: self.options.complexFiltersRemoveAllButtonLabel,
				complexFiltersClearFilterButtonLabel: self.options.complexFiltersClearFilterButtonLabel,
				complexFiltersRemoveFilterButtonLabel: self.options.complexFiltersRemoveFilterButtonLabel,

				filteringSections: self.options.filteringSections,
				quickFilteringControlID: self.getQuickFilteringControlID(),
				height: self.options.height,

				applyFiltersButtonCallback: function () {
					self.buildCriteriaAndReload();
					return false;
				},
				allFiltersRemovedCallback: function () {
					self._buildCriteriaAndRefresh(self.invalidateCache);
				},
				criteriaChangedCallback: function () {
					self.buildCriteriaAndReload();
					return false;
				}
			});*/

			this.toolbarContainer = $('.liveGridToolbar', this.element);
			if (this.getRefreshIcon() != $.ui.CiteLiveGrid.RefreshIconMode.None) {
				var mode = this.getRefreshIcon();
				this.addToolbarButton({
					buttonicon: 'fa-refresh',
					title: this.getRefreshButtonLabel(),
					caption: this.getRefreshButtonLabel(),
					position: '',
					onClickButton: mode == $.ui.CiteLiveGrid.RefreshIconMode.Simple ? function () { self.refresh(false); } : this.getCustomRefreshIconCallback()
				}, false);
			}
			if (this.getAddIcon() != $.ui.CiteLiveGrid.AddIconMode.None) {
				this.addToolbarButton({
					Type: $.ui.CiteLiveGrid.ButtonType.Button,
					buttonicon: this.getAddIconClass(),
					title: this.getNewButtonLabel(),
					caption: this.getNewButtonLabel(),
					position: '',
					onClickButton: function (e) {
						e.preventDefault();
						e.stopPropagation();
						self.onAddToolbarButtonClicked();
					}
				}, false);
			}
			// TODO: Edit & Delete we have to handle with checkboxes
			if (this.getEditIcon() != $.ui.CiteLiveGrid.EditMode.None && this.getShowEditIconOnToolbar()) {
				this.addToolbarButton({
					Type: $.ui.CiteLiveGrid.ButtonType.Button,
					buttonicon: this.getEditIconClass(),
					title: this.getEditButtonLabel(),
					caption: this.getEditButtonLabel(),
					position: '',
					onClickButton: function () {
						var rowIds = self.getSelectedRowIds();
						if (rowIds.length > 1) return;
						self.onRowEditButtonClicked(rowIds[0]);
					}
				}, false);
			}
			if (this.getShowDeleteIconOnToolbar()) {
				this.addToolbarButton({
					Type: $.ui.CiteLiveGrid.ButtonType.Button,
					buttonicon: this.getDeleteIconClass(),
					title: this.getDeleteButtonLabel(),
					caption: this.getDeleteButtonLabel(),
					position: '',
					onClickButton: function () {
						var rowIds = self.getSelectedRowIds();
						for (var i = 0; i < rowIds.length; i++)
							self.onRowDeleteButtonClicked(rowIds[i]);
					}
				}, false);
			}

			if (this.getAllowRowSelection()) {
				/// If class exists it removes it and returns false
				var toggleClass = function (el, className) {
					if ($(el).hasClass(className)) {
						$(el).removeClass(className);
						return false;
					} else {
						$(el).addClass(className);
						return true;
					}
				}
				var selectSingleRow = function (el) {
					var wasSelected = $(el).hasClass('selected');
					var hadMultipleSelected = self.getSelectedRows().length > 1;

					self.clearAllSelections();
					if ((wasSelected && hadMultipleSelected) || !wasSelected) {
						$(el).addClass('selected');
						return true;
					}
					return false;
				}

				$('#' + this.element[0].id + '_table', this.element).addClass('table-hover');

				$('tbody', this.element).on('click', 'tr', function (e) {
					e.preventDefault();
					e.stopPropagation();
					var rowId = self.gridTable.DataTable().row(this).id();
					var state = true;
					if (self.getRowClickAction() == $.ui.CiteLiveGrid.ClickMode.Select) {
						if (self.getMultipleSelectionMode() == $.ui.CiteLiveGrid.MultiSelectMode.Key) {
							if (self.getMultipleSelectionKey() == $.ui.CiteLiveGrid.MultiSelectKey.Control && e.ctrlKey)
								state = toggleClass(this, 'selected');
							else if (self.getMultipleSelectionKey() == $.ui.CiteLiveGrid.MultiSelectKey.Shift && e.shiftKey)
								state = toggleClass(this, 'selected');
							else if (self.getMultipleSelectionKey() == $.ui.CiteLiveGrid.MultiSelectKey.Alt && e.altKey)
								state = toggleClass(this, 'selected');
							else {
								state = selectSingleRow(this);
							}
						}
						else {
							state = selectSingleRow(this);
						}
					}

					if (self.getCustomRowClickCallback()) {
						eval(self.getCustomRowClickCallback())(rowId, state);
					}
				}).on('dblclick', 'tr', function (e) {
					e.preventDefault();
					e.stopPropagation();
					var rowId = self.gridTable.DataTable().row(this).id();
					var state = true;
					if (self.getRowDoubleClickAction() == $.ui.CiteLiveGrid.ClickMode.Select) {
						if (self.getMultipleSelectionMode() == $.ui.CiteLiveGrid.MultiSelectMode.Key) {
							if (self.getMultipleSelectionKey() == $.ui.CiteLiveGrid.MultiSelectKey.Control && e.ctrlKey)
								state = toggleClass(this, 'selected');
							else if (self.getMultipleSelectionKey() == $.ui.CiteLiveGrid.MultiSelectKey.Shift && e.shiftKey)
								state = toggleClass(this, 'selected');
							else if (self.getMultipleSelectionKey() == $.ui.CiteLiveGrid.MultiSelectKey.Alt && e.altKey)
								state = toggleClass(this, 'selected');
							else {
								state = selectSingleRow(this);
							}
						}
						else {
							state = selectSingleRow(this);
						}
					}

					if (self.getCustomRowDoubleClickCallback()) {
						eval(self.getCustomRowDoubleClickCallback())(rowId, state);
					}
				});
			}

			var inlineEditorControl = this.option('inlineEditor');
			if (this.getEditIcon() == $.ui.CiteLiveGrid.EditMode.Inline && inlineEditorControl) {
				var inlineEditor = $.ui.CiteBaseControl.find(inlineEditorControl[0].id);
				var cancelCallback = function () {
					self.stopRowEdit();
				};
				inlineEditor.option('cancelCallback', cancelCallback);

				this.gridTable.on('preXhr.dt', function (e, settings, data) {
					self.stopRowEdit();
				});
			}

			this.applyViewMode();

			return options;
		},

		onDataTableDrawCalled: function (data, callback, settings) {
			this.loadDataFunction(data, callback);
		},

		onAddToolbarButtonClicked: function () {
			if (!this.getEnabled() || this.getCustomAddRowCallback() == null || this.isInEditMode()) return;

			var self = this;

			this.getCustomAddRowCallback()(function (newRow) {

				self.isCurrentAddingRowNew = true;

				self.startRowEdit(null, true, newRow);
			});
		},
		
		//removeFilterSection: function (sectionIndex, callFiltersRemoveCallback) {
		//	var self = this;
		//	var filterSections = this.getFilteringSections();
		//	var liveGridFiltersControls = $('.liveGridFiltersControls', this.liveGridFilters);
		//	return function () {
		//		var section = self.liveGridFilters.children('.liveGridFiltersSection:eq(' + sectionIndex + ')');
		//		var controls = filterSections[sectionIndex].Controls;
		//		for (var l = 0; l < controls.length; l++) {
		//			var ctl = $.ui.CiteBaseControl.find(controls[l]);
		//			ctl.clearCurrentValue();
		//		}

		//		self.filterRemovedCallback();
		//		if ((callFiltersRemoveCallback === undefined || callFiltersRemoveCallback == true) && self.liveGridFilters.children('.liveGridFiltersSection:visible').length == 1)
		//			self.allFiltersRemovedCallback();
		//		section.slideUp({
		//			complete: function () {
		//				if (self.liveGridFilters.children('.liveGridFiltersSection:visible').length == 0) {
		//					liveGridFiltersControls.slideUp(function () { self._onInternalGridElementsHeightChanged(); });
		//					if(self.saveFiltersButtonContainer) self.saveFiltersButtonContainer.hide();
		//				}
		//				else
		//					self._onInternalGridElementsHeightChanged();
		//			}

		//		});
		//		return false;
		//	}
		//},
		//addCustomFiltersHtmlButton: function (buttonDesc, container) {
		//	var buttonClick = function (actionId) {
		//		buttonDesc.onClickButton(actionId ? actionId : undefined);
		//	};

		//	var buttonOptions = [];
		//	for (var i = 0; i < buttonDesc.Options.length; i++) {
		//		buttonOptions.push({ text: buttonDesc.Options[i].Label, click: (function (counter) { return function () { buttonClick(buttonDesc.Options[counter].ID); } })(i) });
		//	}

		//	var btn = $('<button class="norestyle"></button>');
		//	btn.attr('title', buttonDesc.title);
		//	btn.sssplitbutton({ text: buttonDesc.caption || null, classes: buttonDesc.CssClass, iconClass: buttonDesc.buttonicon || null, menuItems: buttonOptions }).bind('click', function () { buttonClick(null); return false; });
		//	var buttonEl = btn.sssplitbutton('getRootElement');
		//	buttonEl.addClass('customFiltersBtn');
		//	container.prepend(buttonEl);
		//},

		///
		/// buttonDesc: {
		///		buttonicon: <string>,
		///		title: <string>
		///		caption: <string>,
		///		position: --,
		///		onClickButton: <func>
		/// },
		/// addFirst: add elemet at start else it adds it at End. Default: true
		addToolbarButton: function (buttonDesc, addFirst) {
			if (addFirst === undefined) addFirst = true;
			
			if (buttonDesc.Type && buttonDesc.Type == $.ui.CiteLiveGrid.ButtonType.Button) {
				var btn = $('<button class="btn btn-sm btn-default"><i class="fa ' + buttonDesc.buttonicon + '"></i> ' + buttonDesc.caption + '</button>')
				if (addFirst) this.toolbarContainer.prepend(btn); else this.toolbarContainer.append(btn);

				btn.unbind('click').click(function (e) {
					buttonDesc.onClickButton(e);
				});
			} else {
				var spn = $('<span class="btn" style="font-size: ' + actionIconFontSize + ';" title="' + buttonDesc.title + '"><i class="fa ' + buttonDesc.buttonicon + '"></i></span>')
				if (addFirst) this.toolbarContainer.prepend(spn); else this.toolbarContainer.append(spn);

				spn.unbind('click').click(function (e) {
					buttonDesc.onClickButton(e);
				});
			}
		},
		//addGenerateReportToolbarIcon: function(addToTop, addToBottom) {
		//	var self = this;
		//	this.addCustomButton({
		//		buttonicon: "ui-icon-clipboard",
		//		title: this.getReportButtonLabel(),
		//		caption: "",
		//		position: "last",
		//		onClickButton: function() {
		//			var postData = self.gridTable.getGridParam("postData");
		//			$.extend(postData, { 'filters': self.currentFilters, 'gn': self.getGridName() });
		//			eval(self.getGenerateReportCallback())(true, postData, function(data) { });
		//		}
		//	},
		//		addToTop,
		//		addToBottom
		//	);
		//},

		getControlColumnWidth: function() {
			return 20;
		},
		getGridName: function() {
			return this.option('gridName');
		},
		getColumnDefinitions: function() {
			return this.option('columnDefinitions');
		},
		setColumnDefinitions: function(value) {
			this.option('columnDefinitions', value);
		},
		setRetrieveDataCallback: function(value) {
			return this.option('retrieveDataCallback', value);
		},
		getRetrieveDataCallback: function() {
			return this.option('retrieveDataCallback');
		},
		setRetrieveDataCompletedCallback: function (value) {
			return this.option('retrieveDataCompletedCallback', value);
		},
		getRetrieveDataCompletedCallback: function () {
			return this.option('retrieveDataCompletedCallback');
		},
		setRetrieveDataCallbackFlags: function (value) {
			return this.option('retrieveDataCallbackFlags', value);
		},
		getRetrieveDataCallbackFlags: function () {
			return this.option('retrieveDataCallbackFlags');
		},
		getUpdateColumnModelCallback: function() {
			return this.option('updateColumnModelCallback');
		},
		setUpdateColumnModelCallback: function(value) {
			return this.option('updateColumnModelCallback', value);
		},
		setGenerateReportCallback: function(value) {
			return this.option('generateReportCallback', value);
		},
		getGenerateReportCallback: function() {
			return this.option('generateReportCallback');
		},
		setRowChangeCallback: function(value) {
			return this.option('rowChangeCallback', value);
		},
		getRowChangeCallback: function() {
			return this.option('rowChangeCallback');
		},
		setRowChangeCancelledCallback: function(value) {
			return this.option('rowChangeCancelledCallback', value);
		},
		getRowChangeCancelledCallback: function() {
			return this.option('rowChangeCancelledCallback');
		},
		getBeginRowChangeCallback: function() {
			return this.option('beginRowChangeCallback');
		},
		setBeginRowChangeCallback: function(value) {
			this.option('beginRowChangeCallback', value);
		},
		getShowContextMenuCallback: function() {
			return this.option('showContextMenuCallback');
		},
		setShowContextMenuCallback: function(value) {
			this.option('showContextMenuCallback', value);
		},
		getContextMenuActionCallback: function() {
			return this.option('contextMenuActionCallback');
		},
		getRowAdditionCallback: function() {
			return this.option('rowAdditionCallback');
		},
		getCaption: function() {
			return this.option('caption');
		},
		getVisibleRows: function() {
			return this.option('visibleRows');
		},
		setVisibleRows: function(value) {
			this.option('visibleRows', value);
		},
		getWidth: function() {
			return this.option('width');
		},
		setWidth: function(value) {
			return this.option('width', value);
		},
		getHeight: function() {
			return this.option('height');
		},
		setHeight: function(value) {
			this.option('height', value);
		},
		getMaxHeight: function() {
			return this.option('maxHeight');
		},
		setMaxHeight: function(value) {
			this.option('maxHeight', value);
			this.gridTable.closest('.ui-jqgrid-bdiv').css('max-height', value + 'px');
		},
		//getNavigationMode: function() {
		//	return this.option('navigationMode');
		//},
		getNavigationMode: function() {
			return this.option('pagingType');
		},
		getDefaultSortColumns: function() {
			return this.option('defaultSortColumns');
		},
		getEditIcon: function() {
			return this.option('editIcon');
		},
		getCustomEditIconCallback: function() {
			return this.option('customEditIconCallback');
		},
		setCustomEditIconCallback: function(value) {
			return this.option('customEditIconCallback', value);
		},
		getAddIcon: function() {
			return this.option('addIcon');
		},
		getShowAddIconOnEachGroupHeaderRow: function() {
			return this.option('showAddIconOnEachGroupHeaderRow');
		},
		getAddIconClass: function () {
			return this.option('addIconClass');
		},
		getCustomAddRowCallback: function() {
			return this.option('customAddRowCallback');
		},
		setCustomAddRowCallback: function(value) {
			return this.option('customAddRowCallback', value);
		},
		getShowRowCommandsMenuCallback: function() {
			return this.option('showRowCommandsMenuCallback');
		},
		getRowCommandCallback: function() {
			return this.option('rowCommandCallback');
		},
		getRowClickAction: function() {
			return this.option('rowClickAction');
		},
		getCustomRowClickCallback: function() {
			return this.option('customRowClickCallback');
		},
		setCustomRowClickCallback: function(value) {
			return this.option('customRowClickCallback', value);
		},
		getCustomRowCheckCallback: function() {
			return this.option('customRowCheckCallback');
		},
		setCustomRowCheckCallback: function(value) {
			return this.option('customRowCheckCallback', value);
		},
		getRowDoubleClickAction: function() {
			return this.option('rowDoubleClickAction');
		},
		getCustomRowDoubleClickCallback: function() {
			return this.option('customRowDoubleClickCallback');
		},
		setCustomRowDoubleClickCallback: function(value) {
			return this.option('customRowDoubleClickCallback', value);
		},
		getShowHeaders: function() {
			return this.option('showHeaders');
		},
		getShowRowNumbers: function() {
			return this.option('showRowNumbers');
		},
		getShowContextMenu: function() {
			return this.option('showContextMenu');
		},
		getShowCommandsColumn: function() {
			return this.option('showCommandsColumn');
		},
		getMultipleSelectionMode: function() {
			return this.option('multipleSelectionMode');
		},
		getMultipleSelectionKey: function() {
			return this.option('multipleSelectionKey');
		},
		getAllowCellSelection: function() {
			return this.option('allowCellSelection');
		},
		getAllowRowSelection: function() {
			return this.option('allowRowSelection');
		},
		getAllowDrag: function() {
			return this.option('allowDrag');
		},
		getAllowDropToOtherGrids: function() {
			return this.option('allowDropToOtherGrids');
		},
		getDropTargetGridClientIDs: function() {
			return this.option('dropTargetGridClientIDs');
		},
		getDragStartCallback: function() {
			return this.option('dragStartCallback');
		},
		setDragStartCallback: function(value) {
			this.option('dragStartCallback', value);
		},
		getAllowDrop: function() {
			return this.option('allowDrop');
		},
		getDropCallback: function() {
			return this.option('dropCallback');
		},
		setDropCallback: function(value) {
			this.option('dropCallback', value);
		},
		getDropToOtherGridCallback: function() {
			return this.option('dropToOtherGridCallback');
		},
		getDropAutoId: function() {
			return this.option('dropAutoId');
		},
		getDropCopy: function() {
			return this.option('dropCopy');
		},
		getDropByName: function() {
			return this.option('dropByName');
		},
		getAllowRowReordering: function() {
			return this.option('allowRowReordering');
		},
		getRowsReorderedCallback: function() {
			return this.option('rowsReorderedCallback');
		},
		setRowsReorderedCallback: function(value) {
			this.option('rowsReorderedCallback', value);
		},
		getSelectedRowsKeys: function() {
			return this.option('selectedRowsKeys');
		},
		setSelectedRowsKeys: function(value) {
			this.option('selectedRowsKeys', value);
		},
		getCheckedRowsKeys: function() {
			return this.option('checkedRowsKeys');
		},
		setCheckedRowsKeys: function(value) {
			this.option('checkedRowsKeys', value);
		},
		getAutoLoadInitialDataset: function() {
			return this.option('autoLoadInitialDataset');
		},
		getAvailablePageSizes: function() {
			return this.option('availablePageSizes');
		},
		getFilteringSections: function() {
			return this.option('filteringSections');
		},
		getAdditionalFiltersCallback: function() {
			return this.option('additionalFiltersCallback');
		},
		setAdditionalFiltersCallback: function(value) {
			this.option('additionalFiltersCallback', value);
		},
		getQuickFilteringControlID: function() {
			return this.option('quickFilteringControlID');
		},
		getFixedFilters: function() {
			return this.option('fixedFilters');
		},
		getCustomDeleteRowCallback: function() {
			return this.option('customDeleteRowCallback');
		},
		setCustomDeleteRowCallback: function(value) {
			this.option('customDeleteRowCallback', value);
		},
		getShowDeleteIconOnToolbar: function() {
			return this.option('showDeleteIconOnToolbar');
		},
		getShowDeleteIconOnEachRow: function() {
			return this.option('showDeleteIconOnEachRow');
		},
		getDeleteIconClass: function() {
			return this.option('deleteIconClass');
		},
		getEditIconClass: function() {
			return this.option('editIconClass');
		},
		getViewIconClass: function () {
			return this.option('viewIconClass');
		},
		getShowGenerateReportIconOnToolbar: function() {
			return this.option('showGenerateReportIconOnToolbar');
		},
		getRefreshIcon: function() {
			return this.option('refreshIcon');
		},
		getCustomRefreshIconCallback: function() {
			return this.option('customRefreshIconCallback');
		},
		setCustomRefreshIconCallback: function(value) {
			this.option('customRefreshIconCallback', value);
		},
		getAutoSizeToParent: function() {
			return this.option('autoSizeToParent');
		},
		getMinWidth: function() {
			return this.option('minWidth');
		},
		getEnabled: function() {
			return this.option('enabled');
		},
		setEnabled: function(value) {
			this.option('enabled', value);
		},
		getColumnResizedCallback: function () {
			return this.option('columnResizedCallback');
		},
		setColumnResizedCallback: function (value) {
			this.option('columnResizedCallback', value);
		},
		getDragHandleWidth: function() {
			return this.option('dragHandleWidth');
		},
		getBlockInteractionsInViewMode: function() {
			return this.option('blockInteractionsInViewMode');
		},
		getShowEditIconOnEachRow: function() {
			return this.option('showEditIconOnEachRow');
		},
		getShowEditIconOnToolbar: function() {
			return this.option('showEditIconOnToolbar');
		},
		getShowControlColumnsFirst: function() {
			return this.option('showControlColumnsFirst');
		},
		getAllowMultiColumnSort: function() {
			return this.option('allowMultiColumnSort');
		},
		getEnableRowGrouping: function() {
			return this.option('enableRowGrouping');
		},
		getRowGroupingColumnName: function() {
			return this.option('rowGroupingColumnName');
		},
		invalidateCacheNextLoad: function () {
			this.invalidateCache = true;
		},

		//setRowGroupingColumnName: function(value) {
		//	//var oldGroupCol = this.getRowGroupingColumnName();
		//	//this.option('rowGroupingColumnName', value);

		//	//// Locate the grouping column index
		//	//var colModel = this.gridTable.jqGrid('getGridParam', 'colModel');
		//	//var groupColName = value;
		//	//var groupColIndex = -1;
		//	//for (var c = 0; c < colModel.length; c++) {
		//	//	if (groupColName == colModel[c].name) {
		//	//		groupColIndex = c;
		//	//		break;
		//	//	}
		//	//}

		//	//// Update jqGrid options
		//	//var options = {};
		//	//options.grouping = true;
		//	//options.groupingView = {
		//	//	groupField: [groupColName],
		//	//	groupColumnShow: groupColIndex == -1 ? false : !colModel[groupColIndex].hidden,
		//	//	groupText: ['<span class="btnLiveGridGroupText">{0}</span>' + (this.getShowAddIconOnEachGroupHeaderRow() ? '<span class="ui-icon ui-icon-document btnLiveGridGroupAddItem"></span>' : '')]
		//	//}
		//	//this.gridTable.jqGrid('setGridParam', options);

		//	//// Update sorting
		//	//for (var i = 0; i < this.sortInfo.length; i++) {
		//	//	if (this.sortInfo[i].colName == oldGroupCol) {
		//	//		this.sortInfo.splice(i, 1);
		//	//		break;
		//	//	}
		//	//}
		//	//for (var i = 0; i < this.sortInfo.length; i++) {
		//	//	if (this.sortInfo[i].colName == groupColName) {
		//	//		this.sortInfo.splice(i, 1);
		//	//		break;
		//	//	}
		//	//}
		//	//var tmp = [{ 'colName': groupColName, 'order': 'asc'}].concat(this.sortInfo);
		//	//this.sortInfo = tmp;

		//	//// Set the new grid sorting options
		//	//this.gridTable.jqGrid('setGridParam', this.getSortNameAndOrderStrings());

		//	//if (oldGroupCol)
		//	//	this.toggleColumn(oldGroupCol, true);
		//	//this.updateSortingIcons();
		//},
		getEnableSubGrid: function() {
			return this.option('enableSubGrid');
		},
		getSubGridRowExpandCollapseCallback: function() {
			return this.option('subGridRowExpandCollapseCallback');
		},
		setSubGridRowExpandCollapseCallback: function(value) {
			this.option('subGridRowExpandCollapseCallback', value);
		},
		getAllowOnlyOneExpandedSubGrid: function () {
			return this.option('allowOnlyOneExpandedSubGrid');
		},
		getExpandAllSubGridsOnLoad: function () {
			return this.option('expandAllSubGridsOnLoad');
		},
		getSubGridClientID: function() {
			return this.option('subGridClientID');
		},

		getNoCommandsMessage: function() {
			return this.option('noCommandsMessage');
		},
		getQuickSearchButtonLabel: function() {
			return this.option('quickSearchButtonLabel');
		},
		getComplexFiltersLabel: function() {
			return this.option('complexFiltersLabel');
		},
		getComplexFiltersApplyButtonLabel: function() {
			return this.option('complexFiltersApplyButtonLabel');
		},
		getComplexFiltersClearButtonLabel: function() {
			return this.option('complexFiltersClearButtonLabel');
		},
		getComplexFiltersRemoveAllButtonLabel: function () {
			return this.option('complexFiltersRemoveAllButtonLabel');
		},
		getComplexFiltersClearFilterButtonLabel: function() {
			return this.option('complexFiltersClearFilterButtonLabel');
		},
		getComplexFiltersRemoveFilterButtonLabel: function () {
			return this.option('complexFiltersRemoveFilterButtonLabel');
		},
		getRefreshButtonLabel: function() {
			return this.option('refreshButtonLabel');
		},
		getNewButtonLabel: function() {
			return this.option('newButtonLabel');
		},
		getEditButtonLabel: function() {
			return this.option('editButtonLabel');
		},
		getViewButtonLabel: function () {
			return this.option('viewButtonLabel');
		},
		getDeleteButtonLabel: function() {
			return this.option('deleteButtonLabel');
		},
		getReportButtonLabel: function() {
			return this.option('reportButtonLabel');
		},
		getRecordCountLabel: function() {
			return this.option('recordCountLabel');
		},
		getPageCountLabel: function() {
			return this.option('pageCountLabel');
		},
		getNoRecordsLabel: function() {
			return this.option('noRecordsLabel');
		},
		getAdvancedFiltersLabel: function() {
			return this.option('advancedFiltersLabel');
		},
		getPageSizeAllLabel: function() {
			return this.option('pageSizeAllLabel');
		},

		getCustomButtons: function() {
			return this.option('customButtons');
		},
		setCustomButtons: function(value) {
			this.option('customButtons', value);
		},
		getCustomRowButtons: function() {
			return this.option('customRowButtons');
		},
		setCustomRowButtons: function(value) {
			this.option('customRowButtons', value);
		},
		getCustomFiltersButtons: function() {
			return this.option('customFiltersButtons');
		},
		getRowCreationCallback: function() {
			return this.option('rowCreationCallback');
		},
		setRowCreationCallback: function(value) {
			this.option('rowCreationCallback', value);
		},
		getInlineEditFormContainerClientID: function() {
			return this.option('inlineEditFormContainerClientID');
		},
		getInlineFormEditCustomButtonsInternal: function() {
			return this.option('inlineFormEditCustomButtons');
		},
		getInlineFormEditCustomButtons: function() {
			return this.inlineFormEditButtonDescs;
		},
		getInlineFormEditCustomButtonByID: function(id) {
			var descs = this.inlineFormEditButtonDescs;
			for (var i = 0; i < descs.length; i++)
				if (descs[i].getID() == id)
					return descs[i];
			return null;
		},
		getBeginCellChangeCallback: function() {
			return this.option('beginCellChangeCallback');
		},
		setBeginCellChangeCallback: function(value) {
			this.option('beginCellChangeCallback', value);
		},
		getCellChangeCallback: function() {
			return this.option('cellChangeCallback:');
		},
		setCellChangeCallback: function(value) {
			this.option('cellChangeCallback', value);
		},
		getColumnCheckCallback: function() {
			return this.option('columnCheckCallback');
		},
		setColumnCheckCallback: function(value) {
			this.option('columnCheckCallback', value);
		},
		getPostRenderCallback: function() {
			return this.option('postRenderCallback');
		},
		setPostRenderCallback: function(value) {
			this.option('postRenderCallback', value);
		},
		getAutoFitColumnsToWidth: function() {
			return this.option('autoFitColumnsToWidth');
		},

		locateContextMenu: function() {
			return $('.contextMenu', this.element)[0];
		},
		locateCommandsMenu: function() {
			return $('.commandsMenu', this.element)[0];
		},
		locateInlineEditFormContainer: function() {
			return $('.inlineEditForm', this.element)[0];
		},
		getTable: function() {
			return this.gridTable;
		},

		//resizeColumnsToMinWidth: function (index, newWidth) {
		//	if (this.currentEditRow !== -1) return;

		//	var self = this;
		//	var tbl = $('.ui-jqgrid-btable', this.element);
		//	var htbl = $('.ui-jqgrid-htable', this.element);
		//	var rows = tbl.find('tr:gt(0)');
		//	var hdrTds = htbl.find('tr').children();
		//	var elTmp = $('<div class="lgTempMeasure"></div>');
		//	elTmp.addClass('ui-jqgrid ui-jqgrid-sortable');
		//	var root = $('<div class="LiveGrid ui-jqgrid ui-widget ui-widget-content ui-corner-all"></div>');
		//	root.append(elTmp);
		//	$(document.body).append(root);
		//	var sumWidth = 0;
		//	var selectedWidth = 0, lastWidth = 0;
		//	var selectedColumnName;
		//	var selectedCol, lastCol;
		//	var widths = {};
		//	var colIndices = {};
		//	var colDefs = this.getColumnDefinitions();
		//	var colModel = this.gridTable.jqGrid('getGridParam', 'colModel');
		//	var columnNames = [];
		//	for (var i = 0; i < colModel.length; i++)
		//		columnNames.push(colModel[i].name);

		//	for (var i in colDefs) {
		//		if (!colDefs.hasOwnProperty(i)) continue;
		//		if ((colDefs[i].Name in this.columnIndices) && !colDefs[i].IsHidden && $.inArray(colDefs[i].Name, columnNames) != -1) {
		//			var colIndex = this.columnIndices[colDefs[i].Name];
		//			colIndices[i] = colIndex;
		//			var elWidth = $(hdrTds[colIndex]).width();
		//			sumWidth += elWidth;

		//			if (colDefs[i].Name == colModel[index].name) {
		//				selectedCol = colDefs[i];
		//				colDefs[i].Width = newWidth;
		//				selectedWidth = elWidth;
		//			}
		//			lastCol = colDefs[i];
		//			lastWidth = elWidth;
		//		}
		//	}
		//	var btnWidth = 0;
		//	for (var i = 0; i < colModel.length; i++) {
		//		if(colModel[i].name == 'row_edit' || colModel[i].name == 'row_delete')
		//			btnWidth += this.getControlColumnWidth();
		//	}

		//	elTmp.removeClass('ui-jqgrid ui-jqgrid-sortable');

		//	root.remove();

		//	var wChanged = false;
		//	if (sumWidth + btnWidth < this.options.minWidth) {
		//		lastCol.Width = this.options.minWidth - sumWidth + lastCol.Width - btnWidth;
		//		self.setColumnDefinitions(colDefs);
		//		if (selectedCol.Name == lastCol.Name)
		//			wChanged = true;
		//		var currRowData = self.getAllRowsData();
		//		var currRowContexts = $.extend(true, {}, self.rowContexts);
		//		var currRowRights = $.extend(true, {}, self.rowRights);
		//		var currPage = self.gridTable.getGridParam('page');
		//		var currTotal = self.gridTable.getGridParam('lastpage');
		//		var currRecords = self.gridTable.getGridParam('records');

		//		var f = function (request) {
		//			var json = $.ui.CiteLiveGrid.generateGridDataResponseFromRowArray(currRowData, null, request);
		//			var rows = [];
		//			self.rowContexts = {};
		//			for (var i in json.rows) {
		//				var celldata = [];
		//				for (var j in json.rows[i].Data) {
		//					if (self.columnIndices[j] !== undefined) {
		//						celldata[self.columnIndices[j]] = json.rows[i].Data[j];
		//					}
		//				}

		//				var rowid = self.generateRowId(json.rows[i].Data);
		//				rows.push({ id: rowid, cell: celldata });
		//				//if (json.rows[i].Context)
		//				//self.rowContexts[rowid] = json.rows[i].Context;
		//			}
		//			json.rows = rows;
		//			json.page = currPage;
		//			json.total = currTotal;
		//			json.records = currRecords;
		//			self.rowContexts = $.extend(true, {}, currRowContexts);
		//			self.rowRights = $.extend(true, {}, currRowRights);
		//			self.gridTable[0].addJSONData(json);

		//			self.bindRowEventHandlers();

		//			self.gridTable.setGridParam({ datatype: self.loadDataFunction });
		//		};

		//		self.recreateGrid(f, true);
		//	}
		//	return wChanged;
		//},

		//resizeColumnsToFitContent: function(columnNames) {
		//	if (this.currentEditRow !== -1) return;

		//	var self = this;
		//	var tbl = $('.ui-jqgrid-btable', this.element);
		//	var htbl = $('.ui-jqgrid-htable', this.element);
		//	var rows = tbl.find('tr:gt(0)');
		//	var hdrTds = htbl.find('tr').children();
		//	var elTmp = $('<div class="lgTempMeasure"></div>');
		//	elTmp.addClass('ui-jqgrid ui-jqgrid-sortable');
		//	var root = $('<div class="LiveGrid ui-jqgrid ui-widget ui-widget-content ui-corner-all"></div>');
		//	root.append(elTmp);
		//	$(document.body).append(root);
		//	var currWidths = {};
		//	var widths = {};
		//	var colIndices = {};
		//	var colDefs = this.getColumnDefinitions();
		//	if (columnNames === undefined || columnNames.length == 0)
		//		columnNames = this.gridTable.getGridParam('colNames');
		//	for (var i in colDefs) {
		//		if ((colDefs[i].Name in this.columnIndices) && !colDefs[i].IsHidden && !colDefs[i].IsFixedWidth && $.inArray(colDefs[i].Name, columnNames) != -1) {
		//			var colIndex = this.columnIndices[colDefs[i].Name];
		//			colIndices[i] = colIndex;
		//			var html = $(hdrTds[colIndex]).html();
		//			elTmp.html('<span class="ui-icon" style="display: inline-block"></span><div style="display: inline-block; white-space: nowrap">' + html + '</div>');
		//			widths[i] = elTmp.outerWidth();
		//			currWidths[i] = $(hdrTds[colIndex]).width();
		//		}
		//	}

		//	elTmp.removeClass('ui-jqgrid ui-jqgrid-sortable');

		//	for (var i = 0; i < rows.length; i++) {
		//		var tds = $(rows[i]).children('td');
		//		for (var j in colIndices) {
		//			var html = $(tds[colIndices[j]]).html();
		//			elTmp.html(html);
		//			widths[j] = Math.max(widths[j], elTmp.outerWidth());
		//		}
		//	}
		//	root.remove();

		//	var wChanged = false;
		//	for (var colIndex in widths) {
		//		if ((widths.hasOwnProperty(colIndex) && colDefs[colIndex].Name in self.columnIndices) && !colDefs[colIndex].IsHidden && !colDefs[colIndex].IsFixedWidth && $.inArray(colDefs[colIndex].Name, columnNames) != -1 && currWidths[colIndex] != widths[colIndex]/*colDefs[colIndex].Width != widths[colIndex]*/) {
		//			colDefs[colIndex].Width = widths[colIndex];
		//			wChanged = true;
		//		}
		//	}

		//	if (wChanged) {
		//		self.setColumnDefinitions(colDefs);
		//		var currRowData = self.getAllRowsData();
		//		var currRowContexts = $.extend(true, {}, self.rowContexts);
		//		var currRowRights = $.extend(true, {}, self.rowRights);
		//		var currPage = self.gridTable.getGridParam('page');
		//		var currTotal = self.gridTable.getGridParam('lastpage');
		//		var currRecords = self.gridTable.getGridParam('records');

		//		var f = function(request) {
		//			var json = $.ui.CiteLiveGrid.generateGridDataResponseFromRowArray(currRowData, null, request);
		//			var rows = [];
		//			self.rowContexts = {};
		//			for (var i in json.rows) {
		//				var celldata = [];
		//				for (var j in json.rows[i].Data) {
		//					if (self.columnIndices[j] !== undefined) {
		//						celldata[self.columnIndices[j]] = json.rows[i].Data[j];
		//					}
		//				}

		//				var rowid = self.generateRowId(json.rows[i].Data);
		//				rows.push({ id: rowid, cell: celldata });
		//				//if (json.rows[i].Context)
		//				//self.rowContexts[rowid] = json.rows[i].Context;
		//			}
		//			json.rows = rows;
		//			json.page = currPage;
		//			json.total = currTotal;
		//			json.records = currRecords;
		//			self.rowContexts = $.extend(true, {}, currRowContexts);
		//			self.rowRights = $.extend(true, {}, currRowRights);
		//			self.gridTable[0].addJSONData(json);

		//			self.bindRowEventHandlers();

		//			self.gridTable.setGridParam({ datatype: self.loadDataFunction });
		//		};

		//		self.recreateGrid(f, true);
		//	}
		//	return wChanged;
		//},

		bindRowEventHandlers: function() {
			var self = this;

			// Bind the click handler for row edit icons
			if (self.getEditIcon() != $.ui.CiteLiveGrid.EditMode.None && self.getShowEditIconOnEachRow()) {
				$('.liveGridCommandEditIcon', self.gridTable).unbind('click').click(function(e) {
					e.stopPropagation();
					var rowId = $(this).closest("tr").attr("id");
					self.onRowEditButtonClicked(rowId);
				});
			}

			// Bind the click handler for row delete icons
			if (self.getShowDeleteIconOnEachRow()) {
				$('.liveGridCommandDeleteIcon', self.gridTable).unbind('click').click(function() {
					var rowId = $(this).closest("tr").attr("id");
					self.onRowDeleteButtonClicked(rowId);
				});
			}

			// Bind the click handler for custom icons
			var customRowButtons = self.getCustomRowButtons();
			var el = $('*[class*="liveGridCommandCustom_"]', self.gridTable);
			el.unbind('click').bind('click', function() {
				var str = $(this).attr('class');
				var matches = /liveGridCommandCustom_(\d)/g.exec(str);
				var buttonIndex = matches[1];

				var rowId = $(this).closest("tr").attr("id");
				self.onRowCustomButtonClicked(buttonIndex, rowId);
				return false;
			});

			// Bind the context menu to the command column cells
			if (self.getShowCommandsColumn()) {
				el = $('.jqgrow > td:nth-child(' + self.commandsColumnIndex + ')', self.gridTable);
				el.unbind('click');
				el.contextMenu(self.locateCommandsMenu().id, {
					triggerEvent: 'click',
					eventPosX: self.getShowControlColumnsFirst() ? 'atTriggerLeft' : 'atTriggerRight',
					eventPosY: 'atTriggerBottom',
					menuStyle: { width: 'auto' },
					onContextMenu: function(event, menu) {
						if (self.getShowRowCommandsMenuCallback() == '' || !self.getEnabled())
							return false;

						var rowId = $(event.target).closest("tr").attr("id");
						var rowData = self.getRowData(rowId);
						var data = eval(self.getShowRowCommandsMenuCallback())(rowId, rowData, self.rowContexts[rowId] ? self.rowContexts[rowId] : undefined, self.getCurrentDisplayMode());
						if (data === false) {
							data = [{ 'actionId': '__nocommands', 'text': self.getNoCommandsMessage()}];
							//return false;
						}

						menu.data('itemsDesc', data);
						return true;
					},
					onShowMenu: function(event, menu) {
						var items = menu.data('itemsDesc');
						menu.removeData('itemsDesc');

						$('ul', menu).empty();
						var t = '';
						for (var i in items) {
							t += '<li id="' + items[i].actionId + '">' + items[i].text + '</li>';
						}
						$('ul', menu).append(t);

						return menu;
					},
					bindings: {
						'allitems': function(trigger, actionId) {
							if (actionId == '__nocommands') return;
							var rowId = $(trigger).closest('tr')[0].id;
							var rowData = self.getRowData(rowId);
							if (self.getRowCommandCallback() != '') {
								eval(self.getRowCommandCallback())(rowId, rowData, self.rowContexts[rowId] ? self.rowContexts[rowId] : undefined, actionId);
							}
						}
					}
				});
			}

			// Bind the context menu to the grid rows
			if (self.getShowContextMenu()) {
				el = $('.jqgrow', self.gridTable);
				el.unbind('click');
				el.contextMenu(self.locateContextMenu().id, {
					onContextMenu: function(event, menu) {
						if (self.getShowContextMenuCallback() == '' || !self.getEnabled())
							return false;

						var rowId = $(event.target).parent("tr").attr("id");
						var rowData = self.getRowData(rowId);
						var rowKeys = self.decodeRowId(rowId);

						var data = eval(self.getShowContextMenuCallback())(rowKeys, rowData, self.rowContexts[rowId] ? self.rowContexts[rowId] : undefined);
						if (data === false)
							return false;

						menu.data('itemsDesc', data);
						return true;
					},
					onShowMenu: function(event, menu) {
						var items = menu.data('itemsDesc');
						menu.removeData('itemsDesc');

						$('ul', menu).empty();
						var t = '';
						for (var i in items) {
							t += '<li id="' + items[i].actionId + '">' + items[i].text + '</li>';
						}
						$('ul', menu).append(t);
						return menu;
					},
					bindings: {
						'allitems': function(trigger, actionId) {
							var rowId = trigger.id;
							var rowData = self.getRowData(rowId);
							var rowKeys = self.decodeRowId(rowId);

							if (self.getContextMenuActionCallback() != '') {
								eval(self.getContextMenuActionCallback())(rowKeys, rowData, self.rowContexts[rowId] ? self.rowContexts[rowId] : undefined, actionId);
							}
						}
					}
				});
			}

			// Bind the click handler for add group item icons
			if (self.getEnableRowGrouping() && self.getShowAddIconOnEachGroupHeaderRow()) {
				el = $('.jqgroup .btnLiveGridGroupAddItem', self.gridTable);
				el.unbind('click');
				el.click(function() {
					if (!self.getEnabled()) return false;

					var formattedGroupValue = $(this).siblings('.btnLiveGridGroupText').html();
					var groupValue = null;
					var groupColName = self.getRowGroupingColumnName();
					var colModel = self.gridTable.jqGrid('getGridParam', 'colModel');
					for (var c = 0; c < colModel.length; c++) {
						if (groupColName == colModel[c].name) {
							groupValue = colModel[c].unformat(formattedGroupValue, null);
							break;
						}
					}
					if (self.getAddIcon() == $.ui.CiteLiveGrid.AddIconMode.Callback) {
						if (!self.getCustomAddRowCallback()) return false;
						eval(self.getCustomAddRowCallback())(groupValue);
					}
					return false;
				});
			}
		},

		//updateSortingIcons: function() {
		//	var colModel = this.gridTable.jqGrid('getGridParam', 'colModel');

		//	// Update the sort icons for all columns
		//	for (var j = 0; j < colModel.length; j++) {
		//		var siIndex = -1;
		//		for (var i = 0; i < this.sortInfo.length; i++) {
		//			if (colModel[j].index == this.sortInfo[i].colName) {
		//				siIndex = i;
		//				break;
		//			}
		//		}

		//		var icons = $('.ui-jqgrid-hdiv th:eq(' + j + ') div.ui-jqgrid-sortable>span.s-ico', this.element);
		//		if (siIndex == -1) {
		//			icons.hide();
		//		}
		//		else {
		//			var ascIcon = icons.find('>span.ui-icon-asc');
		//			var descIcon = icons.find('>span.ui-icon-desc');
		//			if (this.sortInfo[siIndex].order == 'asc') {
		//				icons.show();
		//				ascIcon.removeClass('ui-state-disabled');
		//				descIcon.addClass('ui-state-disabled');
		//			}
		//			else if (this.sortInfo[siIndex].order == 'desc') {
		//				icons.show();
		//				ascIcon.addClass('ui-state-disabled');
		//				descIcon.removeClass('ui-state-disabled');
		//			}
		//		}
		//	}

		//	$('.ui-jqgrid-hdiv th', this.element).removeAttr("aria-selected");
		//},
		setSortChangeCallback: function (value) {
			return this.option('sortChangeCallback', value);
		},
		getSortChangeCallback: function () {
			return this.option('sortChangeCallback');
		},
		//clearSort: function () {
		//	this.sortInfo = [];
		//	this.updateSortingIcons();
		//	this.gridTable.jqGrid('setGridParam', this.getSortNameAndOrderStrings());
		//},
		getSortNameAndOrderStrings: function() {
			var sortname = [];
			var sortorder = [];
			for (var i = 0; i < this.sortInfo.length; i++) {
				sortname.push(this.sortInfo[i].colName);
				sortorder.push(this.sortInfo[i].order);
			}
			return { 'sortname': sortname.join(' '), 'sortorder': sortorder.join(' ') };
		},
		//recreateGrid: function(dataFunction, preserveSorting) {
		//	if (!this._stringIsNullOrEmpty(this.getQuickFilteringControlID())) {
		//		this.element.append($('#' + this.getQuickFilteringControlID()));
		//	}

		//	var filterSections = this.getFilteringSections();
		//	if (filterSections.length > 0) {
		//		this.element.append(this.liveGridFilters);
		//	}

		//	this.setVisibleRows(this.gridTable.jqGrid('getGridParam', 'rowNum'));
		//	this.gridTable.jqGrid('GridUnload');
		//	this.gridTable = this.element.find('.liveGridTable');
		//	this.initGrid(dataFunction, preserveSorting);
		//	this._onInternalGridElementsHeightChanged();
		//},
		//updateColumns: function(doRefresh) {
		//	if (doRefresh === undefined) doRefresh = true;
		//	var self = this;
		//	eval(this.getUpdateColumnModelCallback())(this.getGridName(), function(colModel) {
		//		self.setColumnDefinitions(colModel['cd']);
		//		self.recreateGrid(undefined, true);
		//		if (doRefresh)
		//			self.refresh(null, this.invalidateCache);
		//	});
		//},
		getFilteringControl: function(typeName) {
			var filterSections = this.getFilteringSections();
			for (var i = 0; i < filterSections.length; i++) {
				var controls = filterSections[i].Controls;
				for (var j = 0; j < controls.length; j++) {
					var ctl = $.ui.CiteBaseControl.find(controls[j]);
					if (Object.getTypeName(ctl) === typeName)
						return ctl;
				}
			}
			return null;
		},
		buildCriteriaAndReload: function (bReload, pageNum) {
			if (this.currentEditRow != -1) return;
			if (bReload === undefined) bReload = true;
			var filterSections = this.getFilteringSections();
			this.currentFilters = [];

			for (var i = 0; i < filterSections.length; i++) {
				var controls = filterSections[i].Controls;
				for (var j = 0; j < controls.length; j++) {
					var ctl = $.ui.CiteBaseControl.find(controls[j]);
					if (ctl) this.currentFilters.push(ctl.getCriteria());
				}
			}
			if (!this._stringIsNullOrEmpty(this.getQuickFilteringControlID())) {
				var ctl = $.ui.CiteBaseControl.find(this.getQuickFilteringControlID());
				this.currentFilters.push(ctl.getCriteria());
			}
			var fixedFilters = this.getFixedFilters();
			for (var i = 0; i < fixedFilters.length; i++) {
				this.currentFilters.push(fixedFilters[i]);
			}
			if (this.getAdditionalFiltersCallback() != '')
				this.currentFilters = this.currentFilters.concat(eval(this.getAdditionalFiltersCallback())(this.currentFilters));

			if (bReload) {
				// TODO: Is not supporting page num yet
				this.refresh(true, this.invalidateCache);
			}
		},
		getCurrentFilters: function() {
			return this.currentFilters;
		},
		//clearCriteria: function() {
		//	var filterSections = this.getFilteringSections();
		//	for (var i = 0; i < filterSections.length; i++)
		//		this.clearCriterion(i);
		//},
		//clearCriterion: function(i) {
		//	var filterSections = this.getFilteringSections();
		//	var controls = filterSections[i].Controls;
		//	for (var j = 0; j < controls.length; j++) {
		//		var ctl = $.ui.CiteBaseControl.find(controls[j]);
		//		if (ctl.hasInitialized())
		//			ctl.clearCurrentValue();
		//	}
		//},

		onRowEditButtonClicked: function(rowid) {
			if (!this.getEnabled()) return;
			if (this.currentEditRow != -1 && this.currentEditColumn != null) return; /* don't continue if cell edit is currently active */

			if (this.currentEditRow != -1) {
				this.stopRowEdit(true);
			} else {
				if (this.getEditIcon() == $.ui.CiteLiveGrid.EditMode.Callback) {
					if (this.getCustomEditIconCallback() == '') return;
					if (rowid) {
						var keyValues = this.decodeRowId(rowid);
						var data = this.getRowData(rowid);
						eval(this.getCustomEditIconCallback())(rowid, keyValues, data, this.rowContexts[rowid] ? this.rowContexts[rowid] : undefined);
					}
				} else if (this.getEditIcon() == $.ui.CiteLiveGrid.EditMode.Inline) {
					this.startRowEdit(rowid, false);
				}
			}
		},
		onRowDeleteButtonClicked: function(rowid) {
			if (this.currentEditRow != -1) return;
			if (!this.getEnabled()) return;
			if (this.getCustomDeleteRowCallback() == '') return;
			if (rowid) {
				var keyValues = this.decodeRowId(rowid);
				var data = this.getRowData(rowid);
				var bDelete = this.getCustomDeleteRowCallback()(rowid, keyValues, data, this.rowContexts[rowid] ? this.rowContexts[rowid] : undefined);
			}
		},
		onRowCustomButtonClicked: function(buttonIndex, rowid) {
			if (!this.getEnabled()) return;
			var crb = this.getCustomRowButtons()[buttonIndex];
			if (crb.ClickCallback == '') return;
			var keyValues = this.decodeRowId(rowid);
			var data = this.getRowData(rowid);
			delete data['row_custom_' + buttonIndex];
			eval(crb.ClickCallback)(rowid, keyValues, data, this.rowContexts[rowid] ? this.rowContexts[rowid] : undefined);
		},

		//onInlineEditFormatterStateChanged: function(rowid, colName, newValue) {
		//	var event = jQuery.Event("rowDataChanged");
		//	event.rowId = rowid;
		//	event.columnName = colName;
		//	if (newValue)
		//		event.newValue = newValue;
		//	this.element.trigger(event);
		//},
		startRowEdit: function (id, isNewRow, rowData, callback) {
			var self = this;
			if (this.currentEditRow !== -1 && this.currentEditColumn !== null) return;

			if (isNewRow) {

				this.stopRowEdit();

				var inlineEditorControl = self.option('inlineEditor');
				var inlineEditor = $.ui.CiteBaseControl.find(inlineEditorControl[0].id);
				if (inlineEditor === null) return;

				//Set data and save callback.
				var saveCallback = function () {
					var gridInlineSaveCallback = self.option('inlineEditorSaveItemCallback');
					if (gridInlineSaveCallback === '') return;

					var data = inlineEditor.getData();
					var keyValues = self.extractKesFromData(data);
					eval(gridInlineSaveCallback)(keyValues, data, function () {
						self.stopRowEdit();
						self.refresh();
					});
				};
				inlineEditor.setData(rowData);
				inlineEditor.option('saveCallback', saveCallback);

				//Display Inline Editor

				//var table = $('#example').DataTable();

				var tBody = this.gridTable.DataTable().table().body();
				
				this.newItemContainer = $('<tr><td colspan="100"></td></tr>');

				$(tBody).prepend(this.newItemContainer);

				inlineEditor.setCurrentDisplayModeAndApply(self.getCurrentDisplayMode());
				self.option('inlineEditor').show();
				this.newItemContainer.find('td').append(self.options.inlineEditor);
				this.newItemContainer.addClass('shown');

			} else {
				var row = this.gridTable.DataTable().row('#' + id);
				var tr = this.gridTable.DataTable().row('#' + id).node();

				if (row.child.isShown()) {
					this.stopRowEdit();

					row.child.hide();

					$(tr).removeClass('shown');
				} else {
					this.stopRowEdit();

					var keyValues = this.decodeRowId(id);

					this.options.inlineEditorGetItemCallback(keyValues, function (data) {
						var inlineEditorControl = self.option('inlineEditor');
						var inlineEditor = $.ui.CiteBaseControl.find(inlineEditorControl[0].id);
						if (inlineEditor === null) return;

						//Set data and save callback.
						var saveCallback = function () {
							var gridInlineSaveCallback = self.option('inlineEditorSaveItemCallback');
							if (gridInlineSaveCallback === '') return;

							var data = inlineEditor.getData();
							eval(gridInlineSaveCallback)(keyValues, data, function () {
								self.stopRowEdit();
								self.refresh(false);
							});
						};
						inlineEditor.setData(data);
						inlineEditor.option('saveCallback', saveCallback);

						//Display Inline Editor
						inlineEditor.setCurrentDisplayModeAndApply(self.getCurrentDisplayMode());
						self.option('inlineEditor').show();
						row.child(self.options.inlineEditor).show();
						$(tr).addClass('shown');
					});
				}
			}
		},
		stopRowEdit: function () {
			var self = this;

			// Detach editor from editing row and hide it
			// If we do not detach it, datatables will delete it because it belongs to the child row (datatables)
			//var edtr = $('#' + this.options.inlineEditor[0].id, this.element);
			var edtr = this.option('inlineEditor');
			edtr.hide();
			this.element.append(edtr);

			if (this.newItemContainer) {
				this.newItemContainer.remove();
				this.newItemContainer = null;
				this.isCurrentAddingRow = false;
			}

			this.gridTable.DataTable()
				.rows('.shown')
				.nodes()
				.each(function (el, index) {
					self.gridTable.DataTable().row(el).child.hide();
					$(el).removeClass('shown');
				});
		},
		//saveEditedRow: function() {
		//	if (this.getRowChangeCallback()) {
		//		var id = this.currentEditRow;
		//		var data = this.getRowData(id);
		//		var keyValues = this.decodeRowId(id);
		//		var self = this;
		//		eval(this.getRowChangeCallback())(keyValues, data, this.rowContexts[id] ? this.rowContexts[id] : undefined, function(newData) {
		//			if (newData === 'continueedit') return;
		//			else {
		//				self.stopRowEdit(false);
		//				if (newData !== null && newData !== false) {   // update the row using the supplied modified data
		//					self.setRowData(id, newData);
		//				}
		//			}
		//		});
		//	}
		//},
		//showInfoBalloonOnCell: function(rowId, colName, html) {
		//	if (this.currentEditRow != -1) return;
		//	var c = this.inlineFormCellEditContainer.find('.customInfoContainer');
		//	if (c.length == 0) {
		//		c = $('<div class="customInfoContainer"></div>');
		//		this.inlineFormCellEditContainer.append(c);
		//	}
		//	c.html(html);
		//	this.positionCellEditBalloon(rowId, colName);
		//},
		//positionCellEditBalloon: function(rowId, colName) {
		//	if (rowId === undefined) rowId = this.currentEditRow;
		//	if (colName === undefined) colName = this.currentEditColumn;
		//	var jqgColIndex = this.columnNameToRealColumnIndex(colName);
		//	var tr = $('#' + rowId, this.gridTable);
		//	var td = $('td:eq(' + jqgColIndex + ')', tr);

		//	var gravity = 'n';
		//	this.inlineFormCellEditContainer.show().position({ my: 'center top', at: 'center bottom', of: td, using: function(pos, info) {
		//		var newGravity = gravity;
		//		switch (gravity.charAt(0)) {
		//			case 'n':
		//				if ((info.element.top + info.element.height) <= (info.target.top + info.target.height))
		//					newGravity = 's';
		//				break;
		//			case 's':
		//				if (info.element.top >= info.target.top)
		//					newGravity = 'n';
		//				break;
		//			case 'e':
		//				if (info.element.left >= info.target.left)
		//					newGravity = 'w';
		//				break;
		//			case 'w':
		//				if ((info.element.left + info.element.width) <= info.target.left)
		//					newGravity = 'e';
		//				break;
		//		}

		//		classList = this.inlineFormCellEditContainer[0].className.split(/\s+/);
		//		for (var i = 0; i < classList.length; i++) {
		//			if (classList[i].indexOf('inlineFormCellEditContainer-') === 0) {
		//				this.inlineFormCellEditContainer.removeClass(classList[i]);
		//				break;
		//			}
		//		}
		//		this.inlineFormCellEditContainer.addClass('inlineFormCellEditContainer-' + newGravity);
		//		this.inlineFormCellEditContainer.css(pos);
		//	}
		//	});
		//},
		//onToggleSubGrid: function(subgridId, rowId, expand) {
		//	if (expand) {
		//		if (this.getAllowOnlyOneExpandedSubGrid() && this.currentExpandedRowId != null && this.currentExpandedRowId != rowId) {
		//			this.gridTable.jqGrid('collapseSubGridRow', this.currentExpandedRowId);
		//		}

		//		if (this.getSubGridClientID())
		//			$('#' + this.getSubGridClientID()).appendTo($('#' + subgridId)).show();

		//		this.currentExpandedRowId = rowId;
		//	}
		//	else {
		//		if (this.getSubGridClientID())
		//			$('#' + this.getSubGridClientID()).appendTo(this.element).hide();
		//		this.currentExpandedRowId = null;
		//	}

		//	var rowData = this.getRowData(rowId);
		//	var keyValues = this.decodeRowId(rowId);
		//	eval(this.getSubGridRowExpandCollapseCallback())(expand, rowId, keyValues, rowData, this.rowContexts[rowId] ? this.rowContexts[rowId] : undefined, subgridId);
		//},
		//collapseExpandedSubGridRow: function() {
		//	if (this.currentExpandedRowId != null) {
		//		this.gridTable.jqGrid('collapseSubGridRow', this.currentExpandedRowId);
		//		this.currentExpandedRowId = null;
		//	}
		//},
		//collapseSubGridRow: function (rowId) {
		//	this.gridTable.jqGrid('collapseSubGridRow', this.rowId);
		//},
		//expandSubGridRow: function(rowId) {
		//	this.gridTable.jqGrid('expandSubGridRow', rowId);
		//	this.currentExpandedRowId = rowId;
		//},
		isInEditMode: function() {
			return this.currentEditRow != -1;
		},
		generateRowId: function(rowData) {
			var id = '';
			var lengths = '';
			for (var i = 0; i < this.keyColumnNames.length; i++) {
				var p = '' + rowData[this.keyColumnNames[i]];
				lengths += p.length + '_';
				id += p;
			}
			return lengths + id;
		},
		decodeRowId: function(rowId) {
			var regex = new RegExp("((?:\\d+_){" + this.keyColumnNames.length + "})(.*)");
			var p = rowId.match(regex);
			var lengths = p[1].split('_');
			lengths.splice(lengths.length - 1, 1);
			var keys = {};
			var start = 0;
			for (var i = 0; i < lengths.length; i++) {
				keys[this.keyColumnNames[i]] = p[2].substr(start, lengths[i]);
				start += parseInt(lengths[i], 10);
			}
			return keys;
		},
		extractKesFromData: function (data) {
			var keys = {};
			for (var i = 0; i < this.keyColumnNames.length; i++) {
				var key = this.keyColumnNames[i];
				keys[key] = data[key];
			}
			return keys;
		},
		realColumnIndexToColumnName: function(iCol) {
			if (this.getMultipleSelectionMode() == $.ui.CiteLiveGrid.MultiSelectMode.Checkbox) iCol--;
			if (this.getShowRowNumbers()) iCol--;
			for (var i in this.columnIndices) {
				if (this.columnIndices[i] == iCol)
					return i;
			}
		},
		columnNameToRealColumnIndex: function(colName) {
			var iCol = this.columnIndices[colName];
			if (this.getMultipleSelectionMode() == $.ui.CiteLiveGrid.MultiSelectMode.Checkbox) iCol++;
			if (this.getShowRowNumbers()) iCol++;
			if (this.getEnableSubGrid()) iCol++;
			return iCol;
		},
		getColumnDefinitionByColumnName: function(colName) {
			var colDefs = this.getColumnDefinitions();
			for (var i in colDefs)
				if (colDefs[i].Name == colName) return colDefs[i];
			return null;
		},
		updateRowSelectionFromCellSelections: function(rowId) {
			if (this.getAllowCellSelection() && this.getAllowRowSelection()) {
				var areCellsSelected = false;
				for (var i = 0; i < this.selectedCells.length; i++) {
					if (this.selectedCells[i].rowId == rowId) {
						areCellsSelected = true;
						break;
					}
				}

				var selectedRowIds = this.getSelectedRowIds();
				var isRowSelected = false;
				for (var i = 0; i < selectedRowIds.length; i++) {
					if (selectedRowIds[i] == rowId) {
						isRowSelected = true;
						break;
					}
				}

				if ((areCellsSelected && !isRowSelected) || (!areCellsSelected && isRowSelected)) {
					this._selectRow(rowId, false);
				}

				this.setSelectedRowsKeys(this.getSelectedRows());
			}
		},
		updateRowIdFromData: function(currentRowId) {
			var newRowId = this.generateRowId(this.getRowData(currentRowId));
			if (newRowId != currentRowId) {
				if (this.rowContexts[currentRowId]) {
					this.rowContexts[newRowId] = this.rowContexts[currentRowId];
					delete this.rowContexts[currentRowId];
				}
				if (this.rowRights[currentRowId]) {
					this.rowRights[newRowId] = this.rowRights[currentRowId];
					delete this.rowRights[currentRowId];
				}
				$('tr#' + currentRowId, this.gridTable).attr('id', newRowId);
			}
		},
		getVisibleRowIndexFromId: function(rowId) {
			return this.gridTable.jqGrid('getInd', rowId);
		},
		formatCellValue: function(rowId, columnName) {
			var value = this.getCellData(rowId, columnName);
			var colModel = this.gridTable.jqGrid('getGridParam', 'colModel');
			for (var c = 0; c < colModel.length; c++) {
				if (columnName == colModel[c].name) {
					return $.isFunction(colModel[c].formatter) ? colModel[c].formatter(value, { 'colModel': colModel[c], 'gid': this.gridTable[0].id, 'rowId': rowId }, this.getRowData(rowId)) : value;
				}
			}
			return value;
		},

		///////////////////////////////////////////////
		//
		// COLUMN SELECTION FUNCTIONS
		//
		///////////////////////////////////////////////
		getCheckedColumnNames: function() {
			var names = [];
			var checks = $('input.liveGridColCheck:checked', this.element);
			for (var i = 0; i < checks.length; i++) {
				classList = checks[i].className.split(/\s+/);
				for (var j = 0; j < classList.length; j++) {
					if (classList[j].substr(0, 9) == 'hdrCheck_') {
						names.push(classList[j].split('_')[1]);
						break;
					}
				}
			}
			return names;
		},
		checkColumn: function(name, check) {
			$('input.hdrCheck_' + name, this.element).prop('checked', check);
		},

		///////////////////////////////////////////////
		//
		// CELL AND ROW SELECTION FUNCTIONS
		//
		///////////////////////////////////////////////
		onRowSelection: function(rowid, status) {
			if (!this.getEnabled()) return;
			//if (this.getRowClickAction() != $.ui.CiteLiveGrid.ClickMode.Select) {
			//	this.onRowEditButtonClicked(rowid);
			//}
			//else {
			//	if (!this.getAllowRowSelection()) {
			//		this.gridTable.jqGrid('resetSelection');
			//		return;
			//	}
			//	else {
			//		this.setSelectedRowsKeys(this.getSelectedRows());
			//	}

			//	// Restore the row that is being edited, when clicking on another row
			//	if (this.isEditable && rowid && (this.currentEditRow != -1) && (rowid !== this.currentEditRow)) {
			//		this.gridTable.jqGrid('restoreRow', this.currentEditRow);
			//		this.currentEditRow = -1;
			//		this.isCurrentEditRowNew = false;
			//	}
			//}

			// Handle checkbox-based row selection, if a custom selection row is not specified.
			if (this.getCustomRowClickCallback()) {
				eval(this.getCustomRowClickCallback())(rowid, status);
			}
			else if (this.getMultipleSelectionMode() == $.ui.CiteLiveGrid.MultiSelectMode.Checkbox) {
				var keys = this.decodeRowId(rowid);
				var checkedKeys = this.getCheckedRowsKeys();
				if (status) {
					if (!(rowid in this.checkedRowInfo)) {
						this.checkedRowInfo[rowid] = 1;
						checkedKeys.push({ 'rowKeys': keys, 'rowContext': {} });
					}
				}
				else {
					if (rowid in this.checkedRowInfo) {
						delete this.checkedRowInfo[rowid];
						var found = 0;
						for (var k = 0; k < checkedKeys.length; k++) {
							found = k;
							for (var ki in keys) {
								if (checkedKeys[k].rowKeys[ki] != keys[ki]) {
									found = -1;
									break;
								}
							}
							if (found != -1) {
								checkedKeys.splice(k, 1);
								break;
							}
						}
					}
				}
				this.setCheckedRowsKeys(checkedKeys);

				if (this.getCustomRowCheckCallback())
					eval(this.getCustomRowCheckCallback())(rowid, status);
			}
		},
		_selectRow: function(rowId, clearPreviousSelection, triggerSelectEvent) {
			if (triggerSelectEvent === undefined) triggerSelectEvent = false;
			if (this.getAllowRowSelection() || this.isEditable) {
				if (clearPreviousSelection === undefined) clearPreviousSelection = true;

				if (this.getMultipleSelectionMode() == $.ui.CiteLiveGrid.MultiSelectKey.None || clearPreviousSelection) this.clearAllSelections();
				

				this.gridTable.DataTable()
					.row(rowId)
					.node()
					.addClass('selected');

				//var allRowIds = this.gridTable.jqGrid('getDataIDs');

				//// If the row is currently visible, select it
				//if ($.inArray(rowId, allRowIds) != -1) {
				//	this.gridTable.jqGrid('setSelection', rowId, triggerSelectEvent);
				//	this.setSelectedRowsKeys(this.getSelectedRows());
				//}
				//	// If not (e.g. it's part of another page), update the internal structures if in checkbox multi-select mode
				//else {
				//	if (this.getMultipleSelectionMode() == $.ui.CiteLiveGrid.MultiSelectMode.Checkbox)
				//		this.onRowSelection(rowId, (rowId in this.checkedRowInfo) ? false : true);
				//}
			}
		},
		selectRow: function(rowId, clearPreviousSelection) {
			this._selectRow(rowId, clearPreviousSelection, true);
		},
		selectCell: function(rowId, colName, clearPreviousSelection) {
			if (this.getAllowCellSelection()) {
				if (clearPreviousSelection === undefined) clearPreviousSelection = true;

				if (clearPreviousSelection) {
					$('td', this.gridTable).removeClass('selected-cell');
					this.selectedCells = [];
				}

				var iCol = this.columnNameToRealColumnIndex(colName);
				$('tr#' + rowId + ' td:eq(' + iCol + ')', this.gridTable).addClass('selected-cell');
				this.selectedCells.push({ rowId: rowId, iCol: iCol });

				this.updateRowSelectionFromCellSelections(rowId);
			}
		},
		deselectCell: function(rowId, colName) {
			if (this.getAllowCellSelection()) {
				var iCol = this.columnNameToRealColumnIndex(colName);
				for (var i = 0; i < this.selectedCells.length; i++) {
					if (this.selectedCells[i].rowId == rowId && this.selectedCells[i].iCol == iCol) {
						$('tr#' + rowId + ' td:eq(' + iCol + ')', this.gridTable).removeClass('selected-cell');
						this.selectedCells.splice(i, 1);
						this.updateRowSelectionFromCellSelections(rowId);
						return;
					}
				}
			}
		},
		toggleCellSelection: function(rowId, colName) {
			if (this.getAllowCellSelection()) {
				var iCol = this.columnNameToRealColumnIndex(colName);
				for (var i = 0; i < this.selectedCells.length; i++) {
					if (this.selectedCells[i].rowId == rowId && this.selectedCells[i].iCol == iCol) {
						$('tr#' + rowId + ' td:eq(' + iCol + ')', this.gridTable).removeClass('selected-cell');
						this.selectedCells.splice(i, 1);
						this.updateRowSelectionFromCellSelections(rowId);
						return;
					}
				}

				this.selectCell(rowId, colName, false);
			}
		},
		getCheckedRowIds: function() {
			var ids = [];
			for (var i in this.checkedRowInfo) {
				if (this.checkedRowInfo.hasOwnProperty(i))
					ids.push(i);
			}
			return ids;
		},
		getSelectedRowIds: function() {
			var rowIds = [];
			if (this.getAllowRowSelection()) {
				var multiselect = this.getMultipleSelectionMode();
				if (multiselect != $.ui.CiteLiveGrid.MultiSelectMode.None) {
					rowIds = this.gridTable.DataTable()
								.rows('.selected')
								.ids();
				}
				else {
					var r = this.gridTable.DataTable()
								.row('.selected')
								.id();
					if (r != null)
						rowIds.push(r);
				}
			}
			return rowIds;
		},
		getSelectedRows: function() {
			var selectedRows = [];
			var rowIds = this.getSelectedRowIds();
			for (var i = 0; i < rowIds.length; i++) {
				selectedRows.push({
					rowKeys: this.decodeRowId(rowIds[i]),
					rowContext: this.rowContexts[rowIds[i]] ? this.rowContexts[rowIds[i]] : undefined
				});
			}
			return selectedRows;
		},
		getSelectedCells: function() {
			var selectedCells = [];
			for (var i = 0; i < this.selectedCells.length; i++) {
				var rowId = this.selectedCells[i].rowId;
				var colName = this.realColumnIndexToColumnName(this.selectedCells[i].iCol);
				selectedCells.push({
					rowKeys: this.decodeRowId(rowId),
					rowContext: this.rowContexts[rowId] ? this.rowContexts[rowId] : undefined,
					columnName: colName,
					cellValue: this.gridTable.jqGrid('getCell', rowId, this.selectedCells[i].iCol)
				});
			}
			return selectedCells;
		},

		clearAllSelections: function() {
			this.gridTable.DataTable()
				.rows('.selected')
				.nodes()
				.each(function (el, index) {
					$(el).removeClass('selected');
				});
		},
		//filterRemovedCallback: function() {
		//},

		//allFiltersRemovedCallback: function () {
		//	this._buildCriteriaAndRefresh(this.invalidateCache);
		//},

		///////////////////////////////////////////////
		//
		// DATA MANIPULATION FUNCTIONS
		//
		///////////////////////////////////////////////
		getNumberOfRows: function() {
			return this.gridTable.DataTable().page.info().recordsTotal;
		},
		getVisibleRowIds: function () {
			throw 'Has to be changed';
			return this.gridTable.jqGrid('getDataIDs');
		},
		getRowContext: function(rowId) {
			if (typeof (rowId) == 'object') rowId = this.generateRowId(rowId);
			return this.rowContexts[rowId] ? this.rowContexts[rowId] : undefined;
		},
		setRowContext: function(rowId, context) {
			if (typeof (rowId) == 'object') rowId = this.generateRowId(rowId);
			this.rowContexts[rowId] = context;
			this.setSelectedRowsKeys(this.getSelectedRows());
		},
		getRowRights: function(rowId) {
			if (typeof (rowId) == 'object') rowId = this.generateRowId(rowId);
			return this.rowRights[rowId] ? this.rowRights[rowId] : undefined;
		},
		getCellData: function(rowId, colName) {
			if (typeof (rowId) == 'object') rowId = this.generateRowId(rowId);
			return this.gridTable.jqGrid('getCell', rowId, colName);
		},
		setCellData: function(rowId, colName, data) {
			if (typeof (rowId) == 'object') rowId = this.generateRowId(rowId);
			this.gridTable.jqGrid('setCell', rowId, colName, data, "", {}, true);
			this.setSelectedRowsKeys(this.getSelectedRows());
		},
		getAllRowsData: function() {
			var data = null;
			if (this.gridTable.jqGrid('getGridParam', 'datatype') == 'local') {
				data = this.gridTable.jqGrid('getGridParam', 'data');
			}
			else {
				data = this.gridTable.jqGrid('getRowData');
			}
			if (this.getShowCommandsColumn()) {
				for (var i in data) {
					delete data[i]['grid_commands'];
				}
			}
			if (this.getEditIcon() != $.ui.CiteLiveGrid.EditMode.None && this.getShowEditIconOnEachRow()) {
				for (var i in data) {
					delete data[i]['row_edit'];
				}
			}
			if (this.getShowDeleteIconOnEachRow()) {
				for (var i in data) {
					delete data[i]['row_delete'];
				}
			}
			if (this.getAllowDrag() || this.getAllowRowReordering()) {
				for (var i in data) {
					delete data[i]['row_drag'];
				}
			}
			var customRowButtons = this.getCustomRowButtons();
			if (customRowButtons.length > 0) {
				for (var i in data) {
					for (var crb = 0; crb < customRowButtons.length; crb++)
						delete data[i]['row_custom_' + crb];
				}
			}
			return data;
		},
		getRowData: function(rowId) {
			if (typeof (rowId) == 'object') rowId = this.generateRowId(rowId);
			var rowData = this.gridTable.DataTable().row('#' + rowId).data();
			if (this.getShowCommandsColumn())
				delete rowData['grid_commands'];
			if (this.getEditIcon() != $.ui.CiteLiveGrid.EditMode.None && this.getShowEditIconOnEachRow())
				delete rowData['row_edit'];
			if (this.getShowDeleteIconOnEachRow())
				delete rowData['row_delete'];
			if (this.getAllowDrag() || this.getAllowRowReordering())
				delete rowData['row_drag'];
			var customRowButtons = this.getCustomRowButtons();
			if (customRowButtons.length > 0) {
				for (var crb = 0; crb < customRowButtons.length; crb++)
					delete rowData['row_custom_' + crb];
			}
			return rowData;
		},
		setRowData: function(rowId, data) {
			if (typeof (rowId) == 'object') rowId = this.generateRowId(rowId);
			this.gridTable.jqGrid('setRowData', rowId, data);
			this.updateRowIdFromData(rowId);
			this.setSelectedRowsKeys(this.getSelectedRows());
		},
		clearRowData: function(rowId) {
			var cm = this.gridTable.jqGrid('getGridParam', 'colModel');
			var first = 0;
			for (var i = 0; i < cm.length; i++) {
				var n = cm[i].name;
				if (n != 'grid_commands' && n != 'row_edit' && n != 'row_delete' && n != 'row_drag' && n.indexOf('row_custom_') != 0) {
					first = i;
					break;
				}
			}
			$('#' + rowId + ' > td:gt(' + first + ')', this.gridTable).html('');
		},
		addRow: function(data, context, position, refRowId) {
			if (position === undefined) position = 'first';
			for (var i = 0; i < this.keyColumnNames.length; i++) {
				if (data[this.keyColumnNames[i]] === undefined) {
					return false;
				}
			}

			var rowId = this.generateRowId(data);
			this.gridTable.jqGrid('addRowData', rowId, data, position, refRowId);
			this.rowContexts[rowId] = context;

			if (this.getRowAdditionCallback().length > 0) {
				var rowKeys = this.decodeRowId(rowId);
				eval(this.getRowAdditionCallback())(rowKeys, data);
			}
			this.setSelectedRowsKeys(this.getSelectedRows());
			this.bindRowEventHandlers();
		},
		deleteRow: function(rowid) {
			this.gridTable.jqGrid('delRowData', rowid);
		},
		moveToFirstPage: function() {
			this.gridTable.jqGrid('setGridParam', { 'page': 1 });
		},
		/// Zero based page number
		getPageNum: function () {
			return this.gridTable.DataTable().page.info().page;
		},
		/// fullReset: Refresh and go to First Page
		refresh: function(fullReset, invalidateCache) {
			this.isInPostLayoutDataReload = false;
			this.doColumnLayoutOnComplete = true;
			if (fullReset === undefined) fullReset = true;
			if (invalidateCache) {
				var flags = this.getRetrieveDataCallbackFlags();
				if (!flags) flags = {};
				flags['cc'] = true;
				this.setRetrieveDataCallbackFlags(flags);
			}
			this.gridTable.DataTable().draw(fullReset);
		},
		forceRefresh: function() {
			this.isInPostLayoutDataReload = false;
			this.doColumnLayoutOnComplete = true;
			this.gridTable.DataTable().draw();
		},
		_buildCriteriaAndRefresh: function(invalidateCache) {
			if (this.currentEditRow != -1) return;
			this.buildCriteriaAndReload(false);
			var bRefresh = true;
			if (this.getRefreshIcon() == $.ui.CiteLiveGrid.RefreshIconMode.Callback) {
				if (this.getCustomRefreshIconCallback() != '') {
					bRefresh = (eval(this.getCustomRefreshIconCallback())() === true);
				}
				else {
					bRefresh = false;
				}
			}
			if (bRefresh)
				this.refresh(null, invalidateCache || this.invalidateCache);
		},
		clear: function() {
			this.gridTable.DataTable().clear();
		},
		fitToParent: function () {
			// Get width of parent container
			var parent = this.element.parent();
			if (!parent.is(':visible')) return;
			var width = parent.width();
			width = width - 2; // Fudge factor to prevent horizontal scrollbars
			if (width > 0 &&
				// Only resize if new width exceeds a minimal threshold
				// Fixes IE issue with in-place resizing when mousing-over frame bars
				Math.abs(width - this.gridTable.outerWidth()) > 5) {
				this.gridTable.jqGrid('setGridWidth', Math.max(width, this.getMinWidth()));
			}
		},
		//_onInternalGridElementsHeightChanged: function() {
		//	var height = this.getHeight();
		//	if (height)
		//		this.setGridDimensions(null, height);
		//	else
		//		this._adjustFrozenColumnsLayout();
		//},
		//_adjustFrozenColumnsLayout: function () {
		//	var elements = this.gridTable.closest('.ui-jqgrid-bdiv').siblings().addBack();
		//	var frozenHeaders = elements.filter('.frozen-div');
		//	if (frozenHeaders.length > 0) {
		//		var frozenData = elements.filter('.frozen-bdiv');
		//		frozenHeaders.css('top', elements.filter('.ui-jqgrid-hdiv:not(.frozen-div)').position().top + 'px');
		//		frozenData.css('top', elements.filter('.ui-jqgrid-bdiv:not(.frozen-bdiv)').position().top + 'px');
		//	}
		//},
		setGridDimensions: function(width, height) {
			if (width != null) {
				this.gridTable.jqGrid('setGridWidth', Math.max(width, this.getMinWidth()));
				this.setWidth(width);
			}
			if (height != null) {
				var otherElements = this.element.find('.ui-jqgrid-titlebar, .ui-jqgrid-toppager, .ui-jqgrid-hdiv, .ui-userdata, .liveGridFiltersContainer').filter(':visible');
				var otherElementsHeight = 0;
				for (var i = 0, l = otherElements.length; i < l; i++) {
					otherElementsHeight += $(otherElements[i]).outerHeight(true);
				}
				this.gridTable.jqGrid('setGridHeight', height - otherElementsHeight);
				this.setHeight(height);
			}
		},

		enableGrid: function(bEnable) {
			//if (this.getNavigationMode() == $.ui.CiteLiveGrid.NavigationMode.Paging) {
			//	var toolbars = $('#' + this.pager[0].id + '_left, *[id$="_toppager_left"], #t_' + this.gridTable[0].id, this.element);
			//	toolbars.find('.ui-pg-button').toggleClass('ui-state-disabled', !bEnable);
			//}

			this.element.toggleClass('ui-state-disabled', !bEnable);
			this.setEnabled(bEnable);
		},
		toggleDeleteColumn: function(show) {
			if (this.getShowDeleteIconOnEachRow()) {
				if (show)
					this.gridTable.jqGrid('showCol', 'row_delete');
				else
					this.gridTable.jqGrid('hideCol', 'row_delete');
			}
		},
		toggleSubGridExpandCollapseColumn: function (show) {
			if (this.getEnableSubGrid()) {
				if (show)
					this.gridTable.jqGrid('showCol', 'subgrid');
				else
					this.gridTable.jqGrid('hideCol', 'subgrid');
			}
		},
		toggleEditColumn: function(show) {
			if (this.getEditIcon() != $.ui.CiteLiveGrid.EditMode.None) {
				if (show)
					$('.liveGridCommandEditIcon.' + this.getViewIconClass(), this.element).removeClass(this.getViewIconClass()).addClass(this.getEditIconClass());
				else
					$('.liveGridCommandEditIcon.' + this.getEditIconClass(), this.element).removeClass(this.getEditIconClass()).addClass(this.getViewIconClass());
			}
		},
		toggleNewButton: function(show) {
			if (this.getAddIcon() != $.ui.CiteLiveGrid.AddIconMode.None) {
				$('.fa-file', this.element).closest('.btn').toggle(show);
			}
		},
		toggleCustomButton: function(id, show) {
			var customButtons = this.getCustomButtons();
			for (var b = 0; b < customButtons.length; b++) {
				if (customButtons[b].ID == id) {
					$('.ui-pg-table.navtable', this.element).each(function() {
						var allTds = $('td', this);
						var idx = allTds.length - customButtons.length + b;
						$('td:eq(' + idx + ')', this).toggle(show);
					});
					break;
				}
			}
		},
		toggleColumn: function(colName, show) {
			if (show)
				this.gridTable.jqGrid('showCol', colName);
			else
				this.gridTable.jqGrid('hideCol', colName);
		},

		_stringIsNullOrEmpty: function(s) {
			return (s === null) || (s.length === 0);
		},
		_getColumnNameByIndex: function (index) {
			for (var key in this.columnIndices) {
				if (this.columnIndices.hasOwnProperty(key)) {
					if (this.columnIndices[key] == index) return key;
				}
			}
			return null;
		},
		////////////////////////
		//
		// VIEW MODE
		//
		////////////////////////
		setCurrentDisplayModeAndApply: function (displayMode) {
			this.setCurrentDisplayMode(displayMode);
			if (this.hasInitialized())
				this.applyViewMode();
		},
		applyViewMode: function () {
			//this.element.find('.action-collection button[data-action-type!="cancel"][data-action-type!="approve"][data-action-type!="reject"][data-action-type!="cancel-approval"]').attr('disabled', this.getCurrentDisplayMode() == jQuery.ui.CiteBaseControl.DisplayMode.View);
			if (this.getCurrentDisplayMode() == "0") { //view
				if (this.getAddIcon() != $.ui.CiteLiveGrid.AddIconMode.None)
					$('.' + this.getAddIconClass(), this.element).closest('.btn').hide();
				if (this.getShowDeleteIconOnToolbar())
					$('.' + this.getDeleteIconClass(), this.element).closest('.btn').hide();
				if (this.getShowDeleteIconOnEachRow())
					this.gridTable.DataTable().column('row_delete:name').visible(false);
				var ei = this.getEditIcon();
				if (ei != $.ui.CiteLiveGrid.EditMode.None) {
					if (ei == $.ui.CiteLiveGrid.EditMode.Inline)
						this.stopRowEdit();
					$('.liveGridCommandEditIcon.' + this.getEditIconClass(), this.element).attr('title', this.getViewButtonLabel());
					$('.liveGridCommandEditIcon.' + this.getEditIconClass(), this.element).removeClass(this.getEditIconClass()).addClass(this.getViewIconClass());
				}

				var inlineEditorControl = this.option('inlineEditor');
				if (inlineEditorControl) {
					var inlineEditor = $.ui.CiteBaseControl.find(inlineEditorControl[0].id);
					if (inlineEditor !== null) inlineEditor.setCurrentDisplayModeAndApply(this.getCurrentDisplayMode());
				}
			}
			else if (this.getCurrentDisplayMode() == "1") { //edit
				if (this.getAddIcon() != $.ui.CiteLiveGrid.AddIconMode.None)
					$('.fa-file', this.element).closest('.btn').show();
				if (this.getShowDeleteIconOnToolbar())
					$('.' + this.getDeleteIconClass(), this.element).closest('.btn').show();
				if (this.getShowDeleteIconOnEachRow())
					this.gridTable.DataTable().column('row_delete:name').visible(true);
				var ei = this.getEditIcon();
				if (ei != $.ui.CiteLiveGrid.EditMode.None) {
					for (var rowId in this.rowRights) {
						if (!this.rowRights.hasOwnProperty(rowId)) continue;
						var canEdit = (constants.OBJECT_RIGHT_EDIT in this.rowRights[rowId]) ? this.rowRights[rowId][constants.OBJECT_RIGHT_EDIT] : true;
						if (canEdit) {
							$('.liveGridCommandEditIcon.' + this.getViewIconClass(), $('#' + rowId)).attr('title', this.getEditButtonLabel());
							$('.liveGridCommandEditIcon.' + this.getViewIconClass(), $('#' + rowId)).removeClass(this.getViewIconClass()).addClass(this.getEditIconClass());
						}
					}
				}

				var inlineEditorControl = this.option('inlineEditor');
				if (inlineEditorControl) {
					var inlineEditor = $.ui.CiteBaseControl.find(inlineEditorControl[0].id);
					if (inlineEditor !== null) inlineEditor.setCurrentDisplayModeAndApply(this.getCurrentDisplayMode());
				}
			}
			//this.fitToParent();
		}
	});

	//
	//  UTILITY FUNCTIONS
	//

	$.ui.CiteLiveGrid.setInlineFormCellEditorChildClassNames = function(classes) {
		inlineFormCellEditorChildClassNames = classes;
		inlineFormCellEditorChildClassSelector = $.map(classes, function(val, i) { return '.' + val }).join(',');
	};

	$.ui.CiteLiveGrid.generateGridDataResponseFromRowArray = function(rows, request) {
		return $.ui.CiteLiveGrid.generateGridDataResponseFromRowArray(rows, null, request);
	};

	$.ui.CiteLiveGrid.generateGridDataResponseFromRowArray = function(rows, contexts, request) {
		var pageNum = 1;
		var numRows = rows.length;
		var numPages = 1;

		if (request) {
			pageNum = request['page'];
			numRows = request['rows'];
			var numPages = numRows == -1 ? 1 : Math.ceil(rows.length / numRows);
		}

		var startRow = numRows == -1 ? 0 : (pageNum - 1) * numRows;
		var endRow = numRows == -1 ? rows.length : Math.min(startRow + numRows, rows.length);
		var rowData = [];
		for (var i = startRow; i < endRow; i++) {
			rowData.push({ "Data": $.extend({}, rows[i]), "Context": contexts == null ? null : contexts[i] });
		}

		return {
			"total": numPages,
			"page": numPages == 0 ? 0 : pageNum,
			"records": rows.length,
			"rows": rowData
		};
	};

	$.ui.CiteLiveGrid.createFilteringSection = function(caption, iconClass, controlIds, isAdvanced) {
		if (isAdvanced === undefined) isAdvanced = true;
		if (!$.isArray(controlIds)) controlIds = [controlIds];
		return {
			Caption: caption,
			IconClass: iconClass,
			PutInAdvancedPanel: isAdvanced,
			Controls: controlIds.slice(0)
		};
	};

	$.ui.CiteLiveGrid.createCustomButton = function(id, label, iconClass, clickCallback, cssClass, type, width, options) {
		if (type === undefined) type = $.ui.CiteLiveGrid.ButtonType.Icon;
		if (width === undefined) width = 0;
		if (options === undefined) options = [];
		if (cssClass == undefined) cssClass = '';
		return {
			ID: id,
			IconClass: iconClass,
			Label: label,
			ClickCallback: clickCallback,
			CssClass: cssClass,
			Type: type,
			Width: width,
			Options: options.slice(0)
		};
	};

	$.ui.CiteLiveGrid.createCustomButtonOption = function(id, label) {
		return {
			ID: id,
			Label: label
		};
	};

	$.ui.CiteLiveGrid.createInlineFormEditButton = function(itemID, label, clickCallback, closeOnClick, cssClass, isVisible, options, hideInViewMode) {
		if (closeOnClick === undefined) closeOnClick = false;
		if (isVisible === undefined) isVisible = true;
		if (cssClass === undefined) cssClass = '';
		if (options === undefined) options = [];
		if (hideInViewMode === undefined) hideInViewMode = false;
		return {
			Label: label,
			ClickCallback: clickCallback,
			CloseOnClick: closeOnClick,
			ItemID: itemID,
			IsVisible: isVisible,
			CSSClass: cssClass,
			Options: options,
			HideInViewMode: hideInViewMode
		};
	};

	$.ui.CiteLiveGrid.createInlineFormEditButtonOption = function(itemID, label, isVisible) {
		if (isVisible === undefined) isVisible = true;
		return {
			Label: label,
			ItemID: itemID,
			IsVisible: isVisible
		};
	};

	//
	//  INLINE FORM EDIT BUTTONS
	//

	$.ui.CiteLiveGrid.InlineFormEditSubButton = function(descriptor) {
		this.descriptor = descriptor;
	};
	$.ui.CiteLiveGrid.InlineFormEditSubButton.prototype = {
		initialize: function() {
		},
		getLabel: function() {
			return this.descriptor['Label'];
		},
		setLabel: function(value) {
			this.descriptor['Label'] = value;
		},
		getID: function() {
			return this.descriptor['ItemID'];
		},
		setID: function(value) {
			this.descriptor['ItemID'] = value;
		},
		getIsVisible: function() {
			return this.descriptor['IsVisible'];
		}
	};


	$.ui.CiteLiveGrid.InlineFormEditButton = function(owner, descriptor) {
		this.descriptor = descriptor;
		this.owner = owner;
		this.optionDescs = [];
		this.uiObject = null;
	};
	$.ui.CiteLiveGrid.InlineFormEditButton.prototype = {
		initialize: function() {
			var options = this.descriptor['Options'];
			this.optionDescs = [];
			for (var j = 0; j < options.length; j++) {
				var item = options[j];
				var itemDesc = new $.ui.CiteLiveGrid.InlineFormEditSubButton(item);
				itemDesc.initialize();
				this.optionDescs.push(itemDesc);
			}
		},
		getOwner: function() {
			return this.owner;
		},
		getLabel: function() {
			return this.descriptor['Label'];
		},
		setLabel: function(value) {
			this.descriptor['Label'] = value;
			this.owner.setButtonLabel(this, value);
		},
		getClickCallback: function() {
			return this.descriptor['ClickCallback'];
		},
		setClickCallback: function(value) {
			this.descriptor['ClickCallback'] = value;
		},
		getCloseOnClick: function() {
			return this.descriptor['CloseOnClick'];
		},
		setCloseOnClick: function(value) {
			this.descriptor['CloseOnClick'] = value;
		},
		getID: function() {
			return this.descriptor['ItemID'];
		},
		setID: function(value) {
			this.descriptor['ItemID'] = value;
		},
		getIsVisible: function() {
			return this.descriptor['IsVisible'];
		},
		setIsVisible: function(isVisible) {
			this.descriptor['IsVisible'] = isVisible;
			if (this.uiObject)
				this.uiObject.toggle(isVisible);
		},
		getHideInViewMode: function() {
			return this.descriptor['HideInViewMode'];
		},
		setHideInViewMode: function(hideInViewMode) {
			this.descriptor['HideInViewMode'] = hideInViewMode;
		},
		getCSSClass: function() {
			return this.descriptor['CSSClass'];
		},
		setCSSClass: function(value) {
			this.descriptor['CSSClass'] = value;
		},
		getOptions: function() {
			return this.optionDescs;
		},
		setOptions: function(value) {
			this.optionDescs = value;
		},
		render: function(container) {
		},
		postRender: function(container) {
		},
		setUIObject: function(uiObject) {
			this.uiObject = uiObject;
		},
		getUIObject: function() {
			return this.uiObject;
		}
	};

	//
	//  ENUMERATIONS
	//

	//$.ui.CiteLiveGrid.FilterDisplayMode = {
	//	Icons: 0,
	//	Hoverable: 1,
	//	Suggest: 2
	//};

	///	Numbers - Page number buttons only (1.10.8)
	/// Simple - 'Previous' and 'Next' buttons only
	///	SimpleNumbers - 'Previous' and 'Next' buttons, plus page numbers
	///	Full - 'First', 'Previous', 'Next' and 'Last' buttons
	///	FullNumbers -  'First', 'Previous', 'Next' and 'Last' buttons, plus page numbers
	$.ui.CiteLiveGrid.PagingType = {
		Numbers: 'numbers',
		Simple: 'simple',
		SimpleNumbers: 'simple_numbers',
		Full: 'full',
		FullNumbers: 'full_numbers',
	};

	$.ui.CiteLiveGrid.MultiSelectMode = {
		None: 0,
		Checkbox: 1,
		Key: 2
	};

	$.ui.CiteLiveGrid.MultiSelectKey = {
		Shift: 0,
		Alt: 1,
		Control: 2
	};

	$.ui.CiteLiveGrid.EditMode = {
		None: 0,
		Callback: 1,
		Inline: 2
	};

	$.ui.CiteLiveGrid.AddIconMode = {
		None: 0,
		Callback: 1
	};

	$.ui.CiteLiveGrid.ClickMode = {
		Select: 0,
		Callback: 1
	};

	$.ui.CiteLiveGrid.RefreshIconMode = {
		None: 0,
		Simple: 1,
		Callback: 2
	}

	$.ui.CiteLiveGrid.ButtonType = {
		Icon: 0,
		Button: 1
	};

} (jQuery));
