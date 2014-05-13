(function($){
     $.fn.extend({

        rhChart1: function(options) {
            return this.each(function() {

                Highcharts.getOptions().colors = Highcharts.map(Highcharts.getOptions().colors, function(color) {
                    return {
                        radialGradient: { cx: 0.5, cy: 0.3, r: 0.7 },
                        stops: [
                            [0, color],
                            [1, Highcharts.Color(color).brighten(-0.3).get('rgb')] // darken
                        ]
                    };
                });

                options = $.extend({
                            query: undefined,
                            chart: {
                                plotBackgroundColor: null,
                                plotBorderWidth: null,
                                plotShadow: false
                            },
                            title: {
                                text: 'Brands pie chart of first 100 products'
                            },
                            tooltip: {
                                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
                            },
                            plotOptions: {
                                pie: {
                                    allowPointSelect: true,
                                    cursor: 'pointer',
                                    dataLabels: {
                                        enabled: true,
                                        color: '#000000',
                                        connectorColor: '#000000',
                                        format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                                    }
                                }
                            },
                            series: [{
                                type: 'pie',
                                name: 'Brand share',
                                data: []
                            }]

                        }, options);

                var Sthis = $(this);

                if (options.query !== undefined){

                    var data = options.query.data({ ppage: 100, page:1}).data;
                    var array = [];
                    var counter = 0; 
                    var temp1 = data[0][1];

                    for ( var i = 0; i < data.length; i ++) {
                        var temp2 = data[i][1];
                        if (temp1 !== temp2){
                            array.push([temp1, counter]);
                            counter = 0;
                            temp1 = temp2;
                        }
                        counter++;
                    }
                    options.series[0].data = array;
                }

                Sthis.highcharts(options);

            });
        },
        
        /*** CHART 2 ***/

        rhChart2: function(options) {
            return this.each(function() {

                options = $.extend({
                            query: undefined,
                            params: undefined,

                            chart: {
                                type: 'bar'
                            },
                            title: {
                                text: 'Countries'
                            },
                            xAxis: {
                                categories: ['Africa', 'America', 'Asia', 'Europe', 'Oceania'],
                                title: {
                                    text: null
                                }
                            },
                            yAxis: {
                                min: 0,
                                labels: {
                                    overflow: 'justify'
                                }
                            },
                            tooltip: {
                                valueSuffix: ' millions'
                            },
                            plotOptions: {
                                bar: {
                                    dataLabels: {
                                        enabled: true
                                    }
                                }
                            },
                            legend: {
                                layout: 'vertical',
                                align: 'right',
                                verticalAlign: 'top',
                                x: -40,
                                y: 100,
                                floating: true,
                                borderWidth: 1,
                                backgroundColor: '#FFFFFF',
                                shadow: true
                            },
                            credits: {
                                enabled: false
                            },
                            series: [{
                                name: 'Countries',
                                data: [107, 31, 635]
                            }]

                        }, options);

                // PARAMS
                var params = '';
                if (options.params !== undefined){
                    params = options.params;
                }

                var Sthis = $(this);

                if (options.query !== undefined){

                    var data = options.query.data({ params: params}).data;

                    // get names of countries
                    var countries = [];
                    for ( var i = 0; i < data.length; i ++) {
                        for ( var j = 0; j < data.length; j ++) {
                            var temp = true;
                            for ( var k = 0; k < countries.length; k ++) {
                                if (data[j][3] === countries[k])
                                    temp = false;
                            }
                            if (temp) countries.push(data[j][3]);
                        }
                    }
                    // get values
                    var values = []; var counter = 0;
                    for ( var i = 0; i < countries.length; i ++) {
                        for ( var j = 0; j < data.length; j ++) {
                            if (countries[i] === data[j][3]) counter++
                        }
                        values.push(counter); counter = 0;
                    }

                    options.xAxis.categories = countries;
                    options.series[0].data = values;
                }

                Sthis.highcharts(options);

            });
        },

        /*** CHART 3 ***/

        rhChart3: function(options) {
            return this.each(function() {

                Highcharts.getOptions().colors = Highcharts.map(Highcharts.getOptions().colors, function(color) {
                    return {
                        radialGradient: { cx: 0.5, cy: 0.3, r: 0.7 },
                        stops: [
                            [0, color],
                            [1, Highcharts.Color(color).brighten(-0.3).get('rgb')] // darken
                        ]
                    };
                });

                options = $.extend({
                            query: undefined,
                            chart: {
                                type: 'column'
                            },
                            title: {
                                text: ''
                            },
                            xAxis: {},
                            yAxis: {
                                min: 0,
                                title: {
                                    text: 'Money (k)'
                                }
                            },
                            tooltip: {
                                headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
                                pointFormat: '<tr><td style="color:{series.color};padding:0">Spent: </td>' +
                                    '<td style="padding:0"><b>{point.y:.1f} k</b></td></tr>',
                                footerFormat: '</table>',
                                shared: true,
                                useHTML: true
                            },
                            plotOptions: {
                                column: {
                                    pointPadding: 0.2,
                                    borderWidth: 0
                                }
                            },
                            series: [{
                                name: 'Brands',
                                data: [49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]
                                    }]

                        }, options);

                var Sthis = $(this);

                // PARAMS
                var params = '';
                if (options.params !== undefined){
                    params = options.params;
                }

                if (options.query !== undefined){

                    var data = options.query.data({params: params, ppage: 50, page:1}).data;

                    // get brand names
                    var brands = [];
                    var temp1 = data[0][1];

                    for ( var i = 0; i < data.length; i ++) {
                        var temp2 = data[i][1];
                        if (temp1 !== temp2){
                            brands.push([temp1]);
                            temp1 = temp2;
                        }
                    }
                    // get costs
                    var costs = []; var counter = 0;

                    for ( var i = 0; i < brands.length; i ++) {
                        counter = 0;
                        for ( var j = 0; j < data.length; j++) {
                            if (brands[i] == data[j][1]) counter += data[j][0];
                        }
                        costs.push(counter);
                    }

                    options.xAxis.categories = brands;
                    options.series[0].data = costs;
                }

                Sthis.highcharts(options);

            });
        },

    });
})(jQuery);
