(function($){
     $.fn.extend({
         
        jsonTable: function(url, data, options) {
            return this.each(function() {

                var Sthis = $(this);
                
                var oTable = Sthis.data("table");
                if (oTable === undefined) {

                    $.ajax({
                        type:     "POST",
                        dataType: "json",
                        url: url + (url.indexOf("?") >= 0 ? "&" : "?") + "ajax.structure=true",
                        data: data,
                        success: function (columns) {
                        
                            var defaults = {
                                "sDom":             '<"H"lrCf>t<"F"ip>',
                                "bJQueryUI":        true,
                                "bFilter":          true,
                                "bProcessing":      true,
                                "bServerSide":      true,
                                "bSort":            true,
                                "bSortClasses":     false,
                                "sPaginationType":  "full_numbers",
                                "bAutoWidth":       false,
                                //"sScrollX":         "100%",
                                "iDisplayLength":   10,
                                "sAjaxSource":      url,
                                "fnServerData":     function (sSource, aoData, fnCallback) {
                                    
                                                        if (data) {
                                                            $.each(data, function (k,v) {
                                                                aoData.push({"name": k, "value": v});
                                                            });
                                                        }
                                                        
                                                        this.sAjaxUrl = sSource;
                                                        this.oAjaxData = aoData;
                                                        
                                                        $.ajax({
                                                            "dataType": "json",
                                                            "type":     "POST",
                                                            "url":      sSource,
                                                            "data":     aoData,
                                                            "success":  fnCallback
                                                        });
                                                        
                                                    },
                                "aLengthMenu":      [[10, 20, 30, 40, 50, -1], [10, 20, 30, 40, 50, "All"]],
                                "oLanguage": {
                                    "sSearch": "Filter:"
                                }
                            };

                            if (columns.length > 5) {
                                var hideCols = [];
                                for (var i = 5; i < columns.length; i++) {
                                    hideCols.push(i);
                                }
                                defaults["aoColumnDefs"] = [ { "bVisible": false, "aTargets": hideCols } ];
                                defaults["oColVis"] = { 
                                    "sAlign":     "left",
                                    "aiExclude":  [ 0 ],
                                    "sSize":      "css",
                                    "buttonText": "Columns"
                                };
                            }

                            var toptions = $.extend(defaults, options);

                            //alert($.toJSON(toptions));

                            Sthis.empty();

                            var tbl = $("<table class=\"display\" style=\"font-size: 13px\"/>").appendTo(Sthis);
                            var thd = $("<thead/>").appendTo(tbl);
                            var row = $("<tr/>").appendTo(thd);

                            $.each(columns, function (i, o) {
                                $("<th>" + o.name + "</th>").appendTo(row);
                            });

                            $("<tbody/>").appendTo(tbl);

                            oTable = tbl.dataTable(toptions);
                            oTable.fnSetFilteringEnterPress();
                            
                            var expBtn = $("<button class=\"ColVis_Button TableTools_Button ui-button ui-state-default ColVis_MasterButton\" title=\"Export first 1000 rows\"><span>Export</span></button>");
                            expBtn.click($.proxy(function(event) { 
                                event.preventDefault();

                                var f = $("<form action=\"" + this.sAjaxUrl + "\" method=\"post\"/>");
                                $.each(this.oAjaxData, function (k, e) {
                                    var k = e.name;
                                    var v = e.value;
                                    if (k === 'iDisplayLength') {
                                        v = 1000;
                                    }
                                    if ($.isArray(v) || $.isPlainObject(v)) {
                                        v = $.toJSON(v);
                                    }
                                    f.append($("<input type=\"hidden\" name=\"" + k + "\"/>").val(v));
                                });
                                f.append("<input type=\"hidden\" name=\"ajax.download\" value=\"true\"/>");
                                $("body").append(f);
                                f.submit();
                                f.remove();
                                
                            }, oTable));
                            $(".ColVis", Sthis).append(expBtn);

                            Sthis.data("table", oTable);

                            tbl.show();
                            
                        }       
                    });

                } else {
                    
                    oTable.fnReloadAjax(url);
                    
                }

            });
        },
        
        jsonTableRemove: function () {
            var Sthis = $(this);
            var oTable = Sthis.data("table");
            if (oTable !== undefined) {
                oTable.fnDestroy();
                Sthis.removeData("table");
            }
            Sthis.empty();
            return Sthis;
        }
        
    });
})(jQuery);
