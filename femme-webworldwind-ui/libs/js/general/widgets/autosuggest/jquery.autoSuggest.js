/*
* jQuery scrollintoview() plugin and :scrollable selector filter
*
* Version 1.8 (14 Jul 2011)
* Requires jQuery 1.4 or newer
*
* Copyright (c) 2011 Robert Koritnik
* Licensed under the terms of the MIT license
* http://www.opensource.org/licenses/mit-license.php
*/
(function(f) { var c = { vertical: { x: false, y: true }, horizontal: { x: true, y: false }, both: { x: true, y: true }, x: { x: true, y: false }, y: { x: false, y: true} }; var b = { duration: "fast", direction: "both" }; var e = /^(?:html)$/i; var g = function(k, j) { j = j || (document.defaultView && document.defaultView.getComputedStyle ? document.defaultView.getComputedStyle(k, null) : k.currentStyle); var i = document.defaultView && document.defaultView.getComputedStyle ? true : false; var h = { top: (parseFloat(i ? j.borderTopWidth : f.css(k, "borderTopWidth")) || 0), left: (parseFloat(i ? j.borderLeftWidth : f.css(k, "borderLeftWidth")) || 0), bottom: (parseFloat(i ? j.borderBottomWidth : f.css(k, "borderBottomWidth")) || 0), right: (parseFloat(i ? j.borderRightWidth : f.css(k, "borderRightWidth")) || 0) }; return { top: h.top, left: h.left, bottom: h.bottom, right: h.right, vertical: h.top + h.bottom, horizontal: h.left + h.right} }; var d = function(h) { var j = f(window); var i = e.test(h[0].nodeName); return { border: i ? { top: 0, left: 0, bottom: 0, right: 0} : g(h[0]), scroll: { top: (i ? j : h).scrollTop(), left: (i ? j : h).scrollLeft() }, scrollbar: { right: i ? 0 : h.innerWidth() - h[0].clientWidth, bottom: i ? 0 : h.innerHeight() - h[0].clientHeight }, rect: (function() { var k = h[0].getBoundingClientRect(); return { top: i ? 0 : k.top, left: i ? 0 : k.left, bottom: i ? h[0].clientHeight : k.bottom, right: i ? h[0].clientWidth : k.right} })()} }; f.fn.extend({ scrollintoview: function(j) { j = f.extend({}, b, j); j.direction = c[typeof (j.direction) === "string" && j.direction.toLowerCase()] || c.both; var n = ""; if (j.direction.x === true) { n = "horizontal" } if (j.direction.y === true) { n = n ? "both" : "vertical" } var l = this.eq(0); var i = l.closest(":scrollable(" + n + ")"); if (i.length > 0) { i = i.eq(0); var m = { e: d(l), s: d(i) }; var h = { top: m.e.rect.top - (m.s.rect.top + m.s.border.top), bottom: m.s.rect.bottom - m.s.border.bottom - m.s.scrollbar.bottom - m.e.rect.bottom, left: m.e.rect.left - (m.s.rect.left + m.s.border.left), right: m.s.rect.right - m.s.border.right - m.s.scrollbar.right - m.e.rect.right }; var k = {}; if (j.direction.y === true) { if (h.top < 0) { k.scrollTop = m.s.scroll.top + h.top } else { if (h.top > 0 && h.bottom < 0) { k.scrollTop = m.s.scroll.top + Math.min(h.top, -h.bottom) } } } if (j.direction.x === true) { if (h.left < 0) { k.scrollLeft = m.s.scroll.left + h.left } else { if (h.left > 0 && h.right < 0) { k.scrollLeft = m.s.scroll.left + Math.min(h.left, -h.right) } } } if (!f.isEmptyObject(k)) { if (e.test(i[0].nodeName)) { i = f("html,body") } i.animate(k, j.duration).eq(0).queue(function(o) { f.isFunction(j.complete) && j.complete.call(i[0]); o() }) } else { f.isFunction(j.complete) && j.complete.call(i[0]) } } return this } }); var a = { auto: true, scroll: true, visible: false, hidden: false }; f.extend(f.expr[":"], { scrollable: function(k, i, n, h) { var m = c[typeof (n[3]) === "string" && n[3].toLowerCase()] || c.both; var l = (document.defaultView && document.defaultView.getComputedStyle ? document.defaultView.getComputedStyle(k, null) : k.currentStyle); var o = { x: a[l.overflowX.toLowerCase()] || false, y: a[l.overflowY.toLowerCase()] || false, isRoot: e.test(k.nodeName) }; if (!o.x && !o.y && !o.isRoot) { return false } var j = { height: { scroll: k.scrollHeight, client: k.clientHeight }, width: { scroll: k.scrollWidth, client: k.clientWidth }, scrollableX: function() { return (o.x || o.isRoot) && this.width.scroll > this.width.client }, scrollableY: function() { return (o.y || o.isRoot) && this.height.scroll > this.height.client } }; return m.y && j.scrollableY() || m.x && j.scrollableX() } }) })(jQuery);

 /*
 * AutoSuggest
 * Copyright 2009-2010 Drew Wilson
 * www.drewwilson.com
 * code.drewwilson.com/entry/autosuggest-jquery-plugin
 *
 * Version 1.4   -   Updated: Mar. 23, 2010
 *
 * This Plug-In will auto-complete or auto-suggest completed search queries
 * for you as you type. You can add multiple selections and remove them on
 * the fly. It supports keybord navigation (UP + DOWN + RETURN), as well
 * as multiple AutoSuggest fields on the same page.
 *
 * Inspied by the Autocomplete plugin by: Jšrn Zaefferer
 * and the Facelist plugin by: Ian Tearle (iantearle.com)
 *
 * This AutoSuggest jQuery plug-in is dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */

