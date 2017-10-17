/*
 * jQuery CITE Spinner
 *
 * Depends on:
 *	jquery.ui.core.js 1.9
 *	jquery.ui.widget.js 1.9
 *	jquery.ui.spinner.js 1.9
 *  jquery.cite.basecontrol.js 1.0
 *	jquery Globalize 1.0
 *  jasny bootstrap inputmask
 */

(function($, undefined) {
	var keys = {
		BACKSPACE: 8,
		TAB: 9,
		PAGE_UP: 33,
		PAGE_DOWN: 34,
		END: 35,
		HOME: 36,
		LEFT: 37,
		UP: 38,
		RIGHT: 39,
		DOWN: 40,
		DELETE: 46,
		ZERO: 48,
		NINE: 57,
		A: 65,
		NUM_ZERO: 96,
		NUM_NINE: 105,
		ADD: 107,
		SUBTRACT: 109,
		NUM_DECIMAL_POINT: 110,
		COMMA: 188,
		DASH: 189,
		PERIOD: 190
	};

	$.widget("ui.CiteSpinner", $.ui.CiteBaseControl, {
		version: "1.0.0",
		defaultElement: "<span>",
		options: {
			mode: 0,	// Integer
			value: null,
			maxValue: null,
			minValue: null,
			stepSize: 1,
			decimalPlaces: null,	// How many trailing zeros you want to display
			pageSteps: 10
		},

		doInitialize: function () {
			if (this._super("doInitialize") === false) return false;

			if (this.options.mode == $.ui.CiteSpinner.SpinnerMode.Integer) this.options.decimalPlaces = 0;

			this.element.addClass('CiteSpinner');

			var container = $('<div class="input-group" />');
			this.element.append(container);

			this.spinner = $('<input type="text" class="form-control" />');
			this.spinner.val(this.options.value != null ? this._setValueToInputMask(this.options.value) : this._setValueToInputMask(0));
			if (this.options.maxValue != null) this.spinner.attr('max', this.options.maxValue);
			if (this.options.minValue != null) this.spinner.attr('min', this.options.minValue);
			this.spinner.attr('step', this.options.stepSize);
			container.append(this.spinner);

			var spinButtonsContainer = $('<div class="input-group-btn-vertical" />');
			container.append(spinButtonsContainer);

			this.btnUp = $('<button class="btn btn-default" type="button" style="margin-bottom: 0px;"><i class="fa fa-caret-up"></i></button>');
			this.btnDown = $('<button class="btn btn-default" type="button" style="margin-bottom: 0px;"><i class="fa fa-caret-down"></i></button>');
			spinButtonsContainer.append(this.btnUp).append(this.btnDown);

			this.minusSign = Globalize.cldr.main("numbers/symbols-numberSystem-latn/minusSign");
			this.decimalSeparator = Globalize.cldr.main("numbers/symbols-numberSystem-latn/decimal");
			this.groupSeparator = Globalize.cldr.main("numbers/symbols-numberSystem-latn/group");
			if (this.options.mode == $.ui.CiteSpinner.SpinnerMode.Integer)
				//this.mask = '[' + this.minusSign + ']9{+}';
				this.spinner.inputmask('integer');
			else
				//this.mask = '[' + this.minusSign + '](9|\\' + this.groupSeparator + '){+}';
				this.spinner.inputmask('decimal', { autoGroup: true, groupSeparator: this.groupSeparator, radixPoint: this.decimalSeparator });

			var self = this;

			this._setupChangeEvents();
			this._setupChangeHandlers();

			this.spinner.bind('input', function (e) {
				e.stopPropagation();
				self.fireLiveEvent();
			});

			this.applyViewMode();

			return true;
		},

		getMode: function() {
			return this.option('mode');
		},
		setMode: function (mode) {
			this.option('mode', mode)
		},
		getValue: function () {
			return jQuery.ui.CiteBaseControl.getCultureManager().parseNumber(this.spinner.val());
		},
		setValue: function (value, updateUI) {
			this.option('value', value);
			if (updateUI === undefined) updateUI = true;
			if (updateUI) {
				this.spinner.val(this._setValueToInputMask(value));
			}
		},
		getMaxValue: function() {
			return this.option('maxValue');
		},
		setMaxValue: function (maxValue) {
			this.option('maxValue', maxValue)
		},
		getMinValue: function() {
			return this.option('minValue');
		},
		setMinValue: function (minValue) {
			this.option('minValue', minValue)
		},
		getStepSize: function() {
			return this.option('stepSize');
		},
		setStepSize: function (stepSize) {
			this.option('stepSize', stepSize);
		},
		getPageSteps: function() {
			return this.option('pageSteps');
		},
		setPageSteps: function (pageSteps) {
			this.option('pageSteps', pageSteps);
		},

		setFocus: function() {
			this.spinner.focus();
		},

		_changeValue: function (val) {
			var maxVal = this.options.maxValue;
			var minVal = this.options.minValue;

			var curValue = this.spinner.val();
			if (curValue == '' || curValue == '.') { this.spinner.val(''); return; }

			var modValue = this.getValue() + val;
			if (maxVal != null && modValue > maxVal) modValue = maxVal;
			if (minVal != null && modValue < minVal) modValue = minVal;
			this.spinner.val(this._setValueToInputMask(modValue));

			this.fireModificationEvent();
		},
		_setValueToInputMask: function (val) {
			if (val === undefined) return null;
			//return this.options.mode == $.ui.CiteSpinner.SpinnerMode.Decimal ? parseFloat(val).toFixed(this.options.decimalPlaces) : parseInt(val, 10);
			var formatted = (this.option("mode") == $.ui.CiteSpinner.SpinnerMode.Decimal)
				? jQuery.ui.CiteBaseControl.getCultureManager().formatDecimal(val, { maximumFractionDigits: this.option("decimalPlaces") })
				: jQuery.ui.CiteBaseControl.getCultureManager().formatInteger(val);

			var number = jQuery.ui.CiteBaseControl.getCultureManager().parseNumber(formatted);

			return number;
		},
		//_formatValue: function (val) {
		//	if (val === undefined) return null;
		//	//return this.options.mode == $.ui.CiteSpinner.SpinnerMode.Decimal ? parseFloat(val).toFixed(this.options.decimalPlaces) : parseInt(val, 10);
		//	var formatted = (this.option("mode") == $.ui.CiteSpinner.SpinnerMode.Decimal)
		//		? jQuery.ui.CiteBaseControl.getCultureManager().formatDecimalNothousands(val, { maximumFractionDigits: this.option("decimalPlaces") })
		//		: jQuery.ui.CiteBaseControl.getCultureManager().formatInteger(val);
		//	return formatted;
		//},
		////////////////////////
		//
		// VIEW MODE
		//
		////////////////////////
		setCurrentDisplayModeAndApply: function(displayMode) {
			this.setCurrentDisplayMode(displayMode);
			if (this.hasInitialized())
				this.applyViewMode();
		},
		applyViewMode: function () {
			if (this.getCurrentDisplayMode() == "0") { //view
				this.spinner.attr('disabled', true);
			} else if (this.getCurrentDisplayMode() == "1") { //edit
				this.spinner.attr('disabled', false);
			}
		},

		////////////////////////
		//
		// CHANGES HANDLING
		//
		////////////////////////
		_setupChangeEvents: function () {
			var self = this;

			this.btnUp.unbind('click').bind('click', function () {
				if (self.getCurrentDisplayMode() == "1") {
					self._changeValue(self.options.stepSize);
					self.setFocus();
				}
			});
			this.btnDown.unbind('click').bind('click', function () {
				if (self.getCurrentDisplayMode() == "1") {
					self._changeValue(-self.options.stepSize);
					self.setFocus();
				}
			});

			/*
			function keyHandler() {
				if (arguments.length == 0) return;

				if (self.getCurrentDisplayMode() == "1") {
					var e = arguments[0];
					switch (e.which) {
						case keys.PAGE_UP:
							e.preventDefault();
							self._changeValue(self.options.pageSteps);
							break;
						case keys.PAGE_DOWN:
							e.preventDefault();
							self._changeValue(-self.options.pageSteps);
							break;
						case keys.UP:
							e.preventDefault();
							self._changeValue(self.options.stepSize);
							break;
						case keys.DOWN:
							e.preventDefault();
							self._changeValue(-self.options.stepSize);
							break;
						default:
							// Check if is digit
							var isDigit = (e.keyCode >= keys.ZERO && e.keyCode <= keys.NINE) || (e.keyCode >= keys.NUM_ZERO && e.keyCode <= keys.NUM_NINE);
							// General Allowable keys
							var isAllowableKey = (e.keyCode == keys.A && (navigator.platform.match("Mac") ? e.metaKey : e.ctrlKey))	// Check for Ctrl+A
													|| e.keyCode == keys.BACKSPACE || e.keyCode == keys.DELETE
													|| e.keyCode == keys.SUBTRACT || e.keyCode == keys.DASH
													|| e.keyCode == keys.HOME || e.keyCode == keys.END || e.keyCode == keys.TAB
													|| e.keyCode == keys.LEFT || e.keyCode == keys.RIGHT;

							// Decimal Editor Allowable keys
							var isDecimalAllowableKey = e.keyCode == keys.PERIOD || e.keyCode == keys.NUM_DECIMAL_POINT;		// TODO Support for Culture

							var preventKey = (!isDigit && !isAllowableKey) &&		// if this isn't digit and allowable key
								(self.options.mode == $.ui.CiteSpinner.SpinnerMode.Decimal ? !isDecimalAllowableKey : true);	// if we have decimal mode, check for decimal allowable keys too.

							if (preventKey)	e.preventDefault();
					}
				}
			}
			this.spinner.unbind('keydown').bind('keydown', keyHandler);
			*/

			this.spinner.unbind('focusout').bind('focusout', function () {
				self._changeValue(0);	// Just refresh spinner to check if value is in allowed range. (e.g. user put input with keyboard)
			});
		},
		_setupChangeHandlers: function () {
			var self = this;
			var handleChange = function (e) {
				e.stopPropagation();
				self._valueChanged();
			};
			var handleLiveChange = function (e) {
				e.stopPropagation();
				self._valueChangedLive();
			};
			this.element.on('datachanged', handleChange);
			this.element.on('datachangedlive', handleLiveChange);
		},

		fireModificationEvent: function () {
			this._valueChanged();
		},

		fireLiveEvent: function () {
			this._valueChangedLive();
		},

		getValueHash: function () {
			var v = this._getValue();
			if (v === null || v === undefined) return 0;
			return JSON.stringify(v).hashCode();
		}
	});

	$.ui.CiteSpinner.SpinnerMode = {
		Integer: 0,
		Decimal: 1
	};

} (jQuery));
