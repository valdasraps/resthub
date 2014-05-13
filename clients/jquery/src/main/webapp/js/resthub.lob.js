(function($){
     $.fn.extend({

        rhLoadHtml: function(options) {
            return this.each(function() {
                
                var $this = $(this);
                
                options = $.extend({
                        url: undefined,
                        params: {},
                        mt: "text/plain",
                        callBack: function (o) { }
                    }, options);

                $.ajax({
                    url: options.url,
                    async: true,
                    data: { mt: options.mt },
                    dataType: "text",
                    success: function (data) {
                        $this.html(data);
                        options.callBack($this);
                    }
                });

            });
        }       
        
    });
})(jQuery);

