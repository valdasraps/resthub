(function($){
     $.fn.extend({

        rhTable: function(options) {
            return this.each(function() {

                options = $.extend({
                            query: undefined,
                            params: undefined,
                            showColumns: 4,
                            tableOptions: undefined
                        }, options);

                var Sthis = $(this);
                var oTable = Sthis.data("table");

                if (oTable === undefined) {

                    Sthis.empty();

                    // PARAMS
                    var params = '';
                    if (options.params !== undefined){
                        params = options.params;
                    }

                    // COLUMNS
                    var columnsArray = options.query.columns();
                    var showColumns = options.showColumns;
                    var columns = columnsArray.length;
                    
                    var aoColumns = [];
                    for (var i = 0; i < columns; i++) {                       
                        aoColumns.push( {"sTitle": columnsArray[i].name } );         
                        if ( i >= showColumns) {
                            aoColumns[i] = {"sTitle": columnsArray[i].name , bVisible: false}
                        }
                    }

                    // TABLE
                    var t = $('<table cellpadding="0" cellspacing="0" border="0" class="display"></table>');
                    Sthis.append(t);
                    var tableOptions = {
                        "sDom":             '<"H"lrCf>t<"F"ip>',
                        "sPaginationType": "full_numbers",
                        "bJQueryUI":        true,
                        "bProcessing":      true,
                        "bDeferRender":     true, 
                        "bServerSide":      true,
                        "bSort":            true,
                        "bFilter":          true,
                        "bStateSave":       true,
                        "aoColumns":        aoColumns,
                        "fnInitComplete":   function() {
                                                this.fnSetFilteringDelay(1000);
                                            },
                        "fnServerData":     function (sSource, aoData, fnCallback) {
                                                // WHERE
                                                var search = aoData[5 + columns].value;
                                                var columnsSQL = 'WHERE ';
                                                var isItnumb = !isNaN(search);

                                                for (var i = 0; i < columns; i++) {
                                                    if (columnsArray[i].type === 'STRING' || (isItnumb && columnsArray[i].type === 'NUMBER')){ 
                                                        if ( i !== 0) columnsSQL += 'OR ';
                                                        columnsSQL += 'a."'+columnsArray[i].name+'" = :search ';
                                                    }
                                                }
                                                // ORDER BY
                                                var columnVal = aoData[7 + columns * 4].value;
                                                var columnName = columnsArray[columnVal].name;
                                                var orderType = aoData[8 + columns * 4].value;
                                                var oderBy = ' ORDER BY a.' + columnName + ' ' + orderType; 
                                                
                                                options.query.sql = options.query.reset_sql;

                                                if ( search !== '') {
                                                    columnsSQL = columnsSQL.replace(/:search/g, '\''+search+'\'');
                                                    options.query.sql += columnsSQL;
                                                }
                                                options.query.sql += oderBy;
                                                options.query.refresh();
                                                                  
                                                options.query.data({
                                                    params: params, 
                                                    ppage: aoData[4].value,
                                                    page: Math.floor(aoData[3].value / aoData[4].value) + 1,
                                                    callBack: function(data){
                                                        var dc = options.query.count(params) !== undefined ? data.count : 0;
                                                        var obj = {
                                                            iTotalDisplayRecords: dc,
                                                            iTotalRecords: dc, 
                                                            aaData: data.data,
                                                            sEcho: aoData[0].value,
                                                            sSearch: aoData[5 + columns].value,
                                                            iDisplayStart: aoData[3].value
                                                        }
                                                        data = $.extend(data, obj);
                                                        fnCallback(data); 
                                                    }
                                                });
                                            }
                    };

                    tableOptions = $.extend(tableOptions, options.tableOptions);
                    oTable = t.dataTable(tableOptions); 
                    Sthis.data("table", oTable);
                } 

            });
        },

        rhTableRemove: function () {
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
