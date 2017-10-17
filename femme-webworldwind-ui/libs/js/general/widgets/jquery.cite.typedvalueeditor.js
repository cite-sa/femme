/*
 * jQuery CITE AutoSuggest
 *
 * Depends on:
 *	jquery.ui.core.js 1.9
 *	jquery.ui.widget.js 1.9
 *  jquery.cite.basecontrol.js 1.0
 */

(function ($, undefined) {
	$.widget("ui.CiteTypedValueEditor", $.ui.CiteBaseControl, {
		version: "1.0.0",
		defaultElement: "<div>",
		options: {
			valueType: 0, // String
			isSingleValue: true,
			step: 1,
			minValue: null,
			maxValue: null,
			suggestions: [],
			selectorValueProperty: 'Value',
			selectorNameProperty: 'Text',
			columns: [],
			timeSpanOptions: null
		},

		doInitialize: function () {
			if (this._super("doInitialize") === false) return false;

			this.element.addClass('TypedValueEditor');

			this._createActiveEditor();

			this.refresh();
		},

		_createActiveEditor: function() {
			var valueType = this.option('valueType');
			var isSingleValue = this.option('isSingleValue');

			if (isSingleValue) {
				if (valueType == $.ui.CiteTypedValueEditor.ValueType.String && !this.stringBoxEditor)
					this._createStringBox();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.Text && !this.textBoxEditor)
					this._createTextBox();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.Integer && !this.integerEditor)
					this._createIntegerEditor();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.Decimal && !this.decimalEditor)
					this._createDecimalEditor();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.Boolean && !this.booleanEditor)
					this._createBooleanEditor();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.Date && !this.datePicker)
					this._createDatePicker();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.Time && !this.timePicker)
					this._createTimePicker();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.DateTime && !this.dateTimePicker)
					this._createDateTimePicker();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.DateFrame && !this.dateFramePicker)
					this._createDateFramePicker();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.TimeFrame && !this.timeFramePicker)
					this._createTimeFramePicker();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.DateTimeFrame && !this.dateTimeFramePicker)
					this._createDateTimeFramePicker();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.Array && !this.arrayEditor)
					this._createArrayEditor();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.TimeSpan && !this.timeSpanPicker)
					this._createTimeSpanPicker();
				else if (valueType == $.ui.CiteTypedValueEditor.ValueType.ReadOnlyString && !this.readOnlyStringBoxEditor)
					this._createReadOnlyStringBox();
			}
			else if (!this.selectorEditor)
				this._createSelectorEditor();
		},

		refresh: function () {
			this._createActiveEditor();

			if (this.option('isSingleValue')) {
				if (this.selectorEditor) this.selectorEditor.hide();
				var type = this.option("valueType");
				if (this.stringBoxEditor) this.stringBoxEditor.toggle(type == $.ui.CiteTypedValueEditor.ValueType.String);
				if (this.readOnlyStringBoxEditor) this.readOnlyStringBoxEditor.toggle(type == $.ui.CiteTypedValueEditor.ValueType.ReadOnlyString);
				if (this.textBoxEditor) this.textBoxEditor.toggle(type == $.ui.CiteTypedValueEditor.ValueType.Text);
				if (this.integerEditor) this.integerEditor.toggle(type == $.ui.CiteTypedValueEditor.ValueType.Integer);
				if (this.decimalEditor) this.decimalEditor.toggle(type == $.ui.CiteTypedValueEditor.ValueType.Decimal);
				if (this.dateTimePicker) this.dateTimePicker.toggle(type == $.ui.CiteTypedValueEditor.ValueType.DateTime);
				if (this.datePicker) this.datePicker.toggle(type == $.ui.CiteTypedValueEditor.ValueType.Date);
				if (this.timePicker) this.timePicker.toggle(type == $.ui.CiteTypedValueEditor.ValueType.Time);
				if (this.booleanEditor) this.booleanEditor.toggle(type == $.ui.CiteTypedValueEditor.ValueType.Boolean);
				if (this.dateFramePicker) this.dateFramePicker.toggle(type == $.ui.CiteTypedValueEditor.ValueType.DateFrame);
				if (this.timeFramePicker) this.timeFramePicker.toggle(type == $.ui.CiteTypedValueEditor.ValueType.TimeFrame);
				if (this.dateTimeFramePicker) this.dateTimeFramePicker.toggle(type == $.ui.CiteTypedValueEditor.ValueType.DateTimeFrame);
				if (this.timeSpanPicker) this.timeSpanPicker.toggle(type == $.ui.CiteTypedValueEditor.ValueType.TimeSpan);
				if (this.arrayEditor) {
					var isArray = (type == $.ui.CiteTypedValueEditor.ValueType.Array);
					this.arrayEditor.toggle(isArray);
					if (isArray) {
						this.arrayEditor.CiteArrayValueEditor('option', 'columns', this.option('columns'));
						this.arrayEditor.CiteArrayValueEditor('refresh');
					}
				}
				if (this.selectorEditor) this.selectorEditor.toggle(false);
			} else {
				if (this.stringBoxEditor) this.stringBoxEditor.hide();
				if (this.readOnlyStringBoxEditor) this.readOnlyStringBoxEditor.hide();
				if (this.textBoxEditor) this.textBoxEditor.hide();
				if (this.integerEditor) this.integerEditor.hide();
				if (this.decimalEditor) this.decimalEditor.hide();
				if (this.dateTimePicker) this.dateTimePicker.hide();
				if (this.datePicker) this.datePicker.hide();
				if (this.timePicker) this.timePicker.hide();
				if (this.booleanEditor) this.booleanEditor.hide();
				if (this.dateFramePicker) this.dateFramePicker.hide();
				if (this.timeFramePicker) this.timeFramePicker.hide();
				if (this.dateTimeFramePicker) this.dateTimeFramePicker.hide();
				if (this.timeSpanPicker) this.timeSpanPicker.hide();
				if (this.arrayEditor) this.arrayEditor.hide();
				var suggestions = this.option('suggestions');
				this.selectorEditor.show();
				this.selectorEditor.CiteAutoSuggest('setStaticSuggestions', suggestions);
			}

			this.applyViewMode();
		},

		_createStringBox: function () {
			this.stringBoxEditor = $('<input type="text" autocomplete="off" class="form-control text-box single-line" />');
			this.element.append(this.stringBoxEditor);

			var self = this;
			this.stringBoxEditor.bind('change', function (e) {
				e.stopPropagation();
				self.fireModificationEvent();
			});
		},

		_createReadOnlyStringBox : function(){
			this.readOnlyStringBoxEditor = $('<input type="text" autocomplete="off" class="form-control text-box single-line" readonly="readonly" />');
			this.element.append(this.readOnlyStringBoxEditor);

			var self = this;
			this.readOnlyStringBoxEditor.bind('change', function (e) {
				e.stopPropagation();
				self.fireModificationEvent();
			});
		},

		_createTextBox: function () {
			this.textBoxEditor = $('<textarea class="form-control" rows="5" wrap="off"></textarea>');
			this.element.append(this.textBoxEditor);

			var self = this;
			this.textBoxEditor.bind('change', function (e) {
				e.stopPropagation();
				self.fireModificationEvent();
			});
		},

		_createIntegerEditor: function () {
			this.integerEditor = $('<div></div>');
			this.element.append(this.integerEditor);

			this.integerEditor.CiteSpinner({
				minValue: this.option('minValue'),
				maxValue: this.option('maxValue'),
				stepSize: this.option('step'),
				decimalPlaces: 0,
				mode: $.ui.CiteSpinner.SpinnerMode.Integer
			});

			//CiteSpinner doesn't throw any event 
			//var self = this;
			//this.integerEditor.bind('datachanged', function (e) {
			//	e.stopPropagation();
			//	self.fireModificationEvent();
			//});
		},

		_createDecimalEditor: function () {
			this.decimalEditor = $('<div></div>');
			this.element.append(this.decimalEditor);

			this.decimalEditor.CiteSpinner({
				minValue: this.option('minValue'),
				maxValue: this.option('maxValue'),
				stepSize: this.option('step'),
				decimalPlaces: this.option('decimalPlaces'),
				mode: $.ui.CiteSpinner.SpinnerMode.Decimal
			});

			//CiteSpinner doesn't throw any event
			//var self = this;
			//this.decimalEditor.bind('datachanged', function (e) {
			//	e.stopPropagation();
			//	self.fireModificationEvent();
			//});
		},

		_createBooleanEditor: function () {
			this.booleanEditor = $('<div></div>');
			this.element.append(this.booleanEditor);

			this.booleanEditor.CiteAutoSuggest({
				suggestionMode: $.ui.CiteAutoSuggest.SuggestionMode.Static,
				uiMode: $.ui.CiteAutoSuggest.UIMode.DropDown,
				selectionValueProperty: 'Value',
				selectionNameProperty: 'Text',
				staticSuggestions: [{
					'Text': 'No',
					'Value': '0'
				}, {
					'Text': 'Yes',
					'Value': '1'
				}]
			});
			this.booleanEditor.CiteAutoSuggest('selectItem', '0');

			var self = this;
			this.booleanEditor.bind('selectionchanged', function (e) {
				e.stopPropagation();
				self.fireModificationEvent();
			});
		},

		_createDateTimePicker: function () {
			var self = this;

			this.dateTimePicker = $('<div />');
			this.element.append(this.dateTimePicker);

			this.dateTimePicker.CiteDateTimePicker({
				selectionMode: $.ui.CiteDateTimePicker.SelectionMode.DateTime
			});

			//CiteDateTimePicker doesn't throw any specific event
			//var self = this;
			//this.dateTimePicker.bind('datechanged', function (e) {
			//	e.stopPropagation();
			//	self.fireModificationEvent();
			//});
		},

		_createDatePicker: function () {
			var self = this;

			this.datePicker = $('<div />');
			this.element.append(this.datePicker);

			this.datePicker.CiteDateTimePicker({
				selectionMode: $.ui.CiteDateTimePicker.SelectionMode.Date
			});

			//CiteDateTimePicker doesn't throw any specific event
			//var self = this;
			//this.datePicker.bind('datechanged', function (e) {
			//	e.stopPropagation();
			//	self.fireModificationEvent();
			//});
		},

		_createTimePicker: function () {
			var self = this;

			this.timePicker = $('<div />');
			this.element.append(this.timePicker);

			this.timePicker.CiteDateTimePicker({
				selectionMode: $.ui.CiteDateTimePicker.SelectionMode.Time
			});

			//CiteDateTimePicker doesn't throw any specific event
			//var self = this;
			//this.timePicker.bind('datechanged', function (e) {
			//	e.stopPropagation();
			//	self.fireModificationEvent();
			//});
		},

		_createDateTimeFramePicker: function () {
			var self = this;

			this.dateTimeFramePicker = $('<div><span class="from"></span>-<span class="to"></span></div>');
			this.element.append(this.dateTimeFramePicker);

			this.dateTimeFramePicker.find(".from").CiteDateTimePicker({
				selectionMode: $.ui.CiteDateTimePicker.SelectionMode.DateTime
			});

			this.dateTimeFramePicker.find(".to").CiteDateTimePicker({
				selectionMode: $.ui.CiteDateTimePicker.SelectionMode.DateTime
			});

			//CiteDateTimeFramePicker doesn't throw any specific event
			//var self = this;
			//this.dateTimeFramePicker.bind('datechanged', function (e) {
			//	e.stopPropagation();
			//	self.fireModificationEvent();
			//});
		},

		_createArrayEditor: function () {
			var self = this;

			this.arrayEditor = $('<div></div>');
			this.element.append(this.arrayEditor);

			this.arrayEditor.CiteArrayValueEditor({
				columns: this.option('columns')
			});

			//CiteArrayValueEditor doesn't throw any specific event
			//var self = this;
			//this.arrayEditor.bind('datachanged', function (e) {
			//	e.stopPropagation();
			//	self.fireModificationEvent();
			//});
		},

		_createDateFramePicker: function () {
			var self = this;

			this.dateFramePicker = $('<div><span class="from"></span>-<span class="to"></span></div>');
			this.element.append(this.dateFramePicker);

			this.dateFramePicker.find(".from").CiteDateTimePicker({
				selectionMode: $.ui.CiteDateTimePicker.SelectionMode.Date
			});

			this.dateFramePicker.find(".to").CiteDateTimePicker({
				selectionMode: $.ui.CiteDateTimePicker.SelectionMode.Date
			});

			//CiteDateFramePicker doesn't throw any specific event
			//var self = this;
			//this.dateFramePicker.bind('datechanged', function (e) {
			//	e.stopPropagation();
			//	self.fireModificationEvent();
			//});
		},

		_createTimeFramePicker: function () {
			var self = this;

			this.timeFramePicker = $('<div><span class="from"></span>-<span class="to"></span></div>');
			this.element.append(this.timeFramePicker);

			this.timeFramePicker.find(".from").CiteDateTimePicker({
				selectionMode: $.ui.CiteDateTimePicker.SelectionMode.Time
			});

			this.timeFramePicker.find(".to").CiteDateTimePicker({
				selectionMode: $.ui.CiteDateTimePicker.SelectionMode.Time
			});

			//CiteTimeFramePicker doesn't throw any specific event
			//var self = this;
			//this.timeFramePicker.bind('datechanged', function (e) {
			//	e.stopPropagation();
			//	self.fireModificationEvent();
			//});
		},

		_createTimeSpanPicker: function () {
			var self = this;

			this.timeSpanPicker = $('<div />');
			this.element.append(this.timeSpanPicker);

			var options = this.option('timeSpanOptions') ? this.option('timeSpanOptions') : { };

			this.timeSpanPicker.CiteTimeSpanPicker(options);

			//CiteDateTimePicker doesn't throw any specific event
			//var self = this;
			//this.timePicker.bind('datechanged', function (e) {
			//	e.stopPropagation();
			//	self.fireModificationEvent();
			//});
		},

		_createSelectorEditor: function () {
			this.selectorEditor = $('<div></div>');
			this.element.append(this.selectorEditor);

			this.selectorEditor.CiteAutoSuggest({
				suggestionMode: $.ui.CiteAutoSuggest.SuggestionMode.Static,
				uiMode: $.ui.CiteAutoSuggest.UIMode.DropDown,
				selectionValueProperty: this.option('selectorValueProperty'),
				selectionNameProperty: this.option('selectorNameProperty'),
				staticSuggestions: []
			});

			var self = this;
			this.selectorEditor.bind('selectionchanged', function (e) {
				e.stopPropagation();
				self.fireModificationEvent();
			});
		},

		getValue: function () {
			var isSingleValue = this.option("isSingleValue");
			if (!isSingleValue) {
				return this.selectorEditor.CiteAutoSuggest('getSingleValueOrDefault');
			} else {
				var type = this.option("valueType");
				if (type == $.ui.CiteTypedValueEditor.ValueType.String) {
					return this.stringBoxEditor.val();
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.ReadOnlyString) {
					return this.readOnlyStringBoxEditor.val(); 
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Text) {
					return this.textBoxEditor.val();
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Integer) {
					return this.integerEditor.CiteSpinner('getValue');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Decimal) {
					return this.decimalEditor.CiteSpinner('getValue');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Boolean) {
					return (this.booleanEditor.CiteAutoSuggest('getSelectedValues')[0] == '1');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.DateTime) {
					return this.dateTimePicker.CiteDateTimePicker('getSelectedValue');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Date) {
					return this.datePicker.CiteDateTimePicker('getSelectedValue');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Time) {
					return this.timePicker.CiteDateTimePicker('getSelectedValue');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.TimeSpan) {
					return this.timeSpanPicker.CiteTimeSpanPicker('getValue');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.DateTimeFrame) {
					return {
						'From': this.dateTimeFramePicker.find('.from').CiteDateTimePicker('getSelectedValue'),
						'To': this.dateTimeFramePicker.find('.to').CiteDateTimePicker('getSelectedValue')
					};
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.DateFrame) {
					return {
						'From': this.dateFramePicker.find('.from').CiteDateTimePicker('getSelectedValue'),
						'To': this.dateFramePicker.find('.to').CiteDateTimePicker('getSelectedValue')
					};
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.TimeFrame) {
					return {
						'From': this.timeFramePicker.find('.from').CiteDateTimePicker('getSelectedValue'),
						'To': this.timeFramePicker.find('.to').CiteDateTimePicker('getSelectedValue')
					};
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Array) {
					return this.arrayEditor.CiteArrayValueEditor('getData');
				} 
			}
			return '';
		},

		setValue: function (value) {
			var isSingleValue = this.option("isSingleValue");
			if (!isSingleValue) {
				this.selectorEditor.CiteAutoSuggest('selectItem', value);
			} else {
				var type = this.option("valueType");
				if (type == $.ui.CiteTypedValueEditor.ValueType.String) {
					this.stringBoxEditor.val(value);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.ReadOnlyString) {
					this.readOnlyStringBoxEditor.val(value);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Text) {
					this.textBoxEditor.val(value);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Integer) {
					if (value == null || value === '') value = undefined;
					this.integerEditor.CiteSpinner('setValue', value);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Decimal) {
					if (value == null || value === '') value = undefined;
					this.decimalEditor.CiteSpinner('setValue', value);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Boolean) {
					this.booleanEditor.CiteAutoSuggest('selectItem', value ? '1' : '0');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.DateTime) {
					this.dateTimePicker.CiteDateTimePicker('setSelectedValue', value);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Date) {
					this.datePicker.CiteDateTimePicker('setSelectedValue', value);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Time) {
					this.timePicker.CiteDateTimePicker('setSelectedValue', value);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.TimeSpan) {
					this.timeSpanPicker.CiteTimeSpanPicker('setValue', value);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.DateTimeFrame) {
					this.dateTimeFramePicker.find('.from').CiteDateTimePicker('setSelectedValue', (value == null || value == '') ? '' : value.From);
					this.dateTimeFramePicker.find('.to').CiteDateTimePicker('setSelectedValue', (value == null || value == '') ? '' : value.To);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.DateFrame) {
					this.dateFramePicker.find('.from').CiteDateTimePicker('setSelectedValue', (value == null || value == '') ? '' : value.From);
					this.dateFramePicker.find('.to').CiteDateTimePicker('setSelectedValue', (value == null || value == '') ? '' : value.To);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.TimeFrame) {
					this.timeFramePicker.find('.from').CiteDateTimePicker('setSelectedValue', (value == null || value == '') ? '' : value.From);
					this.timeFramePicker.find('.to').CiteDateTimePicker('setSelectedValue', (value == null || value == '') ? '' : value.To);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Array) {
					this.arrayEditor.CiteArrayValueEditor('setData', value);
				}
			}
		},

		clear: function () {
			var isSingleValue = this.option("isSingleValue");
			if (!isSingleValue) {
				this.selectorEditor.CiteAutoSuggest('clearSelection');
			} else {
				var type = this.option("valueType");
				if (type == $.ui.CiteTypedValueEditor.ValueType.String) {
					this.stringBoxEditor.val('');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.ReadOnlyString) {
					this.readOnlyStringBoxEditor.val('');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Text) {
					this.textBoxEditor.val('');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Integer) {
					this.integerEditor.CiteSpinner('setValue', undefined);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Decimal) {
					this.decimalEditor.CiteSpinner('setValue', undefined);
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Boolean) {
					this.booleanEditor.CiteAutoSuggest('selectItem', '0');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.DateTime) {
					this.dateTimePicker.CiteDateTimePicker('setSelectedValue', '');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Date) {
					this.datePicker.CiteDateTimePicker('setSelectedValue', '');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Time) {
					this.timePicker.CiteDateTimePicker('setSelectedValue', '');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.TimeSpan) {
					this.timeSpanPicker.CiteTimeSpanPicker('setValue', '');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.DateTimeFrame) {
					this.dateTimeFramePicker.find('.from').CiteDateTimePicker('setSelectedValue', '');
					this.dateTimeFramePicker.find('.to').CiteDateTimePicker('setSelectedValue', '');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.DateFrame) {
					this.dateFramePicker.find('.from').CiteDateTimePicker('setSelectedValue', '');
					this.dateFramePicker.find('.to').CiteDateTimePicker('setSelectedValue', '');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.TimeFrame) {
					this.timeFramePicker.find('.from').CiteDateTimePicker('setSelectedValue', '');
					this.timeFramePicker.find('.to').CiteDateTimePicker('setSelectedValue', '');
				} else if (type == $.ui.CiteTypedValueEditor.ValueType.Array) {
					this.arrayEditor.CiteArrayValueEditor('clear');
				}
			}
		},

		fireModificationEvent: function () {
			var event = jQuery.Event("datachanged");
			this.element.trigger(event);
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
			var isSingleValue = this.option("isSingleValue");
			if (!isSingleValue) {
				this.selectorEditor.CiteAutoSuggest('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
			} else {
				var v = this.option("valueType");
				if (v == $.ui.CiteTypedValueEditor.ValueType.String) {
					this.stringBoxEditor.attr('disabled', this.getCurrentDisplayMode() == jQuery.ui.CiteBaseControl.DisplayMode.View);
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.ReadOnlyString) {
					this.readOnlyStringBoxEditor.attr('disabled', this.getCurrentDisplayMode() == jQuery.ui.CiteBaseControl.DisplayMode.View);
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.Text) {
					this.textBoxEditor.attr('disabled', this.getCurrentDisplayMode() == jQuery.ui.CiteBaseControl.DisplayMode.View);
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.Integer) {
					this.integerEditor.CiteSpinner('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.Decimal) {
					this.decimalEditor.CiteSpinner('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.Boolean) {
					this.booleanEditor.CiteAutoSuggest('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.DateTime) {
					this.dateTimePicker.CiteDateTimePicker('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.Date) {
					this.datePicker.CiteDateTimePicker('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.Time) {
					this.timePicker.CiteDateTimePicker('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.TimeSpan) {
					this.timeSpanPicker.CiteTimeSpanPicker('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.DateTimeFrame) {
					this.dateTimeFramePicker.find('.from').CiteDateTimePicker('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
					this.dateTimeFramePicker.find('.to').CiteDateTimePicker('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.DateFrame) {
					this.dateFramePicker.find('.from').CiteDateTimePicker('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
					this.dateFramePicker.find('.to').CiteDateTimePicker('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.TimeFrame) {
					this.timeFramePicker.find('.from').CiteDateTimePicker('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
					this.timeFramePicker.find('.to').CiteDateTimePicker('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
				} else if (v == $.ui.CiteTypedValueEditor.ValueType.Array) {
					this.arrayEditor.CiteArrayValueEditor('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
				}
			}
		}
	});

	$.widget("ui.CiteStringValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.String);
			return this._super("doInitialize");
		},
	});

	$.widget("ui.CiteStringReadOnlyValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.ReadOnlyString);
			return this._super("doInitialize");
		},
	});

	$.widget("ui.CiteBooleanValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.Boolean);
			return this._super("doInitialize");
		}
	});

	$.widget("ui.CiteTextValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.Text);
			return this._super("doInitialize");
		}
	});

	$.widget("ui.CiteIntegerValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.Integer);
			return this._super("doInitialize");
		}
	});

	$.widget("ui.CiteDecimalValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.Decimal);
			return this._super("doInitialize");
		}
	});

	$.widget("ui.CiteDateValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.Date);
			return this._super("doInitialize");
		}
	});

	$.widget("ui.CiteTimeValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.Time);
			return this._super("doInitialize");
		}
	});

	$.widget("ui.CiteTimeSpanValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.TimeSpan);
			return this._super("doInitialize");
		}
	});

	$.widget("ui.CiteDateFrameValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.DateFrame);
			return this._super("doInitialize");
		},
		getValueHash: function () {
			return this.dateFramePicker.find('.from').CiteDateTimePicker('getValueHash') + "_" + this.dateFramePicker.find('.to').CiteDateTimePicker('getValueHash');
		}
	});

	$.widget("ui.CiteTimeFrameValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.TimeFrame);
			return this._super("doInitialize");
		},
		getValueHash: function () {
			return this.timeFramePicker.find('.from').CiteDateTimePicker('getValueHash') + "_" + this.timeFramePicker.find('.to').CiteDateTimePicker('getValueHash');
		}
	});

	$.widget("ui.CiteDateTimeFrameValueEditor", $.ui.CiteTypedValueEditor, {
		doInitialize: function () {
			this.option('valueType', $.ui.CiteTypedValueEditor.ValueType.DateTimeFrame);
			return this._super("doInitialize");
		},
		getValueHash: function () {
			return this.dateTimeFramePicker.find('.from').CiteDateTimePicker('getValueHash') + "_" + this.dateTimeFramePicker.find('.to').CiteDateTimePicker('getValueHash');
		}
	});

	$.widget("ui.CiteArrayValueEditor", $.ui.CiteBaseControl, {

		options: {
			columns: []
		},

		doInitialize: function () {
			if (this._super("doInitialize") === false) return false;

			this.descriptors = [];
			this.rowID = 0;

			this.container = $('<div></div>');
			this.element.append(this.container);
		},

		refresh: function () {
			var self = this;
			this.container.empty();
			this.descriptors = [];

			//Headers
			{
				var columns = this.option('columns');

				var size = Math.floor(12 / (columns.length + 1));
				if (size < 1) size = 1;

				var row = $('<div class="row"></div>');
				for (var i = 0; i < columns.length; i++) {
					var column = columns[i];

					var cell = $('<div class="col-md-' + size + '"></div>');

					row.append(cell);

					var label = $('<span></span>');

					cell.append(label);

					label.text(column.Label);
				}
				this.container.append(row);
			}

			this.datacontainer = $('<div></div>');
			this.container.append(this.datacontainer);

			//Add
			{
				var row = $('<div class="row"></div>');

				var cell = $('<div class="col-md-1"></div>');

				row.append(cell);

				var add = $('<button class="btn btn-sm btn-primary addValue"><i class="fa fa-plus text-white"></i></button>');

				cell.append(add);

				this.container.append(row);

				add.bind('click', function (e) {
					e.stopPropagation();
					e.preventDefault();
					self.addRow([]);
				});
			}
		},

		getData: function () {
			var data = [];
			for (var i = 0; i < this.descriptors.length; i++) {
				var rowData = this.getRowData(this.descriptors[i]);
				data.push(rowData);
			}
			return data;
		},

		getRowData: function (descriptor) {
			var editors = descriptor.Editors;
			var data = [];
			for (var i = 0; i < editors.length; i++) {
				data.push(editors[i].CiteTypedValueEditor('getValue'));
			}
			return data;
		},

		setData: function (data) {
			this.clear();

			for (var i = 0; i < data.length; i++) {
				this.addRow(data[i]);
			}
		},

		addRow: function (data) {
			var self = this;
			var rowID = this.rowID++;

			var row = $('<div class="row"></div>');

			this.datacontainer.append(row);

			var columns = this.option('columns');

			var editors = [];

			var size = Math.floor(12 / (columns.length + 1));
			if (size < 1) size = 1;

			for (var i = 0; i < columns.length; i++) {
				var column = columns[i];

				var cell = $('<div class="col-md-' + size + '"></div>');

				row.append(cell);

				var editor = $('<span></span>');

				cell.append(editor);

				editor.CiteTypedValueEditor(column);
				if (data[i]) editor.CiteTypedValueEditor('setValue', data[i]);
				editor.CiteTypedValueEditor('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());

				editors.push(editor);
			}
			var deleteCell = $('<div class="col-md-1"></div>');

			var deleteButton = $('<a href="#" title="Remove"><i class="fa fa-lg fa-close"></i></a>');

			deleteCell.append(deleteButton);

			row.append(deleteCell);

			var descriptor = {
				'RowID': rowID,
				'Editors': editors,
				'Row': row
			};
			this.descriptors.push(descriptor);

			//Binding
			deleteButton.bind('click', function () { self.removeRow(rowID); });

			return descriptor;
		},

		removeRow: function (id) {
			var idx = 0;
			var descriptor = null;
			for (var i = 0; i < this.descriptors.length; i++) {
				descriptor = this.descriptors[i];
				if (descriptor.RowID == id) {
					idx = i;
					break;
				}
			}
			descriptor.Row.remove();
			this.descriptors.splice(idx, 1);
		},

		getDescriptor: function (id) {
			var descriptor = null;
			for (var i = 0; i < this.descriptors.length; i++) {
				descriptor = this.descriptors[i];
				if (descriptor.RowID == id) {
					break;
				}
			}
			return descriptor;
		},

		clear: function () {
			this.datacontainer.empty();
			this.descriptors = [];
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
			for (var i = 0; i < this.descriptors.length; i++) {
				var descriptor = this.descriptors[i];
				this.applyViewModeToEditor(descriptor);
				this.toggleButtonVisibility(descriptor);
			}
		},
		applyViewModeToEditor: function (descriptor) {
			var editors = descriptor.Editors;
			for (var i = 0; i < editors.length; i++) {
				editors[i].CiteTypedValueEditor('setCurrentDisplayModeAndApply', this.getCurrentDisplayMode());
			}
		},
		toggleButtonVisibility: function (descriptor) {
			descriptor.Row.find('.removeValueSet').toggle(!descriptor.IsOpen && (this.getCurrentDisplayMode() == jQuery.ui.CiteBaseControl.DisplayMode.Edit));
		}
	});

	$.ui.CiteTypedValueEditor.ValueType = {
		String: 0,
		Text: 1,
		Integer: 2,
		Decimal: 3,
		Boolean: 4,
		Date: 5,
		Time: 6,
		TimeSpan: 7,
		DateTime: 8,
		DateFrame: 9,
		TimeFrame: 10,
		DateTimeFrame: 11,
		Array: 12
	};

	$.ui.CiteTypedValueEditor.TextBoxMode = {
		Text: 0,
		Password: 1
	};

}(jQuery));