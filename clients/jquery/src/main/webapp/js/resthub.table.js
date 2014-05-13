(function($){
     $.fn.extend({

        rhTable: function(options) {
            return this.each(function() {

                options = $.extend({
                            query: undefined,
                            params: undefined,
                            showColumns: 4,
                            tableOptions: undefined,
                            idColumn: undefined
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
                        aoColumns.push({ "sTitle": columnsArray[i].name });         
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
                                                var search = aoData[5].value;
                                                var columnsSQL = 'WHERE ';
                                                var isItnumb = !isNaN(search);

                                                var added = false;
                                                for (var i = 0; i < columns; i++) {
                                                    if (columnsArray[i].type === 'STRING') {
                                                        columnsSQL += (added ? 'OR ' : '') + 'a."' + columnsArray[i].name+'" like :search || \'%\' ';
                                                        added = true;
                                                    } else {
                                                        if (isItnumb && columnsArray[i].type === 'NUMBER') {
                                                            columnsSQL += (added ? 'OR ' : '') + 'a."' + columnsArray[i].name+'" = :search ';
                                                            added = true;
                                                        }
                                                    }
                                                }
                                                // ORDER BY
                                                var colNameNr = $.grep(aoData, function(e){ return e.name == "iSortCol_0"});
                                                var colName = columnsArray[colNameNr[0].value].name;
                                                
                                                var orderType = $.grep(aoData, function(e){ return e.name == "sSortDir_0"});
                                                var oderBy = ' ORDER BY a.' + colName + ' ' + orderType[0].value;
                                                if (options.idColumn !== undefined){
                                                    oderBy += ', a.' + options.idColumn + ' ' + orderType[0].value; 
                                                }
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
                                                        var dc = options.query.count({params: params});                 
                                                        var obj = {
                                                            iTotalDisplayRecords: dc,
                                                            iTotalRecords: dc, 
                                                            aaData: data.data,
                                                            sEcho: aoData[0].value,
                                                            sSearch: aoData[5].value,
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
