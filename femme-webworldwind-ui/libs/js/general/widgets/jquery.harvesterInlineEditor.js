(function ($, undefined) {
	$.widget("ui.HarvesterInlineEditor", $.ui.CiteInlineEditor, {
		options: {
		},

		_refreshView: function () {
			this._super();

			var item = this.option('item');
			this.element.empty();
			this.initEditor();
			this.showData(item);
			this.setCurrentDisplayModeAndApply($.ui.CiteBaseControl.DisplayMode.View);
		},

		initEditor: function () {
			var self = this;

			var definitionTable = $('<table></table>');
			this.element.append(definitionTable);

			var endpointInfoRow = $('<tr class = "endpoint-info"></tr>'); 
			var elementInfoRow = $('<tr class = "elements-info"></tr>');
			var elementInfoRow2 = $('<tr class = "error-info2"></tr>');
			var periodInfoRow = $('<tr class = "period-info"></tr>');
			var errorInfoRow = $('<tr class = "error-info"></tr>');

			var id = 0;

			{
				var c = $('<td id=value'+id+'></td>');
				this.sveEndpoint = $('<div id="' + $.ui.CiteBaseControl.generateControlId() + '"></div>');
				c.append(this.sveEndpoint);

				var c1 = $('<td id=label'+id+'></td>');
				var lbl = $('<label class="formFieldLabel" id = cell'+id+' for="' + this.sveEndpoint[0].id + '">' + 'Endpoint' + '</label>');
				c1.append(lbl);

				endpointInfoRow.append(c1);
				endpointInfoRow.append(c);
				definitionTable.append(endpointInfoRow);

				this.sveEndpoint.CiteStringValueEditor({
					currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
					autoInitialize: true
				});
			}

			{
				id++;
				var c = $('<td id=value'+id+'></td>');
				this.sveEndpointAlias = $('<div id="' + $.ui.CiteBaseControl.generateControlId() + '"></div>');
				c.append(this.sveEndpointAlias);

				var c2 = $('<td id=label'+id+'></td>');
				var lbl = $('<label class="formFieldLabel" for="' + this.sveEndpointAlias[0].id + '">' + 'Endpoint Alias' + '</label>');
				c2.append(lbl);

				endpointInfoRow.append(c2);
				endpointInfoRow.append(c);
				definitionTable.append(endpointInfoRow);

				this.sveEndpointAlias.CiteStringValueEditor({
					currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
					autoInitialize: true
				});
			}

			{
				id++;
				var c = $('<td id=value'+id+'></td>');
				this.svePeriod = $('<div id="' + $.ui.CiteBaseControl.generateControlId() + '"></div>');
				c.append(this.svePeriod);

				var c3 = $('<td id=label'+id+'></td>');
				var lbl = $('<label class="formFieldLabel" for="' + this.svePeriod[0].id + '">' + 'Period' + '</label>');
				c3.append(lbl);

				periodInfoRow.append(c3);
				periodInfoRow.append(c);
				definitionTable.append(periodInfoRow);

				this.svePeriod.CiteStringValueEditor({
					currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
					autoInitialize: true
				});
			}

			{
				id++;
				var c = $('<td id=value'+id+' readonly ></td>');
				this.sveTotal = $('<div id="' + $.ui.CiteBaseControl.generateControlId() + '"></div>');
				c.append(this.sveTotal);

				var c4 = $('<td id=label'+id+'></td>');
				var lbl = $('<label class="formFieldLabel" for="' + this.sveTotal[0].id + '">' + 'Total Elements' + '</label>');
				c4.append(lbl);

				elementInfoRow.append(c4);
				elementInfoRow.append(c);
				definitionTable.append(elementInfoRow);

				this.sveTotal.CiteStringReadOnlyValueEditor({
					currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
					autoInitialize: true
				});
			}

			{
				id++;
				var c = $('<td id=value'+id+'></td>');
				this.sveNew = $('<div id="' + $.ui.CiteBaseControl.generateControlId() + '"></div>');
				c.append(this.sveNew);

				var c5 = $('<td id=label'+id+'></td>');
				var lbl = $('<label class="formFieldLabel" for="' + this.sveNew[0].id + '">' + 'New Elements' + '</label>');
				c5.append(lbl);

				elementInfoRow.append(c5);
				elementInfoRow.append(c);
				definitionTable.append(elementInfoRow);

				this.sveNew.CiteStringReadOnlyValueEditor({
					currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
					autoInitialize: true,
				});
			}

			{
				id++;
				var c = $('<td id=value'+id+'></td>');
				this.sveUpdate = $('<div id="' + $.ui.CiteBaseControl.generateControlId() + '"></div>');
				c.append(this.sveUpdate);

				var c6 = $('<td id=label'+id+'></td>');
				var lbl = $('<label class="formFieldLabel" for="' + this.sveUpdate[0].id + '">' + 'Updated Elements' + '</label>');
				c6.append(lbl);

				elementInfoRow2.append(c6);
				elementInfoRow2.append(c);
				definitionTable.append(elementInfoRow2);

				this.sveUpdate.CiteStringReadOnlyValueEditor({
					currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
					autoInitialize: true
				});
			}

			{
				id++;
				var c = $('<td id=value'+id+'></td>');
				this.sveFail = $('<div id="' + $.ui.CiteBaseControl.generateControlId() + '"></div>');
				c.append(this.sveFail);

				var c7 = $('<td id=label'+id+'></td>');
				var lbl = $('<label class="formFieldLabel" for="' + this.sveFail[0].id + '">' + 'Failed Elements' + '</label>');
				c7.append(lbl);

				elementInfoRow2.append(c7);
				elementInfoRow2.append(c);
				definitionTable.append(elementInfoRow2);

				this.sveFail.CiteStringReadOnlyValueEditor({
					currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
					autoInitialize: true
				});
			}

			{
				id++;
				var c = $('<td id=value'+id+'></td>');
				this.asgPeriodType = $('<div id="' + $.ui.CiteBaseControl.generateControlId() + '"></div>');
				c.append(this.asgPeriodType);

				var c8 = $('<td id=label'+id+'></td>');
				var lbl = $('<label class="formFieldLabel" for="' + this.asgPeriodType[0].id + '">' + 'Period Type' + '</label>');
				c8.append(lbl);

				periodInfoRow.append(c8);
				periodInfoRow.append(c);
				definitionTable.append(periodInfoRow);

				var suggestions = [{ 'Text': 'Seconds', 'Value': 'SECONDS' },
				                   { 'Text': 'Minutes', 'Value': 'MINUTES' },
								   { 'Text': 'Hours', 'Value': 'HOURS' },
								   { 'Text': 'Days', 'Value': 'DAYS' }];

				this.asgPeriodType.CiteAutoSuggest({
					currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
					suggestionMode: $.ui.CiteAutoSuggest.SuggestionMode.Static,
					uiMode: $.ui.CiteAutoSuggest.UIMode.DropDown,
					selectionNameProperty: 'Text',
					selectionValueProperty: 'Value',
					staticSuggestions: suggestions,
					autoInitialize: true
				});
			}

			{
				id++;
				var c = $('<td id=value'+id+'></td>');
				this.sveErrMsg = $('<div id="' + $.ui.CiteBaseControl.generateControlId() + '"></div>');
				c.append(this.sveErrMsg);

				var c9 = $('<td id=label'+id+'></td>');
				var lbl = $('<label class="formFieldLabel" for="' + this.sveErrMsg[0].id + '">' + 'Error Message' + '</label>');
				c9.append(lbl);

				errorInfoRow.append(c9);
				errorInfoRow.append(c);
				definitionTable.append(errorInfoRow);

				this.sveErrMsg.CiteStringReadOnlyValueEditor({
					currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
					autoInitialize: true,
				});
			}

			{
				id++;
				var c = $('<td id=value'+id+'></td>');
				this.svePreviousHarvests = $('<div id="' + $.ui.CiteBaseControl.generateControlId() + '"></div>');
				c.append(this.svePreviousHarvests);

				var c10 = $('<td id=label'+id+'></td>');
				var lbl = $('<label class="formFieldLabel" for="' + this.svePreviousHarvests[0].id + '">' + 'Previous Harvests' + '</label>');
				c10.append(lbl);

				errorInfoRow.append(c10);
				errorInfoRow.append(c);
				definitionTable.append(errorInfoRow);

				this.svePreviousHarvests.CiteStringReadOnlyValueEditor({
					currentDisplayMode: $.ui.CiteBaseControl.DisplayMode.Edit,
					autoInitialize: true,
				});
			}
			
			{
				id++;
				var r = $('<tr></tr>');
				var c11 = $('<td id=label'+id+'></td>');
				this.saveButton = $('<button id="save" name="save" class="btn btn-default" value="1">Save</button>');
				c11.append(this.saveButton);

				var c12 = $('<td id=label'+id+'></td>');
				this.cancelButton = $('<button id="cancel" name="cancel" class="btn btn-default" value="2">Cancel</button>');
				c12.append(this.cancelButton);

				r.append(c11);
				r.append(c12);
				definitionTable.append(r);

				this.saveButton.on('click', function () {
					eval(self.options.saveCallback)();
				})

				this.cancelButton.on('click', function () {
					eval(self.options.cancelCallback)();
				})
			}
		},
		
		periodType: function (value) {
			var control = this.asgPeriodType;
			if (arguments.length === 0) {
				if (control.length > 0) { value = control.CiteAutoSuggest('getSingleValueOrDefault'); }
				return value;
			} else {
				if (value === undefined || value === null || value === '')
					control.CiteAutoSuggest('clearSelection');
				else
					control.CiteAutoSuggest('selectItem', value);
				return this;
			}
		},

		endpoint: function (value) {
			var control = this.sveEndpoint;
			if (arguments.length === 0) {
				return (control.length > 0) ? control.CiteStringValueEditor('getValue') : undefined;
			} else {
				control.CiteStringValueEditor('setValue', value);
				return this;
			}
		},

		endpointAlias: function (value) {
			var control = this.sveEndpointAlias;
			if (arguments.length === 0) {
				return (control.length > 0) ? control.CiteStringValueEditor('getValue') : undefined;
			} else {
				control.CiteStringValueEditor('setValue', value);
				return this;
			}
		},

		period: function (value) {
			var control = this.svePeriod;
			if (arguments.length === 0) {
				return (control.length > 0) ? control.CiteStringValueEditor('getValue') : undefined;
			} else {
				control.CiteStringValueEditor('setValue', value);
				return this;
			}
		},

		totalElements: function (value) {
			var control = this.sveTotal;
			if (arguments.length === 0) {
				return (control.length > 0) ? control.CiteStringReadOnlyValueEditor('getValue') : undefined;
			} else {
				control.CiteStringReadOnlyValueEditor('setValue', value);
				return this;
			}
		},

		newElements: function (value) {
			var control = this.sveNew;
			if (arguments.length === 0) {
				return (control.length > 0) ? control.CiteStringReadOnlyValueEditor('getValue') : undefined;
			} else {
				control.CiteStringReadOnlyValueEditor('setValue', value);
				return this;
			}
		},

		updatedElements: function (value) {
			var control = this.sveUpdate;
			if (arguments.length === 0) {
				return (control.length > 0) ? control.CiteStringReadOnlyValueEditor('getValue') : undefined;
			} else {
				control.CiteStringReadOnlyValueEditor('setValue', value);
				return this;
			}
		},

		failedElements: function (value) {
			var control = this.sveFail;
			if (arguments.length === 0) {
				return (control.length > 0) ? control.CiteStringReadOnlyValueEditor('getValue') : undefined;
			} else {
				control.CiteStringReadOnlyValueEditor('setValue', value);
				return this;
			}
		},

		errorMessage: function (value) {
			var control = this.sveErrMsg;
			if (arguments.length === 0) {
				return (control.length > 0) ? control.CiteStringReadOnlyValueEditor('getValue') : undefined;
			} else {
				control.CiteStringReadOnlyValueEditor('setValue', value);
				return this;
			}
		},

		previousHarvests: function (value) {
			var control = this.svePreviousHarvests;
			if (arguments.length === 0) {
				return (control.length > 0) ? control.CiteStringReadOnlyValueEditor('getValue') : undefined;
			} else {
				control.CiteStringReadOnlyValueEditor('setValue', value);
				return this;
			}
		},

		getData: function () {
			var result = {};

			result.endpoint = this.sveEndpoint.CiteStringValueEditor('getValue');
			result.endpointAlias = this.sveEndpointAlias.CiteStringValueEditor('getValue');
			result.period = this.svePeriod.CiteStringValueEditor('getValue');

			var values = this.asgPeriodType.CiteAutoSuggest('getSelectedValues');
			result.periodType = (values.length == 0) ? '' : values[0];

			result.totalElements = this.sveTotal.CiteStringReadOnlyValueEditor('getValue');
			result.newElements = this.sveNew.CiteStringReadOnlyValueEditor('getValue');
			result.updatedElements = this.sveUpdate.CiteStringReadOnlyValueEditor('getValue');
			result.failedElements = this.sveFail.CiteStringReadOnlyValueEditor('getValue');
			result.errorMessage = this.sveErrMsg.CiteStringReadOnlyValueEditor('getValue');
			result.previousHarvests = this.svePreviousHarvests.CiteStringReadOnlyValueEditor('getValue');

			return result;
		},

		showData: function (item) {
			this.endpoint(item.endpoint);
			this.endpointAlias(item.endpointAlias);
			this.period(item.period);
			this.periodType(item.periodType);
			this.totalElements(item.totalElements);
			this.newElements(item.newElements);
			this.updatedElements(item.updatedElements);
			this.failedElements(item.failedElements);
			this.errorMessage(item.errorMessage);
			this.previousHarvests(item.previousHarvests);
		},

		////////////////////////
		//
		// VIEW MODE
		//
		////////////////////////
		
		setCurrentDisplayModeAndApply: function (displayMode) {
			this.setCurrentDisplayMode(displayMode);
			// if (this.hasInitialized())
			// 	this.applyViewMode();
		},

		applyViewMode: function () {
			if (this.asgPeriodType) this.asgPeriodType.CiteAutoSuggest('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
			if (this.sveEndpoint) this.sveEndpoint.CiteStringValueEditor('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
			if (this.sveEndpointAlias) this.sveEndpointAlias.CiteStringValueEditor('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
			if (this.svePeriod) this.svePeriod.CiteStringValueEditor('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());

			if (this.sveTotal) this.sveTotal.CiteStringValueEditor('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
			if (this.sveNew) this.sveNew.CiteStringValueEditor('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
			if (this.sveUpdate) this.sveUpdate.CiteStringValueEditor('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
			if (this.sveFail) this.sveFail.CiteStringValueEditor('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
			if (this.sveErrMsg) this.sveErrMsg.CiteStringValueEditor('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
			if (this.svePreviousHarvests) this.svePreviousHarvests('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
		}
	})
}(jQuery));