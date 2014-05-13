(function($){
     $.fn.extend({

        rhAutocomplete: function(options) {
            return this.each(function() {

                options = $.extend({
                        query: undefined,
                        params: {},
                        term: undefined,
                        page: 10
                    }, options);

                $(this)
                .bind("keydown", function( event ) {
                    if (event.keyCode === $.ui.keyCode.TAB && $(this).data("autocomplete").menu.active) {
                        event.preventDefault();
                    }
                })
                .autocomplete(
                { 
                    minLength: 0,
                    focus: function () { 
                        return false;
                    },
                    source: function(request, response) {
                        var params = {};
                        params[options.term] = request.term;
                        options.query.data({ 
                            ppage: options.page,
                            page: 1,
                            params: $.extend(params, options.params),
                            callBack: function(data) {
                                var a = [];
                                if(data.data.length < 1) {
                                    a.push({ value: "", label: "No data found" });
                                } else {
                                    a = $.map(data.data, function(item) {
                                        return item;
                                    });
                                }
                                response(a);
                            }
                        });
                    }
                });

            });
        }       
        
    });
})(jQuery);

