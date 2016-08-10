angular.module('resthub', [])

.constant('ResthubApiUrl', 'http://localhost:2113')

.factory('resthub', function(ResthubApiUrl, $http) {
  return{

    /*
    options {
		sql   : "SELECT column FROM table"
		type  : "application/json"
		ppage : rows per page
		page  : page number
	}
    */
    query: function(options){

      var sql = "";

      if ('sql' in options){
        sql = "SELECT * FROM ( " + options.sql + " ) a;";
      }
      else{
        return Promise.reject('sql is not defined');
      }

      return $http({
        method : "POST",
        url : ResthubApiUrl + "/query",
        data : sql,
        headers: { 'Content-Type': 'text/plain' },
      })
      .then(function(response){
        console.log(response);

        if (response.status != 200){
          return Promise.reject('resthub status : ' + response.status);
        }

        // response.data == query_id
        var url = ResthubApiUrl + "/query/" + response.data;

        if (('ppage' in options) && ('page' in options)) {
          url += '/page/' + options.ppage + '/' + options.page;
        }

        url += "/data";

        if (!('type' in options)){
          options.type = "application/json"
        }
        console.log(options);

        return $http.get(url, { headers: { 'Accept' : options.type } } )
        .then(function(response){
          console.log(response.data.data);
          return response.data.data;
        },
        function(err){
          console.log("resthub error 1");
          return Promise.reject('resthub error 1');
        });

      },
    function(err){
      console.log("resthub error 2");
      return Promise.reject('Resthub error 2');
    });

    }
  }
}
);
