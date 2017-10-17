/*
 * jQuery CITE AutoSuggest
 *
 * Depends on:
 *	jquery.ui.core.js 1.9
 *	jquery.ui.widget.js 1.9
 *  jquery.cite.basecontrol.js 1.0
 *	jquery.autosuggest.js
 */

(function($, undefined) {

	var constants = {
		EMPTY_OPTION_VALUE: "$NULL$",
		DEFAULT_APPEND_TO_SELECTOR: '.autoSuggestAppendContainer'
	};

	$.widget("ui.CiteAutoSuggest", $.ui.CiteBaseControl, {
		version: "1.0.0",
		defaultElement: "<div>",
		options: {
			presentationColumnDefinitions: [],
			presentationTemplateFormatter: null,
			presentationTemplate: null,
			uiMode: 0,
			suggestionMode: 0,
			staticSuggestions: [],
			allowMultiSelect: false,
			readOnly: false,
			selectedNames: [],
			selectedValues: [],
			selectionNameProperty: '',
			selectionValueProperty: '',
			maximumSelections: 1,
			minimumSelections: 0,
			allowOnlySuggestionSelections: false,
			watermarkText: '',
			minimumCharacters: 1,
			maximumResults: 0,
			noResultsText: '',
			retrieveSuggestionsCallback: null,
			displayAddButton: false,
			addButtonIconClass: null,
			addButtonCallback: null,
			highlightMatches: false,
			fixedFilters: [],
			additionalFiltersCallback: null,
			addDropDownEmptyOption: false,
			resultsListAppendTo: null,
			disabledValues: [],
			beforeSelectCallback: null,
			presentationPoint: null
		},

		doInitialize: function() {
			if (this._super("doInitialize") === false) return false;

			if (this.options.presentationPoint != null) {
				//this.options.maximumResults = this.options.presentationPoint.limit;
				this.options.presentationColumnDefinitions = this.options.presentationPoint.fields;
				this.options.presentationTemplateFormatter = this.options.presentationPoint.templateFormatter;
				this.options.presentationTemplate = this.options.presentationPoint.template;
				this._setRetrievePresentationPointSuggestionsCallback(this.options.retrieveSuggestionsCallback);
			}

			this.loadedItems = {};
			this.currentFilters = [];
			this.blurTimeout = null;

			// If any items are currently selected, construct the pre-selection list
			var preselectedItems = [];
			var selectedValues = this.getSelectedValues();
			if (selectedValues.length > 0) {
				var selectedNames = this.getSelectedNames();
				for (var i = 0; i < selectedValues.length; i++) {
					if ((selectedValues[i] != null) && (selectedValues[i].length > 0)) {
						var o = {};
						o[this.getSelectionNameProperty()] = selectedNames[i];
						o[this.getSelectionValueProperty()] = selectedValues[i];
						preselectedItems.push(o);
					}
				}
			}

			// Construct the options and data objects
			var self = this;
			var isDropDown = (this.getUiMode() == $.ui.CiteAutoSuggest.UIMode.DropDown);
			var appendTo = this.getResultsListAppendTo();
			var options = {
				startText: this.getWatermarkText(),
				emptyText: this.getNoResultsText(),
				preFill: preselectedItems,
				selectedItemProp: this.getSelectionNameProperty(),
				selectedValuesProp: this.getSelectionValueProperty(),
				searchObjProps: this.getSelectionNameProperty(),
				minChars: this.getMinimumCharacters(),
				retrieveLimit: this.getMaximumResults() > 0 ? this.getMaximumResults() : false,
				readOnly: this.getReadOnly() || isDropDown,
				selectionLimit: this.getMaximumSelections(),
				minimumSelections: this.getMinimumSelections(),
				allowOnlySuggestionSelections: this.getAllowOnlySuggestionSelections(),
				resultsHighlight: this.getHighlightMatches(),
				neverSubmit: true,
				resultsListAppendTo: appendTo ? appendTo : constants.DEFAULT_APPEND_TO_SELECTOR,
				selectionAdded: function(element, name, value) {
					self.updateSelectedItems();
					self.fireModificationEvent(true);
				},
				selectionRemoved: function() {
					self.updateSelectedItems();
					self.fireModificationEvent(false);
				},
				beforeShowSuggestions: function (data) {
					var sv = self.getSelectionValueProperty();
					var grayed = (data !== undefined) && (data !== null) && (sv in data) && (($.inArray(data[sv], self.getSelectedValues()) !== -1) || ($.inArray(data[sv], self.getDisabledValues()) !== -1));
					$(this).toggleClass('as-disabled-item', grayed);
				},
				beforeSelect: function (data, clickEvent) {
					// Invoke the beforeSelect callback, if defined. However, if the item is disabled, don't return what beforeSelect says (always return false).
					var isDisabled = $(this).hasClass('as-disabled-item');
					var beforeSelectResult = true;
					if (self.options.beforeSelectCallback)
						beforeSelectResult = eval(self.options.beforeSelectCallback)(clickEvent);
					if (isDisabled) return false;
					return beforeSelectResult;
				}
			};
			var data;
			if (this.getSuggestionMode() == $.ui.CiteAutoSuggest.SuggestionMode.Static) {
				data = this.formatSuggestions(this.getStaticSuggestions());
			} else {
				data = function(text, limit, callback) {
					if (self.getRetrieveSuggestionsCallback()) {
						if (limit === false) limit = null;
						self.loadedItems = {};
						eval(self.getRetrieveSuggestionsCallback()).call(self, text, self.currentFilters, limit, function(items) {
							items = self.addEmptyOptionToList(items);
							var formattedItems = self.formatSuggestions(items);
							var vProp = self.getSelectionValueProperty();
							for (var i = 0; i < items.length; i++) {
								if (items[i][vProp])
									self.loadedItems[items[i][vProp]] = items[i];
							}
							callback(formattedItems);
						});
					}
					else {
						callback([]);
					}
				};
			}

			this.element[0].style.display = 'none';
			this.element.addClass('input-group AutoSuggest');

			this.innerContainer = $('<div class="asgInner"></div>');
			this.element.append(this.innerContainer);

			// Initialize the AutoSuggest input box
			this.textbox = $('<input type="text" class="asgtxt" />');
			this.innerContainer.append(this.textbox);
			//this.texbox.bind('keyup', function () { self.fireLiveModificationEvent() });
			this.textbox.bind('change', function () { self.fireTextModificationEvent() });

			this.textbox.autoSuggest(data, options);
			this.updateSelectedItems();
			//var boxHeight = $(this.textbox).closest('.as-selections').outerHeight();

			//this.buttonsContainer = $('');
			//this.innerContainer.append(this.buttonsContainer);

			// Initialize the dropdown button
			this.dropDownButton = $('<button class="asgbtn btn btn-white dropdown-toggle" style="border-left: none; border-radius: 0;" type="button"><span class="caret"></span></button>');
			this.dropDownButtonInner = this.dropDownButton;
			this.innerContainer.append(this.dropDownButton);
			this.dropDownButton
				.bind('click', { autosuggest: this }, function(e) {
					e.preventDefault();
					if ($(e.data.autosuggest.textbox).autoSuggest('option', 'isOpen')) {
						$(e.data.autosuggest.textbox).autoSuggest('option', 'close');
					} else {
						self.dropDownButtonInner.focus();
						$(e.data.autosuggest.textbox).autoSuggest('option', 'open');
					}
				})
				.bind('keydown', { autosuggest: this }, function(e) {
					if (e.keyCode != 9) {
						e.preventDefault();
						e.stopPropagation();
					}
					$(e.data.autosuggest.textbox).autoSuggest('option', 'keydown', e.keyCode);
				});
			this.dropDownButtonInner.bind('blur', { autosuggest: this }, function(e) {
				if (!$(e.data.autosuggest.textbox).autoSuggest('option', 'isOpen')) return;

				var btn = self.dropDownButtonInner;
				if (e.data.autosuggest.blurTimeout != null) clearTimeout(e.data.autosuggest.blurTimeout);
				e.data.autosuggest.blurTimeout = setTimeout(function() {
					if (!$(e.data.autosuggest.textbox).autoSuggest('option', 'lastClickInSuggestions'))
						$(e.data.autosuggest.textbox).autoSuggest('option', 'close');
					else {
						if ($(e.data.autosuggest.textbox).autoSuggest('option', 'isOpen')) btn.focus();
					}
					e.data.autosuggest.blurTimeout = null;
				}, 300);
			});

			if (isDropDown) {
				// For static dropdowns, make the keyboard keys navigate to the first element starting with the given letter
				if (this.getUiMode() == this.DISPLAY_MODE_DROPDOWN) {
					$('.as-selections', this.element).attr('tabindex', 0);
					$('.as-selections', this.element).bind('click', { autosuggest: this }, function (e) {
						if (!$(e.data.autosuggest.textbox).autoSuggest('option', 'isOpen'))
							$(e.data.autosuggest.textbox).autoSuggest('option', 'open', function() {
								$(e.data.autosuggest.dropDownButtonInner).focus();
							});
					});
					$(this.locateBaseElement()).bind('keypress', { autosuggest: this }, function(e) {
						if (e.which == 0) return;
						var chr = String.fromCharCode(e.which).toLowerCase();
						if ($(e.data.autosuggest.textbox).autoSuggest('option', 'isOpen')) {   // Open autosuggest
							$(e.data.autosuggest.textbox).autoSuggest('option', 'selectFirst', chr);
						}
						else {  // Closed autosuggest
							if (e.data.autosuggest.getSuggestionMode() == $.ui.CiteAutoSuggest.SuggestionMode.Static) {  // static suggestions
								var staticSuggestions = e.data.autosuggest.getStaticSuggestions();
								var nProp = e.data.autosuggest.getSelectionNameProperty();
								for (var j = 0; j < staticSuggestions.length; j++) {
									if (staticSuggestions[j][nProp].length > 0 && staticSuggestions[j][nProp][0].toLowerCase() == chr) {
										e.data.autosuggest.selectItem(staticSuggestions[j][e.data.autosuggest.getSelectionValueProperty()]);
										break;
									}
								}
							}
							else {  // dynamic suggestions
								$(e.data.autosuggest.textbox).autoSuggest('option', 'open', function() {
									$(e.data.autosuggest.textbox).autoSuggest('option', 'selectFirst', chr);
									$(e.data.autosuggest.dropDownButtonInner).focus();
								});
							}
						}
					});
				}
			}

			// build the initial criteria
			if (this.getFixedFilters().length > 0)
				this.buildCriteriaAndReload(false);

			// Initialize the add button
			if (this.getDisplayAddButton()) {
				this.addButton = $('<button class="asgbtn btn btn-white" style="text-align: center;" type="button"><span class="fa fa-plus"></span></button>');
				this.innerContainer.append(this.addButton);
				
				this.addButton
					.bind('click', { autosuggest: this }, function (e) {
						e.preventDefault();
						var autosuggest = e.data.autosuggest;
						var addHandler = autosuggest.getAddButtonCallback();
						if (addHandler != '')
							eval(addHandler)(autosuggest, autosuggest.getSelectedNames(), autosuggest.getSelectedValues());
					});
			};

			var buttons = $('.asgbtn', this.element);
			var numButtons = buttons.length;
			for (var i = 0; i < numButtons; i++)
				$(buttons[i]).css({ 'right': (numButtons - i - 1) * 2 + 'em', 'height': '100%' });
			if (this.getReadOnly() && numButtons > 0)
				buttons.button('disable');
			$('.as-selections', this.element).css('padding-right', (numButtons * 2) + 'em');

			this.updateDropDownIcon();
			this.updateAddIcon();

			this.element[0].style.display = 'block';

			this._setupChangeHandlers();

			if (this.options.autoUpdateHash)
				this.currentEditValueHash = this.getValueHash();

			return true;
		},

		updateDropDownIcon: function () {
			if (!this.dropDownButton) return;
			var maxSelections = this.getMaximumSelections();
			if (maxSelections != 1) {
				var v = this.getSelectedValues();
				var icon = $('.ui-icon', this.dropDownButton);
				icon.removeClass('ui-icon-triangle-1-s ui-icon-plus');
				icon.addClass(v.length > 0 ? 'ui-icon-plus' : 'ui-icon-triangle-1-s');
			}
		},
		updateAddIcon: function () {
			if (!this.addButton || !this.getDisplayAddButton()) return;
			var v = this.getSelectedValues();
			this.addButton.toggleClass('highlighted', v.length > 0);

			var self = this;
			this.stopAddIconAnimation();
			this.animateAddIcon();
			this.addAnimationTimeout = setTimeout(function () { self.stopAddIconAnimation(); }, 3000);
		},
		animateAddIcon: function () {
			var self = this;
			var btn = this.addButton;
			if (btn.hasClass('highlighted')) {
				btn.fadeOut(function () {
					if (btn.hasClass('highlighted')) {
						btn.fadeIn(function () {
							self.animateAddIcon();
						});
					}
				});
			}
			else
				this.stopAddIconAnimation();
		},
		stopAddIconAnimation: function () {
			if (this.addAnimationTimeout) {
				clearTimeout(this.addAnimationTimeout);
				this.addAnimationTimeout = null;
			}
			this.addButton.stop().fadeIn();
		},

		getSelectedNames: function() {
			var n = this.option('selectedNames');
			var v = this.option('selectedValues');
			if (v.length > 0 && this.getAddDropDownEmptyOption()) {
				var i = $.inArray(constants.EMPTY_OPTION_VALUE, v);
				if (i !== -1)
					n.splice(i, 1);
			}
			return n;
		},
		setSelectedNames: function(values) {
			this.option('selectedNames', values);
		},
		getSelectedValues: function() {
			var v = this.option('selectedValues');
			if (v.length > 0 && this.getAddDropDownEmptyOption()) {
				var i = $.inArray(constants.EMPTY_OPTION_VALUE, v);
				if (i !== -1)
					v.splice(i, 1);
			}
			return v;
		},
		getSingleValueOrDefault: function () {
			var values = this.getSelectedValues();
			return (values.length > 0) ? values[0] : '';
		},
		setSelectedValues: function(values) {
			this.option('selectedValues', values);
		},
		getDisabledValues: function () {
			return this.option("disabledValues");
		},
		setDisabledValues: function (values) {
			this.option('disabledValues', values);
		},
		getUiMode: function() {
			return this.option("uiMode");
		},
		getSuggestionMode: function() {
			return this.option("suggestionMode");
		},
		setStaticSuggestions: function(value) {
			this.option('staticSuggestions', value);
			this.setStaticAutoSuggestions();
		},
		getAllowMultiSelect: function() {
			return this.option("allowMultiSelect");
		},
		getStaticSuggestions: function() {
			return this.option("staticSuggestions");
		},
		getRetrieveSuggestionsCallback: function() {
			return this.option("retrieveSuggestionsCallback");
		},
		setRetrieveSuggestionsCallback: function(value) {
			this.option("retrieveSuggestionsCallback", value);
		},
		getSelectionNameProperty: function() {
			return this.option("selectionNameProperty");
		},
		getSelectionValueProperty: function() {
			return this.option("selectionValueProperty");
		},
		setSelectionNameAndValueProperties: function(selNameProp, selValueProp) {
			// Clear the current selections and update the properties
			this.clearSelection();
			this.option("selectionNameProperty", selNameProp);
			this.option("selectionValueProperty", selValueProp);

			// Set the new options in the underlying autosuggest object
			this.textbox.autoSuggest('option', 'setOptions', {
				selectedItemProp: selNameProp,
				selectedValuesProp: selValueProp,
				searchObjProps: selNameProp
			});
		},
		getMaximumSelections: function() {
			return this.option("maximumSelections");
		},
		getMinimumSelections: function() {
			return this.option("minimumSelections");
		},
		getWatermarkText: function() {
			return this.option("watermarkText");
		},
		getNoResultsText: function() {
			return this.option("noResultsText");
		},
		getMinimumCharacters: function() {
			return this.option("minimumCharacters");
		},
		getMaximumResults: function() {
			return this.option("maximumResults");
		},
		setMaximumResults: function (value) {
			this.option('maximumResults', value);
			if (this.textbox)
			    this.textbox.autoSuggest('option', 'retrieveLimit', value > 0 ? value : false);
		},
		getReadOnly: function() {
			return this.option("readOnly");
		},
		setReadOnly: function (value) {
			this.option("readOnly", value);

			var buttons = $('.asgbtn', this.element);
			var numButtons = buttons.length;
			for (var i = 0; i < numButtons; i++)
				$(buttons[i]).css({ 'right': (numButtons - i - 1) * 2 + 'em', 'height': '100%' });
			if (numButtons > 0)
				buttons.button(value ? 'disable' : 'enable');
			$('.as-selections', this.element).css('padding-right', (numButtons * 2) + 'em');

		},
		getAllowOnlySuggestionSelections: function() {
			return this.option("allowOnlySuggestionSelections");
		},
		getDisplayAddButton: function() {
			return this.option("displayAddButton");
		},
		setDisplayAddButton: function (value) {
			this.option('displayAddButton', value);
			if (this.addButton) {
				if (value) {
					this.addButton.removeAttr("disabled");
				}
				else {
					this.addButton.attr("disabled", "disabled");
				}
			}
		},
		getAddButtonCallback: function() {
			return this.option("addButtonCallback");
		},
		setAddButtonCallback: function(value) {
			this.option('addButtonCallback', value);
		},
		getAddButtonIconClass: function() {
			return this.option('addButtonIconClass');
		},
		getHighlightMatches: function() {
			return this.option("highlightMatches");
		},
		getFixedFilters: function() {
			return this.option('fixedFilters');
		},
		setFixedFilters: function(value) {
			this.option('fixedFilters', value);
		},
		getAdditionalFiltersCallback: function() {
			return this.option('additionalFiltersCallback');
		},
		setAdditionalFiltersCallback: function(value) {
			this.option('additionalFiltersCallback', value);
		},
		getAddDropDownEmptyOption: function() {
			return this.option('addDropDownEmptyOption');
		},
		getResultsListAppendTo: function() {
			return this.option('resultsListAppendTo');
		},
		getPresentationColumnDefinitions: function() {
			return this.option('presentationColumnDefinitions');
		},
		getPresentationTemplateFormatter: function() {
			return this.option('presentationTemplateFormatter');
		},
		getPresentationTemplate: function() {
			return this.option('presentationTemplate');
		},

		formatSuggestions: function(items) {
			var vp = this.getSelectionValueProperty();
			var np = this.getSelectionNameProperty();
			var templateFormatter = this.getPresentationTemplateFormatter();
			
			if (templateFormatter) {
				var colModel = this.getPresentationColumnDefinitions();
				var template = this.getPresentationTemplate();
				var formatted = [];

				for (var i = 0; i < items.length; i++) {
					if (items[i][vp] == constants.EMPTY_OPTION_VALUE) {
						formatted.push(items[i]);
						continue;  // Skip the empty entry
					}

					var formattedItem = $.extend(true, {}, items[i]);
					for (var c = 0; c < colModel.length; c++) {
						var colName = colModel[c].Name;
						formattedItem[colName] = this.callFormatter(colModel[c].Formatter.name, items[i][colName]);
					}
					formattedItem[np] = this.callFormatter(templateFormatter.name, template, { formattedValues: formattedItem, originalValues: items[i] });
					formatted.push(formattedItem);
				}

				return formatted;
			}
			else
				return items;
		},

		callFormatter: function(formatterName, value, options) {
			return window['AutoSuggest_' + formatterName].call(this, value, options);
		},

		setStaticAutoSuggestions: function() {
			var data;
			if (this.getSuggestionMode() == $.ui.CiteAutoSuggest.SuggestionMode.Static) {
				data = this.getStaticSuggestions();
				data = this.formatSuggestions(this.addEmptyOptionToList(data));
			} else {
				data = function(text, limit, callback) {
					if (self.getRetrieveSuggestionsCallback()) {
						if (limit === false) limit = null;
						eval(self.getRetrieveSuggestionsCallback()).call(self, text, self.currentFilters, limit, callback);
					}
					else
						callback([]);
				};
			}
			this.textbox.autoSuggest('option', 'setItems', data);
			if (this.options.autoUpdateHash)
				this.currentEditValueHash = this.getValueHash();
		},

		addEmptyOptionToList: function(items) {
			var vp = this.getSelectionValueProperty();
			var np = this.getSelectionNameProperty();
			if (this.getAddDropDownEmptyOption() && ((items.length == 0) || (items[0][vp] !== constants.EMPTY_OPTION_VALUE))) {
				var itm = {};
				itm[np] = '-';
				itm[vp] = constants.EMPTY_OPTION_VALUE;
				items.splice(0, 0, itm);
			}
			return items;
		},

		buildCriteriaAndReload: function(bReload) {
			if (bReload === undefined) bReload = true;
			//        var filterSections = this.getFilteringSections();
			this.currentFilters = [];

			//        for (var i = 0; i < filterSections.length; i++) {
			//            var controls = filterSections[i].Controls.FilteringControls;
			//            for (var j = 0; j < controls.length; j++) {
			//                this.currentFilters.push($find(controls[j].ClientID).getCriteria());
			//            }
			//        }
			var fixedFilters = this.getFixedFilters();
			for (var i = 0; i < fixedFilters.length; i++) {
				this.currentFilters.push(fixedFilters[i]);
			}
			if (this.getAdditionalFiltersCallback() != null)
				this.currentFilters = this.currentFilters.concat(eval(this.getAdditionalFiltersCallback())(this.currentFilters));

			if (bReload) {
				this.textbox.autoSuggest('option', 'open');
			}
		},

		addOrReplaceFixedCriterion: function(filter) {
			var filterType = null;
			for (var key in filter) {
				if (filter.hasOwnProperty(key)) {
					filterType = key;
					break;
				}
			}
			if (filterType == null) return;

			var fixedFilters = this.getFixedFilters();
			var updated = false;
			for (var i = 0; i < fixedFilters.length; i++) {
				if (fixedFilters[i][filterType]) {
					fixedFilters[i] = filter;
					updated = true;
					break;
				}
			}
			if (!updated)
				this.setFixedFilters(fixedFilters);
		},

		updateSelectedItems: function() {
			var v = this.textbox.autoSuggest('option', 'selectedValues');
			var n = this.textbox.autoSuggest('option', 'selectedNames');
			if (v.length > 0 && this.getAddDropDownEmptyOption()) {
				var i = $.inArray(constants.EMPTY_OPTION_VALUE, v);
				if (i !== -1) {
					v.splice(i, 1);
					n.splice(i, 1);

					var vp = this.getSelectionValueProperty();
					var np = this.getSelectionNameProperty();
					var newItems = [];
					for (var j = 0; j < v.length; v++) {
						var itm = {};
						itm[vp] = v[j];
						itm[np] = n[j];
						newItems.push(itm);
					}
					this.textbox.autoSuggest('option', 'setSelectedItems', newItems);
				}
			}

			this.setSelectedValues(v);
			this.setSelectedNames(n);

			this.updateDropDownIcon();
			this.updateAddIcon();

			//TODO this should be uncommented to allow updating the UI when in view mode.
			//However problems are caused because the view mode container is table based and the selectors of other controls (erroneously) interfere with the container's elements instead
			//of their own. Possible solution is to change view mode container's base element to something other than table and style appropriately
			//if (this.getCurrentDisplayMode() == '0') {
			//	var names = this.getSelectedNames();
			//	$(this.locateViewModeTextContainer()).html(names.join(', '));
			//}
		},

		getSelectedItems: function() {
			var vp = this.getSelectionValueProperty();
			var selValues = this.getSelectedValues();
			var items = [];
			for (var i = 0; i < selValues.length; i++) {
				var item = null;
				if (this.getSuggestionMode() == $.ui.CiteAutoSuggest.SuggestionMode.Static) {
					var staticSuggestions = this.getStaticSuggestions();
					for (var j = 0; j < staticSuggestions.length; j++) {
						if (staticSuggestions[j][vp] == selValues[i]) {
							item = staticSuggestions[j];
							break;
						}
					}
				}
				else {
					if (this.loadedItems[selValues[i]]) item = this.loadedItems[selValues[i]];
				}
				if (item != null)
					items.push(item);
			}
			return items;
		},

		selectItem: function(value, name, wholeItem) {
			var vProp = this.getSelectionValueProperty();
			var item = null;
			var doClear = false;
			if ((value === undefined) || (value === null) || (value.length == 0)) {
				doClear = true;
			}
			else {
				// If the suggestions are static, use the value in order to locate the item name.
				// Otherwise, use the supplied item name.
				if (this.getSuggestionMode() == $.ui.CiteAutoSuggest.SuggestionMode.Static) {
					var staticSuggestions = this.getStaticSuggestions();
					for (var j = 0; j < staticSuggestions.length; j++) {
						if (staticSuggestions[j][vProp] == value) {
							item = staticSuggestions[j];
							break;
						}
					}
					if (item == null) doClear = true;
				}
				else {
					if (wholeItem !== undefined)
						this.loadedItems[value] = wholeItem;
					if (this.loadedItems[value]) item = this.loadedItems[value];
					else {
						item = {};
						item[this.getSelectionNameProperty()] = name;
						item[vProp] = value;
					}
				}
			}

			if (doClear) {
				this.textbox.autoSuggest('option', 'setSelectedItems', []);
			}
			else {
				this.textbox.autoSuggest('option', 'setSelectedItems', this.formatSuggestions([item]));
			}

			this.updateSelectedItems();
		},

		selectItems: function(items) {
			if (!$.isArray(items)) items = [];
			this.textbox.autoSuggest('option', 'setSelectedItems', this.formatSuggestions(items));
			this.updateSelectedItems();

			if (this.getSuggestionMode() == $.ui.CiteAutoSuggest.SuggestionMode.Callback) {
				var vProp = this.getSelectionValueProperty();
				for (var i = 0; i < items.length; i++) {
					if (items[i][vProp])
						this.loadedItems[items[i][vProp]] = items[i];
				}
			}

			this.currentEditValueHash = this.getValueHash();
		},

		getStaticItemsByValues: function (values) {
			if (!$.isArray(values)) return [];
			var items = [];
			var vProp = this.getSelectionValueProperty();
			if (this.getSuggestionMode() == $.ui.CiteAutoSuggest.SuggestionMode.Static) {
				var staticSuggestions = this.getStaticSuggestions();
				for (var i = 0; i < values.length; i++) {
					var item = null;
					for (var j = 0; j < staticSuggestions.length; j++) {
						if (staticSuggestions[j][vProp] == values[i]) {
							item = staticSuggestions[j];
							break;
						}
					}
					if (item != null) items.push(item);
				}
			}
			return items;
		},

		selectFirstStaticItem: function() {
			if (this.getSuggestionMode() == $.ui.CiteAutoSuggest.SuggestionMode.Static) {
				var vp = this.getSelectionValueProperty();
				var staticSuggestions = this.getStaticSuggestions();
				var idx = -1;
				if (staticSuggestions.length > 0) idx = 0;
				if (this.getAddDropDownEmptyOption() && (staticSuggestions[idx][vp] === constants.EMPTY_OPTION_VALUE) && (staticSuggestions.length > 1)) idx = 1;
				if (idx != -1)
					this.selectItem(staticSuggestions[idx][vp]);
			}
		},

		clearSelection: function() {
			this.textbox.autoSuggest('option', 'setSelectedItems', []);
			this.setSelectedNames([]);
			this.setSelectedValues([]);
			this.updateDropDownIcon();
			this.updateAddIcon();
		},

		fireModificationEvent: function(added) {
			var event = jQuery.Event("selectionchanged");
			event.names = this.getSelectedNames();
			event.values = this.getSelectedValues();
			event.added = added;
			this.element.trigger(event);
		},
		fireTextModificationEvent: function () {
			var event = jQuery.Event('textChanged');
			this.element.trigger(event);
		},
		fireLiveModificationEvent: function() {
			var event = jQuery.Event('textlivechange');
			this.element.trigger(event);
		},

		setFocus: function() {
			var isDropDown = (this.getUiMode() == $.ui.CiteAutoSuggest.UIMode.DropDown);
			if (isDropDown)
				this.dropDownButtonInner.focus();
			else
				this.textbox.focus();
		},

		getPlainValue: function () {
			return this.textbox.val();
		},
		setPlainValue: function (value) {
			this.textbox.val(value);
		},

		_setRetrievePresentationPointSuggestionsCallback: function (value) {
			if (this.options.presentationPoint != null && value && value.length > 0) {
				var self = this;
				this.options.retrieveSuggestionsCallback = function (text, filters, limit, callback) {
					eval(value)(self.options.presentationPoint.name, text, filters, limit, callback);
				};
			} else {
				this._superApply(arguments);
			}
		},

		////////////////////////
		//
		// VALIDATION
		//
		////////////////////////
		getValidationEventName: function() {
			return "selectionchanged";
		},
		getValueForValidation: function() {
			return this.getSelectedValues();
		},
		hasValidValue: function() {
			var ret = false;
			if ((this.getSuggestionMode() == $.ui.CiteAutoSuggest.SuggestionMode.Static) || (this.getUiMode() == $.ui.CiteAutoSuggest.UIMode.DropDown)) // Static suggestion controls and dropdown controls always have a valid value
				ret = true;
			else {
				var txt = this.textbox.is(':visible') ? $.trim(this.textbox.val()) : null;
				if (txt == null || txt.length == 0) ret = true;
			}
			return ret;
		},

		////////////////////////
		//
		// CHANGES HANDLING
		//
		////////////////////////
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

			this.element.on('selectionchanged', handleChange);
			this.element.on('textlivechange', handleLiveChange);
			this.element.on('textChanged', function (e) { e.stopPropagation(); });
		},

		_doGetValueHash: function (items) {
			var hash = '';
			if (!this.options.presentationPoint) {
				if (this.getSelectionNameProperty() && this.getSelectionValueProperty()) {
					for (var i = 0; i < items.length; i++)
						hash += (i>0 ? '_' : '') + items[i][this.getSelectionNameProperty()] + '_' + items[i][this.getSelectionValueProperty()];
				}
				return hash;
			}
			var fields = this.options.presentationPoint.fields;
			for(var i=0; i<items.length; i++) {
				hash += (i>0 ? '_' : '') + items[i].ID;
				for(var j=0; j<fields.length; j++)
					hash += '_' + items[i][fields[j].Name];
			}
			return hash;
		},

		getValueHash: function (preselected) {
			if(!preselected)
				return this._doGetValueHash(this.getSelectedItems());
			return this._doGetValueHash(preselected);
		},
		////////////////////////
		//
		// VIEW MODE
		//
		////////////////////////
		//TODO change view mode container's base element to sth other than table
		locateViewModeContainer: function(create) {
			var cont = $('.viewmodecont', this.element)[0];
			if (!cont && create) {
				this.element.append('<table class="viewmodecont ui-state-default ui-corner-all"><tr><td><div class="viewmodetext"></div></td></tr></table>');
				cont = $('.viewmodecont', this.element)[0];
			}
			return cont;
		},
		locateViewModeTextContainer: function() {
			var cont = this.locateViewModeContainer(true);
			var tcont = $('.viewmodetext', cont)[0];
			return tcont;
		},
		setCurrentDisplayModeAndApply: function(displayMode) {
			this.setCurrentDisplayMode(displayMode);
			this.applyViewMode();
		},
		applyViewMode: function() {
			if (this.getCurrentDisplayMode() == "0") { //view
				var val = null;
				var names = this.getSelectedNames();
				$('.as-selections, button', this.element).hide();
				$(this.locateViewModeContainer(true)).show();
				$(this.locateViewModeTextContainer()).html(names.join(', '));
			}
			else if (this.getCurrentDisplayMode() == "1") {//edit
				$('.as-selections, button', this.element).show();
				$(this.locateViewModeContainer(false)).hide();
			}
		},

		//Destroying
		_destroy: function () {
			this.textbox.autoSuggest('option', 'destroy');
		}
	});

	$.ui.CiteAutoSuggest.UIMode = {
		TextBox: 0,
		DropDown: 1
	};
	
	$.ui.CiteAutoSuggest.SuggestionMode = {
		Static: 0,
		Callback: 1
	};
	
} (jQuery));