(function($) {
    var idcounter = 0;
    var clickDelegate = null;
    var lastClickedElement = null;

    $.fn.autoSuggest = function(data, options, param) {

        var AutoSuggest = function(element, index, data, options) {
            var defaults = {
                asHtmlID: false,
                startText: "Enter Name Here",
                emptyText: "No Results Found",
                preFill: {},
                limitText: "No More Selections Are Allowed",
                selectedItemProp: "value", //name of object property
                selectedValuesProp: "value", //name of object property
                searchObjProps: "value", //comma separated list of object property names
                queryParam: "q",
                retrieveLimit: false, //number for 'limit' param on ajax request
                extraParams: "",
                matchCase: false,
                minChars: 1,
                keyDelay: 400,
                resultsHighlight: true,
                neverSubmit: false,
                selectionLimit: false,
                minimumSelections: 0,
                showResultList: true,
                start: function() { },
                selectionClick: function(elem) { },
                selectionAdded: function(elem, name, value) { },
                selectionRemoved: function() { },
                formatList: false, //callback function
                beforeRetrieve: function(string) { return string; },
                retrieveComplete: function(data) { return data; },
                resultClick: function(data) { },
                resultsComplete: function() { },
                readOnly: false,
                allowOnlySuggestionSelections: false,
                maxSuggestionsWidth: 500,
                resultsListAppendTo: 'body',
                beforeShowSuggestions: null,
                beforeSelect: null
            };
            var opts = $.extend(defaults, options);
            var elem = $(element);  /* Reference to the input element used by the AutoSuggest */
            var obj = this; 		/* Reference to self */
            var blurTimeout = null;
            var results_holder = null;

            /********************************************************************
            Public function, initializes the AutoSuggest
            ********************************************************************/
            this.initialize = function() {
                // setup the global click delegate
                if (clickDelegate == null) {
                    clickDelegate = function(e) {
                        lastClickedElement = e.target;
                    };
                    $('html').bind('mousedown', clickDelegate);
                }

                var d_type = "object";
                var d_count = 0;
                if (typeof data == "string") {
                    d_type = "string";
                    var req_string = data;
                } else if (typeof data == "function") {
                    d_type = "function";
                    var req_function = data;
                } else {
                    var org_data = data;
                    for (k in data) if (data.hasOwnProperty(k)) d_count++;
                }

                var x = index;
                if (!opts.asHtmlID) {
                    //x = x + "" + Math.floor(Math.random() * 100); //this ensures there will be unique IDs on the page if autoSuggest() is called multiple times
                    x = x + "" + idcounter;
                    idcounter++;
                    var x_id = "as-input-" + x;
                } else {
                    x = opts.asHtmlID;
                    var x_id = x;
                }
                opts.start.call(this);
                var input = $(this);
                input.attr("autocomplete", "off").addClass("as-input").attr("id", x_id).val(opts.startText).addClass("as-watermark");
                if (opts.readOnly) input.prop('disabled', true).css('visibility', 'hidden');
                var input_focus = false;

                // Setup basic elements and render them to the DOM
            	//input.wrap('<ul class="form-control as-selections" id="as-selections-' + x + '"></ul>').wrap('<li class="as-original" id="as-original-' + x + '"></li>');
                input.wrap('<ul class="as-selections form-control" id="as-selections-' + x + '"></ul>').wrap('<li class="as-original" id="as-original-' + x + '"></li>');
                var selections_holder = $("#as-selections-" + x);
                var org_li = $("#as-original-" + x);
                results_holder = $('<div class="as-results" id="as-results-' + x + '"></div>').hide();
                var results_ul = $('<ul class="as-list"></ul>');
                var values_input = $('<input type="hidden" class="as-values" id="as-values-' + x + '" />');
                var prefill_value = "";
                if (typeof opts.preFill == "string") {
                    var vals = opts.preFill.split(",");
                    for (var i = 0; i < vals.length; i++) {
                        var v_data = {};
                        v_data[opts.selectedValuesProp] = vals[i];
                        if (vals[i] != "") {
                            add_selected_item(v_data, "000" + i, false);
                        }
                    }
                    prefill_value = opts.preFill;
                } else {
                    prefill_value = "";
                    var prefill_count = 0;
                    for (k in opts.preFill) if (opts.preFill.hasOwnProperty(k)) prefill_count++;
                    if (prefill_count > 0) {
                        for (var i = 0; i < prefill_count; i++) {
                            var new_v = opts.preFill[i][opts.selectedValuesProp];
                            if (new_v == undefined) { new_v = ""; }
                            prefill_value = prefill_value + new_v + ",";
                            if (new_v != "") {
                                add_selected_item(opts.preFill[i], "000" + i, false);
                            }
                        }
                    }
                }
                if (prefill_value != "") {
                    input.val("");
                    var lastChar = prefill_value.substring(prefill_value.length - 1);
                    if (lastChar != ",") { prefill_value = prefill_value + ","; }
                    values_input.val("," + prefill_value);
                    $("li.as-selection-item", selections_holder).addClass("blur").removeClass("selected");
                }
                else
                    values_input.val(",");
                input.after(values_input);
                selections_holder.click(function() {
                    input_focus = true;
                    input.focus();
                }).mousedown(function() { input_focus = false; });
                //}).mousedown(function() { input_focus = false; }).after(results_holder);

                var appendTo = input.closest(opts.resultsListAppendTo);
                if (appendTo.length == 0) appendTo = document.body;
                results_holder.appendTo(appendTo);

                var timeout = null;
                var prev = "";
                var totalSelections = 0;
                var tab_press = false;

                // Suggestions show
                var showSuggestions = function () {
                	if (opts.beforeShowSuggestions) {
                		results_holder.find('li').each(function () {
                			var raw_data = $(this).data("data");
                			opts.beforeShowSuggestions.call(this, raw_data ? raw_data.attributes : null);
                		});
                	}

                    var appendTo = input.closest(opts.resultsListAppendTo);
                    if (appendTo.length == 0) appendTo = document.body;
                    results_holder.appendTo(appendTo);

                    //var refElement = input.closest('.as-selections');
                    //var iOffset = refElement.offset();
                    //results_holder.show().offset({ 'left': iOffset.left, 'top': iOffset.top + refElement.outerHeight() }).css('z-index', Cite.Web.UI.ScriptControlBase.prototype.getMaxZIndex() + 1);
                    results_holder.show().offset({ 'left': 0, 'top': 0 }).css('z-index', $.ui.CiteBaseControl.getMaxZIndex() + 1).position({ my: 'left top', at: 'left bottom', of: selections_holder, collision: 'flip', within: appendTo });
                }

                // Handle input field events
                var keydownHandler = function(e, isSimulated) {
                    // track last key pressed
                    lastKeyPressCode = e.keyCode;
                    first_focus = false;
                    switch (e.keyCode) {
                        case 38: // up
                            if (!isSimulated) { e.preventDefault(); e.stopPropagation(); }
                            moveSelection("up");
                            return false;
                            break;
                        case 40: // down
                            if (!isSimulated) { e.preventDefault(); e.stopPropagation(); }
                            moveSelection("down");
                            return false;
                            break;
                        case 8:  // delete
                            if (input.val() == "") {
                                var last = values_input.val().split(",");
                                last = last[last.length - 2];
                                selections_holder.children().not(org_li.prev()).removeClass("selected");
                                if (org_li.prev().hasClass("selected")) {
                                    if (!opts.readOnly && !input.prop('disabled') && $("li.as-selection-item", selections_holder).length > opts.minimumSelections) {
                                        values_input.val(values_input.val().replace("," + last + ",", ","));
                                        org_li.prev().remove();
                                        if (opts.selectionLimit && $("li.as-selection-item", selections_holder).length < opts.selectionLimit) {
                                            input.show();
                                        }
                                        opts.selectionRemoved.call(this);
                                    }
                                } else {
                                    opts.selectionClick.call(this, org_li.prev());
                                    org_li.prev().addClass("selected");
                                }
                            }
                            if (input.val().length == 1) {
                                results_holder.hide();
                                prev = "";
                            }
                            if ($(":visible", results_holder).length > 0) {
                                if (timeout) { clearTimeout(timeout); }
                                timeout = setTimeout(function() { keyChange(); }, opts.keyDelay);
                            }
                            break;
                        /*
                        case 9: case 188:  // tab or comma
                        if (!opts.allowOnlySuggestionSelections) {
                        tab_press = true;
                        var i_input = input.val().replace(/(,)/g, "");
                        if (i_input != "" && values_input.val().search("," + i_input + ",") < 0 && i_input.length >= opts.minChars) {
                        if (!isSimulated) e.preventDefault();
                        var n_data = {};
                        n_data[opts.selectedItemProp] = i_input;
                        n_data[opts.selectedValuesProp] = i_input;
                        var lis = $("li", selections_holder).length;
                        add_selected_item(n_data, "00" + (lis + 1));
                        input.val("");
                        }
                        }
                        */ 
                        case 13: // return
                            tab_press = false;
                            var active = $("li.active:first", results_holder);
                            if (active.length > 0) {
                                active.click();
                                setTimeout(function() { results_holder.hide(); }, 300);
                            }
                            if (opts.neverSubmit || active.length > 0) {
                                if (!isSimulated) e.preventDefault();
                            }
                            break;
                        default:
                            if (!isSimulated) {
                                if (opts.showResultList) {
                                    if (opts.selectionLimit && $("li.as-selection-item", selections_holder).length >= opts.selectionLimit) {
                                        results_ul.html('<li class="as-message">' + opts.limitText + '</li>');
                                        showSuggestions();
                                    } else {
                                        if (timeout) { clearTimeout(timeout); }
                                        timeout = setTimeout(function() { keyChange(); }, opts.keyDelay);
                                    }
                                }
                            }
                            break;
                    }
                };
                input.focus(function() {
                    if ($(this).val() == opts.startText && (values_input.val() == "" || values_input.val() == ",")) {
                        $(this).val("").removeClass("as-watermark");
                    } else if (input_focus) {
                        $("li.as-selection-item", selections_holder).removeClass("blur");
                        if ($(this).val() != "") {
                            results_ul.css("min-width", selections_holder.outerWidth());
                            results_ul.css("max-width", Math.max(selections_holder.outerWidth(), opts.maxSuggestionsWidth));
                            showSuggestions();
                        }
                    }
                    input_focus = true;
                    return true;
                }).blur(function(e) {
                    var s = this;
                    if (blurTimeout != null) clearTimeout(blurTimeout);
                    blurTimeout = setTimeout(function() {
                        if (results_holder.find("*").addBack().filter(lastClickedElement).length > 0) {
                            input.focus();
                            return;
                        }
                        if ($(s).val() == "" && (values_input.val() == "" || values_input.val() == ",") && prefill_value == "") {
                            $(s).val(opts.startText).addClass("as-watermark");
                        }
                        if (input_focus) {
                            $("li.as-selection-item", selections_holder).addClass("blur").removeClass("selected");
                            results_holder.hide();
                        }
                        blurTimeout = null;
                    }, 300);
                }).keydown(function(e) { keydownHandler(e, false); });

                elem.bind('opensuggestions', function(e) {
                    if (opts.selectionLimit && (opts.selectionLimit > 1) && ($("li.as-selection-item", selections_holder).length >= opts.selectionLimit)) {
                        results_ul.html('<li class="as-message">' + opts.limitText + '</li>');
                        showSuggestions();
                    } else {
                        lastKeyPressCode = 0;
                        keyChange(true, e.completeCallback);
                    }
                    input_focus = false; // THIS IS A PATCH IN ORDER NOT TO HIDE THE SUGGESTIONS ON TEXTBOX BLUR WHEN CLICKING ON THE DROPDOWN BUTTON
                });
                elem.bind('closesuggestions', function() {
                    if (results_holder.css('display') == 'block') {
                        results_holder.hide();
                    }
                });
                elem.bind('togglesuggestions', function() {
                    if (results_holder.css('display') == 'block')
                        results_holder.hide();
                    else {
                        elem.trigger('opensuggestions');
                    }
                });
                elem.bind('simulateKeydown', function(e) {
                    keydownHandler({ 'keyCode': e.keyCode }, true);
                });
                elem.bind('selectFirst', function(e) {
                    var lis = $("li", results_holder);
                    var target = null;
                    for (var i = 0; i < lis.length; i++) {
                        var h = lis[i].innerHTML;
                        if (h.length > 0 && h[0].toLowerCase() === e.firstLetter) {
                            target = $(lis[i]);
                            lis.removeClass("active");
                            target.addClass("active");
                            break;
                        }
                    }

                    if (target != null) {
                        target.scrollintoview({
                            duration: 300,
                            direction: "vertical"
                        });
                    }
                });
                elem.bind('setSelectedItems', function(e) {
                    var items = e.items;

                    // clear current selections
                    values_input.val(",");
                    $("li.as-selection-item", selections_holder).remove();
                    input.show();
                    input.val('');
                    prev = '';

                    // add the new items
                    if (items.length > 0) {
                        for (var i = 0; i < items.length; i++) {
                            add_selected_item(items[i], "000" + i, false);
                        }
                    }
                    else {
                        input.val(opts.startText).addClass("as-watermark");
                    }
                });
                elem.bind('setItems', function(e) {
                    d_type = "object";
                    d_count = 0;
                    if (typeof e.newdata == "string") {
                        d_type = "string";
                        req_string = e.newdata;
                    } else if (typeof e.newdata == "function") {
                        d_type = "function";
                        req_function = e.newdata;
                    } else {
                        org_data = e.newdata;
                        for (k in e.newdata) if (e.newdata.hasOwnProperty(k)) d_count++;
                    }
                });
                elem.bind('setOptions', function(e) {
                    opts = $.extend(opts, e.options);
                });


                function keyChange(forceOpen, completeCallback) {
                    // ignore if the following keys are pressed: [del] [shift] [capslock]
                    if (lastKeyPressCode == 46 || (lastKeyPressCode > 8 && lastKeyPressCode < 32)) { return results_holder.hide(); }
                    var string = input.val().replace(/[\\]+|[\/]+/g, "");
                    if ((string == prev) && !forceOpen) return;
                    prev = string;
                    if ((string.length >= opts.minChars) || forceOpen) {
                        //selections_holder.addClass("loading");
                        if (d_type == "string") {
                            var limit = "";
                            if (opts.retrieveLimit) {
                                limit = "&limit=" + encodeURIComponent(opts.retrieveLimit);
                            }
                            if (opts.beforeRetrieve) {
                                string = opts.beforeRetrieve.call(this, string);
                            }
                            $.getJSON(req_string + "?" + opts.queryParam + "=" + encodeURIComponent(string) + limit + opts.extraParams, function(data) {
                                d_count = 0;
                                var new_data = opts.retrieveComplete.call(this, data);
                                for (k in new_data) if (new_data.hasOwnProperty(k)) d_count++;
                                processData(new_data, string);
                                if (completeCallback) completeCallback();
                            });
                        } else if (d_type == "function") {
                            if (opts.beforeRetrieve) {
                                string = opts.beforeRetrieve.call(this, string);
                            }

                            req_function(string, opts.retrieveLimit, function(data) {
                                d_count = 0;
                                var new_data = opts.retrieveComplete.call(this, data);
                                for (k in new_data) if (new_data.hasOwnProperty(k)) d_count++;
                                processData(new_data, string);
                                if (completeCallback) completeCallback();
                            });
                        } else {
                            if (opts.beforeRetrieve) {
                                string = opts.beforeRetrieve.call(this, string);
                            }
                            processData(org_data, string);
                            if (completeCallback) completeCallback();
                        }
                    } else {
                        selections_holder.removeClass("loading");
                        results_holder.hide();
                    }
                }
                var num_count = 0;
                function processData(data, query) {
                    if (!opts.matchCase) { query = query.toLowerCase(); }
                    var matchCount = 0;
                    results_holder.html(results_ul.html("")).hide();
                    for (var i = 0; i < d_count; i++) {
                        var num = i;
                        num_count++;
                        var forward = false;
                        if (opts.searchObjProps == "value") {
                            var str = data[num].value;
                        } else {
                            var str = "";
                            var names = opts.searchObjProps.split(",");
                            for (var y = 0; y < names.length; y++) {
                                var name = $.trim(names[y]);
                                str = str + data[num][name] + " ";
                            }
                        }
                        if (str) {
                            if (!opts.matchCase) { str = str.toLowerCase(); }
                            if (str.indexOf('*') == -1 || str.indexOf('%') == -1) {
                                forward = true;
                            }
                            else if (str.search(query) != -1 && values_input.val().search("," + data[num][opts.selectedValuesProp] + ",") == -1) {
                                forward = true;
                            }
                        }
                        if (forward) {
                            var formatted = $('<li class="as-result-item" id="as-result-item-' + num + '"></li>').click(function(e) {
                                var raw_data = $(this).data("data");
                                var number = raw_data.num;
                                if ($("#as-selection-" + number, selections_holder).length <= 0 && !tab_press) {
                                    var data = raw_data.attributes;
                                    input.val("").focus();
                                    prev = "";

                                    if (opts.beforeSelect) {
                                    	if (opts.beforeSelect.call(this, data, e) === false) return;
                                    }

                                    // If multiple selection is allowed and the item being selected is already in the list,
                                    // abort the procedure (it should not be selected twice)
                                    if (opts.selectionLimit !== 1) {
                                        if ($.inArray(data[opts.selectedValuesProp], values_input.val().split(',')) !== -1)
                                            return;
                                    }

                                    // If only one selection is allowed, and the selection list contains one item, remove it before
                                    // adding the new selection, so that a replacement is made.
                                    if (opts.selectionLimit && (opts.selectionLimit == 1) && ($("li.as-selection-item", selections_holder).length == 1)) {
                                        values_input.val(",");
                                        $("li.as-selection-item", selections_holder).remove();
                                    }

                                    add_selected_item(data, number);
                                    opts.resultClick.call(this, raw_data);
                                    results_holder.hide();
                                    var selected = $("li.as-selection-item", selections_holder);
                                    if (selected.length > 0) {
                                        $(selected[0]).focus();
                                    }
                                }
                                tab_press = false;
                            }).mousedown(function() { input_focus = false; }).data("data", { attributes: data[num], num: num_count });
                            var this_data = $.extend({}, data[num]);

                            if (opts.resultsHighlight && (query.length > 0) && (query.indexOf('*') == -1) && (query.indexOf('%') == -1)) {
                                if (!opts.matchCase) {
                                    var regx = new RegExp("(?![^&;]+;)(?!<[^<>]*)(" + query + ")(?![^<>]*>)(?![^&;]+;)", "gi");
                                } else {
                                    var regx = new RegExp("(?![^&;]+;)(?!<[^<>]*)(" + query + ")(?![^<>]*>)(?![^&;]+;)", "g");
                                }
                                this_data[opts.selectedItemProp] = this_data[opts.selectedItemProp].replace(regx, "<em>$1</em>");
                            }
                            if (!opts.formatList) {
                                formatted = formatted.html(this_data[opts.selectedItemProp]);
                            } else {
                                formatted = opts.formatList.call(this, this_data, formatted);
                            }
                            results_ul.append(formatted);
                            delete this_data;
                            matchCount++;
                            if (opts.retrieveLimit && opts.retrieveLimit == matchCount) { break; }
                        }
                    }
                    selections_holder.removeClass("loading");
                    if (matchCount <= 0) {
                        results_ul.html('<li class="as-message">' + opts.emptyText + '</li>');
                    }
                    results_ul.css("min-width", selections_holder.outerWidth());
                    results_ul.css("max-width", Math.max(selections_holder.outerWidth(), opts.maxSuggestionsWidth));
                    showSuggestions();
                    opts.resultsComplete.call(this);
                }

                function add_selected_item(data, num, invokeAddCallback) {
                    values_input.val(values_input.val() + data[opts.selectedValuesProp] + ",");
                    var item = $('<li class="as-selection-item" id="as-selection-' + num + '" tabindex="0"></li>').click(function() {
                        opts.selectionClick.call(this, $(this));
                        selections_holder.children().removeClass("selected");
                        $(this).addClass("selected");
                    }).mousedown(function() { input_focus = false; });
                    if (!opts.readOnly) {
                        var close = $('<a class="as-close">&times;</a>').click(function() {
                            if (!opts.readOnly && !input.prop('disabled') && $("li.as-selection-item", selections_holder).length > opts.minimumSelections) {
                                values_input.val(values_input.val().replace("," + data[opts.selectedValuesProp] + ",", ","));
                                item.remove();
                                if (opts.selectionLimit && $("li.as-selection-item", selections_holder).length < opts.selectionLimit) {
                                    input.show();
                                }
                                input_focus = true;
                                input.focus();
                                opts.selectionRemoved.call(this);
                            }
                            return false;
                        });
                    }
                    else
                    	var close = '<span class="as-noclose"></span>';
                    org_li.before(item.html(data[opts.selectedItemProp]).prepend(close));

                    if (opts.selectionLimit && $("li.as-selection-item", selections_holder).length >= opts.selectionLimit) {
                        input.hide();
                    }

                    if ((invokeAddCallback === undefined) || (invokeAddCallback === true)) {
                        opts.selectionAdded.call(this, org_li.prev(), data[opts.selectedItemProp], data[opts.selectedValuesProp]);
                    }
                }

                function moveSelection(direction) {
                    if ($(":visible", results_holder).length > 0) {
                        var lis = $("li", results_holder);
                        if (direction == "down") {
                            var start = lis.eq(0);
                        } else {
                            var start = lis.filter(":last");
                        }
                        var active = $("li.active:first", results_holder);
                        if (active.length > 0) {
                            if (direction == "down") {
                                start = active.next();
                            } else {
                                start = active.prev();
                            }
                        }
                        lis.removeClass("active");
                        start.addClass("active");

                        if (start.length > 0) {
                            start.scrollintoview({
                                duration: 300,
                                direction: "vertical"
                            });
                        }
                    }
                    else
                        elem.trigger('opensuggestions');
                }
            };

            /********************************************************************
            Public function, checks if the suggestions list is visible or not
            ********************************************************************/
            this.lastClickInSuggestions = function() {
                return (results_holder.find("*").addBack().filter(lastClickedElement).length > 0);
            };

            /********************************************************************
            Public function, checks if the suggestions list is visible or not
            ********************************************************************/
            this.areSuggestionsOpen = function() {
                //return (elem.closest('.as-selections').siblings('.as-results').css('display') == 'block');
                return (results_holder.css('display') == 'block');
            };

            /********************************************************************
            Public function, returns the array of currently selected values
            ********************************************************************/
            this.getSelectedValues = function() {
                var values = elem.siblings('input[type="hidden"]').val().split(',')
                values.splice(0, 1);
                if (values.length >= 1)
                    values.splice(values.length - 1, 1);
                return values;
            };

            /********************************************************************
            Public function, returns the array of currently selected names
            ********************************************************************/
            this.getSelectedNames = function() {
                var names = [];
                elem.closest('li').siblings('li').each(function () {
                	var clone = $(this).clone();
                	clone.children('.as-close').remove();
                	clone.children('.as-noclose').remove();
                	names.push(clone.html());
                });
                return names;
            };

            this.destroy = function () {
            	results_holder.remove();
            };

            obj.initialize.call(element);
        }


        /* 	AutoSuggest construction entry-point. For each selected input element:
        - Check if an AutoSuggest already exists for the element
        - Create an 'AutoSuggest' instance
        - Store the instance as custom jQuery data of the input element
        */
        if ((typeof (data) != 'string') || (data != 'option')) {	// instance construction
            return this.each(function(index) {
                var element = $(this);
                if (element.data('autosuggest')) return;
                var o = new AutoSuggest(this, index, data, options);
                element.data('autosuggest', o);
            });
        }
        else {		// method call on existing instance
            var element = $(this);
            var innerObj = element.data('autosuggest');
            if (!innerObj)
                return undefined;

            if (options == 'selectedValues')
                return innerObj.getSelectedValues();
            else if (options == 'selectedNames')
                return innerObj.getSelectedNames();
            else if (options == 'isOpen')
                return innerObj.areSuggestionsOpen();
            else if (options == 'open') {
                var event = jQuery.Event("opensuggestions");
                event.completeCallback = param;
                element.trigger(event);
            }
            else if (options == 'close') {
                element.trigger('closesuggestions');
            }
            else if (options == 'toggle') {
                element.trigger('togglesuggestions');
            }
            else if (options == 'keydown') {
                var event = jQuery.Event("simulateKeydown");
                event.keyCode = param;
                element.trigger(event);
            }
            else if (options == 'setSelectedItems') {
                var event = jQuery.Event("setSelectedItems");
                event.items = param;
                element.trigger(event);
            }
            else if (options == 'selectFirst') {
                var event = jQuery.Event("selectFirst");
                event.firstLetter = param;
                element.trigger(event);
            }
            else if (options == 'setItems') {
                var event = jQuery.Event("setItems");
                event.newdata = param;
                element.trigger(event);
            }
            else if (options == 'setOptions') {
                var event = jQuery.Event("setOptions");
                event.options = param;
                element.trigger(event);
            }
            else if (options == 'lastClickInSuggestions') {
                return innerObj.lastClickInSuggestions();
            }
            else if (options == 'destroy') {
            	innerObj.destroy();
            }
        }
    };
})(jQuery);  	