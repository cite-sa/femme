/*
 * jQuery CITE BaseControl
 *
 * Depends on:
 *	jquery.ui.core.js 1.9
 *	jquery.ui.widget.js 1.9
 *	jquery Globalize 1.0
 */

(function($, undefined) {

	var zIndexEvaluationClassNames = [];
	var zIndexEvaluationClassSelector = null;
	var culture = null;
	var windowResizeConsumers = [];
	var languageStringsObject = null;
	var ctlIdCounter = 0;

	// Culture
	var decimalFormatters = null;
	var integerFormatter = null;
	var numberParser = null;

	var cultureManager = {
		getCulture: function () {
			return culture.Name;
		},
		getCultureDateFormat: function () {
			return culture.DateFormat;
		},
		getCultureDayNames: function () {
			return culture.DayNames;
		},
		getCultureFirstDayOfWeek: function () {
			return culture.FirstDayOfWeek;
		},
		getCultureIsRTL: function () {
			return culture.IsRTL;
		},
		getCultureMonthNames: function () {
			return culture.MonthNames;
		},
		getCultureNumberDecimalSeparator: function () {
			return culture.NumberDecimalSeparator;
		},
		getCultureNumberGroupSeparator: function () {
			return culture.NumberGroupSeparator;
		},
		getCultureShortDayNames: function () {
			return culture.ShortDayNames;
		},
		getCultureShortestDayNames: function () {
			return culture.ShortestDayNames;
		},
		getCultureShortMonthNames: function () {
			return culture.ShortMonthNames;
		},
		getCultureTimeFormat: function () {
			return culture.TimeFormat;
		},
		getCultureTimeUseAmPm: function () {
			return culture.UseAmPm;
		},
		getCultureTimeAmName: function () {
			return culture.TimeAmName;
		},
		getCultureTimePmName: function () {
			return culture.TimePmName;
		},
		getCultureDateTimeFormat: function () {
			return culture.DateFormat + ' ' + culture.TimeFormat;
		},
		getCustomTimeFormatString: function (key) {
			return culture.CustomTimeFormatStrings[key];
		},

		formatDecimal: function (value, options) {
			options = options || {};
			var maximumFractionDigits = options.maximumFractionDigits;
			var decimalFormatter = maximumFractionDigits || (maximumFractionDigits === 0) ? decimalFormatters.byFractionalDigits[maximumFractionDigits] : decimalFormatters.defaultFormatter;
			if (!decimalFormatter) {
				decimalFormatter = Globalize.numberFormatter({ maximumFractionDigits: maximumFractionDigits });
				decimalFormatters.byFractionalDigits[maximumFractionDigits] = decimalFormatter;
			}
			return decimalFormatter(value);
		},
		formatInteger: function (value) {
			return integerFormatter(value);
		},
		parseNumber: function (value) {
			return numberParser(value);
		}
	};

	$.widget("ui.CiteBaseControl", {
		version: "1.0.0",
		defaultElement: "<div>",
		options: {
			autoInitialize: true,
			currentDisplayMode: 0, // View
			allowDisplayModeToggle: true,
			autoUpdateHash : false
		},

		// Initialization status handling. Does not apply for base control - only used by extenders
		isAutoInitialize: function() {
			return this.options.autoInitialize;
		},
		hasInitialized: function() {
			return this.initializationPerformed;
		},
		setIsInitialized: function() {
			this.initializationPerformed = true;
		},
		doInitialize: function () {
			if (this.hasInitialized()) { return false; }
			this.setIsInitialized();
			this.currentEditValueHash = null;
			this.hasPendingChange = false;
			return true;
		},

		// Language Handling
		getLanguageString: function (key) {
			return jQuery.ui.CiteBaseControl.getLanguageString(key);
		},

		// Culture handling
		getCultureManager: function() {
			return cultureManager;
		},

		// Base properties
		getAllowDisplayModeToggle: function() {
			return this.options.allowDisplayModeToggle;
		},
		setAllowDisplayModeToggle: function(value) {
			this.options.allowDisplayModeToggle = value;
		},
		allowDisplayModeToggle: function() {
			this.options.allowDisplayModeToggle = true;
		},
		denyDisplayModeToggle: function() {
			this.options.allowDisplayModeToggle = false;
		},
		getCurrentDisplayMode: function() {
			return this.options.currentDisplayMode;
		},
		setCurrentDisplayMode: function (value) {
			this.options.currentDisplayMode = value;
			this._fireDisplayModeChangeEvent();
		},
		_fireDisplayModeChangeEvent: function () {
			var event = jQuery.Event("displayModeChanged");
			this.element.trigger(event);
		},

		get: function() {
			return this;
		},

		_create: function() {
			this.initializationPerformed = false;
			if (this.isAutoInitialize()) {
				if (this._getNotifyOnWindowResize)
					windowResizeConsumers.push(this);

				this.doInitialize();
			}
		},

		_destroy: function() {
			if (this._getNotifyOnWindowResize) {
				var idx = $.inArray(this, windowResizeConsumers);
				if (idx !== -1)
					windowResizeConsumers.splice(idx, 1);
			}
		},

		_getNotifyOnWindowResize: function () {
			return false;
		},

		_onWindowResized: function () {
		},

		instanceOf: function (type) {
			var dataItems = this.element.data();
			for (var i in dataItems) {
				if (dataItems[i] instanceof type) {
					return dataItems[i];
				}
			}
			return null;
		},


		/////////////////////////////////
		//
		// Change detection and handling
		//
		/////////////////////////////////

		getValueHash: function () {
			return '';
		},

		_setCurrentValue: function (v, callback) {
			callback();
		},

		_clearCurrentValue: function(callback) {
			callback();
		},

		_stateChanged: function () {
			if ((this.getCurrentDisplayMode() == $.ui.CiteBaseControl.DisplayMode.Edit) && !this.suppressChangeDetection) {
				var csc = jQuery.Event("controlStateChanged");
				this.element.trigger(csc);
			}
		},

		_valueChanged: function(suppressEvent, live) {
			if (/*(this.getCurrentDisplayMode() == $.ui.CiteBaseControl.DisplayMode.Edit) &&*/ !this.suppressChangeDetection) {
				var ev = jQuery.Event(live ? "liveChange" : "dataChanged");
				//var csc = jQuery.Event("controlStateChanged");
				if (this.options.autoUpdateHash) {
					hash = this.getValueHash();
					ev.hasValueChanged = hash !== this.currentEditValueHash;
					ev.canDetectChanges = true;
				//	csc.hasValueChanged = hash !== this.currentEditValueHash;
				//	csc.canDetectChanges = true;
					this.currentEditValueHash = hash;
				} else {
					ev.canDetectChanges = false;
				//	csc.canDetectChanges = false;
				}
				if(!suppressEvent) this.element.trigger(ev);
				//this.element.trigger(csc);
			}
			this.pendingChange = live ? true : false;
		},

		_valueChangedLive: function (suppressEvent) {
			this._valueChanged(suppressEvent, true);
		},

		hasPendingChange: function () {
			return this.pendingChange;
		},

		setCurrentValue: function (v) {
			var self = this;
			this.suppressChangeDetection = true;
			this._setCurrentValue.apply(this, $.makeArray(arguments).concat([function () {
				self.currentEditValueHash = self.getValueHash();
				self.suppressChangeDetection = false;
			}]));
		},

		clearCurrentValue: function() {
			var self = this;
			this.suppressChangeDetection = true;
			this._clearCurrentValue.apply(this, [function () {
				self.currentEditValueHash = self.getValueHash();
				self.suppressChangeDetection = false;
			}]);
		},

		getCurrentValue: function() {
		}

	});

	jQuery.ui.CiteBaseControl.setzIndexEvaluationClassNames = function(classes) {
		zIndexEvaluationClassNames = classes;
		zIndexEvaluationClassSelector = $.map(classes, function(val, i) { return '.' + val }).join(',');
	};
	jQuery.ui.CiteBaseControl.getMaxZIndex = function() {
		return Math.max.apply(null, [0].concat($.map($(zIndexEvaluationClassSelector).filter(':visible'), function(e, n) {
			return parseInt($(e).css('z-index')) || 1;
		})
		));
	};

	jQuery.ui.CiteBaseControl.setLanguageStringsObject = function (s) {
		languageStringsObject = s;
	};

	jQuery.ui.CiteBaseControl.setCulture = function (c) {
		culture = c;
		decimalFormatters = {
			defaultFormatter: Globalize.numberFormatter(),
			byFractionalDigits: []
		};
		integerFormatter = Globalize.numberFormatter({
			maximumFractionDigits: 0,
		});
		numberParser = Globalize.numberParser();
	};

	jQuery.ui.CiteBaseControl.getCultureManager = function () {
		return cultureManager;
	};

	jQuery.ui.CiteBaseControl.find = function (id, root) {
		if (root === undefined) root = 'body';
		var element = $("#" + id, root);
		var dataItems = element.data();
		for (var i in dataItems) {
			if (dataItems[i] instanceof $.ui.CiteBaseControl) {
				return dataItems[i];
			}
		}
		return null;
	};

	jQuery.ui.CiteBaseControl.getWidgetInstance = function (element) {
		var dataItems = element.data();
		for (var i in dataItems) {
			if (dataItems[i] instanceof $.ui.CiteBaseControl) {
				return dataItems[i];
			}
		}
		return null;
	};

	jQuery.ui.CiteBaseControl.getInstance = function (element, type) {
		if (type === undefined) type = $.ui.CiteBaseControl;
		var dataItems = element.data();
		for (var i in dataItems) {
			if (dataItems[i] instanceof type) {
				return dataItems[i];
			}
		}
		return null;
	};

	jQuery.ui.CiteBaseControl.destroyInstance = function (element, type) {
		var instance = jQuery.ui.CiteBaseControl.getInstance(element, type);
		if (instance)
			instance.destroy();
	};

	jQuery.ui.CiteBaseControl.getClassByName = function (str) {
		var arr = str.split(".");
		var fn = window;
		for (var i = 0, len = arr.length; i < len; i++) {
			fn = fn[arr[i]];
		}
		return fn;
	}

	jQuery.ui.CiteBaseControl.isSubclassOf = function (extendedClassName, baseClassName) {
		if (baseClassName === extendedClassName) return true;
		var baseClass = jQuery.ui.CiteBaseControl.getClassByName('$.ui.' + baseClassName);
		if (baseClass.prototype.constructor._childConstructors) {
			var q = baseClass.prototype.constructor._childConstructors.slice(0);
			while (q.length > 0) {
				var c = q.shift();
				if (c.prototype.widgetName === extendedClassName) return true;
				q = q.concat(c._childConstructors.slice(0));
			}
		}

		return false;
	}

	jQuery.ui.CiteBaseControl.windowResized = function() {
		for (var i = 0; i < windowResizeConsumers.length; i++)
			windowResizeConsumers[i]._onWindowResized();
	}

	jQuery.ui.CiteBaseControl.generateControlId = function () {
		return 'cbc' + ctlIdCounter++;
	};

	jQuery.ui.CiteBaseControl.getLanguageString = function (key, defaultValue) {
		var langStringsObj = (languageStringsObject !== null) ? languageStringsObject : window['languageStrings'];
		var s = langStringsObj ? langStringsObj[key] : undefined;
		return s === undefined ? (defaultValue === undefined ? ('_' + key) : defaultValue) : s;
	};

	jQuery.ui.CiteBaseControl.DisplayMode = {
		View: 0,
		Edit: 1
	};
} (jQuery));
